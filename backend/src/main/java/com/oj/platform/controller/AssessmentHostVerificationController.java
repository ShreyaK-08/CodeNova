package com.oj.platform.controller;

import com.oj.platform.dto.AssessmentHostVerificationDto;
import com.oj.platform.dto.SubmitHostVerificationCodeRequest;
import com.oj.platform.security.UserPrincipal;
import com.oj.platform.service.AssessmentHostVerificationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * User-facing Assessment Host verification API. Every method derives the acting user
 * from the JWT-authenticated principal - never from a client-supplied userId - so a
 * user can only ever submit, view or verify their OWN request (task security rules:
 * "Users can see only their own verification status", "Never trust a user-supplied
 * userId for ownership").
 */
@RestController
@RequestMapping("/api/assessment-host/verification")
public class AssessmentHostVerificationController {

    private final AssessmentHostVerificationService verificationService;

    public AssessmentHostVerificationController(AssessmentHostVerificationService verificationService) {
        this.verificationService = verificationService;
    }

    @PostMapping(consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AssessmentHostVerificationDto> submitVerification(
            @ModelAttribute com.oj.platform.dto.SubmitHostBusinessVerificationRequest request,
            @RequestParam(value = "document", required = false) MultipartFile document,
            @RequestParam(value = "primaryDocument", required = false) MultipartFile primaryDocument,
            @RequestParam(value = "gstCertificate", required = false) MultipartFile gstCertificate,
            @RequestParam(value = "authorizationLetter", required = false) MultipartFile authorizationLetter,
            @RequestParam(value = "supportingDocument", required = false) MultipartFile supportingDocument,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        MultipartFile effectivePrimary = primaryDocument != null ? primaryDocument : document;
        if (request != null && request.getOrganizationName() != null && !request.getOrganizationName().trim().isEmpty()) {
            return ResponseEntity.ok(verificationService.submitBusinessVerification(
                    currentUser.getId(), request, effectivePrimary, gstCertificate, authorizationLetter, supportingDocument));
        }
        return ResponseEntity.ok(verificationService.submitVerification(currentUser.getId(), effectivePrimary));
    }

    @GetMapping
    public ResponseEntity<AssessmentHostVerificationDto> getMyVerificationStatus(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(verificationService.getMyVerificationStatus(currentUser.getId()));
    }

    @PostMapping("/verify-code")
    public ResponseEntity<AssessmentHostVerificationDto> verifyCode(
            @Valid @RequestBody SubmitHostVerificationCodeRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(verificationService.verifyCode(currentUser.getId(), request.getCode()));
    }
}
