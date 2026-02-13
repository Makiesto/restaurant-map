package com.example.demo.service;

import com.example.demo.dto.review.ReviewCreateRequestDTO;
import com.example.demo.dto.review.ReviewResponseDTO;
import com.example.demo.dto.review.ReviewStatsDTO;
import com.example.demo.entity.Restaurant;
import com.example.demo.entity.Review;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.exception.ValidationException;
import com.example.demo.repository.RestaurantRepository;
import com.example.demo.repository.ReviewRepository;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ReviewService reviewService;

    private User testUser;
    private Restaurant testRestaurant;
    private Review testReview;
    private ReviewCreateRequestDTO createRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .role(Role.USER)
                .build();

        testRestaurant = Restaurant.builder()
                .id(1L)
                .name("Test Restaurant")
                .rating(4.5)
                .build();

        testReview = Review.builder()
                .id(1L)
                .restaurant(testRestaurant)
                .user(testUser)
                .rating(5)
                .comment("Excellent food!")
                .isVerified(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        createRequest = new ReviewCreateRequestDTO();
        createRequest.setRating(5);
        createRequest.setComment("Great experience!");
    }

    @Test
    void createReview_WithValidData_ShouldSucceed() {
        // Given
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(testRestaurant));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(reviewRepository.existsByRestaurantIdAndUserId(1L, 1L)).thenReturn(false);
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> {
            Review review = invocation.getArgument(0);
            review.setId(1L);
            return review;
        });
        when(reviewRepository.getAverageRatingByRestaurantId(1L)).thenReturn(4.8);

        // When
        ReviewResponseDTO result = reviewService.createReview(1L, createRequest, 1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getRating()).isEqualTo(5);
        assertThat(result.getComment()).isEqualTo("Great experience!");
        assertThat(result.getRestaurantId()).isEqualTo(1L);
        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getIsVerified()).isFalse();

        verify(reviewRepository).save(any(Review.class));
        verify(restaurantRepository).save(argThat(restaurant ->
            restaurant.getRating().equals(4.8)
        ));
    }

    @Test
    void createReview_WhenRestaurantNotFound_ShouldThrowException() {
        // Given
        when(restaurantRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> reviewService.createReview(999L, createRequest, 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Restaurant not found");

        verify(reviewRepository, never()).save(any());
    }

    @Test
    void createReview_WhenUserNotFound_ShouldThrowException() {
        // Given
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(testRestaurant));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> reviewService.createReview(1L, createRequest, 999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");

        verify(reviewRepository, never()).save(any());
    }

    @Test
    void createReview_WhenUserAlreadyReviewed_ShouldThrowException() {
        // Given
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(testRestaurant));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(reviewRepository.existsByRestaurantIdAndUserId(1L, 1L)).thenReturn(true);

        // When/Then
        assertThatThrownBy(() -> reviewService.createReview(1L, createRequest, 1L))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("You have already reviewed this restaurant");

        verify(reviewRepository, never()).save(any());
    }

    @Test
    void updateReview_ByOwner_ShouldSucceed() {
        // Given
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview));
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(testRestaurant));
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(reviewRepository.getAverageRatingByRestaurantId(1L)).thenReturn(4.7);

        ReviewCreateRequestDTO updateRequest = new ReviewCreateRequestDTO();
        updateRequest.setRating(4);
        updateRequest.setComment("Updated: Good food");

        // When
        ReviewResponseDTO result = reviewService.updateReview(1L, updateRequest, 1L);

        // Then
        assertThat(result.getRating()).isEqualTo(4);
        assertThat(result.getComment()).isEqualTo("Updated: Good food");

        verify(reviewRepository).save(argThat(review ->
                review.getRating().equals(4) &&
                review.getComment().equals("Updated: Good food")
        ));
    }

    @Test
    void updateReview_ByNonOwner_ShouldThrowException() {
        // Given
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview));

        // When/Then
        assertThatThrownBy(() -> reviewService.updateReview(1L, createRequest, 999L))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("You can only update your own reviews");

        verify(reviewRepository, never()).save(any());
    }

    @Test
    void deleteReview_ByOwner_ShouldSucceed() {
        // Given
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview));
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(testRestaurant));
        when(reviewRepository.getAverageRatingByRestaurantId(1L)).thenReturn(4.0);

        // When
        reviewService.deleteReview(1L, 1L);

        // Then
        verify(reviewRepository).delete(testReview);
        verify(restaurantRepository).save(argThat(restaurant ->
            restaurant.getRating().equals(4.0)
        ));
    }

    @Test
    void deleteReview_ByNonOwner_ShouldThrowException() {
        // Given
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview));

        // When/Then
        assertThatThrownBy(() -> reviewService.deleteReview(1L, 999L))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("You can only delete your own reviews");

        verify(reviewRepository, never()).delete(any());
    }

    @Test
    void getRestaurantReviews_ShouldReturnAllReviews() {
        // Given
        Review review2 = Review.builder()
                .id(2L)
                .restaurant(testRestaurant)
                .user(testUser)
                .rating(4)
                .comment("Good")
                .createdAt(LocalDateTime.now())
                .build();

        when(reviewRepository.findByRestaurantIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(testReview, review2));

        // When
        List<ReviewResponseDTO> results = reviewService.getRestaurantReviews(1L);

        // Then
        assertThat(results).hasSize(2);
        assertThat(results).extracting(ReviewResponseDTO::getRating)
                .containsExactly(5, 4);
    }

    @Test
    void getUserReviews_ShouldReturnUserReviews() {
        // Given
        when(reviewRepository.findByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(testReview));

        // When
        List<ReviewResponseDTO> results = reviewService.getUserReviews(1L);

        // Then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getUserId()).isEqualTo(1L);
    }

    @Test
    void getRestaurantStats_ShouldCalculateCorrectly() {
        // Given
        List<Review> reviews = List.of(
                createReview(5, "Excellent"),
                createReview(5, "Great"),
                createReview(4, "Good"),
                createReview(4, "Nice"),
                createReview(3, "Okay"),
                createReview(2, "Meh"),
                createReview(1, "Bad")
        );

        when(reviewRepository.findByRestaurantIdOrderByCreatedAtDesc(1L))
                .thenReturn(reviews);

        // When
        ReviewStatsDTO stats = reviewService.getRestaurantStats(1L);

        // Then
        assertThat(stats.getTotalReviews()).isEqualTo(7);
        assertThat(stats.getAverageRating()).isEqualTo((5+5+4+4+3+2+1)/7.0);
        assertThat(stats.getFiveStars()).isEqualTo(2);
        assertThat(stats.getFourStars()).isEqualTo(2);
        assertThat(stats.getThreeStars()).isEqualTo(1);
        assertThat(stats.getTwoStars()).isEqualTo(1);
        assertThat(stats.getOneStar()).isEqualTo(1);
    }

    @Test
    void getRestaurantStats_WithNoReviews_ShouldReturnZeros() {
        // Given
        when(reviewRepository.findByRestaurantIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of());

        // When
        ReviewStatsDTO stats = reviewService.getRestaurantStats(1L);

        // Then
        assertThat(stats.getTotalReviews()).isEqualTo(0);
        assertThat(stats.getAverageRating()).isEqualTo(0.0);
        assertThat(stats.getFiveStars()).isEqualTo(0);
    }

    @Test
    void getUserReviewForRestaurant_WhenExists_ShouldReturnReview() {
        // Given
        when(reviewRepository.findByRestaurantIdAndUserId(1L, 1L))
                .thenReturn(Optional.of(testReview));

        // When
        ReviewResponseDTO result = reviewService.getUserReviewForRestaurant(1L, 1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getRestaurantId()).isEqualTo(1L);
        assertThat(result.getUserId()).isEqualTo(1L);
    }

    @Test
    void getUserReviewForRestaurant_WhenNotExists_ShouldReturnNull() {
        // Given
        when(reviewRepository.findByRestaurantIdAndUserId(1L, 999L))
                .thenReturn(Optional.empty());

        // When
        ReviewResponseDTO result = reviewService.getUserReviewForRestaurant(1L, 999L);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void verifyReview_ShouldSetVerifiedToTrue() {
        // Given
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview));
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        ReviewResponseDTO result = reviewService.verifyReview(1L);

        // Then
        assertThat(result.getIsVerified()).isTrue();

        verify(reviewRepository).save(argThat(review ->
            review.getIsVerified().equals(true)
        ));
    }

    private Review createReview(int rating, String comment) {
        return Review.builder()
                .restaurant(testRestaurant)
                .user(testUser)
                .rating(rating)
                .comment(comment)
                .createdAt(LocalDateTime.now())
                .build();
    }
}