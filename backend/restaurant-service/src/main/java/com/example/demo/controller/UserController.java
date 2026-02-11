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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        log.info("Getting current user profile for: {}", email);
        return ResponseEntity.ok(userService.getUserByEmail(email));
    }

    @PatchMapping("/me")
    public ResponseEntity<UserResponseDTO> updateProfile(
            @Valid @RequestBody UpdateProfileRequestDTO request,
            Authentication authentication) {
        String currentEmail = authentication.getName();
        log.info("Updating profile for user: {}", currentEmail);
        return ResponseEntity.ok(userService.updateProfileByEmail(currentEmail, request));
    }

    @PostMapping("/me/change-password")
    public ResponseEntity<Void> changePassword(
            @Valid @RequestBody ChangePasswordRequestDTO request,
            Authentication authentication) {
        String email = authentication.getName();
        log.info("Password change request for user: {}", email);
        userService.changePasswordByEmail(email, request);
        return ResponseEntity.ok().build();
    }
}