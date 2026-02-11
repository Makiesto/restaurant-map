package com.example.demo.controller;

import com.example.demo.dto.user.ChangePasswordRequestDTO;
import com.example.demo.dto.user.UpdateProfileRequestDTO;
import com.example.demo.dto.user.UserResponseDTO;
import com.example.demo.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;

    private String getCurrentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getName(); // email, set by CustomUserDetailsService
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getCurrentUser() {
        String email = getCurrentUserEmail();
        log.info("GET /api/users/me - {}", email);
        return ResponseEntity.ok(userService.getUserByEmail(email));
    }

    @PatchMapping("/me")
    public ResponseEntity<UserResponseDTO> updateProfile(
            @Valid @RequestBody UpdateProfileRequestDTO request) {
        String email = getCurrentUserEmail();
        log.info("PATCH /api/users/me - {}", email);
        return ResponseEntity.ok(userService.updateProfileByEmail(email, request));
    }

    @PostMapping("/me/change-password")
    public ResponseEntity<Void> changePassword(
            @Valid @RequestBody ChangePasswordRequestDTO request) {
        String email = getCurrentUserEmail();
        log.info("POST /api/users/me/change-password - {}", email);
        userService.changePasswordByEmail(email, request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long id) {
        log.info("GET /api/users/{}", id);
        return ResponseEntity.ok(userService.getUserById(id));
    }
}