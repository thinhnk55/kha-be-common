package com.defi.common.config;

import com.defi.common.web.IpAddressArgumentResolver;
import com.defi.common.web.UserAgentArgumentResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * {@code WebMvcConfig} configures Spring MVC components and customizations.
 * This configuration class registers custom argument resolvers for enhanced
 * controller method parameter injection.
 *
 * <p>
 * Currently registers the following custom argument resolvers:
 * </p>
 * <ul>
 * <li>{@link IpAddressArgumentResolver} - for injecting client IP
 * addresses</li>
 * <li>{@link UserAgentArgumentResolver} - for injecting User-Agent strings</li>
 * </ul>
 *
 * <p>
 * These resolvers enable automatic injection of HTTP request metadata into
 * controller methods
 * using the {@link com.defi.common.web.IpAddress} and
 * {@link com.defi.common.web.UserAgent} annotations.
 * </p>
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * Resolver for injecting client IP addresses into controller method parameters.
     */
    @Autowired
    private IpAddressArgumentResolver ipAddressArgumentResolver;

    /**
     * Resolver for injecting User-Agent strings into controller method parameters.
     */
    @Autowired
    private UserAgentArgumentResolver userAgentArgumentResolver;

    /**
     * Registers custom argument resolvers with Spring MVC.
     * These resolvers enable automatic parameter injection for annotated controller
     * method parameters.
     *
     * @param resolvers the list of argument resolvers to add custom resolvers to
     */
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(ipAddressArgumentResolver);
        resolvers.add(userAgentArgumentResolver);
    }
}