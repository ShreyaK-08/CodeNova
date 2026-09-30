package com.oj.platform.controller;

import com.oj.platform.dto.UpdateUserSettingsRequest;
import com.oj.platform.dto.UserSettingsDto;
import com.oj.platform.security.UserPrincipal;
import com.oj.platform.service.UserSettingsService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/settings")
public class UserSettingsController {

    private final UserSettingsService userSettingsService;

    public UserSettingsController(UserSettingsService userSettingsService) {
        this.userSettingsService = userSettingsService;
    }

    @GetMapping
    public ResponseEntity<UserSettingsDto> getUserSettings(@AuthenticationPrincipal UserPrincipal currentUser) {
        UserSettingsDto settings = userSettingsService.getUserSettings(currentUser.getId());
        return ResponseEntity.ok(settings);
    }

    @PutMapping
    public ResponseEntity<UserSettingsDto> updateUserSettings(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody UpdateUserSettingsRequest request) {
        UserSettingsDto updated = userSettingsService.updateUserSettings(currentUser.getId(), request);
        return ResponseEntity.ok(updated);
    }
}
