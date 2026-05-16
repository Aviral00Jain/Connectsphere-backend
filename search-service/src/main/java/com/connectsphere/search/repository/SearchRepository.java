package com.connectsphere.search.repository;

import com.connectsphere.search.entity.SearchRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SearchRepository extends JpaRepository<SearchRecord, Long> {
    List<SearchRecord> findByKeywordContainingIgnoreCase(String keyword);

    List<SearchRecord> findByPostId(Long postId);

    List<SearchRecord> findByKeywordIgnoreCase(String keyword);

    List<SearchRecord> findByKeywordContainingIgnoreCaseOrderByKeywordAsc(String keyword);

    void deleteByPostId(Long postId);

    @Query("""
            select sr.keyword from SearchRecord sr
            group by sr.keyword
            order by count(sr.id) desc
            """)
    List<String> findTrendingKeywords();

    long countByKeywordIgnoreCase(String keyword);
}
