package com.oj.platform.service;

import com.oj.platform.dto.UpdateUserSettingsRequest;
import com.oj.platform.dto.UserSettingsDto;
import com.oj.platform.entity.Role;
import com.oj.platform.entity.User;
import com.oj.platform.entity.UserSettings;
import com.oj.platform.exception.ResourceNotFoundException;
import com.oj.platform.repository.UserRepository;
import com.oj.platform.repository.UserSettingsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserSettingsServiceTest {

    @Mock
    private UserSettingsRepository userSettingsRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserSettingsService userSettingsService;

    private User testUser;
    private UserSettings testSettings;

    @BeforeEach
    void setUp() {
        testUser = new User("Test User", "testuser", "test@example.com", "password", Role.ROLE_USER);
        testUser.setId(10L);
        testUser.setEmailVerified(true);

        testSettings = new UserSettings(testUser);
        testSettings.setId(100L);
    }

    @Test
    void testGetUserSettings_ExistingSettings() {
        when(userRepository.findById(10L)).thenReturn(Optional.of(testUser));
        when(userSettingsRepository.findByUserId(10L)).thenReturn(Optional.of(testSettings));

        UserSettingsDto dto = userSettingsService.getUserSettings(10L);
        assertNotNull(dto);
        assertEquals("testuser", dto.getUsername());
        assertEquals("test@example.com", dto.getEmail());
        assertEquals("en", dto.getLanguage());
        assertTrue(dto.isEmailNotifications());
    }

    @Test
    void testGetUserSettings_DefaultWhenNoneExists() {
        when(userRepository.findById(10L)).thenReturn(Optional.of(testUser));
        when(userSettingsRepository.findByUserId(10L)).thenReturn(Optional.empty());

        UserSettingsDto dto = userSettingsService.getUserSettings(10L);
        assertNotNull(dto);
        assertEquals("testuser", dto.getUsername());
        assertEquals("en", dto.getLanguage());
    }

    @Test
    void testGetUserSettings_UserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userSettingsService.getUserSettings(999L));
    }

    @Test
    void testUpdateUserSettings_Success() {
        when(userRepository.findById(10L)).thenReturn(Optional.of(testUser));
        when(userSettingsRepository.findByUserId(10L)).thenReturn(Optional.of(testSettings));
        when(userSettingsRepository.save(any(UserSettings.class))).thenAnswer(i -> i.getArgument(0));

        UpdateUserSettingsRequest req = new UpdateUserSettingsRequest();
        req.setLanguage("es");
        req.setEmailNotifications(false);
        req.setShowLeaderboard(false);

        UserSettingsDto updated = userSettingsService.updateUserSettings(10L, req);
        assertNotNull(updated);
        assertEquals("es", updated.getLanguage());
        assertFalse(updated.isEmailNotifications());
        assertFalse(updated.isShowLeaderboard());
        verify(userSettingsRepository).save(any(UserSettings.class));
    }
}
