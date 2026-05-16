package com.connectsphere.comment.service.impl;

import com.connectsphere.comment.client.PostServiceClient;
import com.connectsphere.comment.dto.CommentResponse;
import com.connectsphere.comment.dto.CreateCommentRequest;
import com.connectsphere.comment.dto.UpdateCommentRequest;
import com.connectsphere.comment.entity.Comment;
import com.connectsphere.comment.exception.ResourceNotFoundException;
import com.connectsphere.comment.repository.CommentRepository;
import com.connectsphere.comment.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final PostServiceClient postServiceClient;

    @Override
    public CommentResponse createComment(CreateCommentRequest request) {
        Comment comment = Comment.builder()
                .postId(request.getPostId())
                .authorId(request.getAuthorId())
                .parentCommentId(request.getParentCommentId())
                .content(request.getContent())
                .likesCount(0)
                .deleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Comment savedComment = commentRepository.save(comment);

        postServiceClient.incrementCommentCount(request.getPostId());

        return mapToResponse(savedComment);
    }

    @Override
    public CommentResponse getCommentById(Long id) {
        Comment comment = commentRepository.findById(id)
                .filter(c -> !c.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + id));

        return mapToResponse(comment);
    }

    @Override
    public List<CommentResponse> getCommentsByPostId(Long postId) {
        return commentRepository.findByPostIdAndParentCommentIdIsNullAndDeletedFalse(postId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<CommentResponse> getReplies(Long parentCommentId) {
        return commentRepository.findByParentCommentIdAndDeletedFalse(parentCommentId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<CommentResponse> getCommentsByUser(Long authorId) {
        return commentRepository.findByAuthorIdAndDeletedFalse(authorId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public CommentResponse updateComment(Long id, UpdateCommentRequest request) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + id));

        comment.setContent(request.getContent());
        comment.setUpdatedAt(LocalDateTime.now());
        return mapToResponse(commentRepository.save(comment));
    }

    @Override
    public void deleteComment(Long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + id));

        comment.setDeleted(true);
        comment.setUpdatedAt(LocalDateTime.now());
        commentRepository.save(comment);

        postServiceClient.decrementCommentCount(comment.getPostId());
    }

    @Override
    public void incrementLikes(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + commentId));

        comment.setLikesCount((comment.getLikesCount() == null ? 0 : comment.getLikesCount()) + 1);
        comment.setUpdatedAt(LocalDateTime.now());
        commentRepository.save(comment);
    }

    @Override
    public void decrementLikes(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + commentId));

        comment.setLikesCount(Math.max(0, (comment.getLikesCount() == null ? 0 : comment.getLikesCount()) - 1));
        comment.setUpdatedAt(LocalDateTime.now());
        commentRepository.save(comment);
    }

    private CommentResponse mapToResponse(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .postId(comment.getPostId())
                .authorId(comment.getAuthorId())
                .parentCommentId(comment.getParentCommentId())
                .content(comment.getContent())
                .likesCount(comment.getLikesCount() == null ? 0 : comment.getLikesCount())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
}
