package com.platter.controller;

import com.platter.dto.PageResponse;
import com.platter.dto.RestaurantResponse;
import com.platter.dto.RestaurantRequest;
import com.platter.service.RestaurantService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/restaurants")
@Validated
public class RestaurantController {
    private final RestaurantService restaurantService;

    public RestaurantController(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }

    @GetMapping
    @PreAuthorize("permitAll()")
    public PageResponse<RestaurantResponse> findRestaurants(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String cuisine,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return restaurantService.findRestaurants(search, cuisine, page, size);
    }

    @GetMapping("/{id}")
    @PreAuthorize("permitAll()")
    public RestaurantResponse findRestaurant(@PathVariable Long id) {
        return restaurantService.findRestaurant(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RESTAURANT_OWNER')")
    public ResponseEntity<RestaurantResponse> create(@jakarta.validation.Valid @RequestBody RestaurantRequest request) { return ResponseEntity.status(HttpStatus.CREATED).body(restaurantService.createRestaurant(request)); }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESTAURANT_OWNER')")
    public RestaurantResponse update(@PathVariable Long id, @jakarta.validation.Valid @RequestBody RestaurantRequest request) { return restaurantService.updateRestaurant(id, request); }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) { restaurantService.deleteRestaurant(id); return ResponseEntity.noContent().build(); }
}
