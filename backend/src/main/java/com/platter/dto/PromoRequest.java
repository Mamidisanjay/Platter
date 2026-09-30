package com.platter.dto;

import com.platter.entity.DiscountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.Instant;

public record PromoRequest(@NotBlank String code, @NotNull DiscountType discountType, @NotNull @Positive BigDecimal discountValue, @NotNull BigDecimal minimumOrderAmount, BigDecimal maximumDiscount, @NotNull Instant startTime, @NotNull Instant expiryTime, int usageLimit, int perUserLimit, Long restaurantId) { }
