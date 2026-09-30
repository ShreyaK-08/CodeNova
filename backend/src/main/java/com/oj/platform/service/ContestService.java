package com.oj.platform.service;

import com.oj.platform.dto.*;
import com.oj.platform.entity.*;
import com.oj.platform.exception.BadRequestException;
import com.oj.platform.exception.ResourceNotFoundException;
import com.oj.platform.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Contest management (Task 4): create/list/get/update/delete contests built from
 * EXISTING Problem records, plus user registration. Task 8 extends this same service
 * with real contest participation: starting/reusing a ContestAttempt, server-side
 * contest-submission time enforcement, score/solved-count recalculation, and the
 * contest leaderboard/admin-results queries. Task 9 adds basic exam-security violation
 * tracking (fullscreen-exit / tab-switch) against the same ContestAttempt, with a
 * configurable termination threshold. This is browser-based monitoring only - it is
 * NOT a guaranteed anti-cheat mechanism (see README).
 */
@Service
public class ContestService {

    private final ContestRepository contestRepository;
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ContestService.class);
    private final ContestProblemRepository contestProblemRepository;
    private final ContestRegistrationRepository contestRegistrationRepository;
    private final ProblemRepository problemRepository;
    private final UserRepository userRepository;
    private final ContestAttemptRepository contestAttemptRepository;
    private final SubmissionRepository submissionRepository;
    private final EmailService emailService;
    private final ContestAnnouncementNotificationRepository announcementNotificationRepository;

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ContestService.class);

    /** Configurable (Task 9, Step 6) - not hardcoded. See application.properties. */
    @Value("${contest.security.max-violations:3}")
    private int maxViolations = 3;

    public ContestService(ContestRepository contestRepository,
                           ContestProblemRepository contestProblemRepository,
                           ContestRegistrationRepository contestRegistrationRepository,
                           ProblemRepository problemRepository,
                           UserRepository userRepository,
                           ContestAttemptRepository contestAttemptRepository,
                           SubmissionRepository submissionRepository) {
        this(contestRepository, contestProblemRepository, contestRegistrationRepository,
                problemRepository, userRepository, contestAttemptRepository, submissionRepository, null, null);
    }

    public ContestService(ContestRepository contestRepository,
                           ContestProblemRepository contestProblemRepository,
                           ContestRegistrationRepository contestRegistrationRepository,
                           ProblemRepository problemRepository,
                           UserRepository userRepository,
                           ContestAttemptRepository contestAttemptRepository,
                           SubmissionRepository submissionRepository,
                           EmailService emailService) {
        this(contestRepository, contestProblemRepository, contestRegistrationRepository,
                problemRepository, userRepository, contestAttemptRepository, submissionRepository, emailService, null);
    }

    @Autowired
    public ContestService(ContestRepository contestRepository,
                           ContestProblemRepository contestProblemRepository,
                           ContestRegistrationRepository contestRegistrationRepository,
                           ProblemRepository problemRepository,
                           UserRepository userRepository,
                           ContestAttemptRepository contestAttemptRepository,
                           SubmissionRepository submissionRepository,
                           @Autowired(required = false) EmailService emailService,
                           @Autowired(required = false) ContestAnnouncementNotificationRepository announcementNotificationRepository) {
        this.contestRepository = contestRepository;
        this.contestProblemRepository = contestProblemRepository;
        this.contestRegistrationRepository = contestRegistrationRepository;
        this.problemRepository = problemRepository;
        this.userRepository = userRepository;
        this.contestAttemptRepository = contestAttemptRepository;
        this.submissionRepository = submissionRepository;
        this.emailService = emailService;
        this.announcementNotificationRepository = announcementNotificationRepository;
    }

    // =====================================================================
    // ADMIN
    // =====================================================================

    @Transactional
    public ContestDto createContest(ContestRequest request, Long adminUserId) {
        validateTimes(request);

        User admin = adminUserId != null
                ? userRepository.findById(adminUserId)
                        .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + adminUserId))
                : null;

        Contest contest = new Contest(
                request.getTitle(),
                request.getOrganizationName(),
                request.getDescription(),
                request.getStartTime(),
                request.getEndTime(),
                admin
        );
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            contest.setStatus(parseStatus(request.getStatus()));
        }

        Contest saved = contestRepository.save(contest);
        applyContestProblems(saved, request.getProblems());

        if (saved.getStatus() == ContestStatus.PUBLISHED && !saved.isAnnouncementSent()) {
            broadcastContestAnnouncement(saved);
        }

        return toDto(saved);
    }

    @Transactional
    public ContestDto updateContest(Long contestId, ContestRequest request) {
        validateTimes(request);

        Contest contest = contestRepository.findById(contestId)
                .orElseThrow(() -> new ResourceNotFoundException("Contest not found with id: " + contestId));

        ContestStatus oldStatus = contest.getStatus();
        contest.setTitle(request.getTitle());
        contest.setOrganizationName(request.getOrganizationName());
        contest.setDescription(request.getDescription());
        contest.setStartTime(request.getStartTime());
        contest.setEndTime(request.getEndTime());
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            contest.setStatus(parseStatus(request.getStatus()));
        }

        Contest saved = contestRepository.save(contest);

        if (request.getProblems() != null) {
            reconcileContestProblems(saved, request.getProblems());
        }

        if (saved.getStatus() == ContestStatus.PUBLISHED && !saved.isAnnouncementSent()) {
            broadcastContestAnnouncement(saved);
        }

        return toDto(saved);
    }

    @Transactional
    public void broadcastAnnouncement(Long contestId) {
        Contest contest = contestRepository.findById(contestId)
                .orElseThrow(() -> new ResourceNotFoundException("Contest not found with id: " + contestId));
        broadcastContestAnnouncement(contest);
    }

    void broadcastContestAnnouncement(Contest contest) {
        if (emailService == null || contest == null) return;
        final Long contestId = contest.getId();

        java.util.concurrent.CompletableFuture.runAsync(() -> {
            try {
                List<User> users = userRepository.findAll();
                java.util.Set<String> processedEmails = new java.util.HashSet<>();
                int sentCount = 0;
                int failedCount = 0;

                for (User u : users) {
                    if (u.getEmail() == null || u.getEmail().isBlank()) {
                        continue;
                    }
                    String normalizedEmail = u.getEmail().trim().toLowerCase();
                    if (!normalizedEmail.contains("@") || !normalizedEmail.contains(".")) {
                        continue;
                    }
                    if (!processedEmails.add(normalizedEmail)) {
                        continue;
                    }
                    if (announcementNotificationRepository != null && contestId != null &&
                            announcementNotificationRepository.existsByContestIdAndRecipientEmailIgnoreCase(contestId, normalizedEmail)) {
                        continue;
                    }

                    try {
                        EmailService.EmailResult result = emailService.sendContestAnnouncementEmail(u, contest);
                        boolean sent = result != null && result.isSent();
                        if (sent) {
                            sentCount++;
                        } else {
                            failedCount++;
                        }

                        if (announcementNotificationRepository != null && contestId != null) {
                            announcementNotificationRepository.save(new ContestAnnouncementNotification(
                                    contestId,
                                    u.getId(),
                                    normalizedEmail,
                                    LocalDateTime.now(),
                                    sent ? "SENT" : "FAILED",
                                    sent ? null : (result != null ? result.getMessage() : "Delivery failed")
                            ));
                        }
                    } catch (Exception ex) {
                        failedCount++;
                        logger.error("[Contest Announcement] Failed sending to {}: {}", normalizedEmail, ex.getMessage());
                        if (announcementNotificationRepository != null && contestId != null) {
                            announcementNotificationRepository.save(new ContestAnnouncementNotification(
                                    contestId,
                                    u.getId(),
                                    normalizedEmail,
                                    LocalDateTime.now(),
                                    "FAILED",
                                    ex.getMessage()
                            ));
                        }
                    }
                }

                if (contestId != null) {
                    contestRepository.findById(contestId).ifPresent(c -> {
                        c.setAnnouncementSent(true);
                        c.setAnnouncementSentAt(LocalDateTime.now());
                        contestRepository.save(c);
                    });
                } else {
                    contest.setAnnouncementSent(true);
                    contest.setAnnouncementSentAt(LocalDateTime.now());
                    contestRepository.save(contest);
                }

                logger.info("Contest announcement broadcast finished for contest id={}: sent={}, failed={}",
                        contestId, sentCount, failedCount);
            } catch (Exception e) {
                logger.error("Failed to broadcast contest announcement: {}", e.getMessage(), e);
            }
        });
    }

    @Transactional(readOnly = true)
    public List<ContestDto> listAllContestsForAdmin() {
        return contestRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    /**
     * Deletes a contest only when "safe": if any user has already registered, the
     * contest is not deleted (registrations are real user data and must not be
     * silently destroyed). ContestProblem rows are just internal configuration and are
     * safely cascade-removed with the contest via orphanRemoval.
     */
    @Transactional
    public void deleteContest(Long contestId) {
        if (!contestRepository.existsById(contestId)) {
            throw new ResourceNotFoundException("Contest not found with id: " + contestId);
        }
        if (contestRegistrationRepository.existsByContestId(contestId)) {
            throw new BadRequestException(
                    "Cannot delete a contest that already has registered participants. " +
                            "Consider ending it instead of deleting it.");
        }
        contestRepository.deleteById(contestId);
    }

    // =====================================================================
    // USER-FACING
    // =====================================================================

    @Transactional(readOnly = true)
    public List<ContestDto> listAvailableContests() {
        // "Available" to students means anything the admin has actually published -
        // DRAFT contests are admin-only and never shown here.
        return contestRepository.findByStatusIn(List.of(ContestStatus.PUBLISHED, ContestStatus.ONGOING, ContestStatus.ENDED))
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    /** Returns all contests created by this host user, ordered by creation date desc. */
    @Transactional(readOnly = true)
    public List<ContestDto> listContestsByCreator(Long userId) {
        return contestRepository.findByCreatedByIdOrderByCreatedAtDesc(userId)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ContestDto getContest(Long contestId) {
        Contest contest = contestRepository.findById(contestId)
                .orElseThrow(() -> new ResourceNotFoundException("Contest not found with id: " + contestId));
        return toDto(contest);
    }

    @Transactional
    public ContestRegistrationDto register(Long contestId, Long userId) {
        Contest contest = contestRepository.findById(contestId)
                .orElseThrow(() -> new ResourceNotFoundException("Contest not found with id: " + contestId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (contestRegistrationRepository.existsByUserIdAndContestId(userId, contestId)) {
            throw new BadRequestException("You are already registered for this contest.");
        }

        ContestRegistration registration = contestRegistrationRepository.save(new ContestRegistration(user, contest));

        if (emailService != null) {
            try {
                emailService.sendContestRegistrationEmail(user, contest);
            } catch (Exception ex) {
                logger.warn("Could not dispatch contest registration email to userId={}: {}", userId, ex.getMessage());
            }
        }

        return new ContestRegistrationDto(true, registration.getRegisteredAt());
    }

    @Transactional(readOnly = true)
    public ContestRegistrationDto getRegistrationStatus(Long contestId, Long userId) {
        return contestRegistrationRepository.findByUserIdAndContestId(userId, contestId)
                .map(r -> {
                    ContestRegistrationDto dto = new ContestRegistrationDto(true, r.getRegisteredAt());
                    contestAttemptRepository.findByParticipantIdAndContestId(userId, contestId).ifPresent(attempt -> {
                        boolean terminated = attempt.getStatus() == ContestAttemptStatus.COMPLETED
                                || (attempt.getSecurityViolationCount() != null && attempt.getSecurityViolationCount() >= maxViolations);
                        dto.setAttemptStatus(attempt.getStatus() != null ? attempt.getStatus().name() : null);
                        dto.setSecurityViolationCount(attempt.getSecurityViolationCount() != null ? attempt.getSecurityViolationCount() : 0);
                        dto.setMaxViolations(maxViolations);
                        dto.setTerminated(terminated);
                        dto.setScore(attempt.getScore() != null ? attempt.getScore() : 0);
                        dto.setProblemsSolved(attempt.getProblemsSolved() != null ? attempt.getProblemsSolved() : 0);
                    });
                    return dto;
                })
                .orElseGet(() -> new ContestRegistrationDto(false, null));
    }

    // =====================================================================
    // CONTEST PARTICIPATION / SCORING (Task 8)
    // =====================================================================

    /**
     * Ensures a ContestAttempt exists for this registered user+contest and returns its
     * current state. Called by the frontend both when the student first enters
     * /contests/:id/code (creates the attempt) and afterwards to refresh score/solved/
     * submission counts (idempotent - never creates a second attempt; the DB-level
     * unique constraint on (user_id, contest_id) backs this up too).
     */
    @Transactional
    public ContestAttemptDto getOrCreateAttempt(Long contestId, Long userId) {
        if (!contestRegistrationRepository.existsByUserIdAndContestId(userId, contestId)) {
            throw new BadRequestException("You must register for this contest before entering.");
        }
        Contest contest = contestRepository.findById(contestId)
                .orElseThrow(() -> new ResourceNotFoundException("Contest not found with id: " + contestId));

        LocalDateTime now = LocalDateTime.now();
        if (contest.getStartTime() != null && now.isBefore(contest.getStartTime())) {
            throw new BadRequestException("This contest has not started yet.");
        }
        if (contest.getEndTime() != null && !now.isBefore(contest.getEndTime())) {
            throw new BadRequestException("This contest has ended.");
        }

        ContestAttempt attempt = contestAttemptRepository.findByParticipantIdAndContestId(userId, contestId)
                .orElseGet(() -> createAttempt(contest, userId));

        if (attempt.getSecurityViolationCount() != null && attempt.getSecurityViolationCount() >= maxViolations) {
            if (attempt.getStatus() != ContestAttemptStatus.COMPLETED) {
                attempt.setStatus(ContestAttemptStatus.COMPLETED);
                attempt.setCompletedAt(now);
                contestAttemptRepository.save(attempt);
            }
        }

        completeIfEnded(attempt, contest);
        return toAttemptDto(attempt);
    }

    private ContestAttempt createAttempt(Contest contest, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        ContestAttempt attempt = new ContestAttempt(user, contest);
        LocalDateTime now = LocalDateTime.now();
        attempt.setStartedAt(now);
        // Defensive: if the attempt is only being created after the contest already
        // ended (e.g. the user never actually entered while it was live), mark it
        // completed immediately rather than falsely showing IN_PROGRESS.
        if (contest.getEndTime() != null && !now.isBefore(contest.getEndTime())) {
            attempt.setStatus(ContestAttemptStatus.COMPLETED);
            attempt.setCompletedAt(contest.getEndTime());
        } else {
            attempt.setStatus(ContestAttemptStatus.IN_PROGRESS);
        }
        return contestAttemptRepository.save(attempt);
    }

    /** Lazy completion (Step 9): flips IN_PROGRESS -> COMPLETED once endTime has passed. */
    private boolean completeIfEnded(ContestAttempt attempt, Contest contest) {
        if (attempt.getStatus() == ContestAttemptStatus.COMPLETED) return false;
        if (contest.getEndTime() == null || LocalDateTime.now().isBefore(contest.getEndTime())) return false;

        attempt.setStatus(ContestAttemptStatus.COMPLETED);
        attempt.setCompletedAt(contest.getEndTime());
        contestAttemptRepository.save(attempt);
        return true;
    }

    /**
     * Server-side contest-submission gate (Step 8) - the minimal protection Task 7's
     * UI-only timer was missing. Throws BadRequestException/ResourceNotFoundException
     * with a real message if the submission must be rejected; returns the Contest
     * otherwise so the caller (SubmissionService) can stamp it onto the Submission row.
     * Never trusts the frontend timer - always compares against the server's own clock.
     */
    @Transactional
    public Contest validateContestSubmissionAllowed(Long contestId, Long problemId, Long userId) {
        Contest contest = contestRepository.findById(contestId)
                .orElseThrow(() -> new ResourceNotFoundException("Contest not found with id: " + contestId));

        if (!contestRegistrationRepository.existsByUserIdAndContestId(userId, contestId)) {
            throw new BadRequestException("You must register for this contest before submitting.");
        }
        if (!contestProblemRepository.existsByContestIdAndProblemId(contestId, problemId)) {
            throw new BadRequestException("This problem is not part of this contest.");
        }

        LocalDateTime now = LocalDateTime.now();
        if (contest.getStartTime() != null && now.isBefore(contest.getStartTime())) {
            throw new BadRequestException("This contest has not started yet.");
        }
        if (contest.getEndTime() != null && !now.isBefore(contest.getEndTime())) {
            throw new BadRequestException("This contest has ended.");
        }

        // Make sure an attempt exists/is up to date even if the client never called the
        // attempt-start endpoint for some reason - scoring below always has a row to update.
        ContestAttempt attempt = contestAttemptRepository.findByParticipantIdAndContestId(userId, contestId)
                .orElseGet(() -> createAttempt(contest, userId));

        // Task 9, Step 8: submissions must be rejected once the attempt has been
        // terminated (whether by reaching the security-violation threshold, or by lazy
        // time-based completion) - never trust only the frontend's local state for this.
        if (attempt.getStatus() == ContestAttemptStatus.COMPLETED
                || (attempt.getSecurityViolationCount() != null && attempt.getSecurityViolationCount() >= maxViolations)) {
            throw new BadRequestException("Your contest attempt has ended and no further submissions are accepted.");
        }

        return contest;
    }

    /**
     * Recalculates and persists this participant's contest score/solvedCount/
     * submissionCount from scratch, from the actual Submission rows for this
     * contest+user. Always a full recomputation (never a "+=") so a problem's points can
     * never be double-counted no matter how many times it's (re-)submitted and accepted -
     * see Step 15. Called once per contest submission, after it has been saved.
     */
    @Transactional
    public void recordContestSubmissionOutcome(Long contestId, Long userId, Long problemId, SubmissionStatus ignoredStatus) {
        ContestAttempt attempt = contestAttemptRepository.findByParticipantIdAndContestId(userId, contestId)
                .orElseThrow(() -> new ResourceNotFoundException("Contest attempt not found for user " + userId + " in contest " + contestId));

        List<ContestProblem> contestProblems = contestProblemRepository.findByContestIdOrderByDisplayOrderAsc(contestId);
        Map<Long, Integer> pointsByProblemId = contestProblems.stream()
                .collect(Collectors.toMap(cp -> cp.getProblem().getId(), ContestProblem::getPoints));

        List<Long> distinctSolvedProblemIds = submissionRepository.findDistinctAcceptedProblemIdsByContestAndUser(contestId, userId);
        int score = distinctSolvedProblemIds.stream()
                .mapToInt(pid -> pointsByProblemId.getOrDefault(pid, 0))
                .sum();

        attempt.setScore(score);
        attempt.setProblemsSolved(distinctSolvedProblemIds.size());
        attempt.setSubmissionCount((int) submissionRepository.countByContestIdAndUserId(contestId, userId));
        contestAttemptRepository.save(attempt);
    }

    /**
     * Public per-contest leaderboard (Step 10/11), scoped to exactly this contest - never
     * to be confused with the separate global platform leaderboard. Ranking: higher score
     * first; ties broken by more problems solved; further ties broken by an earlier last-
     * accepted-submission time (classic "whoever finished first wins the tie").
     */
    @Transactional
    public List<ContestLeaderboardEntryDto> getLeaderboard(Long contestId) {
        Contest contest = contestRepository.findById(contestId)
                .orElseThrow(() -> new ResourceNotFoundException("Contest not found with id: " + contestId));

        List<ContestAttempt> attempts = contestAttemptRepository.findByContestId(contestId);
        for (ContestAttempt attempt : attempts) {
            completeIfEnded(attempt, contest);
        }

        List<RankableEntry> ranked = attempts.stream()
                .map(a -> new RankableEntry(a, submissionRepository.findLastAcceptedSubmissionTime(contestId, a.getParticipant().getId())))
                .sorted(RankableEntry.COMPARATOR)
                .collect(Collectors.toList());

        List<ContestLeaderboardEntryDto> result = new ArrayList<>();
        int rank = 1;
        for (RankableEntry entry : ranked) {
            ContestLeaderboardEntryDto dto = new ContestLeaderboardEntryDto();
            dto.setRank(rank++);
            dto.setUsername(entry.attempt.getParticipant().getUsername());
            dto.setName(entry.attempt.getParticipant().getName());
            dto.setScore(entry.attempt.getScore());
            dto.setSolvedCount(entry.attempt.getProblemsSolved());
            dto.setSubmissionCount(entry.attempt.getSubmissionCount());
            result.add(dto);
        }
        return result;
    }

    /**
     * Admin-only per-contest results (Step 12) - same ranking data plus attempt status/
     * timing, for an administrator reviewing one contest. Requires ROLE_ADMIN via the
     * existing /api/admin/** security rule; no new role system was added.
     */
    @Transactional
    public List<ContestParticipantAdminDto> getAdminParticipants(Long contestId) {
        Contest contest = contestRepository.findById(contestId)
                .orElseThrow(() -> new ResourceNotFoundException("Contest not found with id: " + contestId));

        List<ContestAttempt> attempts = contestAttemptRepository.findByContestId(contestId);
        for (ContestAttempt attempt : attempts) {
            completeIfEnded(attempt, contest);
        }

        return attempts.stream()
                .sorted(Comparator.comparing(ContestAttempt::getScore, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(a -> {
                    ContestParticipantAdminDto dto = new ContestParticipantAdminDto();
                    dto.setUserId(a.getParticipant().getId());
                    dto.setUsername(a.getParticipant().getUsername());
                    dto.setName(a.getParticipant().getName());
                    dto.setScore(a.getScore());
                    dto.setSolvedCount(a.getProblemsSolved());
                    dto.setSubmissionCount(a.getSubmissionCount());
                    dto.setStatus(a.getStatus().name());
                    dto.setStartedAt(a.getStartedAt());
                    dto.setCompletedAt(a.getCompletedAt());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public ContestAttemptDto finishContestAttempt(Long contestId, Long userId) {
        Contest contest = contestRepository.findById(contestId)
                .orElseThrow(() -> new ResourceNotFoundException("Contest not found with id: " + contestId));

        ContestAttempt attempt = contestAttemptRepository.findByParticipantIdAndContestId(userId, contestId)
                .orElseThrow(() -> new ResourceNotFoundException("Contest attempt not found for user " + userId));

        if (attempt.getStatus() != ContestAttemptStatus.COMPLETED) {
            attempt.setStatus(ContestAttemptStatus.COMPLETED);
            attempt.setCompletedAt(LocalDateTime.now());
            attempt = contestAttemptRepository.save(attempt);
        }

        // Calculate rank in contest
        List<ContestLeaderboardEntryDto> leaderboard = getLeaderboard(contestId);
        int userRank = 1;
        int totalParticipants = leaderboard.size();
        for (ContestLeaderboardEntryDto entry : leaderboard) {
            if (entry.getUsername() != null && entry.getUsername().equalsIgnoreCase(attempt.getParticipant().getUsername())) {
                userRank = entry.getRank();
                break;
            }
        }

        final int finalRank = userRank;
        final int finalTotal = totalParticipants > 0 ? totalParticipants : 1;
        final int finalScore = attempt.getScore() != null ? attempt.getScore() : 0;
        final User targetUser = attempt.getParticipant();
        final Contest targetContest = contest;

        if (emailService != null && targetUser != null && targetUser.getEmail() != null) {
            java.util.concurrent.CompletableFuture.runAsync(() -> {
                try {
                    emailService.sendContestResultEmail(targetUser, targetContest, finalRank, finalTotal, finalScore);
                } catch (Exception ex) {
                    logger.warn("Could not dispatch contest result email to {}: {}", targetUser.getEmail(), ex.getMessage());
                }
            });
        }

        return toAttemptDto(attempt);
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
        dto.setMaxViolations(maxViolations);
        return dto;
    }

    // =====================================================================
    // CONTEST EXAM SECURITY (Task 9)
    // =====================================================================
    // Basic browser-based monitoring (fullscreen-exit / tab-switch), NOT a guaranteed
    // anti-cheat mechanism - see README "Contest Exam Security" for the honest scope.

    /**
     * Records one security violation (fullscreen exit or tab/visibility switch) against
     * the authenticated user's ContestAttempt for this contest. The user is always taken
     * from the JWT-backed principal by the controller - never a client-supplied id.
     * Idempotent against an already-terminated attempt (returns its current state
     * rather than incrementing further or erroring), so a race of near-simultaneous
     * violation reports after termination doesn't throw at the user.
     */
    @Transactional
    public ContestSecurityViolationDto recordSecurityViolation(Long contestId, Long userId) {
        return recordSecurityViolation(contestId, userId, null);
    }

    @Transactional
    public ContestSecurityViolationDto recordSecurityViolation(Long contestId, Long userId, String violationType) {
        Contest contest = contestRepository.findById(contestId)
                .orElseThrow(() -> new ResourceNotFoundException("Contest not found with id: " + contestId));

        if (!contestRegistrationRepository.existsByUserIdAndContestId(userId, contestId)) {
            throw new BadRequestException("You must register for this contest before entering.");
        }

        LocalDateTime now = LocalDateTime.now();
        boolean active = contest.getStartTime() != null && !now.isBefore(contest.getStartTime())
                && contest.getEndTime() != null && now.isBefore(contest.getEndTime());
        if (!active) {
            throw new BadRequestException("This contest is not currently active.");
        }

        ContestAttempt attempt = contestAttemptRepository.findByParticipantIdAndContestId(userId, contestId)
                .orElseGet(() -> createAttempt(contest, userId));

        if (attempt.getStatus() == ContestAttemptStatus.COMPLETED) {
            return toViolationDto(attempt, true);
        }

        int updatedCount = (attempt.getSecurityViolationCount() != null ? attempt.getSecurityViolationCount() : 0) + 1;
        attempt.setSecurityViolationCount(updatedCount);
        if (violationType != null && !violationType.isBlank()) {
            log.warn("Contest security violation recorded for user {} in contest {}: type={}", userId, contestId, violationType);
        }

        boolean justTerminated = false;
        if (updatedCount >= maxViolations) {
            // Reuses the existing ContestAttemptStatus enum (Step 6) - no new status was
            // added. Score/solvedCount/submissionCount are left completely untouched.
            attempt.setStatus(ContestAttemptStatus.COMPLETED);
            attempt.setCompletedAt(now);
            justTerminated = true;
        }
        contestAttemptRepository.save(attempt);

        return toViolationDto(attempt, justTerminated);
    }

    private ContestSecurityViolationDto toViolationDto(ContestAttempt attempt, boolean terminated) {
        ContestSecurityViolationDto dto = new ContestSecurityViolationDto();
        dto.setViolationCount(attempt.getSecurityViolationCount());
        dto.setMaxViolations(maxViolations);
        dto.setTerminated(terminated);
        dto.setStatus(attempt.getStatus().name());
        return dto;
    }

    /** Small private holder so the leaderboard's three-level sort reads as one comparator. */
    private static class RankableEntry {
        final ContestAttempt attempt;
        final LocalDateTime lastAcceptedAt;

        RankableEntry(ContestAttempt attempt, LocalDateTime lastAcceptedAt) {
            this.attempt = attempt;
            this.lastAcceptedAt = lastAcceptedAt;
        }

        static final Comparator<RankableEntry> COMPARATOR = Comparator
                .comparing((RankableEntry e) -> e.attempt.getScore() != null ? e.attempt.getScore() : 0, Comparator.reverseOrder())
                .thenComparing(e -> e.attempt.getProblemsSolved() != null ? e.attempt.getProblemsSolved() : 0, Comparator.reverseOrder())
                .thenComparing(e -> e.lastAcceptedAt != null ? e.lastAcceptedAt : LocalDateTime.MAX);
    }

    // =====================================================================
    // HELPERS
    // =====================================================================

    private void validateTimes(ContestRequest request) {
        if (request.getStartTime() != null && request.getEndTime() != null
                && !request.getEndTime().isAfter(request.getStartTime())) {
            throw new BadRequestException("Contest end time must be after start time.");
        }
    }

    private ContestStatus parseStatus(String status) {
        if (status != null && "UPCOMING".equalsIgnoreCase(status.trim())) {
            return ContestStatus.PUBLISHED;
        }
        try {
            return ContestStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid contest status: " + status);
        }
    }

    private List<ContestProblemRequest> deduplicateRequests(List<ContestProblemRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return List.of();
        }
        Set<Long> seenProblemIds = new LinkedHashSet<>();
        List<ContestProblemRequest> unique = new ArrayList<>();
        int defaultOrder = 1;
        for (ContestProblemRequest req : requests) {
            if (req != null && req.getProblemId() != null && seenProblemIds.add(req.getProblemId())) {
                ContestProblemRequest clean = new ContestProblemRequest();
                clean.setProblemId(req.getProblemId());
                clean.setDisplayOrder(req.getDisplayOrder() != null ? req.getDisplayOrder() : defaultOrder);
                clean.setPoints(req.getPoints() != null ? req.getPoints() : 100);
                unique.add(clean);
                defaultOrder++;
            }
        }
        return unique;
    }

    private void applyContestProblems(Contest contest, List<ContestProblemRequest> problemRequests) {
        if (problemRequests == null || problemRequests.isEmpty()) {
            return;
        }
        List<ContestProblemRequest> unique = deduplicateRequests(problemRequests);
        List<ContestProblem> entries = new ArrayList<>();
        for (ContestProblemRequest pr : unique) {
            Problem problem = problemRepository.findById(pr.getProblemId())
                    .orElseThrow(() -> new ResourceNotFoundException("Problem not found with id: " + pr.getProblemId()));
            ContestProblem cp = new ContestProblem(contest, problem, pr.getDisplayOrder(), pr.getPoints());
            entries.add(cp);
            if (contest.getContestProblems() != null) {
                contest.getContestProblems().add(cp);
            }
        }
        contestProblemRepository.saveAll(entries);
        contestProblemRepository.flush();
    }

    private void reconcileContestProblems(Contest contest, List<ContestProblemRequest> problemRequests) {
        if (problemRequests == null) {
            return;
        }

        List<ContestProblemRequest> uniqueRequests = deduplicateRequests(problemRequests);
        Map<Long, ContestProblemRequest> requestMap = new LinkedHashMap<>();
        for (ContestProblemRequest req : uniqueRequests) {
            requestMap.put(req.getProblemId(), req);
        }

        List<ContestProblem> existingList = contestProblemRepository
                .findByContestIdOrderByDisplayOrderAsc(contest.getId());
        Map<Long, ContestProblem> existingMap = new HashMap<>();
        for (ContestProblem cp : existingList) {
            existingMap.put(cp.getProblem().getId(), cp);
        }

        // 1. Remove relationships no longer requested
        List<ContestProblem> toRemove = new ArrayList<>();
        for (ContestProblem cp : existingList) {
            Long problemId = cp.getProblem().getId();
            if (!requestMap.containsKey(problemId)) {
                toRemove.add(cp);
            }
        }
        if (!toRemove.isEmpty()) {
            if (contest.getContestProblems() != null) {
                contest.getContestProblems().removeIf(cp ->
                    toRemove.stream().anyMatch(r -> (r.getId() != null && r.getId().equals(cp.getId())) ||
                                                    (r.getProblem() != null && cp.getProblem() != null && r.getProblem().getId().equals(cp.getProblem().getId()))));
            }
            contestProblemRepository.deleteAll(toRemove);
            contestProblemRepository.flush();
        }

        // 2. Reuse and update existing relationships (prevents duplicate key errors)
        List<ContestProblem> toUpdate = new ArrayList<>();
        for (ContestProblem cp : existingList) {
            Long problemId = cp.getProblem().getId();
            if (requestMap.containsKey(problemId)) {
                ContestProblemRequest req = requestMap.get(problemId);
                cp.setDisplayOrder(req.getDisplayOrder());
                cp.setPoints(req.getPoints());
                toUpdate.add(cp);
            }
        }
        if (!toUpdate.isEmpty()) {
            contestProblemRepository.saveAll(toUpdate);
        }

        // 3. Create only genuinely new relationships
        List<ContestProblem> toAdd = new ArrayList<>();
        for (ContestProblemRequest req : uniqueRequests) {
            if (!existingMap.containsKey(req.getProblemId())) {
                Problem problem = problemRepository.findById(req.getProblemId())
                        .orElseThrow(() -> new ResourceNotFoundException("Problem not found with id: " + req.getProblemId()));
                ContestProblem cp = new ContestProblem(contest, problem, req.getDisplayOrder(), req.getPoints());
                toAdd.add(cp);
                if (contest.getContestProblems() != null) {
                    contest.getContestProblems().add(cp);
                }
            }
        }
        if (!toAdd.isEmpty()) {
            contestProblemRepository.saveAll(toAdd);
        }
        contestProblemRepository.flush();
    }

    private ContestDto toDto(Contest contest) {
        ContestDto dto = new ContestDto();
        dto.setId(contest.getId());
        dto.setTitle(contest.getTitle());
        dto.setOrganizationName(contest.getOrganizationName());
        dto.setDescription(contest.getDescription());
        dto.setStartTime(contest.getStartTime());
        dto.setEndTime(contest.getEndTime());
        dto.setStatus(resolveStatus(contest));
        dto.setCreatedByUsername(contest.getCreatedBy() != null ? contest.getCreatedBy().getUsername() : null);
        dto.setCreatedAt(contest.getCreatedAt());
        dto.setUpdatedAt(contest.getUpdatedAt());

        LocalDateTime now = LocalDateTime.now();
        dto.setServerTime(now);
        dto.setServerTimeIso(now.toString());

        List<ContestProblemDto> problemDtos = contestProblemRepository.findByContestIdOrderByDisplayOrderAsc(contest.getId())
                .stream()
                .map(cp -> new ContestProblemDto(
                        cp.getProblem().getId(),
                        cp.getProblem().getTitle(),
                        cp.getProblem().getDifficulty() != null ? cp.getProblem().getDifficulty().name() : null,
                        cp.getDisplayOrder(),
                        cp.getPoints()))
                .collect(Collectors.toList());
        dto.setProblems(problemDtos);
        dto.setAnnouncementSent(contest.isAnnouncementSent());
        if (contest.isAnnouncementSent()) {
            dto.setAnnouncementMessage("Announcement emails queued for registered users.");
        }

        return dto;
    }

    private String resolveStatus(Contest contest) {
        if (contest == null) {
            return ContestStatus.ENDED.name();
        }
        if (contest.getStatus() == ContestStatus.DRAFT) {
            return ContestStatus.DRAFT.name();
        }
        if (contest.getStatus() == ContestStatus.ENDED) {
            return ContestStatus.ENDED.name();
        }

        LocalDateTime now = LocalDateTime.now();
        // A contest whose end time has passed is strictly ENDED
        if (contest.getEndTime() != null && !now.isBefore(contest.getEndTime())) {
            return ContestStatus.ENDED.name();
        }

        return contest.getStatus().name();
    }
}


