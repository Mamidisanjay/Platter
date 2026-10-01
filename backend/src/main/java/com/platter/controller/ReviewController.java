package com.platter.controller;

import com.platter.dto.CreateReviewRequest;
import com.platter.dto.ReviewPageResponse;
import com.platter.dto.ReviewResponse;
import com.platter.service.ReviewService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
public class ReviewController {
    private final ReviewService reviewService;
    public ReviewController(ReviewService reviewService) { this.reviewService=reviewService; }

    @GetMapping("/api/restaurants/{restaurantId}/reviews")
    public ReviewPageResponse findByRestaurant(@PathVariable Long restaurantId, @RequestParam(defaultValue="0") @Min(0) int page, @RequestParam(defaultValue="10") @Min(1) @Max(50) int size, @RequestParam(defaultValue="createdAt") String sort) { return reviewService.findByRestaurant(restaurantId, page, size, sort); }

    @PostMapping("/api/restaurants/{restaurantId}/reviews")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ReviewResponse> create(@AuthenticationPrincipal UserDetails user, @PathVariable Long restaurantId, @Valid @RequestBody CreateReviewRequest request) { return ResponseEntity.status(HttpStatus.CREATED).body(reviewService.create(user, restaurantId, request)); }

    @PutMapping("/api/reviews/{id}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ReviewResponse update(@AuthenticationPrincipal UserDetails user, @PathVariable Long id, @Valid @RequestBody CreateReviewRequest request) { return reviewService.update(user, id, request); }

    @DeleteMapping("/api/reviews/{id}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal UserDetails user, @PathVariable Long id) { reviewService.delete(user, id); return ResponseEntity.noContent().build(); }
}
