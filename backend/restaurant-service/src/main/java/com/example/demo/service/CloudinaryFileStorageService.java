package com.example.demo.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Cloudinary cloud storage implementation.
 * Requires Cloudinary credentials to be configured.
 *
 * To use this service, set in application.properties:
 * storage.type=cloudinary
 * cloudinary.cloud-name=your-cloud-name
 * cloudinary.api-key=your-api-key
 * cloudinary.api-secret=your-api-secret
 */
@Service
@Slf4j
@ConditionalOnProperty(name = "storage.type", havingValue = "cloudinary")
public class CloudinaryFileStorageService implements FileStorageService {

    private final Cloudinary cloudinary;

    private static final List<String> ALLOWED_TYPES = Arrays.asList(
        "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    public CloudinaryFileStorageService(
            @Value("${cloudinary.cloud-name}") String cloudName,
            @Value("${cloudinary.api-key}") String apiKey,
            @Value("${cloudinary.api-secret}") String apiSecret) {

        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
            "cloud_name", cloudName,
            "api_key", apiKey,
            "api_secret", apiSecret,
            "secure", true
        ));

        log.info("Cloudinary service initialized with cloud: {}", cloudName);
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

        // Generate unique public ID
        String publicId = UUID.randomUUID().toString();
        if (folder != null && !folder.isEmpty()) {
            publicId = folder + "/" + publicId;
        }

        // Upload to Cloudinary
        Map<String, Object> uploadParams = ObjectUtils.asMap(
            "public_id", publicId,
            "folder", folder != null ? folder : "restaurant-map",
            "resource_type", "image",
            "transformation", new Transformation<>().quality("auto").fetchFormat("auto")
        );

        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);

        String imageUrl = (String) uploadResult.get("secure_url");
        log.info("File uploaded to Cloudinary: {}", imageUrl);

        return imageUrl;
    }

    @Override
    public void deleteFile(String fileUrl) throws IOException {
        if (fileUrl == null || fileUrl.isEmpty()) {
            log.warn("Cannot delete file: empty URL");
            return;
        }

        try {
            // Extract public ID from Cloudinary URL
            String publicId = extractPublicIdFromUrl(fileUrl);

            if (publicId != null) {
                Map result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
                log.info("File deleted from Cloudinary: {} - Result: {}", publicId, result.get("result"));
            }
        } catch (Exception e) {
            log.error("Error deleting file from Cloudinary: {}", fileUrl, e);
            throw new IOException("Failed to delete file from Cloudinary", e);
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

    /**
     * Extract Cloudinary public ID from URL
     * Example: https://res.cloudinary.com/demo/image/upload/v1234567/folder/abc123.jpg
     * Returns: folder/abc123
     */
    private String extractPublicIdFromUrl(String url) {
        try {
            // Cloudinary URL format: .../upload/v{version}/{public_id}.{format}
            String[] parts = url.split("/upload/");
            if (parts.length < 2) return null;

            String afterUpload = parts[1];
            // Remove version number (v1234567/)
            int firstSlash = afterUpload.indexOf('/');
            if (firstSlash == -1) return null;

            String publicIdWithExtension = afterUpload.substring(firstSlash + 1);
            // Remove file extension
            int lastDot = publicIdWithExtension.lastIndexOf('.');
            if (lastDot > 0) {
                return publicIdWithExtension.substring(0, lastDot);
            }

            return publicIdWithExtension;
        } catch (Exception e) {
            log.error("Error extracting public ID from URL: {}", url, e);
            return null;
        }
    }
}
