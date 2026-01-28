package com.example.demo.controller;

import com.example.demo.dto.user.UserResponseDTO;
import com.example.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;

    /**
     * AUTHENTICATED: Get current user info
     * TODO: Get user ID from JWT token
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getCurrentUser(
            @RequestHeader("User-Id") Long userId) { // TODO: Get from JWT token
        log.info("GET /api/users/me - fetching user: {}", userId);
        UserResponseDTO user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    /**
     * AUTHENTICATED: Get user by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long id) {
        log.info("GET /api/users/{} - fetching user", id);
        UserResponseDTO user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }
}