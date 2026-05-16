package com.connectsphere.search.service.impl;

import com.connectsphere.search.dto.CreateSearchRecordRequest;
import com.connectsphere.search.dto.SearchRecordResponse;
import com.connectsphere.search.entity.SearchRecord;
import com.connectsphere.search.repository.SearchRepository;
import com.connectsphere.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private static final Pattern HASHTAG_PATTERN = Pattern.compile("#(\\w+)");

    private final SearchRepository searchRepository;

    @Override
    public SearchRecordResponse createRecord(CreateSearchRecordRequest request) {
        SearchRecord record = SearchRecord.builder()
                .postId(request.getPostId())
                .keyword(normalizeKeyword(request.getKeyword()))
                .createdAt(LocalDateTime.now())
                .build();

        SearchRecord saved = searchRepository.save(record);
        return mapToResponse(saved);
    }

    @Override
    public List<SearchRecordResponse> searchByKeyword(String keyword) {
        return searchRepository.findByKeywordContainingIgnoreCase(normalizeKeyword(keyword))
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<SearchRecordResponse> indexPost(CreateSearchRecordRequest request) {
        if (request.getContent() == null || request.getContent().isBlank()) {
            return List.of();
        }

        LinkedHashSet<String> hashtags = extractHashtags(request.getContent());
        return hashtags.stream()
                .map(hashtag -> {
                    CreateSearchRecordRequest createRequest = new CreateSearchRecordRequest();
                    createRequest.setPostId(request.getPostId());
                    createRequest.setKeyword(hashtag);
                    return createRecord(createRequest);
                })
                .toList();
    }

    @Override
    public void removePostIndex(Long postId) {
        searchRepository.deleteByPostId(postId);
    }

    @Override
    public List<SearchRecordResponse> getHashtagsForPost(Long postId) {
        return searchRepository.findByPostId(postId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<SearchRecordResponse> getPostsByHashtag(String hashtag) {
        return searchRepository.findByKeywordIgnoreCase(normalizeKeyword(hashtag))
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<String> getTrendingHashtags() {
        return searchRepository.findTrendingKeywords()
                .stream()
                .limit(10)
                .toList();
    }

    @Override
    public List<String> searchHashtags(String keyword) {
        return searchRepository.findByKeywordContainingIgnoreCaseOrderByKeywordAsc(normalizeKeyword(keyword))
                .stream()
                .map(SearchRecord::getKeyword)
                .distinct()
                .toList();
    }

    @Override
    public long getHashtagCount(String hashtag) {
        return searchRepository.countByKeywordIgnoreCase(normalizeKeyword(hashtag));
    }

    private SearchRecordResponse mapToResponse(SearchRecord record) {
        return SearchRecordResponse.builder()
                .id(record.getId())
                .postId(record.getPostId())
                .keyword(record.getKeyword())
                .createdAt(record.getCreatedAt())
                .build();
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null) {
            return null;
        }

        return keyword.startsWith("#") ? keyword.substring(1).toLowerCase() : keyword.toLowerCase();
    }

    private LinkedHashSet<String> extractHashtags(String content) {
        LinkedHashSet<String> hashtags = new LinkedHashSet<>();
        Matcher matcher = HASHTAG_PATTERN.matcher(content);

        while (matcher.find()) {
            hashtags.add(matcher.group(1).toLowerCase());
        }

        return hashtags;
    }
}
