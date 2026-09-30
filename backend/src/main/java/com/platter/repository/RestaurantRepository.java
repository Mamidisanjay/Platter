package com.platter.repository;

import com.platter.entity.Restaurant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    @Query("""
            select r from Restaurant r
            where r.available = true
              and (:search is null or lower(r.name) like lower(concat('%', :search, '%'))
                   or lower(r.cuisine) like lower(concat('%', :search, '%')))
              and (:cuisine is null or lower(r.cuisine) like lower(concat('%', :cuisine, '%')))
            """)
    Page<Restaurant> searchAvailable(@Param("search") String search, @Param("cuisine") String cuisine, Pageable pageable);
}
