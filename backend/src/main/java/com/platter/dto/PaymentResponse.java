package com.platter.dto;

import com.platter.entity.PaymentStatus;
import java.math.BigDecimal;

public record PaymentResponse(Long paymentId, Long orderId, String clientSecret, BigDecimal amount, PaymentStatus status) { }
