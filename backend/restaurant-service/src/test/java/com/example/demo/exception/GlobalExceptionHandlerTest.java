package com.example.demo.exception;

import com.example.demo.controller.RestaurantController;
import com.example.demo.dto.restaurant.RestaurantCreateRequestDTO;
import com.example.demo.security.SecurityUtil;
import com.example.demo.service.RestaurantService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private RestaurantService restaurantService;

    @MockitoBean
    private SecurityUtil securityUtil;

    private RestaurantCreateRequestDTO createRequest;

    @BeforeEach
    void setUp() {
        createRequest = new RestaurantCreateRequestDTO();
        createRequest.setName("Test Restaurant");
        createRequest.setAddress("123 Test St");
    }

    @Test
    void handleResourceNotFoundException_ShouldReturn404() throws Exception {
        // Given
        when(restaurantService.getRestaurantById(999L))
                .thenThrow(new ResourceNotFoundException("Restaurant not found with ID: 999"));

        // When/Then
        mockMvc.perform(get("/api/restaurants/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Restaurant not found with ID: 999"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").exists());
    }

    @Test
    @WithMockUser(roles = "VERIFIED_USER")
    void handleUnauthorizedException_ShouldReturn403() throws Exception {
        // Given
        when(securityUtil.getCurrentUserId()).thenReturn(1L);
        when(restaurantService.createRestaurant(any(), eq(1L)))
                .thenThrow(new UnauthorizedException("Only verified users can create restaurants"));

        // When/Then
        mockMvc.perform(post("/api/restaurants")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value("Only verified users can create restaurants"));
    }

    @Test
    @WithMockUser(roles = "VERIFIED_USER")
    void handleValidationException_ShouldReturn400() throws Exception {
        // Given
        when(securityUtil.getCurrentUserId()).thenReturn(1L);
        when(restaurantService.createRestaurant(any(), eq(1L)))
                .thenThrow(new ValidationException("Invalid restaurant data"));

        // When/Then
        mockMvc.perform(post("/api/restaurants")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Error"))
                .andExpect(jsonPath("$.message").value("Invalid restaurant data"));
    }

    @Test
    @WithMockUser(roles = "VERIFIED_USER")
    void handleGeocodingException_ShouldReturn400() throws Exception {
        // Given
        when(securityUtil.getCurrentUserId()).thenReturn(1L);
        when(restaurantService.createRestaurant(any(), eq(1L)))
                .thenThrow(new GeocodingException("Failed to geocode address"));

        // When/Then
        mockMvc.perform(post("/api/restaurants")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Geocoding Error"))
                .andExpect(jsonPath("$.message").value("Failed to geocode address"));
    }

    @Test
    @WithMockUser(roles = "VERIFIED_USER")
    void handleMethodArgumentNotValid_ShouldReturn400WithFieldErrors() throws Exception {
        // Given - Invalid request with missing required fields
        RestaurantCreateRequestDTO invalidRequest = new RestaurantCreateRequestDTO();
        // name and address are null (violate @NotBlank)

        // When/Then
        mockMvc.perform(post("/api/restaurants")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.message").value("Invalid input data"))
                .andExpect(jsonPath("$.fieldErrors").exists())
                .andExpect(jsonPath("$.fieldErrors.name").exists())
                .andExpect(jsonPath("$.fieldErrors.address").exists());
    }

    @Test
    @WithMockUser(roles = "VERIFIED_USER")
    void handleUnexpectedException_ShouldReturn500() throws Exception {
        // Given
        when(securityUtil.getCurrentUserId()).thenReturn(1L);
        when(restaurantService.createRestaurant(any(), eq(1L)))
                .thenThrow(new RuntimeException("Unexpected error"));

        // When/Then
        mockMvc.perform(post("/api/restaurants")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred. Please try again later."));
    }

    @Test
    @WithMockUser(roles = "VERIFIED_USER")
    void handleValidationException_WithMultipleErrors_ShouldReturnAllErrors() throws Exception {
        // Given - Request with multiple validation errors
        RestaurantCreateRequestDTO invalidRequest = new RestaurantCreateRequestDTO();
        invalidRequest.setName(""); // Empty (violates @NotBlank)
        invalidRequest.setAddress(""); // Empty (violates @NotBlank)

        // When/Then
        mockMvc.perform(post("/api/restaurants")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.name").exists())
                .andExpect(jsonPath("$.fieldErrors.address").exists());
    }

    @Test
    void handleResourceNotFoundException_ShouldIncludeCorrectPath() throws Exception {
        // Given
        when(restaurantService.getRestaurantById(999L))
                .thenThrow(new ResourceNotFoundException("Not found"));

        // When/Then
        mockMvc.perform(get("/api/restaurants/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.path").value("/api/restaurants/999"));
    }

    @Test
    void handleException_ShouldNotExposeInternalDetails() throws Exception {
        // Given
        when(restaurantService.getRestaurantById(1L))
                .thenThrow(new RuntimeException("Internal database connection failed with password xyz123"));

        // When/Then
        mockMvc.perform(get("/api/restaurants/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("An unexpected error occurred. Please try again later."))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("password")
                )))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("xyz123")
                )));
    }
}