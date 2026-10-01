package com.platter.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.platter.entity.Order;
import com.platter.entity.OrderStatus;
import com.platter.entity.PromoCode;
import com.platter.repository.AuditLogRepository;
import com.platter.repository.DeliveryRepository;
import com.platter.repository.OrderRepository;
import com.platter.repository.PaymentRepository;
import com.platter.repository.PromoCodeRepository;
import com.platter.repository.RestaurantRepository;
import com.platter.repository.ReviewRepository;
import com.platter.repository.UserRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {
    @Mock private OrderRepository orderRepository;
    @Mock private UserRepository userRepository;
    @Mock private RestaurantRepository restaurantRepository;
    @Mock private PaymentRepository paymentRepository;
    @Mock private PromoCodeRepository promoCodeRepository;
    @Mock private ReviewRepository reviewRepository;
    @Mock private AuditLogRepository auditLogRepository;
    @Mock private DeliveryRepository deliveryRepository;
    @Mock private Order order;
    @Mock private PromoCode promo;
    @InjectMocks private AdminService adminService;

    @Test
    void dashboardUsesPersistedOrderAndPromoData() {
        when(orderRepository.findAll()).thenReturn(List.of(order));
        when(order.getCreatedAt()).thenReturn(Instant.now());
        when(order.getStatus()).thenReturn(OrderStatus.CONFIRMED);
        when(order.getTotal()).thenReturn(new BigDecimal("24.50"));
        when(deliveryRepository.findAll()).thenReturn(List.of());
        when(promoCodeRepository.findAll()).thenReturn(List.of(promo));
        when(promo.getId()).thenReturn(1L);
        when(promo.getCode()).thenReturn("WELCOME");
        when(promo.isActive()).thenReturn(true);
        when(promo.getUsageCount()).thenReturn(0);
        when(promo.getUsageLimit()).thenReturn(100);
        when(promo.getPerUserLimit()).thenReturn(1);
        when(promo.getDiscountType()).thenReturn(com.platter.entity.DiscountType.FIXED);
        when(promo.getDiscountValue()).thenReturn(new BigDecimal("5"));
        when(promo.getMinimumOrderAmount()).thenReturn(BigDecimal.ZERO);
        when(promo.getMaximumDiscount()).thenReturn(null);
        when(promo.getStartTime()).thenReturn(Instant.now().minusSeconds(60));
        when(promo.getExpiryTime()).thenReturn(Instant.now().plusSeconds(3600));
        when(promo.getRestaurantId()).thenReturn(null);

        var dashboard = adminService.dashboard();

        assertThat(dashboard.ordersToday()).isEqualTo(1);
        assertThat(dashboard.grossSales()).isEqualByComparingTo("24.50");
        assertThat(dashboard.activePromotions()).isEqualTo(1);
        assertThat(dashboard.orderFlow()).containsEntry("CONFIRMED", 1L);
    }
}
