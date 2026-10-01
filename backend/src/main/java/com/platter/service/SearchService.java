package com.platter.service;

import com.platter.dto.PageResponse;
import com.platter.dto.RestaurantResponse;
import com.platter.search.RestaurantSearchDocument;
import com.platter.search.RestaurantSearchRepository;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class SearchService {
    private final RestaurantSearchRepository searchRepository;
    public SearchService(RestaurantSearchRepository searchRepository) { this.searchRepository = searchRepository; }
    public PageResponse<RestaurantResponse> search(String query, int page, int size) {
        var result = searchRepository.findByNameContainingOrCuisineContaining(query, query, PageRequest.of(page, size, Sort.by("rating").descending()));
        return new PageResponse<>(result.map(this::toResponse).getContent(), result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }
    private RestaurantResponse toResponse(RestaurantSearchDocument item) { return new RestaurantResponse(item.getId(), item.getName(), item.getCuisine(), item.getRating(), item.getDeliveryTimeMinutes(), item.getPriceTier(), null, item.getTag(), null, item.isAvailable(), 0); }
}
