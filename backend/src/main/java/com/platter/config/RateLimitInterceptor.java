package com.platter.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {
    private static final Logger log = LoggerFactory.getLogger(RateLimitInterceptor.class);
    private final RedisTemplate<String, String> redisTemplate;
    private final Map<String, Bucket> localBuckets = new ConcurrentHashMap<>();
    private final long capacity;
    private final long windowSeconds;

    public RateLimitInterceptor(RedisTemplate<String, String> redisTemplate, @Value("${platter.rate-limit.capacity:60}") long capacity, @Value("${platter.rate-limit.window-seconds:60}") long windowSeconds) { this.redisTemplate = redisTemplate; this.capacity = capacity; this.windowSeconds = windowSeconds; }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String path = request.getRequestURI();
        if (!isProtected(request.getMethod(), path)) return true;
        String key = path + ":" + clientKey(request);
        Bucket local = localBuckets.computeIfAbsent(key, ignored -> Bucket.builder().addLimit(Bandwidth.simple(capacity, Duration.ofSeconds(windowSeconds))).build());
        if (!local.tryConsume(1)) return reject(response);
        try {
            String redisKey = "platter:rate:" + key;
            Long count = redisTemplate.opsForValue().increment(redisKey);
            if (count != null && count == 1L) redisTemplate.expire(redisKey, Duration.ofSeconds(windowSeconds));
            return count == null || count <= capacity || reject(response);
        } catch (RuntimeException exception) {
            log.warn("Redis rate limiter unavailable; local Bucket4j protection remains active");
            return true;
        }
    }

    private boolean reject(HttpServletResponse response) {
        response.setStatus(429);
        response.setHeader("Retry-After", String.valueOf(windowSeconds));
        return false;
    }

    private boolean isProtected(String method, String path) {
        return ("POST".equals(method) && (path.equals("/api/auth/login") || path.equals("/api/auth/register") || path.equals("/api/promos/validate") || path.equals("/api/orders") || path.equals("/api/payments/create"))) || ("GET".equals(method) && path.equals("/api/search"));
    }

    private String clientKey(HttpServletRequest request) { return request.getRemoteAddr() == null ? "unknown" : request.getRemoteAddr(); }
}
