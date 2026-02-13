package com.example.demo.service;

import com.example.demo.dto.user.ChangePasswordRequestDTO;
import com.example.demo.dto.user.UpdateProfileRequestDTO;
import com.example.demo.dto.user.UserRegistrationRequestDTO;
import com.example.demo.dto.user.UserResponseDTO;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.exception.ValidationException;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Additional edge case and comprehensive tests for UserService
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailVerificationService emailVerificationService;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .password("encodedPassword123")
                .phoneNumber("+48123456789")
                .role(Role.USER)
                .isActive(true)
                .emailVerified(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    // ========== Registration Edge Cases ==========

    @Test
    void registerUser_WithUnicodeCharactersInName_ShouldSucceed() {
        // Given
        UserRegistrationRequestDTO request = new UserRegistrationRequestDTO();
        request.setFirstName("José");
        request.setLastName("Müller");
        request.setEmail("jose.muller@example.com");
        request.setPassword("SecurePass123!");
        request.setPhoneNumber("+48123456789");

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User user = inv.getArgument(0);
            user.setId(1L);
            return user;
        });

        // When
        UserResponseDTO result = userService.registerUser(request);

        // Then
        assertThat(result.getFirstName()).isEqualTo("José");
        assertThat(result.getLastName()).isEqualTo("Müller");
    }

    @Test
    void registerUser_WithVeryLongNames_ShouldSucceed() {
        // Given
        UserRegistrationRequestDTO request = new UserRegistrationRequestDTO();
        request.setFirstName("A".repeat(100));
        request.setLastName("B".repeat(100));
        request.setEmail("long.name@example.com");
        request.setPassword("SecurePass123!");
        request.setPhoneNumber("+48123456789");

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User user = inv.getArgument(0);
            user.setId(1L);
            return user;
        });

        // When
        UserResponseDTO result = userService.registerUser(request);

        // Then
        assertThat(result.getFirstName()).hasSize(100);
        assertThat(result.getLastName()).hasSize(100);
    }

    @Test
    void registerUser_WithInternationalPhoneNumber_ShouldSucceed() {
        // Given
        UserRegistrationRequestDTO request = new UserRegistrationRequestDTO();
        request.setFirstName("Jane");
        request.setLastName("Smith");
        request.setEmail("jane@example.com");
        request.setPassword("SecurePass123!");
        request.setPhoneNumber("+861234567890"); // China

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User user = inv.getArgument(0);
            user.setId(1L);
            return user;
        });

        // When
        UserResponseDTO result = userService.registerUser(request);

        // Then
        assertThat(result.getPhoneNumber()).isEqualTo("+861234567890");
    }

    @Test
    void registerUser_EmailAlreadyExists_CaseSensitive_ShouldFail() {
        // Given
        UserRegistrationRequestDTO request = new UserRegistrationRequestDTO();
        request.setFirstName("Jane");
        request.setLastName("Smith");
        request.setEmail("JOHN@EXAMPLE.COM"); // Uppercase
        request.setPassword("SecurePass123!");

        when(userRepository.existsByEmail("JOHN@EXAMPLE.COM")).thenReturn(true);

        // When/Then
        assertThatThrownBy(() -> userService.registerUser(request))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Email already registered");

        verify(userRepository, never()).save(any());
    }

    @Test
    void registerUser_EmailVerificationServiceFails_ShouldStillRegisterUser() {
        // Given
        UserRegistrationRequestDTO request = new UserRegistrationRequestDTO();
        request.setFirstName("Jane");
        request.setLastName("Smith");
        request.setEmail("jane@example.com");
        request.setPassword("SecurePass123!");
        request.setPhoneNumber("+48123456789");

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User user = inv.getArgument(0);
            user.setId(1L);
            return user;
        });
        doThrow(new RuntimeException("Email service down"))
                .when(emailVerificationService).sendVerificationEmail(any());

        // When
        UserResponseDTO result = userService.registerUser(request);

        // Then - User should still be registered
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("jane@example.com");
        verify(userRepository).save(any());
    }

    @Test
    void registerUser_WithNullPhoneNumber_ShouldSucceed() {
        // Given
        UserRegistrationRequestDTO request = new UserRegistrationRequestDTO();
        request.setFirstName("Jane");
        request.setLastName("Smith");
        request.setEmail("jane@example.com");
        request.setPassword("SecurePass123!");
        request.setPhoneNumber(null);

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User user = inv.getArgument(0);
            user.setId(1L);
            return user;
        });

        // When
        UserResponseDTO result = userService.registerUser(request);

        // Then
        assertThat(result.getPhoneNumber()).isNull();
    }

    // ========== Update Profile Edge Cases ==========

    @Test
    void updateProfileByEmail_ChangingToSameEmail_ShouldSucceed() {
        // Given
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateProfileRequestDTO request = new UpdateProfileRequestDTO();
        request.setEmail("john@example.com"); // Same email
        request.setPhoneNumber("+48999888777");

        // When
        UserResponseDTO result = userService.updateProfileByEmail("john@example.com", request);

        // Then
        assertThat(result.getEmail()).isEqualTo("john@example.com");
        assertThat(result.getPhoneNumber()).isEqualTo("+48999888777");
        verify(userRepository, never()).existsByEmail(anyString());
    }

    @Test
    void updateProfileByEmail_ChangingEmailToDifferentCase_ShouldFail() {
        // Given
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail("already_taken@example.com")).thenReturn(true);

        UpdateProfileRequestDTO request = new UpdateProfileRequestDTO();
        request.setEmail("already_taken@example.com");
        request.setPhoneNumber("+48999888777");

        // When/Then
        assertThatThrownBy(() -> userService.updateProfileByEmail("john@example.com", request))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Email already in use");
    }

    @Test
    void updateProfileByEmail_WithNullEmail_ShouldOnlyUpdatePhone() {
        // Given
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateProfileRequestDTO request = new UpdateProfileRequestDTO();
        request.setEmail(null);
        request.setPhoneNumber("+48999888777");

        // When
        UserResponseDTO result = userService.updateProfileByEmail("john@example.com", request);

        // Then
        assertThat(result.getEmail()).isEqualTo("john@example.com"); // Unchanged
        assertThat(result.getPhoneNumber()).isEqualTo("+48999888777");
    }

    @Test
    void updateProfileByEmail_ClearingPhoneNumber_ShouldSucceed() {
        // Given
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateProfileRequestDTO request = new UpdateProfileRequestDTO();
        request.setEmail("john@example.com");
        request.setPhoneNumber(null);

        // When
        UserResponseDTO result = userService.updateProfileByEmail("john@example.com", request);

        // Then
        assertThat(result.getPhoneNumber()).isNull();
    }

    @Test
    void updateProfileByEmail_MultipleUpdatesInSequence_ShouldAllSucceed() {
        // Given
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        // When - First update
        UpdateProfileRequestDTO request1 = new UpdateProfileRequestDTO();
        request1.setEmail("john.new1@example.com");
        request1.setPhoneNumber("+48111111111");
        userService.updateProfileByEmail("john@example.com", request1);

        testUser.setEmail("john.new1@example.com");

        // Second update
        UpdateProfileRequestDTO request2 = new UpdateProfileRequestDTO();
        request2.setEmail("john.new2@example.com");
        request2.setPhoneNumber("+48222222222");
        UserResponseDTO result = userService.updateProfileByEmail("john.new1@example.com", request2);

        // Then
        assertThat(result.getEmail()).isEqualTo("john.new2@example.com");
        assertThat(result.getPhoneNumber()).isEqualTo("+48222222222");
        verify(userRepository, times(2)).save(any());
    }

    // ========== Password Change Edge Cases ==========

    @Test
    void changePasswordByEmail_WithVeryLongPassword_ShouldSucceed() {
        // Given
        String longPassword = "A".repeat(200) + "123!";
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(passwordEncoder.encode(longPassword)).thenReturn("encodedLongPassword");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        ChangePasswordRequestDTO request = new ChangePasswordRequestDTO();
        request.setCurrentPassword("oldPassword");
        request.setNewPassword(longPassword);

        // When
        userService.changePasswordByEmail("john@example.com", request);

        // Then
        verify(passwordEncoder).encode(longPassword);
        verify(userRepository).save(argThat(user ->
                user.getPassword().equals("encodedLongPassword")
        ));
    }

    @Test
    void changePasswordByEmail_WithSpecialCharactersInPassword_ShouldSucceed() {
        // Given
        String complexPassword = "P@$$w0rd!#%&*()[]{}";
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(passwordEncoder.encode(complexPassword)).thenReturn("encodedComplexPassword");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        ChangePasswordRequestDTO request = new ChangePasswordRequestDTO();
        request.setCurrentPassword("oldPassword");
        request.setNewPassword(complexPassword);

        // When
        userService.changePasswordByEmail("john@example.com", request);

        // Then
        verify(passwordEncoder).encode(complexPassword);
    }

    @Test
    void changePasswordByEmail_SameAsCurrentPassword_ShouldStillSucceed() {
        // Given
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("samePassword", testUser.getPassword())).thenReturn(true);
        when(passwordEncoder.encode("samePassword")).thenReturn("encodedSamePassword");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        ChangePasswordRequestDTO request = new ChangePasswordRequestDTO();
        request.setCurrentPassword("samePassword");
        request.setNewPassword("samePassword");

        // When
        userService.changePasswordByEmail("john@example.com", request);

        // Then
        verify(userRepository).save(any());
    }

    @Test
    void changePasswordByEmail_ChangingMultipleTimes_ShouldAllSucceed() {
        // Given
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        // When - Change 3 times
        for (int i = 1; i <= 3; i++) {
            ChangePasswordRequestDTO request = new ChangePasswordRequestDTO();
            request.setCurrentPassword("oldPassword" + i);
            request.setNewPassword("newPassword" + i);
            userService.changePasswordByEmail("john@example.com", request);
        }

        // Then
        verify(userRepository, times(3)).save(any());
        verify(passwordEncoder, times(3)).encode(anyString());
    }

    @Test
    void changePasswordByEmail_WithWhitespaceInPassword_ShouldSucceed() {
        // Given
        String passwordWithSpaces = "my secure password 123!";
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(passwordEncoder.encode(passwordWithSpaces)).thenReturn("encodedWithSpaces");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        ChangePasswordRequestDTO request = new ChangePasswordRequestDTO();
        request.setCurrentPassword("oldPassword");
        request.setNewPassword(passwordWithSpaces);

        // When
        userService.changePasswordByEmail("john@example.com", request);

        // Then
        verify(passwordEncoder).encode(passwordWithSpaces);
    }

    // ========== Delete User Edge Cases ==========

    @Test
    void deleteUser_WithNonExistentUser_ShouldThrowException() {
        // Given
        when(userRepository.existsById(999L)).thenReturn(false);

        // When/Then
        assertThatThrownBy(() -> userService.deleteUser(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with ID: 999");

        verify(userRepository, never()).deleteById(any());
    }

    @Test
    void deleteUserByEmail_WithCascadingRelations_ShouldDelete() {
        // Given
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));

        // When
        userService.deleteUserByEmail("john@example.com");

        // Then
        verify(userRepository).delete(testUser);
    }

    @Test
    void deleteUserById_AfterDeletion_ShouldNotExist() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When
        userService.deleteUserById(1L);

        // Then
        verify(userRepository).delete(testUser);
    }

    // ========== Verify User Edge Cases ==========

    @Test
    void verifyUser_SetsVerifiedAtTimestamp_ShouldBeRecent() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        LocalDateTime beforeVerify = LocalDateTime.now().minusSeconds(1);
        UserResponseDTO result = userService.verifyUser(1L);
        LocalDateTime afterVerify = LocalDateTime.now().plusSeconds(1);

        // Then
        assertThat(result.getRole()).isEqualTo(Role.VERIFIED_USER);
        assertThat(result.getVerifiedAt()).isNotNull();
        assertThat(result.getVerifiedAt()).isBetween(beforeVerify, afterVerify);
    }

    @Test
    void verifyUser_AlreadyVerified_ShouldUpdateAgain() {
        // Given
        testUser.setRole(Role.VERIFIED_USER);
        testUser.setVerifiedAt(LocalDateTime.now().minusDays(1));

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        UserResponseDTO result = userService.verifyUser(1L);

        // Then - Should update verifiedAt timestamp
        assertThat(result.getRole()).isEqualTo(Role.VERIFIED_USER);
        verify(userRepository).save(any());
    }

    // ========== Get User Edge Cases ==========

    @Test
    void getUserById_CalledMultipleTimes_ShouldReturnConsistently() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When
        UserResponseDTO result1 = userService.getUserById(1L);
        UserResponseDTO result2 = userService.getUserById(1L);
        UserResponseDTO result3 = userService.getUserById(1L);

        // Then
        assertThat(result1.getId()).isEqualTo(1L);
        assertThat(result2.getId()).isEqualTo(1L);
        assertThat(result3.getId()).isEqualTo(1L);
        verify(userRepository, times(3)).findById(1L);
    }

    @Test
    void getUserByEmail_WithDifferentCasing_ShouldFindCorrectUser() {
        // Given
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(userRepository.findByEmail("JOHN@EXAMPLE.COM")).thenReturn(Optional.empty());

        // When
        UserResponseDTO result = userService.getUserByEmail("john@example.com");

        // Then
        assertThat(result.getEmail()).isEqualTo("john@example.com");

        // When/Then - Different case should not find
        assertThatThrownBy(() -> userService.getUserByEmail("JOHN@EXAMPLE.COM"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}