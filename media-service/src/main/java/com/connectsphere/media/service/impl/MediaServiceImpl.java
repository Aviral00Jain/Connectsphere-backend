package com.connectsphere.media.service.impl;

import com.connectsphere.media.dto.CreateMediaRequest;
import com.connectsphere.media.dto.CreateStoryRequest;
import com.connectsphere.media.dto.MediaResponse;
import com.connectsphere.media.dto.StoryResponse;
import com.connectsphere.media.entity.Media;
import com.connectsphere.media.entity.Story;
import com.connectsphere.media.exception.ResourceNotFoundException;
import com.connectsphere.media.repository.MediaRepository;
import com.connectsphere.media.repository.StoryRepository;
import com.connectsphere.media.service.MediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MediaServiceImpl implements MediaService {

    private final MediaRepository mediaRepository;
    private final StoryRepository storyRepository;

    @Override
    public MediaResponse createMedia(CreateMediaRequest request) {
        Media media = Media.builder()
                .postId(request.getPostId())
                .uploaderId(request.getUploaderId())
                .fileName(request.getFileName())
                .fileType(request.getFileType())
                .fileUrl(request.getFileUrl())
                .deleted(false)
                .createdAt(LocalDateTime.now())
                .build();

        Media saved = mediaRepository.save(media);
        return mapToResponse(saved);
    }

    @Override
    public List<MediaResponse> getMediaByPostId(Long postId) {
        return mediaRepository.findByPostIdAndDeletedFalse(postId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<MediaResponse> getMediaByUploaderId(Long uploaderId) {
        return mediaRepository.findByUploaderIdAndDeletedFalse(uploaderId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public void deleteMedia(Long mediaId) {
        Media media = mediaRepository.findById(mediaId)
                .orElseThrow(() -> new ResourceNotFoundException("Media not found with id: " + mediaId));

        media.setDeleted(true);
        mediaRepository.save(media);
    }

    @Override
    public StoryResponse createStory(CreateStoryRequest request) {
        Story story = Story.builder()
                .authorId(request.getAuthorId())
                .mediaUrl(request.getMediaUrl())
                .caption(request.getCaption())
                .mediaType(request.getMediaType())
                .viewsCount(0L)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusHours(24))
                .active(true)
                .build();

        return mapToStoryResponse(storyRepository.save(story));
    }

    @Override
    public List<StoryResponse> getStoriesByUser(Long authorId) {
        return storyRepository.findByAuthorIdAndActiveTrueOrderByCreatedAtDesc(authorId)
                .stream()
                .map(this::mapToStoryResponse)
                .toList();
    }

    @Override
    public List<StoryResponse> getActiveStories(List<Long> authorIds) {
        if (authorIds == null || authorIds.isEmpty()) {
            return List.of();
        }

        return storyRepository.findByAuthorIdInAndActiveTrueOrderByCreatedAtDesc(authorIds)
                .stream()
                .map(this::mapToStoryResponse)
                .toList();
    }

    @Override
    public StoryResponse viewStory(Long storyId) {
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new ResourceNotFoundException("Story not found with id: " + storyId));

        story.setViewsCount((story.getViewsCount() == null ? 0L : story.getViewsCount()) + 1);
        return mapToStoryResponse(storyRepository.save(story));
    }

    @Override
    public void deleteStory(Long storyId) {
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new ResourceNotFoundException("Story not found with id: " + storyId));

        story.setActive(false);
        storyRepository.save(story);
    }

    @Override
    @Scheduled(fixedDelay = 300000)
    public void expireOldStories() {
        List<Story> expiredStories = storyRepository.findByActiveTrueAndExpiresAtBefore(LocalDateTime.now());
        expiredStories.forEach(story -> story.setActive(false));
        storyRepository.saveAll(expiredStories);
    }

    private MediaResponse mapToResponse(Media media) {
        return MediaResponse.builder()
                .id(media.getId())
                .postId(media.getPostId())
                .uploaderId(media.getUploaderId())
                .fileName(media.getFileName())
                .fileType(media.getFileType())
                .fileUrl(media.getFileUrl())
                .deleted(media.isDeleted())
                .createdAt(media.getCreatedAt())
                .build();
    }

    private StoryResponse mapToStoryResponse(Story story) {
        return StoryResponse.builder()
                .id(story.getId())
                .authorId(story.getAuthorId())
                .mediaUrl(story.getMediaUrl())
                .caption(story.getCaption())
                .mediaType(story.getMediaType())
                .viewsCount(story.getViewsCount())
                .expiresAt(story.getExpiresAt())
                .createdAt(story.getCreatedAt())
                .active(story.isActive())
                .build();
    }
}
