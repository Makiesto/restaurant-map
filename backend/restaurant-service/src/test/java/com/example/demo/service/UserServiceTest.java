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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

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
    private UserRegistrationRequestDTO registrationRequest;

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

        registrationRequest = new UserRegistrationRequestDTO();
        registrationRequest.setFirstName("Jane");
        registrationRequest.setLastName("Smith");
        registrationRequest.setEmail("jane@example.com");
        registrationRequest.setPassword("password123");
        registrationRequest.setPhoneNumber("+48987654321");
    }

    @Test
    void registerUser_WithValidData_ShouldSucceed() {
        // Given
        when(userRepository.existsByEmail(registrationRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        // When
        UserResponseDTO result = userService.registerUser(registrationRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getFirstName()).isEqualTo("Jane");
        assertThat(result.getLastName()).isEqualTo("Smith");
        assertThat(result.getEmail()).isEqualTo("jane@example.com");
        assertThat(result.getRole()).isEqualTo(Role.USER);
        assertThat(result.getEmailVerified()).isFalse();

        verify(userRepository).existsByEmail("jane@example.com");
        verify(userRepository).save(any(User.class));
        verify(emailVerificationService).sendVerificationEmail(any(User.class));
    }

    @Test
    void registerUser_WithExistingEmail_ShouldThrowException() {
        // Given
        when(userRepository.existsByEmail(registrationRequest.getEmail())).thenReturn(true);

        // When/Then
        assertThatThrownBy(() -> userService.registerUser(registrationRequest))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Email already registered");

        verify(userRepository).existsByEmail("jane@example.com");
        verify(userRepository, never()).save(any());
        verify(emailVerificationService, never()).sendVerificationEmail(any());
    }

    @Test
    void getUserById_WhenExists_ShouldReturnUser() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When
        UserResponseDTO result = userService.getUserById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo("john@example.com");
        assertThat(result.getFirstName()).isEqualTo("John");
    }

    @Test
    void getUserById_WhenNotExists_ShouldThrowException() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> userService.getUserById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with ID: 999");
    }

    @Test
    void getUserByEmail_WhenExists_ShouldReturnUser() {
        // Given
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));

        // When
        UserResponseDTO result = userService.getUserByEmail("john@example.com");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void getUserByEmail_WhenNotExists_ShouldThrowException() {
        // Given
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> userService.getUserByEmail("nonexistent@example.com"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with email");
    }

    @Test
    void getAllUsers_ShouldReturnAllUsers() {
        // Given
        User user2 = User.builder()
                .id(2L)
                .firstName("Jane")
                .lastName("Smith")
                .email("jane@example.com")
                .role(Role.USER)
                .isActive(true)
                .build();

        when(userRepository.findAll()).thenReturn(List.of(testUser, user2));

        // When
        List<UserResponseDTO> results = userService.getAllUsers();

        // Then
        assertThat(results).hasSize(2);
        assertThat(results).extracting(UserResponseDTO::getEmail)
                .containsExactly("john@example.com", "jane@example.com");
    }

    @Test
    void verifyUser_ShouldPromoteToVerifiedUser() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        UserResponseDTO result = userService.verifyUser(1L);

        // Then
        assertThat(result.getRole()).isEqualTo(Role.VERIFIED_USER);
        assertThat(result.getVerifiedAt()).isNotNull();

        verify(userRepository).save(argThat(user ->
                user.getRole() == Role.VERIFIED_USER && user.getVerifiedAt() != null
        ));
    }

    @Test
    void updateProfileByEmail_WithNewEmail_ShouldUpdateEmail() {
        // Given
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail("newemail@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateProfileRequestDTO updateRequest = new UpdateProfileRequestDTO();
        updateRequest.setEmail("newemail@example.com");
        updateRequest.setPhoneNumber("+48111222333");

        // When
        UserResponseDTO result = userService.updateProfileByEmail("john@example.com", updateRequest);

        // Then
        assertThat(result.getEmail()).isEqualTo("newemail@example.com");
        assertThat(result.getPhoneNumber()).isEqualTo("+48111222333");

        verify(userRepository).save(argThat(user ->
                user.getEmail().equals("newemail@example.com") &&
                user.getPhoneNumber().equals("+48111222333")
        ));
    }

    @Test
    void updateProfileByEmail_WithExistingEmail_ShouldThrowException() {
        // Given
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        UpdateProfileRequestDTO updateRequest = new UpdateProfileRequestDTO();
        updateRequest.setEmail("existing@example.com");
        updateRequest.setPhoneNumber("+48111222333");

        // When/Then
        assertThatThrownBy(() -> userService.updateProfileByEmail("john@example.com", updateRequest))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Email already in use");

        verify(userRepository, never()).save(any());
    }

    @Test
    void updateProfileByEmail_WithSameEmail_ShouldOnlyUpdatePhone() {
        // Given
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateProfileRequestDTO updateRequest = new UpdateProfileRequestDTO();
        updateRequest.setEmail("john@example.com"); // Same email
        updateRequest.setPhoneNumber("+48111222333");

        // When
        UserResponseDTO result = userService.updateProfileByEmail("john@example.com", updateRequest);

        // Then
        assertThat(result.getEmail()).isEqualTo("john@example.com");
        assertThat(result.getPhoneNumber()).isEqualTo("+48111222333");

        verify(userRepository, never()).existsByEmail(anyString());
    }

    @Test
    void changePasswordByEmail_WithCorrectCurrentPassword_ShouldSucceed() {
        // Given
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("oldPassword", "encodedPassword123")).thenReturn(true);
        when(passwordEncoder.encode("newPassword")).thenReturn("encodedNewPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ChangePasswordRequestDTO changeRequest = new ChangePasswordRequestDTO();
        changeRequest.setCurrentPassword("oldPassword");
        changeRequest.setNewPassword("newPassword");

        // When
        userService.changePasswordByEmail("john@example.com", changeRequest);

        // Then
        verify(passwordEncoder).matches("oldPassword", "encodedPassword123");
        verify(passwordEncoder).encode("newPassword");
        verify(userRepository).save(argThat(user ->
                user.getPassword().equals("encodedNewPassword")
        ));
    }

    @Test
    void changePasswordByEmail_WithIncorrectCurrentPassword_ShouldThrowException() {
        // Given
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongPassword", "encodedPassword123")).thenReturn(false);

        ChangePasswordRequestDTO changeRequest = new ChangePasswordRequestDTO();
        changeRequest.setCurrentPassword("wrongPassword");
        changeRequest.setNewPassword("newPassword");

        // When/Then
        assertThatThrownBy(() -> userService.changePasswordByEmail("john@example.com", changeRequest))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Current password is incorrect");

        verify(userRepository, never()).save(any());
    }

    @Test
    void deleteUser_WhenExists_ShouldDeleteUser() {
        // Given
        when(userRepository.existsById(1L)).thenReturn(true);

        // When
        userService.deleteUser(1L);

        // Then
        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUser_WhenNotExists_ShouldThrowException() {
        // Given
        when(userRepository.existsById(999L)).thenReturn(false);

        // When/Then
        assertThatThrownBy(() -> userService.deleteUser(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with ID: 999");

        verify(userRepository, never()).deleteById(any());
    }
}