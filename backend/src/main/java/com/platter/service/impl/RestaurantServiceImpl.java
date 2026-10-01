package com.platter.service.impl;

import com.platter.dto.PageResponse;
import com.platter.dto.RestaurantResponse;
import com.platter.dto.RestaurantRequest;
import com.platter.entity.Restaurant;
import com.platter.exception.ResourceNotFoundException;
import com.platter.repository.RestaurantRepository;
import com.platter.service.RestaurantService;
import com.platter.search.RestaurantSearchIndexer;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class RestaurantServiceImpl implements RestaurantService {
    private final RestaurantRepository restaurantRepository;
    private final RestaurantSearchIndexer searchIndexer;

    public RestaurantServiceImpl(RestaurantRepository restaurantRepository, RestaurantSearchIndexer searchIndexer) {
        this.restaurantRepository = restaurantRepository;
        this.searchIndexer = searchIndexer;
    }

    @Override
    @Cacheable(cacheNames = "restaurant-search", key = "#search + ':' + #cuisine + ':' + #page + ':' + #size")
    public PageResponse<RestaurantResponse> findRestaurants(String search, String cuisine, int page, int size) {
        Page<Restaurant> restaurants = restaurantRepository.searchAvailable(normalize(search), normalize(cuisine), PageRequest.of(page, size, Sort.by("rating").descending()));
        return new PageResponse<>(restaurants.map(this::toResponse).getContent(), restaurants.getNumber(), restaurants.getSize(), restaurants.getTotalElements(), restaurants.getTotalPages());
    }

    @Override
    @Cacheable(cacheNames = "restaurant", key = "#id")
    public RestaurantResponse findRestaurant(Long id) {
        return restaurantRepository.findById(id).map(this::toResponse).orElseThrow(() -> new ResourceNotFoundException("Restaurant not found: " + id));
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = {"restaurant-search", "restaurant"}, allEntries = true)
    public RestaurantResponse createRestaurant(RestaurantRequest request) {
        Restaurant saved = restaurantRepository.save(new Restaurant(request.name().trim(), request.cuisine().trim(), request.rating(), request.deliveryTimeMinutes(), request.priceTier(), request.imageUrl(), request.tag(), request.accent()));
        searchIndexer.reindexAfterCommit(saved.getId());
        return toResponse(saved);
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = {"restaurant-search", "restaurant"}, allEntries = true)
    public RestaurantResponse updateRestaurant(Long id, RestaurantRequest request) {
        Restaurant restaurant = restaurantRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Restaurant not found: " + id));
        restaurant.update(request.name().trim(), request.cuisine().trim(), request.rating(), request.deliveryTimeMinutes(), request.priceTier(), request.imageUrl(), request.tag(), request.accent());
        searchIndexer.reindexAfterCommit(id);
        return toResponse(restaurant);
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = {"restaurant-search", "restaurant"}, allEntries = true)
    public void deleteRestaurant(Long id) {
        if (!restaurantRepository.existsById(id)) throw new ResourceNotFoundException("Restaurant not found: " + id);
        restaurantRepository.deleteById(id);
        searchIndexer.deleteAfterCommit(id);
    }

    private RestaurantResponse toResponse(Restaurant restaurant) {
        return new RestaurantResponse(restaurant.getId(), restaurant.getName(), restaurant.getCuisine(), restaurant.getRating(), restaurant.getDeliveryTimeMinutes(), restaurant.getPriceTier(), restaurant.getImageUrl(), restaurant.getTag(), restaurant.getAccent(), restaurant.isAvailable(), restaurant.getReviewCount());
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
