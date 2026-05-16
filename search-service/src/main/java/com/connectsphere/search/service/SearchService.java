package com.connectsphere.search.service;

import com.connectsphere.search.dto.CreateSearchRecordRequest;
import com.connectsphere.search.dto.SearchRecordResponse;

import java.util.List;

public interface SearchService {
    SearchRecordResponse createRecord(CreateSearchRecordRequest request);
    List<SearchRecordResponse> searchByKeyword(String keyword);
    List<SearchRecordResponse> indexPost(CreateSearchRecordRequest request);
    void removePostIndex(Long postId);
    List<SearchRecordResponse> getHashtagsForPost(Long postId);
    List<SearchRecordResponse> getPostsByHashtag(String hashtag);
    List<String> getTrendingHashtags();
    List<String> searchHashtags(String keyword);
    long getHashtagCount(String hashtag);
}
