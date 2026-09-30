package com.oj.platform.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "hints")
public class Hint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    @JsonIgnoreProperties("hints")
    private Problem problem;

    @NotNull
    @Column(nullable = false)
    private Integer hintOrder = 1;

    @NotBlank
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * Number of failed attempts required by a user on this problem before this hint unlocks.
     * E.g. Hint 1: 1, Hint 2: 2, Hint 3: 3.
     */
    @NotNull
    @Column(nullable = false)
    private Integer unlockAfterAttempts = 1;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Hint() {
    }

    public Hint(Problem problem, Integer hintOrder, String content, Integer unlockAfterAttempts) {
        this.problem = problem;
        this.hintOrder = hintOrder != null ? hintOrder : 1;
        this.content = content;
        this.unlockAfterAttempts = unlockAfterAttempts != null ? unlockAfterAttempts : 1;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Problem getProblem() {
        return problem;
    }

    public void setProblem(Problem problem) {
        this.problem = problem;
    }

    public Integer getHintOrder() {
        return hintOrder;
    }

    public void setHintOrder(Integer hintOrder) {
        this.hintOrder = hintOrder;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getUnlockAfterAttempts() {
        return unlockAfterAttempts;
    }

    public void setUnlockAfterAttempts(Integer unlockAfterAttempts) {
        this.unlockAfterAttempts = unlockAfterAttempts;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
