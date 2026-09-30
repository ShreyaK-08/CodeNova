package com.oj.platform.controller;

import com.oj.platform.dto.EmailStatsDto;
import com.oj.platform.dto.NotificationLogDto;
import com.oj.platform.dto.SendTestEmailRequest;
import com.oj.platform.service.EmailService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/emails")
@PreAuthorize("hasRole('ADMIN')")
public class AdminEmailController {

    private final EmailService emailService;

    public AdminEmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @GetMapping("/logs")
    public ResponseEntity<List<NotificationLogDto>> getEmailLogs() {
        return ResponseEntity.ok(emailService.getRecentLogs());
    }

    @GetMapping("/stats")
    public ResponseEntity<EmailStatsDto> getEmailStats() {
        return ResponseEntity.ok(emailService.getEmailStats());
    }

    @PostMapping("/test")
    public ResponseEntity<Map<String, Object>> sendTestEmail(@Valid @RequestBody SendTestEmailRequest request) {
        EmailService.EmailResult result = emailService.sendTestEmail(request.getRecipientEmail(), request.getNote());
        Map<String, Object> response = new HashMap<>();
        response.put("status", result.getStatus().name());
        response.put("message", result.getMessage());
        response.put("sent", result.isSent());
        return ResponseEntity.ok(response);
    }
}
