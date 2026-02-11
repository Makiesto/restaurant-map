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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponseDTO registerUser(UserRegistrationRequestDTO request) {
        log.info("Registering new user with email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ValidationException("Email already registered");
        }

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .role(Role.VERIFIED_USER) // TODO In future add some mechanism to verify users
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();

        User savedUser = userRepository.save(user);
        log.info("User registered successfully with ID: {}", savedUser.getId());
        return mapToResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public UserResponseDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        return mapToResponse(user);
    }

    @Transactional(readOnly = true)
    public UserResponseDTO getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return mapToResponse(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserResponseDTO verifyUser(Long userId) {
        log.info("Verifying user with ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        user.setRole(Role.VERIFIED_USER);
        user.setVerifiedAt(LocalDateTime.now());
        return mapToResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponseDTO updateProfileByEmail(String currentEmail, UpdateProfileRequestDTO request) {
        log.info("==== UPDATE PROFILE START ====");
        log.info("Current authenticated email: {}", currentEmail);
        log.info("Request email: {}", request.getEmail());
        log.info("Request phoneNumber: {}", request.getPhoneNumber());

        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> {
                    log.error("User not found with email: {}", currentEmail);
                    return new ResourceNotFoundException("User not found with email: " + currentEmail);
                });

        log.info("Found user: id={}, email={}, firstName={}", user.getId(), user.getEmail(), user.getFirstName());

        // Update phone number (always safe)
        user.setPhoneNumber(request.getPhoneNumber());
        log.info("Updated phone number to: {}", request.getPhoneNumber());

        // Check if email is being changed to a different address
        if (request.getEmail() != null && !request.getEmail().equalsIgnoreCase(currentEmail)) {
            log.info("Email change detected: {} -> {}", currentEmail, request.getEmail());

            // Verify new email is not already taken
            if (userRepository.existsByEmail(request.getEmail())) {
                log.error("Email already in use: {}", request.getEmail());
                throw new ValidationException("Email already in use by another account");
            }

            log.info("New email is available, updating...");
            user.setEmail(request.getEmail());
            log.info("Email updated in entity to: {}", user.getEmail());
        } else {
            log.info("No email change (same email or null)");
        }

        User savedUser = userRepository.save(user);
        log.info("User saved: id={}, email={}", savedUser.getId(), savedUser.getEmail());
        log.info("==== UPDATE PROFILE END ====");

        return mapToResponse(savedUser);
    }

    @Transactional
    public void changePasswordByEmail(String email, ChangePasswordRequestDTO request) {
        log.info("Changing password for user: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new ValidationException("Current password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long userId) {
        log.info("Deleting user with ID: {}", userId);
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with ID: " + userId);
        }
        userRepository.deleteById(userId);
        log.info("User deleted successfully: {}", userId);
    }

    private UserResponseDTO mapToResponse(User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .verifiedAt(user.getVerifiedAt())
                .build();
    }
}