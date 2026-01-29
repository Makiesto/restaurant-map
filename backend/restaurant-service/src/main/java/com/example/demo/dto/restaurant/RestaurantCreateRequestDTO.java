package com.example.demo.dto.restaurant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RestaurantCreateRequestDTO {

    @NotBlank(message = "Restaurant name is required")
    private String name;

    @NotBlank(message = "Address is required")
    private String address;

    private Double latitude;

    private Double longitude;

    private String phone;

    private String description;

    private String openingHours;
}