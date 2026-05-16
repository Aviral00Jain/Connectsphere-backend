package com.connectsphere.comment.service.impl;

import com.connectsphere.comment.client.PostServiceClient;
import com.connectsphere.comment.dto.CommentResponse;
import com.connectsphere.comment.dto.CreateCommentRequest;
import com.connectsphere.comment.dto.UpdateCommentRequest;
import com.connectsphere.comment.entity.Comment;
import com.connectsphere.comment.exception.ResourceNotFoundException;
import com.connectsphere.comment.repository.CommentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentServiceImplTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostServiceClient postServiceClient;

    @InjectMocks
    private CommentServiceImpl commentService;

    @Test
    void createCommentShouldPersistAndIncrementPostCount() {
        CreateCommentRequest request = new CreateCommentRequest();
        request.setPostId(10L);
        request.setAuthorId(20L);
        request.setContent("Nice post");

        Comment savedComment = Comment.builder()
                .id(1L)
                .postId(10L)
                .authorId(20L)
                .content("Nice post")
                .likesCount(0)
                .deleted(false)
                .build();

        when(commentRepository.save(any(Comment.class))).thenReturn(savedComment);

        CommentResponse response = commentService.createComment(request);

        assertEquals(1L, response.getId());
        assertEquals(10L, response.getPostId());
        assertEquals(0, response.getLikesCount());
        verify(postServiceClient).incrementCommentCount(10L);
    }

    @Test
    void getCommentByIdShouldRejectDeletedComments() {
        Comment comment = Comment.builder()
                .id(5L)
                .deleted(true)
                .build();

        when(commentRepository.findById(5L)).thenReturn(Optional.of(comment));

        assertThrows(ResourceNotFoundException.class, () -> commentService.getCommentById(5L));
    }

    @Test
    void getCommentsByPostIdShouldMapRepositoryResults() {
        Comment comment = Comment.builder()
                .id(3L)
                .postId(7L)
                .authorId(8L)
                .parentCommentId(null)
                .content("comment")
                .likesCount(null)
                .build();

        when(commentRepository.findByPostIdAndParentCommentIdIsNullAndDeletedFalse(7L)).thenReturn(List.of(comment));

        List<CommentResponse> responses = commentService.getCommentsByPostId(7L);

        assertEquals(1, responses.size());
        assertEquals(0, responses.get(0).getLikesCount());
    }

    @Test
    void getRepliesShouldMapRepositoryResults() {
        when(commentRepository.findByParentCommentIdAndDeletedFalse(11L)).thenReturn(List.of(
                Comment.builder().id(1L).parentCommentId(11L).content("reply").build()
        ));

        List<CommentResponse> responses = commentService.getReplies(11L);

        assertEquals(1, responses.size());
        assertEquals("reply", responses.get(0).getContent());
    }

    @Test
    void getCommentsByUserShouldMapRepositoryResults() {
        when(commentRepository.findByAuthorIdAndDeletedFalse(22L)).thenReturn(List.of(
                Comment.builder().id(2L).authorId(22L).content("mine").build()
        ));

        List<CommentResponse> responses = commentService.getCommentsByUser(22L);

        assertEquals(1, responses.size());
        assertEquals(22L, responses.get(0).getAuthorId());
    }

    @Test
    void updateCommentShouldPersistNewContent() {
        Comment comment = Comment.builder().id(4L).content("old").build();
        UpdateCommentRequest request = new UpdateCommentRequest();
        request.setContent("updated");

        when(commentRepository.findById(4L)).thenReturn(Optional.of(comment));
        when(commentRepository.save(comment)).thenReturn(comment);

        CommentResponse response = commentService.updateComment(4L, request);

        assertEquals("updated", response.getContent());
    }

    @Test
    void deleteCommentShouldSoftDeleteAndDecrementPostCount() {
        Comment comment = Comment.builder()
                .id(9L)
                .postId(4L)
                .deleted(false)
                .build();

        when(commentRepository.findById(9L)).thenReturn(Optional.of(comment));

        commentService.deleteComment(9L);

        assertTrue(comment.isDeleted());
        verify(commentRepository).save(comment);
        verify(postServiceClient).decrementCommentCount(4L);
    }

    @Test
    void incrementLikesShouldIncreaseCounter() {
        Comment comment = Comment.builder().id(3L).likesCount(null).build();
        when(commentRepository.findById(3L)).thenReturn(Optional.of(comment));

        commentService.incrementLikes(3L);

        assertEquals(1, comment.getLikesCount());
        verify(commentRepository).save(comment);
    }

    @Test
    void decrementLikesShouldNotGoBelowZero() {
        Comment comment = Comment.builder().id(3L).likesCount(0).build();
        when(commentRepository.findById(3L)).thenReturn(Optional.of(comment));

        commentService.decrementLikes(3L);

        assertEquals(0, comment.getLikesCount());
        verify(commentRepository).save(comment);
    }
}
