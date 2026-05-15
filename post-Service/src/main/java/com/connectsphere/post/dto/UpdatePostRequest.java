package com.connectsphere.post.dto;

import com.connectsphere.post.enums.Visibility;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class UpdatePostRequest {
    @Size(max = 1000, message = "Content must not exceed 1000 characters")
    private String content;

    private List<String> mediaUrls;

    private String postType;

    @NotNull(message = "Visibility is required")
    private Visibility visibility;
}
