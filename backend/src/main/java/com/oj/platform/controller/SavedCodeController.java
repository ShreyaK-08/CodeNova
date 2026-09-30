package com.oj.platform.controller;

import com.oj.platform.dto.SavedCodeDto;
import com.oj.platform.dto.SavedCodeRequest;
import com.oj.platform.security.UserPrincipal;
import com.oj.platform.service.SavedCodeService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Persistent "last saved code" per problem/language (separate from Submission history).
 * Registered under /api/problems/{problemId}/saved-code as a dedicated controller rather
 * than added to ProblemController, to keep this change small and isolated.
 */
@RestController
@RequestMapping("/api/problems/{problemId}/saved-code")
public class SavedCodeController {

    private final SavedCodeService savedCodeService;

    public SavedCodeController(SavedCodeService savedCodeService) {
        this.savedCodeService = savedCodeService;
    }

    @GetMapping
    public ResponseEntity<SavedCodeDto> getSavedCode(
            @PathVariable Long problemId,
            @RequestParam String language,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return savedCodeService.getSavedCode(currentUser.getId(), problemId, language)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PutMapping
    public ResponseEntity<SavedCodeDto> saveCode(
            @PathVariable Long problemId,
            @Valid @RequestBody SavedCodeRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        SavedCodeDto dto = savedCodeService.saveOrUpdate(
                currentUser.getId(), problemId, request.getLanguage(), request.getCode());
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    public ResponseEntity<SavedCodeDto> saveCodePost(
            @PathVariable Long problemId,
            @Valid @RequestBody SavedCodeRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return saveCode(problemId, request, currentUser);
    }
}
