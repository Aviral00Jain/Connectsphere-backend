package com.connectsphere.notification.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
