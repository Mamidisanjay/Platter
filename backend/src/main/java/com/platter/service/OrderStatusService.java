package com.platter.service;

import com.platter.entity.Order;
import com.platter.entity.OrderStatus;
import com.platter.exception.BadRequestException;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderStatusService {
    private static final Map<OrderStatus, Set<OrderStatus>> TRANSITIONS = Map.of(
            OrderStatus.CREATED, EnumSet.of(OrderStatus.PAYMENT_PENDING, OrderStatus.CANCELLED),
            OrderStatus.PAYMENT_PENDING, EnumSet.of(OrderStatus.PAYMENT_SUCCESS, OrderStatus.PAYMENT_FAILED, OrderStatus.CANCELLED),
            OrderStatus.PAYMENT_SUCCESS, EnumSet.of(OrderStatus.CONFIRMED),
            OrderStatus.CONFIRMED, EnumSet.of(OrderStatus.PREPARING, OrderStatus.CANCELLED),
            OrderStatus.PREPARING, EnumSet.of(OrderStatus.READY_FOR_PICKUP),
            OrderStatus.READY_FOR_PICKUP, EnumSet.of(OrderStatus.OUT_FOR_DELIVERY),
            OrderStatus.OUT_FOR_DELIVERY, EnumSet.of(OrderStatus.DELIVERED),
            OrderStatus.DELIVERED, EnumSet.noneOf(OrderStatus.class),
            OrderStatus.CANCELLED, EnumSet.noneOf(OrderStatus.class),
            OrderStatus.PAYMENT_FAILED, EnumSet.of(OrderStatus.PAYMENT_PENDING));

    private final OrderEventPublisher eventPublisher;

    public OrderStatusService(OrderEventPublisher eventPublisher) { this.eventPublisher = eventPublisher; }

    @Transactional
    public Order transition(Order order, OrderStatus target) {
        if (!TRANSITIONS.getOrDefault(order.getStatus(), Set.of()).contains(target)) throw new BadRequestException("Invalid order transition from " + order.getStatus() + " to " + target);
        order.setStatus(target);
        order.addStatusHistory(new com.platter.entity.OrderStatusHistory(order, target));
        eventPublisher.publish(order);
        return order;
    }
}
