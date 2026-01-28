package com.example.demo.dto.restaurant;

import com.example.demo.dto.dish.DishSummaryDTO;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class RestaurantDetailDTO {
    private Long id;
    private String name;
    private String ownerName;
    private Double latitude;
    private Double longitude;
    private Double rating;
    private String phone;
    private String description;
    private LocalDateTime createdAt;
    private List<DishSummaryDTO> dishe;
}