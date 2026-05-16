package com.connectsphere.post.dto;

import com.connectsphere.post.enums.ReportTargetType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateReportRequest {
    @NotNull(message = "Reporter id is required")
    private Long reporterId;

    @NotNull(message = "Target id is required")
    private Long targetId;

    @NotNull(message = "Target type is required")
    private ReportTargetType targetType;

    @NotBlank(message = "Reason is required")
    private String reason;
}
