package com.platter.service;

import com.platter.dto.OrderResponse;
import java.util.List;
import org.springframework.security.core.userdetails.UserDetails;

public interface OrderService {
    OrderResponse createOrder(UserDetails user, String idempotencyKey);
    List<OrderResponse> findOrders(UserDetails user);
    OrderResponse findOrder(UserDetails user, Long id);
    OrderResponse cancelOrder(UserDetails user, Long id);
}
