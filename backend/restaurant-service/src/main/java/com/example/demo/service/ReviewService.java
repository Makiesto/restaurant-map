package com.example.demo.service;

import com.example.demo.dto.review.ReviewCreateRequestDTO;
import com.example.demo.dto.review.ReviewResponseDTO;
import com.example.demo.dto.review.ReviewStatsDTO;
import com.example.demo.entity.Restaurant;
import com.example.demo.entity.Review;
import com.example.demo.entity.User;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.exception.ValidationException;
import com.example.demo.repository.RestaurantRepository;
import com.example.demo.repository.ReviewRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;

    @Transactional
    public ReviewResponseDTO createReview(Long restaurantId, ReviewCreateRequestDTO request, Long userId) {
        log.info("Creating review for restaurant: {} by user: {}", restaurantId, userId);

        // Check if restaurant exists
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with ID: " + restaurantId));

        // Check if user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        // Check if user already reviewed this restaurant
        if (reviewRepository.existsByRestaurantIdAndUserId(restaurantId, userId)) {
            throw new ValidationException("You have already reviewed this restaurant. Please update your existing review.");
        }

        Review review = Review.builder()
                .restaurant(restaurant)
                .user(user)
                .rating(request.getRating())
                .comment(request.getComment())
                .isVerified(false)
                .build();

        Review savedReview = reviewRepository.save(review);

        // Update restaurant rating
        updateRestaurantRating(restaurantId);

        log.info("Review created with ID: {}", savedReview.getId());
        return mapToResponse(savedReview);
    }

    @Transactional
    public ReviewResponseDTO updateReview(Long reviewId, ReviewCreateRequestDTO request, Long userId) {
        log.info("Updating review: {} by user: {}", reviewId, userId);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with ID: " + reviewId));

        // Check if user is the owner of the review
        if (!review.getUser().getId().equals(userId)) {
            throw new ValidationException("You can only update your own reviews");
        }

        review.setRating(request.getRating());
        review.setComment(request.getComment());

        Review savedReview = reviewRepository.save(review);

        // Update restaurant rating
        updateRestaurantRating(review.getRestaurant().getId());

        log.info("Review updated: {}", reviewId);
        return mapToResponse(savedReview);
    }

    @Transactional
    public void deleteReview(Long reviewId, Long userId) {
        log.info("Deleting review: {} by user: {}", reviewId, userId);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with ID: " + reviewId));

        // Check if user is the owner of the review
        if (!review.getUser().getId().equals(userId)) {
            throw new ValidationException("You can only delete your own reviews");
        }

        Long restaurantId = review.getRestaurant().getId();
        reviewRepository.delete(review);

        // Update restaurant rating
        updateRestaurantRating(restaurantId);

        log.info("Review deleted: {}", reviewId);
    }

    @Transactional(readOnly = true)
    public List<ReviewResponseDTO> getRestaurantReviews(Long restaurantId) {
        return reviewRepository.findByRestaurantIdOrderByCreatedAtDesc(restaurantId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ReviewResponseDTO> getUserReviews(Long userId) {
        return reviewRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ReviewStatsDTO getRestaurantStats(Long restaurantId) {
        List<Review> reviews = reviewRepository.findByRestaurantIdOrderByCreatedAtDesc(restaurantId);

        long totalReviews = reviews.size();
        double averageRating = totalReviews > 0
                ? reviews.stream().mapToInt(Review::getRating).average().orElse(0.0)
                : 0.0;

        long fiveStars = reviews.stream().filter(r -> r.getRating() == 5).count();
        long fourStars = reviews.stream().filter(r -> r.getRating() == 4).count();
        long threeStars = reviews.stream().filter(r -> r.getRating() == 3).count();
        long twoStars = reviews.stream().filter(r -> r.getRating() == 2).count();
        long oneStar = reviews.stream().filter(r -> r.getRating() == 1).count();

        return ReviewStatsDTO.builder()
                .averageRating(averageRating)
                .totalReviews(totalReviews)
                .fiveStars(fiveStars)
                .fourStars(fourStars)
                .threeStars(threeStars)
                .twoStars(twoStars)
                .oneStar(oneStar)
                .build();
    }

    @Transactional(readOnly = true)
    public ReviewResponseDTO getUserReviewForRestaurant(Long restaurantId, Long userId) {
        return reviewRepository.findByRestaurantIdAndUserId(restaurantId, userId)
                .map(this::mapToResponse)
                .orElse(null);
    }

    @Transactional
    public ReviewResponseDTO verifyReview(Long reviewId) {
        log.info("Verifying review: {}", reviewId);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with ID: " + reviewId));

        review.setIsVerified(true);
        Review savedReview = reviewRepository.save(review);

        log.info("Review verified: {}", reviewId);
        return mapToResponse(savedReview);
    }

    private void updateRestaurantRating(Long restaurantId) {
        Double averageRating = reviewRepository.getAverageRatingByRestaurantId(restaurantId);
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));

        restaurant.setRating(averageRating);
        restaurantRepository.save(restaurant);

        log.info("Updated restaurant {} rating to: {}", restaurantId, averageRating);
    }

    private ReviewResponseDTO mapToResponse(Review review) {
        return ReviewResponseDTO.builder()
                .id(review.getId())
                .restaurantId(review.getRestaurant().getId())
                .restaurantName(review.getRestaurant().getName())
                .userId(review.getUser().getId())
                .userName(review.getUser().getFirstName() + " " + review.getUser().getLastName())
                .rating(review.getRating())
                .comment(review.getComment())
                .isVerified(review.getIsVerified())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}