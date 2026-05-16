package com.connectsphere.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateCommentRequest {
    @NotNull(message = "Post id is required")
    private Long postId;

    @NotNull(message = "Author id is required")
    private Long authorId;

    private Long parentCommentId;

    @NotBlank(message = "Content is required")
    @Size(max = 500, message = "Comment must not exceed 500 characters")
    private String content;
}
