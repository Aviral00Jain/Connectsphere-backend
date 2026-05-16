package com.connectsphere.follow.messaging;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewFollowerEvent {
    private Long followerId;
    private Long followingId;
    private LocalDateTime occurredAt;
}
