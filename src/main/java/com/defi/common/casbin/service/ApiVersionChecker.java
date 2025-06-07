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

    private String apiEndpoint;

    /**
     * Sets the API endpoint for version checking.
     *
     * @param endpoint the API endpoint URL template (e.g.,
     *                 "<a href="https://auth-service/auth/v1/internal/version/policy_version">
     *                     https://auth-service/auth/v1/internal/version/policy_version</a>")
     */
    public void setApiEndpoint(String endpoint) {
        this.apiEndpoint = endpoint;
    }

    @Override
    public Optional<Long> getCurrentVersion() {
        if (apiEndpoint == null || apiEndpoint.trim().isEmpty()) {
            log.debug("Version API endpoint not configured");
            return Optional.empty();
        }

        try {
            // Make API call
            String jsonResponse = restTemplate.getForObject(apiEndpoint, String.class);

            if (jsonResponse.trim().isEmpty()) {
                log.debug("Empty response from version API for apiEndpoint: {}", apiEndpoint);
                return Optional.empty();
            }

            // Parse response
            TypeReference<BaseResponse<Long>> typeRef = new TypeReference<>() {
            };
            BaseResponse<Long> response = objectMapper.readValue(jsonResponse, typeRef);

            if (response.data() != null) {
                return Optional.of(response.data());
            } else {
                log.debug("Null data in version API response for apiEndpoint: {}", apiEndpoint);
                return Optional.empty();
            }

        } catch (Exception e) {
            log.debug("Failed to get version for apiEndpoint: {} from API", apiEndpoint, e);
            return Optional.empty();
        }
    }

    @Override
    public boolean isAvailable() {
        if(apiEndpoint != null && !apiEndpoint.trim().isEmpty()){
            String jsonResponse = restTemplate.getForObject(apiEndpoint, String.class);
            return !jsonResponse.trim().isEmpty();
        }
        log.debug("Version API endpoint not configured or empty");
        return false;
    }

    @Override
    public String getDescription() {
        return "API-based version checker using HTTP calls to: " +
                (apiEndpoint != null ? apiEndpoint : "not configured");
    }
}