package com.connectsphere.post.service;

import com.connectsphere.post.dto.CreateReportRequest;
import com.connectsphere.post.dto.ReportResponse;
import com.connectsphere.post.dto.ResolveReportRequest;
import com.connectsphere.post.enums.ReportStatus;

import java.util.List;

public interface ReportService {
    ReportResponse createReport(CreateReportRequest request);
    List<ReportResponse> getReports(ReportStatus status);
    ReportResponse resolveReport(Long reportId, ResolveReportRequest request);
}
