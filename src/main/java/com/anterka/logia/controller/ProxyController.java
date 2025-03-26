package com.anterka.logia.controller;

import com.anterka.logia.config.ProxyConfig;
import com.anterka.logia.service.CacheService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Enumeration;

@RestController
public class ProxyController {
    private static final Logger logger = LoggerFactory.getLogger(ProxyController.class);

    private final CacheService cacheService;
    private final ProxyConfig proxyConfig;

    @Autowired
    public ProxyController(CacheService cacheService, ProxyConfig proxyConfig) {
        this.cacheService = cacheService;
        this.proxyConfig = proxyConfig;

        logger.info("ProxyController initialized with origin: {}", proxyConfig.getOrigin());

        // Check if cache should be cleared
        if (proxyConfig.isClearCache()) {
            logger.info("Cache clear requested via configuration");
            cacheService.clearCache();
        }
    }

    @RequestMapping("/**")
    public ResponseEntity<String> proxyRequest(
            HttpServletRequest request,
            @RequestBody(required = false) String body
    ) {
        String path = request.getRequestURI();
        HttpMethod method = HttpMethod.valueOf(request.getMethod());

        logger.debug("Received {} request for path: {}", method, path);
        logger.trace("Request body size: {} bytes", body != null ? body.length() : 0);

        // Extract headers from the request
        HttpHeaders headers = new HttpHeaders();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headers.set(headerName, request.getHeader(headerName));
        }

        logger.debug("Forwarding request to origin: {}{}", proxyConfig.getOrigin(), path);

        // Forward the request to the origin server and/or use cache
        long startTime = System.currentTimeMillis();
        ResponseEntity<String> response = cacheService.getResponse(proxyConfig.getOrigin(), path, method, headers, body);
        long duration = System.currentTimeMillis() - startTime;

        logger.debug("Request processed in {} ms with status {}", duration, response.getStatusCode().value());

        return response;
    }
}