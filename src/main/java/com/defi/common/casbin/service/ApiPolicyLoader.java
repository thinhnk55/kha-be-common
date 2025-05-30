package com.defi.common.casbin.service;

import com.defi.common.api.BaseResponse;
import com.defi.common.casbin.entity.PolicyRule;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for loading policies from HTTP API endpoints.
 * 
 * <p>
 * This service handles loading policy rules from REST API endpoints.
 * The expected JSON response format is {@code BaseResponse<List<PolicyRule>>}:
 * </p>
 * 
 * <pre>
 * {
 *   "code": 200,
 *   "message": "Success",
 *   "data": [
 *     {
 *       "id": 1,
 *       "roleId": 1,
 *       "resourceCode": "users",
 *       "actionCode": "read"
 *     }
 *   ]
 * }
 * </pre>
 * 
 * @author Defi Team
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ApiPolicyLoader {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Loads policy rules from an HTTP API endpoint.
     * 
     * <p>
     * This method makes a GET request to the specified API endpoint
     * and expects a {@code BaseResponse<List<PolicyRule>>} format response.
     * </p>
     * 
     * @param apiEndpoint the HTTP API endpoint URL
     * @param resources   list of resource codes to filter by (empty list loads all)
     * @return list of policy rules loaded from API
     * @throws RuntimeException if API call fails or response parsing fails
     */
    public List<PolicyRule> loadPolicyRulesFromApi(String apiEndpoint, List<String> resources) {
        log.info("Loading policy rules from API: {}", apiEndpoint);
        log.debug("Resource filter: {}", resources);

        try {
            // Build API URL with resource filter if provided
            String requestUrl = buildApiUrl(apiEndpoint, resources);

            // Make HTTP GET request
            String jsonResponse = restTemplate.getForObject(requestUrl, String.class);

            if (jsonResponse == null || jsonResponse.trim().isEmpty()) {
                log.warn("Received empty response from API: {}", requestUrl);
                return new ArrayList<>();
            }

            log.debug("Raw API response: {}", jsonResponse);

            // Parse the JSON response
            List<PolicyRule> policies = parseApiResponse(jsonResponse);

            log.info("Successfully loaded {} policy rules from API", policies.size());
            log.debug("Loaded policies: {}", policies.stream()
                    .map(PolicyRule::toString)
                    .collect(Collectors.joining(", ")));

            return policies;

        } catch (Exception e) {
            log.error("Failed to load policy rules from API: {}", apiEndpoint, e);
            throw new RuntimeException("API policy loading failed: " + e.getMessage(), e);
        }
    }

    /**
     * Builds the API request URL with optional resource filtering.
     * 
     * @param baseUrl   the base API endpoint URL
     * @param resources list of resource codes for filtering
     * @return complete API URL with query parameters
     */
    private String buildApiUrl(String baseUrl, List<String> resources) {
        if (resources.isEmpty()) {
            return baseUrl;
        }

        // Add resource filter as query parameter
        String resourceParam = String.join(",", resources);
        String separator = baseUrl.contains("?") ? "&" : "?";
        return baseUrl + separator + "resourceCode=" + resourceParam;
    }

    /**
     * Parses the JSON response from API into PolicyRule list.
     * 
     * <p>
     * Expects BaseResponse<List<PolicyRule>> format.
     * </p>
     * 
     * @param jsonResponse the JSON response string from API
     * @return list of parsed PolicyRule objects
     * @throws Exception if JSON parsing fails
     */
    private List<PolicyRule> parseApiResponse(String jsonResponse) throws Exception {
        try {
            // Parse as BaseResponse<List<PolicyRule>>
            TypeReference<BaseResponse<List<PolicyRule>>> typeRef = new TypeReference<BaseResponse<List<PolicyRule>>>() {
            };
            BaseResponse<List<PolicyRule>> response = objectMapper.readValue(jsonResponse, typeRef);

            if (response.data() == null) {
                log.warn("API response data field is null");
                return new ArrayList<>();
            }

            // Validate each policy rule
            List<PolicyRule> validPolicies = new ArrayList<>();
            for (PolicyRule policy : response.data()) {
                if (isValidPolicyRule(policy)) {
                    validPolicies.add(policy);
                } else {
                    log.warn("Skipping invalid policy rule: {}", policy);
                }
            }

            return validPolicies;

        } catch (Exception e) {
            log.error("Failed to parse API response as BaseResponse<List<PolicyRule>>", e);
            throw new RuntimeException("Invalid JSON format in API response", e);
        }
    }

    /**
     * Validates a PolicyRule object for required fields.
     * 
     * @param policy the PolicyRule to validate
     * @return true if policy is valid, false otherwise
     */
    private boolean isValidPolicyRule(PolicyRule policy) {
        if (policy == null) {
            return false;
        }

        if (policy.getRoleId() == null || policy.getResourceCode() == null || policy.getActionCode() == null) {
            return false;
        }

        if (policy.getResourceCode().trim().isEmpty() || policy.getActionCode().trim().isEmpty()) {
            return false;
        }

        return true;
    }

    /**
     * Tests API endpoint connectivity and response format.
     * 
     * @param apiEndpoint the API endpoint to test
     * @return true if endpoint is reachable and returns valid format
     */
    public boolean isApiEndpointValid(String apiEndpoint) {
        try {
            String response = restTemplate.getForObject(apiEndpoint + "?limit=1", String.class);
            return response != null && !response.trim().isEmpty();
        } catch (Exception e) {
            log.warn("API endpoint validation failed for: {}", apiEndpoint, e);
            return false;
        }
    }
}