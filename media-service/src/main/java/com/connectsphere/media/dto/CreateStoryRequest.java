package com.connectsphere.media.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateStoryRequest {
    @NotNull(message = "Author id is required")
    private Long authorId;

    @NotBlank(message = "Media URL is required")
    private String mediaUrl;

    @Size(max = 300, message = "Caption must not exceed 300 characters")
    private String caption;

    @NotBlank(message = "Media type is required")
    private String mediaType;
}
