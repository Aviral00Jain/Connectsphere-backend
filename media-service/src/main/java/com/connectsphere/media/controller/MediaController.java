package com.connectsphere.media.controller;

import com.connectsphere.media.dto.CreateMediaRequest;
import com.connectsphere.media.dto.CreateStoryRequest;
import com.connectsphere.media.dto.MediaResponse;
import com.connectsphere.media.dto.StoryResponse;
import com.connectsphere.media.service.MediaService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/media")
@RequiredArgsConstructor
@Validated
public class MediaController {

    private final MediaService mediaService;

    @PostMapping
    public MediaResponse createMedia(@Valid @RequestBody CreateMediaRequest request) {
        return mediaService.createMedia(request);
    }

    @GetMapping("/post/{postId}")
    public List<MediaResponse> getMediaByPostId(@PathVariable @Positive Long postId) {
        return mediaService.getMediaByPostId(postId);
    }

    @GetMapping("/uploader/{uploaderId}")
    public List<MediaResponse> getMediaByUploaderId(@PathVariable @Positive Long uploaderId) {
        return mediaService.getMediaByUploaderId(uploaderId);
    }

    @DeleteMapping("/{mediaId}")
    public java.util.Map<String, String> deleteMedia(@PathVariable @Positive Long mediaId) {
        mediaService.deleteMedia(mediaId);
        return java.util.Map.of("message", "Media deleted successfully");
    }

    @PostMapping("/stories")
    public StoryResponse createStory(@Valid @RequestBody CreateStoryRequest request) {
        return mediaService.createStory(request);
    }

    @GetMapping("/stories/user/{authorId}")
    public List<StoryResponse> getStoriesByUser(@PathVariable @Positive Long authorId) {
        return mediaService.getStoriesByUser(authorId);
    }

    @GetMapping("/stories/feed")
    public List<StoryResponse> getActiveStories(@RequestParam List<Long> authorIds) {
        return mediaService.getActiveStories(authorIds);
    }

    @PutMapping("/stories/{storyId}/view")
    public StoryResponse viewStory(@PathVariable @Positive Long storyId) {
        return mediaService.viewStory(storyId);
    }

    @DeleteMapping("/stories/{storyId}")
    public java.util.Map<String, String> deleteStory(@PathVariable @Positive Long storyId) {
        mediaService.deleteStory(storyId);
        return java.util.Map.of("message", "Story deleted successfully");
    }
}
