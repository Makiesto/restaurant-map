package com.example.demo.dto.allergen;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AllergenResponseDTO {
    private Long id;
    private String name;
    private String severityLevel;
}