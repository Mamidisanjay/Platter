package com.platter.controller;

import com.platter.dto.DeliveryLocationRequest;
import com.platter.dto.DeliveryResponse;
import com.platter.dto.DeliveryStatusRequest;
import com.platter.service.DeliveryService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/delivery")
public class DeliveryController {
    private final DeliveryService deliveryService;
    public DeliveryController(DeliveryService deliveryService) { this.deliveryService=deliveryService; }

    @GetMapping("/orders")
    @PreAuthorize("hasRole('DELIVERY_PARTNER')")
    public List<DeliveryResponse> findAssigned(@AuthenticationPrincipal UserDetails user) { return deliveryService.findForPartner(user); }

    @GetMapping("/orders/{id}")
    @PreAuthorize("hasAnyRole('DELIVERY_PARTNER', 'ADMIN')")
    public DeliveryResponse findAssigned(@AuthenticationPrincipal UserDetails user, @PathVariable Long id) { return deliveryService.findForPartner(user, id); }

    @PostMapping("/orders/{id}/accept")
    @PreAuthorize("hasRole('DELIVERY_PARTNER')")
    public DeliveryResponse accept(@AuthenticationPrincipal UserDetails user, @PathVariable Long id) { return deliveryService.accept(user, id); }

    @PatchMapping("/orders/{id}/status")
    @PreAuthorize("hasAnyRole('DELIVERY_PARTNER', 'ADMIN')")
    public DeliveryResponse updateStatus(@AuthenticationPrincipal UserDetails user, @PathVariable Long id, @Valid @RequestBody DeliveryStatusRequest request) { return deliveryService.updateStatus(user, id, request.status()); }

    @PatchMapping("/orders/{id}/location")
    @PreAuthorize("hasAnyRole('DELIVERY_PARTNER', 'ADMIN')")
    public DeliveryResponse updateLocation(@AuthenticationPrincipal UserDetails user, @PathVariable Long id, @Valid @RequestBody DeliveryLocationRequest request) { return deliveryService.updateLocation(user, id, request); }
}
