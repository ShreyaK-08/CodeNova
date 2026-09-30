package com.oj.platform.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UserAiContextDto {

    private Long userId;
    private String username;
    private String name;

    // Problems Solved
    private long easySolved;
    private long mediumSolved;
    private long hardSolved;
    private long totalSolved;
    private long totalProblems;

    // Submissions
    private long totalSubmissions;
    private long acceptedSubmissions;
    private long failedSubmissions;
    private double acceptanceRate;

    // Assessments
    private int assessmentsCompleted;
    private int assessmentsAttempted;
    private double averageAssessmentPercentage;
    private Map<String, Object> latestAssessment;
    private List<Map<String, Object>> completedAssessments = new ArrayList<>();
    private List<Map<String, Object>> allAssessments = new ArrayList<>();

    // Contests
    private int contestsParticipated;
    private int contestProblemsSolved;
    private int totalContestSubmissions;
    private int totalContestScore;
    private Map<String, Object> latestContest;
    private List<Map<String, Object>> contestSummaries = new ArrayList<>();

    // Streak & Rank
    private int currentStreakDays;
    private String currentRank;

    public UserAiContextDto() {
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getEasySolved() {
        return easySolved;
    }

    public void setEasySolved(long easySolved) {
        this.easySolved = easySolved;
    }

    public long getMediumSolved() {
        return mediumSolved;
    }

    public void setMediumSolved(long mediumSolved) {
        this.mediumSolved = mediumSolved;
    }

    public long getHardSolved() {
        return hardSolved;
    }

    public void setHardSolved(long hardSolved) {
        this.hardSolved = hardSolved;
    }

    public long getTotalSolved() {
        return totalSolved;
    }

    public void setTotalSolved(long totalSolved) {
        this.totalSolved = totalSolved;
    }

    public long getTotalProblems() {
        return totalProblems;
    }

    public void setTotalProblems(long totalProblems) {
        this.totalProblems = totalProblems;
    }

    public long getTotalSubmissions() {
        return totalSubmissions;
    }

    public void setTotalSubmissions(long totalSubmissions) {
        this.totalSubmissions = totalSubmissions;
    }

    public long getAcceptedSubmissions() {
        return acceptedSubmissions;
    }

    public void setAcceptedSubmissions(long acceptedSubmissions) {
        this.acceptedSubmissions = acceptedSubmissions;
    }

    public double getAcceptanceRate() {
        return acceptanceRate;
    }

    public void setAcceptanceRate(double acceptanceRate) {
        this.acceptanceRate = acceptanceRate;
    }

    public int getAssessmentsCompleted() {
        return assessmentsCompleted;
    }

    public void setAssessmentsCompleted(int assessmentsCompleted) {
        this.assessmentsCompleted = assessmentsCompleted;
    }

    public double getAverageAssessmentPercentage() {
        return averageAssessmentPercentage;
    }

    public void setAverageAssessmentPercentage(double averageAssessmentPercentage) {
        this.averageAssessmentPercentage = averageAssessmentPercentage;
    }

    public List<Map<String, Object>> getCompletedAssessments() {
        return completedAssessments;
    }

    public void setCompletedAssessments(List<Map<String, Object>> completedAssessments) {
        this.completedAssessments = completedAssessments;
    }

    public int getContestsParticipated() {
        return contestsParticipated;
    }

    public void setContestsParticipated(int contestsParticipated) {
        this.contestsParticipated = contestsParticipated;
    }

    public int getContestProblemsSolved() {
        return contestProblemsSolved;
    }

    public void setContestProblemsSolved(int contestProblemsSolved) {
        this.contestProblemsSolved = contestProblemsSolved;
    }

    public int getTotalContestSubmissions() {
        return totalContestSubmissions;
    }

    public void setTotalContestSubmissions(int totalContestSubmissions) {
        this.totalContestSubmissions = totalContestSubmissions;
    }

    public int getTotalContestScore() {
        return totalContestScore;
    }

    public void setTotalContestScore(int totalContestScore) {
        this.totalContestScore = totalContestScore;
    }

    public List<Map<String, Object>> getContestSummaries() {
        return contestSummaries;
    }

    public void setContestSummaries(List<Map<String, Object>> contestSummaries) {
        this.contestSummaries = contestSummaries;
    }

    public int getCurrentStreakDays() {
        return currentStreakDays;
    }

    public void setCurrentStreakDays(int currentStreakDays) {
        this.currentStreakDays = currentStreakDays;
    }

    public long getFailedSubmissions() {
        return failedSubmissions;
    }

    public void setFailedSubmissions(long failedSubmissions) {
        this.failedSubmissions = failedSubmissions;
    }

    public int getAssessmentsAttempted() {
        return assessmentsAttempted;
    }

    public void setAssessmentsAttempted(int assessmentsAttempted) {
        this.assessmentsAttempted = assessmentsAttempted;
    }

    public Map<String, Object> getLatestAssessment() {
        return latestAssessment;
    }

    public void setLatestAssessment(Map<String, Object> latestAssessment) {
        this.latestAssessment = latestAssessment;
    }

    public List<Map<String, Object>> getAllAssessments() {
        return allAssessments;
    }

    public void setAllAssessments(List<Map<String, Object>> allAssessments) {
        this.allAssessments = allAssessments;
    }

    public Map<String, Object> getLatestContest() {
        return latestContest;
    }

    public void setLatestContest(Map<String, Object> latestContest) {
        this.latestContest = latestContest;
    }

    public String getCurrentRank() {
        return currentRank;
    }

    public void setCurrentRank(String currentRank) {
        this.currentRank = currentRank;
    }
}
