package com.connectsphere.post.service;

import com.connectsphere.post.dto.CreatePostRequest;
import com.connectsphere.post.dto.PostResponse;
import com.connectsphere.post.dto.PostStatsResponse;
import com.connectsphere.post.dto.UpdatePostRequest;
import com.connectsphere.post.enums.ModerationStatus;
import com.connectsphere.post.enums.Visibility;

import java.util.List;

public interface PostService {
    PostResponse createPost(CreatePostRequest request);
    PostResponse getPostById(Long id);
    List<PostResponse> getAllPosts();
    List<PostResponse> getPublicPosts();
    List<PostResponse> getPostsByAuthorId(Long authorId);
    List<PostResponse> getFeedForUser(List<Long> followeeIds);
    List<PostResponse> searchPosts(String keyword);
    PostResponse updatePost(Long id, UpdatePostRequest request);
    PostResponse changeVisibility(Long id, Visibility visibility);
    List<PostResponse> getFlaggedPosts();
    PostResponse moderatePost(Long id, ModerationStatus moderationStatus);
    PostStatsResponse getPostStats();
    void deletePost(Long id);

    void incrementLikes(Long postId);
    void decrementLikes(Long postId);

    void incrementComments(Long postId);
    void decrementComments(Long postId);
}
