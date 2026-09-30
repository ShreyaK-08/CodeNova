package com.oj.platform.dto;

import java.util.List;

/** Achievement progress summary for the User Dashboard / Profile pages. */
public class CertificateProgressDto {
    private long solvedCount;
    private int nextMilestone;
    private int remainingToNextMilestone;
    private double progressPercent;
    private List<CertificateDto> earnedCertificates;
    /** All standard milestone tiers up to a reasonable horizon past the user's current progress, each
     *  flagged as earned/in-progress/locked, for rendering milestone cards. */
    private List<MilestoneCardDto> milestoneCards;

    public long getSolvedCount() { return solvedCount; }
    public void setSolvedCount(long solvedCount) { this.solvedCount = solvedCount; }
    public int getNextMilestone() { return nextMilestone; }
    public void setNextMilestone(int nextMilestone) { this.nextMilestone = nextMilestone; }
    public int getRemainingToNextMilestone() { return remainingToNextMilestone; }
    public void setRemainingToNextMilestone(int remainingToNextMilestone) { this.remainingToNextMilestone = remainingToNextMilestone; }
    public double getProgressPercent() { return progressPercent; }
    public void setProgressPercent(double progressPercent) { this.progressPercent = progressPercent; }
    public List<CertificateDto> getEarnedCertificates() { return earnedCertificates; }
    public void setEarnedCertificates(List<CertificateDto> earnedCertificates) { this.earnedCertificates = earnedCertificates; }
    public List<MilestoneCardDto> getMilestoneCards() { return milestoneCards; }
    public void setMilestoneCards(List<MilestoneCardDto> milestoneCards) { this.milestoneCards = milestoneCards; }

    public static class MilestoneCardDto {
        private int milestone;
        private String achievementLevel;
        private String status; // EARNED, IN_PROGRESS, LOCKED
        private String certificateNumber; // only set if earned

        public int getMilestone() { return milestone; }
        public void setMilestone(int milestone) { this.milestone = milestone; }
        public String getAchievementLevel() { return achievementLevel; }
        public void setAchievementLevel(String achievementLevel) { this.achievementLevel = achievementLevel; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getCertificateNumber() { return certificateNumber; }
        public void setCertificateNumber(String certificateNumber) { this.certificateNumber = certificateNumber; }
    }
}
