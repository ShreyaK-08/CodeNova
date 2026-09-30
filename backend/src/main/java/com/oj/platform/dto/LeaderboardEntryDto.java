package com.oj.platform.dto;

public class LeaderboardEntryDto {

    private int rank;
    private Long userId;
    private String username;
    private String name;
    private long problemsSolved;
    private long totalAccepted;
    private long totalSubmissions;
    private double acceptanceRate;
    private int score;
    private Long fastestExecutionTimeMs;
    private long easySolved;
    private long mediumSolved;
    private long hardSolved;
    private int certificatesCount;

    public LeaderboardEntryDto() {
    }

    public LeaderboardEntryDto(int rank, Long userId, String username, String name,
                               long problemsSolved, long totalAccepted, long totalSubmissions,
                               double acceptanceRate, int score) {
        this.rank = rank;
        this.userId = userId;
        this.username = username;
        this.name = name;
        this.problemsSolved = problemsSolved;
        this.totalAccepted = totalAccepted;
        this.totalSubmissions = totalSubmissions;
        this.acceptanceRate = acceptanceRate;
        this.score = score;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
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

    public long getProblemsSolved() {
        return problemsSolved;
    }

    public void setProblemsSolved(long problemsSolved) {
        this.problemsSolved = problemsSolved;
    }

    public long getTotalAccepted() {
        return totalAccepted;
    }

    public void setTotalAccepted(long totalAccepted) {
        this.totalAccepted = totalAccepted;
    }

    public long getTotalSubmissions() {
        return totalSubmissions;
    }

    public void setTotalSubmissions(long totalSubmissions) {
        this.totalSubmissions = totalSubmissions;
    }

    public double getAcceptanceRate() {
        return acceptanceRate;
    }

    public void setAcceptanceRate(double acceptanceRate) {
        this.acceptanceRate = acceptanceRate;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public Long getFastestExecutionTimeMs() {
        return fastestExecutionTimeMs;
    }

    public void setFastestExecutionTimeMs(Long fastestExecutionTimeMs) {
        this.fastestExecutionTimeMs = fastestExecutionTimeMs;
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

    public int getCertificatesCount() {
        return certificatesCount;
    }

    public void setCertificatesCount(int certificatesCount) {
        this.certificatesCount = certificatesCount;
    }
}
