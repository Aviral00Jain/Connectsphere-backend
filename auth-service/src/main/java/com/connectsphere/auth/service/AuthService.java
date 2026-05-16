package com.connectsphere.auth.service;

import com.connectsphere.auth.dto.AuthResponse;
import com.connectsphere.auth.dto.ChangePasswordRequest;
import com.connectsphere.auth.dto.ForgotPasswordRequest;
import com.connectsphere.auth.dto.GitHubAuthRequest;
import com.connectsphere.auth.dto.GoogleAuthRequest;
import com.connectsphere.auth.dto.LoginRequest;
import com.connectsphere.auth.dto.RegisterRequest;
import com.connectsphere.auth.dto.UpdateProfileRequest;
import com.connectsphere.auth.dto.UserProfileResponse;
import com.connectsphere.auth.dto.UserStatsResponse;

import java.util.List;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse loginWithGoogle(GoogleAuthRequest request);

    AuthResponse loginWithGitHub(GitHubAuthRequest request);

    void logout(String email);

    AuthResponse refreshToken(String email);

    UserProfileResponse getProfile(String email);

    UserProfileResponse getPublicProfile(String username);

    UserProfileResponse getPublicProfileById(Long userId);

    UserProfileResponse updateProfile(String email, UpdateProfileRequest request);

    void changePassword(String email, ChangePasswordRequest request);

    void resetPassword(ForgotPasswordRequest request);

    List<UserProfileResponse> searchUsers(String keyword);

    void deactivateAccount(String email);

    List<UserProfileResponse> getUsersByRole(String role);

    void suspendUser(Long userId);

    void reactivateUser(Long userId);

    void deleteUser(Long userId);

    UserStatsResponse getUserStats();
}
