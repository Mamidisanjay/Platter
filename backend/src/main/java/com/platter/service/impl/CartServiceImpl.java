package com.platter.service.impl;

import com.platter.dto.CartItemRequest;
import com.platter.dto.CartItemResponse;
import com.platter.dto.CartResponse;
import com.platter.entity.Cart;
import com.platter.entity.CartItem;
import com.platter.entity.MenuItem;
import com.platter.entity.UserEntity;
import com.platter.exception.ConflictException;
import com.platter.exception.ResourceNotFoundException;
import com.platter.repository.CartRepository;
import com.platter.repository.MenuItemRepository;
import com.platter.repository.UserRepository;
import com.platter.service.CartService;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CartServiceImpl implements CartService {
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final MenuItemRepository menuItemRepository;

    public CartServiceImpl(CartRepository cartRepository, UserRepository userRepository, MenuItemRepository menuItemRepository) {
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
        this.menuItemRepository = menuItemRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public CartResponse getCart(UserDetails user) {
        return toResponse(findOrEmpty(user));
    }

    @Override
    public CartResponse addItem(UserDetails user, CartItemRequest request) {
        Cart cart = findOrCreate(user);
        MenuItem menuItem = menuItemRepository.findById(request.menuItemId()).orElseThrow(() -> new ResourceNotFoundException("Menu item not found: " + request.menuItemId()));
        if (!menuItem.isAvailable()) throw new ConflictException("Menu item is currently unavailable");
        if (!cart.getItems().isEmpty() && cart.getItems().stream().anyMatch(item -> !item.getMenuItem().getRestaurant().getId().equals(menuItem.getRestaurant().getId()))) {
            throw new ConflictException("Cart items must come from one restaurant");
        }
        CartItem existing = cart.getItems().stream().filter(item -> item.getMenuItem().getId().equals(menuItem.getId())).findFirst().orElse(null);
        if (existing == null) cart.addItem(new CartItem(cart, menuItem, request.quantity()));
        else existing.setQuantity(existing.getQuantity() + request.quantity());
        return toResponse(cartRepository.save(cart));
    }

    @Override
    public CartResponse updateItem(UserDetails user, Long itemId, int quantity) {
        Cart cart = findOrCreate(user);
        CartItem item = cart.getItems().stream().filter(candidate -> candidate.getId().equals(itemId)).findFirst().orElseThrow(() -> new ResourceNotFoundException("Cart item not found: " + itemId));
        item.setQuantity(quantity);
        return toResponse(cartRepository.save(cart));
    }

    @Override
    public void removeItem(UserDetails user, Long itemId) {
        Cart cart = findOrCreate(user);
        CartItem item = cart.getItems().stream().filter(candidate -> candidate.getId().equals(itemId)).findFirst().orElseThrow(() -> new ResourceNotFoundException("Cart item not found: " + itemId));
        cart.removeItem(item);
        cartRepository.save(cart);
    }

    @Override
    public void clear(UserDetails user) {
        Cart cart = findOrCreate(user);
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    private Cart findOrCreate(UserDetails details) {
        UserEntity user = userRepository.findByEmailIgnoreCase(details.getUsername()).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return cartRepository.findByUserEmailIgnoreCase(user.getEmail()).orElseGet(() -> cartRepository.save(new Cart(user)));
    }

    private Cart findOrEmpty(UserDetails details) { return findOrCreate(details); }

    private CartResponse toResponse(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream().map(item -> {
            BigDecimal lineTotal = item.getMenuItem().getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            return new CartItemResponse(item.getId(), item.getMenuItem().getId(), item.getMenuItem().getName(), item.getMenuItem().getRestaurant().getName(), item.getMenuItem().getPrice(), item.getQuantity(), lineTotal);
        }).toList();
        BigDecimal subtotal = items.stream().map(CartItemResponse::lineTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new CartResponse(items, subtotal);
    }
}
