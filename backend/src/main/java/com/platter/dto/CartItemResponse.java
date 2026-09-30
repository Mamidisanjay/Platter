package com.platter.dto;

import java.math.BigDecimal;

public record CartItemResponse(Long id, Long menuItemId, String name, String restaurant, BigDecimal unitPrice, int quantity, BigDecimal lineTotal) { }
