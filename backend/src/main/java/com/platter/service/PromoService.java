package com.platter.service;

import com.platter.dto.PromoRequest;
import com.platter.dto.PromoResponse;
import com.platter.dto.PromoValidationRequest;
import com.platter.dto.PromoValidationResponse;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import com.platter.entity.DiscountType;
import com.platter.entity.PromoCode;
import com.platter.exception.BadRequestException;
import com.platter.exception.ResourceNotFoundException;
import com.platter.repository.PromoCodeRepository;
import com.platter.repository.PromoUsageRepository;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;
import com.platter.audit.AuditService;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PromoService {
    private final PromoCodeRepository promoCodeRepository;
    private final PromoUsageRepository promoUsageRepository;
    private final AuditService auditService;

    public PromoService(PromoCodeRepository promoCodeRepository, PromoUsageRepository promoUsageRepository, AuditService auditService) { this.promoCodeRepository = promoCodeRepository; this.promoUsageRepository = promoUsageRepository; this.auditService = auditService; }

    @Transactional(readOnly = true)
    public PromoValidationResponse validate(PromoValidationRequest request, Long userId) {
        PromoCode promo = find(request.code());
        Instant now = Instant.now();
        if (!promo.isActive() || now.isBefore(promo.getStartTime()) || now.isAfter(promo.getExpiryTime())) throw new BadRequestException("Promo code is not active");
        if (request.subtotal().compareTo(promo.getMinimumOrderAmount()) < 0) throw new BadRequestException("Order does not meet the promo minimum");
        if (promo.getUsageLimit() > 0 && promo.getUsageCount() >= promo.getUsageLimit()) throw new BadRequestException("Promo code usage limit reached");
        if (promo.getRestaurantId() != null && !promo.getRestaurantId().equals(request.restaurantId())) throw new BadRequestException("Promo is not valid for this restaurant");
        if (userId != null && promo.getPerUserLimit() > 0 && promoUsageRepository.countByPromoIdAndUserId(promo.getId(), userId) >= promo.getPerUserLimit()) throw new BadRequestException("You have reached this promo's usage limit");
        BigDecimal discount = promo.getDiscountType() == DiscountType.PERCENTAGE ? request.subtotal().multiply(promo.getDiscountValue()).divide(new BigDecimal("100")) : promo.getDiscountValue();
        if (promo.getMaximumDiscount() != null) discount = discount.min(promo.getMaximumDiscount());
        discount = discount.min(request.subtotal()).setScale(2, java.math.RoundingMode.HALF_UP);
        return new PromoValidationResponse(promo.getCode(), discount, request.subtotal().subtract(discount));
    }

    public PromoResponse create(PromoRequest request) { PromoCode saved = promoCodeRepository.save(new PromoCode(request.code().trim().toUpperCase(), request.discountType(), request.discountValue(), request.minimumOrderAmount(), request.maximumDiscount(), request.startTime(), request.expiryTime(), request.usageLimit(), request.perUserLimit(), request.restaurantId())); auditService.record(null, "PROMO_CREATED", "PROMO", saved.getId(), null, "code=" + saved.getCode()); return toResponse(saved); }
    public PromoResponse update(Long id, PromoRequest request) { PromoCode promo = promoCodeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Promo not found: " + id)); promo.update(request.code().trim().toUpperCase(), request.discountType(), request.discountValue(), request.minimumOrderAmount(), request.maximumDiscount(), request.startTime(), request.expiryTime(), request.usageLimit(), request.perUserLimit(), request.restaurantId()); auditService.record(null, "PROMO_UPDATED", "PROMO", id, null, "code=" + promo.getCode()); return toResponse(promo); }
    public void delete(Long id) { PromoCode promo = promoCodeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Promo not found: " + id)); promo.deactivate(); auditService.record(null, "PROMO_DELETED", "PROMO", id, null, "deactivated=true"); }
    @Transactional(readOnly = true) public List<PromoResponse> findAll() { return promoCodeRepository.findAll().stream().map(this::toResponse).toList(); }
    @Scheduled(cron = "${platter.scheduler.promo-expiry}") public void deactivateExpired() { promoCodeRepository.findByActiveTrueAndExpiryTimeBefore(Instant.now()).forEach(PromoCode::deactivate); }
    private PromoCode find(String code) { return promoCodeRepository.findByCodeIgnoreCase(code.trim()).orElseThrow(() -> new BadRequestException("Promo code not found")); }
    private PromoResponse toResponse(PromoCode p) { return new PromoResponse(p.getId(), p.getCode(), p.getDiscountType(), p.getDiscountValue(), p.getMinimumOrderAmount(), p.getMaximumDiscount(), p.getStartTime(), p.getExpiryTime(), p.getUsageLimit(), p.getUsageCount(), p.getPerUserLimit(), p.getRestaurantId(), p.isActive()); }
}
