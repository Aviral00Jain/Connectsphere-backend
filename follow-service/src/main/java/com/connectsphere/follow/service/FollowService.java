package com.connectsphere.follow.service;

import java.util.List;

public interface FollowService {

    void followUser(Long followerId, Long followingId);

    void unfollowUser(Long followerId, Long followingId);

    List<Long> getFollowers(Long userId);

    List<Long> getFollowing(Long userId);

    boolean isFollowing(Long followerId, Long followingId);

    long getFollowerCount(Long userId);

    long getFollowingCount(Long userId);

    List<Long> getMutualFollows(Long firstUserId, Long secondUserId);

    List<Long> getSuggestedUsers(Long userId);
}
