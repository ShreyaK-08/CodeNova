package com.oj.platform.dto;

/**
 * Request DTO for adding or updating a programming test case.
 */
public class ProgrammingTestCaseRequest {
    private String input;
    private String expectedOutput;
    private Boolean isHidden = false;
    private Integer orderIndex;
    private String description;

    public String getInput() { return input; }
    public void setInput(String input) { this.input = input; }
    public String getExpectedOutput() { return expectedOutput; }
    public void setExpectedOutput(String expectedOutput) { this.expectedOutput = expectedOutput; }
    public Boolean getIsHidden() { return isHidden; }
    public void setIsHidden(Boolean isHidden) { this.isHidden = isHidden; }
    public Integer getOrderIndex() { return orderIndex; }
    public void setOrderIndex(Integer orderIndex) { this.orderIndex = orderIndex; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
