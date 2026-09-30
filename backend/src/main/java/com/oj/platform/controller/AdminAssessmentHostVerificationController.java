package com.oj.platform.controller;

import com.oj.platform.dto.AdminAssessmentHostVerificationDto;
import com.oj.platform.dto.RejectHostVerificationRequest;
import com.oj.platform.security.UserPrincipal;
import com.oj.platform.service.AssessmentHostVerificationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Admin-only Assessment Host verification review API. Protected the same way as every
 * other admin controller in this codebase (@PreAuthorize("hasRole('ADMIN')") at the
 * class level, matching AdminAssessmentController/AdminContestController) - no new
 * authorization mechanism was introduced (task security rule: "Only the admin should
 * be able to review verification requests").
 */
@RestController
@RequestMapping("/api/admin/assessment-host/verifications")
@PreAuthorize("hasRole('ADMIN')")
public class AdminAssessmentHostVerificationController {

    private final AssessmentHostVerificationService verificationService;

    public AdminAssessmentHostVerificationController(AssessmentHostVerificationService verificationService) {
        this.verificationService = verificationService;
    }

    @GetMapping
    public ResponseEntity<List<AdminAssessmentHostVerificationDto>> listVerifications() {
        return ResponseEntity.ok(verificationService.listAllForAdmin());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdminAssessmentHostVerificationDto> getVerification(@PathVariable Long id) {
        return ResponseEntity.ok(verificationService.getOneForAdmin(id));
    }

    /** Streams the uploaded document back to the admin only - never publicly
     *  reachable, and never served through a static file path (task requirement:
     *  "Do not expose uploaded documents publicly"). */
    @GetMapping("/{id}/document")
    public ResponseEntity<byte[]> downloadDocument(@PathVariable Long id) {
        return streamDocument(id, "primary");
    }

    @GetMapping("/{id}/document/{docType}")
    public ResponseEntity<byte[]> downloadSpecificDocument(@PathVariable Long id, @PathVariable String docType) {
        return streamDocument(id, docType);
    }

    private ResponseEntity<byte[]> streamDocument(Long id, String docType) {
        AssessmentHostVerificationService.DocumentContent content = verificationService.getDocumentForAdmin(id, docType);
        MediaType mediaType;
        try {
            mediaType = content.getContentType() != null
                    ? MediaType.parseMediaType(content.getContentType())
                    : MediaType.APPLICATION_OCTET_STREAM;
        } catch (Exception e) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }
        String safeName = content.getOriginalFileName() != null ? content.getOriginalFileName() : "document";
        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + sanitizeHeaderValue(safeName) + "\"")
                .body(content.getBytes());
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<AdminAssessmentHostVerificationDto> approve(@PathVariable Long id,
                                                                      @AuthenticationPrincipal UserPrincipal currentUser) {
        AssessmentHostVerificationService.ApprovalResult result = verificationService.approve(id, currentUser.getId());
        return ResponseEntity.ok(result.getVerification());
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<AdminAssessmentHostVerificationDto> reject(@PathVariable Long id,
                                                                      @Valid @RequestBody RejectHostVerificationRequest request,
                                                                      @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(verificationService.reject(id, request.getReason(), currentUser.getId()));
    }

    @PostMapping("/{id}/direct-verify")
    public ResponseEntity<AdminAssessmentHostVerificationDto> directVerify(@PathVariable Long id,
                                                                            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(verificationService.directVerify(id, currentUser.getId()));
    }

    private String sanitizeHeaderValue(String value) {
        return value.replaceAll("[\\r\\n\"]", "_");
    }
}
