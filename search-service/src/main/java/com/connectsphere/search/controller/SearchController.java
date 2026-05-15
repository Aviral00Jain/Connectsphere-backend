package com.connectsphere.search.controller;

import com.connectsphere.search.dto.CreateSearchRecordRequest;
import com.connectsphere.search.dto.SearchRecordResponse;
import com.connectsphere.search.service.SearchService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
@Validated
public class SearchController {

    private final SearchService searchService;

    @PostMapping
    public SearchRecordResponse createRecord(@Valid @RequestBody CreateSearchRecordRequest request) {
        return searchService.createRecord(request);
    }

    @PostMapping("/index")
    public List<SearchRecordResponse> indexPost(@Valid @RequestBody CreateSearchRecordRequest request) {
        return searchService.indexPost(request);
    }

    @GetMapping
    public List<SearchRecordResponse> searchByKeyword(@RequestParam @NotBlank String keyword) {
        return searchService.searchByKeyword(keyword);
    }

    @GetMapping("/post/{postId}")
    public List<SearchRecordResponse> getHashtagsForPost(@PathVariable @Positive Long postId) {
        return searchService.getHashtagsForPost(postId);
    }

    @GetMapping("/hashtags/{hashtag}")
    public List<SearchRecordResponse> getPostsByHashtag(@PathVariable @NotBlank String hashtag) {
        return searchService.getPostsByHashtag(hashtag);
    }

    @GetMapping("/hashtags/trending")
    public List<String> getTrendingHashtags() {
        return searchService.getTrendingHashtags();
    }

    @DeleteMapping("/post/{postId}")
    public java.util.Map<String, String> removePostIndex(@PathVariable @Positive Long postId) {
        searchService.removePostIndex(postId);
        return java.util.Map.of("message", "Post index removed successfully");
    }

    @GetMapping("/hashtags")
    public List<String> searchHashtags(@RequestParam @NotBlank String keyword) {
        return searchService.searchHashtags(keyword);
    }

    @GetMapping("/hashtags/{hashtag}/count")
    public java.util.Map<String, Long> getHashtagCount(@PathVariable @NotBlank String hashtag) {
        return java.util.Map.of("postCount", searchService.getHashtagCount(hashtag));
    }
}
