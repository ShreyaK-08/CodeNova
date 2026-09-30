package com.oj.platform.controller;

import com.oj.platform.dto.CertificateDto;
import com.oj.platform.dto.CertificateProgressDto;
import com.oj.platform.dto.CertificateVerificationDto;
import com.oj.platform.security.UserPrincipal;
import com.oj.platform.service.CertificateService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
public class CertificateController {

    private final CertificateService certificateService;

    public CertificateController(CertificateService certificateService) {
        this.certificateService = certificateService;
    }

    // ---- Student-facing (Part 7/8/10/11) ----

    @GetMapping("/api/certificates")
    public ResponseEntity<List<CertificateDto>> myCertificates(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestParam(required = false) String language) {
        return ResponseEntity.ok(certificateService.getUserCertificates(currentUser.getId(), language));
    }

    @GetMapping("/api/certificates/progress")
    public ResponseEntity<CertificateProgressDto> myProgress(@AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(certificateService.getProgress(currentUser.getId()));
    }

    @GetMapping({"/api/certificates/{id}/pdf", "/api/certificates/{id}/download"})
    public ResponseEntity<?> downloadPdf(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal currentUser) {
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()) || "ADMIN".equals(a.getAuthority()));
        try {
            byte[] pdf = certificateService.generateCertificatePdf(id, currentUser.getId(), isAdmin);
            ByteArrayResource resource = new ByteArrayResource(pdf);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"codenova-certificate-" + id + ".pdf\"")
                    .contentLength(pdf.length)
                    .body(resource);
        } catch (SecurityException se) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(se.getMessage());
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Failed to generate certificate PDF.");
        }
    }

    // ---- Public verification (Part 9) ----

    @GetMapping("/api/certificates/verify/{code}")
    public ResponseEntity<CertificateVerificationDto> verify(@PathVariable String code) {
        return ResponseEntity.ok(certificateService.verify(code));
    }

    // ---- Admin (Part 19) ----

    @GetMapping("/api/admin-certificates")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CertificateDto>> allCertificates() {
        return ResponseEntity.ok(certificateService.getAllCertificatesForAdmin());
    }
}
