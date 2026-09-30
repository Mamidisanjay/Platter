package com.platter.service;

import com.platter.dto.PageResponse;
import com.platter.dto.RestaurantResponse;
import com.platter.dto.RestaurantRequest;

public interface RestaurantService {
    PageResponse<RestaurantResponse> findRestaurants(String search, String cuisine, int page, int size);
    RestaurantResponse findRestaurant(Long id);
    RestaurantResponse createRestaurant(RestaurantRequest request);
    RestaurantResponse updateRestaurant(Long id, RestaurantRequest request);
    void deleteRestaurant(Long id);
}
