package com.platter.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.platter.entity.Delivery;
import com.platter.entity.Order;
import com.platter.entity.Restaurant;
import com.platter.exception.ForbiddenException;
import com.platter.exception.GoogleMapsException;
import com.platter.location.GeocodedLocation;
import com.platter.location.GoogleMapsClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

@ExtendWith(MockitoExtension.class)
class DeliveryRouteServiceTest {
    @Mock private DeliveryService deliveryService;
    @Mock private GoogleMapsClient googleMapsClient;
    @Mock private Delivery delivery;
    @Mock private Order order;
    @Mock private Restaurant restaurant;
    @InjectMocks private DeliveryRouteService routeService;

    private final UserDetails customer = User.withUsername("customer@example.com").password("ignored").roles("CUSTOMER").build();

    @Test
    void combinesGeocodingAndDistanceIntoRouteResponse() {
        when(deliveryService.getEntityByOrder(customer, 10L)).thenReturn(delivery);
        when(delivery.getOrder()).thenReturn(order);
        when(order.getRestaurant()).thenReturn(restaurant);
        when(restaurant.getName()).thenReturn("Saffron Street");
        when(delivery.getDeliveryAddress()).thenReturn("12 Main Street");
        when(delivery.getLatitude()).thenReturn(null);
        GeocodedLocation origin = new GeocodedLocation("Saffron Street, Bengaluru", 12.97, 77.59);
        GeocodedLocation destination = new GeocodedLocation("12 Main Street, Bengaluru", 12.98, 77.60);
        when(googleMapsClient.geocode("Saffron Street")).thenReturn(origin);
        when(googleMapsClient.geocode("12 Main Street")).thenReturn(destination);
        when(googleMapsClient.distance(origin, destination)).thenReturn(new GoogleMapsClient.DistanceResult("2.4 km", 2400, "12 mins", 720));

        var response = routeService.route(customer, 10L);

        assertThat(response.distanceMeters()).isEqualTo(2400);
        assertThat(response.durationSeconds()).isEqualTo(720);
        assertThat(response.origin()).contains("Saffron");
        assertThat(response.destination()).contains("Main");
    }

    @Test
    void preservesDeliveryCoordinatesWithoutSecondGeocodeCall() {
        when(deliveryService.getEntityByOrder(customer, 10L)).thenReturn(delivery);
        when(delivery.getOrder()).thenReturn(order);
        when(order.getRestaurant()).thenReturn(restaurant);
        when(restaurant.getName()).thenReturn("Tokyo Table");
        when(delivery.getDeliveryAddress()).thenReturn("Saved address");
        when(delivery.getLatitude()).thenReturn(12.98);
        when(delivery.getLongitude()).thenReturn(77.60);
        GeocodedLocation origin = new GeocodedLocation("Tokyo Table, Bengaluru", 12.97, 77.59);
        when(googleMapsClient.geocode("Tokyo Table")).thenReturn(origin);
        when(googleMapsClient.distance(origin, new GeocodedLocation("Saved address", 12.98, 77.60))).thenReturn(new GoogleMapsClient.DistanceResult("1 km", 1000, "5 mins", 300));

        assertThat(routeService.route(customer, 10L).distanceText()).isEqualTo("1 km");
    }

    @Test
    void propagatesGoogleMapsFailure() {
        when(deliveryService.getEntityByOrder(customer, 10L)).thenReturn(delivery);
        when(delivery.getOrder()).thenReturn(order);
        when(order.getRestaurant()).thenReturn(restaurant);
        when(restaurant.getName()).thenReturn("Saffron Street");
        when(googleMapsClient.geocode("Saffron Street")).thenThrow(new GoogleMapsException("Google Maps is unavailable"));

        assertThatThrownBy(() -> routeService.route(customer, 10L)).isInstanceOf(GoogleMapsException.class);
    }

    @Test
    void preservesOwnershipFailureWithoutCallingGoogleMaps() {
        when(deliveryService.getEntityByOrder(customer, 10L)).thenThrow(new ForbiddenException("You cannot access this delivery"));

        assertThatThrownBy(() -> routeService.route(customer, 10L)).isInstanceOf(ForbiddenException.class);
    }
}
