package com.oj.platform.controller;

import com.oj.platform.dto.AssessmentQuestionFeedbackDto;
import com.oj.platform.dto.CreateAssessmentQuestionFeedbackRequest;
import com.oj.platform.dto.CreateGeneralFeedbackRequest;
import com.oj.platform.dto.GeneralFeedbackDto;
import com.oj.platform.security.UserPrincipal;
import com.oj.platform.service.SupportFeedbackService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class FeedbackController {

    private final SupportFeedbackService supportFeedbackService;

    public FeedbackController(SupportFeedbackService supportFeedbackService) {
        this.supportFeedbackService = supportFeedbackService;
    }

    @PostMapping("/feedback")
    public ResponseEntity<GeneralFeedbackDto> createGeneralFeedback(
            @Valid @RequestBody CreateGeneralFeedbackRequest req,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(supportFeedbackService.createGeneralFeedback(req, currentUser.getId()));
    }

    @PostMapping("/assessment-feedback")
    public ResponseEntity<AssessmentQuestionFeedbackDto> createAssessmentQuestionFeedback(
            @Valid @RequestBody CreateAssessmentQuestionFeedbackRequest req,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(supportFeedbackService.createAssessmentQuestionFeedback(req, currentUser.getId()));
    }
}
