package com.connectsphere.comment.repository;

import com.connectsphere.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPostIdAndDeletedFalse(Long postId);
    List<Comment> findByPostIdAndParentCommentIdIsNullAndDeletedFalse(Long postId);
    List<Comment> findByParentCommentIdAndDeletedFalse(Long parentCommentId);
    List<Comment> findByAuthorIdAndDeletedFalse(Long authorId);
}
