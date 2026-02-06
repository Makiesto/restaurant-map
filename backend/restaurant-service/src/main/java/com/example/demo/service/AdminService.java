package com.example.demo.service;

import com.example.demo.dto.admin.AdminStatsDTO;
import com.example.demo.entity.RestaurantStatus;
import com.example.demo.repository.RestaurantRepository;
import com.example.demo.repository.ReviewRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final ReviewRepository reviewRepository;

    @Transactional(readOnly = true)
    public AdminStatsDTO getAdminStats() {
        log.info("Fetching admin statistics");

        Long totalUsers = userRepository.count();
        Long totalRestaurants = restaurantRepository.count();
        Long pendingRestaurants = restaurantRepository.countByStatus(RestaurantStatus.PENDING);
        Long approvedRestaurants = restaurantRepository.countByStatus(RestaurantStatus.APPROVED);
        Long rejectedRestaurants = restaurantRepository.countByStatus(RestaurantStatus.REJECTED);
        Long verifiedRestaurants = restaurantRepository.countByIsVerified(true);
        Long unverifiedRestaurants = restaurantRepository.countByIsVerified(false);
        Long totalReviews = reviewRepository.count();
        Long unverifiedReviews = reviewRepository.countByIsVerified(false);

        return AdminStatsDTO.builder()
                .totalUsers(totalUsers)
                .totalRestaurants(totalRestaurants)
                .pendingRestaurants(pendingRestaurants)
                .approvedRestaurants(approvedRestaurants)
                .rejectedRestaurants(rejectedRestaurants)
                .verifiedRestaurants(verifiedRestaurants)
                .unverifiedRestaurants(unverifiedRestaurants)
                .totalReviews(totalReviews)
                .unverifiedReviews(unverifiedReviews)
                .build();
    }
}