package com.platter.repository;

import com.platter.entity.PromoCode;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PromoCodeRepository extends JpaRepository<PromoCode, Long> {
    Optional<PromoCode> findByCodeIgnoreCase(String code);
    List<PromoCode> findByActiveTrueAndExpiryTimeBefore(Instant now);
}
