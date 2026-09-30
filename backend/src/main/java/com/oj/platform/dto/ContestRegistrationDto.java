package com.oj.platform.dto;

import java.time.LocalDateTime;

public class ContestRegistrationDto {
    private boolean registered;
    private LocalDateTime registeredAt;
    private String attemptStatus;
    private Integer securityViolationCount;
    private Integer maxViolations;
    private Boolean terminated;
    private Integer score;
    private Integer problemsSolved;

    public ContestRegistrationDto() {
    }

    public ContestRegistrationDto(boolean registered, LocalDateTime registeredAt) {
        this.registered = registered;
        this.registeredAt = registeredAt;
    }

    public boolean isRegistered() {
        return registered;
    }

    public void setRegistered(boolean registered) {
        this.registered = registered;
    }

    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(LocalDateTime registeredAt) {
        this.registeredAt = registeredAt;
    }

    public String getAttemptStatus() {
        return attemptStatus;
    }

    public void setAttemptStatus(String attemptStatus) {
        this.attemptStatus = attemptStatus;
    }

    public Integer getSecurityViolationCount() {
        return securityViolationCount;
    }

    public void setSecurityViolationCount(Integer securityViolationCount) {
        this.securityViolationCount = securityViolationCount;
    }

    public Integer getMaxViolations() {
        return maxViolations;
    }

    public void setMaxViolations(Integer maxViolations) {
        this.maxViolations = maxViolations;
    }

    public Boolean getTerminated() {
        return terminated;
    }

    public void setTerminated(Boolean terminated) {
        this.terminated = terminated;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Integer getProblemsSolved() {
        return problemsSolved;
    }

    public void setProblemsSolved(Integer problemsSolved) {
        this.problemsSolved = problemsSolved;
    }
}
