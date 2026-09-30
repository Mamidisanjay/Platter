package com.platter.repository;

import com.platter.entity.Payment;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByStripePaymentIntentId(String paymentIntentId);
    Optional<Payment> findByOrderIdAndOrderUserEmailIgnoreCase(Long orderId, String email);
}
