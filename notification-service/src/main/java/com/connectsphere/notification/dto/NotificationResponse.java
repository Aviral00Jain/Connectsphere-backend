package com.connectsphere.notification.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificationResponse {
    private Long id;
    private Long receiverId;
    private Long actorId;
    private String type;
    private String message;
    private Long targetId;
    private String targetType;
    private String deepLinkUrl;
    private boolean isRead;
    private LocalDateTime createdAt;
}
