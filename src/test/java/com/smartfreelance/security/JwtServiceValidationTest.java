package com.smartfreelance.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceValidationTest {

    @Test
    void jwtService_ValidSecretKey_InitializesSuccessfully() {
        String validSecret = "ThisIsAValid32CharacterMinSecretKey1234567890!";
        JwtService jwtService = new JwtService(validSecret, 86400000L);
        assertNotNull(jwtService);

        String token = jwtService.generateToken(1L, "user@example.com", "CLIENT");
        assertNotNull(token);
        assertTrue(jwtService.validateToken(token));
    }

    @Test
    void jwtService_MissingSecretKey_ThrowsIllegalStateException() {
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            new JwtService("", 86400000L);
        });
        assertTrue(ex.getMessage().contains("JWT Secret key is missing"));
    }

    @Test
    void jwtService_NullSecretKey_ThrowsIllegalStateException() {
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            new JwtService(null, 86400000L);
        });
        assertTrue(ex.getMessage().contains("JWT Secret key is missing"));
    }

    @Test
    void jwtService_WhitespaceSecretKey_ThrowsIllegalStateException() {
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            new JwtService("   ", 86400000L);
        });
        assertTrue(ex.getMessage().contains("JWT Secret key is missing"));
    }

    @Test
    void jwtService_ShortSecretKey_ThrowsIllegalStateException() {
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            new JwtService("short_key_under_32_chars", 86400000L);
        });
        assertTrue(ex.getMessage().contains("at least 32 characters"));
    }
}
