package com.platter.controller;

import com.platter.dto.CreatePaymentRequest;
import com.platter.dto.PaymentResponse;
import com.platter.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) { this.paymentService = paymentService; }

    @PostMapping("/create")
    public PaymentResponse create(@AuthenticationPrincipal UserDetails user, @Valid @RequestBody CreatePaymentRequest request, @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) { return paymentService.createPayment(user, request, idempotencyKey); }

    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(@RequestHeader("Stripe-Signature") String signature, @RequestBody String payload) { paymentService.handleWebhook(payload, signature); return ResponseEntity.ok().build(); }
}
