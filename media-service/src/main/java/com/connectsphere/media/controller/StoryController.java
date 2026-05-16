package com.connectsphere.media.controller;

import com.connectsphere.media.dto.CreateStoryRequest;
import com.connectsphere.media.dto.StoryResponse;
import com.connectsphere.media.service.MediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/stories")
@RequiredArgsConstructor
public class StoryController {

    private final MediaService mediaService;

    @PostMapping
    public StoryResponse createStory(@RequestBody CreateStoryRequest request) {
        return mediaService.createStory(request);
    }

    @GetMapping("/user/{authorId}")
    public List<StoryResponse> getStoriesByUser(@PathVariable Long authorId) {
        return mediaService.getStoriesByUser(authorId);
    }

    @GetMapping("/feed")
    public List<StoryResponse> getActiveStories(@RequestParam List<Long> authorIds) {
        return mediaService.getActiveStories(authorIds);
    }

    @PutMapping("/{storyId}/view")
    public StoryResponse viewStory(@PathVariable Long storyId) {
        return mediaService.viewStory(storyId);
    }

    @DeleteMapping("/{storyId}")
    public Map<String, String> deleteStory(@PathVariable Long storyId) {
        mediaService.deleteStory(storyId);
        return Map.of("message", "Story deleted successfully");
    }
}
