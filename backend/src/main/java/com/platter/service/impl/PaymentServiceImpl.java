package com.platter.service.impl;

import com.platter.dto.CreatePaymentRequest;
import com.platter.dto.PaymentResponse;
import com.platter.entity.Order;
import com.platter.entity.OrderStatus;
import com.platter.entity.Payment;
import com.platter.entity.PaymentStatus;
import com.platter.exception.PaymentException;
import com.platter.repository.OrderRepository;
import com.platter.repository.PaymentRepository;
import com.platter.audit.AuditService;
import com.platter.repository.IdempotencyRecordRepository;
import com.platter.service.OrderStatusService;
import com.platter.service.PaymentService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentServiceImpl implements PaymentService {
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final OrderStatusService orderStatusService;
    private final String webhookSecret;
    private final IdempotencyRecordRepository idempotencyRecordRepository;
    private final AuditService auditService;

    public PaymentServiceImpl(OrderRepository orderRepository, PaymentRepository paymentRepository, OrderStatusService orderStatusService, IdempotencyRecordRepository idempotencyRecordRepository, AuditService auditService, @Value("${platter.stripe.secret-key:}") String secretKey, @Value("${platter.stripe.webhook-secret:}") String webhookSecret) {
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
        this.orderStatusService = orderStatusService;
        this.webhookSecret = webhookSecret;
        this.idempotencyRecordRepository = idempotencyRecordRepository;
        this.auditService = auditService;
        if (!secretKey.isBlank()) Stripe.apiKey = secretKey;
    }

    @Override
    @Transactional
    public PaymentResponse createPayment(UserDetails user, CreatePaymentRequest request, String idempotencyKey) {
        Order order = orderRepository.findByIdAndUserEmailIgnoreCase(request.orderId(), user.getUsername()).orElseThrow(() -> new PaymentException("Order not found"));
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            var previous = idempotencyRecordRepository.findByIdempotencyKeyAndUserId(idempotencyKey, order.getUser().getId());
            if (previous.isPresent()) {
                Payment priorPayment = paymentRepository.findById(previous.get().getResourceId()).orElseThrow(() -> new PaymentException("Idempotent payment record not found"));
                return new PaymentResponse(priorPayment.getId(), order.getId(), priorPayment.getClientSecret(), priorPayment.getAmount(), priorPayment.getStatus());
            }
        }
        if (paymentRepository.findByOrderIdAndOrderUserEmailIgnoreCase(order.getId(), user.getUsername()).isPresent()) throw new PaymentException("Payment already created for this order");
        if (Stripe.apiKey == null || Stripe.apiKey.isBlank()) throw new PaymentException("Stripe is not configured");
        try {
            long amountInCents = order.getTotal().multiply(new BigDecimal("100")).setScale(0, RoundingMode.HALF_UP).longValueExact();
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder().setAmount(amountInCents).setCurrency("usd").putMetadata("orderId", order.getId().toString()).build();
            PaymentIntent intent = PaymentIntent.create(params);
            Payment payment = paymentRepository.save(new Payment(order, intent.getId(), intent.getClientSecret(), order.getTotal()));
            if (idempotencyKey != null && !idempotencyKey.isBlank()) idempotencyRecordRepository.save(new com.platter.entity.IdempotencyRecord(idempotencyKey, order.getUser().getId(), "payment:" + order.getId(), "PAYMENT", payment.getId(), "COMPLETED"));
            return new PaymentResponse(payment.getId(), order.getId(), intent.getClientSecret(), payment.getAmount(), payment.getStatus());
        } catch (StripeException | ArithmeticException exception) {
            throw new PaymentException("Unable to create Stripe payment", exception);
        }
    }

    @Override
    @Transactional
    public void handleWebhook(String payload, String signature) {
        if (webhookSecret.isBlank()) throw new PaymentException("Stripe webhook is not configured");
        final Event event;
        try { event = Webhook.constructEvent(payload, signature, webhookSecret); }
        catch (Exception exception) { throw new PaymentException("Invalid Stripe webhook signature", exception); }
        if (!event.getType().equals("payment_intent.succeeded") && !event.getType().equals("payment_intent.payment_failed")) return;
        StripeObject object = event.getDataObjectDeserializer().getObject().orElseThrow(() -> new PaymentException("Stripe webhook payload is incomplete"));
        PaymentIntent intent = (PaymentIntent) object;
        Payment payment = paymentRepository.findByStripePaymentIntentId(intent.getId()).orElseThrow(() -> new PaymentException("Payment record not found"));
        if (event.getType().equals("payment_intent.succeeded")) {
            if (payment.getStatus() == PaymentStatus.CREATED) { payment.markSucceeded(); orderStatusService.transition(payment.getOrder(), OrderStatus.PAYMENT_SUCCESS); }
        } else if (payment.getStatus() == PaymentStatus.CREATED) {
            payment.markFailed(); orderStatusService.transition(payment.getOrder(), OrderStatus.PAYMENT_FAILED);
        }
        paymentRepository.save(payment);
        auditService.record(payment.getOrder().getUser().getId(), "PAYMENT_UPDATED", "PAYMENT", payment.getId(), null, "status=" + payment.getStatus());
    }
}
