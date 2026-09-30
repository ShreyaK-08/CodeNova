package com.oj.platform.dto;

public class AssessmentOptionDto {
    private Long id;
    private String optionText;
    private Integer orderIndex;
    /** Null means "hidden for this request" (e.g. a student's IN_PROGRESS attempt) -
     *  never defaulted to false, which would be misleading rather than absent. */
    private Boolean isCorrect;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOptionText() {
        return optionText;
    }

    public void setOptionText(String optionText) {
        this.optionText = optionText;
    }

    public Integer getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(Integer orderIndex) {
        this.orderIndex = orderIndex;
    }

    public Boolean getIsCorrect() {
        return isCorrect;
    }

    public void setIsCorrect(Boolean isCorrect) {
        this.isCorrect = isCorrect;
    }
}
