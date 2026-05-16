package com.connectsphere.search.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SearchRecordResponse {
    private Long id;
    private Long postId;
    private String keyword;
    private LocalDateTime createdAt;
}