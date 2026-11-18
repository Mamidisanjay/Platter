package com.platter.location;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.platter.exception.GoogleMapsException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

class GoogleMapsClientImplTest {
    @Test
    void failsClearlyWhenApiKeyIsMissing() {
        GoogleMapsProperties properties = new GoogleMapsProperties("", "https://maps.googleapis.com", 1);
        GoogleMapsClient client = new GoogleMapsClientImpl(WebClient.builder().build(), properties);

        assertThatThrownBy(() -> client.geocode("Saffron Street"))
                .isInstanceOf(GoogleMapsException.class)
                .hasMessageContaining("API key is not configured");
    }

    @Test
    void routesApiReturnsDistanceAndDuration() {
        WebClient webClient = WebClient.builder().exchangeFunction(request -> {
            assertThat(request.method().name()).isEqualTo("POST");
            assertThat(request.headers().getFirst("X-Goog-Api-Key")).isEqualTo("test-api-key");
            assertThat(request.headers().getFirst("X-Goog-FieldMask")).isEqualTo("routes.distanceMeters,routes.duration");
            assertThat(request.headers().getFirst("Content-Type")).startsWith("application/json");
            return Mono.just(ClientResponse.create(HttpStatus.OK).header("Content-Type", "application/json").body("{\"routes\":[{\"distanceMeters\":2400,\"duration\":\"720s\"}]}").build());
        }).build();
        GoogleMapsClient client = new GoogleMapsClientImpl(webClient, new GoogleMapsProperties("test-api-key", "https://maps.googleapis.com", 1));

        GoogleMapsClient.DistanceResult result = client.distance(new GeocodedLocation("Origin", 12.97, 77.59), new GeocodedLocation("Destination", 12.98, 77.60));

        assertThat(result.distanceMeters()).isEqualTo(2400);
        assertThat(result.durationSeconds()).isEqualTo(720);
        assertThat(result.distanceText()).isEqualTo("2.4 km");
        assertThat(result.durationText()).isEqualTo("12 mins");
    }

    @Test
    void routesApiHttpErrorIsControlled() {
        WebClient webClient = WebClient.builder().exchangeFunction(request -> Mono.just(ClientResponse.create(HttpStatus.FORBIDDEN).header("Content-Type", "application/json").body("{\"error\":{\"message\":\"forbidden\"}}").build())).build();
        GoogleMapsClient client = new GoogleMapsClientImpl(webClient, new GoogleMapsProperties("test-api-key", "https://maps.googleapis.com", 1));

        assertThatThrownBy(() -> client.distance(new GeocodedLocation("Origin", 12.97, 77.59), new GeocodedLocation("Destination", 12.98, 77.60)))
                .isInstanceOf(GoogleMapsException.class)
                .hasMessageContaining("Google Routes API HTTP error");
    }
}
