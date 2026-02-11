package com.example.demo.dto.user;

import com.example.demo.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private Role role;
    private Boolean isActive;
    private Boolean emailVerified;
    private LocalDateTime emailVerifiedAt;
    private LocalDateTime createdAt;
    private LocalDateTime verifiedAt;
}