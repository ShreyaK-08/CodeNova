package com.oj.platform.service;

import com.oj.platform.dto.UpdateUserSettingsRequest;
import com.oj.platform.dto.UserSettingsDto;
import com.oj.platform.entity.User;
import com.oj.platform.entity.UserSettings;
import com.oj.platform.exception.ResourceNotFoundException;
import com.oj.platform.repository.UserRepository;
import com.oj.platform.repository.UserSettingsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserSettingsService {

    private final UserSettingsRepository userSettingsRepository;
    private final UserRepository userRepository;

    public UserSettingsService(UserSettingsRepository userSettingsRepository, UserRepository userRepository) {
        this.userSettingsRepository = userSettingsRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public UserSettingsDto getUserSettings(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        UserSettings settings = userSettingsRepository.findByUserId(userId)
                .orElseGet(() -> {
                    UserSettings defaultSettings = new UserSettings(user);
                    return defaultSettings;
                });

        return toDto(user, settings);
    }

    @Transactional
    public UserSettingsDto updateUserSettings(Long userId, UpdateUserSettingsRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        UserSettings settings = userSettingsRepository.findByUserId(userId)
                .orElseGet(() -> new UserSettings(user));

        if (request.getLanguage() != null && !request.getLanguage().isBlank()) {
            settings.setLanguage(request.getLanguage().trim().toLowerCase());
        }
        if (request.getEmailNotifications() != null) {
            settings.setEmailNotifications(request.getEmailNotifications());
        }
        if (request.getContestNotifications() != null) {
            settings.setContestNotifications(request.getContestNotifications());
        }
        if (request.getAssessmentNotifications() != null) {
            settings.setAssessmentNotifications(request.getAssessmentNotifications());
        }
        if (request.getPlatformUpdates() != null) {
            settings.setPlatformUpdates(request.getPlatformUpdates());
        }
        if (request.getTheme() != null && !request.getTheme().isBlank()) {
            settings.setTheme(request.getTheme());
        }
        if (request.getShowProfile() != null) {
            settings.setShowProfile(request.getShowProfile());
        }
        if (request.getShowLeaderboard() != null) {
            settings.setShowLeaderboard(request.getShowLeaderboard());
        }

        settings.setUser(user);
        UserSettings saved = userSettingsRepository.save(settings);
        return toDto(user, saved);
    }

    private UserSettingsDto toDto(User user, UserSettings settings) {
        UserSettingsDto dto = new UserSettingsDto();
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole() != null ? user.getRole().name() : "ROLE_USER");
        dto.setAccountStatus(user.isEmailVerified() ? "Active (Verified)" : "Active");
        dto.setLanguage(settings.getLanguage());
        dto.setEmailNotifications(settings.isEmailNotifications());
        dto.setContestNotifications(settings.isContestNotifications());
        dto.setAssessmentNotifications(settings.isAssessmentNotifications());
        dto.setPlatformUpdates(settings.isPlatformUpdates());
        dto.setTheme(settings.getTheme());
        dto.setShowProfile(settings.isShowProfile());
        dto.setShowLeaderboard(settings.isShowLeaderboard());
        return dto;
    }
}
