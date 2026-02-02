package com.example.demo.controller;

import com.example.demo.dto.review.ReviewCreateRequestDTO;
import com.example.demo.dto.review.ReviewResponseDTO;
import com.example.demo.dto.review.ReviewStatsDTO;
import com.example.demo.security.SecurityUtil;
import com.example.demo.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ReviewController {

    private final ReviewService reviewService;
    private final SecurityUtil securityUtil;

    /**
     * PUBLIC: Get all reviews for a restaurant
     */
    @GetMapping("/restaurants/{restaurantId}/reviews")
    public ResponseEntity<List<ReviewResponseDTO>> getRestaurantReviews(
            @PathVariable Long restaurantId) {
        log.info("GET /api/restaurants/{}/reviews - fetching reviews", restaurantId);
        List<ReviewResponseDTO> reviews = reviewService.getRestaurantReviews(restaurantId);
        return ResponseEntity.ok(reviews);
    }

    /**
     * PUBLIC: Get review statistics for a restaurant
     */
    @GetMapping("/restaurants/{restaurantId}/reviews/stats")
    public ResponseEntity<ReviewStatsDTO> getRestaurantStats(
            @PathVariable Long restaurantId) {
        log.info("GET /api/restaurants/{}/reviews/stats - fetching stats", restaurantId);
        ReviewStatsDTO stats = reviewService.getRestaurantStats(restaurantId);
        return ResponseEntity.ok(stats);
    }

    /**
     * AUTHENTICATED: Get current user's review for a restaurant
     */
    @GetMapping("/restaurants/{restaurantId}/reviews/my")
    public ResponseEntity<ReviewResponseDTO> getMyReviewForRestaurant(
            @PathVariable Long restaurantId) {
        Long userId = securityUtil.getCurrentUserId();
        log.info("GET /api/restaurants/{}/reviews/my - fetching review for user: {}",
                restaurantId, userId);

        ReviewResponseDTO review = reviewService.getUserReviewForRestaurant(restaurantId, userId);

        if (review == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(review);
    }

    /**
     * AUTHENTICATED: Get all reviews by current user
     */
    @GetMapping("/reviews/my")
    public ResponseEntity<List<ReviewResponseDTO>> getMyReviews() {
        Long userId = securityUtil.getCurrentUserId();
        log.info("GET /api/reviews/my - fetching reviews for user: {}", userId);

        List<ReviewResponseDTO> reviews = reviewService.getUserReviews(userId);
        return ResponseEntity.ok(reviews);
    }

    /**
     * AUTHENTICATED: Create a new review
     */
    @PostMapping("/restaurants/{restaurantId}/reviews")
    public ResponseEntity<ReviewResponseDTO> createReview(
            @PathVariable Long restaurantId,
            @Valid @RequestBody ReviewCreateRequestDTO request) {

        Long userId = securityUtil.getCurrentUserId();
        log.info("POST /api/restaurants/{}/reviews - creating review for user: {}",
                restaurantId, userId);

        ReviewResponseDTO review = reviewService.createReview(restaurantId, request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(review);
    }

    /**
     * AUTHENTICATED: Update user's own review
     */
    @PutMapping("/reviews/{reviewId}")
    public ResponseEntity<ReviewResponseDTO> updateReview(
            @PathVariable Long reviewId,
            @Valid @RequestBody ReviewCreateRequestDTO request) {

        Long userId = securityUtil.getCurrentUserId();
        log.info("PUT /api/reviews/{} - updating review for user: {}", reviewId, userId);

        ReviewResponseDTO review = reviewService.updateReview(reviewId, request, userId);
        return ResponseEntity.ok(review);
    }

    /**
     * AUTHENTICATED: Delete user's own review
     */
    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long reviewId) {
        Long userId = securityUtil.getCurrentUserId();
        log.info("DELETE /api/reviews/{} - deleting review for user: {}", reviewId, userId);

        reviewService.deleteReview(reviewId, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * ADMIN: Verify a review
     */
    @PutMapping("/admin/reviews/{reviewId}/verify")
    public ResponseEntity<ReviewResponseDTO> verifyReview(@PathVariable Long reviewId) {
        log.info("PUT /api/admin/reviews/{}/verify - verifying review", reviewId);

        ReviewResponseDTO review = reviewService.verifyReview(reviewId);
        return ResponseEntity.ok(review);
    }
}