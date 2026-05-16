package com.connectsphere.notification.messaging;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NewFollowerEvent {
    private Long followerId;
    private Long followingId;
    private LocalDateTime occurredAt;
}
