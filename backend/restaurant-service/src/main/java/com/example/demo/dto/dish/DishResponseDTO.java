package com.example.demo.dto.dish;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class DishResponseDTO {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private String imageUrl;
    private Boolean isAvailable;
    private Double baseProteinG;
    private Double baseFatG;
    private Double baseCarbsG;
    private Double baseKcal;
    private Long restaurantId;
    private String restaurantName;
    private List<ComponentInfo> components;
    private List<String> allergens;

    @Data
    @Builder
    public static class ComponentInfo {
        private Long componentId;
        private String componentName;
        private Double amount;
        private Boolean isOptional;
    }
}
