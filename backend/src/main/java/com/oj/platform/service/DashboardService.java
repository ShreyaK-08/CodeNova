package com.oj.platform.service;

import com.oj.platform.dto.AdminDashboardStatsDto;
import com.oj.platform.dto.CertificateDto;
import com.oj.platform.dto.UserDashboardStatsDto;
import com.oj.platform.entity.Difficulty;
import com.oj.platform.entity.Submission;
import com.oj.platform.entity.SubmissionStatus;
import com.oj.platform.entity.User;
import com.oj.platform.repository.CertificateRepository;
import com.oj.platform.repository.ProblemRepository;
import com.oj.platform.repository.SubmissionRepository;
import com.oj.platform.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final UserRepository userRepository;
    private final ProblemRepository problemRepository;
    private final SubmissionRepository submissionRepository;
    private final CertificateRepository certificateRepository;
    private final CertificateService certificateService;

    public DashboardService(UserRepository userRepository,
                            ProblemRepository problemRepository,
                            SubmissionRepository submissionRepository,
                            CertificateRepository certificateRepository,
                            CertificateService certificateService) {
        this.userRepository = userRepository;
        this.problemRepository = problemRepository;
        this.submissionRepository = submissionRepository;
        this.certificateRepository = certificateRepository;
        this.certificateService = certificateService;
    }

    @Transactional(readOnly = true)
    public UserDashboardStatsDto getUserDashboardStats(Long userId) {
        UserDashboardStatsDto stats = new UserDashboardStatsDto();

        long totalProblems = problemRepository.count();
        long solvedProblems = submissionRepository.countDistinctSolvedProblemsByUser(userId);
        long userSubmissions = submissionRepository.countByUserId(userId);
        long acceptedSubmissions = submissionRepository.countByUserIdAndStatus(userId, SubmissionStatus.ACCEPTED);

        double rate = userSubmissions > 0 ? ((double) acceptedSubmissions / userSubmissions) * 100.0 : 0.0;

        stats.setTotalProblems(totalProblems);
        stats.setTotalProblemsSolved(solvedProblems);
        stats.setAcceptanceRate(Math.round(rate * 10.0) / 10.0);
        stats.setTotalSubmissions(userSubmissions);
        stats.setAcceptedSubmissions(acceptedSubmissions);
        List<Submission> allUserSubmissions = submissionRepository.findByUserIdOrderBySubmittedAtDesc(userId);
        stats.setCurrentStreakDays(calculateCurrentStreak(allUserSubmissions));
        stats.setCurrentRank(calculateRank(userId));

        long distinctLanguages = allUserSubmissions.stream()
                .map(s -> CertificateService.normalizeLanguageName(s.getLanguage()))
                .distinct()
                .count();
        stats.setLanguagesUsedCount((int) distinctLanguages);
        stats.setCertificatesEarnedCount(certificateRepository.findByUserIdOrderByIssuedAtDesc(userId).size());

        stats.setEasySolved(submissionRepository.countDistinctSolvedProblemsByUserAndDifficulty(userId, Difficulty.EASY));
        stats.setMediumSolved(submissionRepository.countDistinctSolvedProblemsByUserAndDifficulty(userId, Difficulty.MEDIUM));
        stats.setHardSolved(submissionRepository.countDistinctSolvedProblemsByUserAndDifficulty(userId, Difficulty.HARD));

        stats.setTotalEasy(problemRepository.countByDifficulty(Difficulty.EASY));
        stats.setTotalMedium(problemRepository.countByDifficulty(Difficulty.MEDIUM));
        stats.setTotalHard(problemRepository.countByDifficulty(Difficulty.HARD));

        stats.setRecentSubmissions(allUserSubmissions.stream().limit(5).map(s -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", s.getId());
            map.put("problemTitle", s.getProblem().getTitle());
            map.put("difficulty", s.getProblem().getDifficulty().name());
            map.put("language", s.getLanguage());
            map.put("status", s.getStatus().name());
            map.put("submittedAt", s.getSubmittedAt());
            return map;
        }).collect(Collectors.toList()));

        return stats;
    }

    @Transactional(readOnly = true)
    public com.oj.platform.dto.DashboardActivityDto getDashboardActivity(Long userId) {
        List<Submission> submissions = submissionRepository.findByUserIdOrderBySubmittedAtDesc(userId);

        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(364); // 52 weeks (365 days total)

        Map<LocalDate, Integer> submissionsPerDay = new HashMap<>();
        Map<LocalDate, Set<Long>> solvedPerDay = new HashMap<>();

        for (Submission s : submissions) {
            if (s.getSubmittedAt() == null) continue;
            LocalDate d = s.getSubmittedAt().toLocalDate();
            if (!d.isBefore(startDate) && !d.isAfter(today)) {
                submissionsPerDay.put(d, submissionsPerDay.getOrDefault(d, 0) + 1);
                if (s.getStatus() == SubmissionStatus.ACCEPTED && s.getProblem() != null) {
                    solvedPerDay.computeIfAbsent(d, k -> new HashSet<>()).add(s.getProblem().getId());
                }
            }
        }

        List<com.oj.platform.dto.DashboardActivityDto.DailyActivityItem> days = new ArrayList<>();
        int activeDays = 0;
        int totalSubmissionsInWindow = 0;

        LocalDate cursor = startDate;
        while (!cursor.isAfter(today)) {
            int subs = submissionsPerDay.getOrDefault(cursor, 0);
            int solved = solvedPerDay.containsKey(cursor) ? solvedPerDay.get(cursor).size() : 0;
            days.add(new com.oj.platform.dto.DashboardActivityDto.DailyActivityItem(cursor.toString(), subs, solved));
            if (subs > 0) {
                activeDays++;
                totalSubmissionsInWindow += subs;
            }
            cursor = cursor.plusDays(1);
        }

        int currentStreak = calculateCurrentStreak(submissions);
        int longestStreak = calculateLongestStreak(submissions);

        return new com.oj.platform.dto.DashboardActivityDto(totalSubmissionsInWindow, activeDays, currentStreak, longestStreak, days);
    }

    @Transactional(readOnly = true)
    public List<com.oj.platform.dto.LanguageStatsDto> getLanguageStats(Long userId) {
        List<Submission> submissions = submissionRepository.findByUserIdOrderBySubmittedAtDesc(userId);

        Map<String, List<Submission>> byLanguage = new LinkedHashMap<>();
        for (Submission s : submissions) {
            String lang = CertificateService.normalizeLanguageName(s.getLanguage());
            byLanguage.computeIfAbsent(lang, k -> new ArrayList<>()).add(s);
        }

        List<com.oj.platform.dto.LanguageStatsDto> result = new ArrayList<>();
        for (Map.Entry<String, List<Submission>> entry : byLanguage.entrySet()) {
            String lang = entry.getKey();
            List<Submission> list = entry.getValue();

            long total = list.size();
            long accepted = list.stream().filter(s -> s.getStatus() == SubmissionStatus.ACCEPTED).count();
            long distinctSolved = list.stream()
                    .filter(s -> s.getStatus() == SubmissionStatus.ACCEPTED && s.getProblem() != null)
                    .map(s -> s.getProblem().getId())
                    .distinct()
                    .count();
            double rate = total > 0 ? Math.round((accepted * 100.0 / total) * 10.0) / 10.0 : 0.0;

            result.add(new com.oj.platform.dto.LanguageStatsDto(lang, distinctSolved, accepted, total, rate));
        }

        result.sort(Comparator.comparingLong(com.oj.platform.dto.LanguageStatsDto::getProblemsSolved)
                .thenComparingLong(com.oj.platform.dto.LanguageStatsDto::getTotalSubmissions).reversed());

        return result;
    }

    @Transactional
    public com.oj.platform.dto.MilestoneProgressDto getMilestoneProgress(Long userId) {
        certificateService.checkAndAwardMilestones(userId);

        long totalSolved = submissionRepository.countDistinctSolvedProblemsByUser(userId);
        List<CertificateDto> earnedCerts = certificateService.getUserCertificates(userId);
        Map<String, CertificateDto> certsByTitle = earnedCerts.stream()
                .collect(Collectors.toMap(CertificateDto::getTitle, c -> c, (a, b) -> a));

        int[] overallTiers = {1, 10, 25, 50, 100};
        List<com.oj.platform.dto.MilestoneProgressDto.MilestoneItemDto> overallList = new ArrayList<>();
        for (int m : overallTiers) {
            String title = m == 1 ? "First Problem Solved" : m + " Problems Solved";
            boolean earned = totalSolved >= m;
            CertificateDto cert = certsByTitle.get(title);
            double progress = Math.min(100.0, Math.round((totalSolved * 100.0 / m) * 10.0) / 10.0);
            overallList.add(new com.oj.platform.dto.MilestoneProgressDto.MilestoneItemDto(
                    "OVERALL",
                    title,
                    null,
                    m,
                    (int) Math.min(totalSolved, m),
                    earned,
                    progress,
                    cert != null && cert.getIssuedAt() != null ? cert.getIssuedAt().toString() : null,
                    cert != null ? cert.getCertificateNumber() : null,
                    cert != null ? cert.getVerificationCode() : null
            ));
        }

        String[] languages = {"C", "C++", "Java", "Python", "JavaScript", "SQL"};
        List<com.oj.platform.dto.MilestoneProgressDto.MilestoneItemDto> langList = new ArrayList<>();
        for (String lang : languages) {
            long solvedInLang = submissionRepository.countDistinctSolvedProblemsByUserAndLanguage(userId, lang);
            String title = lang + " Problem Solver";
            boolean earned = solvedInLang >= CertificateService.LANGUAGE_MILESTONE_THRESHOLD;
            CertificateDto cert = certsByTitle.get(title);
            double progress = Math.min(100.0, Math.round((solvedInLang * 100.0 / CertificateService.LANGUAGE_MILESTONE_THRESHOLD) * 10.0) / 10.0);
            langList.add(new com.oj.platform.dto.MilestoneProgressDto.MilestoneItemDto(
                    "LANGUAGE",
                    title,
                    lang,
                    CertificateService.LANGUAGE_MILESTONE_THRESHOLD,
                    (int) solvedInLang,
                    earned,
                    progress,
                    cert != null && cert.getIssuedAt() != null ? cert.getIssuedAt().toString() : null,
                    cert != null ? cert.getCertificateNumber() : null,
                    cert != null ? cert.getVerificationCode() : null
            ));
        }

        return new com.oj.platform.dto.MilestoneProgressDto(totalSolved, overallList, langList, earnedCerts);
    }

    /**
     * Streak = number of consecutive calendar days (ending today or yesterday)
     * on which the user made at least one submission.
     */
    private int calculateCurrentStreak(List<Submission> submissions) {
        if (submissions == null || submissions.isEmpty()) {
            return 0;
        }

        Set<LocalDate> submissionDays = submissions.stream()
                .map(s -> s.getSubmittedAt().toLocalDate())
                .collect(Collectors.toCollection(HashSet::new));

        LocalDate today = LocalDate.now();
        LocalDate cursor = submissionDays.contains(today) ? today : today.minusDays(1);

        if (!submissionDays.contains(cursor)) {
            return 0;
        }

        int streak = 0;
        while (submissionDays.contains(cursor)) {
            streak++;
            cursor = cursor.minusDays(1);
        }
        return streak;
    }

    private int calculateLongestStreak(List<Submission> submissions) {
        if (submissions == null || submissions.isEmpty()) {
            return 0;
        }

        List<LocalDate> dates = submissions.stream()
                .map(s -> s.getSubmittedAt().toLocalDate())
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        if (dates.isEmpty()) return 0;

        int longest = 1;
        int current = 1;
        for (int i = 1; i < dates.size(); i++) {
            if (dates.get(i).equals(dates.get(i - 1).plusDays(1))) {
                current++;
            } else {
                longest = Math.max(longest, current);
                current = 1;
            }
        }
        return Math.max(longest, current);
    }

    /**
     * Rank among all users by number of distinct problems solved (ACCEPTED submissions).
     * Users who have never solved a problem are not ranked.
     */
    private String calculateRank(Long userId) {
        List<Object[]> rows = submissionRepository.countSolvedProblemsGroupedByUser();

        List<Map.Entry<Long, Long>> solvedCounts = new ArrayList<>();
        for (Object[] row : rows) {
            Long uid = (Long) row[0];
            Long solved = (Long) row[1];
            if (solved != null && solved > 0) {
                solvedCounts.add(Map.entry(uid, solved));
            }
        }

        long userSolved = solvedCounts.stream()
                .filter(e -> e.getKey().equals(userId))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(0L);

        if (userSolved == 0) {
            return "-";
        }

        solvedCounts.sort(Comparator.comparingLong((Map.Entry<Long, Long> e) -> e.getValue()).reversed());

        int rank = 1;
        for (Map.Entry<Long, Long> e : solvedCounts) {
            if (e.getKey().equals(userId)) {
                break;
            }
            rank++;
        }
        return "#" + rank;
    }

    @Transactional(readOnly = true)
    public AdminDashboardStatsDto getAdminDashboardStats() {
        AdminDashboardStatsDto stats = new AdminDashboardStatsDto();

        long totalUsers = userRepository.count();
        long totalProblems = problemRepository.count();
        long totalSubmissions = submissionRepository.count();
        long acceptedSolutions = submissionRepository.countByStatus(SubmissionStatus.ACCEPTED);
        long todaySubmissions = submissionRepository.countBySubmittedAtAfter(LocalDateTime.now().minusDays(1));

        double overallRate = totalSubmissions > 0 ? ((double) acceptedSolutions / totalSubmissions) * 100.0 : 0.0;

        stats.setTotalUsers(totalUsers);
        stats.setActiveUsers(totalUsers);
        stats.setTotalProblems(totalProblems);
        stats.setTotalSubmissions(totalSubmissions);
        stats.setAcceptedSolutions(acceptedSolutions);
        stats.setTodaySubmissions(todaySubmissions);
        stats.setOverallAcceptanceRate(Math.round(overallRate * 10.0) / 10.0);

        List<Submission> recentSubs = submissionRepository.findTop10ByOrderBySubmittedAtDesc();
        stats.setRecentSubmissions(recentSubs.stream().map(s -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", s.getId());
            map.put("username", s.getUser().getUsername());
            map.put("problemTitle", s.getProblem().getTitle());
            map.put("language", s.getLanguage());
            map.put("status", s.getStatus().name());
            map.put("submittedAt", s.getSubmittedAt());
            return map;
        }).collect(Collectors.toList()));

        List<User> recentUsers = userRepository.findAll();
        stats.setRecentRegistrations(recentUsers.stream().limit(10).map(u -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", u.getId());
            map.put("name", u.getName());
            map.put("username", u.getUsername());
            map.put("email", u.getEmail());
            map.put("role", u.getRole().name());
            map.put("createdAt", u.getCreatedAt());
            return map;
        }).collect(Collectors.toList()));

        return stats;
    }
}
