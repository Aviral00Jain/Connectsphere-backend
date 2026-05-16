package com.connectsphere.post.entity;

import com.connectsphere.post.enums.ReportStatus;
import com.connectsphere.post.enums.ReportTargetType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "content_reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContentReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long reporterId;

    private Long targetId;

    @Enumerated(EnumType.STRING)
    private ReportTargetType targetType;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Enumerated(EnumType.STRING)
    private ReportStatus status;

    @Column(columnDefinition = "TEXT")
    private String adminNotes;

    private LocalDateTime createdAt;

    private LocalDateTime resolvedAt;
}
