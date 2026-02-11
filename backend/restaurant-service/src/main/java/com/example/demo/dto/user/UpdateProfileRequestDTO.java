package com.example.demo.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateProfileRequestDTO {

    @NotNull(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    private String phoneNumber;
}