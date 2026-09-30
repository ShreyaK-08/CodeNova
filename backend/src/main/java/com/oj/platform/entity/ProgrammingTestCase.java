package com.oj.platform.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * One test case for a ProgrammingQuestion used in an assessment.
 *
 * Security rule: hidden test cases (isHidden=true) must NEVER be sent to the
 * candidate frontend — only the pass/fail count is exposed (see AssessmentService).
 */
@Entity
@Table(name = "programming_test_cases")
public class ProgrammingTestCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "programming_question_id", nullable = false)
    @JsonIgnoreProperties("testCases")
    private ProgrammingQuestion programmingQuestion;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String input;

    @Column(name = "expected_output", nullable = false, columnDefinition = "TEXT")
    private String expectedOutput;

    /** If true, input and expectedOutput are NEVER sent to the candidate. */
    @Column(name = "is_hidden", nullable = false)
    private Boolean isHidden = false;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex = 1;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { this.createdAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ProgrammingQuestion getProgrammingQuestion() { return programmingQuestion; }
    public void setProgrammingQuestion(ProgrammingQuestion programmingQuestion) { this.programmingQuestion = programmingQuestion; }

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

    public LocalDateTime getCreatedAt() { return createdAt; }
}
