package com.connectsphere.like.dto;

import com.connectsphere.like.enums.ReactionType;
import com.connectsphere.like.enums.TargetType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LikeResponse {
    private Long likeId;
    private Long userId;
    private Long targetId;
    private TargetType targetType;
    private ReactionType reactionType;
    private String message;
}