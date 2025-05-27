package com.defi.common.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

/**
 * {@code JacksonConfig} configures the Jackson ObjectMapper for JSON
 * serialization and deserialization.
 * This configuration customizes the default Jackson behavior to handle Java 8
 * time types and unknown properties.
 *
 * <p>
 * The configuration applies the following customizations:
 * </p>
 * <ul>
 * <li>Registers {@link JavaTimeModule} for proper Java 8 time type support</li>
 * <li>Disables timestamp serialization for dates (uses ISO format instead)</li>
 * <li>Disables failure on unknown JSON properties during deserialization</li>
 * </ul>
 *
 * <p>
 * These settings ensure consistent JSON handling across the application and
 * improve
 * compatibility with different client implementations.
 * </p>
 */
@Configuration
@RequiredArgsConstructor
public class JacksonConfig {

    /**
     * The Spring-managed ObjectMapper instance to be customized.
     */
    private final ObjectMapper mapper;

    /**
     * Customizes the ObjectMapper with application-specific settings.
     * This method is called automatically after dependency injection is complete.
     * 
     * <p>
     * Applied customizations:
     * </p>
     * <ul>
     * <li>Java 8 time module registration for LocalDateTime, LocalDate, etc.</li>
     * <li>ISO date format instead of timestamps</li>
     * <li>Lenient deserialization that ignores unknown properties</li>
     * </ul>
     */
    @PostConstruct
    public void customizeObjectMapper() {
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }
}
