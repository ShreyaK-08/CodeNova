package com.oj.platform.dto;

/**
 * Test case DTO for HOST view — includes input and expectedOutput.
 * NEVER send this to the candidate when isHidden=true.
 */
public class ProgrammingTestCaseDto {
    private Long id;
    private String input;
    private String expectedOutput;
    private Boolean isHidden;
    private Integer orderIndex;
    private String description;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
