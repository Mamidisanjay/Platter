package com.platter.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "cart_items")
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private int quantity;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "menu_item_id", nullable = false)
    private MenuItem menuItem;

    protected CartItem() { }
    public CartItem(Cart cart, MenuItem menuItem, int quantity) { this.cart = cart; this.menuItem = menuItem; this.quantity = quantity; }
    public Long getId() { return id; }
    public int getQuantity() { return quantity; }
    public MenuItem getMenuItem() { return menuItem; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
