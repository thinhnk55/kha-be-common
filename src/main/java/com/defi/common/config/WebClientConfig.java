package com.defi.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Web configuration for HTTP-related beans.
 * 
 * <p>
 * This configuration class provides beans for HTTP client operations
 * and web-related functionality across the application.
 * </p>
 * 
 * @author Defi Team
 * @since 1.0.0
 */
@Configuration
public class WebClientConfig {

    /**
     * Creates a RestTemplate bean for HTTP API calls.
     * 
     * <p>
     * This RestTemplate is used by services that need to make
     * HTTP requests to external APIs, such as policy loading from
     * remote endpoints.
     * </p>
     * 
     * @return configured RestTemplate instance
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}