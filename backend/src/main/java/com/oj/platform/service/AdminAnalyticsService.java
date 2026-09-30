package com.oj.platform.service;

import com.oj.platform.dto.AdminAnalyticsDto;
import com.oj.platform.entity.*;
import com.oj.platform.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminAnalyticsService {

    private final UserRepository userRepository;
    private final ProblemRepository problemRepository;
    private final SubmissionRepository submissionRepository;
    private final AssessmentRepository assessmentRepository;
    private final AssessmentAttemptRepository assessmentAttemptRepository;
    private final AssessmentViolationRepository assessmentViolationRepository;
    private final ContestRepository contestRepository;
    private final ContestRegistrationRepository contestRegistrationRepository;
    private final ContestAttemptRepository contestAttemptRepository;
    private final SupportRequestRepository supportRequestRepository;
    private final AssessmentFeedbackRepository assessmentFeedbackRepository;
    private final AssessmentQuestionFeedbackRepository assessmentQuestionFeedbackRepository;
    private final GeneralFeedbackRepository generalFeedbackRepository;

    public AdminAnalyticsService(
            UserRepository userRepository,
            ProblemRepository problemRepository,
            SubmissionRepository submissionRepository,
            AssessmentRepository assessmentRepository,
            AssessmentAttemptRepository assessmentAttemptRepository,
            AssessmentViolationRepository assessmentViolationRepository,
            ContestRepository contestRepository,
            ContestRegistrationRepository contestRegistrationRepository,
            ContestAttemptRepository contestAttemptRepository,
            SupportRequestRepository supportRequestRepository,
            AssessmentFeedbackRepository assessmentFeedbackRepository,
            AssessmentQuestionFeedbackRepository assessmentQuestionFeedbackRepository,
            GeneralFeedbackRepository generalFeedbackRepository) {
        this.userRepository = userRepository;
        this.problemRepository = problemRepository;
        this.submissionRepository = submissionRepository;
        this.assessmentRepository = assessmentRepository;
        this.assessmentAttemptRepository = assessmentAttemptRepository;
        this.assessmentViolationRepository = assessmentViolationRepository;
        this.contestRepository = contestRepository;
        this.contestRegistrationRepository = contestRegistrationRepository;
        this.contestAttemptRepository = contestAttemptRepository;
        this.supportRequestRepository = supportRequestRepository;
        this.assessmentFeedbackRepository = assessmentFeedbackRepository;
        this.assessmentQuestionFeedbackRepository = assessmentQuestionFeedbackRepository;
        this.generalFeedbackRepository = generalFeedbackRepository;
    }

    @Transactional(readOnly = true)
    public AdminAnalyticsDto getAnalytics(String dateRange) {
        AdminAnalyticsDto dto = new AdminAnalyticsDto();
        dto.setDateRange(dateRange != null && !dateRange.isBlank() ? dateRange.toUpperCase() : "ALL_TIME");

        LocalDateTime since = resolveSinceDate(dto.getDateRange());

        // 1. Overview & Users
        List<User> allUsers = userRepository.findAll();
        long totalUsers = allUsers.size();
        long adminUsers = allUsers.stream().filter(u -> u.getRole() == Role.ROLE_ADMIN).count();
        long regularUsers = totalUsers - adminUsers;

        // Active users: users with verified email OR who made at least one submission / attempt
        Set<Long> usersWithSubmissions = submissionRepository.findAll().stream()
                .map(s -> s.getUser() != null ? s.getUser().getId() : null)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        long activeUsers = allUsers.stream()
                .filter(u -> u.isEmailVerified() || usersWithSubmissions.contains(u.getId()))
                .count();
        long inactiveUsers = Math.max(0, totalUsers - activeUsers);

        dto.getOverview().setTotalUsers(totalUsers);
        dto.getOverview().setActiveUsers(activeUsers);
        dto.getOverview().setInactiveUsers(inactiveUsers);
        dto.getOverview().setTotalAdmins(adminUsers);

        dto.getUsers().setTotalUsers(totalUsers);
        dto.getUsers().setActiveUsers(activeUsers);
        dto.getUsers().setInactiveUsers(inactiveUsers);
        dto.getUsers().setAdminUsers(adminUsers);
        dto.getUsers().setRegularUsers(regularUsers);

        // User registration trend by month
        Map<String, Long> userTrendMap = new TreeMap<>();
        DateTimeFormatter ymFmt = DateTimeFormatter.ofPattern("MMM yyyy");
        for (User u : allUsers) {
            if (u.getCreatedAt() != null) {
                if (since == null || !u.getCreatedAt().isBefore(since)) {
                    String ym = u.getCreatedAt().format(ymFmt);
                    userTrendMap.put(ym, userTrendMap.getOrDefault(ym, 0L) + 1);
                }
            }
        }
        List<AdminAnalyticsDto.MonthlyTrendItemDto> trendList = new ArrayList<>();
        userTrendMap.forEach((month, count) -> trendList.add(new AdminAnalyticsDto.MonthlyTrendItemDto(month, count)));
        dto.getUsers().setRegistrationTrend(trendList);

        // 2. Problems
        List<Problem> allProblems = problemRepository.findAll();
        long totalProblems = allProblems.size();
        long easyProblems = allProblems.stream().filter(p -> p.getDifficulty() == Difficulty.EASY).count();
        long mediumProblems = allProblems.stream().filter(p -> p.getDifficulty() == Difficulty.MEDIUM).count();
        long hardProblems = allProblems.stream().filter(p -> p.getDifficulty() == Difficulty.HARD).count();

        dto.getOverview().setTotalProblems(totalProblems);
        dto.getProblems().setTotalProblems(totalProblems);
        dto.getProblems().setEasy(easyProblems);
        dto.getProblems().setMedium(mediumProblems);
        dto.getProblems().setHard(hardProblems);

        // 3. Submissions
        List<Submission> allSubmissions = submissionRepository.findAll();
        List<Submission> filteredSubmissions = since == null ? allSubmissions :
                allSubmissions.stream().filter(s -> s.getSubmittedAt() != null && !s.getSubmittedAt().isBefore(since)).collect(Collectors.toList());

        long totalSubmissions = filteredSubmissions.size();
        long accepted = 0;
        long wrongAnswer = 0;
        long compilationError = 0;
        long runtimeError = 0;
        long timeLimitExceeded = 0;

        Map<String, Long> languageCounts = new HashMap<>();

        for (Submission s : filteredSubmissions) {
            if (s.getStatus() == SubmissionStatus.ACCEPTED) accepted++;
            else if (s.getStatus() == SubmissionStatus.WRONG_ANSWER) wrongAnswer++;
            else if (s.getStatus() == SubmissionStatus.COMPILE_ERROR || s.getStatus() == SubmissionStatus.COMPILATION_ERROR) compilationError++;
            else if (s.getStatus() == SubmissionStatus.RUNTIME_ERROR) runtimeError++;
            else if (s.getStatus() == SubmissionStatus.TIME_LIMIT_EXCEEDED) timeLimitExceeded++;

            String lang = CertificateService.normalizeLanguageName(s.getLanguage());
            languageCounts.put(lang, languageCounts.getOrDefault(lang, 0L) + 1);
        }

        long failedSubmissions = totalSubmissions - accepted;
        double acceptanceRate = totalSubmissions > 0 ? Math.round(((double) accepted / totalSubmissions) * 1000.0) / 10.0 : 0.0;

        dto.getOverview().setTotalSubmissions(totalSubmissions);
        dto.getProblems().setTotalSubmissions(totalSubmissions);
        dto.getProblems().setAcceptedSubmissions(accepted);
        dto.getProblems().setFailedSubmissions(failedSubmissions);
        dto.getProblems().setAcceptanceRate(acceptanceRate);

        dto.getSubmissions().setTotalSubmissions(totalSubmissions);
        dto.getSubmissions().setAccepted(accepted);
        dto.getSubmissions().setWrongAnswer(wrongAnswer);
        dto.getSubmissions().setCompilationError(compilationError);
        dto.getSubmissions().setRuntimeError(runtimeError);
        dto.getSubmissions().setTimeLimitExceeded(timeLimitExceeded);

        if (totalSubmissions > 0) {
            dto.getSubmissions().setAcceptedPct(Math.round(((double) accepted / totalSubmissions) * 1000.0) / 10.0);
            dto.getSubmissions().setWrongAnswerPct(Math.round(((double) wrongAnswer / totalSubmissions) * 1000.0) / 10.0);
            dto.getSubmissions().setCompilationErrorPct(Math.round(((double) compilationError / totalSubmissions) * 1000.0) / 10.0);
            dto.getSubmissions().setRuntimeErrorPct(Math.round(((double) runtimeError / totalSubmissions) * 1000.0) / 10.0);
            dto.getSubmissions().setTimeLimitExceededPct(Math.round(((double) timeLimitExceeded / totalSubmissions) * 1000.0) / 10.0);
        }

        // Language usage list
        List<AdminAnalyticsDto.LanguageUsageItemDto> langUsageList = new ArrayList<>();
        languageCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .forEach(e -> {
                    double pct = totalSubmissions > 0 ? Math.round(((double) e.getValue() / totalSubmissions) * 1000.0) / 10.0 : 0.0;
                    langUsageList.add(new AdminAnalyticsDto.LanguageUsageItemDto(e.getKey(), e.getValue(), pct));
                });
        dto.setLanguageUsage(langUsageList);

        // 4. Assessments
        List<Assessment> allAssessments = assessmentRepository.findAll();
        List<Assessment> filteredAssessments = since == null ? allAssessments :
                allAssessments.stream().filter(a -> a.getCreatedAt() != null && !a.getCreatedAt().isBefore(since)).collect(Collectors.toList());

        long totalAssessments = filteredAssessments.size();
        long publishedAssessments = filteredAssessments.stream().filter(a -> a.getStatus() == AssessmentStatus.PUBLISHED).count();
        long draftAssessments = filteredAssessments.stream().filter(a -> a.getStatus() == AssessmentStatus.DRAFT).count();
        long archivedAssessments = filteredAssessments.stream().filter(a -> a.getStatus() == AssessmentStatus.ARCHIVED).count();
        long userHostedAssessments = filteredAssessments.stream().filter(a -> a.getCreatedBy() != null && a.getCreatedBy().getRole() == Role.ROLE_USER).count();
        long platformAdminAssessments = totalAssessments - userHostedAssessments;

        List<AssessmentAttempt> allAttempts = assessmentAttemptRepository.findAll();
        List<AssessmentAttempt> filteredAttempts = since == null ? allAttempts :
                allAttempts.stream().filter(a -> a.getStartedAt() != null && !a.getStartedAt().isBefore(since)).collect(Collectors.toList());

        long totalAttempts = filteredAttempts.size();
        long completedAttempts = filteredAttempts.stream().filter(a -> a.getStatus() == AssessmentAttemptStatus.COMPLETED).count();

        double avgScore = filteredAttempts.stream()
                .filter(a -> a.getStatus() == AssessmentAttemptStatus.COMPLETED && a.getScore() != null)
                .mapToInt(AssessmentAttempt::getScore)
                .average()
                .orElse(0.0);
        avgScore = Math.round(avgScore * 10.0) / 10.0;

        long totalViolations = assessmentViolationRepository.count();

        dto.getOverview().setTotalAssessments(totalAssessments);
        dto.getAssessments().setTotalAssessments(totalAssessments);
        dto.getAssessments().setPublished(publishedAssessments);
        dto.getAssessments().setDraft(draftAssessments);
        dto.getAssessments().setArchived(archivedAssessments);
        dto.getAssessments().setTotalAttempts(totalAttempts);
        dto.getAssessments().setCompletedAttempts(completedAttempts);
        dto.getAssessments().setAverageScore(avgScore);
        dto.getAssessments().setTotalViolations(totalViolations);
        dto.getAssessments().setUserHosted(userHostedAssessments);
        dto.getAssessments().setPlatformAdmin(platformAdminAssessments);

        // 5. Contests
        List<Contest> allContests = contestRepository.findAll();
        List<Contest> filteredContests = since == null ? allContests :
                allContests.stream().filter(c -> c.getCreatedAt() != null && !c.getCreatedAt().isBefore(since)).collect(Collectors.toList());

        long totalContests = filteredContests.size();
        long upcomingContests = filteredContests.stream().filter(c -> c.getStatus() == ContestStatus.PUBLISHED).count();
        long runningContests = filteredContests.stream().filter(c -> c.getStatus() == ContestStatus.ONGOING).count();
        long completedContests = filteredContests.stream().filter(c -> c.getStatus() == ContestStatus.ENDED).count();

        long totalParticipants = contestRegistrationRepository.count();
        long totalContestAttempts = contestAttemptRepository.count();

        dto.getOverview().setTotalContests(totalContests);
        dto.getContests().setTotalContests(totalContests);
        dto.getContests().setUpcoming(upcomingContests);
        dto.getContests().setRunning(runningContests);
        dto.getContests().setCompleted(completedContests);
        dto.getContests().setTotalParticipants(totalParticipants);
        dto.getContests().setTotalSubmissions(totalContestAttempts);
        dto.getContests().setTotalViolations(0);

        // 6. Feedback & Question Reports
        List<AssessmentFeedback> postFeedbacks = assessmentFeedbackRepository.findAll();
        List<AssessmentFeedback> filteredPostFeedbacks = since == null ? postFeedbacks :
                postFeedbacks.stream().filter(f -> f.getCreatedAt() != null && !f.getCreatedAt().isBefore(since)).collect(Collectors.toList());

        long totalPostFeedbacks = filteredPostFeedbacks.size();
        double avgRating = filteredPostFeedbacks.stream()
                .filter(f -> f.getRating() != null)
                .mapToInt(AssessmentFeedback::getRating)
                .average()
                .orElse(5.0);
        avgRating = Math.round(avgRating * 10.0) / 10.0;

        List<AssessmentQuestionFeedback> questionReports = assessmentQuestionFeedbackRepository.findAll();
        List<AssessmentQuestionFeedback> filteredQuestionReports = since == null ? questionReports :
                questionReports.stream().filter(q -> q.getCreatedAt() != null && !q.getCreatedAt().isBefore(since)).collect(Collectors.toList());

        long totalQuestionReports = filteredQuestionReports.size();
        Map<String, Long> categoryMap = new HashMap<>();
        for (AssessmentQuestionFeedback qf : filteredQuestionReports) {
            String cat = qf.getReason() != null ? qf.getReason() : "Other";
            categoryMap.put(cat, categoryMap.getOrDefault(cat, 0L) + 1);
        }
        List<AdminAnalyticsDto.CategoryCountDto> categories = new ArrayList<>();
        categoryMap.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .forEach(e -> categories.add(new AdminAnalyticsDto.CategoryCountDto(e.getKey(), e.getValue())));

        dto.getFeedback().setTotalPostFeedbacks(totalPostFeedbacks);
        dto.getFeedback().setAverageAssessmentRating(avgRating);
        dto.getFeedback().setTotalQuestionReports(totalQuestionReports);
        dto.getFeedback().setMostReportedQuestionCategories(categories);

        // 7. Support Requests
        List<SupportRequest> allSupport = supportRequestRepository.findAll();
        List<SupportRequest> filteredSupport = since == null ? allSupport :
                allSupport.stream().filter(s -> s.getCreatedAt() != null && !s.getCreatedAt().isBefore(since)).collect(Collectors.toList());

        long totalSupport = filteredSupport.size();
        long openSupport = filteredSupport.stream().filter(s -> "OPEN".equalsIgnoreCase(s.getStatus())).count();
        long resolvedSupport = filteredSupport.stream().filter(s -> "RESOLVED".equalsIgnoreCase(s.getStatus())).count();

        // Calculate average resolution time for resolved support requests
        double avgResolutionHours = 0.0;
        List<SupportRequest> resolvedList = filteredSupport.stream()
                .filter(s -> "RESOLVED".equalsIgnoreCase(s.getStatus()) && s.getCreatedAt() != null && s.getUpdatedAt() != null)
                .collect(Collectors.toList());
        if (!resolvedList.isEmpty()) {
            double totalHours = 0.0;
            for (SupportRequest sr : resolvedList) {
                Duration d = Duration.between(sr.getCreatedAt(), sr.getUpdatedAt());
                totalHours += Math.max(0.1, d.toMinutes() / 60.0);
            }
            avgResolutionHours = Math.round((totalHours / resolvedList.size()) * 10.0) / 10.0;
        }

        dto.getSupport().setTotalRequests(totalSupport);
        dto.getSupport().setOpenRequests(openSupport);
        dto.getSupport().setResolvedRequests(resolvedSupport);
        dto.getSupport().setAverageResolutionTimeHours(avgResolutionHours);

        // 8. Recent Activity
        List<AdminAnalyticsDto.RecentActivityItemDto> activities = new ArrayList<>();

        // Recent users
        allUsers.stream()
                .filter(u -> u.getCreatedAt() != null)
                .sorted(Comparator.comparing(User::getCreatedAt).reversed())
                .limit(5)
                .forEach(u -> activities.add(new AdminAnalyticsDto.RecentActivityItemDto(
                        "USER_REGISTER",
                        "New User Registered",
                        u.getUsername() + " (" + (u.getRole() != null ? u.getRole().name() : "USER") + ")",
                        u.isEmailVerified() ? "VERIFIED" : "PENDING",
                        u.getCreatedAt()
                )));

        // Recent submissions
        allSubmissions.stream()
                .filter(s -> s.getSubmittedAt() != null)
                .sorted(Comparator.comparing(Submission::getSubmittedAt).reversed())
                .limit(5)
                .forEach(s -> activities.add(new AdminAnalyticsDto.RecentActivityItemDto(
                        "SUBMISSION",
                        "Code Submission",
                        (s.getUser() != null ? s.getUser().getUsername() : "User") + " - " + (s.getProblem() != null ? s.getProblem().getTitle() : "Problem") + " (" + s.getLanguage() + ")",
                        s.getStatus() != null ? s.getStatus().name() : "PENDING",
                        s.getSubmittedAt()
                )));

        // Recent support requests
        allSupport.stream()
                .filter(s -> s.getCreatedAt() != null)
                .sorted(Comparator.comparing(SupportRequest::getCreatedAt).reversed())
                .limit(5)
                .forEach(s -> activities.add(new AdminAnalyticsDto.RecentActivityItemDto(
                        "SUPPORT_TICKET",
                        "Support Ticket #" + s.getTicketNumber(),
                        s.getSubject() + " (" + s.getCategory() + ")",
                        s.getStatus(),
                        s.getCreatedAt()
                )));

        // Sort all activities by timestamp descending and take top 10
        activities.sort(Comparator.comparing(AdminAnalyticsDto.RecentActivityItemDto::getTimestamp).reversed());
        dto.setRecentActivity(activities.stream().limit(10).collect(Collectors.toList()));

        return dto;
    }

    public String generateCsvReport(String dateRange) {
        AdminAnalyticsDto analytics = getAnalytics(dateRange);

        StringBuilder sb = new StringBuilder();
        sb.append("CodeNova Platform Analytics & Performance Report\n");
        sb.append("Generated At,").append(LocalDateTime.now().toString()).append("\n");
        sb.append("Date Range Filter,").append(analytics.getDateRange()).append("\n\n");

        sb.append("=== 1. PLATFORM OVERVIEW ===\n");
        sb.append("Metric,Value\n");
        sb.append("Total Users,").append(analytics.getOverview().getTotalUsers()).append("\n");
        sb.append("Active Users,").append(analytics.getOverview().getActiveUsers()).append("\n");
        sb.append("Inactive Users,").append(analytics.getOverview().getInactiveUsers()).append("\n");
        sb.append("Admin Users,").append(analytics.getOverview().getTotalAdmins()).append("\n");
        sb.append("Total Problems,").append(analytics.getOverview().getTotalProblems()).append("\n");
        sb.append("Total Assessments,").append(analytics.getOverview().getTotalAssessments()).append("\n");
        sb.append("Total Contests,").append(analytics.getOverview().getTotalContests()).append("\n");
        sb.append("Total Submissions,").append(analytics.getOverview().getTotalSubmissions()).append("\n\n");

        sb.append("=== 2. PROBLEM & SUBMISSION METRICS ===\n");
        sb.append("Easy Problems,").append(analytics.getProblems().getEasy()).append("\n");
        sb.append("Medium Problems,").append(analytics.getProblems().getMedium()).append("\n");
        sb.append("Hard Problems,").append(analytics.getProblems().getHard()).append("\n");
        sb.append("Accepted Submissions,").append(analytics.getSubmissions().getAccepted()).append(" (").append(analytics.getSubmissions().getAcceptedPct()).append("%)\n");
        sb.append("Wrong Answer Submissions,").append(analytics.getSubmissions().getWrongAnswer()).append(" (").append(analytics.getSubmissions().getWrongAnswerPct()).append("%)\n");
        sb.append("Compilation Error Submissions,").append(analytics.getSubmissions().getCompilationError()).append(" (").append(analytics.getSubmissions().getCompilationErrorPct()).append("%)\n");
        sb.append("Runtime Error Submissions,").append(analytics.getSubmissions().getRuntimeError()).append(" (").append(analytics.getSubmissions().getRuntimeErrorPct()).append("%)\n");
        sb.append("Time Limit Exceeded Submissions,").append(analytics.getSubmissions().getTimeLimitExceeded()).append(" (").append(analytics.getSubmissions().getTimeLimitExceededPct()).append("%)\n\n");

        sb.append("=== 3. LANGUAGE USAGE ===\n");
        sb.append("Language,Submission Count,Percentage\n");
        for (AdminAnalyticsDto.LanguageUsageItemDto lang : analytics.getLanguageUsage()) {
            sb.append(lang.getLanguage()).append(",").append(lang.getCount()).append(",").append(lang.getPercentage()).append("%\n");
        }
        sb.append("\n");

        sb.append("=== 4. ASSESSMENTS & CONTESTS ===\n");
        sb.append("Published Assessments,").append(analytics.getAssessments().getPublished()).append("\n");
        sb.append("Total Assessment Attempts,").append(analytics.getAssessments().getTotalAttempts()).append("\n");
        sb.append("Completed Assessment Attempts,").append(analytics.getAssessments().getCompletedAttempts()).append("\n");
        sb.append("Average Assessment Score,").append(analytics.getAssessments().getAverageScore()).append("\n");
        sb.append("Assessment Violations Recorded,").append(analytics.getAssessments().getTotalViolations()).append("\n");
        sb.append("Total Contests,").append(analytics.getContests().getTotalContests()).append("\n");
        sb.append("Contest Registrations,").append(analytics.getContests().getTotalParticipants()).append("\n\n");

        sb.append("=== 5. SUPPORT & FEEDBACK ===\n");
        sb.append("Total Support Tickets,").append(analytics.getSupport().getTotalRequests()).append("\n");
        sb.append("Open Tickets,").append(analytics.getSupport().getOpenRequests()).append("\n");
        sb.append("Resolved Tickets,").append(analytics.getSupport().getResolvedRequests()).append("\n");
        sb.append("Average Resolution Time (Hours),").append(analytics.getSupport().getAverageResolutionTimeHours()).append("\n");
        sb.append("Total Candidate Post-Feedbacks,").append(analytics.getFeedback().getTotalPostFeedbacks()).append("\n");
        sb.append("Average Candidate Rating,").append(analytics.getFeedback().getAverageAssessmentRating()).append("/5\n");
        sb.append("Total Question Reports,").append(analytics.getFeedback().getTotalQuestionReports()).append("\n");

        return sb.toString();
    }

    private LocalDateTime resolveSinceDate(String dateRange) {
        if (dateRange == null) return null;
        LocalDateTime now = LocalDateTime.now();
        switch (dateRange.toUpperCase()) {
            case "TODAY":
                return LocalDate.now().atStartOfDay();
            case "LAST_7_DAYS":
                return now.minusDays(7);
            case "LAST_30_DAYS":
                return now.minusDays(30);
            case "THIS_YEAR":
                return LocalDate.of(now.getYear(), 1, 1).atStartOfDay();
            case "ALL_TIME":
            default:
                return null;
        }
    }
}
