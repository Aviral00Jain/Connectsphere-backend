package com.connectsphere.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateNotificationRequest {
    @NotNull(message = "Receiver id is required")
    private Long receiverId;

    private Long actorId;

    @NotBlank(message = "Notification type is required")
    private String type;

    @NotBlank(message = "Message is required")
    @Size(max = 500, message = "Message must not exceed 500 characters")
    private String message;

    private Long targetId;
    private String targetType;
    private String deepLinkUrl;
}
