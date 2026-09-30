package com.oj.platform.service;

import com.oj.platform.dto.PlatformConfigDto;
import com.oj.platform.dto.SystemStatusDto;
import com.oj.platform.dto.UpdatePlatformConfigRequest;
import com.oj.platform.entity.PlatformConfig;
import com.oj.platform.repository.PlatformConfigRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

@Service
public class AdminSettingsService {

    private final PlatformConfigRepository platformConfigRepository;

    public AdminSettingsService(PlatformConfigRepository platformConfigRepository) {
        this.platformConfigRepository = platformConfigRepository;
    }

    @Transactional(readOnly = true)
    public PlatformConfigDto getPlatformSettings() {
        PlatformConfig config = platformConfigRepository.findFirstByOrderByIdAsc()
                .orElseGet(PlatformConfig::new);
        return toDto(config);
    }

    @Transactional
    public PlatformConfigDto updatePlatformSettings(UpdatePlatformConfigRequest request) {
        PlatformConfig config = getOrCreateConfig();

        if (request.getPlatformName() != null && !request.getPlatformName().isBlank()) {
            config.setPlatformName(request.getPlatformName().trim());
        }
        if (request.getPlatformType() != null && !request.getPlatformType().isBlank()) {
            config.setPlatformType(request.getPlatformType().trim());
        }
        if (request.getEnvironment() != null && !request.getEnvironment().isBlank()) {
            config.setEnvironment(request.getEnvironment().trim());
        }
        if (request.getMaxAssessmentDurationMinutes() != null) {
            config.setMaxAssessmentDurationMinutes(request.getMaxAssessmentDurationMinutes());
        }
        if (request.getMaxAssessmentAttemptsDefault() != null) {
            config.setMaxAssessmentAttemptsDefault(request.getMaxAssessmentAttemptsDefault());
        }
        if (request.getProctoringEnabledDefault() != null) {
            config.setProctoringEnabledDefault(request.getProctoringEnabledDefault());
        }
        if (request.getProgrammingQuestionsEnabled() != null) {
            config.setProgrammingQuestionsEnabled(request.getProgrammingQuestionsEnabled());
        }
        if (request.getContestCreationEnabled() != null) {
            config.setContestCreationEnabled(request.getContestCreationEnabled());
        }
        if (request.getContestNotificationsEnabled() != null) {
            config.setContestNotificationsEnabled(request.getContestNotificationsEnabled());
        }
        if (request.getContestProctoringAvailable() != null) {
            config.setContestProctoringAvailable(request.getContestProctoringAvailable());
        }
        if (request.getEmailNotificationsEnabled() != null) {
            config.setEmailNotificationsEnabled(request.getEmailNotificationsEnabled());
        }
        if (request.getContestAnnouncementNotificationsEnabled() != null) {
            config.setContestAnnouncementNotificationsEnabled(request.getContestAnnouncementNotificationsEnabled());
        }
        if (request.getHostVerificationNotificationsEnabled() != null) {
            config.setHostVerificationNotificationsEnabled(request.getHostVerificationNotificationsEnabled());
        }
        if (request.getMaintenanceMode() != null) {
            config.setMaintenanceMode(request.getMaintenanceMode());
        }

        PlatformConfig saved = platformConfigRepository.save(config);
        return toDto(saved);
    }

    public SystemStatusDto getSystemStatus() {
        SystemStatusDto status = new SystemStatusDto();
        status.setStatus("OPERATIONAL");
        status.setJavaVersion(System.getProperty("java.version"));
        status.setOsName(System.getProperty("os.name") + " (" + System.getProperty("os.arch") + ")");

        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        status.setUptimeSeconds(runtimeMXBean.getUptime() / 1000);

        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long maxMemory = runtime.maxMemory();

        status.setTotalMemoryMb(totalMemory / (1024 * 1024));
        status.setFreeMemoryMb(freeMemory / (1024 * 1024));
        status.setUsedMemoryMb((totalMemory - freeMemory) / (1024 * 1024));
        status.setMaxMemoryMb(maxMemory / (1024 * 1024));
        status.setAvailableProcessors(runtime.availableProcessors());
        status.setActiveThreads(Thread.activeCount());
        status.setTimestamp(System.currentTimeMillis());

        return status;
    }

    private PlatformConfig getOrCreateConfig() {
        return platformConfigRepository.findFirstByOrderByIdAsc()
                .orElseGet(() -> {
                    PlatformConfig fresh = new PlatformConfig();
                    return platformConfigRepository.save(fresh);
                });
    }

    private PlatformConfigDto toDto(PlatformConfig config) {
        PlatformConfigDto dto = new PlatformConfigDto();
        dto.setPlatformName(config.getPlatformName());
        dto.setPlatformType(config.getPlatformType());
        dto.setEnvironment(config.getEnvironment());
        dto.setMaxAssessmentDurationMinutes(config.getMaxAssessmentDurationMinutes());
        dto.setMaxAssessmentAttemptsDefault(config.getMaxAssessmentAttemptsDefault());
        dto.setProctoringEnabledDefault(config.isProctoringEnabledDefault());
        dto.setProgrammingQuestionsEnabled(config.isProgrammingQuestionsEnabled());
        dto.setContestCreationEnabled(config.isContestCreationEnabled());
        dto.setContestNotificationsEnabled(config.isContestNotificationsEnabled());
        dto.setContestProctoringAvailable(config.isContestProctoringAvailable());
        dto.setEmailNotificationsEnabled(config.isEmailNotificationsEnabled());
        dto.setContestAnnouncementNotificationsEnabled(config.isContestAnnouncementNotificationsEnabled());
        dto.setHostVerificationNotificationsEnabled(config.isHostVerificationNotificationsEnabled());
        dto.setMaintenanceMode(config.isMaintenanceMode());
        return dto;
    }
}
