package com.connectsphere.media.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateMediaRequest {
    @NotNull(message = "Post id is required")
    private Long postId;

    @NotNull(message = "Uploader id is required")
    private Long uploaderId;

    @NotBlank(message = "File name is required")
    private String fileName;

    @NotBlank(message = "File type is required")
    private String fileType;

    @NotBlank(message = "File URL is required")
    private String fileUrl;
}
