package com.example.demo.controller;

import com.example.demo.dto.user.UserRegistrationRequestDTO;
import com.example.demo.dto.user.UserResponseDTO;
import com.example.demo.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AuthController {
    
    private final UserService userService;
    
    /**
     * PUBLIC: Register new user
     */
    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> register(
            @Valid @RequestBody UserRegistrationRequestDTO request) {
        log.info("POST /api/auth/register - registering user: {}", request.getEmail());
        UserResponseDTO user = userService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }
    
    /**
     * PUBLIC: Login
     * TODO: Implement JWT authentication
     */
    @PostMapping("/login")
    public ResponseEntity<String> login() {
        // TODO: Implement JWT login
        return ResponseEntity.ok("Login endpoint - JWT implementation coming soon");
    }
}