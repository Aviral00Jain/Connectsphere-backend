package com.connectsphere.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateProfileRequest {

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Enter a valid email")
    private String email;

    private String bio;

    private String profilePicUrl;
}
