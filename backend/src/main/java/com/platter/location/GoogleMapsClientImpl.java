package com.platter.location;

import com.fasterxml.jackson.databind.JsonNode;
import com.platter.exception.GoogleMapsException;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Component
public class GoogleMapsClientImpl implements GoogleMapsClient {
    private final WebClient webClient;
    private final GoogleMapsProperties properties;

    public GoogleMapsClientImpl(WebClient googleMapsWebClient, GoogleMapsProperties properties) { this.webClient = googleMapsWebClient; this.properties = properties; }

    @Override
    public GeocodedLocation geocode(String address) {
        requireConfiguration();
        try {
            JsonNode body = webClient.get().uri(uri -> uri.path("/maps/api/geocode/json").queryParam("address", address).queryParam("key", properties.apiKey()).build()).retrieve().bodyToMono(JsonNode.class).timeout(properties.timeout()).block();
            if (body == null || !"OK".equals(body.path("status").asText())) throw new GoogleMapsException("Google Maps geocoding failed: " + status(body));
            JsonNode result = body.path("results").path(0);
            JsonNode location = result.path("geometry").path("location");
            if (result.isMissingNode() || location.isMissingNode()) throw new GoogleMapsException("Google Maps geocoding response was incomplete");
            return new GeocodedLocation(result.path("formatted_address").asText(address), location.path("lat").asDouble(), location.path("lng").asDouble());
        } catch (GoogleMapsException exception) { throw exception; }
        catch (WebClientResponseException exception) { throw new GoogleMapsException("Google Maps geocoding HTTP error: " + exception.getStatusCode(), exception); }
        catch (RuntimeException exception) { throw new GoogleMapsException("Google Maps geocoding request failed", exception); }
    }

    @Override
    public DistanceResult distance(GeocodedLocation origin, GeocodedLocation destination) {
        requireConfiguration();
        try {
            Map<String, Object> request = Map.of(
                    "origin", Map.of("location", Map.of("latLng", Map.of("latitude", origin.latitude(), "longitude", origin.longitude()))),
                    "destination", Map.of("location", Map.of("latLng", Map.of("latitude", destination.latitude(), "longitude", destination.longitude()))),
                    "travelMode", "DRIVE");
            JsonNode body = webClient.post()
                    .uri("https://routes.googleapis.com/directions/v2:computeRoutes")
                    .header("X-Goog-Api-Key", properties.apiKey())
                    .header("X-Goog-FieldMask", "routes.distanceMeters,routes.duration")
                    .header("Content-Type", "application/json")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .timeout(properties.timeout())
                    .block();
            JsonNode route = body == null ? null : body.path("routes").path(0);
            if (route == null || route.isMissingNode()) throw new GoogleMapsException("Google Routes API returned no route");
            long distanceMeters = route.path("distanceMeters").asLong(0);
            long durationSeconds = parseDurationSeconds(route.path("duration").asText());
            if (distanceMeters <= 0 || durationSeconds <= 0) throw new GoogleMapsException("Google Routes API returned incomplete route data");
            return new DistanceResult(formatDistance(distanceMeters), distanceMeters, formatDuration(durationSeconds), durationSeconds);
        } catch (GoogleMapsException exception) { throw exception; }
        catch (WebClientResponseException exception) { throw new GoogleMapsException("Google Routes API HTTP error: " + exception.getStatusCode(), exception); }
        catch (RuntimeException exception) { throw new GoogleMapsException("Google Routes API request failed", exception); }
    }

    private void requireConfiguration() { if (!properties.configured()) throw new GoogleMapsException("Google Maps API key is not configured"); }
    private String status(JsonNode body) { return body == null ? "empty response" : body.path("status").asText("unknown error"); }
    private long parseDurationSeconds(String duration) { if (duration == null || duration.isBlank()) return 0; if (duration.endsWith("s")) { try { return Long.parseLong(duration.substring(0, duration.length() - 1)); } catch (NumberFormatException ignored) { return 0; } } return 0; }
    private String formatDistance(long meters) { return meters >= 1000 ? String.format(Locale.ROOT, "%.1f km", meters / 1000.0) : meters + " m"; }
    private String formatDuration(long seconds) { if (seconds < 60) return seconds + " s"; if (seconds < 3600) return (seconds / 60) + " mins"; long hours = seconds / 3600; long minutes = (seconds % 3600) / 60; return minutes == 0 ? hours + " hr" : hours + " hr " + minutes + " min"; }
}
