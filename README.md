# Logia Caching Proxy Server

A simple caching proxy server built with Java 17 and Spring Boot. This server forwards requests to an origin server and caches the responses. Subsequent identical requests will be served from the cache.

## Features

- Forward requests to a specified origin server
- Cache responses for faster subsequent requests
- Clear cache on demand
- Indicate cache hits/misses through HTTP headers

## Requirements

- Java 17 or higher
- Maven

## Building the Project

```bash
mvn clean package
```

This will create an executable JAR file in the `target` directory.

## Running the Proxy Server

```bash
java -jar target/caching-proxy-0.0.1-SNAPSHOT.jar --port <port> --origin <origin-url>
```

Example:

```bash
java -jar target/caching-proxy-0.0.1-SNAPSHOT.jar --port 3000 --origin http://dummyjson.com
```

This will start the proxy server on port 3000, forwarding requests to `http://dummyjson.com`.

## Clearing the Cache

```bash
java -jar target/caching-proxy-0.0.1-SNAPSHOT.jar --clear-cache
```

## How It Works

1. When a request is received, the proxy server checks if a cached response exists
2. If a cached response exists, it returns it with the header `X-Cache: HIT`
3. If no cached response exists, it forwards the request to the origin server, caches the response, and returns it with the header `X-Cache: MISS`

## Example Usage

After starting the proxy server, you can make requests like:

```bash
curl http://localhost:3000/products
```

This will forward the request to `http://dummyjson.com/products` and cache the response.