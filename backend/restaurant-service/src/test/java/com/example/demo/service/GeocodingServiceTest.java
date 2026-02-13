package com.example.demo.service;

import com.example.demo.exception.GeocodingException;
import com.example.demo.service.GeocodingService.Coordinates;
import com.example.demo.service.GeocodingService.GeocodingResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class GeocodingServiceTest {

    private GeocodingService geocodingService;
    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        geocodingService = new GeocodingService();
        restTemplate = mock(RestTemplate.class);
        ReflectionTestUtils.setField(geocodingService, "restTemplate", restTemplate);
    }

    @Test
    void geocodeAddress_WithValidAddress_ShouldReturnCoordinates() {
        // Given
        String address = "New York, NY";
        Map<String, Object> mockResult = Map.of(
            "lat", "40.7128",
            "lon", "-74.0060",
            "display_name", "New York, New York, United States"
        );

        ResponseEntity<List> response = ResponseEntity.ok(List.of(mockResult));
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(List.class)))
            .thenReturn(response);

        // When
        GeocodingResult result = geocodingService.geocodeAddress(address);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getLatitude()).isEqualTo(40.7128);
        assertThat(result.getLongitude()).isEqualTo(-74.0060);
        assertThat(result.getDisplayName()).isEqualTo("New York, New York, United States");
    }

    @Test
    void geocodeAddress_WithNoResults_ShouldThrowException() {
        // Given
        String address = "InvalidAddress123XYZ";
        ResponseEntity<List> response = ResponseEntity.ok(List.of());
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(List.class)))
            .thenReturn(response);

        // When/Then
        assertThatThrownBy(() -> geocodingService.geocodeAddress(address))
            .isInstanceOf(GeocodingException.class)
            .hasMessageContaining("Address not found");
    }

    @Test
    void geocodeAddress_WithNullResponse_ShouldThrowException() {
        // Given
        String address = "Some Address";
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(List.class)))
            .thenReturn(ResponseEntity.ok(null));

        // When/Then
        assertThatThrownBy(() -> geocodingService.geocodeAddress(address))
            .isInstanceOf(GeocodingException.class)
            .hasMessageContaining("Address not found");
    }

    @Test
    void geocodeAddress_WithAPIError_ShouldThrowException() {
        // Given
        String address = "Test Address";
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(List.class)))
            .thenThrow(new RestClientException("API Error"));

        // When/Then
        assertThatThrownBy(() -> geocodingService.geocodeAddress(address))
            .isInstanceOf(GeocodingException.class)
            .hasMessageContaining("Error geocoding address");
    }

    @Test
    void geocodeAddress_WithInvalidCoordinateFormat_ShouldThrowException() {
        // Given
        String address = "Test Address";
        Map<String, Object> mockResult = Map.of(
            "lat", "invalid",
            "lon", "invalid",
            "display_name", "Test Location"
        );

        ResponseEntity<List> response = ResponseEntity.ok(List.of(mockResult));
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(List.class)))
            .thenReturn(response);

        // When/Then
        assertThatThrownBy(() -> geocodingService.geocodeAddress(address))
            .isInstanceOf(GeocodingException.class)
            .hasMessageContaining("Invalid API response format");
    }

    @Test
    void calculateDistance_WithSameLocation_ShouldReturnZero() {
        // Given
        Coordinates location1 = new Coordinates(40.7128, -74.0060);
        Coordinates location2 = new Coordinates(40.7128, -74.0060);

        // When
        double distance = geocodingService.calculateDistance(location1, location2);

        // Then
        assertThat(distance).isEqualTo(0.0);
    }

    @Test
    void calculateDistance_BetweenNewYorkAndLosAngeles_ShouldBeApproximately4000km() {
        // Given
        Coordinates newYork = new Coordinates(40.7128, -74.0060);
        Coordinates losAngeles = new Coordinates(34.0522, -118.2437);

        // When
        double distance = geocodingService.calculateDistance(newYork, losAngeles);

        // Then
        // Actual distance is approximately 3944 km
        assertThat(distance).isBetween(3900.0, 4000.0);
    }

    @Test
    void calculateDistance_BetweenLondonAndParis_ShouldBeApproximately340km() {
        // Given
        Coordinates london = new Coordinates(51.5074, -0.1278);
        Coordinates paris = new Coordinates(48.8566, 2.3522);

        // When
        double distance = geocodingService.calculateDistance(london, paris);

        // Then
        // Actual distance is approximately 344 km
        assertThat(distance).isBetween(330.0, 360.0);
    }

    @Test
    void calculateDistance_BetweenTokyoAndSydney_ShouldBeApproximately7800km() {
        // Given
        Coordinates tokyo = new Coordinates(35.6762, 139.6503);
        Coordinates sydney = new Coordinates(-33.8688, 151.2093);

        // When
        double distance = geocodingService.calculateDistance(tokyo, sydney);

        // Then
        // Actual distance is approximately 7823 km
        assertThat(distance).isBetween(7700.0, 7900.0);
    }

    @Test
    void calculateDistance_WithNegativeLatitude_ShouldWorkCorrectly() {
        // Given
        Coordinates northHemisphere = new Coordinates(40.0, 20.0);
        Coordinates southHemisphere = new Coordinates(-40.0, 20.0);

        // When
        double distance = geocodingService.calculateDistance(northHemisphere, southHemisphere);

        // Then
        // Distance should be approximately 8900 km (along same longitude)
        assertThat(distance).isGreaterThan(8000.0);
    }

    @Test
    void calculateDistance_WithLongitudeCrossing180_ShouldWorkCorrectly() {
        // Given
        Coordinates location1 = new Coordinates(0.0, 179.0);
        Coordinates location2 = new Coordinates(0.0, -179.0);

        // When
        double distance = geocodingService.calculateDistance(location1, location2);

        // Then
        // Should be approximately 222 km (2 degrees at equator)
        assertThat(distance).isBetween(200.0, 250.0);
    }

    @Test
    void calculateDistance_AtEquator_OneDegreeLongitude_ShouldBeApproximately111km() {
        // Given
        Coordinates location1 = new Coordinates(0.0, 0.0);
        Coordinates location2 = new Coordinates(0.0, 1.0);

        // When
        double distance = geocodingService.calculateDistance(location1, location2);

        // Then
        // At equator, 1 degree longitude ≈ 111 km
        assertThat(distance).isBetween(110.0, 112.0);
    }

    @Test
    void calculateDistance_AtPole_ShouldHandleCorrectly() {
        // Given
        Coordinates northPole = new Coordinates(90.0, 0.0);
        Coordinates nearNorthPole = new Coordinates(89.0, 0.0);

        // When
        double distance = geocodingService.calculateDistance(northPole, nearNorthPole);

        // Then
        // 1 degree latitude ≈ 111 km
        assertThat(distance).isBetween(110.0, 112.0);
    }

    @Test
    void geocodeAddress_WithSpecialCharacters_ShouldEncode() {
        // Given
        String address = "123 Main St, New York, NY";
        Map<String, Object> mockResult = Map.of(
            "lat", "40.7128",
            "lon", "-74.0060",
            "display_name", "123 Main Street, New York"
        );

        ResponseEntity<List> response = ResponseEntity.ok(List.of(mockResult));
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(List.class)))
            .thenReturn(response);

        // When
        GeocodingResult result = geocodingService.geocodeAddress(address);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getLatitude()).isEqualTo(40.7128);
    }
}