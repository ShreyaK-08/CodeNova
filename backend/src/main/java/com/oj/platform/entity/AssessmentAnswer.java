package com.oj.platform.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

/**
 * One student's recorded answer to one AssessmentQuestion within one AssessmentAttempt.
 * isCorrect/marksAwarded are null until the attempt is submitted (see
 * AssessmentService.submitAttempt) - recording an answer never grades it immediately,
 * so changing an answer before final submit never requires "un-grading" logic.
 * A unique constraint on (attempt_id, question_id) backs the service-level upsert
 * (find-or-create) that prevents duplicate answer rows for the same question.
 */
@Entity
@Table(name = "assessment_answers", uniqueConstraints = {
        @UniqueConstraint(name = "uq_assessment_answer_attempt_question", columnNames = {"attempt_id", "question_id"})
})
public class AssessmentAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attempt_id", nullable = false)
    @JsonIgnoreProperties({"assessment", "user"})
    private AssessmentAttempt attempt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    @JsonIgnoreProperties({"assessment", "options"})
    private AssessmentQuestion question;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "selected_option_id", nullable = false)
    @JsonIgnoreProperties({"question"})
    private AssessmentOption selectedOption;

    /** Defaults to false initially; calculated and updated when the attempt is submitted. */
    @Column(name = "is_correct", nullable = false)
    private Boolean isCorrect = false;

    /** Defaults to 0 initially; calculated and updated when the attempt is submitted. */
    @Column(name = "marks_awarded")
    private Integer marksAwarded = 0;

    public AssessmentAnswer() {
        this.isCorrect = false;
        this.marksAwarded = 0;
    }

    public AssessmentAnswer(AssessmentAttempt attempt, AssessmentQuestion question, AssessmentOption selectedOption) {
        this.attempt = attempt;
        this.question = question;
        this.selectedOption = selectedOption;
        this.isCorrect = false;
        this.marksAwarded = 0;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AssessmentAttempt getAttempt() {
        return attempt;
    }

    public void setAttempt(AssessmentAttempt attempt) {
        this.attempt = attempt;
    }

    public AssessmentQuestion getQuestion() {
        return question;
    }

    public void setQuestion(AssessmentQuestion question) {
        this.question = question;
    }

    public AssessmentOption getSelectedOption() {
        return selectedOption;
    }

    public void setSelectedOption(AssessmentOption selectedOption) {
        this.selectedOption = selectedOption;
    }

    public Boolean getIsCorrect() {
        return isCorrect;
    }

    public void setIsCorrect(Boolean isCorrect) {
        this.isCorrect = isCorrect;
    }

    public Integer getMarksAwarded() {
        return marksAwarded;
    }

    public void setMarksAwarded(Integer marksAwarded) {
        this.marksAwarded = marksAwarded;
    }
}
