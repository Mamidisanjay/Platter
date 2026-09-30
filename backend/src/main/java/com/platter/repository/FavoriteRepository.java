package com.platter.repository;

import com.platter.entity.Favorite;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    List<Favorite> findByUserEmailIgnoreCaseOrderByCreatedAtDesc(String email);
    Optional<Favorite> findByUserEmailIgnoreCaseAndRestaurantId(String email, Long restaurantId);
}
