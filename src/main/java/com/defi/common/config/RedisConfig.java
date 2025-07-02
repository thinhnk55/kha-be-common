package com.defi.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * {@code RedisConfig} configures Redis connection and template beans for the
 * application.
 * This configuration sets up Redis connectivity using Lettuce client and
 * provides a configured RedisTemplate.
 *
 * <p>
 * The configuration includes:
 * </p>
 * <ul>
 * <li>Redis connection factory with configurable host, port, password, and
 * database</li>
 * <li>RedisTemplate with String keys and Integer values</li>
 * <li>Proper serialization configuration for keys and values</li>
 * </ul>
 *
 * <p>
 * Redis connection parameters are externalized through application properties.
 * </p>
 */
@Configuration
public class RedisConfig {

    /**
     * Creates a Lettuce Redis connection factory with configurable parameters.
     * Supports optional password authentication and database selection.
     *
     * @param redisHost     the Redis server hostname
     * @param redisPort     the Redis server port
     * @param redisPassword the Redis password (optional, can be empty)
     * @param redisDatabase the Redis database number (defaults to 0)
     * @return a configured LettuceConnectionFactory
     */
    @Bean
    public LettuceConnectionFactory redisConnectionFactory(
            @Value("${spring.redis.host}") String redisHost,
            @Value("${spring.redis.port}") int redisPort,
            @Value("${spring.redis.password:}") String redisPassword,
            @Value("${spring.redis.database:0}") int redisDatabase) {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(redisHost);
        config.setPort(redisPort);
        if (redisPassword != null && !redisPassword.isEmpty()) {
            config.setPassword(redisPassword);
        }
        config.setDatabase(redisDatabase);
        return new LettuceConnectionFactory(config);
    }

    /**
     * Creates a RedisTemplate
     *
     * @param connectionFactory the Redis connection factory
     * @return a configured RedisTemplate operations
     */
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }
}
