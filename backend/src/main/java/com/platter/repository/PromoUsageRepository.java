package com.platter.repository;

import com.platter.entity.PromoUsage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PromoUsageRepository extends JpaRepository<PromoUsage, Long> {
    long countByPromoIdAndUserId(Long promoId, Long userId);
}
