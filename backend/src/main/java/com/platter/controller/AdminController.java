package com.platter.controller;

import com.platter.dto.AdminDashboardResponse;
import com.platter.dto.AdminPaymentResponse;
import com.platter.dto.AuditLogResponse;
import com.platter.dto.OrderResponse;
import com.platter.dto.RestaurantResponse;
import com.platter.dto.ReviewResponse;
import com.platter.dto.UserResponse;
import com.platter.service.AdminService;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private final AdminService adminService;
    public AdminController(AdminService adminService) { this.adminService=adminService; }
    @GetMapping("/dashboard") public AdminDashboardResponse dashboard() { return adminService.dashboard(); }
    @GetMapping("/orders") public List<OrderResponse> orders() { return adminService.orders(); }
    @GetMapping("/users") public List<UserResponse> users() { return adminService.users(); }
    @GetMapping("/restaurants") public List<RestaurantResponse> restaurants() { return adminService.restaurants(); }
    @GetMapping("/payments") public List<AdminPaymentResponse> payments() { return adminService.payments(); }
    @GetMapping("/audit-logs") public List<AuditLogResponse> auditLogs() { return adminService.auditLogs(); }
    @GetMapping("/reviews") public List<ReviewResponse> reviews() { return adminService.reviews(); }
}
