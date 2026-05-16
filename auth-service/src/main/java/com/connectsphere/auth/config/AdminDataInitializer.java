package com.connectsphere.auth.config;

import com.connectsphere.auth.entity.User;
import com.connectsphere.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Creates a default ADMIN account on startup if none exists.
 * Credentials: admin@connectsphere.com / admin123
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminDataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        long adminCount = userRepository.countByRoleIgnoreCase("ADMIN");

        if (adminCount == 0) {
            User admin = User.builder()
                    .username("admin")
                    .email("admin@connectsphere.com")
                    .password(passwordEncoder.encode("admin123"))
                    .fullName("Platform Admin")
                    .bio("ConnectSphere Platform Administrator")
                    .role("ADMIN")
                    .provider("LOCAL")
                    .isActive(true)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            userRepository.save(admin);
            log.info("✅ Default admin account created: admin@connectsphere.com / admin123");
        } else {
            log.info("Admin account already exists, skipping initialization.");
        }
    }
}
