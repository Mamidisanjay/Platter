package com.platter.repository;

import com.platter.entity.Order;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserEmailIgnoreCaseOrderByCreatedAtDesc(String email);
    Optional<Order> findByIdAndUserEmailIgnoreCase(Long id, String email);
}
