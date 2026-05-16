package com.connectsphere.media.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MediaResponse {
    private Long id;
    private Long postId;
    private Long uploaderId;
    private String fileName;
    private String fileType;
    private String fileUrl;
    private boolean deleted;
    private LocalDateTime createdAt;
}
