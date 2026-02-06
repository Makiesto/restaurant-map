package com.example.demo.dto.restaurant;

import com.example.demo.entity.RestaurantStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestaurantResponseDTO {

    private Long id;
    private String name;
    private String address;
    private Double latitude;
    private Double longitude;
    private String phone;
    private String openingHours;
    private String description;
    private String imageUrl;
    private Double rating;
    private boolean isVerified;
    private LocalDateTime verifiedAt;
    private RestaurantStatus status;
    private String cuisineType;
    private String priceRange;
    private Set<String> dietaryOptions;

    private UserDTO owner;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserDTO {
        private Long id;
        private String firstName;
        private String lastName;
        private String email;
    }
}