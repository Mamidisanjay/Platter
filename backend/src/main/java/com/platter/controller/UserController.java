package com.platter.controller;

import com.platter.dto.UpdateProfileRequest;
import com.platter.dto.UserResponse;
import com.platter.entity.UserEntity;
import com.platter.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal UserDetails principal) {
        return toResponse(findUser(principal));
    }

    @PutMapping("/me")
    public UserResponse update(@AuthenticationPrincipal UserDetails principal, @Valid @RequestBody UpdateProfileRequest request) {
        UserEntity user = findUser(principal);
        user.updateProfile(request.name().trim());
        return toResponse(userRepository.save(user));
    }

    private UserEntity findUser(UserDetails principal) {
        return userRepository.findByEmailIgnoreCase(principal.getUsername()).orElseThrow(() -> new IllegalStateException("Authenticated user no longer exists"));
    }

    private UserResponse toResponse(UserEntity user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole());
    }
}
