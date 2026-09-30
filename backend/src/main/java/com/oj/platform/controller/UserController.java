package com.oj.platform.controller;

import com.oj.platform.dto.ChangePasswordRequest;
import com.oj.platform.dto.ProfileUpdateRequest;
import com.oj.platform.dto.SubmissionResponse;
import com.oj.platform.dto.UserDashboardStatsDto;
import com.oj.platform.dto.UserProfileResponse;
import com.oj.platform.security.UserPrincipal;
import com.oj.platform.service.DashboardService;
import com.oj.platform.service.SubmissionService;
import com.oj.platform.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final DashboardService dashboardService;
    private final SubmissionService submissionService;

    public UserController(UserService userService, DashboardService dashboardService, SubmissionService submissionService) {
        this.userService = userService;
        this.dashboardService = dashboardService;
        this.submissionService = submissionService;
    }

    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getCurrentUserProfile(@AuthenticationPrincipal UserPrincipal currentUser) {
        UserProfileResponse profile = userService.getProfile(currentUser.getId());
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/profile")
    public ResponseEntity<UserProfileResponse> updateCurrentUserProfile(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody ProfileUpdateRequest updateRequest) {
        UserProfileResponse updated = userService.updateProfile(currentUser.getId(), updateRequest);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(currentUser.getId(), request);
        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }

    @GetMapping("/dashboard-stats")
    public ResponseEntity<UserDashboardStatsDto> getUserDashboardStats(@AuthenticationPrincipal UserPrincipal currentUser) {
        UserDashboardStatsDto stats = dashboardService.getUserDashboardStats(currentUser.getId());
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/submissions")
    public ResponseEntity<List<SubmissionResponse>> getUserSubmissions(@AuthenticationPrincipal UserPrincipal currentUser) {
        List<SubmissionResponse> submissions = submissionService.getUserSubmissions(currentUser.getId());
        return ResponseEntity.ok(submissions);
    }
}
