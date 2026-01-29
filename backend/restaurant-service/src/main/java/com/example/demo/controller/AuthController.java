package com.example.demo.controller;

import com.example.demo.dto.user.AuthResponseDTO;
import com.example.demo.dto.user.LoginRequestDTO;
import com.example.demo.dto.user.UserRegistrationRequestDTO;
import com.example.demo.dto.user.UserResponseDTO;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtUtil;
import com.example.demo.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AuthController {
    
    private final UserService userService;
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    
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
     * PUBLIC: Login and get JWT token
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        log.info("POST /api/auth/login - login attempt for: {}", request.getEmail());
        
        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
            
            // Get user details
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Generate JWT token
            String token = jwtUtil.generateToken(
                    user.getEmail(),
                    user.getId(),
                    user.getRole().name()
            );
            
            // Build response
            AuthResponseDTO response = AuthResponseDTO.builder()
                    .token(token)
                    .type("Bearer")
                    .id(user.getId())
                    .email(user.getEmail())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .role(user.getRole())
                    .build();
            
            log.info("User logged in successfully: {}", request.getEmail());
            return ResponseEntity.ok(response);
            
        } catch (AuthenticationException e) {
            log.error("Login failed for user: {}", request.getEmail());
            throw new RuntimeException("Invalid email or password");
        }
    }
}