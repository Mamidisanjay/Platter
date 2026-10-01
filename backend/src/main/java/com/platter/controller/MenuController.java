package com.platter.controller;

import com.platter.dto.MenuItemRequest;
import com.platter.dto.MenuItemResponse;
import com.platter.service.MenuService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
public class MenuController {
    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @GetMapping("/api/restaurants/{restaurantId}/menu")
    public List<MenuItemResponse> findAvailableMenu(@PathVariable Long restaurantId) {
        return menuService.findAvailableMenu(restaurantId);
    }

    @GetMapping("/api/menu/{id}")
    public MenuItemResponse findById(@PathVariable Long id) {
        return menuService.findById(id);
    }

    @PostMapping("/api/restaurants/{restaurantId}/menu")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESTAURANT_OWNER')")
    public ResponseEntity<MenuItemResponse> create(@PathVariable Long restaurantId, @Valid @RequestBody MenuItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(menuService.create(restaurantId, request));
    }

    @PutMapping("/api/menu/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESTAURANT_OWNER')")
    public MenuItemResponse update(@PathVariable Long id, @Valid @RequestBody MenuItemRequest request) {
        return menuService.update(id, request);
    }

    @DeleteMapping("/api/menu/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESTAURANT_OWNER')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        menuService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/api/menu/{id}/availability")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESTAURANT_OWNER')")
    public MenuItemResponse updateAvailability(@PathVariable Long id, @RequestParam boolean available) {
        return menuService.updateAvailability(id, available);
    }
}
