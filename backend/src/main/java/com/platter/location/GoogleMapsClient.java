package com.platter.location;

public interface GoogleMapsClient {
    GeocodedLocation geocode(String address);
    DistanceResult distance(GeocodedLocation origin, GeocodedLocation destination);

    record DistanceResult(String distanceText, long distanceMeters, String durationText, long durationSeconds) { }
}
