package com.connectsphere.comment.service;

import com.connectsphere.comment.dto.CommentResponse;
import com.connectsphere.comment.dto.CreateCommentRequest;
import com.connectsphere.comment.dto.UpdateCommentRequest;

import java.util.List;

public interface CommentService {

    CommentResponse createComment(CreateCommentRequest request);

    CommentResponse getCommentById(Long id);

    List<CommentResponse> getCommentsByPostId(Long postId);

    List<CommentResponse> getReplies(Long parentCommentId);

    List<CommentResponse> getCommentsByUser(Long authorId);

    CommentResponse updateComment(Long id, UpdateCommentRequest request);

    void deleteComment(Long id);

    void incrementLikes(Long commentId);

    void decrementLikes(Long commentId);
}
