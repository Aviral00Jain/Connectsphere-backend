package com.connectsphere.auth.repository;

import com.connectsphere.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    List<User> findByRoleIgnoreCase(String role);

    long countByIsActiveTrue();

    long countByRoleIgnoreCase(String role);

    @Query("""
            select u from User u
            where lower(u.username) like lower(concat('%', :keyword, '%'))
               or lower(u.fullName) like lower(concat('%', :keyword, '%'))
            """)
    List<User> searchByKeyword(String keyword);
}
