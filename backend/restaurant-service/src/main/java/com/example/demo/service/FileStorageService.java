package com.example.demo.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Interface for file storage operations.
 * Implementations can use local storage, cloud storage (S3, Cloudinary), etc.
 */
public interface FileStorageService {

    /**
     * Store a file and return its URL
     *
     * @param file The file to store
     * @param folder Optional folder/prefix for organization
     * @return The URL where the file can be accessed
     * @throws IOException if storage fails
     */
    String storeFile(MultipartFile file, String folder) throws IOException;

    /**
     * Delete a file by its URL
     *
     * @param fileUrl The URL of the file to delete
     * @throws IOException if deletion fails
     */
    void deleteFile(String fileUrl) throws IOException;

    /**
     * Check if a file type is allowed
     *
     * @param contentType The MIME type of the file
     * @return true if allowed, false otherwise
     */
    boolean isValidImageType(String contentType);

    /**
     * Validate file size
     *
     * @param size File size in bytes
     * @return true if size is acceptable, false otherwise
     */
    boolean isValidFileSize(long size);
}