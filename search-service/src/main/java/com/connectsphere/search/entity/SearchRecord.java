package com.connectsphere.search.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "search_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long postId;
    private String keyword;

    private LocalDateTime createdAt;
}