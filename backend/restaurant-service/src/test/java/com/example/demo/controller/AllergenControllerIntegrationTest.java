package com.example.demo.controller;

import com.example.demo.dto.allergen.AllergenResponseDTO;
import com.example.demo.dto.allergen.UpdateUserAllergensRequestDTO;
import com.example.demo.security.SecurityUtil;
import com.example.demo.service.AllergenService;
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
class AllergenControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private AllergenService allergenService;

    @MockitoBean
    private SecurityUtil securityUtil;

    private AllergenResponseDTO glutenAllergen;
    private AllergenResponseDTO nutsAllergen;

    @BeforeEach
    void setUp() {
        glutenAllergen = AllergenResponseDTO.builder()
                .id(1L)
                .name("Gluten")
                .build();

        nutsAllergen = AllergenResponseDTO.builder()
                .id(2L)
                .name("Nuts")
                .build();
    }

    @Test
    void getAllAllergens_ShouldReturnAllAllergens() throws Exception {
        // Given
        when(allergenService.getAllAllergens()).thenReturn(List.of(glutenAllergen, nutsAllergen));

        // When/Then
        mockMvc.perform(get("/api/allergens"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Gluten"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Nuts"));
    }

    @Test
    void getAllAllergens_WithEmptyDatabase_ShouldReturnEmptyList() throws Exception {
        // Given
        when(allergenService.getAllAllergens()).thenReturn(List.of());

        // When/Then
        mockMvc.perform(get("/api/allergens"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getAllAllergens_WithoutAuthentication_ShouldSucceed() throws Exception {
        // Given - Public endpoint should work without authentication
        when(allergenService.getAllAllergens()).thenReturn(List.of(glutenAllergen));

        // When/Then
        mockMvc.perform(get("/api/allergens"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Gluten"));
    }

    @Test
    @WithMockUser
    void getUserAllergens_WithAuthentication_ShouldReturnUserAllergens() throws Exception {
        // Given
        when(securityUtil.getCurrentUserId()).thenReturn(1L);
        when(allergenService.getUserAllergens(1L)).thenReturn(List.of(glutenAllergen));

        // When/Then
        mockMvc.perform(get("/api/users/me/allergens"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Gluten"));
    }

    @Test
    void getUserAllergens_WithoutAuthentication_ShouldReturnForbidden() throws Exception {
        // When/Then
        mockMvc.perform(get("/api/users/me/allergens"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void updateUserAllergens_WithValidData_ShouldSucceed() throws Exception {
        // Given
        when(securityUtil.getCurrentUserId()).thenReturn(1L);
        when(allergenService.updateUserAllergens(eq(1L), any()))
                .thenReturn(List.of(glutenAllergen, nutsAllergen));

        UpdateUserAllergensRequestDTO request = new UpdateUserAllergensRequestDTO();
        request.setAllergenNames(List.of("Gluten", "Nuts"));

        // When/Then
        mockMvc.perform(put("/api/users/me/allergens")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @WithMockUser
    void updateUserAllergens_WithEmptyList_ShouldClearAllergens() throws Exception {
        // Given
        when(securityUtil.getCurrentUserId()).thenReturn(1L);
        when(allergenService.updateUserAllergens(eq(1L), any()))
                .thenReturn(List.of());

        UpdateUserAllergensRequestDTO request = new UpdateUserAllergensRequestDTO();
        request.setAllergenNames(List.of());

        // When/Then
        mockMvc.perform(put("/api/users/me/allergens")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void updateUserAllergens_WithoutAuthentication_ShouldReturnForbidden() throws Exception {
        // Given
        UpdateUserAllergensRequestDTO request = new UpdateUserAllergensRequestDTO();
        request.setAllergenNames(List.of("Gluten"));

        // When/Then
        mockMvc.perform(put("/api/users/me/allergens")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}