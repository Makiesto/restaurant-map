package com.example.demo.controller;

import com.example.demo.dto.dish.DishCreateRequestDTO;
import com.example.demo.dto.dish.DishResponseDTO;
import com.example.demo.service.DishService;
import com.example.demo.security.SecurityUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
class DishControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private DishService dishService;

    @MockitoBean
    private SecurityUtil securityUtil;

    private DishResponseDTO testDishDTO;
    private DishCreateRequestDTO createRequest;

    @BeforeEach
    void setUp() {
        testDishDTO = DishResponseDTO.builder()
                .id(1L)
                .name("Grilled Chicken")
                .description("Delicious grilled chicken")
                .price(BigDecimal.valueOf(25.99))
                .imageUrl("http://example.com/chicken.jpg")
                .isAvailable(true)
                .baseProteinG(31.0)
                .baseFatG(3.6)
                .baseCarbsG(0.0)
                .baseKcal(165.0)
                .restaurantId(1L)
                .restaurantName("Test Restaurant")
                .components(List.of())
                .allergens(List.of())
                .build();

        createRequest = new DishCreateRequestDTO();
        createRequest.setName("Grilled Chicken");
        createRequest.setDescription("Delicious grilled chicken");
        createRequest.setPrice(25.99);
        createRequest.setImageUrl("http://example.com/chicken.jpg");
    }

    @Test
    void getRestaurantMenu_ShouldReturnDishes() throws Exception {
        // Given
        when(dishService.getDishesByRestaurant(1L))
                .thenReturn(List.of(testDishDTO));

        // When/Then
        mockMvc.perform(get("/api/restaurants/1/menu"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Grilled Chicken"))
                .andExpect(jsonPath("$[0].price").value(25.99));
    }

    @Test
    void getDishById_WhenExists_ShouldReturnDish() throws Exception {
        // Given
        when(dishService.getDishById(1L)).thenReturn(testDishDTO);

        // When/Then
        mockMvc.perform(get("/api/dishes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Grilled Chicken"))
                .andExpect(jsonPath("$.restaurantName").value("Test Restaurant"));
    }

    @Test
    @WithMockUser(roles = "VERIFIED_USER")
    void createDish_WithValidData_ShouldReturnCreated() throws Exception {
        // Given
        when(securityUtil.getCurrentUserId()).thenReturn(1L);
        when(dishService.createDish(any(), eq(1L), eq(1L))).thenReturn(testDishDTO);

        // When/Then
        mockMvc.perform(post("/api/restaurants/1/dishes")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Grilled Chicken"))
                .andExpect(jsonPath("$.price").value(25.99));
    }

    @Test
    @WithMockUser(roles = "VERIFIED_USER")
    void createDish_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        // Given
        DishCreateRequestDTO invalidRequest = new DishCreateRequestDTO();
        // Missing required fields

        // When/Then
        mockMvc.perform(post("/api/restaurants/1/dishes")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createDish_WithoutAuthentication_ShouldReturnForbidden() throws Exception {
        // When/Then
        mockMvc.perform(post("/api/restaurants/1/dishes")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void createDish_WithUnverifiedUser_ShouldReturnForbidden() throws Exception {
        // When/Then
        mockMvc.perform(post("/api/restaurants/1/dishes")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "VERIFIED_USER")
    void updateDish_WithValidData_ShouldReturnUpdated() throws Exception {
        // Given
        when(securityUtil.getCurrentUserId()).thenReturn(1L);
        testDishDTO.setName("Updated Chicken");
        when(dishService.updateDish(eq(1L), any(), eq(1L)))
                .thenReturn(testDishDTO);

        createRequest.setName("Updated Chicken");

        // When/Then
        mockMvc.perform(put("/api/dishes/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Chicken"));
    }

    @Test
    @WithMockUser(roles = "VERIFIED_USER")
    void deleteDish_ShouldReturnNoContent() throws Exception {
        // Given
        when(securityUtil.getCurrentUserId()).thenReturn(1L);

        // When/Then
        mockMvc.perform(delete("/api/dishes/1")
                .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createDish_WithAdminRole_ShouldSucceed() throws Exception {
        // Given
        when(securityUtil.getCurrentUserId()).thenReturn(1L);
        when(dishService.createDish(any(), eq(1L), eq(1L))).thenReturn(testDishDTO);

        // When/Then
        mockMvc.perform(post("/api/restaurants/1/dishes")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    void getRestaurantMenu_ForNonExistentRestaurant_ShouldReturnEmptyList() throws Exception {
        // Given
        when(dishService.getDishesByRestaurant(999L)).thenReturn(List.of());

        // When/Then
        mockMvc.perform(get("/api/restaurants/999/menu"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @WithMockUser(roles = "VERIFIED_USER")
    void createDish_WithNegativePrice_ShouldReturnBadRequest() throws Exception {
        // Given
        DishCreateRequestDTO invalidRequest = new DishCreateRequestDTO();
        invalidRequest.setName("Test Dish");
        invalidRequest.setPrice(-10.0); // Negative price

        // When/Then
        mockMvc.perform(post("/api/restaurants/1/dishes")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}