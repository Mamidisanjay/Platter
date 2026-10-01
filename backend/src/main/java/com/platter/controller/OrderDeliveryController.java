package com.platter.controller;

import com.platter.dto.DeliveryResponse;
import com.platter.service.DeliveryService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderDeliveryController {
    private final DeliveryService deliveryService;
    public OrderDeliveryController(DeliveryService deliveryService) { this.deliveryService=deliveryService; }
    @GetMapping("/{orderId}/delivery")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'DELIVERY_PARTNER', 'ADMIN')")
    public DeliveryResponse find(@AuthenticationPrincipal UserDetails user, @PathVariable Long orderId) { return deliveryService.getByOrder(user, orderId); }
}
