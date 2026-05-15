package com.connectsphere.post.service.impl;

import com.connectsphere.post.dto.CreateReportRequest;
import com.connectsphere.post.dto.ReportResponse;
import com.connectsphere.post.dto.ResolveReportRequest;
import com.connectsphere.post.entity.ContentReport;
import com.connectsphere.post.entity.Post;
import com.connectsphere.post.enums.ModerationStatus;
import com.connectsphere.post.enums.ReportStatus;
import com.connectsphere.post.enums.ReportTargetType;
import com.connectsphere.post.exception.ResourceNotFoundException;
import com.connectsphere.post.repository.ContentReportRepository;
import com.connectsphere.post.repository.PostRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceImplTest {

    @Mock
    private ContentReportRepository contentReportRepository;

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private ReportServiceImpl reportService;

    @Test
    void createReportShouldFlagTargetPost() {
        CreateReportRequest request = new CreateReportRequest();
        request.setReporterId(1L);
        request.setTargetId(5L);
        request.setTargetType(ReportTargetType.POST);
        request.setReason("spam");

        Post post = Post.builder().id(5L).flagged(false).moderationStatus(ModerationStatus.APPROVED).build();
        ContentReport saved = ContentReport.builder()
                .id(2L)
                .reporterId(1L)
                .targetId(5L)
                .targetType(ReportTargetType.POST)
                .status(ReportStatus.OPEN)
                .reason("spam")
                .build();

        when(postRepository.findById(5L)).thenReturn(Optional.of(post));
        when(contentReportRepository.save(any(ContentReport.class))).thenReturn(saved);

        ReportResponse response = reportService.createReport(request);

        assertEquals(ReportStatus.OPEN, response.getStatus());
        verify(postRepository).save(post);
    }

    @Test
    void getReportsShouldReturnAllWhenStatusMissing() {
        when(contentReportRepository.findAll()).thenReturn(List.of(
                ContentReport.builder().id(1L).status(ReportStatus.OPEN).build()
        ));

        assertEquals(1, reportService.getReports(null).size());
    }

    @Test
    void getReportsShouldFilterByStatus() {
        when(contentReportRepository.findByStatusOrderByCreatedAtDesc(ReportStatus.RESOLVED)).thenReturn(List.of(
                ContentReport.builder().id(1L).status(ReportStatus.RESOLVED).build()
        ));

        assertEquals(1, reportService.getReports(ReportStatus.RESOLVED).size());
    }

    @Test
    void resolveReportShouldThrowWhenMissing() {
        when(contentReportRepository.findById(4L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> reportService.resolveReport(4L, new ResolveReportRequest()));
    }

    @Test
    void resolveReportShouldPersistStatusAndNotesWithoutRemoval() {
        ContentReport report = ContentReport.builder().id(4L).status(ReportStatus.OPEN).build();
        ResolveReportRequest request = new ResolveReportRequest();
        request.setStatus(ReportStatus.RESOLVED);
        request.setAdminNotes("Handled");
        request.setRemoveContent(false);

        when(contentReportRepository.findById(4L)).thenReturn(Optional.of(report));
        when(contentReportRepository.save(report)).thenReturn(report);

        ReportResponse response = reportService.resolveReport(4L, request);

        assertEquals(ReportStatus.RESOLVED, response.getStatus());
        assertEquals("Handled", response.getAdminNotes());
    }
}
