package com.oj.platform.controller;

import com.oj.platform.dto.AdminAnalyticsDto;
import com.oj.platform.service.AdminAnalyticsService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/analytics")
@PreAuthorize("hasRole('ADMIN')")
public class AdminAnalyticsController {

    private final AdminAnalyticsService adminAnalyticsService;

    public AdminAnalyticsController(AdminAnalyticsService adminAnalyticsService) {
        this.adminAnalyticsService = adminAnalyticsService;
    }

    @GetMapping("/overview")
    public ResponseEntity<AdminAnalyticsDto> getOverviewAnalytics(
            @RequestParam(name = "range", required = false, defaultValue = "ALL_TIME") String range) {
        AdminAnalyticsDto dto = adminAnalyticsService.getAnalytics(range);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportAnalyticsCsv(
            @RequestParam(name = "range", required = false, defaultValue = "ALL_TIME") String range) {
        String csvData = adminAnalyticsService.generateCsvReport(range);
        byte[] output = csvData.getBytes(java.nio.charset.StandardCharsets.UTF_8);

        String filename = "codenova_analytics_" + (range != null ? range.toLowerCase() : "all_time") + ".csv";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(output);
    }
}
