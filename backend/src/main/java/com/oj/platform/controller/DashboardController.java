package com.oj.platform.controller;

import com.oj.platform.dto.CertificateDto;
import com.oj.platform.dto.DashboardActivityDto;
import com.oj.platform.dto.LanguageStatsDto;
import com.oj.platform.dto.MilestoneProgressDto;
import com.oj.platform.dto.UserDashboardStatsDto;
import com.oj.platform.security.UserPrincipal;
import com.oj.platform.service.CertificateService;
import com.oj.platform.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;
    private final CertificateService certificateService;

    public DashboardController(DashboardService dashboardService, CertificateService certificateService) {
        this.dashboardService = dashboardService;
        this.certificateService = certificateService;
    }

    @GetMapping("/stats")
    public ResponseEntity<UserDashboardStatsDto> getStats(@AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(dashboardService.getUserDashboardStats(currentUser.getId()));
    }

    @GetMapping("/activity")
    public ResponseEntity<DashboardActivityDto> getActivity(@AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(dashboardService.getDashboardActivity(currentUser.getId()));
    }

    @GetMapping("/language-stats")
    public ResponseEntity<List<LanguageStatsDto>> getLanguageStats(@AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(dashboardService.getLanguageStats(currentUser.getId()));
    }

    @GetMapping("/milestones")
    public ResponseEntity<MilestoneProgressDto> getMilestones(@AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(dashboardService.getMilestoneProgress(currentUser.getId()));
    }

    @GetMapping("/certificates")
    public ResponseEntity<List<CertificateDto>> getCertificates(@AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(certificateService.getUserCertificates(currentUser.getId()));
    }
}
