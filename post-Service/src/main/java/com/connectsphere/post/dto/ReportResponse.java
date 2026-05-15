package com.connectsphere.post.dto;

import com.connectsphere.post.enums.ReportStatus;
import com.connectsphere.post.enums.ReportTargetType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ReportResponse {
    private Long id;
    private Long reporterId;
    private Long targetId;
    private ReportTargetType targetType;
    private String reason;
    private ReportStatus status;
    private String adminNotes;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
}
