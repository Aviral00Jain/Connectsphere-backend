package com.connectsphere.auth.service.impl;

import com.connectsphere.auth.dto.GitHubUserResponse;
import com.connectsphere.auth.dto.GoogleTokenInfoResponse;
import com.connectsphere.auth.entity.User;
import com.connectsphere.auth.repository.UserRepository;
import com.connectsphere.auth.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplAdditionalTest {

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
    void createGoogleUserShouldGenerateUniqueUsernameAndPersistProviderDetails() {
        GoogleTokenInfoResponse tokenInfo = new GoogleTokenInfoResponse();
        tokenInfo.setEmail("New.User@gmail.com");
        tokenInfo.setName("New User");
        tokenInfo.setPicture("avatar.png");

        when(userRepository.existsByUsername("newusergmailcom")).thenReturn(true);
        when(userRepository.existsByUsername("newusergmailcom1")).thenReturn(false);
        when(passwordEncoder.encode(any(String.class))).thenReturn("encoded-random");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User user = ReflectionTestUtils.invokeMethod(authService, "createGoogleUser", tokenInfo);

        assertEquals("newusergmailcom1", user.getUsername());
        assertEquals("GOOGLE", user.getProvider());
        assertTrue(user.isActive());
    }

    @Test
    void updateGoogleUserShouldRefreshNamePictureAndProvider() {
        User user = User.builder()
                .username("old")
                .fullName("Old Name")
                .profilePicUrl("old.png")
                .provider("LOCAL")
                .build();
        GoogleTokenInfoResponse tokenInfo = new GoogleTokenInfoResponse();
        tokenInfo.setName("New Name");
        tokenInfo.setPicture("new.png");

        when(userRepository.save(user)).thenReturn(user);

        User updated = ReflectionTestUtils.invokeMethod(authService, "updateGoogleUser", user, tokenInfo);

        assertEquals("New Name", updated.getFullName());
        assertEquals("new.png", updated.getProfilePicUrl());
        assertEquals("GOOGLE", updated.getProvider());
    }

    @Test
    void createGitHubUserShouldUseLoginWhenNameMissing() {
        GitHubUserResponse githubUser = new GitHubUserResponse();
        githubUser.setLogin("git-user");
        githubUser.setName("");
        githubUser.setAvatarUrl("git.png");

        when(userRepository.existsByUsername("gituser")).thenReturn(false);
        when(passwordEncoder.encode(any(String.class))).thenReturn("encoded-random");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User user = ReflectionTestUtils.invokeMethod(authService, "createGitHubUser", githubUser, "git@example.com");

        assertEquals("gituser", user.getUsername());
        assertEquals("git-user", user.getFullName());
        assertEquals("GITHUB", user.getProvider());
    }

    @Test
    void updateGitHubUserShouldUseLoginWhenExistingNameIsBlank() {
        User user = User.builder()
                .fullName("")
                .provider("LOCAL")
                .build();
        GitHubUserResponse githubUser = new GitHubUserResponse();
        githubUser.setLogin("octo");
        githubUser.setName("");
        githubUser.setAvatarUrl("octo.png");

        when(userRepository.save(user)).thenReturn(user);

        User updated = ReflectionTestUtils.invokeMethod(authService, "updateGitHubUser", user, githubUser, "octo@example.com");

        assertEquals("octo", updated.getFullName());
        assertEquals("octo@example.com", updated.getEmail());
        assertEquals("GITHUB", updated.getProvider());
    }
}
