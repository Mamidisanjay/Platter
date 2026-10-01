package com.platter.repository;

import com.platter.entity.Delivery;
import com.platter.entity.DeliveryStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
    Optional<Delivery> findByOrderId(Long orderId);
    List<Delivery> findByDeliveryPartnerEmailIgnoreCaseOrderByCreatedAtDesc(String email);
    List<Delivery> findAllByOrderByCreatedAtDesc();
    boolean existsByOrderId(Long orderId);
    boolean existsByIdAndDeliveryPartnerEmailIgnoreCase(Long id, String email);
}
