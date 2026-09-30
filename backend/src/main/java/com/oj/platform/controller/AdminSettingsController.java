package com.oj.platform.controller;

import com.oj.platform.dto.PlatformConfigDto;
import com.oj.platform.dto.SystemStatusDto;
import com.oj.platform.dto.UpdatePlatformConfigRequest;
import com.oj.platform.service.AdminSettingsService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/settings")
@PreAuthorize("hasRole('ADMIN')")
public class AdminSettingsController {

    private final AdminSettingsService adminSettingsService;

    public AdminSettingsController(AdminSettingsService adminSettingsService) {
        this.adminSettingsService = adminSettingsService;
    }

    @GetMapping
    public ResponseEntity<PlatformConfigDto> getPlatformSettings() {
        return ResponseEntity.ok(adminSettingsService.getPlatformSettings());
    }

    @PutMapping
    public ResponseEntity<PlatformConfigDto> updatePlatformSettings(@Valid @RequestBody UpdatePlatformConfigRequest request) {
        return ResponseEntity.ok(adminSettingsService.updatePlatformSettings(request));
    }

    @GetMapping("/system-status")
    public ResponseEntity<SystemStatusDto> getSystemStatus() {
        return ResponseEntity.ok(adminSettingsService.getSystemStatus());
    }
}
