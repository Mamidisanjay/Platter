package com.platter.service;

import com.platter.audit.AuditService;
import com.platter.dto.AssignDeliveryRequest;
import com.platter.dto.DeliveryLocationRequest;
import com.platter.dto.DeliveryResponse;
import com.platter.entity.Delivery;
import com.platter.entity.DeliveryStatus;
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
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DeliveryService {
    private static final Map<DeliveryStatus, Set<DeliveryStatus>> TRANSITIONS = Map.of(
            DeliveryStatus.ASSIGNED, EnumSet.of(DeliveryStatus.PICKED_UP, DeliveryStatus.CANCELLED),
            DeliveryStatus.PICKED_UP, EnumSet.of(DeliveryStatus.OUT_FOR_DELIVERY, DeliveryStatus.CANCELLED),
            DeliveryStatus.OUT_FOR_DELIVERY, EnumSet.of(DeliveryStatus.DELIVERED, DeliveryStatus.CANCELLED),
            DeliveryStatus.DELIVERED, EnumSet.noneOf(DeliveryStatus.class),
            DeliveryStatus.CANCELLED, EnumSet.noneOf(DeliveryStatus.class));

    private final DeliveryRepository deliveryRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final OrderStatusService orderStatusService;
    private final AuditService auditService;

    public DeliveryService(DeliveryRepository deliveryRepository, OrderRepository orderRepository, UserRepository userRepository, OrderStatusService orderStatusService, AuditService auditService) { this.deliveryRepository=deliveryRepository; this.orderRepository=orderRepository; this.userRepository=userRepository; this.orderStatusService=orderStatusService; this.auditService=auditService; }

    @Transactional(readOnly = true)
    public DeliveryResponse getForCustomer(UserDetails details, Long orderId) {
        Delivery delivery = findByOrder(orderId);
        if (!delivery.getOrder().getUser().getEmail().equalsIgnoreCase(details.getUsername())) throw new ForbiddenException("You cannot access this delivery");
        return toResponse(delivery);
    }

    @Transactional(readOnly = true)
    public DeliveryResponse getByOrder(UserDetails details, Long orderId) {
        Delivery delivery = findByOrder(orderId);
        boolean admin = details.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean partner = delivery.getDeliveryPartner().getEmail().equalsIgnoreCase(details.getUsername());
        boolean customer = delivery.getOrder().getUser().getEmail().equalsIgnoreCase(details.getUsername());
        if (!admin && !partner && !customer) throw new ForbiddenException("You cannot access this delivery");
        return toResponse(delivery);
    }

    @Transactional(readOnly = true)
    public List<DeliveryResponse> findForPartner(UserDetails details) { return deliveryRepository.findByDeliveryPartnerEmailIgnoreCaseOrderByCreatedAtDesc(details.getUsername()).stream().map(this::toResponse).toList(); }
    @Transactional(readOnly = true)
    public List<DeliveryResponse> findAll() { return deliveryRepository.findAllByOrderByCreatedAtDesc().stream().map(this::toResponse).toList(); }
    @Transactional(readOnly = true)
    public DeliveryResponse findForPartner(UserDetails details, Long id) { Delivery delivery=findById(id); assertPartnerOrAdmin(details, delivery); return toResponse(delivery); }

    public DeliveryResponse assign(AssignDeliveryRequest request, boolean allowReassign) {
        var order = orderRepository.findById(request.orderId()).orElseThrow(() -> new ResourceNotFoundException("Order not found: " + request.orderId()));
        UserEntity partner = findPartner(request.deliveryPartnerId());
        Delivery delivery = deliveryRepository.findByOrderId(request.orderId()).orElse(null);
        if (delivery != null && !allowReassign) throw new ConflictException("A delivery already exists for this order");
        if (delivery == null) delivery = deliveryRepository.save(new Delivery(order, partner, request.deliveryAddress()));
        else { if (delivery.getStatus() == DeliveryStatus.DELIVERED || delivery.getStatus() == DeliveryStatus.CANCELLED) throw new BadRequestException("A completed delivery cannot be reassigned"); delivery.assignPartner(partner); }
        auditService.record(partner.getId(), allowReassign ? "DELIVERY_REASSIGNED" : "DELIVERY_ASSIGNED", "DELIVERY", delivery.getId(), null, "orderId=" + request.orderId());
        return toResponse(delivery);
    }

    public DeliveryResponse accept(UserDetails details, Long id) { Delivery delivery=findById(id); assertPartner(details, delivery); if (delivery.getStatus() != DeliveryStatus.ASSIGNED) throw new BadRequestException("Only assigned deliveries can be accepted"); auditService.record(delivery.getDeliveryPartner().getId(), "DELIVERY_ACCEPTED", "DELIVERY", id, null, "status=ASSIGNED"); return toResponse(delivery); }

    public DeliveryResponse updateStatus(UserDetails details, Long id, DeliveryStatus target) {
        Delivery delivery=findById(id); assertPartnerOrAdmin(details, delivery);
        if (!TRANSITIONS.getOrDefault(delivery.getStatus(), Set.of()).contains(target)) throw new BadRequestException("Invalid delivery transition from " + delivery.getStatus() + " to " + target);
        if (target == DeliveryStatus.PICKED_UP && delivery.getOrder().getStatus() != OrderStatus.READY_FOR_PICKUP && delivery.getOrder().getStatus() != OrderStatus.OUT_FOR_DELIVERY) throw new BadRequestException("Order is not ready for pickup");
        if (target == DeliveryStatus.OUT_FOR_DELIVERY && delivery.getOrder().getStatus() != OrderStatus.OUT_FOR_DELIVERY) throw new BadRequestException("Order is not out for delivery");
        if (target == DeliveryStatus.DELIVERED && delivery.getOrder().getStatus() != OrderStatus.OUT_FOR_DELIVERY && delivery.getOrder().getStatus() != OrderStatus.DELIVERED) throw new BadRequestException("Order is not out for delivery");
        if (target == DeliveryStatus.PICKED_UP && delivery.getOrder().getStatus() == OrderStatus.READY_FOR_PICKUP) orderStatusService.transition(delivery.getOrder(), OrderStatus.OUT_FOR_DELIVERY);
        if (target == DeliveryStatus.DELIVERED && delivery.getOrder().getStatus() == OrderStatus.OUT_FOR_DELIVERY) orderStatusService.transition(delivery.getOrder(), OrderStatus.DELIVERED);
        delivery.transition(target);
        auditService.record(delivery.getDeliveryPartner().getId(), "DELIVERY_STATUS_CHANGED", "DELIVERY", id, null, "status=" + target);
        return toResponse(delivery);
    }

    public DeliveryResponse updateLocation(UserDetails details, Long id, DeliveryLocationRequest request) { Delivery delivery=findById(id); assertPartnerOrAdmin(details, delivery); delivery.updateLocation(request.latitude(), request.longitude()); auditService.record(delivery.getDeliveryPartner().getId(), "DELIVERY_LOCATION_UPDATED", "DELIVERY", id, null, "location_updated=true"); return toResponse(delivery); }

    private Delivery findByOrder(Long orderId) { return deliveryRepository.findByOrderId(orderId).orElseThrow(() -> new ResourceNotFoundException("Delivery not found for order: " + orderId)); }
    private Delivery findById(Long id) { return deliveryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Delivery not found: " + id)); }
    private UserEntity findPartner(Long id) { UserEntity user=userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Delivery partner not found: " + id)); if (user.getRole() != Role.DELIVERY_PARTNER || !user.isEnabled()) throw new BadRequestException("User is not an active delivery partner"); return user; }
    private void assertPartner(UserDetails details, Delivery delivery) { if (!delivery.getDeliveryPartner().getEmail().equalsIgnoreCase(details.getUsername())) throw new ForbiddenException("Delivery is assigned to another partner"); }
    private void assertPartnerOrAdmin(UserDetails details, Delivery delivery) { if (!details.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) assertPartner(details, delivery); }
    private DeliveryResponse toResponse(Delivery d) { return new DeliveryResponse(d.getId(), d.getOrder().getId(), d.getDeliveryPartner().getId(), d.getDeliveryPartner().getName(), d.getStatus(), d.getPickupTime(), d.getPickedUpAt(), d.getDeliveredAt(), d.getDeliveryAddress(), d.getLatitude(), d.getLongitude(), d.getCreatedAt(), d.getUpdatedAt()); }
}
