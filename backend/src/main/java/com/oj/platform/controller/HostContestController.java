package com.oj.platform.controller;

import com.oj.platform.dto.ContestAttemptDto;
import com.oj.platform.dto.ContestDto;
import com.oj.platform.dto.ContestLeaderboardEntryDto;
import com.oj.platform.entity.Contest;
import com.oj.platform.entity.ContestAttempt;
import com.oj.platform.exception.ForbiddenException;
import com.oj.platform.exception.ResourceNotFoundException;
import com.oj.platform.repository.ContestAttemptRepository;
import com.oj.platform.repository.ContestRepository;
import com.oj.platform.security.UserPrincipal;
import com.oj.platform.service.ContestService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Host-facing contest results API — verified hosts can view results ONLY for
 * contests they created. Any attempt to access another host's contest results
 * returns 403 FORBIDDEN from the service layer.
 *
 * Routes:
 *   GET /api/contests/host/{id}/results           — all participant results
 *   GET /api/contests/host/{id}/results/export    — CSV download
 *
 * Security: ownership enforced inside every method via assertOwner().
 */
@RestController
@RequestMapping("/api/contests/host")
public class HostContestController {

    private final ContestRepository contestRepository;
    private final ContestAttemptRepository contestAttemptRepository;
    private final ContestService contestService;

    public HostContestController(ContestRepository contestRepository,
                                  ContestAttemptRepository contestAttemptRepository,
                                  ContestService contestService) {
        this.contestRepository = contestRepository;
        this.contestAttemptRepository = contestAttemptRepository;
        this.contestService = contestService;
    }

    /** Returns all participant results (leaderboard data) for the host's own contest. */
    @GetMapping("/{id}/results")
    public ResponseEntity<List<ContestLeaderboardEntryDto>> getContestResults(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        assertOwner(id, currentUser.getId());
        return ResponseEntity.ok(contestService.getLeaderboard(id));
    }

    /** Returns a list of my own hosted contests with summary info. */
    @GetMapping
    public ResponseEntity<List<ContestDto>> getMyContests(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(contestService.listContestsByCreator(currentUser.getId()));
    }

    /** Returns the full attempt detail for a single participant in this contest. */
    @GetMapping("/{id}/results/{participantId}")
    public ResponseEntity<ContestAttemptDto> getParticipantResult(
            @PathVariable Long id,
            @PathVariable Long participantId,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        assertOwner(id, currentUser.getId());
        ContestAttempt attempt = contestAttemptRepository
                .findByParticipantIdAndContestId(participantId, id)
                .orElseThrow(() -> new ResourceNotFoundException("No attempt found for this participant in this contest."));

        return ResponseEntity.ok(toAttemptDto(attempt));
    }

    /** CSV export of all participant results for this contest. */
    @GetMapping("/{id}/results/export")
    public ResponseEntity<byte[]> exportCsv(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        assertOwner(id, currentUser.getId());

        Contest contest = loadContest(id);
        List<ContestLeaderboardEntryDto> entries = contestService.getLeaderboard(id);

        StringBuilder csv = new StringBuilder();
        csv.append("Rank,Username,Name,Score,Problems Solved,Submission Count\n");

        for (ContestLeaderboardEntryDto entry : entries) {
            csv.append(entry.getRank()).append(',')
               .append(escapeCsv(entry.getUsername())).append(',')
               .append(escapeCsv(entry.getName())).append(',')
               .append(entry.getScore()).append(',')
               .append(entry.getSolvedCount()).append(',')
               .append(entry.getSubmissionCount()).append('\n');
        }

        byte[] bytes = csv.toString().getBytes(StandardCharsets.UTF_8);
        String filename = "contest-" + id + "-results.csv";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(bytes);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────────

    private void assertOwner(Long contestId, Long userId) {
        Contest contest = loadContest(contestId);
        if (contest.getCreatedBy() == null || !contest.getCreatedBy().getId().equals(userId)) {
            throw new ForbiddenException("You do not have permission to view results for this contest.");
        }
    }

    private Contest loadContest(Long contestId) {
        return contestRepository.findById(contestId)
                .orElseThrow(() -> new ResourceNotFoundException("Contest not found: " + contestId));
    }

    private ContestAttemptDto toAttemptDto(ContestAttempt attempt) {
        ContestAttemptDto dto = new ContestAttemptDto();
        dto.setContestId(attempt.getContest().getId());
        dto.setScore(attempt.getScore());
        dto.setProblemsSolved(attempt.getProblemsSolved());
        dto.setSubmissionCount(attempt.getSubmissionCount());
        dto.setStatus(attempt.getStatus().name());
        dto.setStartedAt(attempt.getStartedAt());
        dto.setCompletedAt(attempt.getCompletedAt());
        dto.setSecurityViolationCount(attempt.getSecurityViolationCount());
        return dto;
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
