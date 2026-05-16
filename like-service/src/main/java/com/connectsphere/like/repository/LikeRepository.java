package com.connectsphere.like.repository;

import com.connectsphere.like.entity.LikeEntity;
import com.connectsphere.like.enums.ReactionType;
import com.connectsphere.like.enums.TargetType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LikeRepository extends JpaRepository<LikeEntity, Long> {

    Optional<LikeEntity> findByUserIdAndTargetIdAndTargetType(Long userId, Long targetId, TargetType targetType);

    List<LikeEntity> findByTargetIdAndTargetType(Long targetId, TargetType targetType);

    List<LikeEntity> findByUserId(Long userId);

    long countByTargetIdAndTargetTypeAndReactionType(Long targetId, TargetType targetType, ReactionType reactionType);

    long countByTargetIdAndTargetType(Long targetId, TargetType targetType);
}
