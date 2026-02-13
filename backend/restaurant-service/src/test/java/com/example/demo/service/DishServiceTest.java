package com.example.demo.service;

import com.example.demo.dto.dish.DishCreateRequestDTO;
import com.example.demo.dto.dish.DishResponseDTO;
import com.example.demo.entity.*;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.exception.UnauthorizedException;
import com.example.demo.repository.ComponentRepository;
import com.example.demo.repository.DishRepository;
import com.example.demo.repository.RestaurantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DishServiceTest {

    @Mock
    private DishRepository dishRepository;

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private ComponentRepository componentRepository;

    @InjectMocks
    private DishService dishService;

    private User owner;
    private Restaurant restaurant;
    private Dish testDish;
    private Component testComponent;
    private DishCreateRequestDTO createRequest;

    @BeforeEach
    void setUp() {
        owner = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .role(Role.VERIFIED_USER)
                .build();

        restaurant = Restaurant.builder()
                .id(1L)
                .name("Test Restaurant")
                .owner(owner)
                .build();

        testComponent = Component.builder()
                .id(1L)
                .name("Chicken Breast")
                .proteinPer100g(31.0)
                .fatPer100g(3.6)
                .carbsPer100g(0.0)
                .kcalPer100g(165.0)
                .allergens(new HashSet<>())
                .build();

        testDish = Dish.builder()
                .id(1L)
                .name("Grilled Chicken")
                .description("Delicious grilled chicken")
                .price(25.99)
                .imgUrl("http://example.com/chicken.jpg")
                .isAvailable(true)
                .restaurant(restaurant)
                .dishComponents(new ArrayList<>())
                .baseProteinG(31.0)
                .baseFatG(3.6)
                .baseCarbsG(0.0)
                .baseKcal(165.0)
                .build();

        // Also create dish2 with price for the test that uses it
        Dish dish2 = Dish.builder()
                .id(2L)
                .name("Grilled Salmon")
                .price(29.99)
                .restaurant(restaurant)
                .dishComponents(new ArrayList<>())
                .build();

        createRequest = new DishCreateRequestDTO();
        createRequest.setName("Grilled Chicken");
        createRequest.setDescription("Delicious grilled chicken");
        createRequest.setPrice(25.99);
        createRequest.setImageUrl("http://example.com/chicken.jpg");

        DishCreateRequestDTO.DishComponentRequest componentRequest =
                new DishCreateRequestDTO.DishComponentRequest();
        componentRequest.setComponentId(1L);
        componentRequest.setAmount(100.0);
        componentRequest.setIsOptional(false);

        createRequest.setComponents(List.of(componentRequest));
    }

    @Test
    void createDish_WithValidData_ShouldSucceed() {
        // Given
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));
        when(componentRepository.findById(1L)).thenReturn(Optional.of(testComponent));
        when(dishRepository.save(any(Dish.class))).thenAnswer(invocation -> {
            Dish dish = invocation.getArgument(0);
            dish.setId(1L);
            return dish;
        });

        // When
        DishResponseDTO result = dishService.createDish(createRequest, 1L, 1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Grilled Chicken");
        assertThat(result.getRestaurantId()).isEqualTo(1L);
        assertThat(result.getBaseProteinG()).isEqualTo(31.0);
        assertThat(result.getComponents()).hasSize(1);

        verify(dishRepository).save(any(Dish.class));
    }

    @Test
    void createDish_WhenRestaurantNotFound_ShouldThrowException() {
        // Given
        when(restaurantRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> dishService.createDish(createRequest, 999L, 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Restaurant not found");

        verify(dishRepository, never()).save(any());
    }

    @Test
    void createDish_WhenUserIsNotOwner_ShouldThrowException() {
        // Given
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));

        // When/Then
        assertThatThrownBy(() -> dishService.createDish(createRequest, 1L, 999L))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("You can only add dishes to your own restaurants");

        verify(dishRepository, never()).save(any());
    }

    @Test
    void createDish_WhenComponentNotFound_ShouldThrowException() {
        // Given
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));
        when(componentRepository.findById(1L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> dishService.createDish(createRequest, 1L, 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Component not found");
    }

    @Test
    void getDishById_WhenExists_ShouldReturnDish() {
        // Given
        when(dishRepository.findById(1L)).thenReturn(Optional.of(testDish));

        // When
        DishResponseDTO result = dishService.getDishById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Grilled Chicken");
    }

    @Test
    void getDishById_WhenNotExists_ShouldThrowException() {
        // Given
        when(dishRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> dishService.getDishById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Dish not found");
    }

    @Test
    void getDishesByRestaurant_ShouldReturnAllDishes() {
        // Given
        Dish dish2 = Dish.builder()
                .id(2L)
                .name("Grilled Salmon")
                .price(29.99)
                .restaurant(restaurant)
                .dishComponents(new ArrayList<>())
                .baseProteinG(20.0)
                .baseFatG(10.0)
                .baseCarbsG(0.0)
                .baseKcal(200.0)
                .build();

        when(dishRepository.findByRestaurantId(1L)).thenReturn(List.of(testDish, dish2));

        // When
        List<DishResponseDTO> results = dishService.getDishesByRestaurant(1L);

        // Then
        assertThat(results).hasSize(2);
        assertThat(results).extracting(DishResponseDTO::getName)
                .containsExactly("Grilled Chicken", "Grilled Salmon");
    }

    @Test
    void updateDish_ByOwner_ShouldSucceed() {
        // Given
        when(dishRepository.findById(1L)).thenReturn(Optional.of(testDish));
        when(componentRepository.findById(1L)).thenReturn(Optional.of(testComponent));
        when(dishRepository.save(any(Dish.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DishCreateRequestDTO updateRequest = new DishCreateRequestDTO();
        updateRequest.setName("Updated Chicken");
        updateRequest.setDescription("Updated description");
        updateRequest.setPrice(29.99);
        updateRequest.setImageUrl("http://example.com/new-chicken.jpg");

        DishCreateRequestDTO.DishComponentRequest componentRequest =
                new DishCreateRequestDTO.DishComponentRequest();
        componentRequest.setComponentId(1L);
        componentRequest.setAmount(150.0);
        componentRequest.setIsOptional(false);
        updateRequest.setComponents(List.of(componentRequest));

        // When
        DishResponseDTO result = dishService.updateDish(1L, updateRequest, 1L);

        // Then
        assertThat(result.getName()).isEqualTo("Updated Chicken");
        assertThat(result.getDescription()).isEqualTo("Updated description");

        verify(dishRepository).save(argThat(dish ->
                dish.getName().equals("Updated Chicken") &&
                dish.getPrice().equals(29.99)
        ));
    }

    @Test
    void updateDish_ByNonOwner_ShouldThrowException() {
        // Given
        when(dishRepository.findById(1L)).thenReturn(Optional.of(testDish));

        // When/Then
        assertThatThrownBy(() -> dishService.updateDish(1L, createRequest, 999L))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("You can only update dishes from your own restaurants");

        verify(dishRepository, never()).save(any());
    }

    @Test
    void deleteDish_ByOwner_ShouldSucceed() {
        // Given
        when(dishRepository.findById(1L)).thenReturn(Optional.of(testDish));

        // When
        dishService.deleteDish(1L, 1L);

        // Then
        verify(dishRepository).deleteById(1L);
    }

    @Test
    void deleteDish_ByNonOwner_ShouldThrowException() {
        // Given
        when(dishRepository.findById(1L)).thenReturn(Optional.of(testDish));

        // When/Then
        assertThatThrownBy(() -> dishService.deleteDish(1L, 999L))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("You can only delete dishes from your own restaurants");

        verify(dishRepository, never()).deleteById(any());
    }

    @Test
    void createDish_WithoutComponents_ShouldHaveZeroMacros() {
        // Given
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));
        when(dishRepository.save(any(Dish.class))).thenAnswer(invocation -> {
            Dish dish = invocation.getArgument(0);
            dish.setId(1L);
            return dish;
        });

        DishCreateRequestDTO requestWithoutComponents = new DishCreateRequestDTO();
        requestWithoutComponents.setName("Simple Dish");
        requestWithoutComponents.setDescription("No components");
        requestWithoutComponents.setPrice(10.0);
        requestWithoutComponents.setComponents(List.of());

        // When
        DishResponseDTO result = dishService.createDish(requestWithoutComponents, 1L, 1L);

        // Then
        assertThat(result.getBaseProteinG()).isEqualTo(0.0);
        assertThat(result.getBaseFatG()).isEqualTo(0.0);
        assertThat(result.getBaseCarbsG()).isEqualTo(0.0);
        assertThat(result.getBaseKcal()).isEqualTo(0.0);
    }

    @Test
    void createDish_WithMultipleComponents_ShouldCalculateTotalMacros() {
        // Given
        Component component2 = Component.builder()
                .id(2L)
                .name("Rice")
                .proteinPer100g(2.7)
                .fatPer100g(0.3)
                .carbsPer100g(28.0)
                .kcalPer100g(130.0)
                .allergens(new HashSet<>())
                .build();

        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));
        when(componentRepository.findById(1L)).thenReturn(Optional.of(testComponent)); // Chicken
        when(componentRepository.findById(2L)).thenReturn(Optional.of(component2)); // Rice
        when(dishRepository.save(any(Dish.class))).thenAnswer(invocation -> {
            Dish dish = invocation.getArgument(0);
            dish.setId(1L);
            return dish;
        });

        DishCreateRequestDTO.DishComponentRequest chickenRequest =
                new DishCreateRequestDTO.DishComponentRequest();
        chickenRequest.setComponentId(1L);
        chickenRequest.setAmount(100.0); // 100g chicken
        chickenRequest.setIsOptional(false);

        DishCreateRequestDTO.DishComponentRequest riceRequest =
                new DishCreateRequestDTO.DishComponentRequest();
        riceRequest.setComponentId(2L);
        riceRequest.setAmount(200.0); // 200g rice
        riceRequest.setIsOptional(false);

        DishCreateRequestDTO request = new DishCreateRequestDTO();
        request.setName("Chicken with Rice");
        request.setDescription("Healthy meal");
        request.setPrice(35.0);
        request.setComponents(List.of(chickenRequest, riceRequest));

        // When
        DishResponseDTO result = dishService.createDish(request, 1L, 1L);

        // Then
        // Chicken: 31.0 protein, 3.6 fat, 0.0 carbs, 165 kcal (per 100g)
        // Rice: 2.7 protein, 0.3 fat, 28.0 carbs, 130 kcal (per 100g) * 2 (200g)
        // Total: 31.0 + 5.4 = 36.4 protein, 3.6 + 0.6 = 4.2 fat, 0 + 56 = 56 carbs, 165 + 260 = 425 kcal
        assertThat(result.getBaseProteinG()).isEqualTo(36.4);
        assertThat(result.getBaseFatG()).isEqualTo(4.2);
        assertThat(result.getBaseCarbsG()).isEqualTo(56.0);
        assertThat(result.getBaseKcal()).isEqualTo(425.0);
    }
}