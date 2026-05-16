package com.connectsphere.post.controller;

import com.connectsphere.post.dto.CreatePostRequest;
import com.connectsphere.post.dto.PostResponse;
import com.connectsphere.post.dto.PostStatsResponse;
import com.connectsphere.post.dto.UpdatePostRequest;
import com.connectsphere.post.enums.ModerationStatus;
import com.connectsphere.post.enums.Visibility;
import com.connectsphere.post.service.PostService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
@Validated
public class PostController {

    private final PostService postService;

    @PostMapping
    public PostResponse createPost(@Valid @RequestBody CreatePostRequest request) {
        return postService.createPost(request);
    }

    @GetMapping("/{id}")
    public PostResponse getPostById(@PathVariable @Positive Long id) {
        return postService.getPostById(id);
    }

    @GetMapping
    public List<PostResponse> getAllPosts() {
        return postService.getAllPosts();
    }

    @GetMapping("/public")
    public List<PostResponse> getPublicPosts() {
        return postService.getPublicPosts();
    }

    @GetMapping("/author/{authorId}")
    public List<PostResponse> getPostsByAuthorId(@PathVariable @Positive Long authorId) {
        return postService.getPostsByAuthorId(authorId);
    }

    @GetMapping("/feed")
    public List<PostResponse> getFeed(@RequestParam List<Long> authorIds) {
        return postService.getFeedForUser(authorIds);
    }

    @GetMapping("/search")
    public List<PostResponse> searchPosts(@RequestParam @NotBlank String keyword) {
        return postService.searchPosts(keyword);
    }

    @PutMapping("/{id}")
    public PostResponse updatePost(@PathVariable @Positive Long id, @Valid @RequestBody UpdatePostRequest request) {
        return postService.updatePost(id, request);
    }

    @PutMapping("/{id}/visibility")
    public PostResponse changeVisibility(@PathVariable @Positive Long id, @RequestParam Visibility visibility) {
        return postService.changeVisibility(id, visibility);
    }

    @GetMapping("/flagged")
    public List<PostResponse> getFlaggedPosts() {
        return postService.getFlaggedPosts();
    }

    @PutMapping("/{id}/moderation")
    public PostResponse moderatePost(@PathVariable @Positive Long id, @RequestParam ModerationStatus status) {
        return postService.moderatePost(id, status);
    }

    @GetMapping("/stats")
    public PostStatsResponse getPostStats() {
        return postService.getPostStats();
    }

    @DeleteMapping("/{id}")
    public Map<String, String> deletePost(@PathVariable @Positive Long id) {
        postService.deletePost(id);
        return Map.of("message", "Post deleted successfully");
    }

    @PutMapping("/{id}/increment-like")
    public Map<String, String> incrementLike(@PathVariable @Positive Long id) {
        postService.incrementLikes(id);
        return Map.of("message", "Post like count incremented");
    }

    @PutMapping("/{id}/decrement-like")
    public Map<String, String> decrementLike(@PathVariable @Positive Long id) {
        postService.decrementLikes(id);
        return Map.of("message", "Post like count decremented");
    }

    @PutMapping("/{id}/increment-comment")
    public Map<String, String> incrementComment(@PathVariable @Positive Long id) {
        postService.incrementComments(id);
        return Map.of("message", "Post comment count incremented");
    }

    @PutMapping("/{id}/decrement-comment")
    public Map<String, String> decrementComment(@PathVariable @Positive Long id) {
        postService.decrementComments(id);
        return Map.of("message", "Post comment count decremented");
    }
}
