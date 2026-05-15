package com.connectsphere.auth.security;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    @Test
    void generatedTokenShouldExposeSubjectAndValidateEmail() {
        JwtService jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", "this-is-a-very-long-test-secret-key");
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 60_000L);

        String token = jwtService.generateToken("user@example.com");

        assertEquals("user@example.com", jwtService.extractUsername(token));
        assertTrue(jwtService.isTokenValid(token, "user@example.com"));
        assertFalse(jwtService.isTokenValid(token, "other@example.com"));
    }
}
