package com.platter.service;

import com.platter.dto.DeliveryRouteResponse;
import com.platter.entity.Delivery;
import com.platter.location.GeocodedLocation;
import com.platter.location.GoogleMapsClient;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeliveryRouteService {
    private final DeliveryService deliveryService;
    private final GoogleMapsClient googleMapsClient;

    public DeliveryRouteService(DeliveryService deliveryService, GoogleMapsClient googleMapsClient) { this.deliveryService=deliveryService; this.googleMapsClient=googleMapsClient; }

    @Transactional(readOnly = true)
    public DeliveryRouteResponse route(UserDetails user, Long orderId) {
        Delivery delivery = deliveryService.getEntityByOrder(user, orderId);
        GeocodedLocation origin = googleMapsClient.geocode(delivery.getOrder().getRestaurant().getName());
        GeocodedLocation destination = delivery.getLatitude() != null && delivery.getLongitude() != null
                ? new GeocodedLocation(delivery.getDeliveryAddress(), delivery.getLatitude(), delivery.getLongitude())
                : googleMapsClient.geocode(delivery.getDeliveryAddress());
        GoogleMapsClient.DistanceResult distance = googleMapsClient.distance(origin, destination);
        return new DeliveryRouteResponse(origin.formattedAddress(), destination.formattedAddress(), distance.distanceText(), distance.distanceMeters(), distance.durationText(), distance.durationSeconds(), origin.latitude(), origin.longitude(), destination.latitude(), destination.longitude());
    }
}
