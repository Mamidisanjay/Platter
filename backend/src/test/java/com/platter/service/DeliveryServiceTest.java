package com.platter.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.platter.audit.AuditService;
import com.platter.dto.AssignDeliveryRequest;
import com.platter.dto.DeliveryStatusRequest;
import com.platter.entity.Delivery;
import com.platter.entity.DeliveryStatus;
import com.platter.entity.Order;
import com.platter.entity.OrderStatus;
import com.platter.entity.Role;
import com.platter.entity.UserEntity;
import com.platter.exception.BadRequestException;
import com.platter.exception.ConflictException;
import com.platter.exception.ForbiddenException;
import com.platter.exception.ResourceNotFoundException;
import com.platter.repository.DeliveryRepository;
import com.platter.repository.OrderRepository;
import com.platter.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

@ExtendWith(MockitoExtension.class)
class DeliveryServiceTest {
    @Mock private DeliveryRepository deliveryRepository;
    @Mock private OrderRepository orderRepository;
    @Mock private UserRepository userRepository;
    @Mock private OrderStatusService orderStatusService;
    @Mock private AuditService auditService;
    @Mock private Order order;
    @Mock private UserEntity partner;
    @Mock private UserEntity customer;
    @Mock private Delivery delivery;
    @InjectMocks private DeliveryService deliveryService;

    private final UserDetails partnerPrincipal = User.withUsername("partner@example.com").password("ignored").roles("DELIVERY_PARTNER").build();
    private final UserDetails customerPrincipal = User.withUsername("customer@example.com").password("ignored").roles("CUSTOMER").build();

    @Test
    void createsDeliveryForValidOrderAndPartner() {
        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));
        when(userRepository.findById(20L)).thenReturn(Optional.of(partner));
        when(partner.getRole()).thenReturn(Role.DELIVERY_PARTNER);
        when(partner.isEnabled()).thenReturn(true);
        when(partner.getId()).thenReturn(20L);
        when(deliveryRepository.findByOrderId(10L)).thenReturn(Optional.empty());
        when(deliveryRepository.save(any(Delivery.class))).thenReturn(delivery);
        when(delivery.getOrder()).thenReturn(order);
        when(delivery.getDeliveryPartner()).thenReturn(partner);
        when(delivery.getStatus()).thenReturn(DeliveryStatus.ASSIGNED);
        when(delivery.getId()).thenReturn(30L);

        deliveryService.assign(new AssignDeliveryRequest(10L, 20L, "12 Main Street"), false);

        verify(deliveryRepository).save(any(Delivery.class));
        verify(auditService).record(20L, "DELIVERY_ASSIGNED", "DELIVERY", 30L, null, "orderId=10");
    }

    @Test
    void rejectsUnknownOrder() {
        when(orderRepository.findById(10L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> deliveryService.assign(new AssignDeliveryRequest(10L, 20L, "Address"), false)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void rejectsInvalidDeliveryPartner() {
        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));
        when(userRepository.findById(20L)).thenReturn(Optional.of(partner));
        when(partner.getRole()).thenReturn(Role.CUSTOMER);
        assertThatThrownBy(() -> deliveryService.assign(new AssignDeliveryRequest(10L, 20L, "Address"), false)).isInstanceOf(BadRequestException.class);
    }

    @Test
    void rejectsDuplicateDelivery() {
        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));
        when(userRepository.findById(20L)).thenReturn(Optional.of(partner));
        when(partner.getRole()).thenReturn(Role.DELIVERY_PARTNER);
        when(partner.isEnabled()).thenReturn(true);
        when(deliveryRepository.findByOrderId(10L)).thenReturn(Optional.of(delivery));
        assertThatThrownBy(() -> deliveryService.assign(new AssignDeliveryRequest(10L, 20L, "Address"), false)).isInstanceOf(ConflictException.class);
    }

    @Test
    void preventsCustomerAccessToAnotherCustomersDelivery() {
        when(deliveryRepository.findByOrderId(10L)).thenReturn(Optional.of(delivery));
        when(delivery.getOrder()).thenReturn(order);
        when(delivery.getDeliveryPartner()).thenReturn(partner);
        when(partner.getEmail()).thenReturn("partner@example.com");
        when(order.getUser()).thenReturn(customer);
        when(customer.getEmail()).thenReturn("other@example.com");
        assertThatThrownBy(() -> deliveryService.getByOrder(customerPrincipal, 10L)).isInstanceOf(ForbiddenException.class);
    }

    @Test
    void transitionsPickedUpAndUpdatesOrderLifecycle() {
        when(deliveryRepository.findById(5L)).thenReturn(Optional.of(delivery));
        when(delivery.getDeliveryPartner()).thenReturn(partner);
        when(partner.getEmail()).thenReturn("partner@example.com");
        when(delivery.getStatus()).thenReturn(DeliveryStatus.ASSIGNED);
        when(delivery.getOrder()).thenReturn(order);
        when(order.getStatus()).thenReturn(OrderStatus.READY_FOR_PICKUP);
        when(order.getId()).thenReturn(10L);
        when(partner.getId()).thenReturn(20L);
        when(partner.getName()).thenReturn("Delivery Partner");

        deliveryService.updateStatus(partnerPrincipal, 5L, DeliveryStatus.PICKED_UP);

        verify(orderStatusService).transition(order, OrderStatus.OUT_FOR_DELIVERY);
        verify(delivery).transition(DeliveryStatus.PICKED_UP);
    }

    @Test
    void rejectsInvalidTransition() {
        when(deliveryRepository.findById(5L)).thenReturn(Optional.of(delivery));
        when(delivery.getDeliveryPartner()).thenReturn(partner);
        when(partner.getEmail()).thenReturn("partner@example.com");
        when(delivery.getStatus()).thenReturn(DeliveryStatus.DELIVERED);
        assertThatThrownBy(() -> deliveryService.updateStatus(partnerPrincipal, 5L, DeliveryStatus.PICKED_UP)).isInstanceOf(BadRequestException.class);
    }

    @Test
    void deliveredUpdatesOrderWhenOutForDelivery() {
        when(deliveryRepository.findById(5L)).thenReturn(Optional.of(delivery));
        when(delivery.getDeliveryPartner()).thenReturn(partner);
        when(partner.getEmail()).thenReturn("partner@example.com");
        when(partner.getId()).thenReturn(20L);
        when(delivery.getStatus()).thenReturn(DeliveryStatus.OUT_FOR_DELIVERY);
        when(delivery.getOrder()).thenReturn(order);
        when(order.getStatus()).thenReturn(OrderStatus.OUT_FOR_DELIVERY);
        when(order.getId()).thenReturn(10L);
        when(partner.getName()).thenReturn("Delivery Partner");

        deliveryService.updateStatus(partnerPrincipal, 5L, DeliveryStatus.DELIVERED);

        verify(orderStatusService).transition(order, OrderStatus.DELIVERED);
        verify(delivery).transition(DeliveryStatus.DELIVERED);
    }
}
