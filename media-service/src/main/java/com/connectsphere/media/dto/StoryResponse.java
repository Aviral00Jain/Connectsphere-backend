package com.connectsphere.media.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class StoryResponse {
    private Long id;
    private Long authorId;
    private String mediaUrl;
    private String caption;
    private String mediaType;
    private Long viewsCount;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private boolean active;
}
