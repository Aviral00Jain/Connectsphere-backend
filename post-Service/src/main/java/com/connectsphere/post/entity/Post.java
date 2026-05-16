package com.connectsphere.post.entity;

import com.connectsphere.post.enums.ModerationStatus;
import com.connectsphere.post.enums.Visibility;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "posts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long authorId;

    @Column(columnDefinition = "TEXT")
    private String content;

    @ElementCollection
    @CollectionTable(name = "post_media_urls", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "media_url", columnDefinition = "LONGTEXT")
    private List<String> mediaUrls;

    private String postType;

    @Enumerated(EnumType.STRING)
    private Visibility visibility;

    private Integer likesCount = 0;
    private Integer commentsCount = 0;
    private Integer sharesCount = 0;
    private boolean flagged;

    @Enumerated(EnumType.STRING)
    private ModerationStatus moderationStatus;

    private boolean deleted;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
