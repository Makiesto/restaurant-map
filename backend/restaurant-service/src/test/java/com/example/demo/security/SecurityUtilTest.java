package com.example.demo.security;

import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecurityUtilTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SecurityUtil securityUtil;

    private User testUser;
    private SecurityContext securityContext;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .password("encodedPassword")
                .role(Role.USER)
                .isActive(true)
                .build();

        securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentUser_WithAuthenticatedUser_ShouldReturnUser() {
        // Given
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "john@example.com",
                "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(testUser));

        // When
        User result = securityUtil.getCurrentUser();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void getCurrentUser_WithNoAuthentication_ShouldThrowException() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(null);

        // When/Then
        assertThatThrownBy(() -> securityUtil.getCurrentUser())
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("No authenticated user found");
    }

    @Test
    void getCurrentUser_WithUnauthenticatedUser_ShouldThrowException() {
        // Given
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(false);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        // When/Then
        assertThatThrownBy(() -> securityUtil.getCurrentUser())
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("No authenticated user found");
    }

    @Test
    void getCurrentUser_WithNonExistentUserEmail_ShouldThrowException() {
        // Given
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "nonexistent@example.com",
                "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(userRepository.findByEmail("nonexistent@example.com"))
                .thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> securityUtil.getCurrentUser())
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with email: nonexistent@example.com");
    }

    @Test
    void getCurrentUserId_WithAuthenticatedUser_ShouldReturnUserId() {
        // Given
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "john@example.com",
                "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(testUser));

        // When
        Long userId = securityUtil.getCurrentUserId();

        // Then
        assertThat(userId).isEqualTo(1L);
    }

    @Test
    void getCurrentUserId_WithNoAuthentication_ShouldThrowException() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(null);

        // When/Then
        assertThatThrownBy(() -> securityUtil.getCurrentUserId())
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("No authenticated user found");
    }

    @Test
    void getCurrentUserEmail_WithAuthenticatedUser_ShouldReturnEmail() {
        // Given
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "john@example.com",
                "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        when(securityContext.getAuthentication()).thenReturn(authentication);

        // When
        String email = securityUtil.getCurrentUserEmail();

        // Then
        assertThat(email).isEqualTo("john@example.com");
    }

    @Test
    void getCurrentUserEmail_WithNoAuthentication_ShouldThrowException() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(null);

        // When/Then
        assertThatThrownBy(() -> securityUtil.getCurrentUserEmail())
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("No authenticated user found");
    }

    @Test
    void getCurrentUserEmail_WithUnauthenticatedUser_ShouldThrowException() {
        // Given
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(false);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        // When/Then
        assertThatThrownBy(() -> securityUtil.getCurrentUserEmail())
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("No authenticated user found");
    }

    @Test
    void isAuthenticated_WithAuthenticatedUser_ShouldReturnTrue() {
        // Given
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "john@example.com",
                "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        when(securityContext.getAuthentication()).thenReturn(authentication);

        // When
        boolean isAuthenticated = securityUtil.isAuthenticated();

        // Then
        assertThat(isAuthenticated).isTrue();
    }

    @Test
    void isAuthenticated_WithNoAuthentication_ShouldReturnFalse() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(null);

        // When
        boolean isAuthenticated = securityUtil.isAuthenticated();

        // Then
        assertThat(isAuthenticated).isFalse();
    }

    @Test
    void isAuthenticated_WithUnauthenticatedUser_ShouldReturnFalse() {
        // Given
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(false);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        // When
        boolean isAuthenticated = securityUtil.isAuthenticated();

        // Then
        assertThat(isAuthenticated).isFalse();
    }

    @Test
    void getCurrentUser_WithDifferentUsers_ShouldReturnCorrectUser() {
        // Given
        User user1 = User.builder()
                .id(1L)
                .email("user1@example.com")
                .build();

        User user2 = User.builder()
                .id(2L)
                .email("user2@example.com")
                .build();

        // First call
        Authentication auth1 = new UsernamePasswordAuthenticationToken("user1@example.com", "password", Collections.emptyList());
        when(securityContext.getAuthentication()).thenReturn(auth1);
        when(userRepository.findByEmail("user1@example.com")).thenReturn(Optional.of(user1));

        User result1 = securityUtil.getCurrentUser();
        assertThat(result1.getId()).isEqualTo(1L);

        // Second call with different user
        Authentication auth2 = new UsernamePasswordAuthenticationToken("user2@example.com", "password", Collections.emptyList());
        when(securityContext.getAuthentication()).thenReturn(auth2);
        when(userRepository.findByEmail("user2@example.com")).thenReturn(Optional.of(user2));

        User result2 = securityUtil.getCurrentUser();
        assertThat(result2.getId()).isEqualTo(2L);
    }

    @Test
    void getCurrentUser_WithAdminUser_ShouldReturnAdminUser() {
        // Given
        testUser.setRole(Role.ADMIN);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "john@example.com",
                "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(testUser));

        // When
        User result = securityUtil.getCurrentUser();

        // Then
        assertThat(result.getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    void getCurrentUser_WithVerifiedUser_ShouldReturnVerifiedUser() {
        // Given
        testUser.setRole(Role.VERIFIED_USER);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "john@example.com",
                "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_VERIFIED_USER"))
        );

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(testUser));

        // When
        User result = securityUtil.getCurrentUser();

        // Then
        assertThat(result.getRole()).isEqualTo(Role.VERIFIED_USER);
    }
}