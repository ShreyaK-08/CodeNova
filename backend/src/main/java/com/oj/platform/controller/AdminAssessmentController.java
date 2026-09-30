package com.oj.platform.controller;

import com.oj.platform.dto.AssessmentDto;
import com.oj.platform.dto.AssessmentOptionDto;
import com.oj.platform.dto.AssessmentQuestionDto;
import com.oj.platform.dto.CreateAssessmentOptionRequest;
import com.oj.platform.dto.CreateAssessmentQuestionRequest;
import com.oj.platform.dto.CreateAssessmentRequest;
import com.oj.platform.security.UserPrincipal;
import com.oj.platform.service.AssessmentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin-facing skill assessment authoring API (Task 10) - create/update/publish/
 * archive assessments and manage their questions/options. Protected by
 * @PreAuthorize("hasRole('ADMIN')") at the class level, the same pattern already used
 * by AdminContestController - no new role system was added, and students cannot reach
 * any endpoint here (security rule 3).
 */
@RestController
@RequestMapping("/api/admin/assessments")
@PreAuthorize("hasRole('ADMIN')")
public class AdminAssessmentController {

    private final AssessmentService assessmentService;

    public AdminAssessmentController(AssessmentService assessmentService) {
        this.assessmentService = assessmentService;
    }

    @GetMapping
    public ResponseEntity<List<AssessmentDto>> listAllAssessments() {
        return ResponseEntity.ok(assessmentService.listAssessments());
    }

    @PostMapping
    public ResponseEntity<AssessmentDto> createAssessment(@Valid @RequestBody CreateAssessmentRequest request,
                                                           @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(assessmentService.createAssessment(request, currentUser.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AssessmentDto> getAssessment(@PathVariable Long id) {
        return ResponseEntity.ok(assessmentService.getAssessmentAdmin(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AssessmentDto> updateAssessment(@PathVariable Long id,
                                                          @Valid @RequestBody CreateAssessmentRequest request) {
        return ResponseEntity.ok(assessmentService.updateAssessment(id, request));
    }

    @PostMapping("/{id}/publish")
    public ResponseEntity<AssessmentDto> publishAssessment(@PathVariable Long id) {
        return ResponseEntity.ok(assessmentService.publishAssessment(id));
    }

    @PostMapping("/{id}/archive")
    public ResponseEntity<AssessmentDto> archiveAssessment(@PathVariable Long id) {
        return ResponseEntity.ok(assessmentService.archiveAssessment(id));
    }

    @PostMapping("/{id}/questions")
    public ResponseEntity<AssessmentQuestionDto> addQuestion(@PathVariable Long id,
                                                              @Valid @RequestBody CreateAssessmentQuestionRequest request) {
        return ResponseEntity.ok(assessmentService.addQuestion(id, request));
    }

    @PutMapping("/{id}/questions/{questionId}")
    public ResponseEntity<AssessmentQuestionDto> updateQuestion(@PathVariable Long id,
                                                                 @PathVariable Long questionId,
                                                                 @Valid @RequestBody CreateAssessmentQuestionRequest request) {
        return ResponseEntity.ok(assessmentService.updateQuestion(id, questionId, request));
    }

    @DeleteMapping("/{id}/questions/{questionId}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable Long id, @PathVariable Long questionId) {
        assessmentService.deleteQuestion(id, questionId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/questions/{questionId}/options")
    public ResponseEntity<AssessmentOptionDto> addOption(@PathVariable Long id,
                                                          @PathVariable Long questionId,
                                                          @Valid @RequestBody CreateAssessmentOptionRequest request) {
        return ResponseEntity.ok(assessmentService.addOption(id, questionId, request));
    }

    @PutMapping("/{id}/questions/{questionId}/options/{optionId}")
    public ResponseEntity<AssessmentOptionDto> updateOption(@PathVariable Long id,
                                                            @PathVariable Long questionId,
                                                            @PathVariable Long optionId,
                                                            @Valid @RequestBody CreateAssessmentOptionRequest request) {
        return ResponseEntity.ok(assessmentService.updateOption(id, questionId, optionId, request));
    }

    @DeleteMapping("/{id}/questions/{questionId}/options/{optionId}")
    public ResponseEntity<Void> deleteOption(@PathVariable Long id, @PathVariable Long questionId, @PathVariable Long optionId) {
        assessmentService.deleteOption(id, questionId, optionId);
        return ResponseEntity.noContent().build();
    }
}
