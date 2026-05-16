package com.connectsphere.follow.service.impl;

import com.connectsphere.follow.entity.Follow;
import com.connectsphere.follow.exception.ResourceNotFoundException;
import com.connectsphere.follow.repository.FollowRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FollowServiceImplTest {

    @Mock
    private FollowRepository followRepository;

    @InjectMocks
    private FollowServiceImpl followService;

    @Test
    void followUserShouldSaveRelationWhenValid() {
        when(followRepository.existsByFollowerIdAndFollowingId(1L, 2L)).thenReturn(false);

        followService.followUser(1L, 2L);

        verify(followRepository).save(any(Follow.class));
    }

    @Test
    void followUserShouldRejectSelfFollow() {
        RuntimeException exception =
                assertThrows(RuntimeException.class, () -> followService.followUser(1L, 1L));

        assertEquals("You cannot follow yourself", exception.getMessage());
    }

    @Test
    void followUserShouldRejectDuplicateFollow() {
        when(followRepository.existsByFollowerIdAndFollowingId(1L, 2L)).thenReturn(true);

        RuntimeException exception =
                assertThrows(RuntimeException.class, () -> followService.followUser(1L, 2L));

        assertEquals("Already following this user", exception.getMessage());
    }

    @Test
    void unfollowUserShouldThrowWhenRelationMissing() {
        when(followRepository.findByFollowerIdAndFollowingId(1L, 2L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> followService.unfollowUser(1L, 2L));
    }

    @Test
    void getFollowersShouldReturnFollowerIds() {
        when(followRepository.findByFollowingId(2L)).thenReturn(List.of(
                Follow.builder().followerId(10L).followingId(2L).build(),
                Follow.builder().followerId(11L).followingId(2L).build()
        ));

        List<Long> followers = followService.getFollowers(2L);

        assertEquals(List.of(10L, 11L), followers);
    }

    @Test
    void getFollowingShouldReturnFollowingIds() {
        when(followRepository.findByFollowerId(2L)).thenReturn(List.of(
                Follow.builder().followerId(2L).followingId(10L).build(),
                Follow.builder().followerId(2L).followingId(11L).build()
        ));

        List<Long> following = followService.getFollowing(2L);

        assertEquals(List.of(10L, 11L), following);
    }

    @Test
    void isFollowingShouldDelegateToRepository() {
        when(followRepository.existsByFollowerIdAndFollowingId(1L, 2L)).thenReturn(false);

        assertFalse(followService.isFollowing(1L, 2L));
    }

    @Test
    void getFollowerCountShouldDelegateToRepository() {
        when(followRepository.countByFollowingId(8L)).thenReturn(4L);

        assertEquals(4L, followService.getFollowerCount(8L));
    }

    @Test
    void getFollowingCountShouldDelegateToRepository() {
        when(followRepository.countByFollowerId(8L)).thenReturn(5L);

        assertEquals(5L, followService.getFollowingCount(8L));
    }

    @Test
    void getMutualFollowsShouldReturnIntersection() {
        when(followRepository.findByFollowerId(1L)).thenReturn(List.of(
                Follow.builder().followingId(2L).build(),
                Follow.builder().followingId(3L).build()
        ));
        when(followRepository.findByFollowerId(9L)).thenReturn(List.of(
                Follow.builder().followingId(3L).build(),
                Follow.builder().followingId(4L).build()
        ));

        List<Long> mutual = followService.getMutualFollows(1L, 9L);

        assertEquals(List.of(3L), mutual);
    }

    @Test
    void getSuggestedUsersShouldFlattenSecondDegreeConnections() {
        when(followRepository.findByFollowerId(1L)).thenReturn(List.of(
                Follow.builder().followingId(2L).build(),
                Follow.builder().followingId(3L).build()
        ));
        when(followRepository.findByFollowerId(2L)).thenReturn(List.of(
                Follow.builder().followingId(4L).build(),
                Follow.builder().followingId(1L).build()
        ));
        when(followRepository.findByFollowerId(3L)).thenReturn(List.of(
                Follow.builder().followingId(4L).build(),
                Follow.builder().followingId(5L).build()
        ));

        List<Long> suggestions = followService.getSuggestedUsers(1L);

        assertEquals(List.of(4L, 5L), suggestions);
    }
}
