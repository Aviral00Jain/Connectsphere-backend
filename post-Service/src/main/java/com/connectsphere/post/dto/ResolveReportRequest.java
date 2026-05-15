package com.connectsphere.post.dto;

import com.connectsphere.post.enums.ReportStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ResolveReportRequest {
    @NotNull(message = "Status is required")
    private ReportStatus status;

    private String adminNotes;

    private boolean removeContent;
}
