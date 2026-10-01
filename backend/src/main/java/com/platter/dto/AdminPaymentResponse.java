package com.platter.dto;

import com.platter.entity.PaymentStatus;
import java.math.BigDecimal;

public record AdminPaymentResponse(Long id, Long orderId, BigDecimal amount, PaymentStatus status, String stripePaymentIntentId) { }
