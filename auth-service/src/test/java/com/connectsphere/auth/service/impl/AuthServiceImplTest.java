package com.connectsphere.auth.service.impl;

import com.connectsphere.auth.dto.AuthResponse;
import com.connectsphere.auth.dto.ChangePasswordRequest;
import com.connectsphere.auth.dto.ForgotPasswordRequest;
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
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void registerShouldSaveUserAndReturnResponse() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("user1");
        request.setEmail("user1@gmail.com");
        request.setPassword("123456");
        request.setFullName("User One");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuthResponse response = authService.register(request);

        assertEquals("User registered successfully", response.getMessage());
        assertEquals("user1", response.getUsername());
        assertEquals("user1@gmail.com", response.getEmail());
        assertEquals("USER", response.getRole());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerShouldThrowWhenEmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("existing@gmail.com");
        request.setUsername("new-user");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerShouldThrowWhenUsernameAlreadyExists() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("user@gmail.com");
        request.setUsername("taken");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(request.getUsername())).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> authService.register(request));
    }

    @Test
    void loginShouldReturnTokenForValidCredentials() {
        LoginRequest request = new LoginRequest();
        request.setEmail("user1@gmail.com");
        request.setPassword("123456");

        User user = User.builder()
                .username("user1")
                .email("user1@gmail.com")
                .password("encodedPassword")
                .role("USER")
                .isActive(true)
                .build();

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getPassword(), user.getPassword())).thenReturn(true);
        when(jwtService.generateToken(user.getEmail())).thenReturn("jwt-token");

        AuthResponse response = authService.login(request);

        assertEquals("jwt-token", response.getToken());
        assertEquals("Login successful", response.getMessage());
        assertEquals("user1", response.getUsername());
    }

    @Test
    void loginShouldThrowWhenPasswordDoesNotMatch() {
        LoginRequest request = new LoginRequest();
        request.setEmail("user1@gmail.com");
        request.setPassword("wrong");

        User user = User.builder()
                .email("user1@gmail.com")
                .password("encodedPassword")
                .isActive(true)
                .build();

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getPassword(), user.getPassword())).thenReturn(false);

        InvalidCredentialsException exception =
                assertThrows(InvalidCredentialsException.class, () -> authService.login(request));

        assertTrue(exception.getMessage().contains("Invalid email or password"));
    }

    @Test
    void loginWithGoogleShouldThrowWhenServerConfigMissing() {
        assertThrows(InvalidCredentialsException.class, () -> authService.loginWithGoogle(new com.connectsphere.auth.dto.GoogleAuthRequest()));
    }

    @Test
    void loginWithGitHubShouldThrowWhenServerConfigMissing() {
        assertThrows(InvalidCredentialsException.class, () -> authService.loginWithGitHub(new com.connectsphere.auth.dto.GitHubAuthRequest()));
    }

    @Test
    void logoutShouldValidateActiveUser() {
        User user = User.builder().email("user@gmail.com").isActive(true).build();
        when(userRepository.findByEmail("user@gmail.com")).thenReturn(Optional.of(user));

        authService.logout("user@gmail.com");

        verify(userRepository).findByEmail("user@gmail.com");
    }

    @Test
    void refreshTokenShouldReturnNewTokenForActiveUser() {
        User user = User.builder().email("user@gmail.com").username("user").role("USER").isActive(true).build();
        when(userRepository.findByEmail("user@gmail.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken("user@gmail.com")).thenReturn("new-token");

        AuthResponse response = authService.refreshToken("user@gmail.com");

        assertEquals("new-token", response.getToken());
    }

    @Test
    void getProfileShouldMapActiveUser() {
        User user = User.builder().id(1L).email("user@gmail.com").username("user").fullName("User").isActive(true).build();
        when(userRepository.findByEmail("user@gmail.com")).thenReturn(Optional.of(user));

        UserProfileResponse response = authService.getProfile("user@gmail.com");

        assertEquals("user", response.getUsername());
    }

    @Test
    void getPublicProfileShouldReturnActiveUserByUsername() {
        User user = User.builder().id(1L).username("user").isActive(true).build();
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));

        assertEquals("user", authService.getPublicProfile("user").getUsername());
    }

    @Test
    void getPublicProfileByIdShouldReturnActiveUser() {
        User user = User.builder().id(1L).username("user").isActive(true).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertEquals(1L, authService.getPublicProfileById(1L).getId());
    }

    @Test
    void updateProfileShouldPersistFields() {
        User user = User.builder().id(1L).username("old").email("old@gmail.com").isActive(true).build();
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setUsername("newuser");
        request.setFullName("New User");
        request.setEmail("new@gmail.com");
        request.setBio("bio");
        request.setProfilePicUrl("pic");

        when(userRepository.findByEmail("old@gmail.com")).thenReturn(Optional.of(user));
        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("new@gmail.com")).thenReturn(Optional.empty());
        when(userRepository.save(user)).thenReturn(user);

        UserProfileResponse response = authService.updateProfile("old@gmail.com", request);

        assertEquals("newuser", response.getUsername());
        assertEquals("new@gmail.com", response.getEmail());
    }

    @Test
    void changePasswordShouldUpdateEncodedPassword() {
        User user = User.builder().email("user@gmail.com").password("old-encoded").isActive(true).build();
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("old");
        request.setNewPassword("newpass");

        when(userRepository.findByEmail("user@gmail.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("old", "old-encoded")).thenReturn(true);
        when(passwordEncoder.encode("newpass")).thenReturn("new-encoded");

        authService.changePassword("user@gmail.com", request);

        assertEquals("new-encoded", user.getPassword());
        verify(userRepository).save(user);
    }

    @Test
    void resetPasswordShouldThrowWhenUserMissing() {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("missing@gmail.com");
        request.setNewPassword("newpass");

        when(userRepository.findByEmail("missing@gmail.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> authService.resetPassword(request));
    }

    @Test
    void resetPasswordShouldPersistNewPassword() {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("user@gmail.com");
        request.setNewPassword("newpass");

        User user = User.builder().email("user@gmail.com").isActive(true).build();
        when(userRepository.findByEmail("user@gmail.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newpass")).thenReturn("encoded-new");

        authService.resetPassword(request);

        assertEquals("encoded-new", user.getPassword());
        verify(userRepository).save(user);
    }

    @Test
    void searchUsersShouldFilterInactiveUsers() {
        when(userRepository.searchByKeyword("jo")).thenReturn(List.of(
                User.builder().username("john").isActive(true).build(),
                User.builder().username("jane").isActive(false).build()
        ));

        List<UserProfileResponse> responses = authService.searchUsers("jo");

        assertEquals(1, responses.size());
        assertEquals("john", responses.get(0).getUsername());
    }

    @Test
    void deactivateAccountShouldMarkUserInactive() {
        User user = User.builder().email("user@gmail.com").isActive(true).build();
        when(userRepository.findByEmail("user@gmail.com")).thenReturn(Optional.of(user));

        authService.deactivateAccount("user@gmail.com");

        assertFalse(user.isActive());
        verify(userRepository).save(user);
    }

    @Test
    void getUsersByRoleShouldMapAllUsers() {
        when(userRepository.findByRoleIgnoreCase("ADMIN")).thenReturn(List.of(
                User.builder().username("admin").role("ADMIN").build()
        ));

        assertEquals(1, authService.getUsersByRole("ADMIN").size());
    }

    @Test
    void suspendUserShouldSetInactive() {
        User user = User.builder().id(3L).isActive(true).build();
        when(userRepository.findById(3L)).thenReturn(Optional.of(user));

        authService.suspendUser(3L);

        assertFalse(user.isActive());
        verify(userRepository).save(user);
    }

    @Test
    void reactivateUserShouldSetActive() {
        User user = User.builder().id(3L).isActive(false).build();
        when(userRepository.findById(3L)).thenReturn(Optional.of(user));

        authService.reactivateUser(3L);

        assertTrue(user.isActive());
        verify(userRepository).save(user);
    }

    @Test
    void deleteUserShouldDelegateToRepository() {
        User user = User.builder().id(3L).build();
        when(userRepository.findById(3L)).thenReturn(Optional.of(user));

        authService.deleteUser(3L);

        verify(userRepository).delete(user);
    }

    @Test
    void getUserStatsShouldAggregateRepositoryCounts() {
        when(userRepository.count()).thenReturn(10L);
        when(userRepository.countByIsActiveTrue()).thenReturn(8L);
        when(userRepository.countByRoleIgnoreCase("ADMIN")).thenReturn(2L);

        UserStatsResponse response = authService.getUserStats();

        assertEquals(10L, response.getTotalUsers());
        assertEquals(8L, response.getActiveUsers());
        assertEquals(2L, response.getAdminUsers());
    }
}
