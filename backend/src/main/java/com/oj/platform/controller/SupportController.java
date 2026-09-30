package com.oj.platform.controller;

import com.oj.platform.dto.*;
import com.oj.platform.security.UserPrincipal;
import com.oj.platform.service.SupportFeedbackService;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/support")
public class SupportController {

    private final SupportFeedbackService supportFeedbackService;

    public SupportController(SupportFeedbackService supportFeedbackService) {
        this.supportFeedbackService = supportFeedbackService;
    }

    @PostMapping
    public ResponseEntity<SupportRequestDto> createSupportRequest(
            @Valid @RequestBody CreateSupportRequest req,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(supportFeedbackService.createSupportRequest(req, currentUser.getId()));
    }

    @GetMapping
    public ResponseEntity<List<SupportRequestDto>> getUserSupportRequests(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(supportFeedbackService.getMySupportRequests(currentUser.getId(), status, category, search));
    }

    @GetMapping("/my")
    public ResponseEntity<List<SupportRequestDto>> getMySupportRequests(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(supportFeedbackService.getMySupportRequests(currentUser.getId(), status, category, search));
    }

    @GetMapping("/summary")
    public ResponseEntity<UserSupportSummaryDto> getUserSupportSummary(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(supportFeedbackService.getUserSupportSummary(currentUser.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SupportRequestDto> getSupportRequestById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        boolean isAdmin = currentUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return ResponseEntity.ok(supportFeedbackService.getSupportRequestById(id, currentUser.getId(), isAdmin));
    }

    @PostMapping("/{id}/messages")
    public ResponseEntity<SupportMessageDto> addSupportMessage(
            @PathVariable Long id,
            @Valid @RequestBody CreateSupportMessageRequest req,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        boolean isAdmin = currentUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return ResponseEntity.ok(supportFeedbackService.addSupportMessage(id, req, currentUser.getId(), isAdmin));
    }

    @PostMapping("/{id}/resolve")
    public ResponseEntity<SupportRequestDto> resolveSupportTicket(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        boolean isAdmin = currentUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return ResponseEntity.ok(supportFeedbackService.resolveSupportTicket(id, currentUser.getId(), isAdmin));
    }

    @PostMapping("/{id}/reopen")
    public ResponseEntity<SupportRequestDto> reopenSupportTicket(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        boolean isAdmin = currentUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return ResponseEntity.ok(supportFeedbackService.reopenSupportTicket(id, currentUser.getId(), isAdmin));
    }

    @PostMapping("/{id}/close")
    public ResponseEntity<SupportRequestDto> closeSupportTicket(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        boolean isAdmin = currentUser.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return ResponseEntity.ok(supportFeedbackService.closeSupportTicket(id, currentUser.getId(), isAdmin));
    }

    @PostMapping("/{id}/rating")
    public ResponseEntity<SupportRequestDto> submitSupportRating(
            @PathVariable Long id,
            @Valid @RequestBody SupportRatingRequest ratingReq,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(supportFeedbackService.submitSupportRating(id, ratingReq, currentUser.getId()));
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadAttachment(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(supportFeedbackService.storeAttachment(file, currentUser.getId()));
    }

    @GetMapping("/attachments/{fileName:.+}")
    public ResponseEntity<Resource> downloadAttachment(@PathVariable String fileName) {
        Resource resource = supportFeedbackService.getAttachmentResource(fileName);

        String contentType = "application/octet-stream";
        try {
            contentType = Files.probeContentType(resource.getFile().toPath());
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
        } catch (IOException ignored) {}

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}
