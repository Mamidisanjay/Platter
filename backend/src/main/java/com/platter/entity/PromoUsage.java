package com.platter.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "promo_usages")
public class PromoUsage {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    private Long promoId;
    private Long userId;
    private Long orderId;
    private Instant usedAt;
    protected PromoUsage() { }
    public PromoUsage(Long promoId, Long userId, Long orderId) { this.promoId=promoId; this.userId=userId; this.orderId=orderId; this.usedAt=Instant.now(); }
}
