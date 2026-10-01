package com.platter.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record AdminDashboardResponse(long ordersToday, BigDecimal grossSales, long averageDeliveryTimeMinutes, long activePromotions, Map<String, Long> orderFlow, List<PromoResponse> promoPerformance) { }
