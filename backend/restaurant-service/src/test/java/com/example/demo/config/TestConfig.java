package com.example.demo.config;

import com.example.demo.security.JwtAuthenticationFilter;
import com.example.demo.service.FileStorageService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Test configuration to provide mock beans for testing
 */
@TestConfiguration
public class TestConfig {

    /**
     * Mock FileStorageService for tests
     */
    @Bean
    @Primary
    public FileStorageService mockFileStorageService() {
        return new FileStorageService() {
            @Override
            public String storeFile(MultipartFile file, String folder) throws IOException {
                return "http://localhost:8080/uploads/test-file.jpg";
            }

            @Override
            public void deleteFile(String fileUrl) throws IOException {
                // Mock implementation - do nothing
            }

            @Override
            public boolean isValidImageType(String contentType) {
                return contentType != null && contentType.startsWith("image/");
            }

            @Override
            public boolean isValidFileSize(long size) {
                return size > 0 && size <= 10 * 1024 * 1024; // 10MB
            }
        };
    }
}