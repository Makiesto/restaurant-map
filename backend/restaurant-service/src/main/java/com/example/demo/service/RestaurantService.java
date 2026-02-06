package com.example.demo.service;

import com.example.demo.dto.admin.VerifyRestaurantRequestDTO;
import com.example.demo.dto.restaurant.RestaurantCreateRequestDTO;
import com.example.demo.dto.restaurant.RestaurantResponseDTO;
import com.example.demo.entity.*;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.exception.UnauthorizedException;
import com.example.demo.repository.RestaurantRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.SecurityUtil;
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
    private final SecurityUtil securityUtil;

    @Transactional
    public RestaurantResponseDTO createRestaurant(RestaurantCreateRequestDTO request, Long ownerId) {
        log.info("Creating restaurant: {} for user: {}", request.getName(), ownerId);

        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + ownerId));

        if (owner.getRole() != Role.VERIFIED_USER && owner.getRole() != Role.ADMIN) {
            throw new UnauthorizedException("Only verified users can create restaurants");
        }

        Double latitude = 50.061698;
        Double longitude = 19.937206;

        Restaurant restaurant = Restaurant.builder()
                .name(request.getName())
                .address(request.getAddress())
                .latitude(latitude)
                .longitude(longitude)
                .phone(request.getPhone())
                .description(request.getDescription())
                .openingHours(request.getOpeningHours())
                .owner(owner)
                .status(RestaurantStatus.PENDING)
                .isVerified(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .cuisineType(request.getCuisineType())
                .dietaryOptions(request.getDietaryOptions())
                .priceRange(PriceRange.valueOf(request.getPriceRange()))
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
    public List<RestaurantResponseDTO> getUnverifiedRestaurants() {
        return restaurantRepository.findByStatusAndIsVerified(RestaurantStatus.APPROVED, false).stream()
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
    public RestaurantResponseDTO verifyRestaurant(Long restaurantId, VerifyRestaurantRequestDTO request) {
        log.info("Verifying restaurant with ID: {}", restaurantId);

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with ID: " + restaurantId));

        User currentAdmin = securityUtil.getCurrentUser();

        restaurant.setIsVerified(true);
        restaurant.setVerifiedAt(LocalDateTime.now());
        restaurant.setVerifiedBy(currentAdmin);
        if (request != null && request.getNotes() != null) {
            restaurant.setVerificationNotes(request.getNotes());
        }
        restaurant.setUpdatedAt(LocalDateTime.now());

        Restaurant savedRestaurant = restaurantRepository.save(restaurant);
        log.info("Restaurant verified: {} by admin: {}", restaurantId, currentAdmin.getEmail());

        return mapToResponse(savedRestaurant);
    }

    @Transactional
    public RestaurantResponseDTO unverifyRestaurant(Long restaurantId) {
        log.info("Unverifying restaurant with ID: {}", restaurantId);

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with ID: " + restaurantId));

        restaurant.setIsVerified(false);
        restaurant.setVerifiedAt(null);
        restaurant.setVerifiedBy(null);
        restaurant.setVerificationNotes(null);
        restaurant.setUpdatedAt(LocalDateTime.now());

        Restaurant savedRestaurant = restaurantRepository.save(restaurant);
        log.info("Restaurant unverified: {}", restaurantId);

        return mapToResponse(savedRestaurant);
    }

    @Transactional
    public RestaurantResponseDTO updateRestaurant(Long restaurantId, RestaurantCreateRequestDTO request, Long userId) {
        log.info("Updating restaurant: {} by user: {}", restaurantId, userId);

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with ID: " + restaurantId));

        if (!restaurant.getOwner().getId().equals(userId)) {
            throw new UnauthorizedException("You can only update your own restaurants");
        }

        restaurant.setName(request.getName());
        restaurant.setAddress(request.getAddress());
        restaurant.setPhone(request.getPhone());
        restaurant.setOpeningHours(request.getOpeningHours());
        restaurant.setDescription(request.getDescription());

        restaurant.setCuisineType(request.getCuisineType());
        restaurant.setPriceRange(PriceRange.fromString(request.getPriceRange()));

        restaurant.clearDietaryOptions();
        if (request.getDietaryOptions() != null && !request.getDietaryOptions().isEmpty()) {
            request.getDietaryOptions().forEach(restaurant::addDietaryOption);
        }

        try {
            var coordinates = geocodingService.geocodeAddress(request.getAddress());
            restaurant.setLatitude(coordinates.getLatitude());
            restaurant.setLongitude(coordinates.getLongitude());
        } catch (Exception e) {
            // Keep existing coordinates if geocoding fails
        }

        Restaurant updated = restaurantRepository.save(restaurant);
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteRestaurant(Long restaurantId, Long userId) {
        log.info("Deleting restaurant: {} by user: {}", restaurantId, userId);

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with ID: " + restaurantId));

        if (!restaurant.getOwner().getId().equals(userId)) {
            throw new UnauthorizedException("You can only delete your own restaurants");
        }

        restaurantRepository.deleteById(restaurantId);
        log.info("Restaurant deleted: {}", restaurantId);
    }

    private RestaurantResponseDTO mapToResponse(Restaurant restaurant) {
        RestaurantResponseDTO.UserDTO ownerDTO = new RestaurantResponseDTO.UserDTO(
            restaurant.getOwner().getId(),
            restaurant.getOwner().getFirstName(),
            restaurant.getOwner().getLastName(),
            restaurant.getOwner().getEmail()
        );

        return RestaurantResponseDTO.builder()
            .id(restaurant.getId())
            .name(restaurant.getName())
            .address(restaurant.getAddress())
            .latitude(restaurant.getLatitude())
            .longitude(restaurant.getLongitude())
            .phone(restaurant.getPhone())
            .openingHours(restaurant.getOpeningHours())
            .description(restaurant.getDescription())
            .rating(restaurant.getRating())
            .status(restaurant.getStatus())
            .isVerified(restaurant.getIsVerified())
            .verifiedAt(restaurant.getVerifiedAt())
            .cuisineType(restaurant.getCuisineType())
            .priceRange(restaurant.getPriceRange() != null ?
                restaurant.getPriceRange().name().toLowerCase() : null)
            .dietaryOptions(restaurant.getDietaryOptions())
            .owner(ownerDTO)
            .createdAt(restaurant.getCreatedAt())
            .updatedAt(restaurant.getUpdatedAt())
            .build();
    }
}