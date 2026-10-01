package com.platter.controller;

import com.platter.dto.AssignDeliveryRequest;
import com.platter.dto.DeliveryResponse;
import com.platter.service.DeliveryService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/deliveries")
@PreAuthorize("hasRole('ADMIN')")
public class AdminDeliveryController {
    private final DeliveryService deliveryService;
    public AdminDeliveryController(DeliveryService deliveryService) { this.deliveryService=deliveryService; }
    @GetMapping public List<DeliveryResponse> findAll() { return deliveryService.findAll(); }
    @PostMapping public ResponseEntity<DeliveryResponse> assign(@Valid @RequestBody AssignDeliveryRequest request) { return ResponseEntity.status(HttpStatus.CREATED).body(deliveryService.assign(request, false)); }
    @PutMapping("/{id}/assignment") public DeliveryResponse reassign(@PathVariable Long id, @Valid @RequestBody AssignDeliveryRequest request) { if (!id.equals(request.orderId())) throw new com.platter.exception.BadRequestException("Order id does not match delivery assignment path"); return deliveryService.assign(request, true); }
}
