package com.example.demo.dto.review;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReviewStatsDTO {
    private Double averageRating;
    private Long totalReviews;
    private Long fiveStars;
    private Long fourStars;
    private Long threeStars;
    private Long twoStars;
    private Long oneStar;
}