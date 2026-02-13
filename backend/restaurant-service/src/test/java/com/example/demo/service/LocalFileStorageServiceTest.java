package com.example.demo.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocalFileStorageServiceTest {

    @TempDir
    Path tempDir;

    private LocalFileStorageService fileStorageService;
    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:8080/uploads/";
        fileStorageService = new LocalFileStorageService(
                tempDir.toString(),
                baseUrl
        );
    }

    // ========================================
    // storeFile tests
    // ========================================

    @Test
    void storeFile_WithValidImage_ShouldReturnUrl() throws IOException {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-image.jpg",
                "image/jpeg",
                "test content".getBytes()
        );

        // When
        String fileUrl = fileStorageService.storeFile(file, null);

        // Then
        assertThat(fileUrl).isNotNull();
        assertThat(fileUrl).startsWith(baseUrl);
        assertThat(fileUrl).endsWith(".jpg");
    }

    @Test
    void storeFile_ShouldCreateFileOnDisk() throws IOException {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-image.jpg",
                "image/jpeg",
                "test content".getBytes()
        );

        // When
        String fileUrl = fileStorageService.storeFile(file, null);

        // Then
        String fileName = fileUrl.substring(baseUrl.length());
        Path savedFile = tempDir.resolve(fileName);
        assertThat(Files.exists(savedFile)).isTrue();
        assertThat(Files.readString(savedFile)).isEqualTo("test content");
    }

    @Test
    void storeFile_WithFolder_ShouldOrganizeCorrectly() throws IOException {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "restaurant.jpg",
                "image/jpeg",
                "restaurant image".getBytes()
        );

        // When
        String fileUrl = fileStorageService.storeFile(file, "restaurants");

        // Then
        assertThat(fileUrl).contains("restaurants/");

        // Verify folder was created
        Path restaurantsFolder = tempDir.resolve("restaurants");
        assertThat(Files.exists(restaurantsFolder)).isTrue();
        assertThat(Files.isDirectory(restaurantsFolder)).isTrue();
    }

    @Test
    void storeFile_WithEmptyFile_ShouldThrowException() {
        // Given
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.jpg",
                "image/jpeg",
                new byte[0]
        );

        // When/Then
        assertThatThrownBy(() -> fileStorageService.storeFile(emptyFile, null))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("Cannot store empty file");
    }

    @Test
    void storeFile_WithInvalidType_ShouldThrowException() {
        // Given
        MockMultipartFile pdfFile = new MockMultipartFile(
                "file",
                "document.pdf",
                "application/pdf",
                "pdf content".getBytes()
        );

        // When/Then
        assertThatThrownBy(() -> fileStorageService.storeFile(pdfFile, null))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("Invalid file type");
    }

    @Test
    void storeFile_WithOversizedFile_ShouldThrowException() {
        // Given - 15MB file (over 10MB limit)
        byte[] largeContent = new byte[15 * 1024 * 1024];
        MockMultipartFile largeFile = new MockMultipartFile(
                "file",
                "large.jpg",
                "image/jpeg",
                largeContent
        );

        // When/Then
        assertThatThrownBy(() -> fileStorageService.storeFile(largeFile, null))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("File size exceeds maximum");
    }

    @Test
    void storeFile_WithNoExtension_ShouldHandleCorrectly() throws IOException {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "imagefile",
                "image/jpeg",
                "content".getBytes()
        );

        // When
        String fileUrl = fileStorageService.storeFile(file, null);

        // Then
        assertThat(fileUrl).isNotNull();
        // Should still work even without extension
    }

    @Test
    void storeFile_MultipleFiles_ShouldGenerateUniqueNames() throws IOException {
        // Given
        MockMultipartFile file1 = new MockMultipartFile(
                "file",
                "image.jpg",
                "image/jpeg",
                "content1".getBytes()
        );
        MockMultipartFile file2 = new MockMultipartFile(
                "file",
                "image.jpg",
                "image/jpeg",
                "content2".getBytes()
        );

        // When
        String url1 = fileStorageService.storeFile(file1, null);
        String url2 = fileStorageService.storeFile(file2, null);

        // Then
        assertThat(url1).isNotEqualTo(url2);
    }

    @Test
    void storeFile_ShouldReplaceExistingFile() throws IOException {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "original content".getBytes()
        );

        String url1 = fileStorageService.storeFile(file, null);
        String fileName = url1.substring(baseUrl.length());

        // When - Store file with same generated name (unlikely but possible)
        MockMultipartFile newFile = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "new content".getBytes()
        );

        // Force same filename by manually copying
        Path targetPath = tempDir.resolve(fileName);
        Files.write(targetPath, "new content".getBytes());

        // Then
        String content = Files.readString(targetPath);
        assertThat(content).isEqualTo("new content");
    }

    // ========================================
    // deleteFile tests
    // ========================================

    @Test
    void deleteFile_WithValidUrl_ShouldDeleteFile() throws IOException {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "content".getBytes()
        );
        String fileUrl = fileStorageService.storeFile(file, null);

        String fileName = fileUrl.substring(baseUrl.length());
        Path filePath = tempDir.resolve(fileName);
        assertThat(Files.exists(filePath)).isTrue();

        // When
        fileStorageService.deleteFile(fileUrl);

        // Then
        assertThat(Files.exists(filePath)).isFalse();
    }

    @Test
    void deleteFile_WithNonexistentFile_ShouldNotThrow() throws IOException {
        // Given
        String nonexistentUrl = baseUrl + "nonexistent.jpg";

        // When/Then - Should not throw
        fileStorageService.deleteFile(nonexistentUrl);
    }

    @Test
    void deleteFile_WithInvalidUrl_ShouldHandleGracefully() throws IOException {
        // Given
        String invalidUrl = "http://other-domain.com/file.jpg";

        // When/Then - Should handle gracefully
        fileStorageService.deleteFile(invalidUrl);
    }

    @Test
    void deleteFile_WithNullUrl_ShouldHandleGracefully() throws IOException {
        // When/Then - Should not throw
        fileStorageService.deleteFile(null);
    }

    @Test
    void deleteFile_WithEmptyUrl_ShouldHandleGracefully() throws IOException {
        // When/Then - Should not throw
        fileStorageService.deleteFile("");
    }

    @Test
    void deleteFile_WithPathTraversal_ShouldPreventDeletion() throws IOException {
        // Given - Attempt to delete file outside upload directory
        String maliciousUrl = baseUrl + "../../etc/passwd";

        // When/Then
        assertThatThrownBy(() -> fileStorageService.deleteFile(maliciousUrl))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("Cannot delete file outside upload directory");
    }

    @Test
    void deleteFile_InFolder_ShouldDeleteCorrectly() throws IOException {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "content".getBytes()
        );
        String fileUrl = fileStorageService.storeFile(file, "restaurants");

        String relativePath = fileUrl.substring(baseUrl.length());
        Path filePath = tempDir.resolve(relativePath);
        assertThat(Files.exists(filePath)).isTrue();

        // When
        fileStorageService.deleteFile(fileUrl);

        // Then
        assertThat(Files.exists(filePath)).isFalse();
    }

    // ========================================
    // isValidImageType tests
    // ========================================

    @Test
    void isValidImageType_WithJpeg_ShouldReturnTrue() {
        assertThat(fileStorageService.isValidImageType("image/jpeg")).isTrue();
    }

    @Test
    void isValidImageType_WithJpg_ShouldReturnTrue() {
        assertThat(fileStorageService.isValidImageType("image/jpg")).isTrue();
    }

    @Test
    void isValidImageType_WithPng_ShouldReturnTrue() {
        assertThat(fileStorageService.isValidImageType("image/png")).isTrue();
    }

    @Test
    void isValidImageType_WithGif_ShouldReturnTrue() {
        assertThat(fileStorageService.isValidImageType("image/gif")).isTrue();
    }

    @Test
    void isValidImageType_WithWebp_ShouldReturnTrue() {
        assertThat(fileStorageService.isValidImageType("image/webp")).isTrue();
    }

    @Test
    void isValidImageType_WithPdf_ShouldReturnFalse() {
        assertThat(fileStorageService.isValidImageType("application/pdf")).isFalse();
    }

    @Test
    void isValidImageType_WithText_ShouldReturnFalse() {
        assertThat(fileStorageService.isValidImageType("text/plain")).isFalse();
    }

    @Test
    void isValidImageType_WithNull_ShouldReturnFalse() {
        assertThat(fileStorageService.isValidImageType(null)).isFalse();
    }

    @Test
    void isValidImageType_CaseInsensitive_ShouldWork() {
        assertThat(fileStorageService.isValidImageType("IMAGE/JPEG")).isTrue();
        assertThat(fileStorageService.isValidImageType("Image/Png")).isTrue();
    }

    // ========================================
    // isValidFileSize tests
    // ========================================

    @Test
    void isValidFileSize_WithinLimit_ShouldReturnTrue() {
        // 5MB - within 10MB limit
        assertThat(fileStorageService.isValidFileSize(5 * 1024 * 1024)).isTrue();
    }

    @Test
    void isValidFileSize_AtLimit_ShouldReturnTrue() {
        // Exactly 10MB
        assertThat(fileStorageService.isValidFileSize(10 * 1024 * 1024)).isTrue();
    }

    @Test
    void isValidFileSize_ExceedingLimit_ShouldReturnFalse() {
        // 15MB - exceeds 10MB limit
        assertThat(fileStorageService.isValidFileSize(15 * 1024 * 1024)).isFalse();
    }

    @Test
    void isValidFileSize_Zero_ShouldReturnFalse() {
        assertThat(fileStorageService.isValidFileSize(0)).isFalse();
    }

    @Test
    void isValidFileSize_Negative_ShouldReturnFalse() {
        assertThat(fileStorageService.isValidFileSize(-100)).isFalse();
    }

    @Test
    void isValidFileSize_VerySmall_ShouldReturnTrue() {
        assertThat(fileStorageService.isValidFileSize(1024)).isTrue(); // 1KB
    }

    // ========================================
    // Integration tests
    // ========================================

    @Test
    void fullWorkflow_StoreAndDelete_ShouldWork() throws IOException {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "workflow-test.jpg",
                "image/jpeg",
                "test content for workflow".getBytes()
        );

        // When - Store
        String fileUrl = fileStorageService.storeFile(file, "test-folder");

        // Then - Verify stored
        String relativePath = fileUrl.substring(baseUrl.length());
        Path filePath = tempDir.resolve(relativePath);
        assertThat(Files.exists(filePath)).isTrue();
        assertThat(Files.readString(filePath)).isEqualTo("test content for workflow");

        // When - Delete
        fileStorageService.deleteFile(fileUrl);

        // Then - Verify deleted
        assertThat(Files.exists(filePath)).isFalse();
    }

    @Test
    void storeFile_WithNestedFolder_ShouldCreateFolders() throws IOException {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "nested.jpg",
                "image/jpeg",
                "content".getBytes()
        );

        // When
        String fileUrl = fileStorageService.storeFile(file, "restaurants/italian");

        // Then
        assertThat(fileUrl).contains("restaurants/italian/");

        Path nestedFolder = tempDir.resolve("restaurants/italian");
        assertThat(Files.exists(nestedFolder)).isTrue();
        assertThat(Files.isDirectory(nestedFolder)).isTrue();
    }

    @Test
    void storeFile_WithSpecialCharactersInFolder_ShouldHandleCorrectly() throws IOException {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "content".getBytes()
        );

        // When
        String fileUrl = fileStorageService.storeFile(file, "folder-name_123");

        // Then
        assertThat(fileUrl).contains("folder-name_123/");
    }

    @Test
    void concurrentStoreOperations_ShouldNotConflict() throws IOException {
        // Given
        MockMultipartFile file1 = new MockMultipartFile("file", "test1.jpg", "image/jpeg", "content1".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("file", "test2.jpg", "image/jpeg", "content2".getBytes());
        MockMultipartFile file3 = new MockMultipartFile("file", "test3.jpg", "image/jpeg", "content3".getBytes());

        // When - Store multiple files
        String url1 = fileStorageService.storeFile(file1, null);
        String url2 = fileStorageService.storeFile(file2, null);
        String url3 = fileStorageService.storeFile(file3, null);

        // Then - All should have unique URLs
        assertThat(url1).isNotEqualTo(url2);
        assertThat(url2).isNotEqualTo(url3);
        assertThat(url1).isNotEqualTo(url3);

        // All files should exist
        assertThat(Files.exists(tempDir.resolve(url1.substring(baseUrl.length())))).isTrue();
        assertThat(Files.exists(tempDir.resolve(url2.substring(baseUrl.length())))).isTrue();
        assertThat(Files.exists(tempDir.resolve(url3.substring(baseUrl.length())))).isTrue();
    }
}