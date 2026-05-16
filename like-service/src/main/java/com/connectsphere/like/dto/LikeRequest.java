package com.connectsphere.like.dto;

import com.connectsphere.like.enums.ReactionType;
import com.connectsphere.like.enums.TargetType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LikeRequest {
    @NotNull(message = "User id is required")
    private Long userId;

    @NotNull(message = "Target id is required")
    private Long targetId;

    @NotNull(message = "Target type is required")
    private TargetType targetType;

    @NotNull(message = "Reaction type is required")
    private ReactionType reactionType;
}
