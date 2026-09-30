package com.platter.controller;

import com.platter.dto.PromoValidationRequest;
import com.platter.dto.PromoValidationResponse;
import com.platter.service.PromoService;
import com.platter.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/promos")
public class PromoController {
    private final PromoService promoService;
    private final UserRepository userRepository;
    public PromoController(PromoService promoService, UserRepository userRepository) { this.promoService = promoService; this.userRepository = userRepository; }
    @PostMapping("/validate")
    public PromoValidationResponse validate(@AuthenticationPrincipal UserDetails user, @Valid @RequestBody PromoValidationRequest request) { return promoService.validate(request, user == null ? null : userRepository.findByEmailIgnoreCase(user.getUsername()).map(account -> account.getId()).orElse(null)); }
}
