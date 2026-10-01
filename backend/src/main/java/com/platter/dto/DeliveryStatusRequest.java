package com.platter.dto;

import com.platter.entity.DeliveryStatus;
import jakarta.validation.constraints.NotNull;

public record DeliveryStatusRequest(@NotNull DeliveryStatus status) { }
