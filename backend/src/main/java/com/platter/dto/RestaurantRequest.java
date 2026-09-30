package com.platter.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record RestaurantRequest(
        @NotBlank @Size(max = 120) String name,
        @NotBlank @Size(max = 255) String cuisine,
        @NotNull @DecimalMin("0.0") @DecimalMax("5.0") BigDecimal rating,
        @NotNull @Min(1) Integer deliveryTimeMinutes,
        @NotBlank @Size(max = 10) String priceTier,
        @Size(max = 500) String imageUrl,
        @Size(max = 80) String tag,
        @Size(max = 20) String accent) { }
