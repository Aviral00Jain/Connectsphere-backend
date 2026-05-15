package com.connectsphere.post.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PostStatsResponse {
    private long totalPosts;
    private long flaggedPosts;
}
