package com.connectsphere.post.controller;

import com.connectsphere.post.dto.CreateReportRequest;
import com.connectsphere.post.dto.ReportResponse;
import com.connectsphere.post.dto.ResolveReportRequest;
import com.connectsphere.post.enums.ReportStatus;
import com.connectsphere.post.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    public ReportResponse createReport(@Valid @RequestBody CreateReportRequest request) {
        return reportService.createReport(request);
    }

    @GetMapping
    public List<ReportResponse> getReports(@RequestParam(required = false) ReportStatus status) {
        return reportService.getReports(status);
    }

    @PutMapping("/{reportId}/resolve")
    public ReportResponse resolveReport(@PathVariable Long reportId,
                                        @Valid @RequestBody ResolveReportRequest request) {
        return reportService.resolveReport(reportId, request);
    }
}
