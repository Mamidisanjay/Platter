package com.platter.dto;

import jakarta.validation.constraints.NotNull;

public record DeliveryLocationRequest(@NotNull Double latitude, @NotNull Double longitude) { }
