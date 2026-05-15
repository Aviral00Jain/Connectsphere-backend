package com.connectsphere.post.service.impl;

import com.connectsphere.post.dto.CreatePostRequest;
import com.connectsphere.post.dto.PostResponse;
import com.connectsphere.post.dto.PostStatsResponse;
import com.connectsphere.post.dto.UpdatePostRequest;
import com.connectsphere.post.entity.Post;
import com.connectsphere.post.enums.ModerationStatus;
import com.connectsphere.post.enums.Visibility;
import com.connectsphere.post.exception.ResourceNotFoundException;
import com.connectsphere.post.repository.PostRepository;
import com.connectsphere.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private static final Set<String> FLAGGED_TERMS = Set.of("abuse", "hate", "violent");

    private final PostRepository postRepository;

    @Override
    public PostResponse createPost(CreatePostRequest request) {
        String normalizedContent = normalizeContent(request.getContent());
        validatePostPayload(normalizedContent, request.getMediaUrls());

        Post post = Post.builder()
                .authorId(request.getAuthorId())
                .content(normalizedContent)
                .mediaUrls(request.getMediaUrls())
                .postType(request.getPostType())
                .visibility(request.getVisibility())
                .likesCount(0)
                .commentsCount(0)
                .sharesCount(0)
                .flagged(containsFlaggedContent(normalizedContent))
                .moderationStatus(containsFlaggedContent(normalizedContent) ? ModerationStatus.FLAGGED : ModerationStatus.APPROVED)
                .deleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Post savedPost = postRepository.save(post);
        return mapToResponse(savedPost);
    }

    @Override
    public PostResponse getPostById(Long id) {
        Post post = postRepository.findById(id)
                .filter(p -> !p.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + id));

        return mapToResponse(post);
    }

    @Override
    public List<PostResponse> getAllPosts() {
        return postRepository.findByDeletedFalse()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<PostResponse> getPublicPosts() {
        return postRepository.findByVisibilityAndDeletedFalse(Visibility.PUBLIC)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<PostResponse> getPostsByAuthorId(Long authorId) {
        return postRepository.findByAuthorIdAndDeletedFalse(authorId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<PostResponse> getFeedForUser(List<Long> followeeIds) {
        if (followeeIds == null || followeeIds.isEmpty()) {
            return List.of();
        }

        return postRepository.findByAuthorIdInAndDeletedFalseOrderByCreatedAtDesc(followeeIds)
                .stream()
                .filter(post -> post.getVisibility() != Visibility.PRIVATE)
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<PostResponse> searchPosts(String keyword) {
        return postRepository.searchByContent(keyword)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public PostResponse updatePost(Long id, UpdatePostRequest request) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + id));

        String normalizedContent = normalizeContent(request.getContent());
        validatePostPayload(normalizedContent, request.getMediaUrls());

        post.setContent(normalizedContent);
        post.setMediaUrls(request.getMediaUrls());
        post.setPostType(request.getPostType());
        if (request.getVisibility() != null) {
            post.setVisibility(request.getVisibility());
        }
        post.setFlagged(containsFlaggedContent(normalizedContent));
        post.setModerationStatus(containsFlaggedContent(normalizedContent) ? ModerationStatus.FLAGGED : ModerationStatus.APPROVED);
        post.setUpdatedAt(LocalDateTime.now());

        return mapToResponse(postRepository.save(post));
    }

    @Override
    public PostResponse changeVisibility(Long id, Visibility visibility) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + id));

        post.setVisibility(visibility);
        post.setUpdatedAt(LocalDateTime.now());
        return mapToResponse(postRepository.save(post));
    }

    @Override
    public List<PostResponse> getFlaggedPosts() {
        return postRepository.findByFlaggedTrueAndDeletedFalseOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public PostResponse moderatePost(Long id, ModerationStatus moderationStatus) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + id));

        post.setModerationStatus(moderationStatus);
        post.setFlagged(moderationStatus == ModerationStatus.FLAGGED);
        if (moderationStatus == ModerationStatus.REMOVED) {
            post.setDeleted(true);
        }
        post.setUpdatedAt(LocalDateTime.now());
        return mapToResponse(postRepository.save(post));
    }

    @Override
    public PostStatsResponse getPostStats() {
        return PostStatsResponse.builder()
                .totalPosts(postRepository.countByDeletedFalse())
                .flaggedPosts(postRepository.countByFlaggedTrueAndDeletedFalse())
                .build();
    }

    @Override
    public void deletePost(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + id));

        post.setDeleted(true);
        post.setUpdatedAt(LocalDateTime.now());
        postRepository.save(post);
    }

    @Override
    public void incrementLikes(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));

        post.setLikesCount((post.getLikesCount() == null ? 0 : post.getLikesCount()) + 1);
        post.setUpdatedAt(LocalDateTime.now());
        postRepository.save(post);
    }

    @Override
    public void decrementLikes(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));

        post.setLikesCount(Math.max(0, (post.getLikesCount() == null ? 0 : post.getLikesCount()) - 1));
        post.setUpdatedAt(LocalDateTime.now());
        postRepository.save(post);
    }

    @Override
    public void incrementComments(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));

        post.setCommentsCount((post.getCommentsCount() == null ? 0 : post.getCommentsCount()) + 1);
        post.setUpdatedAt(LocalDateTime.now());
        postRepository.save(post);
    }

    @Override
    public void decrementComments(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));

        post.setCommentsCount(Math.max(0, (post.getCommentsCount() == null ? 0 : post.getCommentsCount()) - 1));
        post.setUpdatedAt(LocalDateTime.now());
        postRepository.save(post);
    }

    private PostResponse mapToResponse(Post post) {
        return PostResponse.builder()
                .id(post.getId())
                .authorId(post.getAuthorId())
                .content(post.getContent())
                .mediaUrls(post.getMediaUrls())
                .postType(post.getPostType())
                .visibility(post.getVisibility())
                .likesCount(post.getLikesCount() == null ? 0 : post.getLikesCount())
                .commentsCount(post.getCommentsCount() == null ? 0 : post.getCommentsCount())
                .sharesCount(post.getSharesCount() == null ? 0 : post.getSharesCount())
                .flagged(post.isFlagged())
                .moderationStatus(post.getModerationStatus())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }

    private boolean containsFlaggedContent(String content) {
        if (content == null) {
            return false;
        }

        String normalized = content.toLowerCase();
        return FLAGGED_TERMS.stream().anyMatch(normalized::contains);
    }

    private String normalizeContent(String content) {
        if (content == null) {
            return null;
        }

        String normalized = content.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private void validatePostPayload(String content, List<String> mediaUrls) {
        boolean hasMedia = mediaUrls != null && !mediaUrls.isEmpty();
        if (content == null && !hasMedia) {
            throw new RuntimeException("Post must contain text or at least one photo/video");
        }
    }
}
