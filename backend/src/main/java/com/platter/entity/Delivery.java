package com.platter.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;

@Entity
@Table(name = "deliveries", uniqueConstraints = @UniqueConstraint(name = "delivery_order_unique", columnNames = "order_id"))
public class Delivery {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @OneToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "order_id", nullable = false) private Order order;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "delivery_partner_id", nullable = false) private UserEntity deliveryPartner;
    @Enumerated(EnumType.STRING) private DeliveryStatus status;
    private Instant pickupTime;
    private Instant pickedUpAt;
    private Instant deliveredAt;
    private String deliveryAddress;
    private Double latitude;
    private Double longitude;
    private Instant createdAt;
    private Instant updatedAt;
    protected Delivery() { }
    public Delivery(Order order, UserEntity deliveryPartner, String deliveryAddress) { this.order=order; this.deliveryPartner=deliveryPartner; this.deliveryAddress=deliveryAddress; this.status=DeliveryStatus.ASSIGNED; }
    @PrePersist void onCreate() { createdAt=Instant.now(); updatedAt=createdAt; pickupTime=createdAt; }
    @PreUpdate void onUpdate() { updatedAt=Instant.now(); }
    public Long getId(){return id;} public Order getOrder(){return order;} public UserEntity getDeliveryPartner(){return deliveryPartner;} public DeliveryStatus getStatus(){return status;} public Instant getPickupTime(){return pickupTime;} public Instant getPickedUpAt(){return pickedUpAt;} public Instant getDeliveredAt(){return deliveredAt;} public String getDeliveryAddress(){return deliveryAddress;} public Double getLatitude(){return latitude;} public Double getLongitude(){return longitude;} public Instant getCreatedAt(){return createdAt;} public Instant getUpdatedAt(){return updatedAt;}
    public void assignPartner(UserEntity partner) { deliveryPartner=partner; }
    public void updateLocation(Double latitude, Double longitude) { this.latitude=latitude; this.longitude=longitude; }
    public void transition(DeliveryStatus status) { this.status=status; if (status == DeliveryStatus.PICKED_UP) pickedUpAt=Instant.now(); if (status == DeliveryStatus.DELIVERED) deliveredAt=Instant.now(); }
}
