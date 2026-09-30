package com.oj.platform.controller;

import com.oj.platform.dto.*;
import com.oj.platform.security.UserPrincipal;
import com.oj.platform.service.AssessmentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Host Assessment authoring API for verified non-admin users.
 * - Service enforces host-verification and strict ownership checks (never trusts route hiding).
 * - Admins can also use this API for their own assessments.
 *
 * New in this version:
 *   - Test case management endpoints for PROGRAMMING questions
 *   - /attempts endpoint for host results view
 */
@RestController
@RequestMapping("/api/assessments/host")
public class HostAssessmentController {

    private final AssessmentService assessmentService;
    private final com.oj.platform.service.SupportFeedbackService supportFeedbackService;

    public HostAssessmentController(AssessmentService assessmentService,
                                  com.oj.platform.service.SupportFeedbackService supportFeedbackService) {
        this.assessmentService = assessmentService;
        this.supportFeedbackService = supportFeedbackService;
    }

    // ── Assessment CRUD ───────────────────────────────────────────────────

    @PostMapping
    public ResponseEntity<AssessmentDto> createAssessment(@Valid @RequestBody CreateAssessmentRequest request,
                                                           @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(assessmentService.createHostedAssessment(request, currentUser.getId()));
    }

    @GetMapping
    public ResponseEntity<List<AssessmentDto>> listMyAssessments(@AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(assessmentService.listOwnAssessments(currentUser.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AssessmentDto> getAssessment(@PathVariable Long id,
                                                        @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(assessmentService.getOwnAssessment(id, currentUser.getId()));
    }

    @GetMapping("/{id}/attempts")
    public ResponseEntity<List<AssessmentAttemptDto>> getAssessmentAttempts(@PathVariable Long id,
                                                                             @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(assessmentService.getHostAssessmentAttempts(id, currentUser.getId()));
    }

    @GetMapping("/{id}/feedback")
    public ResponseEntity<AssessmentHostFeedbackSummaryDto> getAssessmentFeedback(@PathVariable Long id,
                                                                                  @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(supportFeedbackService.getHostAssessmentFeedback(id, currentUser.getId()));
    }

    @GetMapping("/feedback")
    public ResponseEntity<List<AssessmentFeedbackDto>> getAllMyAssessmentFeedback(@AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(supportFeedbackService.getAllHostAssessmentFeedback(currentUser.getId()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AssessmentDto> updateAssessment(@PathVariable Long id,
                                                           @Valid @RequestBody CreateAssessmentRequest request,
                                                           @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(assessmentService.updateOwnAssessment(id, request, currentUser.getId()));
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity<AssessmentDto> publishAssessment(@PathVariable Long id,
                                                            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(assessmentService.publishOwnAssessment(id, currentUser.getId()));
    }

    @PostMapping("/{id}/archive")
    public ResponseEntity<AssessmentDto> archiveAssessment(@PathVariable Long id,
                                                            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(assessmentService.archiveOwnAssessment(id, currentUser.getId()));
    }

    // ── Questions ─────────────────────────────────────────────────────────

    @PostMapping("/{id}/questions")
    public ResponseEntity<AssessmentQuestionDto> addQuestion(@PathVariable Long id,
                                                              @Valid @RequestBody CreateAssessmentQuestionRequest request,
                                                              @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(assessmentService.addOwnQuestion(id, request, currentUser.getId()));
    }

    @PutMapping("/{id}/questions/{questionId}")
    public ResponseEntity<AssessmentQuestionDto> updateQuestion(@PathVariable Long id,
                                                                 @PathVariable Long questionId,
                                                                 @Valid @RequestBody CreateAssessmentQuestionRequest request,
                                                                 @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(assessmentService.updateOwnQuestion(id, questionId, request, currentUser.getId()));
    }

    @DeleteMapping("/{id}/questions/{questionId}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable Long id, @PathVariable Long questionId,
                                                @AuthenticationPrincipal UserPrincipal currentUser) {
        assessmentService.deleteOwnQuestion(id, questionId, currentUser.getId());
        return ResponseEntity.noContent().build();
    }

    // ── Options (MCQ/TRUE_FALSE) ──────────────────────────────────────────

    @PostMapping("/{id}/questions/{questionId}/options")
    public ResponseEntity<AssessmentOptionDto> addOption(@PathVariable Long id,
                                                          @PathVariable Long questionId,
                                                          @Valid @RequestBody CreateAssessmentOptionRequest request,
                                                          @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(assessmentService.addOwnOption(id, questionId, request, currentUser.getId()));
    }

    @PutMapping("/{id}/questions/{questionId}/options/{optionId}")
    public ResponseEntity<AssessmentOptionDto> updateOption(@PathVariable Long id,
                                                             @PathVariable Long questionId,
                                                             @PathVariable Long optionId,
                                                             @Valid @RequestBody CreateAssessmentOptionRequest request,
                                                             @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(assessmentService.updateOwnOption(id, questionId, optionId, request, currentUser.getId()));
    }

    @DeleteMapping("/{id}/questions/{questionId}/options/{optionId}")
    public ResponseEntity<Void> deleteOption(@PathVariable Long id, @PathVariable Long questionId,
                                              @PathVariable Long optionId,
                                              @AuthenticationPrincipal UserPrincipal currentUser) {
        assessmentService.deleteOwnOption(id, questionId, optionId, currentUser.getId());
        return ResponseEntity.noContent().build();
    }

    // ── Test Cases (PROGRAMMING questions only) ───────────────────────────

    @GetMapping("/{id}/questions/{questionId}/test-cases")
    public ResponseEntity<List<ProgrammingTestCaseDto>> getTestCases(@PathVariable Long id,
                                                                       @PathVariable Long questionId,
                                                                       @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(assessmentService.getOwnTestCases(id, questionId, currentUser.getId()));
    }

    @PostMapping("/{id}/questions/{questionId}/test-cases")
    public ResponseEntity<ProgrammingTestCaseDto> addTestCase(@PathVariable Long id,
                                                               @PathVariable Long questionId,
                                                               @RequestBody ProgrammingTestCaseRequest request,
                                                               @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(assessmentService.addOwnTestCase(id, questionId, request, currentUser.getId()));
    }

    @PutMapping("/{id}/questions/{questionId}/test-cases/{testCaseId}")
    public ResponseEntity<ProgrammingTestCaseDto> updateTestCase(@PathVariable Long id,
                                                                  @PathVariable Long questionId,
                                                                  @PathVariable Long testCaseId,
                                                                  @RequestBody ProgrammingTestCaseRequest request,
                                                                  @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(assessmentService.updateOwnTestCase(id, questionId, testCaseId, request, currentUser.getId()));
    }

    @DeleteMapping("/{id}/questions/{questionId}/test-cases/{testCaseId}")
    public ResponseEntity<Void> deleteTestCase(@PathVariable Long id,
                                                @PathVariable Long questionId,
                                                @PathVariable Long testCaseId,
                                                @AuthenticationPrincipal UserPrincipal currentUser) {
        assessmentService.deleteOwnTestCase(id, questionId, testCaseId, currentUser.getId());
        return ResponseEntity.noContent().build();
    }
}
