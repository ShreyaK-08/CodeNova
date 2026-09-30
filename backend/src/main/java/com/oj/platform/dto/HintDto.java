package com.oj.platform.dto;

public class HintDto {
    private Long id;
    private Long problemId;
    private Integer hintOrder;
    private String content;
    private Integer unlockAfterAttempts;
    private boolean unlocked;

    public HintDto() {
    }

    public HintDto(Long id, Long problemId, Integer hintOrder, String content, Integer unlockAfterAttempts, boolean unlocked) {
        this.id = id;
        this.problemId = problemId;
        this.hintOrder = hintOrder;
        this.content = content;
        this.unlockAfterAttempts = unlockAfterAttempts;
        this.unlocked = unlocked;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProblemId() {
        return problemId;
    }

    public void setProblemId(Long problemId) {
        this.problemId = problemId;
    }

    public Integer getHintOrder() {
        return hintOrder;
    }

    public void setHintOrder(Integer hintOrder) {
        this.hintOrder = hintOrder;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getUnlockAfterAttempts() {
        return unlockAfterAttempts;
    }

    public void setUnlockAfterAttempts(Integer unlockAfterAttempts) {
        this.unlockAfterAttempts = unlockAfterAttempts;
    }

    public boolean isUnlocked() {
        return unlocked;
    }

    public void setUnlocked(boolean unlocked) {
        this.unlocked = unlocked;
    }
}
