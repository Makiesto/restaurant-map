package com.example.demo.dto.review;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ReviewResponseDTO {
    private Long id;
    private Long restaurantId;
    private String restaurantName;
    private Long userId;
    private String userName;
    private Integer rating;
    private String comment;
    private Boolean isVerified;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}