package com.connectsphere.media.service;

import com.connectsphere.media.dto.CreateMediaRequest;
import com.connectsphere.media.dto.CreateStoryRequest;
import com.connectsphere.media.dto.MediaResponse;
import com.connectsphere.media.dto.StoryResponse;

import java.util.List;

public interface MediaService {
    MediaResponse createMedia(CreateMediaRequest request);
    List<MediaResponse> getMediaByPostId(Long postId);
    List<MediaResponse> getMediaByUploaderId(Long uploaderId);
    void deleteMedia(Long mediaId);
    StoryResponse createStory(CreateStoryRequest request);
    List<StoryResponse> getStoriesByUser(Long authorId);
    List<StoryResponse> getActiveStories(List<Long> authorIds);
    StoryResponse viewStory(Long storyId);
    void deleteStory(Long storyId);
    void expireOldStories();
}
