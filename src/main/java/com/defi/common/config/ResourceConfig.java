package com.defi.common.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
@Getter
@Setter
@ConfigurationProperties(prefix = "app.resources")
public class ResourceConfig {
    private List<String> resources;
}
