package com.example.demo.controller;

import com.example.demo.dto.review.ReviewCreateRequestDTO;
import com.example.demo.dto.review.ReviewResponseDTO;
import com.example.demo.dto.review.ReviewStatsDTO;
import com.example.demo.service.ReviewService;
import com.example.demo.security.SecurityUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
class ReviewControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private ReviewService reviewService;

    @MockitoBean
    private SecurityUtil securityUtil;

    private ReviewResponseDTO testReviewDTO;
    private ReviewCreateRequestDTO createRequest;
    private ReviewStatsDTO statsDTO;

    @BeforeEach
    void setUp() {
        testReviewDTO = ReviewResponseDTO.builder()
                .id(1L)
                .restaurantId(1L)
                .restaurantName("Test Restaurant")
                .userId(1L)
                .userName("John Doe")
                .rating(5)
                .comment("Excellent food!")
                .isVerified(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        createRequest = new ReviewCreateRequestDTO();
        createRequest.setRating(5);
        createRequest.setComment("Great experience!");

        statsDTO = ReviewStatsDTO.builder()
                .averageRating(4.5)
                .totalReviews(100L)
                .fiveStars(50L)
                .fourStars(30L)
                .threeStars(10L)
                .twoStars(5L)
                .oneStar(5L)
                .build();
    }

    @Test
    @WithMockUser  // Add authentication
    void getRestaurantReviews_ShouldReturnReviews() throws Exception {
        // Given
        when(reviewService.getRestaurantReviews(1L))
                .thenReturn(List.of(testReviewDTO));

        // When/Then
        mockMvc.perform(get("/api/restaurants/1/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].rating").value(5))
                .andExpect(jsonPath("$[0].comment").value("Excellent food!"));
    }

    @Test
    @WithMockUser  // Add authentication
    void getRestaurantStats_ShouldReturnStatistics() throws Exception {
        // Given
        when(reviewService.getRestaurantStats(1L)).thenReturn(statsDTO);

        // When/Then
        mockMvc.perform(get("/api/restaurants/1/reviews/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageRating").value(4.5))
                .andExpect(jsonPath("$.totalReviews").value(100))
                .andExpect(jsonPath("$.fiveStars").value(50));
    }

    @Test
    @WithMockUser
    void getMyReviewForRestaurant_WhenExists_ShouldReturnReview() throws Exception {
        // Given
        when(securityUtil.getCurrentUserId()).thenReturn(1L);
        when(reviewService.getUserReviewForRestaurant(1L, 1L))
                .thenReturn(testReviewDTO);

        // When/Then
        mockMvc.perform(get("/api/restaurants/1/reviews/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.rating").value(5));
    }

    @Test
    @WithMockUser
    void getMyReviewForRestaurant_WhenNotExists_ShouldReturnNotFound() throws Exception {
        // Given
        when(securityUtil.getCurrentUserId()).thenReturn(1L);
        when(reviewService.getUserReviewForRestaurant(1L, 1L)).thenReturn(null);

        // When/Then
        mockMvc.perform(get("/api/restaurants/1/reviews/my"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void getMyReviews_ShouldReturnUserReviews() throws Exception {
        // Given
        when(securityUtil.getCurrentUserId()).thenReturn(1L);
        when(reviewService.getUserReviews(1L))
                .thenReturn(List.of(testReviewDTO));

        // When/Then
        mockMvc.perform(get("/api/reviews/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(1))
                .andExpect(jsonPath("$[0].restaurantName").value("Test Restaurant"));
    }

    @Test
    @WithMockUser
    void createReview_WithValidData_ShouldReturnCreated() throws Exception {
        // Given
        when(securityUtil.getCurrentUserId()).thenReturn(1L);
        when(reviewService.createReview(eq(1L), any(), eq(1L)))
                .thenReturn(testReviewDTO);

        // When/Then
        mockMvc.perform(post("/api/restaurants/1/reviews")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.comment").value("Excellent food!"));
    }

    @Test
    @WithMockUser
    void createReview_WithInvalidRating_ShouldReturnBadRequest() throws Exception {
        // Given
        ReviewCreateRequestDTO invalidRequest = new ReviewCreateRequestDTO();
        invalidRequest.setRating(6); // Invalid: max is 5
        invalidRequest.setComment("Test");

        // When/Then
        mockMvc.perform(post("/api/restaurants/1/reviews")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void createReview_WithRatingZero_ShouldReturnBadRequest() throws Exception {
        // Given
        ReviewCreateRequestDTO invalidRequest = new ReviewCreateRequestDTO();
        invalidRequest.setRating(0); // Invalid: min is 1
        invalidRequest.setComment("Test");

        // When/Then
        mockMvc.perform(post("/api/restaurants/1/reviews")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void createReview_WithNullRating_ShouldReturnBadRequest() throws Exception {
        // Given
        ReviewCreateRequestDTO invalidRequest = new ReviewCreateRequestDTO();
        invalidRequest.setComment("Test");
        // rating is null

        // When/Then
        mockMvc.perform(post("/api/restaurants/1/reviews")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createReview_WithoutAuthentication_ShouldReturnForbidden() throws Exception {
        // When/Then
        mockMvc.perform(post("/api/restaurants/1/reviews")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void updateReview_WithValidData_ShouldReturnUpdated() throws Exception {
        // Given
        when(securityUtil.getCurrentUserId()).thenReturn(1L);
        testReviewDTO.setRating(4);
        testReviewDTO.setComment("Updated: Good food");
        when(reviewService.updateReview(eq(1L), any(), eq(1L)))
                .thenReturn(testReviewDTO);

        ReviewCreateRequestDTO updateRequest = new ReviewCreateRequestDTO();
        updateRequest.setRating(4);
        updateRequest.setComment("Updated: Good food");

        // When/Then
        mockMvc.perform(put("/api/reviews/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating").value(4))
                .andExpect(jsonPath("$.comment").value("Updated: Good food"));
    }

    @Test
    @WithMockUser
    void deleteReview_ShouldReturnNoContent() throws Exception {
        // Given
        when(securityUtil.getCurrentUserId()).thenReturn(1L);

        // When/Then
        mockMvc.perform(delete("/api/reviews/1")
                .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void verifyReview_WithAdminRole_ShouldSucceed() throws Exception {
        // Given
        testReviewDTO.setIsVerified(true);
        when(reviewService.verifyReview(1L)).thenReturn(testReviewDTO);

        // When/Then
        mockMvc.perform(put("/api/admin/reviews/1/verify")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isVerified").value(true));
    }

    @Test
    @WithMockUser(roles = "USER")
    void verifyReview_WithUserRole_ShouldReturnForbidden() throws Exception {
        // When/Then
        mockMvc.perform(put("/api/admin/reviews/1/verify")
                .with(csrf()))
                .andExpect(status().isForbidden());
    }
}