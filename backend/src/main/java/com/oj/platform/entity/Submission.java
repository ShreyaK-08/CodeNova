package com.oj.platform.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "submissions")
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"password", "bio", "skills", "githubUrl", "linkedinUrl", "avatarUrl"})
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    @JsonIgnoreProperties("testCases")
    private Problem problem;

    /**
     * Nullable reference to the Contest this submission was made under (Task 8).
     * Null for every normal /problems submission - only set when the submission came
     * through the contest coding screen with a contestId. This is the ONLY schema
     * change needed to associate a submission with a contest; no separate
     * ContestSubmission table was created.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contest_id", nullable = true)
    @JsonIgnoreProperties({"contestProblems", "createdBy"})
    private Contest contest;

    @NotBlank
    @Column(nullable = false, length = 50)
    private String language;

    @NotBlank
    @Column(nullable = false, columnDefinition = "TEXT")
    private String code;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubmissionStatus status = SubmissionStatus.PENDING;

    private Long executionTime;

    private Long memoryUsed;

    private Integer passedTestCases = 0;

    private Integer totalTestCases = 0;

    private Integer failedTestCases = 0;

    @Column(nullable = false, updatable = false)
    private LocalDateTime submittedAt;

    @OneToMany(mappedBy = "submission", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("submission")
    private List<SubmissionResult> results = new ArrayList<>();

    public Submission() {
    }

    public Submission(User user, Problem problem, String language, String code) {
        this.user = user;
        this.problem = problem;
        this.language = language;
        this.code = code;
        this.status = SubmissionStatus.PENDING;
    }

    @PrePersist
    protected void onCreate() {
        this.submittedAt = LocalDateTime.now();
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Problem getProblem() {
        return problem;
    }

    public void setProblem(Problem problem) {
        this.problem = problem;
    }

    public Contest getContest() {
        return contest;
    }

    public void setContest(Contest contest) {
        this.contest = contest;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public SubmissionStatus getStatus() {
        return status;
    }

    public void setStatus(SubmissionStatus status) {
        this.status = status;
    }

    public Long getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(Long executionTime) {
        this.executionTime = executionTime;
    }

    public Long getMemoryUsed() {
        return memoryUsed;
    }

    public void setMemoryUsed(Long memoryUsed) {
        this.memoryUsed = memoryUsed;
    }

    public Integer getPassedTestCases() {
        return passedTestCases;
    }

    public void setPassedTestCases(Integer passedTestCases) {
        this.passedTestCases = passedTestCases;
    }

    public Integer getTotalTestCases() {
        return totalTestCases;
    }

    public void setTotalTestCases(Integer totalTestCases) {
        this.totalTestCases = totalTestCases;
    }

    public Integer getFailedTestCases() {
        if (failedTestCases != null) {
            return failedTestCases;
        }
        if (totalTestCases != null && passedTestCases != null) {
            return Math.max(0, totalTestCases - passedTestCases);
        }
        return 0;
    }

    public void setFailedTestCases(Integer failedTestCases) {
        this.failedTestCases = failedTestCases;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public List<SubmissionResult> getResults() {
        return results;
    }

    public void setResults(List<SubmissionResult> results) {
        this.results = results;
    }
}
