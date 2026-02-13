package com.example.demo.service;

import com.example.demo.dto.admin.AdminStatsDTO;
import com.example.demo.entity.RestaurantStatus;
import com.example.demo.repository.RestaurantRepository;
import com.example.demo.repository.ReviewRepository;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private AdminService adminService;

    @BeforeEach
    void setUp() {
        // Mock data will be set up in individual tests
    }

    @Test
    void getAdminStats_ShouldReturnCorrectStatistics() {
        // Given
        when(userRepository.count()).thenReturn(100L);
        when(restaurantRepository.count()).thenReturn(50L);
        when(restaurantRepository.countByStatus(RestaurantStatus.PENDING)).thenReturn(10L);
        when(restaurantRepository.countByStatus(RestaurantStatus.APPROVED)).thenReturn(35L);
        when(restaurantRepository.countByStatus(RestaurantStatus.REJECTED)).thenReturn(5L);
        when(restaurantRepository.countByIsVerified(true)).thenReturn(30L);
        when(restaurantRepository.countByIsVerified(false)).thenReturn(20L);
        when(reviewRepository.count()).thenReturn(200L);
        when(reviewRepository.countByIsVerified(false)).thenReturn(50L);

        // When
        AdminStatsDTO stats = adminService.getAdminStats();

        // Then
        assertThat(stats.getTotalUsers()).isEqualTo(100L);
        assertThat(stats.getTotalRestaurants()).isEqualTo(50L);
        assertThat(stats.getPendingRestaurants()).isEqualTo(10L);
        assertThat(stats.getApprovedRestaurants()).isEqualTo(35L);
        assertThat(stats.getRejectedRestaurants()).isEqualTo(5L);
        assertThat(stats.getVerifiedRestaurants()).isEqualTo(30L);
        assertThat(stats.getUnverifiedRestaurants()).isEqualTo(20L);
        assertThat(stats.getTotalReviews()).isEqualTo(200L);
        assertThat(stats.getUnverifiedReviews()).isEqualTo(50L);
    }

    @Test
    void getAdminStats_WithNoData_ShouldReturnZeros() {
        // Given
        when(userRepository.count()).thenReturn(0L);
        when(restaurantRepository.count()).thenReturn(0L);
        when(restaurantRepository.countByStatus(RestaurantStatus.PENDING)).thenReturn(0L);
        when(restaurantRepository.countByStatus(RestaurantStatus.APPROVED)).thenReturn(0L);
        when(restaurantRepository.countByStatus(RestaurantStatus.REJECTED)).thenReturn(0L);
        when(restaurantRepository.countByIsVerified(true)).thenReturn(0L);
        when(restaurantRepository.countByIsVerified(false)).thenReturn(0L);
        when(reviewRepository.count()).thenReturn(0L);
        when(reviewRepository.countByIsVerified(false)).thenReturn(0L);

        // When
        AdminStatsDTO stats = adminService.getAdminStats();

        // Then
        assertThat(stats.getTotalUsers()).isEqualTo(0L);
        assertThat(stats.getTotalRestaurants()).isEqualTo(0L);
        assertThat(stats.getPendingRestaurants()).isEqualTo(0L);
        assertThat(stats.getApprovedRestaurants()).isEqualTo(0L);
        assertThat(stats.getRejectedRestaurants()).isEqualTo(0L);
        assertThat(stats.getVerifiedRestaurants()).isEqualTo(0L);
        assertThat(stats.getUnverifiedRestaurants()).isEqualTo(0L);
        assertThat(stats.getTotalReviews()).isEqualTo(0L);
        assertThat(stats.getUnverifiedReviews()).isEqualTo(0L);
    }

    @Test
    void getAdminStats_WithAllPendingRestaurants_ShouldShowCorrectCounts() {
        // Given
        when(userRepository.count()).thenReturn(50L);
        when(restaurantRepository.count()).thenReturn(20L);
        when(restaurantRepository.countByStatus(RestaurantStatus.PENDING)).thenReturn(20L);
        when(restaurantRepository.countByStatus(RestaurantStatus.APPROVED)).thenReturn(0L);
        when(restaurantRepository.countByStatus(RestaurantStatus.REJECTED)).thenReturn(0L);
        when(restaurantRepository.countByIsVerified(true)).thenReturn(0L);
        when(restaurantRepository.countByIsVerified(false)).thenReturn(20L);
        when(reviewRepository.count()).thenReturn(0L);
        when(reviewRepository.countByIsVerified(false)).thenReturn(0L);

        // When
        AdminStatsDTO stats = adminService.getAdminStats();

        // Then
        assertThat(stats.getPendingRestaurants()).isEqualTo(20L);
        assertThat(stats.getApprovedRestaurants()).isEqualTo(0L);
        assertThat(stats.getRejectedRestaurants()).isEqualTo(0L);
        assertThat(stats.getUnverifiedRestaurants()).isEqualTo(20L);
    }

    @Test
    void getAdminStats_WithMixedRestaurantStatuses_ShouldCalculateCorrectly() {
        // Given
        when(userRepository.count()).thenReturn(150L);
        when(restaurantRepository.count()).thenReturn(100L);
        when(restaurantRepository.countByStatus(RestaurantStatus.PENDING)).thenReturn(25L);
        when(restaurantRepository.countByStatus(RestaurantStatus.APPROVED)).thenReturn(60L);
        when(restaurantRepository.countByStatus(RestaurantStatus.REJECTED)).thenReturn(15L);
        when(restaurantRepository.countByIsVerified(true)).thenReturn(45L);
        when(restaurantRepository.countByIsVerified(false)).thenReturn(55L);
        when(reviewRepository.count()).thenReturn(500L);
        when(reviewRepository.countByIsVerified(false)).thenReturn(100L);

        // When
        AdminStatsDTO stats = adminService.getAdminStats();

        // Then
        // Verify the sum of statuses equals total restaurants
        Long totalByStatus = stats.getPendingRestaurants() +
                            stats.getApprovedRestaurants() +
                            stats.getRejectedRestaurants();
        assertThat(totalByStatus).isEqualTo(100L);

        // Verify the sum of verified statuses equals total restaurants
        Long totalByVerification = stats.getVerifiedRestaurants() +
                                   stats.getUnverifiedRestaurants();
        assertThat(totalByVerification).isEqualTo(100L);
    }

    @Test
    void getAdminStats_WithHighVolumeData_ShouldHandleCorrectly() {
        // Given - Simulating a large platform
        when(userRepository.count()).thenReturn(10000L);
        when(restaurantRepository.count()).thenReturn(5000L);
        when(restaurantRepository.countByStatus(RestaurantStatus.PENDING)).thenReturn(500L);
        when(restaurantRepository.countByStatus(RestaurantStatus.APPROVED)).thenReturn(4000L);
        when(restaurantRepository.countByStatus(RestaurantStatus.REJECTED)).thenReturn(500L);
        when(restaurantRepository.countByIsVerified(true)).thenReturn(3500L);
        when(restaurantRepository.countByIsVerified(false)).thenReturn(1500L);
        when(reviewRepository.count()).thenReturn(25000L);
        when(reviewRepository.countByIsVerified(false)).thenReturn(5000L);

        // When
        AdminStatsDTO stats = adminService.getAdminStats();

        // Then
        assertThat(stats.getTotalUsers()).isEqualTo(10000L);
        assertThat(stats.getTotalRestaurants()).isEqualTo(5000L);
        assertThat(stats.getTotalReviews()).isEqualTo(25000L);

        // Verify percentages make sense
        double approvalRate = (double) stats.getApprovedRestaurants() / stats.getTotalRestaurants();
        assertThat(approvalRate).isEqualTo(0.8); // 80% approval rate
    }
}