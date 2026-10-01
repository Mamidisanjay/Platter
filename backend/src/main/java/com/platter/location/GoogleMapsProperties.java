package com.platter.location;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "platter.google-maps")
public record GoogleMapsProperties(String apiKey, String baseUrl, long timeoutSeconds) {
    public Duration timeout() { return Duration.ofSeconds(timeoutSeconds); }
    public boolean configured() { return apiKey != null && !apiKey.isBlank(); }
}
