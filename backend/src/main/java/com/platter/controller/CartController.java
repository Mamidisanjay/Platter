package com.platter.controller;

import com.platter.dto.CartItemRequest;
import com.platter.dto.CartResponse;
import com.platter.service.CartService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cart")
@Validated
public class CartController {
    private final CartService cartService;

    public CartController(CartService cartService) { this.cartService = cartService; }

    @GetMapping
    public CartResponse getCart(@AuthenticationPrincipal UserDetails user) { return cartService.getCart(user); }

    @PostMapping("/items")
    public CartResponse addItem(@AuthenticationPrincipal UserDetails user, @Valid @RequestBody CartItemRequest request) { return cartService.addItem(user, request); }

    @PutMapping("/items/{id}")
    public CartResponse updateItem(@AuthenticationPrincipal UserDetails user, @PathVariable Long id, @RequestParam @Min(1) int quantity) { return cartService.updateItem(user, id, quantity); }

    @DeleteMapping("/items/{id}")
    public void removeItem(@AuthenticationPrincipal UserDetails user, @PathVariable Long id) { cartService.removeItem(user, id); }

    @DeleteMapping
    public void clear(@AuthenticationPrincipal UserDetails user) { cartService.clear(user); }
}
