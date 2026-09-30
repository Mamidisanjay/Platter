package com.platter.dto;

import com.platter.entity.DiscountType;
import java.math.BigDecimal;
import java.time.Instant;

public record PromoResponse(Long id, String code, DiscountType discountType, BigDecimal discountValue, BigDecimal minimumOrderAmount, BigDecimal maximumDiscount, Instant startTime, Instant expiryTime, int usageLimit, int usageCount, int perUserLimit, Long restaurantId, boolean active) { }
