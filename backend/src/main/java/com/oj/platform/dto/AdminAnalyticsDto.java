package com.oj.platform.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AdminAnalyticsDto {

    private String dateRange;
    private PlatformOverviewDto overview = new PlatformOverviewDto();
    private UserAnalyticsDto users = new UserAnalyticsDto();
    private ProblemAnalyticsDto problems = new ProblemAnalyticsDto();
    private AssessmentAnalyticsDto assessments = new AssessmentAnalyticsDto();
    private ContestAnalyticsDto contests = new ContestAnalyticsDto();
    private SubmissionAnalyticsDto submissions = new SubmissionAnalyticsDto();
    private List<LanguageUsageItemDto> languageUsage = new ArrayList<>();
    private FeedbackAnalyticsDto feedback = new FeedbackAnalyticsDto();
    private SupportAnalyticsDto support = new SupportAnalyticsDto();
    private List<RecentActivityItemDto> recentActivity = new ArrayList<>();

    public AdminAnalyticsDto() {
    }

    // ── Sub-DTOs ─────────────────────────────────────────────────────────────

    public static class PlatformOverviewDto {
        private long totalUsers;
        private long activeUsers;
        private long inactiveUsers;
        private long totalAdmins;
        private long totalProblems;
        private long totalAssessments;
        private long totalContests;
        private long totalSubmissions;

        public PlatformOverviewDto() {}

        public long getTotalUsers() { return totalUsers; }
        public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }

        public long getActiveUsers() { return activeUsers; }
        public void setActiveUsers(long activeUsers) { this.activeUsers = activeUsers; }

        public long getInactiveUsers() { return inactiveUsers; }
        public void setInactiveUsers(long inactiveUsers) { this.inactiveUsers = inactiveUsers; }

        public long getTotalAdmins() { return totalAdmins; }
        public void setTotalAdmins(long totalAdmins) { this.totalAdmins = totalAdmins; }

        public long getTotalProblems() { return totalProblems; }
        public void setTotalProblems(long totalProblems) { this.totalProblems = totalProblems; }

        public long getTotalAssessments() { return totalAssessments; }
        public void setTotalAssessments(long totalAssessments) { this.totalAssessments = totalAssessments; }

        public long getTotalContests() { return totalContests; }
        public void setTotalContests(long totalContests) { this.totalContests = totalContests; }

        public long getTotalSubmissions() { return totalSubmissions; }
        public void setTotalSubmissions(long totalSubmissions) { this.totalSubmissions = totalSubmissions; }
    }

    public static class UserAnalyticsDto {
        private long totalUsers;
        private long activeUsers;
        private long inactiveUsers;
        private long adminUsers;
        private long regularUsers;
        private List<MonthlyTrendItemDto> registrationTrend = new ArrayList<>();

        public UserAnalyticsDto() {}

        public long getTotalUsers() { return totalUsers; }
        public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }

        public long getActiveUsers() { return activeUsers; }
        public void setActiveUsers(long activeUsers) { this.activeUsers = activeUsers; }

        public long getInactiveUsers() { return inactiveUsers; }
        public void setInactiveUsers(long inactiveUsers) { this.inactiveUsers = inactiveUsers; }

        public long getAdminUsers() { return adminUsers; }
        public void setAdminUsers(long adminUsers) { this.adminUsers = adminUsers; }

        public long getRegularUsers() { return regularUsers; }
        public void setRegularUsers(long regularUsers) { this.regularUsers = regularUsers; }

        public List<MonthlyTrendItemDto> getRegistrationTrend() { return registrationTrend; }
        public void setRegistrationTrend(List<MonthlyTrendItemDto> registrationTrend) { this.registrationTrend = registrationTrend; }
    }

    public static class ProblemAnalyticsDto {
        private long totalProblems;
        private long easy;
        private long medium;
        private long hard;
        private long totalSubmissions;
        private long acceptedSubmissions;
        private long failedSubmissions;
        private double acceptanceRate;

        public ProblemAnalyticsDto() {}

        public long getTotalProblems() { return totalProblems; }
        public void setTotalProblems(long totalProblems) { this.totalProblems = totalProblems; }

        public long getEasy() { return easy; }
        public void setEasy(long easy) { this.easy = easy; }

        public long getMedium() { return medium; }
        public void setMedium(long medium) { this.medium = medium; }

        public long getHard() { return hard; }
        public void setHard(long hard) { this.hard = hard; }

        public long getTotalSubmissions() { return totalSubmissions; }
        public void setTotalSubmissions(long totalSubmissions) { this.totalSubmissions = totalSubmissions; }

        public long getAcceptedSubmissions() { return acceptedSubmissions; }
        public void setAcceptedSubmissions(long acceptedSubmissions) { this.acceptedSubmissions = acceptedSubmissions; }

        public long getFailedSubmissions() { return failedSubmissions; }
        public void setFailedSubmissions(long failedSubmissions) { this.failedSubmissions = failedSubmissions; }

        public double getAcceptanceRate() { return acceptanceRate; }
        public void setAcceptanceRate(double acceptanceRate) { this.acceptanceRate = acceptanceRate; }
    }

    public static class AssessmentAnalyticsDto {
        private long totalAssessments;
        private long published;
        private long draft;
        private long archived;
        private long totalAttempts;
        private long completedAttempts;
        private double averageScore;
        private long totalViolations;
        private long userHosted;
        private long platformAdmin;

        public AssessmentAnalyticsDto() {}

        public long getTotalAssessments() { return totalAssessments; }
        public void setTotalAssessments(long totalAssessments) { this.totalAssessments = totalAssessments; }

        public long getPublished() { return published; }
        public void setPublished(long published) { this.published = published; }

        public long getDraft() { return draft; }
        public void setDraft(long draft) { this.draft = draft; }

        public long getArchived() { return archived; }
        public void setArchived(long archived) { this.archived = archived; }

        public long getTotalAttempts() { return totalAttempts; }
        public void setTotalAttempts(long totalAttempts) { this.totalAttempts = totalAttempts; }

        public long getCompletedAttempts() { return completedAttempts; }
        public void setCompletedAttempts(long completedAttempts) { this.completedAttempts = completedAttempts; }

        public double getAverageScore() { return averageScore; }
        public void setAverageScore(double averageScore) { this.averageScore = averageScore; }

        public long getTotalViolations() { return totalViolations; }
        public void setTotalViolations(long totalViolations) { this.totalViolations = totalViolations; }

        public long getUserHosted() { return userHosted; }
        public void setUserHosted(long userHosted) { this.userHosted = userHosted; }

        public long getPlatformAdmin() { return platformAdmin; }
        public void setPlatformAdmin(long platformAdmin) { this.platformAdmin = platformAdmin; }
    }

    public static class ContestAnalyticsDto {
        private long totalContests;
        private long upcoming;
        private long running;
        private long completed;
        private long totalParticipants;
        private long totalSubmissions;
        private long totalViolations;

        public ContestAnalyticsDto() {}

        public long getTotalContests() { return totalContests; }
        public void setTotalContests(long totalContests) { this.totalContests = totalContests; }

        public long getUpcoming() { return upcoming; }
        public void setUpcoming(long upcoming) { this.upcoming = upcoming; }

        public long getRunning() { return running; }
        public void setRunning(long running) { this.running = running; }

        public long getCompleted() { return completed; }
        public void setCompleted(long completed) { this.completed = completed; }

        public long getTotalParticipants() { return totalParticipants; }
        public void setTotalParticipants(long totalParticipants) { this.totalParticipants = totalParticipants; }

        public long getTotalSubmissions() { return totalSubmissions; }
        public void setTotalSubmissions(long totalSubmissions) { this.totalSubmissions = totalSubmissions; }

        public long getTotalViolations() { return totalViolations; }
        public void setTotalViolations(long totalViolations) { this.totalViolations = totalViolations; }
    }

    public static class SubmissionAnalyticsDto {
        private long totalSubmissions;
        private long accepted;
        private long wrongAnswer;
        private long compilationError;
        private long runtimeError;
        private long timeLimitExceeded;
        private double acceptedPct;
        private double wrongAnswerPct;
        private double compilationErrorPct;
        private double runtimeErrorPct;
        private double timeLimitExceededPct;

        public SubmissionAnalyticsDto() {}

        public long getTotalSubmissions() { return totalSubmissions; }
        public void setTotalSubmissions(long totalSubmissions) { this.totalSubmissions = totalSubmissions; }

        public long getAccepted() { return accepted; }
        public void setAccepted(long accepted) { this.accepted = accepted; }

        public long getWrongAnswer() { return wrongAnswer; }
        public void setWrongAnswer(long wrongAnswer) { this.wrongAnswer = wrongAnswer; }

        public long getCompilationError() { return compilationError; }
        public void setCompilationError(long compilationError) { this.compilationError = compilationError; }

        public long getRuntimeError() { return runtimeError; }
        public void setRuntimeError(long runtimeError) { this.runtimeError = runtimeError; }

        public long getTimeLimitExceeded() { return timeLimitExceeded; }
        public void setTimeLimitExceeded(long timeLimitExceeded) { this.timeLimitExceeded = timeLimitExceeded; }

        public double getAcceptedPct() { return acceptedPct; }
        public void setAcceptedPct(double acceptedPct) { this.acceptedPct = acceptedPct; }

        public double getWrongAnswerPct() { return wrongAnswerPct; }
        public void setWrongAnswerPct(double wrongAnswerPct) { this.wrongAnswerPct = wrongAnswerPct; }

        public double getCompilationErrorPct() { return compilationErrorPct; }
        public void setCompilationErrorPct(double compilationErrorPct) { this.compilationErrorPct = compilationErrorPct; }

        public double getRuntimeErrorPct() { return runtimeErrorPct; }
        public void setRuntimeErrorPct(double runtimeErrorPct) { this.runtimeErrorPct = runtimeErrorPct; }

        public double getTimeLimitExceededPct() { return timeLimitExceededPct; }
        public void setTimeLimitExceededPct(double timeLimitExceededPct) { this.timeLimitExceededPct = timeLimitExceededPct; }
    }

    public static class LanguageUsageItemDto {
        private String language;
        private long count;
        private double percentage;

        public LanguageUsageItemDto() {}
        public LanguageUsageItemDto(String language, long count, double percentage) {
            this.language = language;
            this.count = count;
            this.percentage = percentage;
        }

        public String getLanguage() { return language; }
        public void setLanguage(String language) { this.language = language; }

        public long getCount() { return count; }
        public void setCount(long count) { this.count = count; }

        public double getPercentage() { return percentage; }
        public void setPercentage(double percentage) { this.percentage = percentage; }
    }

    public static class FeedbackAnalyticsDto {
        private long totalPostFeedbacks;
        private double averageAssessmentRating;
        private long totalQuestionReports;
        private List<CategoryCountDto> mostReportedQuestionCategories = new ArrayList<>();

        public FeedbackAnalyticsDto() {}

        public long getTotalPostFeedbacks() { return totalPostFeedbacks; }
        public void setTotalPostFeedbacks(long totalPostFeedbacks) { this.totalPostFeedbacks = totalPostFeedbacks; }

        public double getAverageAssessmentRating() { return averageAssessmentRating; }
        public void setAverageAssessmentRating(double averageAssessmentRating) { this.averageAssessmentRating = averageAssessmentRating; }

        public long getTotalQuestionReports() { return totalQuestionReports; }
        public void setTotalQuestionReports(long totalQuestionReports) { this.totalQuestionReports = totalQuestionReports; }

        public List<CategoryCountDto> getMostReportedQuestionCategories() { return mostReportedQuestionCategories; }
        public void setMostReportedQuestionCategories(List<CategoryCountDto> mostReportedQuestionCategories) { this.mostReportedQuestionCategories = mostReportedQuestionCategories; }
    }

    public static class CategoryCountDto {
        private String category;
        private long count;

        public CategoryCountDto() {}
        public CategoryCountDto(String category, long count) {
            this.category = category;
            this.count = count;
        }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public long getCount() { return count; }
        public void setCount(long count) { this.count = count; }
    }

    public static class SupportAnalyticsDto {
        private long totalRequests;
        private long openRequests;
        private long resolvedRequests;
        private double averageResolutionTimeHours;

        public SupportAnalyticsDto() {}

        public long getTotalRequests() { return totalRequests; }
        public void setTotalRequests(long totalRequests) { this.totalRequests = totalRequests; }

        public long getOpenRequests() { return openRequests; }
        public void setOpenRequests(long openRequests) { this.openRequests = openRequests; }

        public long getResolvedRequests() { return resolvedRequests; }
        public void setResolvedRequests(long resolvedRequests) { this.resolvedRequests = resolvedRequests; }

        public double getAverageResolutionTimeHours() { return averageResolutionTimeHours; }
        public void setAverageResolutionTimeHours(double averageResolutionTimeHours) { this.averageResolutionTimeHours = averageResolutionTimeHours; }
    }

    public static class MonthlyTrendItemDto {
        private String month;
        private long count;

        public MonthlyTrendItemDto() {}
        public MonthlyTrendItemDto(String month, long count) {
            this.month = month;
            this.count = count;
        }

        public String getMonth() { return month; }
        public void setMonth(String month) { this.month = month; }

        public long getCount() { return count; }
        public void setCount(long count) { this.count = count; }
    }

    public static class RecentActivityItemDto {
        private String type; // USER_REGISTER, SUBMISSION, ASSESSMENT_CREATED, CONTEST_CREATED, SUPPORT_TICKET, FEEDBACK
        private String title;
        private String detail;
        private String status;
        private LocalDateTime timestamp;

        public RecentActivityItemDto() {}
        public RecentActivityItemDto(String type, String title, String detail, String status, LocalDateTime timestamp) {
            this.type = type;
            this.title = title;
            this.detail = detail;
            this.status = status;
            this.timestamp = timestamp;
        }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getDetail() { return detail; }
        public void setDetail(String detail) { this.detail = detail; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }

    // ── Getters and Setters for Root DTO ─────────────────────────────────────

    public String getDateRange() { return dateRange; }
    public void setDateRange(String dateRange) { this.dateRange = dateRange; }

    public PlatformOverviewDto getOverview() { return overview; }
    public void setOverview(PlatformOverviewDto overview) { this.overview = overview; }

    public UserAnalyticsDto getUsers() { return users; }
    public void setUsers(UserAnalyticsDto users) { this.users = users; }

    public ProblemAnalyticsDto getProblems() { return problems; }
    public void setProblems(ProblemAnalyticsDto problems) { this.problems = problems; }

    public AssessmentAnalyticsDto getAssessments() { return assessments; }
    public void setAssessments(AssessmentAnalyticsDto assessments) { this.assessments = assessments; }

    public ContestAnalyticsDto getContests() { return contests; }
    public void setContests(ContestAnalyticsDto contests) { this.contests = contests; }

    public SubmissionAnalyticsDto getSubmissions() { return submissions; }
    public void setSubmissions(SubmissionAnalyticsDto submissions) { this.submissions = submissions; }

    public List<LanguageUsageItemDto> getLanguageUsage() { return languageUsage; }
    public void setLanguageUsage(List<LanguageUsageItemDto> languageUsage) { this.languageUsage = languageUsage; }

    public FeedbackAnalyticsDto getFeedback() { return feedback; }
    public void setFeedback(FeedbackAnalyticsDto feedback) { this.feedback = feedback; }

    public SupportAnalyticsDto getSupport() { return support; }
    public void setSupport(SupportAnalyticsDto support) { this.support = support; }

    public List<RecentActivityItemDto> getRecentActivity() { return recentActivity; }
    public void setRecentActivity(List<RecentActivityItemDto> recentActivity) { this.recentActivity = recentActivity; }
}
