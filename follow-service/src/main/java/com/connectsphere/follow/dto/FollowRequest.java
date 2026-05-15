package com.connectsphere.follow.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FollowRequest {
    @NotNull(message = "Follower id is required")
    private Long followerId;

    @NotNull(message = "Following id is required")
    private Long followingId;
}
