package com.oj.platform.controller;

import com.oj.platform.dto.*;
import com.oj.platform.security.UserPrincipal;
import com.oj.platform.service.SupportFeedbackService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminSupportFeedbackController {

    private final SupportFeedbackService supportFeedbackService;

    public AdminSupportFeedbackController(SupportFeedbackService supportFeedbackService) {
        this.supportFeedbackService = supportFeedbackService;
    }

    @GetMapping("/support")
    public ResponseEntity<List<SupportRequestDto>> getAllSupportRequests(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Long assignedToId,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(supportFeedbackService.getAdminSupportRequests(status, priority, category, assignedToId, search));
    }

    @GetMapping("/support/{id}")
    public ResponseEntity<SupportRequestDto> getAdminSupportTicket(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(supportFeedbackService.getSupportRequestById(id, currentUser.getId(), true));
    }

    @PatchMapping("/support/{id}/triage")
    public ResponseEntity<SupportRequestDto> triageSupportTicket(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSupportTicketRequest triageReq,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(supportFeedbackService.adminTriageTicket(id, triageReq, currentUser.getId()));
    }

    @PutMapping("/support/{id}/status")
    public ResponseEntity<SupportRequestDto> updateSupportStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSupportStatusRequest req,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        UpdateSupportTicketRequest triage = new UpdateSupportTicketRequest();
        triage.setStatus(req.getStatus());
        return ResponseEntity.ok(supportFeedbackService.adminTriageTicket(id, triage, currentUser.getId()));
    }

    @PostMapping("/support/{id}/messages")
    public ResponseEntity<SupportMessageDto> addAdminSupportMessage(
            @PathVariable Long id,
            @Valid @RequestBody CreateSupportMessageRequest req,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(supportFeedbackService.addSupportMessage(id, req, currentUser.getId(), true));
    }

    @PostMapping("/support/{id}/ai-draft")
    public ResponseEntity<AiSupportDraftResponse> generateAiSupportDraft(
            @PathVariable Long id,
            @RequestBody(required = false) AiSupportDraftRequest req,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(supportFeedbackService.generateAiSupportDraft(id, req, currentUser.getId()));
    }

    @GetMapping("/support/staff")
    public ResponseEntity<List<StaffMemberDto>> getStaffMembers() {
        return ResponseEntity.ok(supportFeedbackService.getStaffMembers());
    }

    @GetMapping("/support/analytics")
    public ResponseEntity<SupportAnalyticsDto> getSupportAnalytics() {
        return ResponseEntity.ok(supportFeedbackService.getSupportAnalytics());
    }

    @GetMapping("/assessment-feedback")
    public ResponseEntity<List<AssessmentQuestionFeedbackDto>> getAllAssessmentQuestionFeedback() {
        return ResponseEntity.ok(supportFeedbackService.getAllAssessmentQuestionFeedback());
    }

    @GetMapping("/assessment-post-feedback")
    public ResponseEntity<List<com.oj.platform.dto.AssessmentFeedbackDto>> getAllAssessmentFeedback() {
        return ResponseEntity.ok(supportFeedbackService.getAllAssessmentFeedbackForAdmin());
    }

    @GetMapping("/feedback")
    public ResponseEntity<List<GeneralFeedbackDto>> getAllGeneralFeedback() {
        return ResponseEntity.ok(supportFeedbackService.getAllGeneralFeedback());
    }
}
