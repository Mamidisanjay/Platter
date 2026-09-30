package com.platter.service;

import com.platter.dto.OrderStatusUpdate;
import com.platter.entity.Order;
import java.time.Instant;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderEventPublisher {
    private final SimpMessagingTemplate messagingTemplate;

    public OrderEventPublisher(SimpMessagingTemplate messagingTemplate) { this.messagingTemplate = messagingTemplate; }

    public void publish(Order order) {
        messagingTemplate.convertAndSend("/topic/orders/" + order.getId(), new OrderStatusUpdate(order.getId(), order.getStatus(), Instant.now()));
    }
}
