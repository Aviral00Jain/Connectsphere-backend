package com.connectsphere.auth.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    private String fullName;

    private String bio;

    private String profilePicUrl;

    private String role; // USER / ADMIN

    private String provider;

    private boolean isActive = true;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
