package com.connectsphere.media.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "media")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Media {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long postId;
    private Long uploaderId;
    private String fileName;
    private String fileType;
    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String fileUrl;
    private boolean deleted;

    private LocalDateTime createdAt;
}
