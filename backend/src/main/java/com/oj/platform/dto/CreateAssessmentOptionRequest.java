package com.oj.platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreateAssessmentOptionRequest {

    @NotBlank(message = "Option text is required")
    private String optionText;

    @NotNull(message = "isCorrect is required")
    private Boolean isCorrect;

    /** Optional - defaults to insertion order (1-based) if omitted. */
    private Integer orderIndex;

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
