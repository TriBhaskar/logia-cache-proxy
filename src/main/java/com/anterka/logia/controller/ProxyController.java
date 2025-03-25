package com.anterka.logia.controller;

import com.anterka.logia.config.ProxyConfig;
import com.anterka.logia.service.CacheService;
import jakarta.servlet.http.HttpServletRequest;
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

    private final CacheService cacheService;
    private final ProxyConfig proxyConfig;

    @Autowired
    public ProxyController(CacheService cacheService, ProxyConfig proxyConfig) {
        this.cacheService = cacheService;
        this.proxyConfig = proxyConfig;

        // Check if cache should be cleared
        if (proxyConfig.isClearCache()) {
            cacheService.clearCache();
        }
    }

    @RequestMapping("/**")
    public ResponseEntity<String> proxyRequest(
            HttpServletRequest request,
            @RequestBody(required = false) String body
    ) {
        // Extract path from the request
        String path = request.getRequestURI();

        // Extract headers from the request
        HttpHeaders headers = new HttpHeaders();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headers.set(headerName, request.getHeader(headerName));
        }

        // Extract HTTP method
        HttpMethod method = HttpMethod.valueOf(request.getMethod());

        // Forward the request to the origin server and/or use cache
        return cacheService.getResponse(proxyConfig.getOrigin(), path, method, headers, body);
    }
}
