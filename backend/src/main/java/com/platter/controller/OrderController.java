package com.platter.controller;

import com.platter.dto.OrderResponse;
import com.platter.service.OrderService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) { this.orderService = orderService; }

    @PostMapping
    public ResponseEntity<OrderResponse> create(@AuthenticationPrincipal UserDetails user, @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) { return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrder(user, idempotencyKey)); }

    @GetMapping
    public List<OrderResponse> findAll(@AuthenticationPrincipal UserDetails user) { return orderService.findOrders(user); }

    @GetMapping("/{id}")
    public OrderResponse findById(@AuthenticationPrincipal UserDetails user, @PathVariable Long id) { return orderService.findOrder(user, id); }

    @PostMapping("/{id}/cancel")
    public OrderResponse cancel(@AuthenticationPrincipal UserDetails user, @PathVariable Long id) { return orderService.cancelOrder(user, id); }
}
