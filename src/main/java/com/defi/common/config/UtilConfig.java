package com.defi.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * {@code UtilConfig} provides utility bean configurations for the application.
 * This configuration class defines commonly used utility beans such as password
 * encoders.
 *
 * <p>
 * Currently provides:
 * </p>
 * <ul>
 * <li>{@link PasswordEncoder} - BCrypt password encoder for secure password
 * hashing</li>
 * </ul>
 */
@Configuration
public class UtilConfig {

    /**
     * Creates a BCrypt password encoder bean for secure password hashing.
     * BCrypt is a strong hashing function designed for password storage.
     *
     * @return a {@link BCryptPasswordEncoder} instance for password encoding
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
