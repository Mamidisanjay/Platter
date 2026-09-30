package com.platter.dto;

import com.platter.entity.Role;

public record UserResponse(Long id, String name, String email, Role role) { }
