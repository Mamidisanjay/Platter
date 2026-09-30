package com.platter.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "promo_codes")
public class PromoCode {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    private String code;
    @Enumerated(EnumType.STRING) private DiscountType discountType;
    private BigDecimal discountValue;
    private BigDecimal minimumOrderAmount;
    private BigDecimal maximumDiscount;
    private Instant startTime;
    private Instant expiryTime;
    private int usageLimit;
    private int usageCount;
    private int perUserLimit;
    private Long restaurantId;
    private boolean active;
    protected PromoCode() { }
    public PromoCode(String code, DiscountType type, BigDecimal value, BigDecimal minimum, BigDecimal maximum, Instant start, Instant expiry, int usageLimit, int perUserLimit, Long restaurantId) { this.code=code; this.discountType=type; this.discountValue=value; this.minimumOrderAmount=minimum; this.maximumDiscount=maximum; this.startTime=start; this.expiryTime=expiry; this.usageLimit=usageLimit; this.perUserLimit=perUserLimit; this.restaurantId=restaurantId; this.active=true; }
    public Long getId(){return id;} public String getCode(){return code;} public DiscountType getDiscountType(){return discountType;} public BigDecimal getDiscountValue(){return discountValue;} public BigDecimal getMinimumOrderAmount(){return minimumOrderAmount;} public BigDecimal getMaximumDiscount(){return maximumDiscount;} public Instant getStartTime(){return startTime;} public Instant getExpiryTime(){return expiryTime;} public int getUsageLimit(){return usageLimit;} public int getUsageCount(){return usageCount;} public int getPerUserLimit(){return perUserLimit;} public Long getRestaurantId(){return restaurantId;} public boolean isActive(){return active;}
    public void deactivate(){active=false;} public void incrementUsage(){usageCount++;}
    public void update(String code, DiscountType type, BigDecimal value, BigDecimal minimum, BigDecimal maximum, Instant start, Instant expiry, int usageLimit, int perUserLimit, Long restaurantId) { this.code=code; this.discountType=type; this.discountValue=value; this.minimumOrderAmount=minimum; this.maximumDiscount=maximum; this.startTime=start; this.expiryTime=expiry; this.usageLimit=usageLimit; this.perUserLimit=perUserLimit; this.restaurantId=restaurantId; }
}
