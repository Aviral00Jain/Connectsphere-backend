package com.connectsphere.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class GitHubEmailResponse {
    private String email;

    private boolean primary;

    private boolean verified;

    @JsonProperty("visibility")
    private String visibility;
}
