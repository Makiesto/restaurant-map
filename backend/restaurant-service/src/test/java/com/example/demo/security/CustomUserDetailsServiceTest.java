package com.example.demo.security;

import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .password("encodedPassword123")
                .role(Role.USER)
                .isActive(true)
                .emailVerified(true)
                .build();
    }

    @Test
    void loadUserByUsername_WithExistingUser_ShouldReturnUserDetails() {
        // Given
        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(testUser));

        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("john@example.com");

        // Then
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("john@example.com");
        assertThat(userDetails.getPassword()).isEqualTo("encodedPassword123");
        assertThat(userDetails.isEnabled()).isTrue();
        assertThat(userDetails.isAccountNonExpired()).isTrue();
        assertThat(userDetails.isAccountNonLocked()).isTrue();
        assertThat(userDetails.isCredentialsNonExpired()).isTrue();

        verify(userRepository).findByEmail("john@example.com");
    }

    @Test
    void loadUserByUsername_WithNonExistentUser_ShouldThrowException() {
        // Given
        when(userRepository.findByEmail("nonexistent@example.com"))
                .thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() ->
                customUserDetailsService.loadUserByUsername("nonexistent@example.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found with email: nonexistent@example.com");
    }

    @Test
    void loadUserByUsername_ShouldSetCorrectAuthorities() {
        // Given
        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(testUser));

        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("john@example.com");

        // Then
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        assertThat(authorities).hasSize(1);
        assertThat(authorities)
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_USER");
    }

    @Test
    void loadUserByUsername_WithVerifiedUser_ShouldReturnVerifiedUserRole() {
        // Given
        testUser.setRole(Role.VERIFIED_USER);
        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(testUser));

        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("john@example.com");

        // Then
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        assertThat(authorities)
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_VERIFIED_USER");
    }

    @Test
    void loadUserByUsername_WithAdminUser_ShouldReturnAdminRole() {
        // Given
        testUser.setRole(Role.ADMIN);
        when(userRepository.findByEmail("admin@example.com"))
                .thenReturn(Optional.of(testUser));

        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("admin@example.com");

        // Then
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        assertThat(authorities)
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_ADMIN");
    }

    @Test
    void loadUserByUsername_WithInactiveUser_ShouldReturnDisabledUserDetails() {
        // Given
        testUser.setIsActive(false);
        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(testUser));

        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("john@example.com");

        // Then
        assertThat(userDetails.isEnabled()).isFalse();
    }

    @Test
    void loadUserByUsername_WithActiveUser_ShouldReturnEnabledUserDetails() {
        // Given
        testUser.setIsActive(true);
        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(testUser));

        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("john@example.com");

        // Then
        assertThat(userDetails.isEnabled()).isTrue();
    }

    @Test
    void loadUserByUsername_ShouldPreservePassword() {
        // Given
        String encodedPassword = "$2a$10$someEncodedPassword";
        testUser.setPassword(encodedPassword);
        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(testUser));

        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("john@example.com");

        // Then
        assertThat(userDetails.getPassword()).isEqualTo(encodedPassword);
    }

    @Test
    void loadUserByUsername_WithDifferentEmails_ShouldReturnDifferentUserDetails() {
        // Given
        User user1 = User.builder()
                .email("user1@example.com")
                .password("password1")
                .role(Role.USER)
                .isActive(true)
                .build();

        User user2 = User.builder()
                .email("user2@example.com")
                .password("password2")
                .role(Role.ADMIN)
                .isActive(true)
                .build();

        when(userRepository.findByEmail("user1@example.com"))
                .thenReturn(Optional.of(user1));
        when(userRepository.findByEmail("user2@example.com"))
                .thenReturn(Optional.of(user2));

        // When
        UserDetails userDetails1 = customUserDetailsService.loadUserByUsername("user1@example.com");
        UserDetails userDetails2 = customUserDetailsService.loadUserByUsername("user2@example.com");

        // Then
        assertThat(userDetails1.getUsername()).isEqualTo("user1@example.com");
        assertThat(userDetails2.getUsername()).isEqualTo("user2@example.com");
        assertThat(userDetails1.getPassword()).isEqualTo("password1");
        assertThat(userDetails2.getPassword()).isEqualTo("password2");
    }

    @Test
    void loadUserByUsername_ShouldHandleCaseInsensitiveEmail() {
        // Given
        when(userRepository.findByEmail("John@Example.Com"))
                .thenReturn(Optional.of(testUser));

        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("John@Example.Com");

        // Then
        assertThat(userDetails).isNotNull();
        verify(userRepository).findByEmail("John@Example.Com");
    }

    @Test
    void loadUserByUsername_WithNullEmail_ShouldThrowException() {
        // Given
        when(userRepository.findByEmail(null))
                .thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() ->
                customUserDetailsService.loadUserByUsername(null))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    void loadUserByUsername_WithEmptyEmail_ShouldThrowException() {
        // Given
        when(userRepository.findByEmail(""))
                .thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() ->
                customUserDetailsService.loadUserByUsername(""))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}