package com.connectsphere.media.service.impl;

import com.connectsphere.media.dto.CreateMediaRequest;
import com.connectsphere.media.dto.CreateStoryRequest;
import com.connectsphere.media.dto.MediaResponse;
import com.connectsphere.media.dto.StoryResponse;
import com.connectsphere.media.entity.Media;
import com.connectsphere.media.entity.Story;
import com.connectsphere.media.repository.MediaRepository;
import com.connectsphere.media.repository.StoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MediaServiceImplTest {

    @Mock
    private MediaRepository mediaRepository;

    @Mock
    private StoryRepository storyRepository;

    @InjectMocks
    private MediaServiceImpl mediaService;

    @Test
    void createMediaShouldReturnMappedResponse() {
        CreateMediaRequest request = new CreateMediaRequest();
        request.setPostId(1L);
        request.setUploaderId(10L);
        request.setFileName("image.png");
        request.setFileType("image/png");
        request.setFileUrl("http://cdn/image.png");

        Media saved = Media.builder()
                .id(7L)
                .postId(1L)
                .uploaderId(10L)
                .fileName("image.png")
                .fileType("image/png")
                .fileUrl("http://cdn/image.png")
                .build();

        when(mediaRepository.save(any(Media.class))).thenReturn(saved);

        MediaResponse response = mediaService.createMedia(request);

        assertEquals(7L, response.getId());
        assertEquals("image.png", response.getFileName());
    }

    @Test
    void getMediaByPostIdShouldMapAllRows() {
        when(mediaRepository.findByPostIdAndDeletedFalse(1L)).thenReturn(List.of(
                Media.builder().id(1L).postId(1L).fileName("a.png").build(),
                Media.builder().id(2L).postId(1L).fileName("b.png").build()
        ));

        List<MediaResponse> responses = mediaService.getMediaByPostId(1L);

        assertEquals(2, responses.size());
        assertEquals("a.png", responses.get(0).getFileName());
    }

    @Test
    void getMediaByUploaderIdShouldMapAllRows() {
        when(mediaRepository.findByUploaderIdAndDeletedFalse(5L)).thenReturn(List.of(
                Media.builder().id(1L).uploaderId(5L).fileName("a.png").build()
        ));

        assertEquals(1, mediaService.getMediaByUploaderId(5L).size());
    }

    @Test
    void deleteMediaShouldSoftDelete() {
        Media media = Media.builder().id(8L).deleted(false).build();
        when(mediaRepository.findById(8L)).thenReturn(Optional.of(media));

        mediaService.deleteMedia(8L);

        assertTrue(media.isDeleted());
        verify(mediaRepository).save(media);
    }

    @Test
    void createStoryShouldReturnMappedResponse() {
        CreateStoryRequest request = new CreateStoryRequest();
        request.setAuthorId(1L);
        request.setMediaUrl("image-data");
        request.setCaption("caption");
        request.setMediaType("IMAGE");

        Story saved = Story.builder()
                .id(4L)
                .authorId(1L)
                .mediaUrl("image-data")
                .caption("caption")
                .mediaType("IMAGE")
                .viewsCount(0L)
                .active(true)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusHours(24))
                .build();

        when(storyRepository.save(any(Story.class))).thenReturn(saved);

        StoryResponse response = mediaService.createStory(request);

        assertEquals(4L, response.getId());
        assertEquals("IMAGE", response.getMediaType());
    }

    @Test
    void getStoriesByUserShouldMapActiveStories() {
        when(storyRepository.findByAuthorIdAndActiveTrueOrderByCreatedAtDesc(3L)).thenReturn(List.of(
                Story.builder().id(1L).authorId(3L).active(true).build()
        ));

        assertEquals(1, mediaService.getStoriesByUser(3L).size());
    }

    @Test
    void getActiveStoriesShouldReturnEmptyForNoAuthors() {
        assertEquals(List.of(), mediaService.getActiveStories(List.of()));
    }

    @Test
    void viewStoryShouldIncrementCounter() {
        Story story = Story.builder().id(6L).viewsCount(null).build();
        when(storyRepository.findById(6L)).thenReturn(Optional.of(story));
        when(storyRepository.save(story)).thenReturn(story);

        StoryResponse response = mediaService.viewStory(6L);

        assertEquals(1L, response.getViewsCount());
    }

    @Test
    void deleteStoryShouldDeactivateStory() {
        Story story = Story.builder().id(6L).active(true).build();
        when(storyRepository.findById(6L)).thenReturn(Optional.of(story));

        mediaService.deleteStory(6L);

        assertTrue(!story.isActive());
        verify(storyRepository).save(story);
    }

    @Test
    void expireOldStoriesShouldDeactivateExpiredStories() {
        Story expired = Story.builder().id(1L).active(true).expiresAt(LocalDateTime.now().minusMinutes(5)).build();
        when(storyRepository.findByActiveTrueAndExpiresAtBefore(any(LocalDateTime.class))).thenReturn(List.of(expired));

        mediaService.expireOldStories();

        assertTrue(!expired.isActive());
        verify(storyRepository).saveAll(List.of(expired));
    }
}
