package com.anterka.logia.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "proxy")
public class ProxyConfig {
    private String origin = ""; // Default empty string instead of null
    private boolean clearCache = false;

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public boolean isClearCache() {
        return clearCache;
    }

    public void setClearCache(boolean clearCache) {
        this.clearCache = clearCache;
    }

    // Add this method to check if the origin is set
    public boolean isOriginSet() {
        return origin != null && !origin.isEmpty();
    }
}