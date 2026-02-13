package com.example.demo.controller;

import com.example.demo.dto.restaurant.RestaurantCreateRequestDTO;
import com.example.demo.dto.restaurant.RestaurantResponseDTO;
import com.example.demo.entity.RestaurantStatus;
import com.example.demo.service.RestaurantService;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for RestaurantController using full Spring Boot context
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
                .status(RestaurantStatus.PENDING)
                .isVerified(false)
                .owner(ownerDTO)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void getAllApprovedRestaurants_ShouldReturnRestaurants() throws Exception {
        // Given
        when(restaurantService.getAllApprovedRestaurants())
                .thenReturn(List.of(testRestaurantDTO));

        // When/Then
        mockMvc.perform(get("/api/restaurants"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Test Restaurant"));
    }

    @Test
    void getRestaurantById_WhenExists_ShouldReturnRestaurant() throws Exception {
        // Given
        when(restaurantService.getRestaurantById(1L)).thenReturn(testRestaurantDTO);

        // When/Then
        mockMvc.perform(get("/api/restaurants/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Restaurant"));
    }

    @Test
    @WithMockUser(roles = "VERIFIED_USER")
    void createRestaurant_WithValidData_ShouldReturnCreated() throws Exception {
        // Given
        when(securityUtil.getCurrentUserId()).thenReturn(1L);
        when(restaurantService.createRestaurant(any(), eq(1L))).thenReturn(testRestaurantDTO);

        // When/Then
        mockMvc.perform(post("/api/restaurants")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Restaurant"));
    }

    @Test
    @WithMockUser(roles = "VERIFIED_USER")
    void createRestaurant_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        // Given - create request with missing required fields
        RestaurantCreateRequestDTO invalidRequest = new RestaurantCreateRequestDTO();

        // When/Then
        mockMvc.perform(post("/api/restaurants")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createRestaurant_WithoutAuthentication_ShouldReturnForbidden() throws Exception {
        // When/Then
        mockMvc.perform(post("/api/restaurants")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void createRestaurant_WithUnverifiedUser_ShouldReturnForbidden() throws Exception {
        // When/Then
        mockMvc.perform(post("/api/restaurants")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "VERIFIED_USER")
    void updateRestaurant_WithValidData_ShouldReturnUpdated() throws Exception {
        // Given
        when(securityUtil.getCurrentUserId()).thenReturn(1L);
        testRestaurantDTO.setName("Updated Restaurant");
        when(restaurantService.updateRestaurant(eq(1L), any(), eq(1L)))
                .thenReturn(testRestaurantDTO);

        createRequest.setName("Updated Restaurant");

        // When/Then
        mockMvc.perform(put("/api/restaurants/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Restaurant"));
    }

    @Test
    @WithMockUser(roles = "VERIFIED_USER")
    void deleteRestaurant_ShouldReturnNoContent() throws Exception {
        // Given
        when(securityUtil.getCurrentUserId()).thenReturn(1L);

        // When/Then
        mockMvc.perform(delete("/api/restaurants/1")
                .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "john@example.com")
    void getMyRestaurants_ShouldReturnUserRestaurants() throws Exception {
        // Given
        when(securityUtil.getCurrentUserId()).thenReturn(1L);
        when(restaurantService.getRestaurantsByOwner(1L))
                .thenReturn(List.of(testRestaurantDTO));

        // When/Then
        mockMvc.perform(get("/api/restaurants/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test Restaurant"));
    }
}