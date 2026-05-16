package com.connectsphere.auth.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserProfileResponse {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String bio;
    private String profilePicUrl;
    private String role;
    private String provider;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
