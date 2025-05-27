package com.defi.common.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * {@code CorsProperties} holds configuration properties for Cross-Origin
 * Resource Sharing (CORS).
 * These properties are loaded from the application configuration with the
 * {@code app.cors} prefix.
 *
 * <p>
 * This configuration class allows external configuration of:
 * </p>
 * <ul>
 * <li>Allowed origins for cross-origin requests</li>
 * </ul>
 *
 * <p>
 * Example configuration:
 * </p>
 * 
 * <pre>
 * app:
 *   cors:
 *     origins:
 *       - "http://localhost:3000"
 *       - "https://example.com"
 * </pre>
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "app.cors")
public class CorsProperties {

    /**
     * List of allowed origins for CORS requests.
     * Can include specific URLs or "*" for all origins.
     */
    private List<String> origins;
}
