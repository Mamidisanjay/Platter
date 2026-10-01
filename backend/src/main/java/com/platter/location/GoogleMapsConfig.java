package com.platter.location;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@EnableConfigurationProperties(GoogleMapsProperties.class)
public class GoogleMapsConfig {
    @Bean
    WebClient googleMapsWebClient(WebClient.Builder builder, GoogleMapsProperties properties) {
        return builder.baseUrl(properties.baseUrl()).build();
    }
}
