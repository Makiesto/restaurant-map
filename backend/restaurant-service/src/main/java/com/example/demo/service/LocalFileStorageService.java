package com.example.demo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Local file system storage implementation.
 * Stores files in a local directory and serves them via HTTP.
 */
@Service
@Slf4j
@ConditionalOnProperty(name = "storage.type", havingValue = "local")
public class LocalFileStorageService implements FileStorageService {

    private final Path uploadPath;
    private final String baseUrl;

    private static final List<String> ALLOWED_TYPES = Arrays.asList(
        "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    public LocalFileStorageService(
            @Value("${upload.path:uploads/}") String uploadPath,
            @Value("${upload.base-url:http://localhost:8080/uploads/}") String baseUrl) {
        this.uploadPath = Paths.get(uploadPath).toAbsolutePath().normalize();
        this.baseUrl = baseUrl;

        try {
            Files.createDirectories(this.uploadPath);
            log.info("Upload directory created/verified at: {}", this.uploadPath);
        } catch (IOException e) {
            log.error("Could not create upload directory!", e);
            throw new RuntimeException("Could not create upload directory!", e);
        }
    }

    @Override
    public String storeFile(MultipartFile file, String folder) throws IOException {
        // Validate file
        if (file.isEmpty()) {
            throw new IOException("Cannot store empty file");
        }

        if (!isValidImageType(file.getContentType())) {
            throw new IOException("Invalid file type. Only images are allowed.");
        }

        if (!isValidFileSize(file.getSize())) {
            throw new IOException("File size exceeds maximum allowed size (10MB)");
        }

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String filename = UUID.randomUUID().toString() + extension;

        // Create folder path if specified
        Path targetLocation;
        if (folder != null && !folder.isEmpty()) {
            Path folderPath = this.uploadPath.resolve(folder);
            Files.createDirectories(folderPath);
            targetLocation = folderPath.resolve(filename);
        } else {
            targetLocation = this.uploadPath.resolve(filename);
        }

        // Copy file to target location
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        log.info("File stored successfully: {}", filename);

        // Return URL
        String relativePath = this.uploadPath.relativize(targetLocation).toString().replace("\\", "/");
        return baseUrl + relativePath;
    }

    @Override
    public void deleteFile(String fileUrl) throws IOException {
        if (fileUrl == null || !fileUrl.startsWith(baseUrl)) {
            log.warn("Cannot delete file: invalid URL {}", fileUrl);
            return;
        }

        // Extract relative path from URL
        String relativePath = fileUrl.substring(baseUrl.length());
        Path filePath = this.uploadPath.resolve(relativePath).normalize();

        // Security check: ensure file is within upload directory
        if (!filePath.startsWith(this.uploadPath)) {
            throw new IOException("Cannot delete file outside upload directory");
        }

        // Delete file if exists
        if (Files.exists(filePath)) {
            Files.delete(filePath);
            log.info("File deleted successfully: {}", relativePath);
        } else {
            log.warn("File not found for deletion: {}", relativePath);
        }
    }

    @Override
    public boolean isValidImageType(String contentType) {
        return contentType != null && ALLOWED_TYPES.contains(contentType.toLowerCase());
    }

    @Override
    public boolean isValidFileSize(long size) {
        return size > 0 && size <= MAX_FILE_SIZE;
    }
}