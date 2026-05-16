package com.connectsphere.auth.service.impl;

import com.connectsphere.auth.dto.AuthResponse;
import com.connectsphere.auth.dto.ChangePasswordRequest;
import com.connectsphere.auth.dto.ForgotPasswordRequest;
import com.connectsphere.auth.dto.GitHubAccessTokenResponse;
import com.connectsphere.auth.dto.GitHubAuthRequest;
import com.connectsphere.auth.dto.GitHubEmailResponse;
import com.connectsphere.auth.dto.GitHubUserResponse;
import com.connectsphere.auth.dto.GoogleAuthRequest;
import com.connectsphere.auth.dto.GoogleTokenInfoResponse;
import com.connectsphere.auth.dto.LoginRequest;
import com.connectsphere.auth.dto.RegisterRequest;
import com.connectsphere.auth.dto.UpdateProfileRequest;
import com.connectsphere.auth.dto.UserProfileResponse;
import com.connectsphere.auth.dto.UserStatsResponse;
import com.connectsphere.auth.entity.User;
import com.connectsphere.auth.exception.InvalidCredentialsException;
import com.connectsphere.auth.exception.ResourceNotFoundException;
import com.connectsphere.auth.exception.UserAlreadyExistsException;
import com.connectsphere.auth.repository.UserRepository;
import com.connectsphere.auth.security.JwtService;
import com.connectsphere.auth.service.AuthService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final ObjectMapper objectMapper;

    @Value("${google.oauth.client-id:}")
    private String googleClientId;

    @Value("${github.oauth.client-id:}")
    private String githubClientId;

    
    @Value("${github.oauth.client-secret:}")
    private String githubClientSecret;

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email already registered");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("Username already taken");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .role("USER")
                .provider("LOCAL")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        User savedUser = userRepository.save(user);

        return AuthResponse.builder()
                .message("User registered successfully")
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .role(savedUser.getRole())
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        if (!user.isActive()) {
            throw new InvalidCredentialsException("User account is inactive");
        }

        return buildAuthResponse(user, "Login successful");
    }

    @Override
    public AuthResponse loginWithGoogle(GoogleAuthRequest request) {
        if (googleClientId == null || googleClientId.isBlank()) {
            throw new InvalidCredentialsException("Google login is not configured on the server");
        }

        GoogleTokenInfoResponse tokenInfo = verifyGoogleToken(request.getCredential());
        if (!googleClientId.equals(tokenInfo.getAud())) {
            throw new InvalidCredentialsException("Google token audience mismatch");
        }

        if (!Boolean.parseBoolean(tokenInfo.getEmailVerified())) {
            throw new InvalidCredentialsException("Google account email is not verified");
        }

        if (tokenInfo.getEmail() == null || tokenInfo.getEmail().isBlank()) {
            throw new InvalidCredentialsException("Google account email is missing");
        }

        User user = userRepository.findByEmail(tokenInfo.getEmail())
                .map(existingUser -> updateGoogleUser(existingUser, tokenInfo))
                .orElseGet(() -> createGoogleUser(tokenInfo));

        if (!user.isActive()) {
            throw new InvalidCredentialsException("User account is inactive");
        }

        return buildAuthResponse(user, "Google login successful");
    }

    @Override
    public AuthResponse loginWithGitHub(GitHubAuthRequest request) {
        if (githubClientId == null || githubClientId.isBlank() || githubClientSecret == null || githubClientSecret.isBlank()) {
            throw new InvalidCredentialsException("GitHub login is not configured on the server");
        }

        GitHubAccessTokenResponse tokenResponse = exchangeGitHubCode(request);
        if (tokenResponse.getAccessToken() == null || tokenResponse.getAccessToken().isBlank()) {
            throw new InvalidCredentialsException("GitHub access token exchange failed");
        }

        GitHubUserResponse githubUser = fetchGitHubUser(tokenResponse.getAccessToken());
        String email = resolveGitHubEmail(tokenResponse.getAccessToken(), githubUser);
        if (email == null || email.isBlank()) {
            throw new InvalidCredentialsException("GitHub account email is missing or not verified");
        }

        User user = userRepository.findByEmail(email)
                .map(existingUser -> updateGitHubUser(existingUser, githubUser, email))
                .orElseGet(() -> createGitHubUser(githubUser, email));

        if (!user.isActive()) {
            throw new InvalidCredentialsException("User account is inactive");
        }

        return buildAuthResponse(user, "GitHub login successful");
    }

    @Override
    public void logout(String email) {
        getActiveUserByEmail(email);
    }

    @Override
    public AuthResponse refreshToken(String email) {
        User user = getActiveUserByEmail(email);
        return buildAuthResponse(user, "Token refreshed successfully");
    }

    @Override
    public UserProfileResponse getProfile(String email) {
        return mapToProfile(getActiveUserByEmail(email));
    }

    @Override
    public UserProfileResponse getPublicProfile(String username) {
        return userRepository.findByUsername(username)
                .filter(User::isActive)
                .map(this::mapToProfile)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
    }

    @Override
    public UserProfileResponse getPublicProfileById(Long userId) {
        return userRepository.findById(userId)
                .filter(User::isActive)
                .map(this::mapToProfile)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }

    @Override
    public UserProfileResponse updateProfile(String email, UpdateProfileRequest request) {
        User user = getActiveUserByEmail(email);

        userRepository.findByUsername(request.getUsername())
                .filter(existing -> !existing.getId().equals(user.getId()))
                .ifPresent(existing -> {
                    throw new UserAlreadyExistsException("Username already taken");
                });

        userRepository.findByEmail(request.getEmail())
                .filter(existing -> !existing.getId().equals(user.getId()))
                .ifPresent(existing -> {
                    throw new UserAlreadyExistsException("Email already registered");
                });

        user.setUsername(request.getUsername());
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setBio(request.getBio());
        user.setProfilePicUrl(request.getProfilePicUrl());
        user.setUpdatedAt(LocalDateTime.now());

        return mapToProfile(userRepository.save(user));
    }

    @Override
    public void changePassword(String email, ChangePasswordRequest request) {
        User user = getActiveUserByEmail(email);

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Override
    public void resetPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("No account found with that email"));

        if (!user.isActive()) {
            throw new InvalidCredentialsException("User account is inactive");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Override
    public List<UserProfileResponse> searchUsers(String keyword) {
        return userRepository.searchByKeyword(keyword)
                .stream()
                .filter(User::isActive)
                .map(this::mapToProfile)
                .toList();
    }

    @Override
    public void deactivateAccount(String email) {
        User user = getActiveUserByEmail(email);
        user.setActive(false);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Override
    public List<UserProfileResponse> getUsersByRole(String role) {
        return userRepository.findByRoleIgnoreCase(role)
                .stream()
                .map(this::mapToProfile)
                .toList();
    }

    @Override
    public void suspendUser(Long userId) {
        User user = getUserById(userId);
        user.setActive(false);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Override
    public void reactivateUser(Long userId) {
        User user = getUserById(userId);
        user.setActive(true);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Override
    public void deleteUser(Long userId) {
        userRepository.delete(getUserById(userId));
    }

    @Override
    public UserStatsResponse getUserStats() {
        return UserStatsResponse.builder()
                .totalUsers(userRepository.count())
                .activeUsers(userRepository.countByIsActiveTrue())
                .adminUsers(userRepository.countByRoleIgnoreCase("ADMIN"))
                .build();
    }

    private AuthResponse buildAuthResponse(User user, String message) {
        String token = jwtService.generateToken(user.getEmail());
        return AuthResponse.builder()
                .token(token)
                .message(message)
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    private GoogleTokenInfoResponse verifyGoogleToken(String credential) {
        try {
            String encodedCredential = URLEncoder.encode(credential, StandardCharsets.UTF_8);
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create("https://oauth2.googleapis.com/tokeninfo?id_token=" + encodedCredential))
                    .GET()
                    .build();

            HttpResponse<String> response = HTTP_CLIENT.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new InvalidCredentialsException("Google token verification failed");
            }

            return objectMapper.readValue(response.body(), GoogleTokenInfoResponse.class);
        } catch (IOException ex) {
            throw new InvalidCredentialsException("Google token verification failed");
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new InvalidCredentialsException("Google token verification failed");
        }
    }

    private GitHubAccessTokenResponse exchangeGitHubCode(GitHubAuthRequest request) {
        try {
            String body = "client_id=" + URLEncoder.encode(githubClientId, StandardCharsets.UTF_8)
                    + "&client_secret=" + URLEncoder.encode(githubClientSecret, StandardCharsets.UTF_8)
                    + "&code=" + URLEncoder.encode(request.getCode(), StandardCharsets.UTF_8)
                    + "&redirect_uri=" + URLEncoder.encode(request.getRedirectUri(), StandardCharsets.UTF_8);

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create("https://github.com/login/oauth/access_token"))
                    .header("Accept", MediaType.APPLICATION_JSON_VALUE)
                    .header("Content-Type", MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = HTTP_CLIENT.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new InvalidCredentialsException("GitHub token exchange failed");
            }

            return objectMapper.readValue(response.body(), GitHubAccessTokenResponse.class);
        } catch (IOException ex) {
            throw new InvalidCredentialsException("GitHub token exchange failed");
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new InvalidCredentialsException("GitHub token exchange failed");
        }
    }

    private GitHubUserResponse fetchGitHubUser(String accessToken) {
        try {
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.github.com/user"))
                    .header("Accept", MediaType.APPLICATION_JSON_VALUE)
                    .header("Authorization", "Bearer " + accessToken)
                    .GET()
                    .build();

            HttpResponse<String> response = HTTP_CLIENT.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new InvalidCredentialsException("GitHub user lookup failed");
            }

            return objectMapper.readValue(response.body(), GitHubUserResponse.class);
        } catch (IOException ex) {
            throw new InvalidCredentialsException("GitHub user lookup failed");
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new InvalidCredentialsException("GitHub user lookup failed");
        }
    }

    private String resolveGitHubEmail(String accessToken, GitHubUserResponse userResponse) {
        if (userResponse.getEmail() != null && !userResponse.getEmail().isBlank()) {
            return userResponse.getEmail();
        }

        try {
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.github.com/user/emails"))
                    .header("Accept", MediaType.APPLICATION_JSON_VALUE)
                    .header("Authorization", "Bearer " + accessToken)
                    .GET()
                    .build();

            HttpResponse<String> response = HTTP_CLIENT.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new InvalidCredentialsException("GitHub email lookup failed");
            }

            List<GitHubEmailResponse> emails = objectMapper.readValue(response.body(), new TypeReference<>() {});
            return emails.stream()
                    .filter(GitHubEmailResponse::isPrimary)
                    .filter(GitHubEmailResponse::isVerified)
                    .map(GitHubEmailResponse::getEmail)
                    .findFirst()
                    .orElseGet(() -> emails.stream()
                            .filter(GitHubEmailResponse::isVerified)
                            .map(GitHubEmailResponse::getEmail)
                            .findFirst()
                            .orElse(null));
        } catch (IOException ex) {
            throw new InvalidCredentialsException("GitHub email lookup failed");
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new InvalidCredentialsException("GitHub email lookup failed");
        }
    }

    private User updateGoogleUser(User user, GoogleTokenInfoResponse tokenInfo) {
        if (tokenInfo.getName() != null && !tokenInfo.getName().isBlank()) {
            user.setFullName(tokenInfo.getName());
        }

        if (tokenInfo.getPicture() != null && !tokenInfo.getPicture().isBlank()) {
            user.setProfilePicUrl(tokenInfo.getPicture());
        }

        user.setProvider("GOOGLE");
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    private User createGoogleUser(GoogleTokenInfoResponse tokenInfo) {
        LocalDateTime now = LocalDateTime.now();
        User user = User.builder()
                .username(generateUniqueUsername(tokenInfo.getEmail(), "googleuser"))
                .email(tokenInfo.getEmail())
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .fullName(tokenInfo.getName())
                .profilePicUrl(tokenInfo.getPicture())
                .role("USER")
                .provider("GOOGLE")
                .isActive(true)
                .createdAt(now)
                .updatedAt(now)
                .build();

        return userRepository.save(user);
    }

    private User updateGitHubUser(User user, GitHubUserResponse githubUser, String email) {
        if (githubUser.getName() != null && !githubUser.getName().isBlank()) {
            user.setFullName(githubUser.getName());
        } else if (user.getFullName() == null || user.getFullName().isBlank()) {
            user.setFullName(githubUser.getLogin());
        }

        if (githubUser.getAvatarUrl() != null && !githubUser.getAvatarUrl().isBlank()) {
            user.setProfilePicUrl(githubUser.getAvatarUrl());
        }

        user.setEmail(email);
        user.setProvider("GITHUB");
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    private User createGitHubUser(GitHubUserResponse githubUser, String email) {
        LocalDateTime now = LocalDateTime.now();
        User user = User.builder()
                .username(generateUniqueUsername(githubUser.getLogin(), "githubuser"))
                .email(email)
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .fullName((githubUser.getName() == null || githubUser.getName().isBlank()) ? githubUser.getLogin() : githubUser.getName())
                .profilePicUrl(githubUser.getAvatarUrl())
                .role("USER")
                .provider("GITHUB")
                .isActive(true)
                .createdAt(now)
                .updatedAt(now)
                .build();

        return userRepository.save(user);
    }

    private String generateUniqueUsername(String seed, String fallback) {
        String base = seed == null ? fallback : seed.toLowerCase().replaceAll("[^a-z0-9_]", "");
        if (base.isBlank()) {
            base = fallback;
        }

        String candidate = base;
        int suffix = 1;
        while (userRepository.existsByUsername(candidate)) {
            candidate = base + suffix;
            suffix += 1;
        }

        return candidate;
    }

    private User getActiveUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("User not found"));

        if (!user.isActive()) {
            throw new InvalidCredentialsException("User account is inactive");
        }

        return user;
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }

    private UserProfileResponse mapToProfile(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .bio(user.getBio())
                .profilePicUrl(user.getProfilePicUrl())
                .role(user.getRole())
                .provider(user.getProvider())
                .active(user.isActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
