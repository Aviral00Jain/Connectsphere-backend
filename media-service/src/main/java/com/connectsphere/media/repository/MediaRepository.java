package com.connectsphere.media.repository;

import com.connectsphere.media.entity.Media;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MediaRepository extends JpaRepository<Media, Long> {
    List<Media> findByPostIdAndDeletedFalse(Long postId);

    List<Media> findByUploaderIdAndDeletedFalse(Long uploaderId);
}
