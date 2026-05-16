package com.connectsphere.like.service.impl;

import com.connectsphere.like.client.CommentServiceClient;
import com.connectsphere.like.client.PostServiceClient;
import com.connectsphere.like.dto.LikeRequest;
import com.connectsphere.like.dto.LikeResponse;
import com.connectsphere.like.entity.LikeEntity;
import com.connectsphere.like.enums.ReactionType;
import com.connectsphere.like.enums.TargetType;
import com.connectsphere.like.repository.LikeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LikeServiceImplTest {

    @Mock
    private LikeRepository likeRepository;

    @Mock
    private PostServiceClient postServiceClient;

    @Mock
    private CommentServiceClient commentServiceClient;

    @InjectMocks
    private LikeServiceImpl likeService;

    @Test
    void likeTargetShouldDefaultReactionToLikeAndIncrementPostCount() {
        LikeRequest request = new LikeRequest();
        request.setUserId(1L);
        request.setTargetId(2L);
        request.setTargetType(TargetType.POST);

        LikeEntity saved = LikeEntity.builder()
                .likeId(99L)
                .userId(1L)
                .targetId(2L)
                .targetType(TargetType.POST)
                .reactionType(ReactionType.LIKE)
                .build();

        when(likeRepository.findByUserIdAndTargetIdAndTargetType(1L, 2L, TargetType.POST))
                .thenReturn(Optional.empty());
        when(likeRepository.save(any(LikeEntity.class))).thenReturn(saved);

        LikeResponse response = likeService.likeTarget(request);

        assertEquals(ReactionType.LIKE, response.getReactionType());
        verify(postServiceClient).incrementLikeCount(2L);
    }

    @Test
    void likeTargetShouldThrowWhenAlreadyReacted() {
        LikeRequest request = new LikeRequest();
        request.setUserId(1L);
        request.setTargetId(2L);
        request.setTargetType(TargetType.POST);

        when(likeRepository.findByUserIdAndTargetIdAndTargetType(1L, 2L, TargetType.POST))
                .thenReturn(Optional.of(LikeEntity.builder().build()));

        assertThrows(RuntimeException.class, () -> likeService.likeTarget(request));
    }

    @Test
    void unlikeTargetShouldDeleteReactionAndDecrementCommentCount() {
        LikeEntity entity = LikeEntity.builder()
                .likeId(4L)
                .userId(1L)
                .targetId(6L)
                .targetType(TargetType.COMMENT)
                .build();

        when(likeRepository.findByUserIdAndTargetIdAndTargetType(1L, 6L, TargetType.COMMENT))
                .thenReturn(Optional.of(entity));

        String message = likeService.unlikeTarget(1L, 6L, TargetType.COMMENT);

        assertEquals("Reaction removed successfully", message);
        verify(likeRepository).delete(entity);
        verify(commentServiceClient).decrementLikeCount(6L);
    }

    @Test
    void hasLikedShouldReturnFalseWhenMissing() {
        when(likeRepository.findByUserIdAndTargetIdAndTargetType(1L, 2L, TargetType.POST))
                .thenReturn(Optional.empty());

        assertFalse(likeService.hasLiked(1L, 2L, TargetType.POST));
    }

    @Test
    void getLikesByTargetShouldReturnRepositoryResults() {
        LikeEntity entity = LikeEntity.builder().likeId(1L).targetId(2L).targetType(TargetType.POST).build();
        when(likeRepository.findByTargetIdAndTargetType(2L, TargetType.POST)).thenReturn(List.of(entity));

        List<LikeEntity> likes = likeService.getLikesByTarget(2L, TargetType.POST);

        assertEquals(1, likes.size());
    }

    @Test
    void getLikesByUserShouldReturnRepositoryResults() {
        when(likeRepository.findByUserId(9L)).thenReturn(List.of(LikeEntity.builder().userId(9L).build()));

        assertEquals(1, likeService.getLikesByUser(9L).size());
    }

    @Test
    void getLikeCountShouldDelegateToRepository() {
        when(likeRepository.countByTargetIdAndTargetType(8L, TargetType.POST)).thenReturn(3L);

        assertEquals(3L, likeService.getLikeCount(8L, TargetType.POST));
    }

    @Test
    void getLikeCountByTypeShouldDelegateToRepository() {
        when(likeRepository.countByTargetIdAndTargetTypeAndReactionType(8L, TargetType.POST, ReactionType.LOVE)).thenReturn(2L);

        assertEquals(2L, likeService.getLikeCountByType(8L, TargetType.POST, ReactionType.LOVE));
    }

    @Test
    void getReactionSummaryShouldIncludeAllReactionTypes() {
        for (ReactionType reactionType : ReactionType.values()) {
            when(likeRepository.countByTargetIdAndTargetTypeAndReactionType(5L, TargetType.POST, reactionType)).thenReturn(1L);
        }

        Map<ReactionType, Long> summary = likeService.getReactionSummary(5L, TargetType.POST);

        assertEquals(ReactionType.values().length, summary.size());
        assertEquals(1L, summary.get(ReactionType.LIKE));
    }

    @Test
    void changeReactionShouldThrowWhenExistingReactionMissing() {
        LikeRequest request = new LikeRequest();
        request.setUserId(1L);
        request.setTargetId(2L);
        request.setTargetType(TargetType.POST);
        request.setReactionType(ReactionType.LOVE);

        when(likeRepository.findByUserIdAndTargetIdAndTargetType(1L, 2L, TargetType.POST))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> likeService.changeReaction(request));
    }

    @Test
    void changeReactionShouldPersistUpdatedReaction() {
        LikeRequest request = new LikeRequest();
        request.setUserId(1L);
        request.setTargetId(2L);
        request.setTargetType(TargetType.POST);
        request.setReactionType(ReactionType.WOW);

        LikeEntity existing = LikeEntity.builder()
                .likeId(6L)
                .userId(1L)
                .targetId(2L)
                .targetType(TargetType.POST)
                .reactionType(ReactionType.LIKE)
                .build();

        when(likeRepository.findByUserIdAndTargetIdAndTargetType(1L, 2L, TargetType.POST))
                .thenReturn(Optional.of(existing));
        when(likeRepository.save(existing)).thenReturn(existing);

        LikeResponse response = likeService.changeReaction(request);

        assertEquals(ReactionType.WOW, response.getReactionType());
    }
}
