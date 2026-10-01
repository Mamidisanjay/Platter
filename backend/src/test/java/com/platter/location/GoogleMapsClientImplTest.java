package com.platter.location;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.platter.exception.GoogleMapsException;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

class GoogleMapsClientImplTest {
    @Test
    void failsClearlyWhenApiKeyIsMissing() {
        GoogleMapsProperties properties = new GoogleMapsProperties("", "https://maps.googleapis.com", 1);
        GoogleMapsClient client = new GoogleMapsClientImpl(WebClient.builder().build(), properties);

        assertThatThrownBy(() -> client.geocode("Saffron Street"))
                .isInstanceOf(GoogleMapsException.class)
                .hasMessageContaining("API key is not configured");
    }
}
