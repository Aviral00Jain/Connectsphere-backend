package com.connectsphere.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GitHubAuthRequest {
    @NotBlank(message = "GitHub authorization code is required")
    private String code;

    @NotBlank(message = "GitHub redirect URI is required")
    private String redirectUri;
}
