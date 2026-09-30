package com.oj.platform.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * A user's most recently saved code for a given problem + language - separate from
 * Submission (which records permanent, immutable submission history). There is at most
 * ONE SavedCode row per (user, problem, language); it is created on first save and
 * overwritten (code + updatedAt) on every subsequent save, whether that save comes from
 * an explicit "save" action or automatically from a Run/Submit.
 */
@Entity
@Table(name = "saved_codes", uniqueConstraints = {
        @UniqueConstraint(name = "uq_saved_code_user_problem_language",
                columnNames = {"user_id", "problem_id", "language"})
})
public class SavedCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    @NotBlank
    @Column(nullable = false, length = 50)
    private String language;

    @NotBlank
    @Column(nullable = false, columnDefinition = "TEXT")
    private String code;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public SavedCode() {
    }

    public SavedCode(User user, Problem problem, String language, String code) {
        this.user = user;
        this.problem = problem;
        this.language = language;
        this.code = code;
    }

    @PrePersist
    @PreUpdate
    protected void touch() {
        this.updatedAt = LocalDateTime.now();
    }

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

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
