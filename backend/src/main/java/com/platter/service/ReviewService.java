package com.platter.service;

import com.platter.dto.CreateReviewRequest;
import com.platter.dto.ReviewPageResponse;
import com.platter.dto.ReviewResponse;
import com.platter.entity.OrderStatus;
import com.platter.entity.Review;
import com.platter.entity.Role;
import com.platter.entity.UserEntity;
import com.platter.exception.BadRequestException;
import com.platter.exception.ConflictException;
import com.platter.exception.ForbiddenException;
import com.platter.exception.ResourceNotFoundException;
import com.platter.repository.OrderRepository;
import com.platter.repository.RestaurantRepository;
import com.platter.repository.ReviewRepository;
import com.platter.repository.UserRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final OrderRepository orderRepository;

    public ReviewService(ReviewRepository reviewRepository, UserRepository userRepository, RestaurantRepository restaurantRepository, OrderRepository orderRepository) { this.reviewRepository=reviewRepository; this.userRepository=userRepository; this.restaurantRepository=restaurantRepository; this.orderRepository=orderRepository; }

    @Transactional(readOnly = true)
    public ReviewPageResponse findByRestaurant(Long restaurantId, int page, int size, String sort) {
        if (!restaurantRepository.existsById(restaurantId)) throw new ResourceNotFoundException("Restaurant not found: " + restaurantId);
        Sort ordering = "rating".equalsIgnoreCase(sort) ? Sort.by(Sort.Direction.DESC, "rating") : Sort.by(Sort.Direction.DESC, "createdAt");
        Page<Review> reviews = reviewRepository.findByRestaurantId(restaurantId, PageRequest.of(page, size, ordering));
        return new ReviewPageResponse(reviews.map(review -> toResponse(review, null)).getContent(), reviews.getNumber(), reviews.getSize(), reviews.getTotalElements(), reviews.getTotalPages(), average(restaurantId), reviewRepository.countByRestaurantId(restaurantId));
    }

    public ReviewResponse create(UserDetails details, Long restaurantId, CreateReviewRequest request) {
        UserEntity user = findUser(details);
        if (user.getRole() != Role.CUSTOMER) throw new ForbiddenException("Only customers can create reviews");
        var restaurant = restaurantRepository.findById(restaurantId).orElseThrow(() -> new ResourceNotFoundException("Restaurant not found: " + restaurantId));
        if (!orderRepository.existsByUserEmailIgnoreCaseAndRestaurantIdAndStatus(user.getEmail(), restaurantId, OrderStatus.DELIVERED)) throw new ForbiddenException("A delivered order is required to review this restaurant");
        if (reviewRepository.existsByUserEmailIgnoreCaseAndRestaurantId(user.getEmail(), restaurantId)) throw new ConflictException("You have already reviewed this restaurant");
        Review saved = reviewRepository.save(new Review(user, restaurant, request.rating(), request.content().trim()));
        refreshRating(restaurantId);
        return toResponse(saved, user.getEmail());
    }

    public ReviewResponse update(UserDetails details, Long reviewId, CreateReviewRequest request) {
        UserEntity user = findUser(details); Review review = findReview(reviewId);
        assertOwnerOrAdmin(user, review);
        review.update(request.rating(), request.content().trim());
        reviewRepository.flush();
        refreshRating(review.getRestaurant().getId());
        return toResponse(review, user.getEmail());
    }

    public void delete(UserDetails details, Long reviewId) {
        UserEntity user = findUser(details); Review review = findReview(reviewId);
        assertOwnerOrAdmin(user, review);
        Long restaurantId = review.getRestaurant().getId();
        reviewRepository.delete(review);
        reviewRepository.flush();
        refreshRating(restaurantId);
    }

    private void refreshRating(Long restaurantId) {
        var restaurant = restaurantRepository.findById(restaurantId).orElseThrow(() -> new ResourceNotFoundException("Restaurant not found: " + restaurantId));
        long count = reviewRepository.countByRestaurantId(restaurantId);
        restaurant.updateRating(BigDecimal.valueOf(average(restaurantId)).setScale(1, RoundingMode.HALF_UP), (int) count);
        restaurantRepository.save(restaurant);
    }

    private double average(Long restaurantId) { return reviewRepository.averageRating(restaurantId); }
    private UserEntity findUser(UserDetails details) { return userRepository.findByEmailIgnoreCase(details.getUsername()).orElseThrow(() -> new ResourceNotFoundException("User not found")); }
    private Review findReview(Long id) { return reviewRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Review not found: " + id)); }
    private void assertOwnerOrAdmin(UserEntity user, Review review) { if (user.getRole() != Role.ADMIN && !review.getUser().getId().equals(user.getId())) throw new ForbiddenException("You can only manage your own review"); }
    private ReviewResponse toResponse(Review review, String email) { return new ReviewResponse(review.getId(), review.getUser().getId(), review.getUser().getName(), review.getRating(), review.getContent(), review.getCreatedAt(), review.getUpdatedAt(), email != null && review.getUser().getEmail().equalsIgnoreCase(email)); }
}
