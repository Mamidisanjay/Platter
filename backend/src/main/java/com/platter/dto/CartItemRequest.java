package com.platter.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CartItemRequest(@NotNull Long menuItemId, @Min(1) int quantity) { }
