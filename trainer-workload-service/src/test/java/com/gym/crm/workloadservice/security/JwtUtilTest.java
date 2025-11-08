package com.gym.crm.workloadservice.security;

import com.gym.crm.workload.security.JwtUtil;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

public class JwtUtilTest {

    private JwtUtil jwtUtil;
    private String validSecret;

    @BeforeEach
    void setUp() {
        validSecret = Base64.getEncoder().encodeToString(
                "test-secret-key-that-is-at-least-256-bits-long-for-testing".getBytes()
        );
        jwtUtil = new JwtUtil(validSecret, 3600000L);
    }

    @Test
    void testGenerateServiceToken() {
        String token = jwtUtil.generateServiceToken();

        assertNotNull(token);
        assertFalse(token.isEmpty());

        Claims claims = jwtUtil.extractAllClaims(token);
        assertEquals(JwtUtil.SERVICE_USERNAME, claims.getSubject());
        assertEquals("SERVICE", claims.get("role"));
    }

    @Test
    void testValidateToken_ValidToken() {
        String token = jwtUtil.generateServiceToken();
        assertTrue(jwtUtil.validateToken(token));
    }

    @Test
    void testValidateToken_InvalidToken() {
        assertFalse(jwtUtil.validateToken("invalid.token.here"));
    }

    @Test
    void testIsServiceToken() {
        String serviceToken = jwtUtil.generateServiceToken();
        assertTrue(jwtUtil.isServiceToken(serviceToken));
    }

    @Test
    void testExtractUsername() {
        String token = jwtUtil.generateServiceToken();
        String username = jwtUtil.extractUsername(token);
        assertEquals(JwtUtil.SERVICE_USERNAME, username);
    }

    @Test
    void testIsTokenExpired() {
        String token = jwtUtil.generateServiceToken();
        assertFalse(jwtUtil.isTokenExpired(token));
    }
}