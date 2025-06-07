package com.defi.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

/**
 * Configuration properties for Casbin authorization framework.
 * 
 * <p>
 * This class holds essential configuration settings for Casbin policy
 * management,
 * including service identification, resource filtering, and Redis communication
 * settings.
 * </p>
 * 
 * <p>
 * Configuration properties are bound from application properties with prefix
 * {@code app.casbin}.
 * </p>
 * 
 * <p>
 * Example configuration:
 * </p>
 * 
 * <pre>
 * app.casbin.service-name=auth-service
 * app.casbin.resources=users,roles,permissions
 * app.casbin.enable-filtering=true
 * app.casbin.redis-channel=casbin:policy:changes
 * </pre>
 * 
 * @author Defi Team
 * @since 1.0.0
 */
@Component
@ConfigurationProperties(prefix = "app.casbin")
@Data
public class CasbinProperties {

    /**
     * List of resources this service manages or needs policies for.
     * When filtering is enabled, only policies for these resources will be loaded.
     * 
     * @default empty list (loads all policies)
     */
    private List<String> resources = List.of();
    private String policySource;

    /**
     * Polling configuration for version checking and policy reloading.
     */
    private PollingConfig polling = new PollingConfig();

    /**
     * Configuration for policy version polling.
     */
    @Data
    public static class PollingConfig {

        /**
         * Duration between version checks.
         * 
         * <p>
         * Minimum allowed duration is 1 minute (PT1M).
         * If not configured or disabled, polling will not be enabled.
         * </p>
         * 
         * <p>
         * Examples:
         * </p>
         * <ul>
         * <li>PT1M - 1 minute (minimum)</li>
         * <li>PT5M - 5 minutes</li>
         * <li>PT1H - 1 hour</li>
         * </ul>
         */
        private Duration duration;

        /**
         * Whether polling is enabled.
         * 
         * <p>
         * Polling is enabled or not
         * </p>
         */
        private boolean enabled = false;

        /**
         * Version source configuration for checking policy changes.
         *
         * <ul>
         * <li><strong>API source</strong>:
         * {@code http://api.example.com/auth/v1/internal/version/policy_version}</li>
         * </ul>
         *
         */
        private String versionSource;

        /**
         * Checks if polling configuration is valid.
         * 
         * @return true if polling can be enabled
         */
        public boolean isValidForPolling() {
            return duration != null &&
                    duration.toMinutes() >= 1;
        }

        /**
         * Gets the minimum allowed polling duration (1 minute).
         * 
         * @return minimum duration
         */
        public static Duration getMinimumDuration() {
            return Duration.ofMinutes(1);
        }
    }
}