package com.connectsphere.search.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateSearchRecordRequest {
    @NotNull(message = "Post id is required")
    private Long postId;

    @Size(max = 100, message = "Keyword must not exceed 100 characters")
    private String keyword;

    @Size(max = 2000, message = "Content must not exceed 2000 characters")
    private String content;
}
