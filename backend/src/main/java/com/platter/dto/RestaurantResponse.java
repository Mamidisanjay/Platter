package com.platter.dto;

import java.math.BigDecimal;

public record RestaurantResponse(
        Long id,
        String name,
        String cuisine,
        BigDecimal rating,
        Integer deliveryTimeMinutes,
        String priceTier,
        String imageUrl,
        String tag,
        String accent,
        boolean available) { }
