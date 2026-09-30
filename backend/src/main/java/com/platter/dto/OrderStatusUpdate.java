package com.platter.dto;

import com.platter.entity.OrderStatus;
import java.time.Instant;

public record OrderStatusUpdate(Long orderId, OrderStatus status, Instant updatedAt) { }
