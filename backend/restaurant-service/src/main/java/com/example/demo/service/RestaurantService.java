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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;
    private final GeocodingService geocodingService;

    @Transactional
    public RestaurantResponseDTO createRestaurant(RestaurantCreateRequestDTO request, Long ownerId) {
        log.info("Creating restaurant: {} for user: {}", request.getName(), ownerId);

        // Verify user exists and is verified
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + ownerId));

        if (owner.getRole() != Role.VERIFIED_USER && owner.getRole() != Role.ADMIN) {
            throw new UnauthorizedException("Only verified users can create restaurants");
        }

        // TODO
//        GeocodingService.GeocodingResult coordinates =
//                geocodingService.geocodeAddress(request.getAddress());

        Double latitude = 50.061698;
        Double longitude = 19.937206;

        Restaurant restaurant = Restaurant.builder()
                .name(request.getName())
                .address(request.getAddress())
                .latitude(latitude)
                .longitude(longitude)
//                .latitude(coordinates.getLatitude())
//                .longitude(coordinates.getLongitude())
                .phone(request.getPhone())
                .description(request.getDescription())
                .openingHours(request.getOpeningHours())
                .owner(owner)
                .status(RestaurantStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Restaurant savedRestaurant = restaurantRepository.save(restaurant);
        log.info("Restaurant created with ID: {} and status: PENDING", savedRestaurant.getId());

        return mapToResponse(savedRestaurant);
    }

    @Transactional(readOnly = true)
    public RestaurantResponseDTO getRestaurantById(Long id) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with ID: " + id));
        return mapToResponse(restaurant);
    }

    @Transactional(readOnly = true)
    public List<RestaurantResponseDTO> getAllApprovedRestaurants() {
        return restaurantRepository.findByStatus(RestaurantStatus.APPROVED).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RestaurantResponseDTO> getPendingRestaurants() {
        return restaurantRepository.findByStatus(RestaurantStatus.PENDING).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RestaurantResponseDTO> getRestaurantsByOwner(Long ownerId) {
        return restaurantRepository.findByOwnerId(ownerId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public RestaurantResponseDTO approveRestaurant(Long restaurantId) {
        log.info("Approving restaurant with ID: {}", restaurantId);

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with ID: " + restaurantId));

        restaurant.setStatus(RestaurantStatus.APPROVED);
        restaurant.setUpdatedAt(LocalDateTime.now());

        Restaurant savedRestaurant = restaurantRepository.save(restaurant);
        log.info("Restaurant approved: {}", restaurantId);

        return mapToResponse(savedRestaurant);
    }

    @Transactional
    public RestaurantResponseDTO rejectRestaurant(Long restaurantId) {
        log.info("Rejecting restaurant with ID: {}", restaurantId);

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with ID: " + restaurantId));

        restaurant.setStatus(RestaurantStatus.REJECTED);
        restaurant.setUpdatedAt(LocalDateTime.now());

        Restaurant savedRestaurant = restaurantRepository.save(restaurant);
        log.info("Restaurant rejected: {}", restaurantId);

        return mapToResponse(savedRestaurant);
    }

    @Transactional
    public RestaurantResponseDTO updateRestaurant(Long restaurantId, RestaurantCreateRequestDTO request, Long userId) {
        log.info("Updating restaurant: {} by user: {}", restaurantId, userId);

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with ID: " + restaurantId));

        // Check if user is the owner
        if (!restaurant.getOwner().getId().equals(userId)) {
            throw new UnauthorizedException("You can only update your own restaurants");
        }

        GeocodingService.GeocodingResult coordinates =
                geocodingService.geocodeAddress(request.getAddress());

        restaurant.setName(request.getName());
        restaurant.setAddress(request.getAddress());
        restaurant.setLatitude(coordinates.getLatitude());
        restaurant.setLongitude(coordinates.getLongitude());
        restaurant.setPhone(request.getPhone());
        restaurant.setDescription(request.getDescription());
        restaurant.setOpeningHours(request.getOpeningHours());
        restaurant.setUpdatedAt(LocalDateTime.now());

        Restaurant savedRestaurant = restaurantRepository.save(restaurant);
        log.info("Restaurant updated: {}", restaurantId);

        return mapToResponse(savedRestaurant);
    }

    @Transactional
    public void deleteRestaurant(Long restaurantId, Long userId) {
        log.info("Deleting restaurant: {} by user: {}", restaurantId, userId);

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with ID: " + restaurantId));

        // Check if user is the owner
        if (!restaurant.getOwner().getId().equals(userId)) {
            throw new UnauthorizedException("You can only delete your own restaurants");
        }

        restaurantRepository.deleteById(restaurantId);
        log.info("Restaurant deleted: {}", restaurantId);
    }

    private RestaurantResponseDTO mapToResponse(Restaurant restaurant) {
        return RestaurantResponseDTO.builder()
                .id(restaurant.getId())
                .name(restaurant.getName())
                .address(restaurant.getAddress())
                .latitude(restaurant.getLatitude())
                .longitude(restaurant.getLongitude())
                .phone(restaurant.getPhone())
                .description(restaurant.getDescription())
                .openingHours(restaurant.getOpeningHours())
                .rating(restaurant.getRating())
                .status(restaurant.getStatus())
                .ownerId(restaurant.getOwner().getId())
                .ownerName(restaurant.getOwner().getFirstName() + " " + restaurant.getOwner().getLastName())
                .createdAt(restaurant.getCreatedAt())
                .updatedAt(restaurant.getUpdatedAt())
                .build();
    }
}