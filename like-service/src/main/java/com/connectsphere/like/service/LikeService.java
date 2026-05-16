package com.connectsphere.like.service;

import com.connectsphere.like.dto.LikeRequest;
import com.connectsphere.like.dto.LikeResponse;
import com.connectsphere.like.entity.LikeEntity;
import com.connectsphere.like.enums.ReactionType;
import com.connectsphere.like.enums.TargetType;

import java.util.Map;
import java.util.List;

public interface LikeService {

    LikeResponse likeTarget(LikeRequest request);

    String unlikeTarget(Long userId, Long targetId, TargetType targetType);

    boolean hasLiked(Long userId, Long targetId, TargetType targetType);

    List<LikeEntity> getLikesByTarget(Long targetId, TargetType targetType);

    List<LikeEntity> getLikesByUser(Long userId);

    long getLikeCount(Long targetId, TargetType targetType);

    long getLikeCountByType(Long targetId, TargetType targetType, ReactionType reactionType);

    Map<ReactionType, Long> getReactionSummary(Long targetId, TargetType targetType);

    LikeResponse changeReaction(LikeRequest request);
}
