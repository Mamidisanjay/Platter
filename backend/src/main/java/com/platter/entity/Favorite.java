package com.platter.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;

@Entity
@Table(name = "favorites", uniqueConstraints = @UniqueConstraint(name = "favorite_user_restaurant_unique", columnNames = {"user_id", "restaurant_id"}))
public class Favorite {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id", nullable = false) private UserEntity user;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "restaurant_id", nullable = false) private com.platter.entity.Restaurant restaurant;
    private Instant createdAt;
    protected Favorite() { }
    public Favorite(UserEntity user, com.platter.entity.Restaurant restaurant) { this.user=user; this.restaurant=restaurant; this.createdAt=Instant.now(); }
    public Long getId(){return id;} public com.platter.entity.Restaurant getRestaurant(){return restaurant;}
}
