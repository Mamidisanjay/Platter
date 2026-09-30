package com.platter.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "menu_items")
public class MenuItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private String imageUrl;
    private boolean available;
    private int inventory;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    protected MenuItem() { }

    public MenuItem(String name, String description, BigDecimal price, String imageUrl, Restaurant restaurant, Category category) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;
        this.restaurant = restaurant;
        this.category = category;
        this.available = true;
        this.inventory = 100;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public BigDecimal getPrice() { return price; }
    public String getImageUrl() { return imageUrl; }
    public boolean isAvailable() { return available; }
    public int getInventory() { return inventory; }
    public Restaurant getRestaurant() { return restaurant; }
    public Category getCategory() { return category; }
    public void update(String name, String description, BigDecimal price, String imageUrl, Category category) { this.name = name; this.description = description; this.price = price; this.imageUrl = imageUrl; this.category = category; }
    public void setAvailable(boolean available) { this.available = available; }
    public void reserve(int quantity) {
        if (quantity > inventory) throw new IllegalStateException("Insufficient inventory for " + name);
        inventory -= quantity;
    }
}
