package com.oj.platform.dto;

/**
 * Programming question data returned to the HOST (includes all fields).
 * For the CANDIDATE, use ProgrammingQuestionCandidateDto which omits hidden tests.
 */
public class ProgrammingQuestionDto {
    private Long id;
    private String problemStatement;
    private String inputFormat;
    private String outputFormat;
    private String constraints;
    private String sampleInput;
    private String sampleOutput;
    private String supportedLanguages;
    private String starterCodeJava;
    private String starterCodePython;
    private String starterCodeCpp;
    private String starterCodeJavascript;
    private Integer timeLimitMs;
    private Integer memoryLimitMb;
    private int totalTestCases;
    private int hiddenTestCases;
    private int sampleTestCases;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getProblemStatement() { return problemStatement; }
    public void setProblemStatement(String s) { this.problemStatement = s; }
    public String getInputFormat() { return inputFormat; }
    public void setInputFormat(String s) { this.inputFormat = s; }
    public String getOutputFormat() { return outputFormat; }
    public void setOutputFormat(String s) { this.outputFormat = s; }
    public String getConstraints() { return constraints; }
    public void setConstraints(String s) { this.constraints = s; }
    public String getSampleInput() { return sampleInput; }
    public void setSampleInput(String s) { this.sampleInput = s; }
    public String getSampleOutput() { return sampleOutput; }
    public void setSampleOutput(String s) { this.sampleOutput = s; }
    public String getSupportedLanguages() { return supportedLanguages; }
    public void setSupportedLanguages(String s) { this.supportedLanguages = s; }
    public String getStarterCodeJava() { return starterCodeJava; }
    public void setStarterCodeJava(String s) { this.starterCodeJava = s; }
    public String getStarterCodePython() { return starterCodePython; }
    public void setStarterCodePython(String s) { this.starterCodePython = s; }
    public String getStarterCodeCpp() { return starterCodeCpp; }
    public void setStarterCodeCpp(String s) { this.starterCodeCpp = s; }
    public String getStarterCodeJavascript() { return starterCodeJavascript; }
    public void setStarterCodeJavascript(String s) { this.starterCodeJavascript = s; }
    public Integer getTimeLimitMs() { return timeLimitMs; }
    public void setTimeLimitMs(Integer i) { this.timeLimitMs = i; }
    public Integer getMemoryLimitMb() { return memoryLimitMb; }
    public void setMemoryLimitMb(Integer i) { this.memoryLimitMb = i; }
    public int getTotalTestCases() { return totalTestCases; }
    public void setTotalTestCases(int i) { this.totalTestCases = i; }
    public int getHiddenTestCases() { return hiddenTestCases; }
    public void setHiddenTestCases(int i) { this.hiddenTestCases = i; }
    public int getSampleTestCases() { return sampleTestCases; }
    public void setSampleTestCases(int i) { this.sampleTestCases = i; }
}
