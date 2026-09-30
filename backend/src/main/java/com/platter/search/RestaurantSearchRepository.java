package com.platter.search;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface RestaurantSearchRepository extends ElasticsearchRepository<RestaurantSearchDocument, Long> {
    Page<RestaurantSearchDocument> findByNameContainingOrCuisineContaining(String name, String cuisine, Pageable pageable);
}
