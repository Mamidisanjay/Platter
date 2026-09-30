package com.platter.repository;

import com.platter.entity.MenuItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    List<MenuItem> findByRestaurantIdAndAvailableTrueOrderByCategoryNameAscNameAsc(Long restaurantId);
    List<MenuItem> findByRestaurantIdOrderByCategoryNameAscNameAsc(Long restaurantId);
}
