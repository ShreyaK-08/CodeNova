package com.oj.platform.dto;

import java.util.List;
import java.util.Map;

public class UserDashboardStatsDto {

    private long totalProblemsSolved;
    private long totalProblems;
    private double acceptanceRate;
    private long totalSubmissions;
    private long acceptedSubmissions;
    private int currentStreakDays;
    private String currentRank;
    private long easySolved;
    private long mediumSolved;
    private long hardSolved;
    private long totalEasy;
    private long totalMedium;
    private long totalHard;
    private int languagesUsedCount;
    private int certificatesEarnedCount;
    private List<Map<String, Object>> recentSubmissions;

    public UserDashboardStatsDto() {
    }

    public long getTotalProblemsSolved() {
        return totalProblemsSolved;
    }

    public void setTotalProblemsSolved(long totalProblemsSolved) {
        this.totalProblemsSolved = totalProblemsSolved;
    }

    public long getTotalProblems() {
        return totalProblems;
    }

    public void setTotalProblems(long totalProblems) {
        this.totalProblems = totalProblems;
    }

    public double getAcceptanceRate() {
        return acceptanceRate;
    }

    public void setAcceptanceRate(double acceptanceRate) {
        this.acceptanceRate = acceptanceRate;
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

    public int getCurrentStreakDays() {
        return currentStreakDays;
    }

    public void setCurrentStreakDays(int currentStreakDays) {
        this.currentStreakDays = currentStreakDays;
    }

    public String getCurrentRank() {
        return currentRank;
    }

    public void setCurrentRank(String currentRank) {
        this.currentRank = currentRank;
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

    public long getTotalEasy() {
        return totalEasy;
    }

    public void setTotalEasy(long totalEasy) {
        this.totalEasy = totalEasy;
    }

    public long getTotalMedium() {
        return totalMedium;
    }

    public void setTotalMedium(long totalMedium) {
        this.totalMedium = totalMedium;
    }

    public long getTotalHard() {
        return totalHard;
    }

    public void setTotalHard(long totalHard) {
        this.totalHard = totalHard;
    }

    public List<Map<String, Object>> getRecentSubmissions() {
        return recentSubmissions;
    }

    public void setRecentSubmissions(List<Map<String, Object>> recentSubmissions) {
        this.recentSubmissions = recentSubmissions;
    }

    public int getLanguagesUsedCount() {
        return languagesUsedCount;
    }

    public void setLanguagesUsedCount(int languagesUsedCount) {
        this.languagesUsedCount = languagesUsedCount;
    }

    public int getCertificatesEarnedCount() {
        return certificatesEarnedCount;
    }

    public void setCertificatesEarnedCount(int certificatesEarnedCount) {
        this.certificatesEarnedCount = certificatesEarnedCount;
    }
}
