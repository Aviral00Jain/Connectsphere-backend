package com.connectsphere.comment.controller;

import com.connectsphere.comment.dto.CommentResponse;
import com.connectsphere.comment.dto.CreateCommentRequest;
import com.connectsphere.comment.dto.UpdateCommentRequest;
import com.connectsphere.comment.service.CommentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
@Validated
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public CommentResponse createComment(@Valid @RequestBody CreateCommentRequest request) {
        return commentService.createComment(request);
    }

    @GetMapping("/{id}")
    public CommentResponse getCommentById(@PathVariable @Positive Long id) {
        return commentService.getCommentById(id);
    }

    @GetMapping("/post/{postId}")
    public List<CommentResponse> getCommentsByPostId(@PathVariable @Positive Long postId) {
        return commentService.getCommentsByPostId(postId);
    }

    @GetMapping("/replies/{parentCommentId}")
    public List<CommentResponse> getReplies(@PathVariable @Positive Long parentCommentId) {
        return commentService.getReplies(parentCommentId);
    }

    @GetMapping("/author/{authorId}")
    public List<CommentResponse> getCommentsByUser(@PathVariable @Positive Long authorId) {
        return commentService.getCommentsByUser(authorId);
    }

    @PutMapping("/{id}")
    public CommentResponse updateComment(@PathVariable @Positive Long id, @Valid @RequestBody UpdateCommentRequest request) {
        return commentService.updateComment(id, request);
    }

    @DeleteMapping("/{id}")
    public Map<String, String> deleteComment(@PathVariable @Positive Long id) {
        commentService.deleteComment(id);
        return Map.of("message", "Comment deleted successfully");
    }

    @PutMapping("/{id}/increment-like")
    public Map<String, String> incrementLike(@PathVariable @Positive Long id) {
        commentService.incrementLikes(id);
        return Map.of("message", "Comment like count incremented");
    }

    @PutMapping("/{id}/decrement-like")
    public Map<String, String> decrementLike(@PathVariable @Positive Long id) {
        commentService.decrementLikes(id);
        return Map.of("message", "Comment like count decremented");
    }
}
