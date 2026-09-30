package com.oj.platform.dto;

import java.util.List;

public class MilestoneProgressDto {

    private long totalSolved;
    private List<MilestoneItemDto> overallMilestones;
    private List<MilestoneItemDto> languageMilestones;
    private List<CertificateDto> earnedCertificates;

    public MilestoneProgressDto() {
    }

    public MilestoneProgressDto(long totalSolved, List<MilestoneItemDto> overallMilestones,
                                List<MilestoneItemDto> languageMilestones, List<CertificateDto> earnedCertificates) {
        this.totalSolved = totalSolved;
        this.overallMilestones = overallMilestones;
        this.languageMilestones = languageMilestones;
        this.earnedCertificates = earnedCertificates;
    }

    public long getTotalSolved() {
        return totalSolved;
    }

    public void setTotalSolved(long totalSolved) {
        this.totalSolved = totalSolved;
    }

    public List<MilestoneItemDto> getOverallMilestones() {
        return overallMilestones;
    }

    public void setOverallMilestones(List<MilestoneItemDto> overallMilestones) {
        this.overallMilestones = overallMilestones;
    }

    public List<MilestoneItemDto> getLanguageMilestones() {
        return languageMilestones;
    }

    public void setLanguageMilestones(List<MilestoneItemDto> languageMilestones) {
        this.languageMilestones = languageMilestones;
    }

    public List<CertificateDto> getEarnedCertificates() {
        return earnedCertificates;
    }

    public void setEarnedCertificates(List<CertificateDto> earnedCertificates) {
        this.earnedCertificates = earnedCertificates;
    }

    public static class MilestoneItemDto {
        private String category; // "OVERALL" or "LANGUAGE"
        private String title;
        private String language;
        private int requiredCount;
        private int currentCount;
        private boolean earned;
        private double progressPercent;
        private String earnedAt;
        private String certificateNumber;
        private String verificationCode;

        public MilestoneItemDto() {
        }

        public MilestoneItemDto(String category, String title, String language, int requiredCount, int currentCount,
                                boolean earned, double progressPercent, String earnedAt, String certificateNumber, String verificationCode) {
            this.category = category;
            this.title = title;
            this.language = language;
            this.requiredCount = requiredCount;
            this.currentCount = currentCount;
            this.earned = earned;
            this.progressPercent = progressPercent;
            this.earnedAt = earnedAt;
            this.certificateNumber = certificateNumber;
            this.verificationCode = verificationCode;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getLanguage() {
            return language;
        }

        public void setLanguage(String language) {
            this.language = language;
        }

        public int getRequiredCount() {
            return requiredCount;
        }

        public void setRequiredCount(int requiredCount) {
            this.requiredCount = requiredCount;
        }

        public int getCurrentCount() {
            return currentCount;
        }

        public void setCurrentCount(int currentCount) {
            this.currentCount = currentCount;
        }

        public boolean isEarned() {
            return earned;
        }

        public void setEarned(boolean earned) {
            this.earned = earned;
        }

        public double getProgressPercent() {
            return progressPercent;
        }

        public void setProgressPercent(double progressPercent) {
            this.progressPercent = progressPercent;
        }

        public String getEarnedAt() {
            return earnedAt;
        }

        public void setEarnedAt(String earnedAt) {
            this.earnedAt = earnedAt;
        }

        public String getCertificateNumber() {
            return certificateNumber;
        }

        public void setCertificateNumber(String certificateNumber) {
            this.certificateNumber = certificateNumber;
        }

        public String getVerificationCode() {
            return verificationCode;
        }

        public void setVerificationCode(String verificationCode) {
            this.verificationCode = verificationCode;
        }
    }
}
