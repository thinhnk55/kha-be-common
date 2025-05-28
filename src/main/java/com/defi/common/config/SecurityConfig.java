package com.defi.common.config;

import com.defi.common.filter.JwtAuthenticationEntryPoint;
import com.defi.common.filter.JwtTokenFilter;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * {@code SecurityConfig} configures Spring Security for the application.
 * This configuration sets up JWT-based authentication and authorization with
 * custom filters and entry points.
 *
 * <p>
 * The security configuration includes:
 * </p>
 * <ul>
 * <li>JWT token-based authentication using {@link JwtTokenFilter}</li>
 * <li>Public paths that bypass authentication (from {@link JwtConfig})</li>
 * <li>Custom authentication entry point for unauthorized access</li>
 * <li>CSRF protection disabled for stateless JWT authentication</li>
 * </ul>
 *
 * <p>
 * All requests except those matching public paths require valid JWT
 * authentication.
 * </p>
 */
@Configuration
@EnableWebSecurity
@AllArgsConstructor
public class SecurityConfig {

        /**
         * JWT token filter for processing and validating JWT tokens in requests.
         */
        private final JwtTokenFilter jwtTokenFilter;

        /**
         * Custom authentication entry point for handling unauthorized access attempts.
         */
        private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

        /**
         * JWT configuration containing public paths and other JWT-related settings.
         */
        private final SecurityProperties securityProperties;

        /**
         * Configures the security filter chain with JWT authentication and
         * authorization rules.
         * Sets up public paths, authentication requirements, and custom filters.
         *
         * @param http the HttpSecurity object to configure
         * @return a configured SecurityFilterChain
         * @throws Exception if configuration fails
         */
        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                String[] publicPaths = securityProperties.getPublicPaths().stream()
                                .map(s -> s + "/**").toArray(String[]::new);
                http
                                .csrf(AbstractHttpConfigurer::disable)
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(publicPaths).permitAll()
                                                .anyRequest().authenticated())
                                .exceptionHandling(ex -> ex
                                                .authenticationEntryPoint(jwtAuthenticationEntryPoint))
                                .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }
}
