package com.platter.service;

import com.platter.dto.CartItemRequest;
import com.platter.dto.CartResponse;
import org.springframework.security.core.userdetails.UserDetails;

public interface CartService {
    CartResponse getCart(UserDetails user);
    CartResponse addItem(UserDetails user, CartItemRequest request);
    CartResponse updateItem(UserDetails user, Long itemId, int quantity);
    void removeItem(UserDetails user, Long itemId);
    void clear(UserDetails user);
}
