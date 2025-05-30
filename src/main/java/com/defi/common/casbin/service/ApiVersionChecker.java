package com.defi.common.casbin.service;

import com.defi.common.api.BaseResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

/**
 * API-based version checker implementation.
 * 
 * <p>
 * This implementation checks version numbers via HTTP API calls,
 * suitable for services that don't have direct database access.
 * </p>
 * 
 * @author Defi Team
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ApiVersionChecker implements VersionChecker {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private String versionApiEndpoint;

    /**
     * Sets the API endpoint for version checking.
     * 
     * @param endpoint the API endpoint URL template (e.g.,
     *                 "https://auth-service/auth/v1/internal/version/{code}")
     */
    public void setVersionApiEndpoint(String endpoint) {
        this.versionApiEndpoint = endpoint;
    }

    @Override
    public Optional<Long> getCurrentVersion(String code) {
        if (versionApiEndpoint == null || versionApiEndpoint.trim().isEmpty()) {
            log.debug("Version API endpoint not configured");
            return Optional.empty();
        }

        try {
            // Replace {code} placeholder with actual code
            String url = versionApiEndpoint.replace("{code}", code);

            // Make API call
            String jsonResponse = restTemplate.getForObject(url, String.class);

            if (jsonResponse == null || jsonResponse.trim().isEmpty()) {
                log.debug("Empty response from version API for code: {}", code);
                return Optional.empty();
            }

            // Parse response
            TypeReference<BaseResponse<Long>> typeRef = new TypeReference<BaseResponse<Long>>() {
            };
            BaseResponse<Long> response = objectMapper.readValue(jsonResponse, typeRef);

            if (response.data() != null) {
                return Optional.of(response.data());
            } else {
                log.debug("Null data in version API response for code: {}", code);
                return Optional.empty();
            }

        } catch (Exception e) {
            log.debug("Failed to get version for code: {} from API", code, e);
            return Optional.empty();
        }
    }

    @Override
    public boolean isAvailable() {
        if (versionApiEndpoint == null || versionApiEndpoint.trim().isEmpty()) {
            return false;
        }

        try {
            // Try to call health endpoint or a simple version check
            String healthUrl = versionApiEndpoint.replace("/{code}", "/health");
            String response = restTemplate.getForObject(healthUrl, String.class);
            return response != null && !response.trim().isEmpty();
        } catch (Exception e) {
            log.debug("API version checker is not available", e);
            return false;
        }
    }

    @Override
    public String getDescription() {
        return "API-based version checker using HTTP calls to: " +
                (versionApiEndpoint != null ? versionApiEndpoint : "not configured");
    }
}