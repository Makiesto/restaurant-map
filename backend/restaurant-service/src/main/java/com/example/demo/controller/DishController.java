package com.example.demo.controller;

import com.example.demo.dto.dish.DishCreateRequestDTO;
import com.example.demo.dto.dish.DishResponseDTO;
import com.example.demo.security.SecurityUtil;
import com.example.demo.service.DishService;
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
public class DishController {

    private final DishService dishService;
    private final SecurityUtil securityUtil;

    /**
     * PUBLIC: Get restaurant menu (all dishes)
     */
    @GetMapping("/restaurants/{restaurantId}/menu")
    public ResponseEntity<List<DishResponseDTO>> getRestaurantMenu(
            @PathVariable Long restaurantId) {
        log.info("GET /api/restaurants/{}/menu - fetching menu", restaurantId);
        List<DishResponseDTO> dishes = dishService.getDishesByRestaurant(restaurantId);
        return ResponseEntity.ok(dishes);
    }

    /**
     * PUBLIC: Get dish details
     */
    @GetMapping("/dishes/{id}")
    public ResponseEntity<DishResponseDTO> getDishById(@PathVariable Long id) {
        log.info("GET /api/dishes/{} - fetching dish", id);
        DishResponseDTO dish = dishService.getDishById(id);
        return ResponseEntity.ok(dish);
    }

    /**
     * VERIFIED_USER: Add dish to my restaurant
     */
    @PostMapping("/restaurants/{restaurantId}/dishes")
    public ResponseEntity<DishResponseDTO> createDish(
            @PathVariable Long restaurantId,
            @Valid @RequestBody DishCreateRequestDTO request) {

        Long userId = securityUtil.getCurrentUserId();

        log.info("POST /api/restaurants/{}/dishes - creating dish: {} for user: {}",
                restaurantId, request.getName(), userId);

        DishResponseDTO dish = dishService.createDish(request, restaurantId, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(dish);
    }

    /**
     * VERIFIED_USER: Update my dish
     */
    @PutMapping("/dishes/{id}")
    public ResponseEntity<DishResponseDTO> updateDish(
            @PathVariable Long id,
            @Valid @RequestBody DishCreateRequestDTO request) {

        Long userId = securityUtil.getCurrentUserId();

        log.info("PUT /api/dishes/{} - updating dish for user: {}", id, userId);
        DishResponseDTO dish = dishService.updateDish(id, request, userId);
        return ResponseEntity.ok(dish);
    }

    /**
     * VERIFIED_USER: Delete my dish
     */
    @DeleteMapping("/dishes/{id}")
    public ResponseEntity<Void> deleteDish(@PathVariable Long id) {

        Long userId = securityUtil.getCurrentUserId();

        log.info("DELETE /api/dishes/{} - deleting dish for user: {}", id, userId);
        dishService.deleteDish(id, userId);
        return ResponseEntity.noContent().build();
    }
}