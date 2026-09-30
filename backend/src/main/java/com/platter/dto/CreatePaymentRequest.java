package com.platter.dto;

import jakarta.validation.constraints.NotNull;

public record CreatePaymentRequest(@NotNull Long orderId) { }
