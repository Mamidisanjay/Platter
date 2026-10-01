package com.platter.service;

import com.platter.dto.RestaurantResponse;
import com.platter.entity.Favorite;
import com.platter.entity.UserEntity;
import com.platter.exception.ConflictException;
import com.platter.exception.ResourceNotFoundException;
import com.platter.repository.FavoriteRepository;
import com.platter.repository.RestaurantRepository;
import com.platter.repository.UserRepository;
import java.util.List;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class FavoriteService {
    private final FavoriteRepository favoriteRepository; private final UserRepository userRepository; private final RestaurantRepository restaurantRepository;
    public FavoriteService(FavoriteRepository favoriteRepository, UserRepository userRepository, RestaurantRepository restaurantRepository) { this.favoriteRepository=favoriteRepository; this.userRepository=userRepository; this.restaurantRepository=restaurantRepository; }
    @Transactional(readOnly=true) public List<RestaurantResponse> findAll(UserDetails user) { return favoriteRepository.findByUserEmailIgnoreCaseOrderByCreatedAtDesc(user.getUsername()).stream().map(f -> toResponse(f.getRestaurant())).toList(); }
    public RestaurantResponse add(UserDetails details, Long restaurantId) { UserEntity user=findUser(details); var restaurant=restaurantRepository.findById(restaurantId).orElseThrow(() -> new ResourceNotFoundException("Restaurant not found: " + restaurantId)); if (favoriteRepository.findByUserEmailIgnoreCaseAndRestaurantId(user.getEmail(), restaurantId).isPresent()) throw new ConflictException("Restaurant is already saved"); favoriteRepository.save(new Favorite(user, restaurant)); return toResponse(restaurant); }
    public void remove(UserDetails details, Long restaurantId) { favoriteRepository.findByUserEmailIgnoreCaseAndRestaurantId(details.getUsername(), restaurantId).ifPresentOrElse(favoriteRepository::delete, () -> { throw new ResourceNotFoundException("Favorite not found"); }); }
    private UserEntity findUser(UserDetails details) { return userRepository.findByEmailIgnoreCase(details.getUsername()).orElseThrow(() -> new ResourceNotFoundException("User not found")); }
    private RestaurantResponse toResponse(com.platter.entity.Restaurant r) { return new RestaurantResponse(r.getId(), r.getName(), r.getCuisine(), r.getRating(), r.getDeliveryTimeMinutes(), r.getPriceTier(), r.getImageUrl(), r.getTag(), r.getAccent(), r.isAvailable(), r.getReviewCount()); }
}
