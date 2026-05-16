package com.connectsphere.like.service.impl;

import com.connectsphere.like.client.CommentServiceClient;
import com.connectsphere.like.client.PostServiceClient;
import com.connectsphere.like.dto.LikeRequest;
import com.connectsphere.like.dto.LikeResponse;
import com.connectsphere.like.entity.LikeEntity;
import com.connectsphere.like.enums.ReactionType;
import com.connectsphere.like.enums.TargetType;
import com.connectsphere.like.repository.LikeRepository;
import com.connectsphere.like.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LikeServiceImpl implements LikeService {

    private final LikeRepository likeRepository;
    private final PostServiceClient postServiceClient;
    private final CommentServiceClient commentServiceClient;

    @Override
    public LikeResponse likeTarget(LikeRequest request) {
        likeRepository.findByUserIdAndTargetIdAndTargetType(
                request.getUserId(),
                request.getTargetId(),
                request.getTargetType()
        ).ifPresent(existing -> {
            throw new RuntimeException("You already reacted on this target");
        });

        LikeEntity likeEntity = LikeEntity.builder()
                .userId(request.getUserId())
                .targetId(request.getTargetId())
                .targetType(request.getTargetType())
                .reactionType(request.getReactionType() == null ? com.connectsphere.like.enums.ReactionType.LIKE : request.getReactionType())
                .createdAt(LocalDateTime.now())
                .build();

        LikeEntity saved = likeRepository.save(likeEntity);

        if (request.getTargetType() == TargetType.POST) {
            postServiceClient.incrementLikeCount(request.getTargetId());
        } else if (request.getTargetType() == TargetType.COMMENT) {
            commentServiceClient.incrementLikeCount(request.getTargetId());
        }

        return LikeResponse.builder()
                .likeId(saved.getLikeId())
                .userId(saved.getUserId())
                .targetId(saved.getTargetId())
                .targetType(saved.getTargetType())
                .reactionType(saved.getReactionType())
                .message("Reaction added successfully")
                .build();
    }

    @Override
    public String unlikeTarget(Long userId, Long targetId, TargetType targetType) {
        LikeEntity likeEntity = likeRepository.findByUserIdAndTargetIdAndTargetType(userId, targetId, targetType)
                .orElseThrow(() -> new RuntimeException("Reaction not found"));

        likeRepository.delete(likeEntity);

        if (targetType == TargetType.POST) {
            postServiceClient.decrementLikeCount(targetId);
        } else if (targetType == TargetType.COMMENT) {
            commentServiceClient.decrementLikeCount(targetId);
        }

        return "Reaction removed successfully";
    }

    @Override
    public boolean hasLiked(Long userId, Long targetId, TargetType targetType) {
        return likeRepository.findByUserIdAndTargetIdAndTargetType(userId, targetId, targetType).isPresent();
    }

    @Override
    public List<LikeEntity> getLikesByTarget(Long targetId, TargetType targetType) {
        return likeRepository.findByTargetIdAndTargetType(targetId, targetType);
    }

    @Override
    public List<LikeEntity> getLikesByUser(Long userId) {
        return likeRepository.findByUserId(userId);
    }

    @Override
    public long getLikeCount(Long targetId, TargetType targetType) {
        return likeRepository.countByTargetIdAndTargetType(targetId, targetType);
    }

    @Override
    public long getLikeCountByType(Long targetId, TargetType targetType, ReactionType reactionType) {
        return likeRepository.countByTargetIdAndTargetTypeAndReactionType(targetId, targetType, reactionType);
    }

    @Override
    public Map<ReactionType, Long> getReactionSummary(Long targetId, TargetType targetType) {
        Map<ReactionType, Long> summary = new EnumMap<>(ReactionType.class);
        for (ReactionType reactionType : ReactionType.values()) {
            summary.put(reactionType, getLikeCountByType(targetId, targetType, reactionType));
        }
        return summary;
    }

    @Override
    public LikeResponse changeReaction(LikeRequest request) {
        LikeEntity likeEntity = likeRepository.findByUserIdAndTargetIdAndTargetType(
                request.getUserId(),
                request.getTargetId(),
                request.getTargetType()
        ).orElseThrow(() -> new RuntimeException("Existing reaction not found"));

        likeEntity.setReactionType(request.getReactionType());
        LikeEntity updated = likeRepository.save(likeEntity);

        return LikeResponse.builder()
                .likeId(updated.getLikeId())
                .userId(updated.getUserId())
                .targetId(updated.getTargetId())
                .targetType(updated.getTargetType())
                .reactionType(updated.getReactionType())
                .message("Reaction changed successfully")
                .build();
    }
}
