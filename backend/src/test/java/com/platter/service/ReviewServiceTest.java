package com.platter.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.platter.dto.CreateReviewRequest;
import com.platter.entity.OrderStatus;
import com.platter.entity.Restaurant;
import com.platter.entity.Review;
import com.platter.entity.Role;
import com.platter.entity.UserEntity;
import com.platter.exception.ConflictException;
import com.platter.exception.ForbiddenException;
import com.platter.repository.OrderRepository;
import com.platter.repository.RestaurantRepository;
import com.platter.repository.ReviewRepository;
import com.platter.repository.UserRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {
    @Mock private ReviewRepository reviewRepository;
    @Mock private UserRepository userRepository;
    @Mock private RestaurantRepository restaurantRepository;
    @Mock private OrderRepository orderRepository;
    @Mock private Restaurant restaurant;
    @Mock private Review review;
    @InjectMocks private ReviewService reviewService;

    private final UserDetails customerPrincipal = User.withUsername("customer@example.com").password("ignored").roles("CUSTOMER").build();

    @Test
    void createsReviewOnlyAfterDeliveredOrderAndRefreshesRating() {
        UserEntity user = org.mockito.Mockito.mock(UserEntity.class);
        when(user.getEmail()).thenReturn("customer@example.com");
        when(user.getRole()).thenReturn(Role.CUSTOMER);
        when(restaurantRepository.findById(7L)).thenReturn(Optional.of(restaurant));
        when(reviewRepository.existsByUserEmailIgnoreCaseAndRestaurantId("customer@example.com", 7L)).thenReturn(false);
        when(orderRepository.existsByUserEmailIgnoreCaseAndRestaurantIdAndStatus("customer@example.com", 7L, OrderStatus.DELIVERED)).thenReturn(true);
        when(reviewRepository.save(any(Review.class))).thenReturn(review);
        when(reviewRepository.averageRating(7L)).thenReturn(4.5);
        when(reviewRepository.countByRestaurantId(7L)).thenReturn(2L);
        when(restaurantRepository.findById(7L)).thenReturn(Optional.of(restaurant));
        when(review.getUser()).thenReturn(user);
        when(review.getRating()).thenReturn(5);
        when(review.getContent()).thenReturn("Excellent food");
        when(userRepository.findByEmailIgnoreCase("customer@example.com")).thenReturn(Optional.of(user));

        reviewService.create(customerPrincipal, 7L, new CreateReviewRequest(5, "Excellent food"));

        verify(reviewRepository).save(any(Review.class));
        verify(restaurant).updateRating(new BigDecimal("4.5"), 2);
    }

    @Test
    void rejectsDuplicateReview() {
        UserEntity user = org.mockito.Mockito.mock(UserEntity.class);
        when(user.getEmail()).thenReturn("customer@example.com");
        when(user.getRole()).thenReturn(Role.CUSTOMER);
        when(userRepository.findByEmailIgnoreCase("customer@example.com")).thenReturn(Optional.of(user));
        when(restaurantRepository.findById(7L)).thenReturn(Optional.of(restaurant));
        when(orderRepository.existsByUserEmailIgnoreCaseAndRestaurantIdAndStatus("customer@example.com", 7L, OrderStatus.DELIVERED)).thenReturn(true);
        when(reviewRepository.existsByUserEmailIgnoreCaseAndRestaurantId("customer@example.com", 7L)).thenReturn(true);

        assertThatThrownBy(() -> reviewService.create(customerPrincipal, 7L, new CreateReviewRequest(4, "Again"))).isInstanceOf(ConflictException.class);
    }

    @Test
    void rejectsCustomerWithoutDeliveredOrder() {
        UserEntity user = org.mockito.Mockito.mock(UserEntity.class);
        when(user.getEmail()).thenReturn("customer@example.com");
        when(user.getRole()).thenReturn(Role.CUSTOMER);
        when(userRepository.findByEmailIgnoreCase("customer@example.com")).thenReturn(Optional.of(user));
        when(restaurantRepository.findById(7L)).thenReturn(Optional.of(restaurant));
        when(orderRepository.existsByUserEmailIgnoreCaseAndRestaurantIdAndStatus("customer@example.com", 7L, OrderStatus.DELIVERED)).thenReturn(false);

        assertThatThrownBy(() -> reviewService.create(customerPrincipal, 7L, new CreateReviewRequest(4, "No order"))).isInstanceOf(ForbiddenException.class);
    }

    @Test
    void rejectsUpdatingAnotherUsersReview() {
        UserEntity owner = org.mockito.Mockito.mock(UserEntity.class);
        UserEntity anotherUser = org.mockito.Mockito.mock(UserEntity.class);
        when(owner.getId()).thenReturn(10L);
        when(anotherUser.getId()).thenReturn(20L);
        when(review.getUser()).thenReturn(owner);
        when(userRepository.findByEmailIgnoreCase("customer@example.com")).thenReturn(Optional.of(anotherUser));
        when(reviewRepository.findById(3L)).thenReturn(Optional.of(review));

        assertThatThrownBy(() -> reviewService.update(customerPrincipal, 3L, new CreateReviewRequest(2, "Nope"))).isInstanceOf(ForbiddenException.class);
    }

    @Test
    void allowsOwnerToDeleteReviewAndRefreshesRating() {
        UserEntity owner = org.mockito.Mockito.mock(UserEntity.class);
        when(owner.getId()).thenReturn(20L);
        when(review.getUser()).thenReturn(owner);
        when(review.getRestaurant()).thenReturn(restaurant);
        when(restaurant.getId()).thenReturn(7L);
        when(userRepository.findByEmailIgnoreCase("customer@example.com")).thenReturn(Optional.of(owner));
        when(reviewRepository.findById(3L)).thenReturn(Optional.of(review));
        when(reviewRepository.averageRating(7L)).thenReturn(0.0);
        when(reviewRepository.countByRestaurantId(7L)).thenReturn(0L);
        when(restaurantRepository.findById(7L)).thenReturn(Optional.of(restaurant));

        reviewService.delete(customerPrincipal, 3L);

        verify(reviewRepository).delete(review);
        verify(restaurant).updateRating(new BigDecimal("0.0"), 0);
    }
}
