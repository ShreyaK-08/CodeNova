package com.oj.platform.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * One question in an assessment.  Supports MCQ, TRUE_FALSE, and PROGRAMMING types.
 * For PROGRAMMING questions, programmingQuestion holds the full problem spec + test cases.
 * For MCQ / TRUE_FALSE, the options list holds the answer choices.
 */
@Entity
@Table(name = "assessment_questions")
public class AssessmentQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assessment_id", nullable = false)
    @JsonIgnoreProperties({"questions"})
    private Assessment assessment;

    @NotBlank
    @Column(name = "question_text", nullable = false, columnDefinition = "TEXT")
    private String questionText;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", nullable = false, length = 20)
    private AssessmentQuestionType questionType;

    @NotNull
    @Column(nullable = false)
    private Integer marks = 0;

    @NotNull
    @Column(name = "order_index", nullable = false)
    private Integer orderIndex = 1;

    /** Shown to candidates only once their attempt is COMPLETED/EXPIRED. */
    @Column(columnDefinition = "TEXT")
    private String explanation;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Populated when questionType = PROGRAMMING. */
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "programming_question_id")
    private ProgrammingQuestion programmingQuestion;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("question")
    private List<AssessmentOption> options = new ArrayList<>();

    public AssessmentQuestion() {}

    public AssessmentQuestion(Assessment assessment, String questionText, AssessmentQuestionType questionType,
                               Integer marks, Integer orderIndex, String explanation) {
        this.assessment = assessment;
        this.questionText = questionText;
        this.questionType = questionType;
        this.marks = marks != null ? marks : 0;
        this.orderIndex = orderIndex != null ? orderIndex : 1;
        this.explanation = explanation;
    }

    @PrePersist
    protected void onCreate() { this.createdAt = LocalDateTime.now(); }

    // ── Getters / Setters ────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Assessment getAssessment() { return assessment; }
    public void setAssessment(Assessment assessment) { this.assessment = assessment; }

    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }

    public AssessmentQuestionType getQuestionType() { return questionType; }
    public void setQuestionType(AssessmentQuestionType questionType) { this.questionType = questionType; }

    public Integer getMarks() { return marks; }
    public void setMarks(Integer marks) { this.marks = marks; }

    public Integer getOrderIndex() { return orderIndex; }
    public void setOrderIndex(Integer orderIndex) { this.orderIndex = orderIndex; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public ProgrammingQuestion getProgrammingQuestion() { return programmingQuestion; }
    public void setProgrammingQuestion(ProgrammingQuestion programmingQuestion) { this.programmingQuestion = programmingQuestion; }

    public List<AssessmentOption> getOptions() { return options; }
    public void setOptions(List<AssessmentOption> options) { this.options = options; }
}
