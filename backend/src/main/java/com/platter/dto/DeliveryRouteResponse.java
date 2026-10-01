package com.platter.dto;

public record DeliveryRouteResponse(String origin, String destination, String distanceText, long distanceMeters, String durationText, long durationSeconds, double originLatitude, double originLongitude, double destinationLatitude, double destinationLongitude) { }
