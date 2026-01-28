package com.example.demo.dto.dish;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class DishCreateRequestDTO {

    @NotBlank(message = "Dish name is required")
    private String name;

    private String description;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private Double price;

    private String imageUrl;

    private List<DishComponentRequest> components;

    @Data
    public static class DishComponentRequest {
        @NotNull
        private Long componentId;

        @NotNull
        @Positive
        private Double amount;

        private Boolean isOptional = false;
    }
}