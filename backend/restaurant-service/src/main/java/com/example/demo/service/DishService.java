package com.example.demo.service;

import com.example.demo.dto.dish.DishCreateRequestDTO;
import com.example.demo.dto.dish.DishResponseDTO;
import com.example.demo.entity.*;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.exception.UnauthorizedException;
import com.example.demo.repository.ComponentRepository;
import com.example.demo.repository.DishRepository;
import com.example.demo.repository.RestaurantRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DishService {

    private final DishRepository dishRepository;
    private final RestaurantRepository restaurantRepository;
    private final ComponentRepository componentRepository;

    @Transactional
    public DishResponseDTO createDish(DishCreateRequestDTO request, Long restaurantId, Long userId) {
        log.info("Creating dish: {} for restaurant: {}", request.getName(), restaurantId);

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with ID: " + restaurantId));

        // Check if user is the owner
        if (!restaurant.getOwner().getId().equals(userId)) {
            throw new UnauthorizedException("You can only add dishes to your own restaurants");
        }

        Dish dish = Dish.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .imgUrl(request.getImageUrl())
                .isAvailable(true)
                .restaurant(restaurant)
                .dishComponents(new ArrayList<>())
                .build();

        // Add components if provided
        if (request.getComponents() != null && !request.getComponents().isEmpty()) {
            for (DishCreateRequestDTO.DishComponentRequest compReq : request.getComponents()) {
                Component component = componentRepository.findById(compReq.getComponentId())
                        .orElseThrow(() -> new ResourceNotFoundException("Component not found with ID: " + compReq.getComponentId()));

                DishComponent dishComponent = DishComponent.builder()
                        .dish(dish)
                        .component(component)
                        .amount(compReq.getAmount())
                        .isOptional(compReq.getIsOptional())
                        .build();

                dish.getDishComponents().add(dishComponent);
            }
        }

        // Calculate macros from components
        calculateMacros(dish);

        Dish savedDish = dishRepository.save(dish);
        log.info("Dish created with ID: {}", savedDish.getId());

        return mapToResponse(savedDish);
    }

    @Transactional(readOnly = true)
    public DishResponseDTO getDishById(Long id) {
        Dish dish = dishRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dish not found with ID: " + id));
        return mapToResponse(dish);
    }

    @Transactional(readOnly = true)
    public List<DishResponseDTO> getDishesByRestaurant(Long restaurantId) {
        return dishRepository.findByRestaurantId(restaurantId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public DishResponseDTO updateDish(Long id, DishCreateRequestDTO request, Long userId) {
        log.info("Updating dish: {} by user: {}", id, userId);

        Dish dish = dishRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dish not found with ID: " + id));

        // Check if user is the restaurant owner
        if (!dish.getRestaurant().getOwner().getId().equals(userId)) {
            throw new UnauthorizedException("You can only update dishes from your own restaurants");
        }

        // Update basic fields
        dish.setName(request.getName());
        dish.setDescription(request.getDescription());
        dish.setPrice(request.getPrice());
        dish.setImgUrl(request.getImageUrl());

        // Update components if provided
        if (request.getComponents() != null) {
            // Clear existing components
            dish.getDishComponents().clear();

            // Add new components
            for (DishCreateRequestDTO.DishComponentRequest compReq : request.getComponents()) {
                Component component = componentRepository.findById(compReq.getComponentId())
                        .orElseThrow(() -> new ResourceNotFoundException("Component not found with ID: " + compReq.getComponentId()));

                DishComponent dishComponent = DishComponent.builder()
                        .dish(dish)
                        .component(component)
                        .amount(compReq.getAmount())
                        .isOptional(compReq.getIsOptional())
                        .build();

                dish.getDishComponents().add(dishComponent);
            }

            // Recalculate macros
            calculateMacros(dish);
        }

        Dish savedDish = dishRepository.save(dish);
        log.info("Dish updated: {}", id);

        return mapToResponse(savedDish);
    }

    @Transactional
    public void deleteDish(Long dishId, Long userId) {
        log.info("Deleting dish: {} by user: {}", dishId, userId);

        Dish dish = dishRepository.findById(dishId)
                .orElseThrow(() -> new ResourceNotFoundException("Dish not found with ID: " + dishId));

        // Check if user is the restaurant owner
        if (!dish.getRestaurant().getOwner().getId().equals(userId)) {
            throw new UnauthorizedException("You can only delete dishes from your own restaurants");
        }

        dishRepository.deleteById(dishId);
        log.info("Dish deleted: {}", dishId);
    }

    private void calculateMacros(Dish dish) {
        double totalProtein = 0.0;
        double totalFat = 0.0;
        double totalCarbs = 0.0;
        double totalKcal = 0.0;

        for (DishComponent dc : dish.getDishComponents()) {
            Component comp = dc.getComponent();
            double amount = dc.getAmount();

            totalProtein += (comp.getProteinPer100g() * amount / 100);
            totalFat += (comp.getFatPer100g() * amount / 100);
            totalCarbs += (comp.getCarbsPer100g() * amount / 100);
            totalKcal += (comp.getKcalPer100g() * amount / 100);
        }

        dish.setBaseProteinG(totalProtein);
        dish.setBaseFatG(totalFat);
        dish.setBaseCarbsG(totalCarbs);
        dish.setBaseKcal(totalKcal);
    }

    private DishResponseDTO mapToResponse(Dish dish) {
        // Get all unique allergens from components
        Set<String> allergens = dish.getDishComponents().stream()
                .flatMap(dc -> dc.getComponent().getAllergens().stream())
                .map(Allergen::getName)
                .collect(Collectors.toSet());

        // Map components
        List<DishResponseDTO.ComponentInfo> componentInfos = dish.getDishComponents().stream()
                .map(dc -> DishResponseDTO.ComponentInfo.builder()
                        .componentId(dc.getComponent().getId())
                        .componentName(dc.getComponent().getName())
                        .amount(dc.getAmount())
                        .isOptional(dc.getIsOptional())
                        .build())
                .collect(Collectors.toList());

        return DishResponseDTO.builder()
                .id(dish.getId())
                .name(dish.getName())
                .description(dish.getDescription())
                .price(BigDecimal.valueOf(dish.getPrice()))
                .imageUrl(dish.getImgUrl())
                .isAvailable(dish.getIsAvailable())
                .baseProteinG(dish.getBaseProteinG())
                .baseFatG(dish.getBaseFatG())
                .baseCarbsG(dish.getBaseCarbsG())
                .baseKcal(dish.getBaseKcal())
                .restaurantId(dish.getRestaurant().getId())
                .restaurantName(dish.getRestaurant().getName())
                .components(componentInfos)
                .allergens(allergens.stream().collect(Collectors.toList()))
                .build();
    }

}