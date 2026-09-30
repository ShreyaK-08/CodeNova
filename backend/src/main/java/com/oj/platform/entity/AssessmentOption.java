package com.oj.platform.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "assessment_options")
public class AssessmentOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    @JsonIgnoreProperties({"options"})
    private AssessmentQuestion question;

    @NotBlank
    @Column(name = "option_text", nullable = false, length = 500)
    private String optionText;

    /** Never serialized to a student while their attempt is IN_PROGRESS - see
     *  AssessmentService.toOptionDto. The frontend never decides correctness. */
    @NotNull
    @Column(name = "is_correct", nullable = false)
    private Boolean isCorrect = false;

    @NotNull
    @Column(name = "order_index", nullable = false)
    private Integer orderIndex = 1;

    public AssessmentOption() {
    }

    public AssessmentOption(AssessmentQuestion question, String optionText, Boolean isCorrect, Integer orderIndex) {
        this.question = question;
        this.optionText = optionText;
        this.isCorrect = isCorrect != null ? isCorrect : false;
        this.orderIndex = orderIndex != null ? orderIndex : 1;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AssessmentQuestion getQuestion() {
        return question;
    }

    public void setQuestion(AssessmentQuestion question) {
        this.question = question;
    }

    public String getOptionText() {
        return optionText;
    }

    public void setOptionText(String optionText) {
        this.optionText = optionText;
    }

    public Boolean getIsCorrect() {
        return isCorrect;
    }

    public void setIsCorrect(Boolean isCorrect) {
        this.isCorrect = isCorrect;
    }

    public Integer getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(Integer orderIndex) {
        this.orderIndex = orderIndex;
    }
}
