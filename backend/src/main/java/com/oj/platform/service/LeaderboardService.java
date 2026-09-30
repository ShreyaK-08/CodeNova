package com.oj.platform.service;

import com.oj.platform.dto.LeaderboardEntryDto;
import com.oj.platform.entity.Difficulty;
import com.oj.platform.entity.Problem;
import com.oj.platform.entity.Submission;
import com.oj.platform.entity.SubmissionStatus;
import com.oj.platform.entity.User;
import com.oj.platform.repository.SubmissionRepository;
import com.oj.platform.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LeaderboardService {

    private final SubmissionRepository submissionRepository;
    private final UserRepository userRepository;
    private final com.oj.platform.repository.CertificateRepository certificateRepository;

    public LeaderboardService(SubmissionRepository submissionRepository, UserRepository userRepository,
                              com.oj.platform.repository.CertificateRepository certificateRepository) {
        this.submissionRepository = submissionRepository;
        this.userRepository = userRepository;
        this.certificateRepository = certificateRepository;
    }

    @Transactional(readOnly = true)
    public List<LeaderboardEntryDto> getLeaderboard(String scope, String language) {
        List<User> users = userRepository.findAll();
        List<Submission> allSubmissions = submissionRepository.findAll();

        LocalDateTime weekStart = LocalDateTime.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .withHour(0).withMinute(0).withSecond(0).withNano(0);

        boolean isWeekly = "WEEKLY".equalsIgnoreCase(scope);
        String langFilter = normalizeLanguage(language);

        // Group submissions by user
        Map<Long, List<Submission>> subsByUser = new HashMap<>();
        for (Submission s : allSubmissions) {
            // Apply weekly filter
            if (isWeekly && s.getSubmittedAt().isBefore(weekStart)) {
                continue;
            }
            // Apply language filter
            if (langFilter != null && !langFilter.equalsIgnoreCase(normalizeLanguage(s.getLanguage()))) {
                continue;
            }
            subsByUser.computeIfAbsent(s.getUser().getId(), k -> new ArrayList<>()).add(s);
        }

        List<LeaderboardEntryDto> entries = new ArrayList<>();

        for (User u : users) {
            List<Submission> userSubs = subsByUser.getOrDefault(u.getId(), Collections.emptyList());

            // In weekly or language-specific leaderboards, only list users who participated in that scope
            if ((isWeekly || langFilter != null) && userSubs.isEmpty()) {
                continue;
            }

            long totalSubmissions = userSubs.size();
            List<Submission> acceptedSubs = userSubs.stream()
                    .filter(s -> s.getStatus() == SubmissionStatus.ACCEPTED)
                    .collect(Collectors.toList());
            long totalAccepted = acceptedSubs.size();

            double acceptanceRate = totalSubmissions > 0
                    ? Math.round(((double) totalAccepted / totalSubmissions) * 1000.0) / 10.0
                    : 0.0;

            // Unique solved problems (do NOT award duplicate points for solving the same problem multiple times)
            Map<Long, Problem> uniqueSolvedProblems = new HashMap<>();
            Long fastestTime = null;

            for (Submission s : acceptedSubs) {
                if (s.getProblem() != null) {
                    uniqueSolvedProblems.put(s.getProblem().getId(), s.getProblem());
                }
                if (s.getExecutionTime() != null) {
                    if (fastestTime == null || s.getExecutionTime() < fastestTime) {
                        fastestTime = s.getExecutionTime();
                    }
                }
            }

            int score = 0;
            long easyCount = 0;
            long mediumCount = 0;
            long hardCount = 0;

            for (Problem p : uniqueSolvedProblems.values()) {
                Difficulty diff = p.getDifficulty() != null ? p.getDifficulty() : Difficulty.EASY;
                if (diff == Difficulty.EASY) {
                    score += 100;
                    easyCount++;
                } else if (diff == Difficulty.MEDIUM) {
                    score += 200;
                    mediumCount++;
                } else if (diff == Difficulty.HARD) {
                    score += 300;
                    hardCount++;
                }
            }

            LeaderboardEntryDto entry = new LeaderboardEntryDto();
            entry.setUserId(u.getId());
            entry.setUsername(u.getUsername());
            entry.setName(u.getName() != null && !u.getName().isBlank() ? u.getName() : u.getUsername());
            entry.setProblemsSolved(uniqueSolvedProblems.size());
            entry.setTotalAccepted(totalAccepted);
            entry.setTotalSubmissions(totalSubmissions);
            entry.setAcceptanceRate(acceptanceRate);
            entry.setScore(score);
            entry.setFastestExecutionTimeMs(fastestTime);
            entry.setEasySolved(easyCount);
            entry.setMediumSolved(mediumCount);
            entry.setHardSolved(hardCount);
            entry.setCertificatesCount(certificateRepository.findByUserIdOrderByMilestoneAsc(u.getId()).size());

            entries.add(entry);
        }

        // Sorting:
        // 1. Score DESC (weighted problems solved)
        // 2. Problems solved count DESC
        // 3. Acceptance rate DESC
        // 4. Fastest accepted execution time ASC (nulls last)
        // 5. User ID ASC (stable tie-breaker)
        entries.sort((a, b) -> {
            int cmp = Integer.compare(b.getScore(), a.getScore());
            if (cmp != 0) return cmp;
            cmp = Long.compare(b.getProblemsSolved(), a.getProblemsSolved());
            if (cmp != 0) return cmp;
            cmp = Double.compare(b.getAcceptanceRate(), a.getAcceptanceRate());
            if (cmp != 0) return cmp;
            long timeA = a.getFastestExecutionTimeMs() != null ? a.getFastestExecutionTimeMs() : Long.MAX_VALUE;
            long timeB = b.getFastestExecutionTimeMs() != null ? b.getFastestExecutionTimeMs() : Long.MAX_VALUE;
            cmp = Long.compare(timeA, timeB);
            if (cmp != 0) return cmp;
            return Long.compare(a.getUserId(), b.getUserId());
        });

        // Assign dynamic ranks: 1, 2, 3...
        int rank = 1;
        for (LeaderboardEntryDto entry : entries) {
            entry.setRank(rank++);
        }

        return entries;
    }

    private String normalizeLanguage(String lang) {
        if (lang == null || lang.isBlank() || "ALL".equalsIgnoreCase(lang)) {
            return null;
        }
        String upper = lang.trim().toUpperCase();
        if ("C++".equals(upper) || "CPP".equals(upper)) return "CPP";
        if ("JS".equals(upper) || "JAVASCRIPT".equals(upper)) return "JAVASCRIPT";
        if ("PY".equals(upper) || "PYTHON".equals(upper) || "PYTHON3".equals(upper)) return "PYTHON";
        return upper;
    }
}
