package com.example.demo.controller;

import com.example.demo.dto.user.*;
import com.example.demo.entity.Role;
import com.example.demo.service.UserService;
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

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private SecurityUtil securityUtil;

    private UserResponseDTO testUserDTO;
    private UserRegistrationRequestDTO registrationRequest;
    private UpdateProfileRequestDTO updateRequest;
    private ChangePasswordRequestDTO changePasswordRequest;

    @BeforeEach
    void setUp() {
        testUserDTO = UserResponseDTO.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .phoneNumber("+48123456789")
                .role(Role.USER)
                .isActive(true)
                .emailVerified(false)
                .createdAt(LocalDateTime.now())
                .build();

        registrationRequest = new UserRegistrationRequestDTO();
        registrationRequest.setFirstName("Jane");
        registrationRequest.setLastName("Smith");
        registrationRequest.setEmail("jane@example.com");
        registrationRequest.setPassword("SecurePassword123!");
        registrationRequest.setPhoneNumber("+48987654321");

        updateRequest = new UpdateProfileRequestDTO();
        updateRequest.setEmail("newemail@example.com");
        updateRequest.setPhoneNumber("+48111222333");

        changePasswordRequest = new ChangePasswordRequestDTO();
        changePasswordRequest.setCurrentPassword("OldPassword123!");
        changePasswordRequest.setNewPassword("NewPassword123!");
    }

    @Test
    void registerUser_WithValidData_ShouldReturnCreated() throws Exception {
        // Given
        when(userService.registerUser(any())).thenReturn(testUserDTO);

        // When/Then
        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    void registerUser_WithInvalidEmail_ShouldReturnBadRequest() throws Exception {
        // Given
        registrationRequest.setEmail("invalid-email");

        // When/Then
        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerUser_WithShortPassword_ShouldReturnBadRequest() throws Exception {
        // Given
        registrationRequest.setPassword("short");

        // When/Then
        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerUser_WithMissingFields_ShouldReturnBadRequest() throws Exception {
        // Given
        UserRegistrationRequestDTO invalidRequest = new UserRegistrationRequestDTO();
        // Missing all required fields

        // When/Then
        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void getMyProfile_ShouldReturnCurrentUser() throws Exception {
        // Given
        when(securityUtil.getCurrentUserId()).thenReturn(1L);
        when(userService.getUserById(1L)).thenReturn(testUserDTO);

        // When/Then
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    void getMyProfile_WithoutAuthentication_ShouldReturnForbidden() throws Exception {
        // When/Then
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_WithAdminRole_ShouldReturnUsers() throws Exception {
        // Given
        when(userService.getAllUsers()).thenReturn(List.of(testUserDTO));

        // When/Then
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].email").value("john@example.com"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAllUsers_WithUserRole_ShouldReturnForbidden() throws Exception {
        // When/Then
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUserById_WithAdminRole_ShouldReturnUser() throws Exception {
        // Given
        when(userService.getUserById(1L)).thenReturn(testUserDTO);

        // When/Then
        mockMvc.perform(get("/api/admin/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser
    void updateProfile_WithValidData_ShouldReturnUpdated() throws Exception {
        // Given
        when(securityUtil.getCurrentUserEmail()).thenReturn("john@example.com");
        testUserDTO.setEmail("newemail@example.com");
        when(userService.updateProfileByEmail(eq("john@example.com"), any()))
                .thenReturn(testUserDTO);

        // When/Then
        mockMvc.perform(put("/api/users/me")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("newemail@example.com"));
    }

    @Test
    @WithMockUser
    void changePassword_WithValidData_ShouldReturnNoContent() throws Exception {
        // Given
        when(securityUtil.getCurrentUserEmail()).thenReturn("john@example.com");

        // When/Then
        mockMvc.perform(put("/api/users/me/password")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(changePasswordRequest)))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    void changePassword_WithMissingCurrentPassword_ShouldReturnBadRequest() throws Exception {
        // Given
        ChangePasswordRequestDTO invalidRequest = new ChangePasswordRequestDTO();
        invalidRequest.setNewPassword("NewPassword123!");
        // Missing currentPassword

        // When/Then
        mockMvc.perform(put("/api/users/me/password")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void verifyUser_WithAdminRole_ShouldReturnVerified() throws Exception {
        // Given
        testUserDTO.setRole(Role.VERIFIED_USER);
        when(userService.verifyUser(1L)).thenReturn(testUserDTO);

        // When/Then
        mockMvc.perform(put("/api/admin/users/1/verify")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("VERIFIED_USER"));
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
    @WithMockUser(roles = "ADMIN")
    void deleteUser_WithAdminRole_ShouldReturnNoContent() throws Exception {
        // When/Then
        mockMvc.perform(delete("/api/admin/users/1")
                .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "USER")
    void deleteUser_WithUserRole_ShouldReturnForbidden() throws Exception {
        // When/Then
        mockMvc.perform(delete("/api/admin/users/1")
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void deleteMyAccount_ShouldReturnNoContent() throws Exception {
        // Given
        when(securityUtil.getCurrentUserId()).thenReturn(1L);

        // When/Then
        mockMvc.perform(delete("/api/users/me")
                .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    void registerUser_WithInvalidPhoneNumber_ShouldReturnBadRequest() throws Exception {
        // Given
        registrationRequest.setPhoneNumber("invalid");

        // When/Then
        mockMvc.perform(post("/api/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationRequest)))
                .andExpect(status().isBadRequest());
    }
}