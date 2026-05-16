package com.connectsphere.like.controller;

import com.connectsphere.like.dto.LikeRequest;
import com.connectsphere.like.dto.LikeResponse;
import com.connectsphere.like.entity.LikeEntity;
import com.connectsphere.like.enums.ReactionType;
import com.connectsphere.like.enums.TargetType;
import com.connectsphere.like.service.LikeService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/likes")
@RequiredArgsConstructor
@Validated
public class LikeController {

    private final LikeService likeService;

    @PostMapping
    public LikeResponse likeTarget(@Valid @RequestBody LikeRequest request) {
        return likeService.likeTarget(request);
    }

    @DeleteMapping
    public Map<String, String> unlikeTarget(@RequestParam @Positive Long userId,
                                            @RequestParam @Positive Long targetId,
                                            @RequestParam TargetType targetType) {
        String message = likeService.unlikeTarget(userId, targetId, targetType);
        return Map.of("message", message);
    }

    @GetMapping("/target")
    public List<LikeEntity> getLikesByTarget(@RequestParam @Positive Long targetId,
                                             @RequestParam TargetType targetType) {
        return likeService.getLikesByTarget(targetId, targetType);
    }

    @GetMapping("/user/{userId}")
    public List<LikeEntity> getLikesByUser(@PathVariable @Positive Long userId) {
        return likeService.getLikesByUser(userId);
    }

    @GetMapping("/has-liked")
    public Map<String, Boolean> hasLiked(@RequestParam @Positive Long userId,
                                         @RequestParam @Positive Long targetId,
                                         @RequestParam TargetType targetType) {
        return Map.of("hasLiked", likeService.hasLiked(userId, targetId, targetType));
    }

    @GetMapping("/count")
    public Map<String, Long> getLikeCount(@RequestParam @Positive Long targetId,
                                          @RequestParam TargetType targetType) {
        return Map.of("likeCount", likeService.getLikeCount(targetId, targetType));
    }

    @GetMapping("/count-by-type")
    public Map<String, Long> getLikeCountByType(@RequestParam @Positive Long targetId,
                                                @RequestParam TargetType targetType,
                                                @RequestParam ReactionType reactionType) {
        return Map.of("reactionCount", likeService.getLikeCountByType(targetId, targetType, reactionType));
    }

    @GetMapping("/summary")
    public Map<ReactionType, Long> getReactionSummary(@RequestParam @Positive Long targetId,
                                                      @RequestParam TargetType targetType) {
        return likeService.getReactionSummary(targetId, targetType);
    }

    @PutMapping("/change-reaction")
    public LikeResponse changeReaction(@Valid @RequestBody LikeRequest request) {
        return likeService.changeReaction(request);
    }
}
