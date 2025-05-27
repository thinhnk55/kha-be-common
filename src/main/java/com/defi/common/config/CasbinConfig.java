package com.defi.common.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.casbin.jcasbin.main.Enforcer;
import org.casbin.jcasbin.model.Model;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.nio.charset.StandardCharsets;

/**
 * {@code CasbinConfig} configures Casbin authorization framework for the
 * application.
 * This configuration sets up the Casbin enforcer with RBAC model for
 * fine-grained access control.
 *
 * <p>
 * Casbin is an authorization library that supports various access control
 * models like ACL, RBAC, ABAC.
 * This configuration loads the authorization model from classpath resources.
 * </p>
 *
 * <p>
 * The configuration expects a model file at {@code classpath:casbin/model.conf}
 * that defines
 * the authorization rules and policies.
 * </p>
 */
@Configuration
@RequiredArgsConstructor
@Slf4j(topic = "Casbin")
public class CasbinConfig {

    /**
     * Constructor for dependency injection.
     * 
     * @param resourceLoader the Spring resource loader
     */

    /**
     * Spring resource loader for accessing classpath resources.
     */
    private final ResourceLoader resourceLoader;

    /**
     * Creates and configures a Casbin enforcer bean for authorization.
     * Loads the authorization model from the classpath and initializes the
     * enforcer.
     *
     * @return a configured Casbin enforcer instance
     * @throws Exception if the model file cannot be loaded or parsed
     */
    @Bean
    public Enforcer casbinEnforcer() throws Exception {
        Resource modelResource = resourceLoader.getResource("classpath:casbin/model.conf");
        String modelText = new String(modelResource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        Model model = new Model();
        model.loadModelFromText(modelText);
        Enforcer enforcer = new Enforcer(model);
        return enforcer;
    }
}
