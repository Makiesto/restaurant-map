package com.example.demo.service;

import com.example.demo.dto.restaurant.RestaurantCreateRequestDTO;
import com.example.demo.dto.restaurant.RestaurantResponseDTO;
import com.example.demo.entity.Restaurant;
import com.example.demo.entity.RestaurantStatus;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.exception.UnauthorizedException;
import com.example.demo.repository.RestaurantRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.SecurityUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RestaurantServiceTest {

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GeocodingService geocodingService;

    @Mock
    private SecurityUtil securityUtil;

    @InjectMocks
    private RestaurantService restaurantService;

    private User testUser;
    private Restaurant testRestaurant;
    private RestaurantCreateRequestDTO createRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .role(Role.VERIFIED_USER)
                .build();

        testRestaurant = Restaurant.builder()
                .id(1L)
                .name("Test Restaurant")
                .address("123 Test St")
                .latitude(50.061698)
                .longitude(19.937206)
                .owner(testUser)
                .status(RestaurantStatus.PENDING)
                .isVerified(false)
                .dietaryOptions(new HashSet<>())  // Initialize to avoid NPE
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        createRequest = new RestaurantCreateRequestDTO();
        createRequest.setName("Test Restaurant");
        createRequest.setAddress("123 Test St");
        createRequest.setPhone("+48123456789");
        createRequest.setDescription("A test restaurant");
    }

    @Test
    void createRestaurant_WithVerifiedUser_ShouldSucceed() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(restaurantRepository.save(any(Restaurant.class))).thenReturn(testRestaurant);

        // When
        RestaurantResponseDTO result = restaurantService.createRestaurant(createRequest, 1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Test Restaurant");
        assertThat(result.getStatus()).isEqualTo(RestaurantStatus.PENDING);
        assertThat(result.isVerified()).isFalse();
        verify(restaurantRepository).save(any(Restaurant.class));
    }

    @Test
    void createRestaurant_WithUnverifiedUser_ShouldThrowException() {
        // Given
        testUser.setRole(Role.USER);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When/Then
        assertThatThrownBy(() -> restaurantService.createRestaurant(createRequest, 1L))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Only verified users and admins can create restaurants");

        verify(restaurantRepository, never()).save(any());
    }

    @Test
    void createRestaurant_WithNonExistentUser_ShouldThrowException() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> restaurantService.createRestaurant(createRequest, 999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void getRestaurantById_WhenExists_ShouldReturnRestaurant() {
        // Given
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(testRestaurant));

        // When
        RestaurantResponseDTO result = restaurantService.getRestaurantById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Test Restaurant");
    }

    @Test
    void getRestaurantById_WhenNotExists_ShouldThrowException() {
        // Given
        when(restaurantRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> restaurantService.getRestaurantById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Restaurant not found");
    }

    @Test
    void getAllApprovedRestaurants_ShouldReturnOnlyApprovedRestaurants() {
        // Given
        Restaurant approved1 = testRestaurant;
        approved1.setStatus(RestaurantStatus.APPROVED);

        Restaurant approved2 = Restaurant.builder()
                .id(2L)
                .name("Second Restaurant")
                .status(RestaurantStatus.APPROVED)
                .owner(testUser)
                .build();

        when(restaurantRepository.findByStatus(RestaurantStatus.APPROVED))
                .thenReturn(List.of(approved1, approved2));

        // When
        List<RestaurantResponseDTO> results = restaurantService.getAllApprovedRestaurants();

        // Then
        assertThat(results).hasSize(2);
        assertThat(results).allMatch(r -> r.getStatus() == RestaurantStatus.APPROVED);
    }

    @Test
    void approveRestaurant_WhenPending_ShouldChangeStatusToApproved() {
        // Given
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(testRestaurant));
        when(restaurantRepository.save(any(Restaurant.class))).thenAnswer(i -> i.getArgument(0));

        // When
        RestaurantResponseDTO result = restaurantService.approveRestaurant(1L);

        // Then
        assertThat(result.getStatus()).isEqualTo(RestaurantStatus.APPROVED);
        verify(restaurantRepository).save(argThat(r ->
            r.getStatus() == RestaurantStatus.APPROVED
        ));
    }

    @Test
    void rejectRestaurant_WhenPending_ShouldChangeStatusToRejected() {
        // Given
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(testRestaurant));
        when(restaurantRepository.save(any(Restaurant.class))).thenAnswer(i -> i.getArgument(0));

        // When
        RestaurantResponseDTO result = restaurantService.rejectRestaurant(1L);

        // Then
        assertThat(result.getStatus()).isEqualTo(RestaurantStatus.REJECTED);
    }

    @Test
    void updateRestaurant_ByOwner_ShouldSucceed() {
        // Given
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(testRestaurant));
        when(restaurantRepository.save(any(Restaurant.class))).thenAnswer(i -> i.getArgument(0));

        RestaurantCreateRequestDTO updateRequest = new RestaurantCreateRequestDTO();
        updateRequest.setName("Updated Restaurant");
        updateRequest.setAddress("456 New St");

        // When
        RestaurantResponseDTO result = restaurantService.updateRestaurant(1L, updateRequest, 1L);

        // Then
        assertThat(result.getName()).isEqualTo("Updated Restaurant");
        assertThat(result.getAddress()).isEqualTo("456 New St");
    }

    @Test
    void updateRestaurant_ByNonOwner_ShouldThrowException() {
        // Given
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(testRestaurant));

        // When/Then
        assertThatThrownBy(() -> restaurantService.updateRestaurant(1L, createRequest, 999L))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("You can only update your own restaurants");
    }

    @Test
    void deleteRestaurant_ByOwner_ShouldSucceed() {
        // Given
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(testRestaurant));

        // When
        restaurantService.deleteRestaurant(1L, 1L);

        // Then
        verify(restaurantRepository).deleteById(1L);
    }

    @Test
    void deleteRestaurant_ByNonOwner_ShouldThrowException() {
        // Given
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(testRestaurant));

        // When/Then
        assertThatThrownBy(() -> restaurantService.deleteRestaurant(1L, 999L))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("You can only delete your own restaurants");

        verify(restaurantRepository, never()).deleteById(any());
    }

    @Test
    void getRestaurantsByOwner_ShouldReturnOwnerRestaurants() {
        // Given
        Restaurant restaurant2 = Restaurant.builder()
                .id(2L)
                .name("Second Restaurant")
                .owner(testUser)
                .status(RestaurantStatus.APPROVED)
                .build();

        when(restaurantRepository.findByOwnerId(1L))
                .thenReturn(List.of(testRestaurant, restaurant2));

        // When
        List<RestaurantResponseDTO> results = restaurantService.getRestaurantsByOwner(1L);

        // Then
        assertThat(results).hasSize(2);
        assertThat(results).allMatch(r -> r.getOwner().getId().equals(1L));
    }
}