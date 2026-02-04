package com.example.demo.dto.restaurant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

@Data
public class RestaurantCreateRequestDTO {

    @NotBlank(message = "Restaurant name is required")
    private String name;

    @NotBlank(message = "Address is required")
    private String address;

    private String phone;

    private String openingHours;

    private String description;

    private String imageUrl;

    private String cuisineType;

    private String priceRange;

    private Set<String> dietaryOptions;
}