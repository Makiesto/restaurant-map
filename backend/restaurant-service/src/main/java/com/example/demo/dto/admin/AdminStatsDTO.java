package com.example.demo.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminStatsDTO {
    private Long totalUsers;
    private Long totalRestaurants;
    private Long pendingRestaurants;
    private Long approvedRestaurants;
    private Long rejectedRestaurants;
    private Long verifiedRestaurants;
    private Long unverifiedRestaurants;
    private Long totalReviews;
    private Long unverifiedReviews;
}