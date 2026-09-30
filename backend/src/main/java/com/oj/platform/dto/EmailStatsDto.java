package com.oj.platform.dto;

import java.util.Map;

public class EmailStatsDto {
    private long totalLogged;
    private long totalSent;
    private long totalFailed;
    private long totalNotConfigured;
    private long totalSkipped;
    private double successRate;
    private boolean smtpConfigured;
    private String mailHost;
    private String mailFrom;
    private Map<String, Long> typeBreakdown;

    public EmailStatsDto() {
    }

    public EmailStatsDto(long totalLogged, long totalSent, long totalFailed, long totalNotConfigured,
                         long totalSkipped, double successRate, boolean smtpConfigured,
                         String mailHost, String mailFrom, Map<String, Long> typeBreakdown) {
        this.totalLogged = totalLogged;
        this.totalSent = totalSent;
        this.totalFailed = totalFailed;
        this.totalNotConfigured = totalNotConfigured;
        this.totalSkipped = totalSkipped;
        this.successRate = successRate;
        this.smtpConfigured = smtpConfigured;
        this.mailHost = mailHost;
        this.mailFrom = mailFrom;
        this.typeBreakdown = typeBreakdown;
    }

    public long getTotalLogged() {
        return totalLogged;
    }

    public void setTotalLogged(long totalLogged) {
        this.totalLogged = totalLogged;
    }

    public long getTotalSent() {
        return totalSent;
    }

    public void setTotalSent(long totalSent) {
        this.totalSent = totalSent;
    }

    public long getTotalFailed() {
        return totalFailed;
    }

    public void setTotalFailed(long totalFailed) {
        this.totalFailed = totalFailed;
    }

    public long getTotalNotConfigured() {
        return totalNotConfigured;
    }

    public void setTotalNotConfigured(long totalNotConfigured) {
        this.totalNotConfigured = totalNotConfigured;
    }

    public long getTotalSkipped() {
        return totalSkipped;
    }

    public void setTotalSkipped(long totalSkipped) {
        this.totalSkipped = totalSkipped;
    }

    public double getSuccessRate() {
        return successRate;
    }

    public void setSuccessRate(double successRate) {
        this.successRate = successRate;
    }

    public boolean isSmtpConfigured() {
        return smtpConfigured;
    }

    public void setSmtpConfigured(boolean smtpConfigured) {
        this.smtpConfigured = smtpConfigured;
    }

    public String getMailHost() {
        return mailHost;
    }

    public void setMailHost(String mailHost) {
        this.mailHost = mailHost;
    }

    public String getMailFrom() {
        return mailFrom;
    }

    public void setMailFrom(String mailFrom) {
        this.mailFrom = mailFrom;
    }

    public Map<String, Long> getTypeBreakdown() {
        return typeBreakdown;
    }

    public void setTypeBreakdown(Map<String, Long> typeBreakdown) {
        this.typeBreakdown = typeBreakdown;
    }
}
