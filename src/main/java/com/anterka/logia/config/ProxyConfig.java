package com.anterka.logia.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "proxy")
public class ProxyConfig {
    private String origin = ""; // Default empty string instead of null
    private boolean clearCache = false;

    // Add this method to check if the origin is set
    public boolean isOriginSet() {
        return origin != null && !origin.isEmpty();
    }
}