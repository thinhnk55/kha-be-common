package com.defi.common.config;

import com.defi.common.log.ErrorLogger;
import com.defi.common.log.EventLogger;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class that defines beans related to application logging.
 *
 * <p>This includes the registration of {@link ErrorLogger} as a Spring-managed bean,
 * allowing it to be injected wherever structured error logging is needed.</p>
 *
 * <p>The {@link ErrorLogger} depends on {@link ObjectMapper} for JSON serialization.</p>
 */
@Configuration
public class LoggerConfig {

    /**
     * Registers a singleton {@link ErrorLogger} bean using the provided {@link ObjectMapper}.
     *
     * @param mapper the Jackson ObjectMapper used for JSON serialization
     * @return an instance of {@link ErrorLogger}
     */
    @Bean
    public ErrorLogger errorLogger(ObjectMapper mapper) {
        return new ErrorLogger(mapper);
    }

    /**
     * Registers a singleton {@link ErrorLogger} bean using the provided {@link ObjectMapper}.
     *
     * @param mapper the Jackson ObjectMapper used for JSON serialization
     * @return an instance of {@link ErrorLogger}
     */
    @Bean
    public EventLogger eventLogger(ObjectMapper mapper) {
        return new EventLogger(mapper);
    }
}
