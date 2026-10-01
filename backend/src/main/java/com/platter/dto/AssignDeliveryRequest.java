package com.platter.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AssignDeliveryRequest(@NotNull Long orderId, @NotNull Long deliveryPartnerId, @NotBlank @Size(max = 500) String deliveryAddress) { }
