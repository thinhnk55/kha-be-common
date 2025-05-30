package com.defi.common.casbin.util;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility class for parsing and validating policy source configurations.
 * 
 * <p>
 * Handles parsing of policy source strings in the format: {@code type:query}
 * </p>
 * 
 * <p>
 * Supported types:
 * </p>
 * <ul>
 * <li><strong>database</strong>: SQL query to load from database</li>
 * <li><strong>resource</strong>: Path to resource file (CSV/JSON)</li>
 * <li><strong>api</strong>: HTTP API endpoint URL</li>
 * </ul>
 * 
 * @author Defi Team
 * @since 1.0.0
 */
@Slf4j
public class PolicySourceParser {

    /**
     * Parses a policy source configuration string.
     * 
     * @param policySource the policy source string in format "type:query"
     * @return parsed PolicySourceConfig object
     * @throws IllegalArgumentException if the policy source format is invalid
     */
    public static PolicySourceConfig parse(String policySource) {
        if (policySource == null || policySource.trim().isEmpty()) {
            throw new IllegalArgumentException("Policy source cannot be null or empty");
        }

        String[] segments = policySource.split(":", 2);
        if (segments.length != 2) {
            throw new IllegalArgumentException("Policy source must be in format 'type:query', got: " + policySource);
        }

        String type = segments[0].trim().toLowerCase();
        String query = segments[1].trim();

        if (query.isEmpty()) {
            throw new IllegalArgumentException("Policy source query cannot be empty for type: " + type);
        }

        validatePolicyType(type);
        validatePolicyQuery(type, query);

        log.debug("Parsed policy source - Type: {}, Query: {}", type, query);

        return PolicySourceConfig.builder()
                .type(type)
                .query(query)
                .build();
    }

    /**
     * Validates that the policy type is supported.
     * 
     * @param type the policy type to validate
     * @throws IllegalArgumentException if the policy type is not supported
     */
    private static void validatePolicyType(String type) {
        if (!"database".equals(type) && !"resource".equals(type) && !"api".equals(type)) {
            throw new IllegalArgumentException("Unsupported policy type: " + type +
                    ". Supported types: database, resource, api");
        }
    }

    /**
     * Validates the policy query for specific types.
     * 
     * @param type  the policy type
     * @param query the policy query to validate
     * @throws IllegalArgumentException if the query is invalid for the given type
     */
    private static void validatePolicyQuery(String type, String query) {
        switch (type) {
            case "database":
                if (!query.toUpperCase().startsWith("SELECT")) {
                    throw new IllegalArgumentException("Database policy query must be a SELECT statement");
                }
                break;
            case "resource":
                if (!query.contains("csv")) {
                    throw new IllegalArgumentException("Resource policy query must be a valid c CSV file");
                }
                break;
            case "api":
                if (!query.startsWith("http://") && !query.startsWith("https://")) {
                    throw new IllegalArgumentException("API policy query must be a valid HTTP/HTTPS URL");
                }
                break;
        }
    }

    /**
     * Configuration object representing a parsed policy source.
     */
    @Data
    @lombok.Builder
    public static class PolicySourceConfig {
        /**
         * The policy source type (database, resource, api).
         */
        private String type;

        /**
         * The policy query (SQL, file path, or URL).
         */
        private String query;
    }
}