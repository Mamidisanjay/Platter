package com.platter.controller;

import com.platter.dto.DeliveryRouteResponse;
import com.platter.service.DeliveryRouteService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class DeliveryRouteController {
    private final DeliveryRouteService deliveryRouteService;
    public DeliveryRouteController(DeliveryRouteService deliveryRouteService) { this.deliveryRouteService=deliveryRouteService; }
    @GetMapping("/{orderId}/delivery/route")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'DELIVERY_PARTNER', 'ADMIN')")
    public DeliveryRouteResponse route(@AuthenticationPrincipal UserDetails user, @PathVariable Long orderId) { return deliveryRouteService.route(user, orderId); }
}
