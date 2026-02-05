package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

/**
 * Configuration for serving uploaded files as static resources.
 * This allows files in the uploads/ directory to be accessed via HTTP.
 */
@Configuration
public class FileUploadConfig implements WebMvcConfigurer {

    @Value("${upload.path:uploads/}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve uploaded files
        String absolutePath = Paths.get(uploadPath).toAbsolutePath().normalize().toString();
        String resourceLocation = "file:" + absolutePath + "/";

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(resourceLocation)
                .setCachePeriod(3600); // Cache for 1 hour

        // Allow CORS for uploaded images
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(resourceLocation)
                .resourceChain(true);
    }
}