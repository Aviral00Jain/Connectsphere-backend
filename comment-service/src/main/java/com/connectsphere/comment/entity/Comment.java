package com.connectsphere.comment.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long postId;
    private Long authorId;
    private Long parentCommentId;

    @Column(columnDefinition = "TEXT")
    private String content;

    private Integer likesCount;

    private boolean deleted;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
