package com.platter.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "restaurants")
public class Restaurant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String cuisine;
    private BigDecimal rating;
    private int reviewCount;
    private Integer deliveryTimeMinutes;
    private String priceTier;
    private String imageUrl;
    private String tag;
    private String accent;
    private boolean available;

    protected Restaurant() { }

    public Restaurant(String name, String cuisine, BigDecimal rating, Integer deliveryTimeMinutes, String priceTier, String imageUrl, String tag, String accent) {
        this.name = name;
        this.cuisine = cuisine;
        this.rating = rating;
        this.deliveryTimeMinutes = deliveryTimeMinutes;
        this.priceTier = priceTier;
        this.imageUrl = imageUrl;
        this.tag = tag;
        this.accent = accent;
        this.available = true;
        this.reviewCount = 0;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getCuisine() { return cuisine; }
    public BigDecimal getRating() { return rating; }
    public int getReviewCount() { return reviewCount; }
    public Integer getDeliveryTimeMinutes() { return deliveryTimeMinutes; }
    public String getPriceTier() { return priceTier; }
    public String getImageUrl() { return imageUrl; }
    public String getTag() { return tag; }
    public String getAccent() { return accent; }
    public boolean isAvailable() { return available; }
    public void updateRating(BigDecimal rating, int reviewCount) { this.rating = rating; this.reviewCount = reviewCount; }
    public void update(String name, String cuisine, BigDecimal rating, Integer deliveryTimeMinutes, String priceTier, String imageUrl, String tag, String accent) { this.name = name; this.cuisine = cuisine; this.rating = rating; this.deliveryTimeMinutes = deliveryTimeMinutes; this.priceTier = priceTier; this.imageUrl = imageUrl; this.tag = tag; this.accent = accent; }
}
