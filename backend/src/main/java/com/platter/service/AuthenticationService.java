package com.platter.service;

import com.platter.dto.AuthResponse;
import com.platter.dto.LoginRequest;
import com.platter.dto.RefreshRequest;
import com.platter.dto.RegisterRequest;
import com.platter.dto.UserResponse;
import com.platter.audit.AuditService;
import com.platter.entity.RefreshToken;
import com.platter.entity.Role;
import com.platter.entity.UserEntity;
import com.platter.exception.ConflictException;
import com.platter.exception.UnauthorizedException;
import com.platter.repository.RefreshTokenRepository;
import com.platter.repository.UserRepository;
import com.platter.security.JwtService;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthenticationService {
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final AuditService auditService;

    public AuthenticationService(UserRepository userRepository, RefreshTokenRepository refreshTokenRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JwtService jwtService, AuditService auditService) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.auditService = auditService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new ConflictException("An account already exists for this email");
        }
        UserEntity user = userRepository.save(new UserEntity(request.name().trim(), request.email().trim().toLowerCase(), passwordEncoder.encode(request.password()), Role.CUSTOMER));
        auditService.record(user.getId(), "REGISTER", "USER", user.getId(), null, "account_registered");
        return issueTokens(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.email().trim().toLowerCase(), request.password()));
        UserEntity user = userRepository.findByEmailIgnoreCase(authentication.getName()).orElseThrow(() -> new UnauthorizedException("Invalid credentials"));
        auditService.record(user.getId(), "LOGIN", "USER", user.getId(), null, "login_success");
        return issueTokens(user);
    }

    @Transactional
    public AuthResponse refresh(RefreshRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.refreshToken()).filter(token -> !token.isExpired()).orElseThrow(() -> new UnauthorizedException("Refresh token is invalid or expired"));
        refreshTokenRepository.delete(refreshToken);
        return issueTokens(refreshToken.getUser());
    }

    @Transactional
    public void logout(String token) {
        refreshTokenRepository.deleteByToken(token);
    }

    public UserResponse toResponse(UserEntity user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole());
    }

    private AuthResponse issueTokens(UserEntity user) {
        String refreshValue = UUID.randomUUID().toString();
        refreshTokenRepository.save(new RefreshToken(refreshValue, Instant.now().plus(30, ChronoUnit.DAYS), user));
        return new AuthResponse(jwtService.generateAccessToken(user), refreshValue, toResponse(user));
    }
}
