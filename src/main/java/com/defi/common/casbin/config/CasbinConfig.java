package com.defi.common.casbin.config;

import com.defi.common.casbin.event.PolicyEventConstant;
import com.defi.common.casbin.event.PolicyEventListener;
import com.defi.common.casbin.service.PolicyLoader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

import org.casbin.jcasbin.main.Enforcer;
import org.casbin.jcasbin.model.Model;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Configuration class for Casbin authorization framework.
 * 
 * <p>
 * This configuration sets up the Casbin enforcer with RBAC (Role-Based Access
 * Control) model
 * and provides Redis-based policy change notifications for distributed systems.
 * </p>
 * 
 * <p>
 * Key features:
 * </p>
 * <ul>
 * <li>Synchronous policy loading during application startup</li>
 * <li>Redis message listener for real-time policy updates</li>
 * <li>Fail-fast initialization to ensure policies are ready before API
 * access</li>
 * </ul>
 * 
 * @author Defi Team
 * @since 1.0.0
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
@EnableScheduling
public class CasbinConfig {

    private final ResourceLoader resourceLoader;
    private final PolicyLoader policyLoader;

    /**
     * Creates and configures the Casbin Enforcer bean with policies loaded
     * synchronously.
     * 
     * <p>
     * This method ensures that all policies are loaded from the database before the
     * application becomes available to handle requests. If policy loading fails,
     * the
     * application startup will fail.
     * </p>
     * 
     * @return configured Casbin enforcer with policies loaded
     * @throws RuntimeException if enforcer creation or policy loading fails
     */
    @Bean
    public Enforcer enforcer() {
        try {
            log.info("Creating Casbin enforcer");

            // Load model from classpath
            Resource modelResource = resourceLoader.getResource("classpath:casbin/model.conf");
            String modelText = new String(modelResource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            Model model = new Model();
            model.loadModelFromText(modelText);

            // Create enforcer
            Enforcer enforcer = new Enforcer(model);

            // Load policies synchronously during bean creation
            log.info("Loading initial policies synchronously...");

            policyLoader.loadPolicies(enforcer);

            log.info("Casbin enforcer created and policies loaded successfully");
            return enforcer;

        } catch (Exception e) {
            log.error("Failed to create Casbin enforcer or load policies", e);
            throw new RuntimeException("Casbin initialization failed", e);
        }
    }

    /**
     * Creates Redis message listener container for policy change events.
     * 
     * <p>
     * This container listens for policy change notifications on the configured
     * Redis channel
     * and triggers automatic policy reloading across all service instances.
     * </p>
     * 
     * @param connectionFactory   Redis connection factory for establishing
     *                            connections
     * @param policyEventListener listener that handles policy change events
     * @return configured Redis message listener container
     */
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            PolicyEventListener policyEventListener) {

        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);

        // Subscribe to policy change events
        container.addMessageListener(policyEventListener, new ChannelTopic(
                PolicyEventConstant.DEFAULT_CHANNEL));

        log.info("Redis message listener configured for channel: {}",
                PolicyEventConstant.DEFAULT_CHANNEL);
        return container;
    }
}
