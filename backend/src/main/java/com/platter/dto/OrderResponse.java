package com.platter.dto;

import com.platter.entity.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponse(Long id, String restaurant, OrderStatus status, BigDecimal subtotal, BigDecimal discount, BigDecimal tax, BigDecimal deliveryFee, BigDecimal total, Instant createdAt, List<CartItemResponse> items) { }
