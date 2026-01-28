package com.example.demo.dto.dish;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DishSummaryDTO {

    private Long id;
    private String name;
    private Double price;

}
