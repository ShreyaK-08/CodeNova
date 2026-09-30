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
 * Student-facing assessment API: browse published assessments, start/resume
 * attempts, record MCQ answers, run/submit programming code, record violations,
 * save candidate details, and submit the attempt.
 *
 * All user identity comes from JWT principal — never from request body.
 * Score is always computed server-side.
 */
@RestController
@RequestMapping("/api/assessments")
public class AssessmentController {

    private final AssessmentService assessmentService;
    private final com.oj.platform.service.SupportFeedbackService supportFeedbackService;

    public AssessmentController(AssessmentService assessmentService,
                                com.oj.platform.service.SupportFeedbackService supportFeedbackService) {
        this.assessmentService = assessmentService;
        this.supportFeedbackService = supportFeedbackService;
    }

    @GetMapping
    public ResponseEntity<List<AssessmentDto>> listPublishedAssessments() {
        return ResponseEntity.ok(assessmentService.listPublishedAssessments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AssessmentDto> getAssessment(@PathVariable Long id) {
        return ResponseEntity.ok(assessmentService.getAssessment(id));
    }

    /**
     * Starts a new attempt, or resumes the caller's own existing IN_PROGRESS one.
     * Multi-attempt: enforces maxAttempts limit server-side.
     * Synchronized to prevent duplicate attempt creation under concurrent requests.
     */
    @PostMapping("/{id}/attempt")
    public ResponseEntity<AssessmentAttemptDto> startAttempt(@PathVariable Long id,
                                                              @AuthenticationPrincipal UserPrincipal currentUser) {
        synchronized (("assessment-attempt:" + id + ":" + currentUser.getId()).intern()) {
            return ResponseEntity.ok(assessmentService.startAttempt(id, currentUser.getId()));
        }
    }

    @GetMapping("/{id}/attempt/{attemptId}")
    public ResponseEntity<AssessmentAttemptDto> getAttempt(@PathVariable Long id,
                                                            @PathVariable Long attemptId,
                                                            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(assessmentService.getAttempt(id, attemptId, currentUser.getId()));
    }

    /** Record a MCQ or TRUE_FALSE answer. */
    @PostMapping("/{id}/attempt/{attemptId}/answers")
    public ResponseEntity<AssessmentAnswerDto> recordAnswer(@PathVariable Long id,
                                                             @PathVariable Long attemptId,
                                                             @Valid @RequestBody SubmitAssessmentAnswerRequest request,
                                                             @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(assessmentService.recordAnswer(id, attemptId, currentUser.getId(), request));
    }

    /**
     * Run code against custom input (submitForGrading=false) or all test cases (submitForGrading=true).
     * Hidden test case inputs/outputs are NEVER returned — only pass/fail counts.
     */
    @PostMapping("/{id}/attempt/{attemptId}/code")
    public ResponseEntity<AssessmentCodeRunResponse> runOrSubmitCode(@PathVariable Long id,
                                                                      @PathVariable Long attemptId,
                                                                      @RequestBody AssessmentCodeRunRequest request,
                                                                      @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(assessmentService.runOrSubmitCode(id, attemptId, currentUser.getId(), request));
    }

    /** Record a proctoring violation. Returns 204 so the frontend fire-and-forgets. */
    @PostMapping("/{id}/attempt/{attemptId}/violations")
    public ResponseEntity<Void> recordViolation(@PathVariable Long id,
                                                 @PathVariable Long attemptId,
                                                 @RequestBody AssessmentViolationRequest request,
                                                 @AuthenticationPrincipal UserPrincipal currentUser) {
        assessmentService.recordViolation(id, attemptId, currentUser.getId(), request);
        return ResponseEntity.noContent().build();
    }

    /** Save/update candidate details for this attempt. */
    @PostMapping("/{id}/attempt/{attemptId}/candidate-details")
    public ResponseEntity<Void> saveCandidateDetails(@PathVariable Long id,
                                                      @PathVariable Long attemptId,
                                                      @RequestBody CandidateDetailsRequest request,
                                                      @AuthenticationPrincipal UserPrincipal currentUser) {
        assessmentService.saveCandidateDetails(id, attemptId, currentUser.getId(), request);
        return ResponseEntity.noContent().build();
    }

    /** Grade all answers and mark attempt COMPLETED. Score is always server-computed. */
    @PostMapping("/{id}/attempt/{attemptId}/submit")
    public ResponseEntity<AssessmentAttemptDto> submitAttempt(@PathVariable Long id,
                                                               @PathVariable Long attemptId,
                                                               @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(assessmentService.submitAttempt(id, attemptId, currentUser.getId()));
    }

    /** Submit post-completion feedback for an assessment. */
    @PostMapping("/{id}/feedback")
    public ResponseEntity<AssessmentFeedbackDto> submitAssessmentFeedback(@PathVariable Long id,
                                                                          @Valid @RequestBody CreateAssessmentFeedbackRequest request,
                                                                          @AuthenticationPrincipal UserPrincipal currentUser) {
        request.setAssessmentId(id);
        return ResponseEntity.ok(supportFeedbackService.submitAssessmentFeedback(request, currentUser.getId()));
    }

    /** Get feedback for a specific attempt if previously submitted. */
    @GetMapping("/{id}/attempt/{attemptId}/feedback")
    public ResponseEntity<AssessmentFeedbackDto> getAttemptFeedback(@PathVariable Long id,
                                                                    @PathVariable Long attemptId,
                                                                    @AuthenticationPrincipal UserPrincipal currentUser) {
        return supportFeedbackService.getFeedbackForAttempt(attemptId, currentUser.getId())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
}
