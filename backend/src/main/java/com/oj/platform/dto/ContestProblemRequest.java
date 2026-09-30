package com.oj.platform.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;

public class ContestProblemRequest {

    @NotNull(message = "Problem ID is required")
    private Long problemId;

    @NotNull(message = "Display order is required")
    @Min(value = 1, message = "Display order must be at least 1")
    private Integer displayOrder;

    @NotNull(message = "Points is required")
    @Min(value = 0, message = "Points cannot be negative")
    private Integer points;

    public ContestProblemRequest() {
    }

    public Long getProblemId() {
        return problemId;
    }

    public void setProblemId(Long problemId) {
        this.problemId = problemId;
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
