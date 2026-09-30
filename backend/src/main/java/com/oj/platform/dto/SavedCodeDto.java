package com.oj.platform.dto;

import java.time.LocalDateTime;

public class SavedCodeDto {
    private Long problemId;
    private String language;
    private String code;
    private LocalDateTime updatedAt;

    public SavedCodeDto() {
    }

    public SavedCodeDto(Long problemId, String language, String code, LocalDateTime updatedAt) {
        this.problemId = problemId;
        this.language = language;
        this.code = code;
        this.updatedAt = updatedAt;
    }

    public Long getProblemId() {
        return problemId;
    }

    public void setProblemId(Long problemId) {
        this.problemId = problemId;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
