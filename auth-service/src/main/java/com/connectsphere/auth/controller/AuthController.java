package com.connectsphere.auth.controller;

import com.connectsphere.auth.dto.AuthResponse;
import com.connectsphere.auth.dto.ChangePasswordRequest;
import com.connectsphere.auth.dto.ForgotPasswordRequest;
import com.connectsphere.auth.dto.GitHubAuthRequest;
import com.connectsphere.auth.dto.GoogleAuthRequest;
import com.connectsphere.auth.dto.LoginRequest;
import com.connectsphere.auth.dto.RegisterRequest;
import com.connectsphere.auth.dto.UpdateProfileRequest;
import com.connectsphere.auth.dto.UserProfileResponse;
import com.connectsphere.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return new ResponseEntity<>(authService.register(request), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/google")
    public ResponseEntity<AuthResponse> loginWithGoogle(@Valid @RequestBody GoogleAuthRequest request) {
        return ResponseEntity.ok(authService.loginWithGoogle(request));
    }

    @PostMapping("/github")
    public ResponseEntity<AuthResponse> loginWithGitHub(@Valid @RequestBody GitHubAuthRequest request) {
        return ResponseEntity.ok(authService.loginWithGitHub(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(Authentication authentication) {
        authService.logout(authentication.getName());
        return ResponseEntity.ok("Logout successful");
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(Authentication authentication) {
        return ResponseEntity.ok(authService.refreshToken(authentication.getName()));
    }

    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getProfile(Authentication authentication) {
        return ResponseEntity.ok(authService.getProfile(authentication.getName()));
    }

    @GetMapping("/profile/{username}")
    public ResponseEntity<UserProfileResponse> getPublicProfile(@PathVariable String username) {
        return ResponseEntity.ok(authService.getPublicProfile(username));
    }

    @GetMapping("/profile/id/{userId}")
    public ResponseEntity<UserProfileResponse> getPublicProfileById(@PathVariable Long userId) {
        return ResponseEntity.ok(authService.getPublicProfileById(userId));
    }

    @PutMapping("/profile")
    public ResponseEntity<UserProfileResponse> updateProfile(Authentication authentication,
                                                             @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(authService.updateProfile(authentication.getName(), request));
    }

    @PutMapping("/password")
    public ResponseEntity<String> changePassword(Authentication authentication,
                                                 @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(authentication.getName(), request);
        return ResponseEntity.ok("Password changed successfully");
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok("Password reset successfully");
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserProfileResponse>> searchUsers(@RequestParam String keyword) {
        return ResponseEntity.ok(authService.searchUsers(keyword));
    }

    @PutMapping("/deactivate")
    public ResponseEntity<String> deactivateAccount(Authentication authentication) {
        authService.deactivateAccount(authentication.getName());
        return ResponseEntity.ok("Account deactivated successfully");
    }
}
