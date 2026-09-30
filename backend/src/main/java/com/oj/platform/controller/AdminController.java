package com.oj.platform.controller;

import com.oj.platform.dto.AdminDashboardStatsDto;
import com.oj.platform.dto.UserProfileResponse;
import com.oj.platform.security.UserPrincipal;
import com.oj.platform.service.DashboardService;
import com.oj.platform.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final DashboardService dashboardService;
    private final UserService userService;

    public AdminController(DashboardService dashboardService, UserService userService) {
        this.dashboardService = dashboardService;
        this.userService = userService;
    }

    @GetMapping("/dashboard-stats")
    public ResponseEntity<AdminDashboardStatsDto> getAdminDashboardStats() {
        AdminDashboardStatsDto stats = dashboardService.getAdminDashboardStats();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserProfileResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Map<String, String>> deleteUser(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long id) {
        if (currentUser.getId().equals(id)) {
            return ResponseEntity.badRequest().body(Map.of("message", "You cannot delete your own account"));
        }
        userService.deleteUser(id);
        return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
    }
}
