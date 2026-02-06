package com.example.demo.dto.allergen;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserAllergensRequestDTO {

    @NotNull(message = "Allergen names list is required")
    private List<String> allergenNames;
}