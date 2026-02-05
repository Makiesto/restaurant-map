package com.example.demo.controller;

import com.example.demo.service.FileStorageService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class FileUploadController {

    private final FileStorageService fileStorageService;

    /**
     * Upload an image file
     *
     * @param file The image file to upload
     * @param folder Optional folder for organization (e.g., "restaurants", "dishes")
     * @return Response containing the file URL and metadata
     */
    @PostMapping("/image")
    public ResponseEntity<?> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", required = false) String folder) {

        log.info("Upload request received - File: {}, Size: {}, Type: {}, Folder: {}",
                file.getOriginalFilename(), file.getSize(), file.getContentType(), folder);

        try {
            // Validate file
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(new ErrorResponse("File is empty"));
            }

            if (!fileStorageService.isValidImageType(file.getContentType())) {
                return ResponseEntity.badRequest()
                    .body(new ErrorResponse("Invalid file type. Only images (JPEG, PNG, GIF, WebP) are allowed."));
            }

            if (!fileStorageService.isValidFileSize(file.getSize())) {
                return ResponseEntity.badRequest()
                    .body(new ErrorResponse("File size exceeds maximum allowed size (10MB)"));
            }

            // Store file
            String fileUrl = fileStorageService.storeFile(file, folder);

            // Build response
            FileUploadResponse response = FileUploadResponse.builder()
                .url(fileUrl)
                .fileName(file.getOriginalFilename())
                .fileSize(file.getSize())
                .contentType(file.getContentType())
                .build();

            log.info("File uploaded successfully: {}", fileUrl);
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            log.error("Error uploading file: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Failed to upload file: " + e.getMessage()));
        }
    }

    /**
     * Delete an uploaded image
     *
     * @param url The URL of the file to delete
     * @return 204 No Content on success
     */
    @DeleteMapping("/image")
    public ResponseEntity<?> deleteImage(@RequestParam("url") String url) {
        log.info("Delete request received for URL: {}", url);

        try {
            fileStorageService.deleteFile(url);
            log.info("File deleted successfully: {}", url);
            return ResponseEntity.noContent().build();

        } catch (IOException e) {
            log.error("Error deleting file: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Failed to delete file: " + e.getMessage()));
        }
    }

    /**
     * Response DTO for successful uploads
     */
    @Data
    @Builder
    @AllArgsConstructor
    public static class FileUploadResponse {
        private String url;
        private String fileName;
        private Long fileSize;
        private String contentType;
    }

    /**
     * Error response DTO
     */
    @Data
    @AllArgsConstructor
    public static class ErrorResponse {
        private String message;
    }
}