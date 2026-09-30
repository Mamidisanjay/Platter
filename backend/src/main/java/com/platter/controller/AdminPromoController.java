package com.platter.controller;

import com.platter.dto.PromoRequest;
import com.platter.dto.PromoResponse;
import com.platter.service.PromoService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/promos")
@PreAuthorize("hasRole('ADMIN')")
public class AdminPromoController {
    private final PromoService promoService;
    public AdminPromoController(PromoService promoService) { this.promoService = promoService; }
    @GetMapping public List<PromoResponse> findAll() { return promoService.findAll(); }
    @PostMapping public ResponseEntity<PromoResponse> create(@Valid @RequestBody PromoRequest request) { return ResponseEntity.status(HttpStatus.CREATED).body(promoService.create(request)); }
    @PutMapping("/{id}") public PromoResponse update(@PathVariable Long id, @Valid @RequestBody PromoRequest request) { return promoService.update(id, request); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable Long id) { promoService.delete(id); return ResponseEntity.noContent().build(); }
}
