package com.oj.platform.dto;

import java.util.List;
import java.util.Map;

public class AdminDashboardStatsDto {

    private long totalUsers;
    private long activeUsers;
    private long totalProblems;
    private long totalSubmissions;
    private long acceptedSolutions;
    private long todaySubmissions;
    private double overallAcceptanceRate;
    private List<Map<String, Object>> recentSubmissions;
    private List<Map<String, Object>> recentRegistrations;

    public AdminDashboardStatsDto() {
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getActiveUsers() {
        return activeUsers;
    }

    public void setActiveUsers(long activeUsers) {
        this.activeUsers = activeUsers;
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

    public long getAcceptedSolutions() {
        return acceptedSolutions;
    }

    public void setAcceptedSolutions(long acceptedSolutions) {
        this.acceptedSolutions = acceptedSolutions;
    }

    public long getTodaySubmissions() {
        return todaySubmissions;
    }

    public void setTodaySubmissions(long todaySubmissions) {
        this.todaySubmissions = todaySubmissions;
    }

    public double getOverallAcceptanceRate() {
        return overallAcceptanceRate;
    }

    public void setOverallAcceptanceRate(double overallAcceptanceRate) {
        this.overallAcceptanceRate = overallAcceptanceRate;
    }

    public List<Map<String, Object>> getRecentSubmissions() {
        return recentSubmissions;
    }

    public void setRecentSubmissions(List<Map<String, Object>> recentSubmissions) {
        this.recentSubmissions = recentSubmissions;
    }

    public List<Map<String, Object>> getRecentRegistrations() {
        return recentRegistrations;
    }

    public void setRecentRegistrations(List<Map<String, Object>> recentRegistrations) {
        this.recentRegistrations = recentRegistrations;
    }
}
