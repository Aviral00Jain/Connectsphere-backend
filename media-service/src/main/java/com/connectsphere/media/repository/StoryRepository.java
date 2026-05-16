package com.connectsphere.media.repository;

import com.connectsphere.media.entity.Story;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface StoryRepository extends JpaRepository<Story, Long> {
    List<Story> findByAuthorIdAndActiveTrueOrderByCreatedAtDesc(Long authorId);

    List<Story> findByAuthorIdInAndActiveTrueOrderByCreatedAtDesc(List<Long> authorIds);

    List<Story> findByActiveTrueAndExpiresAtBefore(LocalDateTime timestamp);
}
