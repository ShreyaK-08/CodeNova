package com.oj.platform.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "problems")
public class Problem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 200)
    private String title;

    @NotBlank
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Difficulty difficulty = Difficulty.EASY;

    @Column(length = 100)
    private String topic;

    /**
     * Free-form constraints text for the problem, e.g.
     * "2 <= nums.length <= 10^4\n-10^9 <= nums[i] <= 10^9". Stored as-is (including
     * line breaks/bullets) and rendered with preserved whitespace on the frontend.
     * Optional: existing problems created before this field existed simply have
     * this as null, which is handled gracefully everywhere it's read.
     */
    @Column(columnDefinition = "TEXT")
    private String constraints;

    @Column(columnDefinition = "TEXT")
    private String starterCode;

    @Column(columnDefinition = "TEXT")
    private String starterCodePython;

    @Column(columnDefinition = "TEXT")
    private String starterCodeCpp;

    @Column(columnDefinition = "TEXT")
    private String starterCodeJs;

    @Column(length = 200)
    private String editorialTitle;

    @Column(columnDefinition = "TEXT")
    private String editorialApproach;

    @Column(columnDefinition = "TEXT")
    private String editorialAlgorithm;

    @Column(columnDefinition = "TEXT")
    private String editorialComplexity;

    @Column(columnDefinition = "TEXT")
    private String editorialSolution;

    @Column(nullable = false)
    private Integer editorialUnlockAttempts = 3;

    /**
     * Name of the method the user's Solution class must implement, e.g. "twoSum".
     * When set, Java submissions are compiled as a plain Solution class (no main()
     * required) and run against a backend-generated Main.java driver that calls
     * this method for each test case. When null/blank, Java submissions fall back
     * to the older "complete runnable program" mode (user code must have its own main()).
     */
    @Column(length = 100)
    private String methodName;

    private Integer timeLimitMs = 1000;

    private Integer memoryLimitMb = 256;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "problem", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("problem")
    private List<TestCase> testCases = new ArrayList<>();

    @OneToMany(mappedBy = "problem", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("problem")
    @OrderBy("hintOrder ASC")
    private List<Hint> hints = new ArrayList<>();

    public Problem() {
    }

    public Problem(String title, String description, Difficulty difficulty, String topic, String starterCode) {
        this.title = title;
        this.description = description;
        this.difficulty = difficulty;
        this.topic = topic;
        this.starterCode = starterCode;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters

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

    public List<TestCase> getTestCases() {
        return testCases;
    }

    public void setTestCases(List<TestCase> testCases) {
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

    public List<Hint> getHints() {
        return hints;
    }

    public void setHints(List<Hint> hints) {
        this.hints = hints;
    }
}
