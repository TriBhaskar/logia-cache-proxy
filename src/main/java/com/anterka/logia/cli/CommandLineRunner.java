package com.anterka.logia.cli;

import com.anterka.logia.config.ProxyConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.stereotype.Component;

@Component
public class CommandLineRunner implements ApplicationRunner, WebServerFactoryCustomizer<ConfigurableWebServerFactory> {
    private static final Logger logger = LoggerFactory.getLogger(CommandLineRunner.class);

    private final ProxyConfig proxyConfig;

    // Use Spring's @Value to directly get properties from command line
    @Value("${port:8080}")
    private int port;

    @Value("${origin:}")
    private String origin;

    @Value("${clearCache:false}")
    private boolean clearCache;

    @Autowired
    public CommandLineRunner(ProxyConfig proxyConfig) {
        this.proxyConfig = proxyConfig;
    }

    @Override
    public void run(ApplicationArguments args) {
        logger.info("Running with arguments: port={}, origin={}, clearCache={}", port, origin, clearCache);

        // Set the values in the ProxyConfig
        if (origin != null && !origin.isEmpty()) {
            proxyConfig.setOrigin(origin);
            logger.info("Setting origin to: {}", proxyConfig.getOrigin());
        } else {
            logger.error("Origin is required but not specified or is empty");
        }

        if (clearCache) {
            proxyConfig.setClearCache(true);
            logger.info("Cache clearing requested");
        }

        logger.info("Starting proxy server on port {} with origin {}", port, proxyConfig.getOrigin());
    }

    @Override
    public void customize(ConfigurableWebServerFactory factory) {
        logger.info("Setting server port to: {}", port);
        factory.setPort(port);
    }
}