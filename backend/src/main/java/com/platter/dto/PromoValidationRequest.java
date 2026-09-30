package com.platter.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

public record PromoValidationRequest(@NotBlank String code, @NotNull @PositiveOrZero BigDecimal subtotal, Long restaurantId) { }
