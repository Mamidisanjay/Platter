package com.platter.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;

@Entity
@Table(name = "reviews", uniqueConstraints = @UniqueConstraint(name = "review_user_restaurant_unique", columnNames = {"user_id", "restaurant_id"}))
public class Review {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id", nullable = false) private UserEntity user;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "restaurant_id", nullable = false) private Restaurant restaurant;
    private int rating;
    private String content;
    private Instant createdAt;
    private Instant updatedAt;
    protected Review() { }
    public Review(UserEntity user, Restaurant restaurant, int rating, String content) { this.user=user; this.restaurant=restaurant; this.rating=rating; this.content=content; }
    @PrePersist void onCreate() { createdAt=Instant.now(); updatedAt=createdAt; }
    @PreUpdate void onUpdate() { updatedAt=Instant.now(); }
    public Long getId(){return id;} public UserEntity getUser(){return user;} public Restaurant getRestaurant(){return restaurant;} public int getRating(){return rating;} public String getContent(){return content;} public Instant getCreatedAt(){return createdAt;} public Instant getUpdatedAt(){return updatedAt;}
    public void update(int rating, String content) { this.rating=rating; this.content=content; }
}
