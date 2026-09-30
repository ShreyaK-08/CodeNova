package com.oj.platform.dto;

public class ContestProblemDto {
    private Long problemId;
    private String problemTitle;
    private String difficulty;
    private Integer displayOrder;
    private Integer points;

    public ContestProblemDto() {
    }

    public ContestProblemDto(Long problemId, String problemTitle, String difficulty, Integer displayOrder, Integer points) {
        this.problemId = problemId;
        this.problemTitle = problemTitle;
        this.difficulty = difficulty;
        this.displayOrder = displayOrder;
        this.points = points;
    }

    public Long getProblemId() {
        return problemId;
    }

    public void setProblemId(Long problemId) {
        this.problemId = problemId;
    }

    public String getProblemTitle() {
        return problemTitle;
    }

    public void setProblemTitle(String problemTitle) {
        this.problemTitle = problemTitle;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }
}
