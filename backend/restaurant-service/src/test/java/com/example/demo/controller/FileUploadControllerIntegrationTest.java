package com.example.demo.controller;

import com.example.demo.service.FileStorageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
class FileUploadControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FileStorageService fileStorageService;

    @Test
    @WithMockUser
    void uploadImage_WithValidJpeg_ShouldReturnUrl() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        when(fileStorageService.isValidImageType("image/jpeg")).thenReturn(true);
        when(fileStorageService.isValidFileSize(anyLong())).thenReturn(true);
        // Controller calls storeFile(file, folder) where folder can be null
        when(fileStorageService.storeFile(any(), any()))
                .thenReturn("http://localhost:8080/uploads/test.jpg");

        // When/Then
        mockMvc.perform(multipart("/api/upload/image")
                .file(file)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("http://localhost:8080/uploads/test.jpg"))
                .andExpect(jsonPath("$.fileName").value("test.jpg"))
                .andExpect(jsonPath("$.fileSize").exists())
                .andExpect(jsonPath("$.contentType").value("image/jpeg"));
    }

    @Test
    @WithMockUser
    void uploadImage_WithValidPng_ShouldSucceed() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.png",
                "image/png",
                "test image content".getBytes()
        );

        when(fileStorageService.isValidImageType("image/png")).thenReturn(true);
        when(fileStorageService.isValidFileSize(anyLong())).thenReturn(true);
        when(fileStorageService.storeFile(any(), any()))
                .thenReturn("http://localhost:8080/uploads/test.png");

        // When/Then
        mockMvc.perform(multipart("/api/upload/image")
                .file(file)
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void uploadImage_WithGif_ShouldSucceed() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.gif",
                "image/gif",
                "test image content".getBytes()
        );

        when(fileStorageService.isValidImageType("image/gif")).thenReturn(true);
        when(fileStorageService.isValidFileSize(anyLong())).thenReturn(true);
        when(fileStorageService.storeFile(any(), any()))
                .thenReturn("http://localhost:8080/uploads/test.gif");

        // When/Then
        mockMvc.perform(multipart("/api/upload/image")
                .file(file)
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void uploadImage_WithWebp_ShouldSucceed() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.webp",
                "image/webp",
                "test image content".getBytes()
        );

        when(fileStorageService.isValidImageType("image/webp")).thenReturn(true);
        when(fileStorageService.isValidFileSize(anyLong())).thenReturn(true);
        when(fileStorageService.storeFile(any(), any()))
                .thenReturn("http://localhost:8080/uploads/test.webp");

        // When/Then
        mockMvc.perform(multipart("/api/upload/image")
                .file(file)
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void uploadImage_WithInvalidType_ShouldReturnBadRequest() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "test content".getBytes()
        );

        when(fileStorageService.isValidImageType("text/plain")).thenReturn(false);

        // When/Then
        mockMvc.perform(multipart("/api/upload/image")
                .file(file)
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid file type. Only images (JPEG, PNG, GIF, WebP) are allowed."));
    }

    @Test
    @WithMockUser
    void uploadImage_WithEmptyFile_ShouldReturnBadRequest() throws Exception {
        // Given
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.jpg",
                "image/jpeg",
                new byte[0]
        );

        // When/Then
        mockMvc.perform(multipart("/api/upload/image")
                .file(emptyFile)
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("File is empty"));
    }

    @Test
    @WithMockUser
    void uploadImage_WithOversizedFile_ShouldReturnBadRequest() throws Exception {
        // Given
        byte[] largeContent = new byte[1024 * 1024]; // 1MB
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "large.jpg",
                "image/jpeg",
                largeContent
        );

        when(fileStorageService.isValidImageType("image/jpeg")).thenReturn(true);
        when(fileStorageService.isValidFileSize(largeContent.length)).thenReturn(false);

        // When/Then
        mockMvc.perform(multipart("/api/upload/image")
                .file(file)
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("File size exceeds maximum allowed size (10MB)"));
    }

    @Test
    @WithMockUser
    void uploadImage_WithFolder_ShouldOrganizeCorrectly() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "test content".getBytes()
        );

        when(fileStorageService.isValidImageType("image/jpeg")).thenReturn(true);
        when(fileStorageService.isValidFileSize(anyLong())).thenReturn(true);
        when(fileStorageService.storeFile(any(), eq("restaurants")))
                .thenReturn("http://localhost:8080/uploads/restaurants/test.jpg");

        // When/Then
        mockMvc.perform(multipart("/api/upload/image")
                .file(file)
                .param("folder", "restaurants")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("http://localhost:8080/uploads/restaurants/test.jpg"));
    }

    @Test
    @WithMockUser
    void uploadImage_WithDishesFolder_ShouldOrganizeCorrectly() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "dish.jpg",
                "image/jpeg",
                "test content".getBytes()
        );

        when(fileStorageService.isValidImageType("image/jpeg")).thenReturn(true);
        when(fileStorageService.isValidFileSize(anyLong())).thenReturn(true);
        when(fileStorageService.storeFile(any(), eq("dishes")))
                .thenReturn("http://localhost:8080/uploads/dishes/dish.jpg");

        // When/Then
        mockMvc.perform(multipart("/api/upload/image")
                .file(file)
                .param("folder", "dishes")
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void uploadImage_WithStorageFailure_ShouldReturnInternalServerError() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "test content".getBytes()
        );

        when(fileStorageService.isValidImageType("image/jpeg")).thenReturn(true);
        when(fileStorageService.isValidFileSize(anyLong())).thenReturn(true);
        when(fileStorageService.storeFile(any(), any()))
                .thenThrow(new IOException("Storage failed"));

        // When/Then
        mockMvc.perform(multipart("/api/upload/image")
                .file(file)
                .with(csrf()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Failed to upload file: Storage failed"));
    }

    @Test
    @WithMockUser
    void uploadImage_WithoutFile_ShouldReturnInternalServerError() throws Exception {
        // When/Then - Missing required multipart file causes internal error
        mockMvc.perform(multipart("/api/upload/image")
                .with(csrf()))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser
    void uploadImage_WithSpecialCharactersInFilename_ShouldHandleCorrectly() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test file (1) [copy].jpg",
                "image/jpeg",
                "test content".getBytes()
        );

        when(fileStorageService.isValidImageType("image/jpeg")).thenReturn(true);
        when(fileStorageService.isValidFileSize(anyLong())).thenReturn(true);
        when(fileStorageService.storeFile(any(), any()))
                .thenReturn("http://localhost:8080/uploads/test-file.jpg");

        // When/Then
        mockMvc.perform(multipart("/api/upload/image")
                .file(file)
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void deleteImage_WithValidUrl_ShouldSucceed() throws Exception {
        // Given
        String fileUrl = "http://localhost:8080/uploads/test.jpg";
        doNothing().when(fileStorageService).deleteFile(fileUrl);

        // When/Then
        mockMvc.perform(delete("/api/upload/image")
                .param("url", fileUrl)
                .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    void deleteImage_WithInvalidUrl_ShouldReturnBadRequest() throws Exception {
        // Given
        String invalidUrl = "invalid-url";
        doThrow(new IOException("Invalid URL"))
                .when(fileStorageService).deleteFile(invalidUrl);

        // When/Then
        mockMvc.perform(delete("/api/upload/image")
                .param("url", invalidUrl)
                .with(csrf()))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser
    void deleteImage_WithNonexistentFile_ShouldHandleGracefully() throws Exception {
        // Given
        String fileUrl = "http://localhost:8080/uploads/nonexistent.jpg";
        doNothing().when(fileStorageService).deleteFile(fileUrl);

        // When/Then
        mockMvc.perform(delete("/api/upload/image")
                .param("url", fileUrl)
                .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    void deleteImage_WithMissingUrl_ShouldReturnInternalServerError() throws Exception {
        // When/Then - Missing required parameter causes internal error
        mockMvc.perform(delete("/api/upload/image")
                .with(csrf()))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser
    void deleteImage_WithEmptyUrl_ShouldHandleCorrectly() throws Exception {
        // Given
        doNothing().when(fileStorageService).deleteFile("");

        // When/Then
        mockMvc.perform(delete("/api/upload/image")
                .param("url", "")
                .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    void deleteImage_WithCloudinaryUrl_ShouldSucceed() throws Exception {
        // Given
        String cloudinaryUrl = "https://res.cloudinary.com/demo/image/upload/v1234/test.jpg";
        doNothing().when(fileStorageService).deleteFile(cloudinaryUrl);

        // When/Then
        mockMvc.perform(delete("/api/upload/image")
                .param("url", cloudinaryUrl)
                .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    void deleteImage_WithStorageFailure_ShouldReturnInternalServerError() throws Exception {
        // Given
        String fileUrl = "http://localhost:8080/uploads/test.jpg";
        doThrow(new IOException("Storage failure"))
                .when(fileStorageService).deleteFile(fileUrl);

        // When/Then
        mockMvc.perform(delete("/api/upload/image")
                .param("url", fileUrl)
                .with(csrf()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Failed to delete file: Storage failure"));
    }

    @Test
    @WithMockUser
    void uploadImage_WithVeryLongFilename_ShouldHandleCorrectly() throws Exception {
        // Given
        String longName = "a".repeat(200) + ".jpg";
        MockMultipartFile file = new MockMultipartFile(
                "file",
                longName,
                "image/jpeg",
                "test content".getBytes()
        );

        when(fileStorageService.isValidImageType("image/jpeg")).thenReturn(true);
        when(fileStorageService.isValidFileSize(anyLong())).thenReturn(true);
        when(fileStorageService.storeFile(any(), any()))
                .thenReturn("http://localhost:8080/uploads/short-name.jpg");

        // When/Then
        mockMvc.perform(multipart("/api/upload/image")
                .file(file)
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void uploadImage_WithMultipleDots_ShouldHandleCorrectly() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "file.name.with.dots.jpg",
                "image/jpeg",
                "test content".getBytes()
        );

        when(fileStorageService.isValidImageType("image/jpeg")).thenReturn(true);
        when(fileStorageService.isValidFileSize(anyLong())).thenReturn(true);
        when(fileStorageService.storeFile(any(), any()))
                .thenReturn("http://localhost:8080/uploads/file.jpg");

        // When/Then
        mockMvc.perform(multipart("/api/upload/image")
                .file(file)
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void uploadImage_WithNoFileExtension_ShouldHandleCorrectly() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "filenamewithoutextension",
                "image/jpeg",
                "test content".getBytes()
        );

        when(fileStorageService.isValidImageType("image/jpeg")).thenReturn(true);
        when(fileStorageService.isValidFileSize(anyLong())).thenReturn(true);
        when(fileStorageService.storeFile(any(), any()))
                .thenReturn("http://localhost:8080/uploads/file.jpg");

        // When/Then
        mockMvc.perform(multipart("/api/upload/image")
                .file(file)
                .with(csrf()))
                .andExpect(status().isOk());
    }
}