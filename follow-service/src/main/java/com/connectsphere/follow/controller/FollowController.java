package com.connectsphere.follow.controller;

import com.connectsphere.follow.dto.FollowRequest;
import com.connectsphere.follow.service.FollowService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/follows")
@RequiredArgsConstructor
@Validated
public class FollowController {

    private final FollowService followService;

    @PostMapping
    public Map<String, String> follow(@Valid @RequestBody FollowRequest request) {
        followService.followUser(request.getFollowerId(), request.getFollowingId());
        return Map.of("message", "Followed successfully");
    }

    @DeleteMapping
    public Map<String, String> unfollow(@RequestParam @Positive Long followerId,
                                        @RequestParam @Positive Long followingId) {
        followService.unfollowUser(followerId, followingId);
        return Map.of("message", "Unfollowed successfully");
    }

    @GetMapping("/followers/{userId}")
    public List<Long> getFollowers(@PathVariable @Positive Long userId) {
        return followService.getFollowers(userId);
    }

    @GetMapping("/following/{userId}")
    public List<Long> getFollowing(@PathVariable @Positive Long userId) {
        return followService.getFollowing(userId);
    }

    @GetMapping("/is-following")
    public Map<String, Boolean> isFollowing(@RequestParam @Positive Long followerId,
                                            @RequestParam @Positive Long followingId) {
        return Map.of("isFollowing", followService.isFollowing(followerId, followingId));
    }

    @GetMapping("/followers/{userId}/count")
    public Map<String, Long> getFollowerCount(@PathVariable @Positive Long userId) {
        return Map.of("followerCount", followService.getFollowerCount(userId));
    }

    @GetMapping("/following/{userId}/count")
    public Map<String, Long> getFollowingCount(@PathVariable @Positive Long userId) {
        return Map.of("followingCount", followService.getFollowingCount(userId));
    }

    @GetMapping("/mutual")
    public List<Long> getMutualFollows(@RequestParam @Positive Long firstUserId,
                                       @RequestParam @Positive Long secondUserId) {
        return followService.getMutualFollows(firstUserId, secondUserId);
    }

    @GetMapping("/suggested/{userId}")
    public List<Long> getSuggestedUsers(@PathVariable @Positive Long userId) {
        return followService.getSuggestedUsers(userId);
    }
}
