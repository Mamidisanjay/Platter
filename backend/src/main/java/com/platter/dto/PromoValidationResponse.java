package com.platter.dto;

import java.math.BigDecimal;

public record PromoValidationResponse(String code, BigDecimal discount, BigDecimal totalAfterDiscount) { }
