package com.example.demo.repository;

import com.example.demo.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@TestPropertySource(locations = "classpath:application-test.properties")
class RepositoryIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private AllergenRepository allergenRepository;

    private User testUser;
    private Restaurant testRestaurant;
    private Allergen glutenAllergen;

    @BeforeEach
    void setUp() {
        // Create test user
        testUser = User.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .password("password")
                .role(Role.USER)
                .isActive(true)
                .emailVerified(false)
                .allergens(new HashSet<>())
                .build();
        testUser = userRepository.save(testUser);

        // Create test restaurant
        testRestaurant = Restaurant.builder()
                .name("Test Restaurant")
                .address("123 Test St")
                .latitude(50.0)
                .longitude(19.0)
                .owner(testUser)
                .status(RestaurantStatus.APPROVED)
                .isVerified(false)
                .dietaryOptions(new HashSet<>())
                .build();
        testRestaurant = restaurantRepository.save(testRestaurant);

        // Create test allergen
        glutenAllergen = Allergen.builder()
                .name("Gluten")
                .severityLevel("High")
                .build();
        glutenAllergen = allergenRepository.save(glutenAllergen);
    }

    // User Repository Tests
    @Test
    void userRepository_findByEmail_ShouldReturnUser() {
        // When
        Optional<User> found = userRepository.findByEmail("john@example.com");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getFirstName()).isEqualTo("John");
    }

    @Test
    void userRepository_findByEmail_WithNonExistentEmail_ShouldReturnEmpty() {
        // When
        Optional<User> found = userRepository.findByEmail("nonexistent@example.com");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void userRepository_existsByEmail_WithExistingEmail_ShouldReturnTrue() {
        // When
        boolean exists = userRepository.existsByEmail("john@example.com");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void userRepository_existsByEmail_WithNonExistentEmail_ShouldReturnFalse() {
        // When
        boolean exists = userRepository.existsByEmail("nonexistent@example.com");

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    void userRepository_findByIdWithAllergens_ShouldEagerlyLoadAllergens() {
        // Given - Add allergen to user
        testUser.getAllergens().add(glutenAllergen);
        userRepository.save(testUser);

        // When
        Optional<User> found = userRepository.findByIdWithAllergens(testUser.getId());

        // Then
        assertThat(found).isPresent();
        // This should not cause LazyInitializationException
        assertThat(found.get().getAllergens()).hasSize(1);
        assertThat(found.get().getAllergens().iterator().next().getName()).isEqualTo("Gluten");
    }

    // Restaurant Repository Tests
    @Test
    void restaurantRepository_findByStatus_ShouldReturnRestaurantsWithStatus() {
        // Given - Create another restaurant with different status
        Restaurant pendingRestaurant = Restaurant.builder()
                .name("Pending Restaurant")
                .address("456 Pending St")
                .latitude(51.0)
                .longitude(20.0)
                .owner(testUser)
                .status(RestaurantStatus.PENDING)
                .isVerified(false)
                .dietaryOptions(new HashSet<>())
                .build();
        restaurantRepository.save(pendingRestaurant);

        // When
        List<Restaurant> approved = restaurantRepository.findByStatus(RestaurantStatus.APPROVED);
        List<Restaurant> pending = restaurantRepository.findByStatus(RestaurantStatus.PENDING);

        // Then
        assertThat(approved).hasSize(1);
        assertThat(approved.get(0).getName()).isEqualTo("Test Restaurant");
        assertThat(pending).hasSize(1);
        assertThat(pending.get(0).getName()).isEqualTo("Pending Restaurant");
    }

    @Test
    void restaurantRepository_findByOwnerId_ShouldReturnOwnerRestaurants() {
        // Given - Create another restaurant for same owner
        Restaurant restaurant2 = Restaurant.builder()
                .name("Second Restaurant")
                .address("789 Second St")
                .owner(testUser)
                .status(RestaurantStatus.APPROVED)
                .dietaryOptions(new HashSet<>())
                .build();
        restaurantRepository.save(restaurant2);


        // When
        List<Restaurant> restaurants = restaurantRepository.findByOwnerId(testUser.getId());

        // Then
        assertThat(restaurants).hasSize(2);
        assertThat(restaurants).extracting(Restaurant::getName)
                .containsExactlyInAnyOrder("Test Restaurant", "Second Restaurant");
    }

    @Test
    void restaurantRepository_findByIsVerified_ShouldReturnVerifiedRestaurants() {
        // Given - Create verified restaurant
        Restaurant verifiedRestaurant = Restaurant.builder()
                .name("Verified Restaurant")
                .address("999 Verified St")
                .owner(testUser)
                .status(RestaurantStatus.APPROVED)
                .isVerified(true)
                .verifiedAt(LocalDateTime.now())
                .dietaryOptions(new HashSet<>())
                .build();
        restaurantRepository.save(verifiedRestaurant);


        // When
        List<Restaurant> verified = restaurantRepository.findByIsVerified(true);
        List<Restaurant> unverified = restaurantRepository.findByIsVerified(false);

        // Then
        assertThat(verified).hasSize(1);
        assertThat(verified.get(0).getName()).isEqualTo("Verified Restaurant");
        assertThat(unverified).hasSize(1);
        assertThat(unverified.get(0).getName()).isEqualTo("Test Restaurant");
    }

    @Test
    void restaurantRepository_countByStatus_ShouldReturnCorrectCounts() {
        // Given
        Restaurant pending = Restaurant.builder()
                .name("Pending")
                .address("123 St")
                .owner(testUser)
                .status(RestaurantStatus.PENDING)
                .dietaryOptions(new HashSet<>())
                .build();
        Restaurant rejected = Restaurant.builder()
                .name("Rejected")
                .address("456 St")
                .owner(testUser)
                .status(RestaurantStatus.REJECTED)
                .dietaryOptions(new HashSet<>())
                .build();
        restaurantRepository.save(pending);
        restaurantRepository.save(rejected);


        // When
        Long approvedCount = restaurantRepository.countByStatus(RestaurantStatus.APPROVED);
        Long pendingCount = restaurantRepository.countByStatus(RestaurantStatus.PENDING);
        Long rejectedCount = restaurantRepository.countByStatus(RestaurantStatus.REJECTED);

        // Then
        assertThat(approvedCount).isEqualTo(1L);
        assertThat(pendingCount).isEqualTo(1L);
        assertThat(rejectedCount).isEqualTo(1L);
    }

    @Test
    void restaurantRepository_countByIsVerified_ShouldReturnCorrectCounts() {
        // Given
        Restaurant verified = Restaurant.builder()
                .name("Verified")
                .address("123 St")
                .owner(testUser)
                .status(RestaurantStatus.APPROVED)
                .isVerified(true)
                .dietaryOptions(new HashSet<>())
                .build();
        restaurantRepository.save(verified);


        // When
        Long verifiedCount = restaurantRepository.countByIsVerified(true);
        Long unverifiedCount = restaurantRepository.countByIsVerified(false);

        // Then
        assertThat(verifiedCount).isEqualTo(1L);
        assertThat(unverifiedCount).isEqualTo(1L);
    }

    // Review Repository Tests
    @Test
    void reviewRepository_findByRestaurantIdOrderByCreatedAtDesc_ShouldReturnOrderedReviews() {
        // Given
        Review review1 = Review.builder()
                .restaurant(testRestaurant)
                .user(testUser)
                .rating(5)
                .comment("Great!")
                .build();

        reviewRepository.save(review1);


        // Wait a bit to ensure different timestamps
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        Review review2 = Review.builder()
                .restaurant(testRestaurant)
                .user(testUser)
                .rating(4)
                .comment("Good")
                .build();
        reviewRepository.save(review2);


        // When
        List<Review> reviews = reviewRepository.findByRestaurantIdOrderByCreatedAtDesc(
                testRestaurant.getId());

        // Then
        assertThat(reviews).hasSize(2);
        // Most recent should be first
        assertThat(reviews.get(0).getCreatedAt())
                .isAfterOrEqualTo(reviews.get(1).getCreatedAt());
    }

    @Test
    void reviewRepository_findByUserIdOrderByCreatedAtDesc_ShouldReturnUserReviews() {
        // Given
        Review review = Review.builder()
                .restaurant(testRestaurant)
                .user(testUser)
                .rating(5)
                .comment("Excellent!")
                .build();
        reviewRepository.save(review);


        // When
        List<Review> reviews = reviewRepository.findByUserIdOrderByCreatedAtDesc(testUser.getId());

        // Then
        assertThat(reviews).hasSize(1);
        assertThat(reviews.get(0).getComment()).isEqualTo("Excellent!");
    }

    @Test
    void reviewRepository_findByRestaurantIdAndUserId_ShouldReturnReview() {
        // Given
        Review review = Review.builder()
                .restaurant(testRestaurant)
                .user(testUser)
                .rating(5)
                .comment("Great!")
                .build();
        reviewRepository.save(review);


        // When
        Optional<Review> found = reviewRepository.findByRestaurantIdAndUserId(
                testRestaurant.getId(), testUser.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getComment()).isEqualTo("Great!");
    }

    @Test
    void reviewRepository_existsByRestaurantIdAndUserId_ShouldReturnTrue() {
        // Given
        Review review = Review.builder()
                .restaurant(testRestaurant)
                .user(testUser)
                .rating(5)
                .build();
        reviewRepository.save(review);


        // When
        boolean exists = reviewRepository.existsByRestaurantIdAndUserId(
                testRestaurant.getId(), testUser.getId());

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void reviewRepository_getAverageRatingByRestaurantId_ShouldCalculateCorrectly() {
        // Given
        Review review1 = Review.builder()
                .restaurant(testRestaurant)
                .user(testUser)
                .rating(5)
                .build();
        Review review2 = Review.builder()
                .restaurant(testRestaurant)
                .user(testUser)
                .rating(3)
                .build();
        reviewRepository.save(review1);
        reviewRepository.save(review2);


        // When
        Double average = reviewRepository.getAverageRatingByRestaurantId(testRestaurant.getId());

        // Then
        assertThat(average).isEqualTo(4.0); // (5 + 3) / 2
    }

    @Test
    void reviewRepository_countByIsVerified_ShouldReturnCorrectCount() {
        // Given
        Review verified = Review.builder()
                .restaurant(testRestaurant)
                .user(testUser)
                .rating(5)
                .isVerified(true)
                .build();
        Review unverified = Review.builder()
                .restaurant(testRestaurant)
                .user(testUser)
                .rating(4)
                .isVerified(false)
                .build();
        reviewRepository.save(verified);
        reviewRepository.save(unverified);


        // When
        Long verifiedCount = reviewRepository.countByIsVerified(true);
        Long unverifiedCount = reviewRepository.countByIsVerified(false);

        // Then
        assertThat(verifiedCount).isEqualTo(1L);
        assertThat(unverifiedCount).isEqualTo(1L);
    }

    // Allergen Repository Tests
    @Test
    void allergenRepository_findByName_ShouldReturnAllergen() {
        // When
        Optional<Allergen> found = allergenRepository.findByName("Gluten");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getSeverityLevel()).isEqualTo("High");
    }

    @Test
    void allergenRepository_findByName_WithNonExistentName_ShouldReturnEmpty() {
        // When
        Optional<Allergen> found = allergenRepository.findByName("NonExistent");

        // Then
        assertThat(found).isEmpty();
    }
}