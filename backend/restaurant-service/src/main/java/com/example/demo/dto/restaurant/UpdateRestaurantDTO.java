package com.example.demo.dto.restaurant;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateRestaurantDTO {
    private String name;
    private String phone;
    private String description;
    private String openingHours;
}