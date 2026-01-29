package com.example.demo.controller;

import com.example.demo.dto.restaurant.RestaurantCreateRequestDTO;
import com.example.demo.dto.restaurant.RestaurantResponseDTO;
import com.example.demo.security.SecurityUtil;
import com.example.demo.service.RestaurantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*") // TODO: Change in production
public class RestaurantController {

    private final RestaurantService restaurantService;
    private final SecurityUtil securityUtil;

    /**
     * PUBLIC: Get all approved restaurants (for map)
     */
    @GetMapping
    public ResponseEntity<List<RestaurantResponseDTO>> getAllApprovedRestaurants() {
        log.info("GET /api/restaurants - fetching all approved restaurants");
        List<RestaurantResponseDTO> restaurants = restaurantService.getAllApprovedRestaurants();
        return ResponseEntity.ok(restaurants);
    }

    /**
     * PUBLIC: Get restaurant by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<RestaurantResponseDTO> getRestaurantById(@PathVariable Long id) {
        log.info("GET /api/restaurants/{} - fetching restaurant", id);
        RestaurantResponseDTO restaurant = restaurantService.getRestaurantById(id);
        return ResponseEntity.ok(restaurant);
    }

    /**
     * AUTHENTICATED: Get my restaurants (owner only)
     */
    @GetMapping("/my")
    public ResponseEntity<List<RestaurantResponseDTO>> getMyRestaurants() {
        Long userId = securityUtil.getCurrentUserId();

        log.info("GET /api/restaurants/my - fetching restaurants for user: {}", userId);
        List<RestaurantResponseDTO> restaurants = restaurantService.getRestaurantsByOwner(userId);
        return ResponseEntity.ok(restaurants);
    }

    /**
     * VERIFIED_USER: Create new restaurant (status: PENDING)
     */
    @PostMapping
    public ResponseEntity<RestaurantResponseDTO> createRestaurant(
            @Valid @RequestBody RestaurantCreateRequestDTO request) {

        Long userId = securityUtil.getCurrentUserId();

        log.info("POST /api/restaurants - creating restaurant: {} for user: {}",
                 request.getName(), userId);

        RestaurantResponseDTO restaurant = restaurantService.createRestaurant(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(restaurant);
    }

    /**
     * VERIFIED_USER: Update my restaurant
     */
    @PutMapping("/{id}")
    public ResponseEntity<RestaurantResponseDTO> updateRestaurant(
            @PathVariable Long id,
            @Valid @RequestBody RestaurantCreateRequestDTO request) {

        Long userId = securityUtil.getCurrentUserId();

        log.info("PUT /api/restaurants/{} - updating restaurant for user: {}", id, userId);
        RestaurantResponseDTO restaurant = restaurantService.updateRestaurant(id, request, userId);
        return ResponseEntity.ok(restaurant);
    }

    /**
     * VERIFIED_USER: Delete my restaurant
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRestaurant(@PathVariable Long id) {

        Long userId = securityUtil.getCurrentUserId();

        log.info("DELETE /api/restaurants/{} - deleting restaurant for user: {}", id, userId);
        restaurantService.deleteRestaurant(id, userId);
        return ResponseEntity.noContent().build();
    }
}