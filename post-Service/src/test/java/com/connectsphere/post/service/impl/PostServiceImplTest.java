package com.connectsphere.post.service.impl;

import com.connectsphere.post.dto.CreatePostRequest;
import com.connectsphere.post.dto.PostResponse;
import com.connectsphere.post.dto.UpdatePostRequest;
import com.connectsphere.post.entity.Post;
import com.connectsphere.post.enums.ModerationStatus;
import com.connectsphere.post.enums.Visibility;
import com.connectsphere.post.exception.ResourceNotFoundException;
import com.connectsphere.post.repository.PostRepository;
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
class PostServiceImplTest {

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private PostServiceImpl postService;

    @Test
    void createPostShouldInitializeCounters() {
        CreatePostRequest request = new CreatePostRequest();
        request.setAuthorId(1L);
        request.setContent("Hello world");
        request.setVisibility(Visibility.PUBLIC);

        Post savedPost = Post.builder()
                .id(10L)
                .authorId(1L)
                .content("Hello world")
                .visibility(Visibility.PUBLIC)
                .likesCount(0)
                .commentsCount(0)
                .deleted(false)
                .build();

        when(postRepository.save(any(Post.class))).thenReturn(savedPost);

        PostResponse response = postService.createPost(request);

        assertEquals(0, response.getLikesCount());
        assertEquals(0, response.getCommentsCount());
    }

    @Test
    void createPostShouldAllowMediaOnlyPosts() {
        CreatePostRequest request = new CreatePostRequest();
        request.setAuthorId(1L);
        request.setMediaUrls(List.of("data:image/png;base64,abc"));
        request.setPostType("IMAGE");
        request.setVisibility(Visibility.PUBLIC);

        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PostResponse response = postService.createPost(request);

        assertEquals(List.of("data:image/png;base64,abc"), response.getMediaUrls());
    }

    @Test
    void getPostByIdShouldThrowWhenDeleted() {
        Post post = Post.builder()
                .id(2L)
                .deleted(true)
                .build();

        when(postRepository.findById(2L)).thenReturn(Optional.of(post));

        assertThrows(ResourceNotFoundException.class, () -> postService.getPostById(2L));
    }

    @Test
    void getAllPostsShouldMapRepositoryResults() {
        when(postRepository.findByDeletedFalse()).thenReturn(List.of(
                Post.builder().id(1L).authorId(1L).content("post").visibility(Visibility.PUBLIC).likesCount(null).commentsCount(null).build()
        ));

        List<PostResponse> responses = postService.getAllPosts();

        assertEquals(1, responses.size());
        assertEquals(0, responses.get(0).getLikesCount());
        assertEquals(0, responses.get(0).getCommentsCount());
    }

    @Test
    void getPublicPostsShouldDelegateToRepository() {
        when(postRepository.findByVisibilityAndDeletedFalse(Visibility.PUBLIC)).thenReturn(List.of(
                Post.builder().id(1L).visibility(Visibility.PUBLIC).build()
        ));

        assertEquals(1, postService.getPublicPosts().size());
    }

    @Test
    void getPostsByAuthorIdShouldDelegateToRepository() {
        when(postRepository.findByAuthorIdAndDeletedFalse(5L)).thenReturn(List.of(
                Post.builder().id(1L).authorId(5L).build()
        ));

        assertEquals(1, postService.getPostsByAuthorId(5L).size());
    }

    @Test
    void getFeedForUserShouldFilterPrivatePosts() {
        when(postRepository.findByAuthorIdInAndDeletedFalseOrderByCreatedAtDesc(List.of(1L, 2L))).thenReturn(List.of(
                Post.builder().id(1L).visibility(Visibility.PUBLIC).build(),
                Post.builder().id(2L).visibility(Visibility.PRIVATE).build()
        ));

        List<PostResponse> feed = postService.getFeedForUser(List.of(1L, 2L));

        assertEquals(1, feed.size());
    }

    @Test
    void searchPostsShouldMapRepositoryResults() {
        when(postRepository.searchByContent("java")).thenReturn(List.of(
                Post.builder().id(1L).content("java").visibility(Visibility.PUBLIC).build()
        ));

        assertEquals(1, postService.searchPosts("java").size());
    }

    @Test
    void updatePostShouldPersistFields() {
        Post post = Post.builder().id(4L).content("old").visibility(Visibility.PUBLIC).build();
        UpdatePostRequest request = new UpdatePostRequest();
        request.setContent("updated");
        request.setVisibility(Visibility.FOLLOWERS_ONLY);
        request.setPostType("TEXT");
        request.setMediaUrls(List.of("url"));

        when(postRepository.findById(4L)).thenReturn(Optional.of(post));
        when(postRepository.save(post)).thenReturn(post);

        PostResponse response = postService.updatePost(4L, request);

        assertEquals("updated", response.getContent());
        assertEquals(Visibility.FOLLOWERS_ONLY, response.getVisibility());
    }

    @Test
    void changeVisibilityShouldPersistNewVisibility() {
        Post post = Post.builder().id(4L).visibility(Visibility.PUBLIC).build();
        when(postRepository.findById(4L)).thenReturn(Optional.of(post));
        when(postRepository.save(post)).thenReturn(post);

        PostResponse response = postService.changeVisibility(4L, Visibility.PRIVATE);

        assertEquals(Visibility.PRIVATE, response.getVisibility());
    }

    @Test
    void getFlaggedPostsShouldMapRepositoryResults() {
        when(postRepository.findByFlaggedTrueAndDeletedFalseOrderByCreatedAtDesc()).thenReturn(List.of(
                Post.builder().id(3L).flagged(true).build()
        ));

        assertEquals(1, postService.getFlaggedPosts().size());
    }

    @Test
    void moderatePostShouldMarkRemovedPostDeleted() {
        Post post = Post.builder().id(2L).deleted(false).build();
        when(postRepository.findById(2L)).thenReturn(Optional.of(post));
        when(postRepository.save(post)).thenReturn(post);

        PostResponse response = postService.moderatePost(2L, ModerationStatus.REMOVED);

        assertTrue(post.isDeleted());
        assertEquals(ModerationStatus.REMOVED, response.getModerationStatus());
    }

    @Test
    void getPostStatsShouldReturnRepositoryCounts() {
        when(postRepository.countByDeletedFalse()).thenReturn(9L);
        when(postRepository.countByFlaggedTrueAndDeletedFalse()).thenReturn(2L);

        assertEquals(9L, postService.getPostStats().getTotalPosts());
        assertEquals(2L, postService.getPostStats().getFlaggedPosts());
    }

    @Test
    void deletePostShouldSoftDeleteRecord() {
        Post post = Post.builder()
                .id(8L)
                .deleted(false)
                .build();

        when(postRepository.findById(8L)).thenReturn(Optional.of(post));

        postService.deletePost(8L);

        assertTrue(post.isDeleted());
        verify(postRepository).save(post);
    }

    @Test
    void incrementLikesShouldIncreaseCounter() {
        Post post = Post.builder().id(1L).likesCount(null).build();
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        postService.incrementLikes(1L);

        assertEquals(1, post.getLikesCount());
    }

    @Test
    void decrementLikesShouldNotGoBelowZero() {
        Post post = Post.builder().id(1L).likesCount(0).build();
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        postService.decrementLikes(1L);

        assertEquals(0, post.getLikesCount());
    }

    @Test
    void incrementCommentsShouldIncreaseCounter() {
        Post post = Post.builder().id(1L).commentsCount(null).build();
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        postService.incrementComments(1L);

        assertEquals(1, post.getCommentsCount());
    }

    @Test
    void decrementCommentsShouldNotGoBelowZero() {
        Post post = Post.builder().id(1L).commentsCount(0).build();
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        postService.decrementComments(1L);

        assertEquals(0, post.getCommentsCount());
    }
}
