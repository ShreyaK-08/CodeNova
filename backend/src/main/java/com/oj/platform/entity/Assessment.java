package com.oj.platform.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A skill assessment (quiz) made up of AssessmentQuestions — MCQ, TRUE_FALSE, and/or
 * PROGRAMMING types.  Hosted by a verified non-admin user or created by an admin.
 *
 * New fields added for the 30-requirement upgrade:
 *  - maxAttempts       : how many times a candidate may start this assessment (default 1)
 *  - startTime/endTime : optional availability window
 *  - negativeMarkingEnabled / negativeMarkingValue : per-wrong-MCQ penalty
 *  - requireCandidateDetails : whether the pre-attempt form is shown
 *  - candidateFieldsConfig   : JSON list of fields to collect e.g. ["fullName","email","organization"]
 */
@Entity
@Table(name = "assessments")
public class Assessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String instructions;

    @NotNull
    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    /** Always server-recomputed as the sum of its questions' marks. */
    @NotNull
    @Column(name = "total_marks", nullable = false)
    private Integer totalMarks = 0;

    @NotNull
    @Column(name = "passing_marks", nullable = false)
    private Integer passingMarks = 0;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AssessmentStatus status = AssessmentStatus.DRAFT;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    @JsonIgnoreProperties({"password", "email"})
    private User createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ── New fields ─────────────────────────────────────────────────────────

    /** Maximum number of attempts a candidate may make.  Default 1 (single attempt). */
    @Column(name = "max_attempts", nullable = false)
    private Integer maxAttempts = 1;

    /** Optional: assessment only available from this time. Null = no restriction. */
    @Column(name = "start_time")
    private LocalDateTime startTime;

    /** Optional: assessment only available until this time. Null = no restriction. */
    @Column(name = "end_time")
    private LocalDateTime endTime;

    /** If true, wrong MCQ answers deduct marks. */
    @Column(name = "negative_marking_enabled", nullable = false)
    private Boolean negativeMarkingEnabled = false;

    /** Marks to deduct per wrong MCQ answer (only relevant when negativeMarkingEnabled=true). */
    @Column(name = "negative_marking_value")
    private Double negativeMarkingValue = 0.0;

    /** Whether to show the candidate-details form before starting. */
    @Column(name = "require_candidate_details", nullable = false)
    private Boolean requireCandidateDetails = false;

    /**
     * JSON array of field names to collect, e.g.:
     *   ["fullName","email","organization","registrationId","phone"]
     * Stored as plain TEXT; parsed in the service/DTO layer.
     */
    @Column(name = "candidate_fields_config", columnDefinition = "TEXT")
    private String candidateFieldsConfig;

    @OneToMany(mappedBy = "assessment", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("assessment")
    private List<AssessmentQuestion> questions = new ArrayList<>();

    public Assessment() {}

    public Assessment(String title, String description, String instructions,
                       Integer durationMinutes, Integer passingMarks, User createdBy) {
        this.title = title;
        this.description = description;
        this.instructions = instructions;
        this.durationMinutes = durationMinutes;
        this.passingMarks = passingMarks != null ? passingMarks : 0;
        this.createdBy = createdBy;
        this.status = AssessmentStatus.DRAFT;
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ── Getters / Setters ────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }

    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }

    public Integer getTotalMarks() { return totalMarks; }
    public void setTotalMarks(Integer totalMarks) { this.totalMarks = totalMarks; }

    public Integer getPassingMarks() { return passingMarks; }
    public void setPassingMarks(Integer passingMarks) { this.passingMarks = passingMarks; }

    public AssessmentStatus getStatus() { return status; }
    public void setStatus(AssessmentStatus status) { this.status = status; }

    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public Integer getMaxAttempts() { return maxAttempts; }
    public void setMaxAttempts(Integer maxAttempts) { this.maxAttempts = maxAttempts != null ? maxAttempts : 1; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public Boolean getNegativeMarkingEnabled() { return negativeMarkingEnabled; }
    public void setNegativeMarkingEnabled(Boolean negativeMarkingEnabled) { this.negativeMarkingEnabled = Boolean.TRUE.equals(negativeMarkingEnabled); }

    public Double getNegativeMarkingValue() { return negativeMarkingValue; }
    public void setNegativeMarkingValue(Double negativeMarkingValue) { this.negativeMarkingValue = negativeMarkingValue; }

    public Boolean getRequireCandidateDetails() { return requireCandidateDetails; }
    public void setRequireCandidateDetails(Boolean requireCandidateDetails) { this.requireCandidateDetails = Boolean.TRUE.equals(requireCandidateDetails); }

    public String getCandidateFieldsConfig() { return candidateFieldsConfig; }
    public void setCandidateFieldsConfig(String candidateFieldsConfig) { this.candidateFieldsConfig = candidateFieldsConfig; }

    public List<AssessmentQuestion> getQuestions() { return questions; }
    public void setQuestions(List<AssessmentQuestion> questions) { this.questions = questions; }
}
