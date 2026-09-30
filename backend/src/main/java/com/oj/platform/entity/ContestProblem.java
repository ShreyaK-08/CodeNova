package com.oj.platform.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

/**
 * Join entity attaching an EXISTING Problem to a Contest with a display order and a
 * point value, e.g. Contest "Weekly #1" -> Problem "Two Sum" -> order 1 -> 100 points.
 * Never duplicates Problem data - only references it.
 */
@Entity
@Table(name = "contest_problems", uniqueConstraints = {
        @UniqueConstraint(name = "uq_contest_problem", columnNames = {"contest_id", "problem_id"})
})
public class ContestProblem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contest_id", nullable = false)
    private Contest contest;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    @NotNull
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @NotNull
    @Column(nullable = false)
    private Integer points;

    public ContestProblem() {
    }

    public ContestProblem(Contest contest, Problem problem, Integer displayOrder, Integer points) {
        this.contest = contest;
        this.problem = problem;
        this.displayOrder = displayOrder;
        this.points = points;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Contest getContest() {
        return contest;
    }

    public void setContest(Contest contest) {
        this.contest = contest;
    }

    public Problem getProblem() {
        return problem;
    }

    public void setProblem(Problem problem) {
        this.problem = problem;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ContestProblem that = (ContestProblem) o;
        if (id != null && that.id != null) {
            return id.equals(that.id);
        }
        Long thisContestId = (contest != null ? contest.getId() : null);
        Long thatContestId = (that.contest != null ? that.contest.getId() : null);
        Long thisProblemId = (problem != null ? problem.getId() : null);
        Long thatProblemId = (that.problem != null ? that.problem.getId() : null);
        return java.util.Objects.equals(thisContestId, thatContestId) &&
               java.util.Objects.equals(thisProblemId, thatProblemId);
    }

    @Override
    public int hashCode() {
        if (id != null) {
            return id.hashCode();
        }
        Long thisContestId = (contest != null ? contest.getId() : null);
        Long thisProblemId = (problem != null ? problem.getId() : null);
        return java.util.Objects.hash(thisContestId, thisProblemId);
    }
}
