package com.platter.dto;

import com.platter.entity.DeliveryStatus;
import java.time.Instant;

public record DeliveryResponse(Long id, Long orderId, Long deliveryPartnerId, String deliveryPartnerName, DeliveryStatus status, Instant pickupTime, Instant pickedUpAt, Instant deliveredAt, String deliveryAddress, Double latitude, Double longitude, Instant createdAt, Instant updatedAt) { }
