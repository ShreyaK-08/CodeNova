package com.oj.platform.controller;

import com.oj.platform.dto.ContestDto;
import com.oj.platform.dto.ContestParticipantAdminDto;
import com.oj.platform.dto.ContestRequest;
import com.oj.platform.security.UserPrincipal;
import com.oj.platform.service.ContestService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin-only contest management. Protected by the existing "/api/admin/**" ->
 * hasRole("ADMIN") rule already in SecurityConfig - no new security rule was needed.
 */
@RestController
@RequestMapping("/api/admin/contests")
@PreAuthorize("hasRole('ADMIN')")
public class AdminContestController {

    private final ContestService contestService;

    public AdminContestController(ContestService contestService) {
        this.contestService = contestService;
    }

    @PostMapping
    public ResponseEntity<ContestDto> createContest(@Valid @RequestBody ContestRequest request,
                                                      @AuthenticationPrincipal UserPrincipal currentUser) {
        ContestDto created = contestService.createContest(request, currentUser != null ? currentUser.getId() : null);
        return ResponseEntity.ok(created);
    }

    @GetMapping
    public ResponseEntity<List<ContestDto>> listContests() {
        return ResponseEntity.ok(contestService.listAllContestsForAdmin());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContestDto> getContest(@PathVariable Long id) {
        return ResponseEntity.ok(contestService.getContest(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ContestDto> updateContest(@PathVariable Long id, @Valid @RequestBody ContestRequest request) {
        synchronized (("contest-update:" + id).intern()) {
            return ResponseEntity.ok(contestService.updateContest(id, request));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContest(@PathVariable Long id) {
        contestService.deleteContest(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/broadcast")
    public ResponseEntity<Void> broadcastAnnouncement(@PathVariable Long id) {
        contestService.broadcastAnnouncement(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Admin results for one contest (Task 8, Step 12) - participant, score, solved
     * count, submission count, attempt status, and completion time. Protected by the
     * class-level @PreAuthorize("hasRole('ADMIN')") already on this controller - no new
     * role system was added.
     */
    @GetMapping("/{id}/participants")
    public ResponseEntity<List<ContestParticipantAdminDto>> getParticipants(@PathVariable Long id) {
        return ResponseEntity.ok(contestService.getAdminParticipants(id));
    }
}
