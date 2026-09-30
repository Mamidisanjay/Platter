package com.platter.service.impl;

import com.platter.dto.CartItemResponse;
import com.platter.dto.OrderResponse;
import com.platter.entity.Cart;
import com.platter.entity.CartItem;
import com.platter.entity.Order;
import com.platter.entity.OrderItem;
import com.platter.entity.OrderStatus;
import com.platter.entity.OrderStatusHistory;
import com.platter.entity.UserEntity;
import com.platter.exception.BadRequestException;
import com.platter.exception.InventoryException;
import com.platter.exception.ResourceNotFoundException;
import com.platter.audit.AuditService;
import com.platter.lock.InventoryLockService;
import com.platter.repository.CartRepository;
import com.platter.repository.MenuItemRepository;
import com.platter.repository.IdempotencyRecordRepository;
import com.platter.repository.OrderRepository;
import com.platter.repository.UserRepository;
import com.platter.service.OrderService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderServiceImpl implements OrderService {
    private static final BigDecimal TAX_RATE = new BigDecimal("0.05");
    private static final BigDecimal STANDARD_DELIVERY_FEE = new BigDecimal("3.99");
    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final MenuItemRepository menuItemRepository;
    private final InventoryLockService inventoryLockService;
    private final IdempotencyRecordRepository idempotencyRecordRepository;
    private final AuditService auditService;

    public OrderServiceImpl(OrderRepository orderRepository, CartRepository cartRepository, UserRepository userRepository, MenuItemRepository menuItemRepository, InventoryLockService inventoryLockService, IdempotencyRecordRepository idempotencyRecordRepository, AuditService auditService) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
        this.menuItemRepository = menuItemRepository;
        this.inventoryLockService = inventoryLockService;
        this.idempotencyRecordRepository = idempotencyRecordRepository;
        this.auditService = auditService;
    }

    @Override
    @Transactional
    public OrderResponse createOrder(UserDetails details, String idempotencyKey) {
        UserEntity user = findUser(details);
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            var previous = idempotencyRecordRepository.findByIdempotencyKeyAndUserId(idempotencyKey, user.getId());
            if (previous.isPresent()) return findOrder(details, previous.get().getResourceId());
        }
        Cart cart = cartRepository.findByUserEmailIgnoreCase(user.getEmail()).orElseThrow(() -> new BadRequestException("Cart is empty"));
        if (cart.getItems().isEmpty()) throw new BadRequestException("Cart is empty");
        java.util.Map<Long, String> locks = acquireInventoryLocks(cart);
        try {
        Long restaurantId = cart.getItems().get(0).getMenuItem().getRestaurant().getId();
        if (cart.getItems().stream().anyMatch(item -> !item.getMenuItem().getRestaurant().getId().equals(restaurantId))) throw new BadRequestException("Order items must come from one restaurant");
        BigDecimal subtotal = cart.getItems().stream().map(item -> item.getMenuItem().getPrice().multiply(BigDecimal.valueOf(item.getQuantity()))).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal tax = subtotal.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal deliveryFee = STANDARD_DELIVERY_FEE;
        BigDecimal total = subtotal.add(tax).add(deliveryFee).setScale(2, RoundingMode.HALF_UP);
        Order order = new Order(user, cart.getItems().get(0).getMenuItem().getRestaurant(), subtotal, BigDecimal.ZERO, tax, deliveryFee, total);
        order.addStatusHistory(new OrderStatusHistory(order, OrderStatus.CREATED));
        order.addStatusHistory(new OrderStatusHistory(order, OrderStatus.PAYMENT_PENDING));
        for (CartItem item : cart.getItems()) order.addItem(new OrderItem(order, item.getMenuItem().getName(), item.getMenuItem().getPrice(), item.getQuantity()));
        Order saved = orderRepository.save(order);
        auditService.record(user.getId(), "ORDER_CREATED", "ORDER", saved.getId(), null, "status=PAYMENT_PENDING");
        if (idempotencyKey != null && !idempotencyKey.isBlank()) idempotencyRecordRepository.save(new com.platter.entity.IdempotencyRecord(idempotencyKey, user.getId(), "order:" + user.getId(), "ORDER", saved.getId(), "COMPLETED"));
        cart.getItems().clear();
        cartRepository.save(cart);
        return toResponse(saved);
        } finally {
            locks.forEach(inventoryLockService::release);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> findOrders(UserDetails details) { return orderRepository.findByUserEmailIgnoreCaseOrderByCreatedAtDesc(details.getUsername()).stream().map(this::toResponse).toList(); }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse findOrder(UserDetails details, Long id) { return toResponse(orderRepository.findByIdAndUserEmailIgnoreCase(id, details.getUsername()).orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id))); }

    @Override
    @Transactional
    public OrderResponse cancelOrder(UserDetails details, Long id) {
        Order order = orderRepository.findByIdAndUserEmailIgnoreCase(id, details.getUsername()).orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));
        if (order.getStatus() != OrderStatus.CREATED && order.getStatus() != OrderStatus.PAYMENT_PENDING) throw new BadRequestException("Order cannot be cancelled after payment processing");
        order.setStatus(OrderStatus.CANCELLED);
        order.addStatusHistory(new OrderStatusHistory(order, OrderStatus.CANCELLED));
        auditService.record(order.getUser().getId(), "ORDER_CANCELLED", "ORDER", order.getId(), null, "status=CANCELLED");
        return toResponse(order);
    }

    private UserEntity findUser(UserDetails details) { return userRepository.findByEmailIgnoreCase(details.getUsername()).orElseThrow(() -> new ResourceNotFoundException("User not found")); }

    private java.util.Map<Long, String> acquireInventoryLocks(Cart cart) {
        java.util.Map<Long, String> locks = new java.util.LinkedHashMap<>();
        cart.getItems().stream().map(item -> item.getMenuItem().getId()).distinct().sorted().forEach(id -> {
            String token = inventoryLockService.tryLock(id);
            if (token == null) {
                locks.forEach(inventoryLockService::release);
                throw new InventoryException("Inventory is currently being reserved. Please retry.");
            }
            locks.put(id, token);
        });
        cart.getItems().forEach(item -> {
            var menuItem = menuItemRepository.findById(item.getMenuItem().getId()).orElseThrow(() -> new ResourceNotFoundException("Menu item not found: " + item.getMenuItem().getId()));
            if (menuItem.getInventory() < item.getQuantity()) throw new InventoryException("Insufficient inventory for " + menuItem.getName());
            menuItem.reserve(item.getQuantity());
        });
        return locks;
    }

    private OrderResponse toResponse(Order order) {
        List<CartItemResponse> items = order.getItems().stream().map(item -> new CartItemResponse(null, null, item.getItemName(), order.getRestaurant().getName(), item.getUnitPrice(), item.getQuantity(), item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))).toList();
        return new OrderResponse(order.getId(), order.getRestaurant().getName(), order.getStatus(), order.getSubtotal(), order.getDiscount(), order.getTax(), order.getDeliveryFee(), order.getTotal(), order.getCreatedAt(), items);
    }
}
