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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CacheService {
    private static final Logger logger = LoggerFactory.getLogger(CacheService.class);
    private final Map<CacheKey, CachedResponse> cache = new ConcurrentHashMap<>();
    private final RestTemplate restTemplate = new RestTemplate();


    public ResponseEntity<String> getResponse(String origin, String path, HttpMethod method, HttpHeaders headers, String body) {
        CacheKey key = new CacheKey(path, method, headers, body);

        // Check if we have a cached response
        if (cache.containsKey(key)) {
            logger.info("Cache HIT for {}", path);
            CachedResponse cachedResponse = cache.get(key);

            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.putAll(cachedResponse.headers());
            responseHeaders.set("X-Cache", "HIT");

            return new ResponseEntity<>(cachedResponse.body(), responseHeaders, cachedResponse.statusCode());
        }
        logger.info("Cache MISS for {}", path);
        // Forward the request to the origin server
        HttpEntity<String> requestEntity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.exchange(
                origin + path,
                method,
                requestEntity,
                String.class
        );

        // Cache the response
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.putAll(response.getHeaders());
        responseHeaders.set("X-Cache", "MISS");

        cache.put(key, new CachedResponse(
                response.getBody(),
                response.getHeaders(),
                response.getStatusCode()
        ));

        return new ResponseEntity<>(response.getBody(), responseHeaders, response.getStatusCode());
    }

    public void clearCache() {
        logger.info("Clearing cache");
        cache.clear();
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
