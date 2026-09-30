package com.oj.platform.dto;

public class LanguageStatsDto {

    private String language;
    private long problemsSolved;
    private long acceptedSubmissions;
    private long totalSubmissions;
    private double acceptanceRate;

    public LanguageStatsDto() {
    }

    public LanguageStatsDto(String language, long problemsSolved, long acceptedSubmissions, long totalSubmissions, double acceptanceRate) {
        this.language = language;
        this.problemsSolved = problemsSolved;
        this.acceptedSubmissions = acceptedSubmissions;
        this.totalSubmissions = totalSubmissions;
        this.acceptanceRate = acceptanceRate;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public long getProblemsSolved() {
        return problemsSolved;
    }

    public void setProblemsSolved(long problemsSolved) {
        this.problemsSolved = problemsSolved;
    }

    public long getAcceptedSubmissions() {
        return acceptedSubmissions;
    }

    public void setAcceptedSubmissions(long acceptedSubmissions) {
        this.acceptedSubmissions = acceptedSubmissions;
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
}
