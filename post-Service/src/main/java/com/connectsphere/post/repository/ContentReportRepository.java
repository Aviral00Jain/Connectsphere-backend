package com.connectsphere.post.repository;

import com.connectsphere.post.entity.ContentReport;
import com.connectsphere.post.enums.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContentReportRepository extends JpaRepository<ContentReport, Long> {
    List<ContentReport> findByStatusOrderByCreatedAtDesc(ReportStatus status);
}
