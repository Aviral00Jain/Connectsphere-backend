package com.connectsphere.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class BulkNotificationRequest {
    @NotEmpty(message = "At least one receiver id is required")
    private List<Long> receiverIds;

    private Long actorId;

    @NotBlank(message = "Message is required")
    @Size(max = 500, message = "Message must not exceed 500 characters")
    private String message;

    @NotBlank(message = "Notification type is required")
    private String type;

    private Long targetId;
    private String targetType;
    private String deepLinkUrl;
}
