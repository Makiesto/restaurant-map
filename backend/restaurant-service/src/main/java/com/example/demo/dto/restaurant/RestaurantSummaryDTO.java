package com.example.demo.dto.restaurant;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RestaurantSummaryDTO {
    private Long id;
    private String name;
    private Double rating;
}