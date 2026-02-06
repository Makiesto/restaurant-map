package com.example.demo.controller;

import com.example.demo.dto.admin.AdminStatsDTO;
import com.example.demo.dto.admin.VerifyRestaurantRequestDTO;
import com.example.demo.dto.restaurant.RestaurantResponseDTO;
import com.example.demo.dto.user.UserResponseDTO;
import com.example.demo.service.RestaurantService;
import com.example.demo.service.UserService;
import com.example.demo.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final RestaurantService restaurantService;
    private final UserService userService;
    private final AdminService adminService;

    /**
     * ADMIN: Get admin statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<AdminStatsDTO> getAdminStats() {
        log.info("GET /api/admin/stats - fetching admin statistics");
        AdminStatsDTO stats = adminService.getAdminStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * ADMIN: Get all pending restaurants for approval
     */
    @GetMapping("/restaurants/pending")
    public ResponseEntity<List<RestaurantResponseDTO>> getPendingRestaurants() {
        log.info("GET /api/admin/restaurants/pending - fetching pending restaurants");
        List<RestaurantResponseDTO> restaurants = restaurantService.getPendingRestaurants();
        return ResponseEntity.ok(restaurants);
    }

    /**
     * ADMIN: Get all unverified restaurants
     */
    @GetMapping("/restaurants/unverified")
    public ResponseEntity<List<RestaurantResponseDTO>> getUnverifiedRestaurants() {
        log.info("GET /api/admin/restaurants/unverified - fetching unverified restaurants");
        List<RestaurantResponseDTO> restaurants = restaurantService.getUnverifiedRestaurants();
        return ResponseEntity.ok(restaurants);
    }

    /**
     * ADMIN: Approve restaurant
     */
    @PutMapping("/restaurants/{id}/approve")
    public ResponseEntity<RestaurantResponseDTO> approveRestaurant(@PathVariable Long id) {
        log.info("PUT /api/admin/restaurants/{}/approve - approving restaurant", id);
        RestaurantResponseDTO restaurant = restaurantService.approveRestaurant(id);
        return ResponseEntity.ok(restaurant);
    }

    /**
     * ADMIN: Reject restaurant
     */
    @PutMapping("/restaurants/{id}/reject")
    public ResponseEntity<RestaurantResponseDTO> rejectRestaurant(@PathVariable Long id) {
        log.info("PUT /api/admin/restaurants/{}/reject - rejecting restaurant", id);
        RestaurantResponseDTO restaurant = restaurantService.rejectRestaurant(id);
        return ResponseEntity.ok(restaurant);
    }

    /**
     * ADMIN: Verify restaurant (mark as legitimate/not spam)
     */
    @PutMapping("/restaurants/{id}/verify")
    public ResponseEntity<RestaurantResponseDTO> verifyRestaurant(
            @PathVariable Long id,
            @Valid @RequestBody(required = false) VerifyRestaurantRequestDTO request) {
        log.info("PUT /api/admin/restaurants/{}/verify - verifying restaurant", id);
        RestaurantResponseDTO restaurant = restaurantService.verifyRestaurant(id, request);
        return ResponseEntity.ok(restaurant);
    }

    /**
     * ADMIN: Unverify restaurant
     */
    @PutMapping("/restaurants/{id}/unverify")
    public ResponseEntity<RestaurantResponseDTO> unverifyRestaurant(@PathVariable Long id) {
        log.info("PUT /api/admin/restaurants/{}/unverify - unverifying restaurant", id);
        RestaurantResponseDTO restaurant = restaurantService.unverifyRestaurant(id);
        return ResponseEntity.ok(restaurant);
    }

    /**
     * ADMIN: Verify user (promote to VERIFIED_USER)
     */
    @PutMapping("/users/{id}/verify")
    public ResponseEntity<UserResponseDTO> verifyUser(@PathVariable Long id) {
        log.info("PUT /api/admin/users/{}/verify - verifying user", id);
        UserResponseDTO user = userService.verifyUser(id);
        return ResponseEntity.ok(user);
    }

    /**
     * ADMIN: Get all users
     */
    @GetMapping("/users")
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        log.info("GET /api/admin/users - fetching all users");
        List<UserResponseDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }
}