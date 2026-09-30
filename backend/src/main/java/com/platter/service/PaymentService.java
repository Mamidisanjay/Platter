package com.platter.service;

import com.platter.dto.CreatePaymentRequest;
import com.platter.dto.PaymentResponse;
import org.springframework.security.core.userdetails.UserDetails;

public interface PaymentService {
    PaymentResponse createPayment(UserDetails user, CreatePaymentRequest request, String idempotencyKey);
    void handleWebhook(String payload, String signature);
}
