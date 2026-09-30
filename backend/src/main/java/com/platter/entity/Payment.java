package com.platter.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "payments")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;
    private String stripePaymentIntentId;
    private String clientSecret;
    private BigDecimal amount;
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;
    private Instant createdAt;
    private Instant updatedAt;

    protected Payment() { }
    public Payment(Order order, String stripePaymentIntentId, String clientSecret, BigDecimal amount) { this.order = order; this.stripePaymentIntentId = stripePaymentIntentId; this.clientSecret = clientSecret; this.amount = amount; this.status = PaymentStatus.CREATED; this.createdAt = Instant.now(); this.updatedAt = this.createdAt; }
    public Long getId() { return id; }
    public Order getOrder() { return order; }
    public String getStripePaymentIntentId() { return stripePaymentIntentId; }
    public String getClientSecret() { return clientSecret; }
    public BigDecimal getAmount() { return amount; }
    public PaymentStatus getStatus() { return status; }
    public void markSucceeded() { this.status = PaymentStatus.SUCCEEDED; this.updatedAt = Instant.now(); }
    public void markFailed() { this.status = PaymentStatus.FAILED; this.updatedAt = Instant.now(); }
}
