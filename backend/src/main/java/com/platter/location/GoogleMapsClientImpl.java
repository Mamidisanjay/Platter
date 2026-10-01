package com.platter.location;

import com.fasterxml.jackson.databind.JsonNode;
import com.platter.exception.GoogleMapsException;
import java.util.Locale;
import org.springframework.http.HttpStatusCode;
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
            JsonNode body = webClient.get().uri(uri -> uri.path("/maps/api/distancematrix/json").queryParam("origins", coordinates(origin)).queryParam("destinations", coordinates(destination)).queryParam("key", properties.apiKey()).build()).retrieve().bodyToMono(JsonNode.class).timeout(properties.timeout()).block();
            if (body == null || !"OK".equals(body.path("status").asText())) throw new GoogleMapsException("Google Maps distance request failed: " + status(body));
            JsonNode element = body.path("rows").path(0).path("elements").path(0);
            if (!"OK".equals(element.path("status").asText())) throw new GoogleMapsException("Google Maps route unavailable: " + element.path("status").asText());
            return new DistanceResult(element.path("distance").path("text").asText(), element.path("distance").path("value").asLong(), element.path("duration").path("text").asText(), element.path("duration").path("value").asLong());
        } catch (GoogleMapsException exception) { throw exception; }
        catch (WebClientResponseException exception) { throw new GoogleMapsException("Google Maps distance HTTP error: " + exception.getStatusCode(), exception); }
        catch (RuntimeException exception) { throw new GoogleMapsException("Google Maps distance request failed", exception); }
    }

    private void requireConfiguration() { if (!properties.configured()) throw new GoogleMapsException("Google Maps API key is not configured"); }
    private String coordinates(GeocodedLocation location) { return String.format(Locale.ROOT, "%f,%f", location.latitude(), location.longitude()); }
    private String status(JsonNode body) { return body == null ? "empty response" : body.path("status").asText("unknown error"); }
}
