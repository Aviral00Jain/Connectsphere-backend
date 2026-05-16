package com.connectsphere.post.repository;

import com.connectsphere.post.entity.Post;
import com.connectsphere.post.enums.Visibility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    List<Post> findByDeletedFalse();

    List<Post> findByAuthorIdAndDeletedFalse(Long authorId);

    List<Post> findByVisibilityAndDeletedFalse(Visibility visibility);

    @Query("""
            select p from Post p
            where p.deleted = false
              and lower(p.content) like lower(concat('%', :keyword, '%'))
            order by p.createdAt desc
            """)
    List<Post> searchByContent(String keyword);

    List<Post> findByAuthorIdInAndDeletedFalseOrderByCreatedAtDesc(List<Long> authorIds);

    List<Post> findByFlaggedTrueAndDeletedFalseOrderByCreatedAtDesc();

    long countByDeletedFalse();

    long countByFlaggedTrueAndDeletedFalse();
}
