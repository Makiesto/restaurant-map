package com.example.demo.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResendVerificationRequestDTO {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
}