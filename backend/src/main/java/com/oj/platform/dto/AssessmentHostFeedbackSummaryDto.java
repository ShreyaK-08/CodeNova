package com.oj.platform.dto;

import java.util.ArrayList;
import java.util.List;

public class AssessmentHostFeedbackSummaryDto {

    private Long assessmentId;
    private String assessmentTitle;
    private int totalFeedback;
    private Double averageRating;
    private List<AssessmentFeedbackDto> feedbacks = new ArrayList<>();

    public AssessmentHostFeedbackSummaryDto() {}

    public AssessmentHostFeedbackSummaryDto(Long assessmentId, String assessmentTitle, int totalFeedback, Double averageRating, List<AssessmentFeedbackDto> feedbacks) {
        this.assessmentId = assessmentId;
        this.assessmentTitle = assessmentTitle;
        this.totalFeedback = totalFeedback;
        this.averageRating = averageRating;
        this.feedbacks = feedbacks != null ? feedbacks : new ArrayList<>();
    }

    public Long getAssessmentId() {
        return assessmentId;
    }

    public void setAssessmentId(Long assessmentId) {
        this.assessmentId = assessmentId;
    }

    public String getAssessmentTitle() {
        return assessmentTitle;
    }

    public void setAssessmentTitle(String assessmentTitle) {
        this.assessmentTitle = assessmentTitle;
    }

    public int getTotalFeedback() {
        return totalFeedback;
    }

    public void setTotalFeedback(int totalFeedback) {
        this.totalFeedback = totalFeedback;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }

    public List<AssessmentFeedbackDto> getFeedbacks() {
        return feedbacks;
    }

    public void setFeedbacks(List<AssessmentFeedbackDto> feedbacks) {
        this.feedbacks = feedbacks;
    }
}
