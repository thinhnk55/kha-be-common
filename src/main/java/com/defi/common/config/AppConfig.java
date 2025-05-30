package com.defi.common.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Common application configuration that enables all configuration properties.
 * 
 * <p>
 * This configuration class is responsible for enabling Spring Boot's
 * configuration property binding for all @ConfigurationProperties classes
 * used throughout the application.
 * </p>
 * 
 * @author Defi Team
 * @since 1.0.0
 */
@Configuration
@EnableConfigurationProperties({
        CasbinProperties.class,
        SecurityProperties.class,
        CorsProperties.class
})
public class AppConfig {
    // This class serves to enable configuration properties binding
    // No additional configuration needed
}