package com.example.demo.dto.user;

import com.example.demo.entity.Role;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserResponseDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private Role role;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime verifiedAt;
}