package com.example.demo.controller;

import com.example.demo.dto.admin.AdminStatsDTO;
import com.example.demo.dto.admin.VerifyRestaurantRequestDTO;
import com.example.demo.dto.restaurant.RestaurantResponseDTO;
import com.example.demo.dto.user.UserResponseDTO;
import com.example.demo.entity.RestaurantStatus;
import com.example.demo.entity.Role;
import com.example.demo.service.AdminService;
import com.example.demo.service.RestaurantService;
import com.example.demo.service.UserService;
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

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
class AdminControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private AdminService adminService;

    @MockitoBean
    private RestaurantService restaurantService;

    @MockitoBean
    private UserService userService;

    private AdminStatsDTO statsDTO;
    private RestaurantResponseDTO restaurantDTO;
    private UserResponseDTO userDTO;

    @BeforeEach
    void setUp() {
        statsDTO = AdminStatsDTO.builder()
                .totalUsers(100L)
                .totalRestaurants(50L)
                .pendingRestaurants(10L)
                .approvedRestaurants(35L)
                .rejectedRestaurants(5L)
                .verifiedRestaurants(30L)
                .unverifiedRestaurants(20L)
                .totalReviews(200L)
                .unverifiedReviews(50L)
                .build();

        RestaurantResponseDTO.UserDTO ownerDTO = new RestaurantResponseDTO.UserDTO(
                1L, "John", "Doe", "john@example.com"
        );

        restaurantDTO = RestaurantResponseDTO.builder()
                .id(1L)
                .name("Test Restaurant")
                .address("123 Test St")
                .status(RestaurantStatus.PENDING)
                .isVerified(false)
                .owner(ownerDTO)
                .createdAt(LocalDateTime.now())
                .build();

        userDTO = UserResponseDTO.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .role(Role.USER)
                .isActive(true)
                .build();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAdminStats_WithAdminRole_ShouldReturnStats() throws Exception {
        // Given
        when(adminService.getAdminStats()).thenReturn(statsDTO);

        // When/Then
        mockMvc.perform(get("/api/admin/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").value(100))
                .andExpect(jsonPath("$.totalRestaurants").value(50))
                .andExpect(jsonPath("$.pendingRestaurants").value(10))
                .andExpect(jsonPath("$.approvedRestaurants").value(35))
                .andExpect(jsonPath("$.rejectedRestaurants").value(5))
                .andExpect(jsonPath("$.totalReviews").value(200));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAdminStats_WithUserRole_ShouldReturnForbidden() throws Exception {
        // When/Then
        mockMvc.perform(get("/api/admin/stats"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAdminStats_WithoutAuthentication_ShouldReturnForbidden() throws Exception {
        // When/Then
        mockMvc.perform(get("/api/admin/stats"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getPendingRestaurants_ShouldReturnPendingList() throws Exception {
        // Given
        when(restaurantService.getPendingRestaurants()).thenReturn(List.of(restaurantDTO));

        // When/Then
        mockMvc.perform(get("/api/admin/restaurants/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUnverifiedRestaurants_ShouldReturnUnverifiedList() throws Exception {
        // Given
        when(restaurantService.getUnverifiedRestaurants()).thenReturn(List.of(restaurantDTO));

        // When/Then
        mockMvc.perform(get("/api/admin/restaurants/unverified"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].verified").value(false));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void approveRestaurant_ShouldReturnApprovedRestaurant() throws Exception {
        // Given
        restaurantDTO.setStatus(RestaurantStatus.APPROVED);
        when(restaurantService.approveRestaurant(1L)).thenReturn(restaurantDTO);

        // When/Then
        mockMvc.perform(put("/api/admin/restaurants/1/approve")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void rejectRestaurant_ShouldReturnRejectedRestaurant() throws Exception {
        // Given
        restaurantDTO.setStatus(RestaurantStatus.REJECTED);
        when(restaurantService.rejectRestaurant(1L)).thenReturn(restaurantDTO);

        // When/Then
        mockMvc.perform(put("/api/admin/restaurants/1/reject")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("REJECTED"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void verifyRestaurant_WithNotes_ShouldReturnVerifiedRestaurant() throws Exception {
        // Given
        restaurantDTO.setVerified(true);
        restaurantDTO.setVerifiedAt(LocalDateTime.now());

        VerifyRestaurantRequestDTO request = new VerifyRestaurantRequestDTO();
        request.setNotes("Verified manually by admin");

        when(restaurantService.verifyRestaurant(eq(1L), any())).thenReturn(restaurantDTO);

        // When/Then
        mockMvc.perform(put("/api/admin/restaurants/1/verify")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.verified").value(true));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void verifyRestaurant_WithoutNotes_ShouldSucceed() throws Exception {
        // Given
        restaurantDTO.setVerified(true);
        when(restaurantService.verifyRestaurant(eq(1L), any())).thenReturn(restaurantDTO);

        // When/Then
        mockMvc.perform(put("/api/admin/restaurants/1/verify")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verified").value(true));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void unverifyRestaurant_ShouldReturnUnverifiedRestaurant() throws Exception {
        // Given
        restaurantDTO.setVerified(false);
        restaurantDTO.setVerifiedAt(null);
        when(restaurantService.unverifyRestaurant(1L)).thenReturn(restaurantDTO);

        // When/Then
        mockMvc.perform(put("/api/admin/restaurants/1/unverify")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.verified").value(false));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void verifyUser_ShouldReturnVerifiedUser() throws Exception {
        // Given
        userDTO.setRole(Role.VERIFIED_USER);
        userDTO.setVerifiedAt(LocalDateTime.now());
        when(userService.verifyUser(1L)).thenReturn(userDTO);

        // When/Then
        mockMvc.perform(put("/api/admin/users/1/verify")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.role").value("VERIFIED_USER"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_ShouldReturnUserList() throws Exception {
        // Given
        UserResponseDTO user2 = UserResponseDTO.builder()
                .id(2L)
                .firstName("Jane")
                .lastName("Smith")
                .email("jane@example.com")
                .role(Role.USER)
                .build();

        when(userService.getAllUsers()).thenReturn(List.of(userDTO, user2));

        // When/Then
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].email").value("john@example.com"))
                .andExpect(jsonPath("$[1].email").value("jane@example.com"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void approveRestaurant_WithUserRole_ShouldReturnForbidden() throws Exception {
        // When/Then
        mockMvc.perform(put("/api/admin/restaurants/1/approve")
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void verifyUser_WithUserRole_ShouldReturnForbidden() throws Exception {
        // When/Then
        mockMvc.perform(put("/api/admin/users/1/verify")
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "VERIFIED_USER")
    void getAdminStats_WithVerifiedUserRole_ShouldReturnForbidden() throws Exception {
        // When/Then
        mockMvc.perform(get("/api/admin/stats"))
                .andExpect(status().isForbidden());
    }
}