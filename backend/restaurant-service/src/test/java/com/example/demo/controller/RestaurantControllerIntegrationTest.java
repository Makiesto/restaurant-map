package com.example.demo.controller;

import com.example.demo.dto.restaurant.RestaurantCreateRequestDTO;
import com.example.demo.dto.restaurant.RestaurantResponseDTO;
import com.example.demo.entity.RestaurantStatus;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.exception.UnauthorizedException;
import com.example.demo.security.SecurityUtil;
import com.example.demo.service.RestaurantService;
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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Additional comprehensive tests for RestaurantController
 * Tests edge cases, validation, security, and error handling
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
class RestaurantControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private RestaurantService restaurantService;

    @MockitoBean
    private SecurityUtil securityUtil;

    private RestaurantResponseDTO testRestaurantDTO;
    private RestaurantCreateRequestDTO createRequest;

    @BeforeEach
    void setUp() {
        createRequest = new RestaurantCreateRequestDTO();
        createRequest.setName("Test Restaurant");
        createRequest.setAddress("123 Test St");
        createRequest.setPhone("+48123456789");
        createRequest.setDescription("A test restaurant");
        createRequest.setCuisineType("Italian");
        createRequest.setPriceRange("MODERATE");
        createRequest.setDietaryOptions(new HashSet<>(Arrays.asList("Vegetarian", "Vegan")));

        RestaurantResponseDTO.UserDTO ownerDTO = new RestaurantResponseDTO.UserDTO(
                1L, "John", "Doe", "john@example.com"
        );

        testRestaurantDTO = RestaurantResponseDTO.builder()
                .id(1L)
                .name("Test Restaurant")
                .address("123 Test St")
                .latitude(50.061698)
                .longitude(19.937206)
                .phone("+48123456789")
                .description("A test restaurant")
                .cuisineType("Italian")
                .priceRange("moderate")
                .dietaryOptions(new HashSet<>(Arrays.asList("Vegetarian", "Vegan")))
                .status(RestaurantStatus.PENDING)
                .isVerified(false)
                .owner(ownerDTO)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // ========== GET /api/restaurants Tests ==========

    @Test
    void getAllApprovedRestaurants_WithMultipleRestaurants_ShouldReturnAll() throws Exception {
        // Given
        RestaurantResponseDTO restaurant2 = RestaurantResponseDTO.builder()
                .id(2L)
                .name("Second Restaurant")
                .address("456 Second St")
                .status(RestaurantStatus.APPROVED)
                .build();

        RestaurantResponseDTO restaurant3 = RestaurantResponseDTO.builder()
                .id(3L)
                .name("Third Restaurant")
                .address("789 Third St")
                .status(RestaurantStatus.APPROVED)
                .build();

        when(restaurantService.getAllApprovedRestaurants())
                .thenReturn(Arrays.asList(testRestaurantDTO, restaurant2, restaurant3));

        // When/Then
        mockMvc.perform(get("/api/restaurants"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].name").value("Test Restaurant"))
                .andExpect(jsonPath("$[1].name").value("Second Restaurant"))
                .andExpect(jsonPath("$[2].name").value("Third Restaurant"));
    }

    @Test
    void getAllApprovedRestaurants_WithNoRestaurants_ShouldReturnEmptyArray() throws Exception {
        // Given
        when(restaurantService.getAllApprovedRestaurants()).thenReturn(Collections.emptyList());

        // When/Then
        mockMvc.perform(get("/api/restaurants"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getAllApprovedRestaurants_CalledMultipleTimes_ShouldReturnConsistently() throws Exception {
        // Given
        when(restaurantService.getAllApprovedRestaurants())
                .thenReturn(List.of(testRestaurantDTO));

        // When/Then - Multiple calls
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(get("/api/restaurants"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1));
        }
    }

    // ========== GET /api/restaurants/{id} Tests ==========

    @Test
    void getRestaurantById_WithValidId_ShouldReturnFullDetails() throws Exception {
        // Given
        when(restaurantService.getRestaurantById(1L)).thenReturn(testRestaurantDTO);

        // When/Then
        mockMvc.perform(get("/api/restaurants/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Restaurant"))
                .andExpect(jsonPath("$.address").value("123 Test St"))
                .andExpect(jsonPath("$.phone").value("+48123456789"))
                .andExpect(jsonPath("$.cuisineType").value("Italian"))
                .andExpect(jsonPath("$.priceRange").value("moderate"))
                .andExpect(jsonPath("$.dietaryOptions").isArray())
                .andExpect(jsonPath("$.owner.email").value("john@example.com"));
    }

    @Test
    void getRestaurantById_WithNonExistentId_ShouldReturn404() throws Exception {
        // Given
        when(restaurantService.getRestaurantById(999L))
                .thenThrow(new ResourceNotFoundException("Restaurant not found"));

        // When/Then
        mockMvc.perform(get("/api/restaurants/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getRestaurantById_WithNegativeId_ShouldReturn404() throws Exception {
        // Given
        when(restaurantService.getRestaurantById(-1L))
                .thenThrow(new ResourceNotFoundException("Restaurant not found"));

        // When/Then
        mockMvc.perform(get("/api/restaurants/-1"))
                .andExpect(status().isNotFound());
    }

    // ========== GET /api/restaurants/my Tests ==========

    @Test
    @WithMockUser(username = "john@example.com")
    void getMyRestaurants_WithMultipleRestaurants_ShouldReturnAll() throws Exception {
        // Given
        RestaurantResponseDTO restaurant2 = RestaurantResponseDTO.builder()
                .id(2L)
                .name("My Second Restaurant")
                .build();

        when(securityUtil.getCurrentUserId()).thenReturn(1L);
        when(restaurantService.getRestaurantsByOwner(1L))
                .thenReturn(Arrays.asList(testRestaurantDTO, restaurant2));

        // When/Then
        mockMvc.perform(get("/api/restaurants/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @WithMockUser(username = "john@example.com")
    void getMyRestaurants_WithNoRestaurants_ShouldReturnEmptyArray() throws Exception {
        // Given
        when(securityUtil.getCurrentUserId()).thenReturn(1L);
        when(restaurantService.getRestaurantsByOwner(1L))
                .thenReturn(Collections.emptyList());

        // When/Then
        mockMvc.perform(get("/api/restaurants/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getMyRestaurants_WithoutAuthentication_ShouldReturn403() throws Exception {
        // When/Then
        mockMvc.perform(get("/api/restaurants/my"))
                .andExpect(status().isForbidden());
    }

    // ========== POST /api/restaurants Tests ==========

    @Test
    @WithMockUser(roles = "VERIFIED_USER")
    void createRestaurant_WithAllFields_ShouldSucceed() throws Exception {
        // Given
        when(securityUtil.getCurrentUserId()).thenReturn(1L);
        when(restaurantService.createRestaurant(any(), eq(1L))).thenReturn(testRestaurantDTO);

        // When/Then
        mockMvc.perform(post("/api/restaurants")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Restaurant"))
                .andExpect(jsonPath("$.cuisineType").value("Italian"))
                .andExpect(jsonPath("$.priceRange").value("moderate"))
                .andExpect(jsonPath("$.dietaryOptions").isArray());
    }

    @Test
    @WithMockUser(roles = "VERIFIED_USER")
    void createRestaurant_WithMinimalFields_ShouldSucceed() throws Exception {
        // Given
        RestaurantCreateRequestDTO minimalRequest = new RestaurantCreateRequestDTO();
        minimalRequest.setName("Minimal Restaurant");
        minimalRequest.setAddress("123 Main St");

        when(securityUtil.getCurrentUserId()).thenReturn(1L);
        when(restaurantService.createRestaurant(any(), eq(1L))).thenReturn(testRestaurantDTO);

        // When/Then
        mockMvc.perform(post("/api/restaurants")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(minimalRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "VERIFIED_USER")
    void createRestaurant_WithBlankName_ShouldReturn400() throws Exception {
        // Given
        createRequest.setName("");

        // When/Then
        mockMvc.perform(post("/api/restaurants")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.name").exists());
    }

    @Test
    @WithMockUser(roles = "VERIFIED_USER")
    void createRestaurant_WithBlankAddress_ShouldReturn400() throws Exception {
        // Given
        createRequest.setAddress("");

        // When/Then
        mockMvc.perform(post("/api/restaurants")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.address").exists());
    }

    @Test
    @WithMockUser(roles = "VERIFIED_USER")
    void createRestaurant_WithNullName_ShouldReturn400() throws Exception {
        // Given
        createRequest.setName(null);

        // When/Then
        mockMvc.perform(post("/api/restaurants")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "VERIFIED_USER")
    void createRestaurant_WithVeryLongName_ShouldSucceed() throws Exception {
        // Given
        createRequest.setName("A".repeat(200));
        when(securityUtil.getCurrentUserId()).thenReturn(1L);
        when(restaurantService.createRestaurant(any(), eq(1L))).thenReturn(testRestaurantDTO);

        // When/Then
        mockMvc.perform(post("/api/restaurants")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "VERIFIED_USER")
    void createRestaurant_WithSpecialCharactersInName_ShouldSucceed() throws Exception {
        // Given
        createRequest.setName("José's Café & Bäckerei");
        when(securityUtil.getCurrentUserId()).thenReturn(1L);
        when(restaurantService.createRestaurant(any(), eq(1L))).thenReturn(testRestaurantDTO);

        // When/Then
        mockMvc.perform(post("/api/restaurants")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createRestaurant_AsAdmin_ShouldSucceed() throws Exception {
        // Given
        when(securityUtil.getCurrentUserId()).thenReturn(2L);
        when(restaurantService.createRestaurant(any(), eq(2L))).thenReturn(testRestaurantDTO);

        // When/Then
        mockMvc.perform(post("/api/restaurants")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "USER")
    void createRestaurant_AsUnverifiedUser_ShouldReturn403() throws Exception {
        // When/Then
        mockMvc.perform(post("/api/restaurants")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createRestaurant_WithoutAuthentication_ShouldReturn403() throws Exception {
        // When/Then
        mockMvc.perform(post("/api/restaurants")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isForbidden());
    }

    // ========== PUT /api/restaurants/{id} Tests ==========

    @Test
    @WithMockUser(roles = "VERIFIED_USER")
    void updateRestaurant_AsOwner_ShouldSucceed() throws Exception {
        // Given
        RestaurantCreateRequestDTO updateRequest = new RestaurantCreateRequestDTO();
        updateRequest.setName("Updated Restaurant");
        updateRequest.setAddress("456 Updated St");
        updateRequest.setDescription("Updated description");
        updateRequest.setCuisineType("French");

        testRestaurantDTO.setName("Updated Restaurant");
        testRestaurantDTO.setDescription("Updated description");

        when(securityUtil.getCurrentUserId()).thenReturn(1L);
        when(restaurantService.updateRestaurant(eq(1L), any(), eq(1L)))
                .thenReturn(testRestaurantDTO);

        // When/Then
        mockMvc.perform(put("/api/restaurants/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Restaurant"));
    }

    @Test
    @WithMockUser(roles = "VERIFIED_USER")
    void updateRestaurant_AsNonOwner_ShouldReturn403() throws Exception {
        // Given
        when(securityUtil.getCurrentUserId()).thenReturn(2L);
        when(restaurantService.updateRestaurant(eq(1L), any(), eq(2L)))
                .thenThrow(new UnauthorizedException("You can only update your own restaurants"));

        // When/Then
        mockMvc.perform(put("/api/restaurants/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "VERIFIED_USER")
    void updateRestaurant_WithInvalidData_ShouldReturn400() throws Exception {
        // Given
        createRequest.setName("");

        // When/Then
        mockMvc.perform(put("/api/restaurants/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "VERIFIED_USER")
    void updateRestaurant_NonExistentRestaurant_ShouldReturn404() throws Exception {
        // Given
        when(securityUtil.getCurrentUserId()).thenReturn(1L);
        when(restaurantService.updateRestaurant(eq(999L), any(), eq(1L)))
                .thenThrow(new ResourceNotFoundException("Restaurant not found"));

        // When/Then
        mockMvc.perform(put("/api/restaurants/999")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isNotFound());
    }

    // ========== DELETE /api/restaurants/{id} Tests ==========

    @Test
    @WithMockUser(roles = "VERIFIED_USER")
    void deleteRestaurant_AsOwner_ShouldReturn204() throws Exception {
        // Given
        when(securityUtil.getCurrentUserId()).thenReturn(1L);

        // When/Then
        mockMvc.perform(delete("/api/restaurants/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "VERIFIED_USER")
    void deleteRestaurant_AsNonOwner_ShouldReturn403() throws Exception {
        // Given
        when(securityUtil.getCurrentUserId()).thenReturn(2L);
        doThrow(new UnauthorizedException("You can only delete your own restaurants"))
                .when(restaurantService).deleteRestaurant(eq(1L), eq(2L));

        // When/Then
        mockMvc.perform(delete("/api/restaurants/1")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "VERIFIED_USER")
    void deleteRestaurant_NonExistent_ShouldReturn404() throws Exception {
        Long nonExistentId = 999L;
        Long currentUserId = 1L;

        // Given
        when(securityUtil.getCurrentUserId()).thenReturn(1L);
        doThrow(new ResourceNotFoundException("Restaurant not found"))
                .when(restaurantService).deleteRestaurant(eq(nonExistentId), eq(currentUserId));

        // When/Then
        mockMvc.perform(delete("/api/restaurants/999")
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteRestaurant_WithoutAuthentication_ShouldReturn403() throws Exception {
        // When/Then
        mockMvc.perform(delete("/api/restaurants/1")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void deleteRestaurant_AsUnverifiedUser_ShouldReturn403() throws Exception {
        // When/Then
        mockMvc.perform(delete("/api/restaurants/1")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    // ========== CORS and Security Tests ==========

    @Test
    void options_Request_ShouldBeAllowed() throws Exception {
        // When/Then
        mockMvc.perform(options("/api/restaurants"))
                .andExpect(status().isOk());
    }

}