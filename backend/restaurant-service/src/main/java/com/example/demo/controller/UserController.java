package com.example.demo.controller;

import com.example.demo.dto.user.ChangePasswordRequestDTO;
import com.example.demo.dto.user.UpdateProfileRequestDTO;
import com.example.demo.dto.user.UserResponseDTO;
import com.example.demo.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;

    // ========================================
    // AUTHENTICATED USER ENDPOINTS
    // ========================================

    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        log.info("Getting current user profile for: {}", email);
        return ResponseEntity.ok(userService.getUserByEmail(email));
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponseDTO> updateProfile(
            @Valid @RequestBody UpdateProfileRequestDTO request,
            Authentication authentication) {
        String currentEmail = authentication.getName();
        log.info("Updating profile for user: {}", currentEmail);
        return ResponseEntity.ok(userService.updateProfileByEmail(currentEmail, request));
    }

    @PutMapping("/me/change-password")
    public ResponseEntity<Void> changePassword(
            @Valid @RequestBody ChangePasswordRequestDTO request,
            Authentication authentication) {
        String email = authentication.getName();
        log.info("Password change request for user: {}", email);
        userService.changePasswordByEmail(email, request);
        return ResponseEntity.noContent().build(); // 204 instead of 200
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMyAccount(Authentication authentication) {
        String email = authentication.getName();
        log.info("Account deletion request for user: {}", email);
        userService.deleteUserByEmail(email);
        return ResponseEntity.noContent().build(); // 204
    }

    // ========================================
    // ADMIN ENDPOINTS (tests expect these)
    // ========================================

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long id) {
        log.info("Admin fetching user with id: {}", id);
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        log.info("Admin deleting user with id: {}", id);
        userService.deleteUserById(id);
        return ResponseEntity.noContent().build(); // 204
    }
}