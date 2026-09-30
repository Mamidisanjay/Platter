package com.platter.controller;

import com.platter.dto.RestaurantResponse;
import com.platter.service.FavoriteService;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/favorites")
@PreAuthorize("hasRole('CUSTOMER')")
public class FavoriteController {
    private final FavoriteService favoriteService;
    public FavoriteController(FavoriteService favoriteService) { this.favoriteService=favoriteService; }
    @GetMapping public List<RestaurantResponse> findAll(@AuthenticationPrincipal UserDetails user) { return favoriteService.findAll(user); }
    @PostMapping("/{restaurantId}") public RestaurantResponse add(@AuthenticationPrincipal UserDetails user, @PathVariable Long restaurantId) { return favoriteService.add(user, restaurantId); }
    @DeleteMapping("/{restaurantId}") public void remove(@AuthenticationPrincipal UserDetails user, @PathVariable Long restaurantId) { favoriteService.remove(user, restaurantId); }
}
