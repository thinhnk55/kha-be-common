package com.defi.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * {@code SecurityProperties} provides configuration for application security,
 * including public API paths that bypass authentication.
 *
 * <p>
 * Configuration properties are loaded from the {@code security} prefix and include:
 * </p>
 * <ul>
 *   <li>List of URL patterns that are permitted for all (no authentication required).</li>
 * </ul>
 *
 * <p>
 * This configuration class can be injected anywhere in your application to retrieve
 * the list of permitted API paths, typically for use in security filters.
 * </p>
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "security")
public class SecurityProperties {

    /**
     * List of Ant-style URL patterns that should be publicly accessible without authentication.
     *
     * Example:
     *   - /api/public/**
     *   - /auth/v1/public/**
     */
    private List<String> publicPaths;
}
