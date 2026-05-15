package com.connectsphere.search.service.impl;

import com.connectsphere.search.dto.CreateSearchRecordRequest;
import com.connectsphere.search.dto.SearchRecordResponse;
import com.connectsphere.search.entity.SearchRecord;
import com.connectsphere.search.repository.SearchRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchServiceImplTest {

    @Mock
    private SearchRepository searchRepository;

    @InjectMocks
    private SearchServiceImpl searchService;

    @Test
    void createRecordShouldPersistAndMapResponse() {
        CreateSearchRecordRequest request = new CreateSearchRecordRequest();
        request.setPostId(2L);
        request.setKeyword("spring");

        SearchRecord saved = SearchRecord.builder()
                .id(1L)
                .postId(2L)
                .keyword("spring")
                .build();

        when(searchRepository.save(any(SearchRecord.class))).thenReturn(saved);

        SearchRecordResponse response = searchService.createRecord(request);

        assertEquals(1L, response.getId());
        assertEquals("spring", response.getKeyword());
    }

    @Test
    void searchByKeywordShouldNormalizeAndMapMatchingRecords() {
        when(searchRepository.findByKeywordContainingIgnoreCase("java")).thenReturn(List.of(
                SearchRecord.builder().id(3L).postId(8L).keyword("java").build()
        ));

        List<SearchRecordResponse> responses = searchService.searchByKeyword("#Java");

        assertEquals(1, responses.size());
        assertEquals(8L, responses.get(0).getPostId());
    }

    @Test
    void indexPostShouldCreateUniqueHashtagsOnly() {
        CreateSearchRecordRequest request = new CreateSearchRecordRequest();
        request.setPostId(9L);
        request.setContent("Hello #Java #Spring #Java");

        when(searchRepository.save(any(SearchRecord.class)))
                .thenAnswer(invocation -> {
                    SearchRecord record = invocation.getArgument(0);
                    return SearchRecord.builder()
                            .id(1L)
                            .postId(record.getPostId())
                            .keyword(record.getKeyword())
                            .createdAt(record.getCreatedAt())
                            .build();
                });

        List<SearchRecordResponse> responses = searchService.indexPost(request);

        assertEquals(List.of("java", "spring"), responses.stream().map(SearchRecordResponse::getKeyword).toList());
    }

    @Test
    void removePostIndexShouldDelegateToRepository() {
        searchService.removePostIndex(7L);

        verify(searchRepository).deleteByPostId(7L);
    }

    @Test
    void getHashtagsForPostShouldMapRepositoryResults() {
        when(searchRepository.findByPostId(3L)).thenReturn(List.of(
                SearchRecord.builder().id(1L).postId(3L).keyword("java").build()
        ));

        assertEquals(1, searchService.getHashtagsForPost(3L).size());
    }

    @Test
    void getPostsByHashtagShouldNormalizeInput() {
        when(searchRepository.findByKeywordIgnoreCase("spring")).thenReturn(List.of(
                SearchRecord.builder().id(2L).postId(5L).keyword("spring").build()
        ));

        assertEquals(1, searchService.getPostsByHashtag("#Spring").size());
    }

    @Test
    void getTrendingHashtagsShouldLimitToTen() {
        when(searchRepository.findTrendingKeywords()).thenReturn(List.of(
                "a","b","c","d","e","f","g","h","i","j","k"
        ));

        assertEquals(10, searchService.getTrendingHashtags().size());
    }

    @Test
    void searchHashtagsShouldReturnDistinctKeywords() {
        when(searchRepository.findByKeywordContainingIgnoreCaseOrderByKeywordAsc("ja")).thenReturn(List.of(
                SearchRecord.builder().keyword("java").build(),
                SearchRecord.builder().keyword("java").build(),
                SearchRecord.builder().keyword("javascript").build()
        ));

        assertEquals(List.of("java", "javascript"), searchService.searchHashtags("ja"));
    }

    @Test
    void getHashtagCountShouldNormalizeInput() {
        when(searchRepository.countByKeywordIgnoreCase("java")).thenReturn(4L);

        assertEquals(4L, searchService.getHashtagCount("#Java"));
    }
}
