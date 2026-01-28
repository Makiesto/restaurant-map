package com.example.demo.service;

import com.example.demo.exception.GeocodingException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class GeocodingService {

    private static final String NOMINATIM_URL = "https://nominatim.openstreetmap.org/search";
    private static final long MIN_REQUEST_INTERVAL_MS = 1000;

    private final RestTemplate restTemplate;
    private long lastRequestTime = 0;

    public GeocodingService() {
        this.restTemplate = new RestTemplate();
    }

    public GeocodingResult geocodeAddress(String address) {
        log.info("Geocoding address: {}", address);

        enforceRateLimit();

        try {
            // Build URL with parameters
            String url = UriComponentsBuilder.fromUriString(NOMINATIM_URL)
                    .queryParam("q", address)
                    .queryParam("format", "json")
                    .queryParam("limit", 1)
                    .queryParam("addressdetails", 1)
                    .build()
                    .toUriString();

            // Set User-Agent header (required by Nominatim)
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "RestaurantMapApp/1.0 (contact@example.com)");
            HttpEntity<String> entity = new HttpEntity<>(headers);

            // Call API
            ResponseEntity<List> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                List.class
            );

            List<Map<String, Object>> results = response.getBody();

            // Check if results exist
            if (results == null || results.isEmpty()) {
                log.warn("No results found for address: {}", address);
                throw new GeocodingException("Address not found: " + address);
            }

            // Get first result
            Map<String, Object> location = results.get(0);
            Double latitude = Double.parseDouble(location.get("lat").toString());
            Double longitude = Double.parseDouble(location.get("lon").toString());
            String displayName = location.get("display_name").toString();

            log.info("Geocoded address: {} -> lat={}, lon={}, full name: {}",
                     address, latitude, longitude, displayName);

            return new GeocodingResult(latitude, longitude, displayName);

        } catch (RestClientException e) {
            log.error("Error calling Nominatim API for address: {}", address, e);
            throw new GeocodingException("Error geocoding address: " + e.getMessage());
        } catch (NumberFormatException e) {
            log.error("Error parsing coordinates for address: {}", address, e);
            throw new GeocodingException("Invalid API response format");
        }
    }

    /**
     * Enforces rate limiting - waits 1 second between requests
     */
    private void enforceRateLimit() {
        long now = System.currentTimeMillis();
        long timeSinceLastRequest = now - lastRequestTime;

        if (timeSinceLastRequest < MIN_REQUEST_INTERVAL_MS) {
            long sleepTime = MIN_REQUEST_INTERVAL_MS - timeSinceLastRequest;
            log.debug("Rate limiting: waiting {} ms", sleepTime);
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Rate limiting interrupted", e);
            }
        }

        lastRequestTime = System.currentTimeMillis();
    }

    /**
     * Result class holding geocoding data
     */
    @Getter
    public static class GeocodingResult {
        private final Double latitude;
        private final Double longitude;
        private final String displayName;

        public GeocodingResult(Double latitude, Double longitude, String displayName) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.displayName = displayName;
        }

    }
}