package com.example.demo.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private static final String TEST_SECRET = "test-secret-key-for-testing-purposes-must-be-long-enough-for-256-bits-minimum";
    private static final Long TEST_EXPIRATION = 3600000L; // 1 hour

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtUtil, "expiration", TEST_EXPIRATION);
    }

    @Test
    void generateToken_ShouldCreateValidToken() {
        // Given
        String email = "test@example.com";
        Long userId = 1L;
        String role = "ROLE_USER";

        // When
        String token = jwtUtil.generateToken(email, userId, role);

        // Then
        assertThat(token).isNotNull();
        assertThat(token.split("\\.")).hasSize(3); // JWT has 3 parts
    }

    @Test
    void extractEmail_FromValidToken_ShouldReturnCorrectEmail() {
        // Given
        String email = "test@example.com";
        String token = jwtUtil.generateToken(email, 1L, "ROLE_USER");

        // When
        String extractedEmail = jwtUtil.extractEmail(token);

        // Then
        assertThat(extractedEmail).isEqualTo(email);
    }

    @Test
    void extractUserId_FromValidToken_ShouldReturnCorrectId() {
        // Given
        Long userId = 123L;
        String token = jwtUtil.generateToken("test@example.com", userId, "ROLE_USER");

        // When
        Long extractedUserId = jwtUtil.extractUserId(token);

        // Then
        assertThat(extractedUserId).isEqualTo(userId);
    }

    @Test
    void extractRole_FromValidToken_ShouldReturnCorrectRole() {
        // Given
        String role = "ROLE_ADMIN";
        String token = jwtUtil.generateToken("test@example.com", 1L, role);

        // When
        String extractedRole = jwtUtil.extractRole(token);

        // Then
        assertThat(extractedRole).isEqualTo(role);
    }

    @Test
    void extractExpiration_ShouldReturnFutureDate() {
        // Given
        String token = jwtUtil.generateToken("test@example.com", 1L, "ROLE_USER");

        // When
        Date expiration = jwtUtil.extractExpiration(token);

        // Then
        assertThat(expiration).isAfter(new Date());
    }

    @Test
    void validateToken_WithValidToken_ShouldReturnTrue() {
        // Given
        String email = "test@example.com";
        String token = jwtUtil.generateToken(email, 1L, "ROLE_USER");

        // When
        Boolean isValid = jwtUtil.validateToken(token);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    void validateToken_WithExpiredToken_ShouldReturnFalse() {
        // Given - Create token with very short expiration
        ReflectionTestUtils.setField(jwtUtil, "expiration", -1000L); // Already expired
        String token = jwtUtil.generateToken("test@example.com", 1L, "ROLE_USER");

        // Reset expiration for validation
        ReflectionTestUtils.setField(jwtUtil, "expiration", TEST_EXPIRATION);

        // When
        Boolean isValid = jwtUtil.validateToken(token);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    void validateToken_WithInvalidToken_ShouldReturnFalse() {
        // Given
        String invalidToken = "invalid.token.here";

        // When
        Boolean isValid = jwtUtil.validateToken(invalidToken);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    void validateToken_WithMalformedToken_ShouldReturnFalse() {
        // Given
        String malformedToken = "not-a-valid-jwt";

        // When
        Boolean isValid = jwtUtil.validateToken(malformedToken);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    void validateToken_WithNullToken_ShouldReturnFalse() {
        // When
        Boolean isValid = jwtUtil.validateToken(null);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    void extractEmail_FromExpiredToken_ShouldThrowException() {
        // Given - Create expired token
        ReflectionTestUtils.setField(jwtUtil, "expiration", -1000L);
        String token = jwtUtil.generateToken("test@example.com", 1L, "ROLE_USER");
        ReflectionTestUtils.setField(jwtUtil, "expiration", TEST_EXPIRATION);

        // When/Then
        assertThatThrownBy(() -> jwtUtil.extractEmail(token))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void extractEmail_FromMalformedToken_ShouldThrowException() {
        // Given
        String malformedToken = "not.a.valid.jwt.token";

        // When/Then
        assertThatThrownBy(() -> jwtUtil.extractEmail(malformedToken))
                .isInstanceOf(MalformedJwtException.class);
    }

    @Test
    void generateToken_WithDifferentUsers_ShouldGenerateDifferentTokens() {
        // Given
        String token1 = jwtUtil.generateToken("user1@example.com", 1L, "ROLE_USER");
        String token2 = jwtUtil.generateToken("user2@example.com", 2L, "ROLE_USER");

        // Then
        assertThat(token1).isNotEqualTo(token2);
        assertThat(jwtUtil.extractEmail(token1)).isEqualTo("user1@example.com");
        assertThat(jwtUtil.extractEmail(token2)).isEqualTo("user2@example.com");
    }

    @Test
    void generateToken_WithDifferentRoles_ShouldIncludeCorrectRole() {
        // Given
        String userToken = jwtUtil.generateToken("test@example.com", 1L, "ROLE_USER");
        String adminToken = jwtUtil.generateToken("admin@example.com", 2L, "ROLE_ADMIN");

        // Then
        assertThat(jwtUtil.extractRole(userToken)).isEqualTo("ROLE_USER");
        assertThat(jwtUtil.extractRole(adminToken)).isEqualTo("ROLE_ADMIN");
    }

    @Test
    void extractExpiration_ShouldBeApproximatelyOneHourFromNow() {
        // Given
        String token = jwtUtil.generateToken("test@example.com", 1L, "ROLE_USER");

        // When
        Date expiration = jwtUtil.extractExpiration(token);
        long timeDiff = expiration.getTime() - new Date().getTime();

        // Then - Should be approximately 1 hour (with 1 minute tolerance)
        assertThat(timeDiff).isBetween(3540000L, 3660000L); // 59-61 minutes
    }

    @Test
    void tokenGeneration_ShouldBeConsistent() {
        // Given - Same parameters
        String email = "test@example.com";
        Long userId = 1L;
        String role = "ROLE_USER";

        // When - Generate multiple times
        String token1 = jwtUtil.generateToken(email, userId, role);

        // Wait 1 second to ensure different timestamps
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        String token2 = jwtUtil.generateToken(email, userId, role);

        // Then - Tokens should be different (due to different issuedAt timestamps)
        assertThat(token1).isNotEqualTo(token2);

        // But should contain same user info
        assertThat(jwtUtil.extractEmail(token1)).isEqualTo(jwtUtil.extractEmail(token2));
        assertThat(jwtUtil.extractUserId(token1)).isEqualTo(jwtUtil.extractUserId(token2));
        assertThat(jwtUtil.extractRole(token1)).isEqualTo(jwtUtil.extractRole(token2));
    }
}