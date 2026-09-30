package com.platter.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record MenuItemRequest(
        @NotBlank @Size(max = 160) String name,
        @Size(max = 500) String description,
        @NotNull @PositiveOrZero BigDecimal price,
        @Size(max = 500) String imageUrl,
        @NotNull Long categoryId) { }
