package com.defi.common.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Jackson configuration for JSON serialization and deserialization.
 * 
 * <p>
 * This configuration class provides ObjectMapper and other JSON-related
 * beans used throughout the application for data conversion.
 * </p>
 * 
 * @author Defi Team
 * @since 1.0.0
 */
@Configuration
public class JacksonConfig {

    /**
     * Creates an ObjectMapper bean for JSON parsing and serialization.
     * 
     * <p>
     * This ObjectMapper is used by components that need to convert
     * between Java objects and JSON, such as API response parsing,
     * error response generation, and data serialization.
     * </p>
     * 
     * <p>
     * Applied customizations:
     * </p>
     * <ul>
     * <li>Java 8 time module registration for LocalDateTime, LocalDate, etc.</li>
     * <li>ISO date format instead of timestamps</li>
     * <li>Lenient deserialization that ignores unknown properties</li>
     * </ul>
     * 
     * @return configured ObjectMapper instance
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // Register Java 8 time module
        mapper.registerModule(new JavaTimeModule());

        // Use ISO date format instead of timestamps
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Ignore unknown properties during deserialization
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        return mapper;
    }
}
