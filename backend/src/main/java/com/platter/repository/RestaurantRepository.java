package com.platter.repository;

import com.platter.entity.Restaurant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    @Query(value = """
            select * from restaurants r
            where r.available = true
              and (cast(:search as text) is null or lower(r.name) like lower('%' || cast(:search as text) || '%')
                   or lower(r.cuisine) like lower('%' || cast(:search as text) || '%'))
              and (cast(:cuisine as text) is null or lower(r.cuisine) like lower('%' || cast(:cuisine as text) || '%'))
            """, countQuery = """
            select count(*) from restaurants r
            where r.available = true
              and (cast(:search as text) is null or lower(r.name) like lower('%' || cast(:search as text) || '%')
                   or lower(r.cuisine) like lower('%' || cast(:search as text) || '%'))
              and (cast(:cuisine as text) is null or lower(r.cuisine) like lower('%' || cast(:cuisine as text) || '%'))
            """, nativeQuery = true)
    Page<Restaurant> searchAvailable(@Param("search") String search, @Param("cuisine") String cuisine, Pageable pageable);
}
