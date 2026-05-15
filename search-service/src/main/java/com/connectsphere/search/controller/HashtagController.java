package com.connectsphere.search.controller;

import com.connectsphere.search.dto.SearchRecordResponse;
import com.connectsphere.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/hashtags")
@RequiredArgsConstructor
public class HashtagController {

    private final SearchService searchService;

    @GetMapping("/post/{postId}")
    public List<SearchRecordResponse> getHashtagsForPost(@PathVariable Long postId) {
        return searchService.getHashtagsForPost(postId);
    }

    @GetMapping("/{hashtag}/posts")
    public List<SearchRecordResponse> getPostsByHashtag(@PathVariable String hashtag) {
        return searchService.getPostsByHashtag(hashtag);
    }

    @GetMapping("/trending")
    public List<String> getTrendingHashtags() {
        return searchService.getTrendingHashtags();
    }
}
