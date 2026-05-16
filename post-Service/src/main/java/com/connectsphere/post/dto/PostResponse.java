package com.connectsphere.post.dto;

import com.connectsphere.post.enums.ModerationStatus;
import com.connectsphere.post.enums.Visibility;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class PostResponse {
    private Long id;
    private Long authorId;
    private String content;
    private List<String> mediaUrls;
    private String postType;
    private Visibility visibility;
    private Integer likesCount;
    private Integer commentsCount;
    private Integer sharesCount;
    private boolean flagged;
    private ModerationStatus moderationStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
