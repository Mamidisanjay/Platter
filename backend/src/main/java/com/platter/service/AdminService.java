package com.platter.service;

import com.platter.dto.AdminDashboardResponse;
import com.platter.dto.AdminPaymentResponse;
import com.platter.dto.AuditLogResponse;
import com.platter.dto.CartItemResponse;
import com.platter.dto.OrderResponse;
import com.platter.dto.PromoResponse;
import com.platter.dto.RestaurantResponse;
import com.platter.dto.ReviewResponse;
import com.platter.dto.UserResponse;
import com.platter.entity.Order;
import com.platter.entity.OrderStatus;
import com.platter.entity.Payment;
import com.platter.entity.PromoCode;
import com.platter.entity.Restaurant;
import com.platter.entity.Review;
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
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AdminService {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final PaymentRepository paymentRepository;
    private final PromoCodeRepository promoCodeRepository;
    private final ReviewRepository reviewRepository;
    private final AuditLogRepository auditLogRepository;
    private final DeliveryRepository deliveryRepository;

    public AdminService(OrderRepository orderRepository, UserRepository userRepository, RestaurantRepository restaurantRepository, PaymentRepository paymentRepository, PromoCodeRepository promoCodeRepository, ReviewRepository reviewRepository, AuditLogRepository auditLogRepository, DeliveryRepository deliveryRepository) { this.orderRepository=orderRepository; this.userRepository=userRepository; this.restaurantRepository=restaurantRepository; this.paymentRepository=paymentRepository; this.promoCodeRepository=promoCodeRepository; this.reviewRepository=reviewRepository; this.auditLogRepository=auditLogRepository; this.deliveryRepository=deliveryRepository; }

    public AdminDashboardResponse dashboard() {
        Instant today = Instant.now().truncatedTo(ChronoUnit.DAYS);
        List<Order> orders = orderRepository.findAll();
        long ordersToday = orders.stream().filter(order -> order.getCreatedAt().isAfter(today)).count();
        BigDecimal grossSales = orders.stream().filter(order -> order.getStatus() != OrderStatus.CANCELLED && order.getStatus() != OrderStatus.PAYMENT_FAILED).map(Order::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        long averageDelivery = Math.round(deliveryRepository.findAll().stream().filter(delivery -> delivery.getDeliveredAt() != null).mapToLong(delivery -> ChronoUnit.MINUTES.between(delivery.getOrder().getCreatedAt(), delivery.getDeliveredAt())).average().orElse(0));
        Map<String, Long> orderFlow = orders.stream().collect(Collectors.groupingBy(order -> order.getStatus().name(), Collectors.counting()));
        List<PromoResponse> promos = promoCodeRepository.findAll().stream().map(this::toPromoResponse).toList();
        long activePromotions = promos.stream().filter(PromoResponse::active).count();
        return new AdminDashboardResponse(ordersToday, grossSales, averageDelivery, activePromotions, orderFlow, promos);
    }

    public List<OrderResponse> orders() { return orderRepository.findAll().stream().map(this::toOrderResponse).toList(); }
    public List<UserResponse> users() { return userRepository.findAll().stream().map(user -> new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole())).toList(); }
    public List<RestaurantResponse> restaurants() { return restaurantRepository.findAll().stream().map(this::toRestaurantResponse).toList(); }
    public List<AdminPaymentResponse> payments() { return paymentRepository.findAll().stream().map(payment -> new AdminPaymentResponse(payment.getId(), payment.getOrder().getId(), payment.getAmount(), payment.getStatus(), payment.getStripePaymentIntentId())).toList(); }
    public List<PromoResponse> promos() { return promoCodeRepository.findAll().stream().map(this::toPromoResponse).toList(); }
    public List<AuditLogResponse> auditLogs() { return auditLogRepository.findAll().stream().map(log -> new AuditLogResponse(log.getId(), log.getUserId(), log.getAction(), log.getEntityType(), log.getEntityId(), log.getTimestamp(), log.getIpAddress(), log.getMetadata())).toList(); }
    public List<ReviewResponse> reviews() { return reviewRepository.findAll().stream().map(review -> new ReviewResponse(review.getId(), review.getUser().getId(), review.getUser().getName(), review.getRating(), review.getContent(), review.getCreatedAt(), review.getUpdatedAt(), false)).toList(); }

    private OrderResponse toOrderResponse(Order order) { List<CartItemResponse> items = order.getItems().stream().map(item -> new CartItemResponse(null, null, item.getItemName(), order.getRestaurant().getName(), item.getUnitPrice(), item.getQuantity(), item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))).toList(); return new OrderResponse(order.getId(), order.getRestaurant().getName(), order.getStatus(), order.getSubtotal(), order.getDiscount(), order.getTax(), order.getDeliveryFee(), order.getTotal(), order.getCreatedAt(), items); }
    private RestaurantResponse toRestaurantResponse(Restaurant restaurant) { return new RestaurantResponse(restaurant.getId(), restaurant.getName(), restaurant.getCuisine(), restaurant.getRating(), restaurant.getDeliveryTimeMinutes(), restaurant.getPriceTier(), restaurant.getImageUrl(), restaurant.getTag(), restaurant.getAccent(), restaurant.isAvailable(), restaurant.getReviewCount()); }
    private PromoResponse toPromoResponse(PromoCode promo) { return new PromoResponse(promo.getId(), promo.getCode(), promo.getDiscountType(), promo.getDiscountValue(), promo.getMinimumOrderAmount(), promo.getMaximumDiscount(), promo.getStartTime(), promo.getExpiryTime(), promo.getUsageLimit(), promo.getUsageCount(), promo.getPerUserLimit(), promo.getRestaurantId(), promo.isActive()); }
}
