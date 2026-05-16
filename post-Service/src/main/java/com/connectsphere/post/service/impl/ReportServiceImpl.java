package com.connectsphere.post.service.impl;

import com.connectsphere.post.dto.CreateReportRequest;
import com.connectsphere.post.dto.ReportResponse;
import com.connectsphere.post.dto.ResolveReportRequest;
import com.connectsphere.post.entity.ContentReport;
import com.connectsphere.post.enums.ModerationStatus;
import com.connectsphere.post.enums.ReportStatus;
import com.connectsphere.post.enums.ReportTargetType;
import com.connectsphere.post.exception.ResourceNotFoundException;
import com.connectsphere.post.repository.ContentReportRepository;
import com.connectsphere.post.repository.PostRepository;
import com.connectsphere.post.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

    private final ContentReportRepository contentReportRepository;
    private final PostRepository postRepository;

    @Value("${comment.service.base-url:http://localhost:8083/comments}")
    private String commentServiceBaseUrl;

    @Value("${auth.admin.base-url:http://localhost:8081/api/v1/admin/users}")
    private String authAdminBaseUrl;

    @Override
    public ReportResponse createReport(CreateReportRequest request) {
        ContentReport report = ContentReport.builder()
                .reporterId(request.getReporterId())
                .targetId(request.getTargetId())
                .targetType(request.getTargetType())
                .reason(request.getReason())
                .status(ReportStatus.OPEN)
                .createdAt(LocalDateTime.now())
                .build();

        if (request.getTargetType() == ReportTargetType.POST) {
            postRepository.findById(request.getTargetId()).ifPresent(post -> {
                post.setFlagged(true);
                post.setModerationStatus(ModerationStatus.FLAGGED);
                postRepository.save(post);
            });
        }

        return mapToResponse(contentReportRepository.save(report));
    }

    @Override
    public List<ReportResponse> getReports(ReportStatus status) {
        if (status == null) {
            return contentReportRepository.findAll()
                    .stream()
                    .map(this::mapToResponse)
                    .toList();
        }

        return contentReportRepository.findByStatusOrderByCreatedAtDesc(status)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public ReportResponse resolveReport(Long reportId, ResolveReportRequest request) {
        ContentReport report = contentReportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found with id: " + reportId));

        report.setStatus(request.getStatus());
        report.setAdminNotes(request.getAdminNotes());
        report.setResolvedAt(LocalDateTime.now());

        if (request.isRemoveContent()) {
            applyModerationAction(report);
        }

        return mapToResponse(contentReportRepository.save(report));
    }

    private void applyModerationAction(ContentReport report) {
        if (report.getTargetType() == ReportTargetType.POST) {
            postRepository.findById(report.getTargetId()).ifPresent(post -> {
                post.setDeleted(true);
                post.setModerationStatus(ModerationStatus.REMOVED);
                postRepository.save(post);
            });
            return;
        }

        if (report.getTargetType() == ReportTargetType.COMMENT) {
            executeDelete(commentServiceBaseUrl + "/" + report.getTargetId(), "comment");
            return;
        }

        if (report.getTargetType() == ReportTargetType.USER) {
            executePut(authAdminBaseUrl + "/" + report.getTargetId() + "/suspend", "user");
        }
    }

    private void executeDelete(String url, String targetLabel) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .DELETE()
                    .build();
            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                throw new IllegalStateException("Failed to moderate " + targetLabel + ": " + response.statusCode());
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to moderate " + targetLabel, ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Failed to moderate " + targetLabel, ex);
        }
    }

    private void executePut(String url, String targetLabel) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .PUT(HttpRequest.BodyPublishers.noBody())
                    .build();
            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                throw new IllegalStateException("Failed to moderate " + targetLabel + ": " + response.statusCode());
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to moderate " + targetLabel, ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Failed to moderate " + targetLabel, ex);
        }
    }

    private ReportResponse mapToResponse(ContentReport report) {
        return ReportResponse.builder()
                .id(report.getId())
                .reporterId(report.getReporterId())
                .targetId(report.getTargetId())
                .targetType(report.getTargetType())
                .reason(report.getReason())
                .status(report.getStatus())
                .adminNotes(report.getAdminNotes())
                .createdAt(report.getCreatedAt())
                .resolvedAt(report.getResolvedAt())
                .build();
    }
}
