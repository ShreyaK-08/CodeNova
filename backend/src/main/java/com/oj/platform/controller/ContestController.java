package com.oj.platform.controller;

import com.oj.platform.dto.ContestAttemptDto;
import com.oj.platform.dto.ContestDto;
import com.oj.platform.dto.ContestLeaderboardEntryDto;
import com.oj.platform.dto.ContestRegistrationDto;
import com.oj.platform.dto.ContestSecurityViolationDto;
import com.oj.platform.security.UserPrincipal;
import com.oj.platform.service.ContestService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * User-facing contest browsing/registration/participation. Falls under the existing
 * ".anyRequest().authenticated()" rule in SecurityConfig (no admin role required) -
 * no new security rule was needed. Contest-specific scoring/ranking (Task 8) and basic
 * exam-security violation reporting (Task 9) are added here alongside the existing
 * browse/register endpoints (Task 6).
 */
@RestController
@RequestMapping("/api/contests")
public class ContestController {

    private final ContestService contestService;

    public ContestController(ContestService contestService) {
        this.contestService = contestService;
    }

    @GetMapping
    public ResponseEntity<List<ContestDto>> listAvailableContests() {
        return ResponseEntity.ok(contestService.listAvailableContests());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContestDto> getContest(@PathVariable Long id) {
        return ResponseEntity.ok(contestService.getContest(id));
    }

    @PostMapping("/{id}/register")
    public ResponseEntity<ContestRegistrationDto> register(@PathVariable Long id,
                                                             @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(contestService.register(id, currentUser.getId()));
    }

    @GetMapping("/{id}/registration")
    public ResponseEntity<ContestRegistrationDto> getRegistrationStatus(@PathVariable Long id,
                                                                         @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(contestService.getRegistrationStatus(id, currentUser.getId()));
    }

    /**
     * Get-or-create the current user's ContestAttempt (Task 8, Step 3). Idempotent -
     * the contest coding screen calls this both on entry and to refresh score/solved/
     * submission counts after a Run/Submit, and it never creates a second attempt.
     * The current user is always taken from the JWT-backed principal, never a
     * client-supplied id.
     */
    @PostMapping("/{id}/attempt")
    public ResponseEntity<ContestAttemptDto> startOrGetAttempt(@PathVariable Long id,
                                                                 @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(contestService.getOrCreateAttempt(id, currentUser.getId()));
    }

    /**
     * Public per-contest leaderboard (Task 8, Step 10/11) - scoped to this one contest
     * only, never the separate global platform leaderboard.
     */
    @GetMapping("/{id}/leaderboard")
    public ResponseEntity<List<ContestLeaderboardEntryDto>> getLeaderboard(@PathVariable Long id) {
        return ResponseEntity.ok(contestService.getLeaderboard(id));
    }

    /**
     * Records one fullscreen-exit or tab-switch violation against the current user's
     * ContestAttempt (Task 9). Basic browser-based exam-security monitoring only - see
     * README "Contest Exam Security" for the honest scope of what this can and can't
     * detect. The user is always taken from the JWT principal, never a client-supplied id.
     */
    @PostMapping("/{id}/security-violation")
    public ResponseEntity<ContestSecurityViolationDto> recordSecurityViolation(
            @PathVariable Long id,
            @RequestBody(required = false) java.util.Map<String, String> payload,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        String violationType = (payload != null) ? payload.get("violationType") : null;
        return ResponseEntity.ok(contestService.recordSecurityViolation(id, currentUser.getId(), violationType));
    }

    @PostMapping("/{id}/finish")
    public ResponseEntity<ContestAttemptDto> finishAttempt(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(contestService.finishContestAttempt(id, currentUser.getId()));
    }
}

