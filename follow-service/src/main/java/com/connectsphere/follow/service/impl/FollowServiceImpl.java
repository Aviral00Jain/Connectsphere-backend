package com.connectsphere.follow.service.impl;

import com.connectsphere.follow.entity.Follow;
import com.connectsphere.follow.exception.ResourceNotFoundException;
import com.connectsphere.follow.messaging.NotificationEventPublisher;
import com.connectsphere.follow.repository.FollowRepository;
import com.connectsphere.follow.service.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FollowServiceImpl implements FollowService {

    private final FollowRepository followRepository;
    private final NotificationEventPublisher notificationEventPublisher;

    @Override
    public void followUser(Long followerId, Long followingId) {

        if (followerId.equals(followingId)) {
            throw new RuntimeException("You cannot follow yourself");
        }

        if (followRepository.existsByFollowerIdAndFollowingId(followerId, followingId)) {
            throw new RuntimeException("Already following this user");
        }

        Follow follow = Follow.builder()
                .followerId(followerId)
                .followingId(followingId)
                .createdAt(LocalDateTime.now())
                .build();

        followRepository.save(follow);
        notificationEventPublisher.publishNewFollower(followerId, followingId);
    }

    @Override
    public void unfollowUser(Long followerId, Long followingId) {
        Follow follow = followRepository.findByFollowerIdAndFollowingId(followerId, followingId)
                .orElseThrow(() -> new ResourceNotFoundException("Follow relation not found"));

        followRepository.delete(follow);
    }

    @Override
    public List<Long> getFollowers(Long userId) {
        return followRepository.findByFollowingId(userId)
                .stream()
                .map(Follow::getFollowerId)
                .toList();
    }

    @Override
    public List<Long> getFollowing(Long userId) {
        return followRepository.findByFollowerId(userId)
                .stream()
                .map(Follow::getFollowingId)
                .toList();
    }

    @Override
    public boolean isFollowing(Long followerId, Long followingId) {
        return followRepository.existsByFollowerIdAndFollowingId(followerId, followingId);
    }

    @Override
    public long getFollowerCount(Long userId) {
        return followRepository.countByFollowingId(userId);
    }

    @Override
    public long getFollowingCount(Long userId) {
        return followRepository.countByFollowerId(userId);
    }

    @Override
    public List<Long> getMutualFollows(Long firstUserId, Long secondUserId) {
        List<Long> firstUserFollowing = getFollowing(firstUserId);
        return getFollowing(secondUserId)
                .stream()
                .filter(firstUserFollowing::contains)
                .toList();
    }

    @Override
    public List<Long> getSuggestedUsers(Long userId) {
        List<Long> directFollowing = getFollowing(userId);

        return directFollowing.stream()
                .flatMap(followingId -> getFollowing(followingId).stream())
                .filter(suggestedId -> !suggestedId.equals(userId))
                .filter(suggestedId -> !directFollowing.contains(suggestedId))
                .distinct()
                .toList();
    }
}
