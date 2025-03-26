package com.anterka.logia.service;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CacheService {
    private static final Logger logger = LoggerFactory.getLogger(CacheService.class);
    private final Map<CacheKey, CachedResponse> cache = new ConcurrentHashMap<>();
    private final RestTemplate restTemplate = new RestTemplate();

    @Getter
    private long cacheHits = 0;

    @Getter
    private long cacheMisses = 0;

    public ResponseEntity<String> getResponse(String origin, String path, HttpMethod method, HttpHeaders headers, String body) {
        CacheKey key = new CacheKey(path, method, headers, body);

        logger.debug("Processing request: {} {} with body size: {}",
                method, path, body != null ? body.length() : 0);

        // Check if we have a cached response
        if (cache.containsKey(key)) {
            cacheHits++;
            CachedResponse cachedResponse = cache.get(key);

            logger.info("Cache HIT for {} {} (hit rate: {}%)",
                    method, path, calculateHitRate());
            logger.debug("Cached response size: {} bytes, status: {}",
                    cachedResponse.body() != null ? cachedResponse.body().length() : 0,
                    cachedResponse.statusCode().value());

            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.putAll(cachedResponse.headers());
            responseHeaders.set("X-Cache", "HIT");

            return new ResponseEntity<>(cachedResponse.body(), responseHeaders, cachedResponse.statusCode());
        }

        cacheMisses++;
        logger.info("Cache MISS for {} {} (hit rate: {}%)",
                method, path, calculateHitRate());

        // Forward the request to the origin server
        HttpEntity<String> requestEntity = new HttpEntity<>(body, headers);
        logger.debug("Forwarding request to origin: {}{}", origin, path);

        long startTime = System.currentTimeMillis();
        ResponseEntity<String> response;
        try {
            response = restTemplate.exchange(
                    origin + path,
                    method,
                    requestEntity,
                    String.class
            );
            long requestTime = System.currentTimeMillis() - startTime;
            logger.debug("Origin server responded in {} ms with status {}",
                    requestTime, response.getStatusCode().value());
        } catch (Exception e) {
            logger.error("Error forwarding request to origin server: {}", e.getMessage(), e);
            throw e;
        }

        // Cache the response
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.putAll(response.getHeaders());
        responseHeaders.set("X-Cache", "MISS");

        cache.put(key, new CachedResponse(
                response.getBody(),
                response.getHeaders(),
                response.getStatusCode()
        ));

        logger.debug("Cached new response for {} {}, cache size: {}",
                method, path, cache.size());

        return new ResponseEntity<>(response.getBody(), responseHeaders, response.getStatusCode());
    }

    public void clearCache() {
        int cacheSize = cache.size();
        logger.info("Clearing cache, removing {} entries", cacheSize);
        cache.clear();
        logger.debug("Cache cleared successfully");
    }

    public Map<String, Object> getCacheStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("cacheSize", cache.size());
        stats.put("cacheHits", cacheHits);
        stats.put("cacheMisses", cacheMisses);
        stats.put("hitRate", calculateHitRate());
        return stats;
    }

    private double calculateHitRate() {
        long total = cacheHits + cacheMisses;
        return total > 0 ? (double) cacheHits / total * 100 : 0.0;
    }

    private record CacheKey(String path, HttpMethod method, HttpHeaders headers, String body) {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            CacheKey cacheKey = (CacheKey) o;

            if (!path.equals(cacheKey.path)) return false;
            if (method != cacheKey.method) return false;
            // Headers comparison should focus on relevant headers for caching
            // Simplified for this example
            return body != null ? body.equals(cacheKey.body) : cacheKey.body == null;
        }

        @Override
        public int hashCode() {
            int result = path.hashCode();
            result = 31 * result + method.hashCode();
            result = 31 * result + (body != null ? body.hashCode() : 0);
            return result;
        }
    }

    private record CachedResponse(String body, HttpHeaders headers,
                                  org.springframework.http.HttpStatusCode statusCode) {
    }
}