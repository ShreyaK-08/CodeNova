package com.oj.platform.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class CreateAssessmentQuestionRequest {

    @NotBlank(message = "Question text is required")
    private String questionText;

    @NotNull(message = "Question type is required")
    private String questionType;

    @NotNull(message = "Marks is required")
    @Min(value = 0, message = "Marks cannot be negative")
    private Integer marks;

    /** Optional - defaults to insertion order (1-based) if omitted. */
    private Integer orderIndex;

    private String explanation;

    /** For MCQ/TRUE_FALSE - lets a host create a question with its options in one call. */
    @Valid
    private List<CreateAssessmentOptionRequest> options;

    /** For PROGRAMMING type questions - if provided, creates/updates the linked ProgrammingQuestion. */
    @Valid
    private ProgrammingQuestionRequest programmingQuestion;

    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }

    public String getQuestionType() { return questionType; }
    public void setQuestionType(String questionType) { this.questionType = questionType; }

    public Integer getMarks() { return marks; }
    public void setMarks(Integer marks) { this.marks = marks; }

    public Integer getOrderIndex() { return orderIndex; }
    public void setOrderIndex(Integer orderIndex) { this.orderIndex = orderIndex; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public List<CreateAssessmentOptionRequest> getOptions() { return options; }
    public void setOptions(List<CreateAssessmentOptionRequest> options) { this.options = options; }

    public ProgrammingQuestionRequest getProgrammingQuestion() { return programmingQuestion; }
    public void setProgrammingQuestion(ProgrammingQuestionRequest programmingQuestion) { this.programmingQuestion = programmingQuestion; }
}
