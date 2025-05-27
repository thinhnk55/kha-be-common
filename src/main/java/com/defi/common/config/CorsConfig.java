package com.defi.common.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * {@code CorsConfig} provides Cross-Origin Resource Sharing (CORS)
 * configuration for the application.
 * This configuration enables controlled access to resources from different
 * origins.
 *
 * <p>
 * The CORS configuration includes:
 * </p>
 * <ul>
 * <li>Allowed origins from {@link CorsProperties}</li>
 * <li>Permitted HTTP methods (GET, POST, PUT, DELETE, OPTIONS)</li>
 * <li>Allowed headers (all headers permitted)</li>
 * <li>Credential support based on origin configuration</li>
 * <li>Preflight request caching (1 hour)</li>
 * </ul>
 *
 * <p>
 * Credentials are enabled only when specific origins are configured (not
 * wildcard).
 * </p>
 */
@Configuration
@EnableConfigurationProperties(CorsProperties.class)
@RequiredArgsConstructor
public class CorsConfig {

    /**
     * Constructor for dependency injection.
     * 
     * @param corsProperties the CORS configuration properties
     */

    /**
     * CORS properties containing allowed origins configuration.
     */
    private final CorsProperties corsProperties;

    /**
     * Creates a CORS configuration source bean that defines cross-origin access
     * rules.
     * The configuration is applied to all endpoints (/**) and includes:
     * <ul>
     * <li>Origins from {@link CorsProperties}</li>
     * <li>Standard HTTP methods</li>
     * <li>All headers</li>
     * <li>Credentials when not using wildcard origins</li>
     * <li>1-hour preflight cache</li>
     * </ul>
     *
     * @return a configured {@link CorsConfigurationSource} for the application
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        List<String> allowedOrigins = corsProperties.getOrigins();
        config.setAllowedOrigins(allowedOrigins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));

        if (!allowedOrigins.contains("*")) {
            config.setAllowCredentials(true);
        }

        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
