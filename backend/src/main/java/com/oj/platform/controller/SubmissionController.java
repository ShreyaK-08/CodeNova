package com.oj.platform.controller;

import com.oj.platform.dto.RunResponse;
import com.oj.platform.dto.SubmissionRequest;
import com.oj.platform.dto.SubmissionResponse;
import com.oj.platform.security.UserPrincipal;
import com.oj.platform.service.SubmissionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/submissions")
public class SubmissionController {

    private final SubmissionService submissionService;

    public SubmissionController(SubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    @PostMapping
    public ResponseEntity<SubmissionResponse> submitSolution(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody SubmissionRequest request) {
        SubmissionResponse response = submissionService.submitSolution(currentUser.getId(), request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * "Run" - executes code against the problem's visible/sample test cases only.
     * Does NOT create a submission record; purely lets the student sanity-check
     * their code before using Submit.
     */
    @PostMapping("/run")
    public ResponseEntity<RunResponse> runCode(@Valid @RequestBody SubmissionRequest request) {
        RunResponse response = submissionService.runCode(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my")
    public ResponseEntity<List<SubmissionResponse>> getMySubmissions(@AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(submissionService.getUserSubmissions(currentUser.getId()));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SubmissionResponse>> getAllSubmissions() {
        return ResponseEntity.ok(submissionService.getAllSubmissions());
    }
}
