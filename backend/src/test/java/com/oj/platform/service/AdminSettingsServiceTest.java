package com.oj.platform.service;

import com.oj.platform.dto.PlatformConfigDto;
import com.oj.platform.dto.SystemStatusDto;
import com.oj.platform.dto.UpdatePlatformConfigRequest;
import com.oj.platform.entity.PlatformConfig;
import com.oj.platform.repository.PlatformConfigRepository;
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
public class AdminSettingsServiceTest {

    @Mock
    private PlatformConfigRepository platformConfigRepository;

    @InjectMocks
    private AdminSettingsService adminSettingsService;

    private PlatformConfig testConfig;

    @BeforeEach
    void setUp() {
        testConfig = new PlatformConfig();
        testConfig.setId(1L);
        testConfig.setPlatformName("CodeNova");
        testConfig.setMaxAssessmentDurationMinutes(180);
    }

    @Test
    void testGetPlatformSettings() {
        when(platformConfigRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(testConfig));

        PlatformConfigDto dto = adminSettingsService.getPlatformSettings();
        assertNotNull(dto);
        assertEquals("CodeNova", dto.getPlatformName());
        assertEquals(180, dto.getMaxAssessmentDurationMinutes());
        assertTrue(dto.isProctoringEnabledDefault());
    }

    @Test
    void testUpdatePlatformSettings() {
        when(platformConfigRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(testConfig));
        when(platformConfigRepository.save(any(PlatformConfig.class))).thenAnswer(i -> i.getArgument(0));

        UpdatePlatformConfigRequest req = new UpdatePlatformConfigRequest();
        req.setPlatformName("CodeNova Pro");
        req.setMaxAssessmentDurationMinutes(240);
        req.setMaintenanceMode(true);

        PlatformConfigDto updated = adminSettingsService.updatePlatformSettings(req);
        assertNotNull(updated);
        assertEquals("CodeNova Pro", updated.getPlatformName());
        assertEquals(240, updated.getMaxAssessmentDurationMinutes());
        assertTrue(updated.isMaintenanceMode());
        verify(platformConfigRepository).save(any(PlatformConfig.class));
    }

    @Test
    void testGetSystemStatus() {
        SystemStatusDto status = adminSettingsService.getSystemStatus();
        assertNotNull(status);
        assertEquals("OPERATIONAL", status.getStatus());
        assertNotNull(status.getJavaVersion());
        assertTrue(status.getTotalMemoryMb() > 0);
    }
}
