package com.connectsphere.auth.controller;

import com.connectsphere.auth.dto.UserProfileResponse;
import com.connectsphere.auth.dto.UserStatsResponse;
import com.connectsphere.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AuthService authService;

    @GetMapping
    public ResponseEntity<List<UserProfileResponse>> getUsersByRole(@RequestParam(defaultValue = "USER") String role) {
        return ResponseEntity.ok(authService.getUsersByRole(role));
    }

    @PutMapping("/{userId}/suspend")
    public ResponseEntity<Map<String, String>> suspendUser(@PathVariable Long userId) {
        authService.suspendUser(userId);
        return ResponseEntity.ok(Map.of("message", "User suspended successfully"));
    }

    @PutMapping("/{userId}/reactivate")
    public ResponseEntity<Map<String, String>> reactivateUser(@PathVariable Long userId) {
        authService.reactivateUser(userId);
        return ResponseEntity.ok(Map.of("message", "User reactivated successfully"));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long userId) {
        authService.deleteUser(userId);
        return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
    }

    @GetMapping("/stats")
    public ResponseEntity<UserStatsResponse> getUserStats() {
        return ResponseEntity.ok(authService.getUserStats());
    }
}
