package com.oj.platform.controller;

import com.oj.platform.dto.ProblemDto;
import com.oj.platform.service.ProblemService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/problems")
public class ProblemController {

    private final ProblemService problemService;
    private final com.oj.platform.service.HintEditorialService hintEditorialService;

    public ProblemController(ProblemService problemService, com.oj.platform.service.HintEditorialService hintEditorialService) {
        this.problemService = problemService;
        this.hintEditorialService = hintEditorialService;
    }

    @GetMapping
    public ResponseEntity<List<ProblemDto>> getAllProblems() {
        return ResponseEntity.ok(problemService.getAllProblems());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProblemDto> getProblemById(@PathVariable Long id) {
        return ResponseEntity.ok(problemService.getProblemById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProblemDto> createProblem(@Valid @RequestBody ProblemDto problemDto) {
        ProblemDto created = problemService.createProblem(problemDto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProblemDto> updateProblem(@PathVariable Long id, @Valid @RequestBody ProblemDto problemDto) {
        ProblemDto updated = problemService.updateProblem(id, problemDto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteProblem(@PathVariable Long id) {
        problemService.deleteProblem(id);
        return ResponseEntity.ok(Map.of("message", "Problem deleted successfully"));
    }

    @GetMapping("/{id}/hints")
    public ResponseEntity<List<com.oj.platform.dto.HintDto>> getHints(
            @PathVariable Long id,
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.oj.platform.security.UserPrincipal currentUser) {
        boolean isAdmin = currentUser != null && currentUser.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()) || "ADMIN".equals(a.getAuthority()));
        Long userId = currentUser != null ? currentUser.getId() : null;
        return ResponseEntity.ok(hintEditorialService.getHintsForProblem(id, userId, isAdmin));
    }

    @GetMapping("/{id}/editorial")
    public ResponseEntity<com.oj.platform.dto.EditorialDto> getEditorial(
            @PathVariable Long id,
            @org.springframework.security.core.annotation.AuthenticationPrincipal com.oj.platform.security.UserPrincipal currentUser) {
        boolean isAdmin = currentUser != null && currentUser.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()) || "ADMIN".equals(a.getAuthority()));
        Long userId = currentUser != null ? currentUser.getId() : null;
        return ResponseEntity.ok(hintEditorialService.getEditorialForProblem(id, userId, isAdmin));
    }

    @PostMapping("/{id}/hints")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<com.oj.platform.dto.HintDto> createHint(
            @PathVariable Long id,
            @RequestBody com.oj.platform.dto.HintDto dto) {
        return new ResponseEntity<>(hintEditorialService.createHint(id, dto), HttpStatus.CREATED);
    }

    @PutMapping("/hints/{hintId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<com.oj.platform.dto.HintDto> updateHint(
            @PathVariable Long hintId,
            @RequestBody com.oj.platform.dto.HintDto dto) {
        return ResponseEntity.ok(hintEditorialService.updateHint(hintId, dto));
    }

    @DeleteMapping("/hints/{hintId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteHint(@PathVariable Long hintId) {
        hintEditorialService.deleteHint(hintId);
        return ResponseEntity.ok(Map.of("message", "Hint deleted successfully"));
    }
}
