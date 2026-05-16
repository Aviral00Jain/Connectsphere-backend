package com.connectsphere.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateCommentRequest {
    @NotBlank(message = "Content is required")
    @Size(max = 500, message = "Comment must not exceed 500 characters")
    private String content;
}
