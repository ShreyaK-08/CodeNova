package com.oj.platform.dto;

import com.oj.platform.entity.Difficulty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;
import java.util.List;

public class ProblemDto {

    private Long id;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Difficulty is required")
    private Difficulty difficulty;

    private String topic;
    private String constraints;
    private String starterCode;
    private String starterCodePython;
    private String starterCodeCpp;
    private String starterCodeJs;
    private String methodName;
    private Integer timeLimitMs = 1000;
    private Integer memoryLimitMb = 256;
    private String editorialTitle;
    private String editorialApproach;
    private String editorialAlgorithm;
    private String editorialComplexity;
    private String editorialSolution;
    private Integer editorialUnlockAttempts = 3;
    private Boolean hasEditorial = false;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<TestCaseDto> testCases;
    private List<HintDto> hints;

    // ── Problem browser stats (populated from Submission table) ──────────
    private Long totalSubmissions;
    private Long acceptedSubmissions;
    private Double acceptanceRate; // 0.0–100.0, null if no submissions
    private Boolean isSolved;      // true if current user has at least one ACCEPTED submission


    public ProblemDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getConstraints() {
        return constraints;
    }

    public void setConstraints(String constraints) {
        this.constraints = constraints;
    }

    public String getStarterCode() {
        return starterCode;
    }

    public void setStarterCode(String starterCode) {
        this.starterCode = starterCode;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Integer getTimeLimitMs() {
        return timeLimitMs;
    }

    public void setTimeLimitMs(Integer timeLimitMs) {
        this.timeLimitMs = timeLimitMs;
    }

    public Integer getMemoryLimitMb() {
        return memoryLimitMb;
    }

    public void setMemoryLimitMb(Integer memoryLimitMb) {
        this.memoryLimitMb = memoryLimitMb;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<TestCaseDto> getTestCases() {
        return testCases;
    }

    public void setTestCases(List<TestCaseDto> testCases) {
        this.testCases = testCases;
    }

    public String getStarterCodePython() {
        return starterCodePython;
    }

    public void setStarterCodePython(String starterCodePython) {
        this.starterCodePython = starterCodePython;
    }

    public String getStarterCodeCpp() {
        return starterCodeCpp;
    }

    public void setStarterCodeCpp(String starterCodeCpp) {
        this.starterCodeCpp = starterCodeCpp;
    }

    public String getStarterCodeJs() {
        return starterCodeJs;
    }

    public void setStarterCodeJs(String starterCodeJs) {
        this.starterCodeJs = starterCodeJs;
    }

    public String getEditorialTitle() {
        return editorialTitle;
    }

    public void setEditorialTitle(String editorialTitle) {
        this.editorialTitle = editorialTitle;
    }

    public String getEditorialApproach() {
        return editorialApproach;
    }

    public void setEditorialApproach(String editorialApproach) {
        this.editorialApproach = editorialApproach;
    }

    public String getEditorialAlgorithm() {
        return editorialAlgorithm;
    }

    public void setEditorialAlgorithm(String editorialAlgorithm) {
        this.editorialAlgorithm = editorialAlgorithm;
    }

    public String getEditorialComplexity() {
        return editorialComplexity;
    }

    public void setEditorialComplexity(String editorialComplexity) {
        this.editorialComplexity = editorialComplexity;
    }

    public String getEditorialSolution() {
        return editorialSolution;
    }

    public void setEditorialSolution(String editorialSolution) {
        this.editorialSolution = editorialSolution;
    }

    public Integer getEditorialUnlockAttempts() {
        return editorialUnlockAttempts;
    }

    public void setEditorialUnlockAttempts(Integer editorialUnlockAttempts) {
        this.editorialUnlockAttempts = editorialUnlockAttempts;
    }

    public Boolean getHasEditorial() {
        return hasEditorial;
    }

    public void setHasEditorial(Boolean hasEditorial) {
        this.hasEditorial = hasEditorial;
    }

    public List<HintDto> getHints() {
        return hints;
    }

    public void setHints(List<HintDto> hints) {
        this.hints = hints;
    }

    public Long getTotalSubmissions() { return totalSubmissions; }
    public void setTotalSubmissions(Long totalSubmissions) { this.totalSubmissions = totalSubmissions; }

    public Long getAcceptedSubmissions() { return acceptedSubmissions; }
    public void setAcceptedSubmissions(Long acceptedSubmissions) { this.acceptedSubmissions = acceptedSubmissions; }

    public Double getAcceptanceRate() { return acceptanceRate; }
    public void setAcceptanceRate(Double acceptanceRate) { this.acceptanceRate = acceptanceRate; }

    public Boolean getIsSolved() { return isSolved; }
    public void setIsSolved(Boolean isSolved) { this.isSolved = isSolved; }
}
