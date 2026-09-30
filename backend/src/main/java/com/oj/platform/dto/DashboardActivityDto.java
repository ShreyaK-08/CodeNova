package com.oj.platform.dto;

import java.time.LocalDate;
import java.util.List;

public class DashboardActivityDto {

    private int totalSubmissions;
    private int activeDays;
    private int currentStreak;
    private int longestStreak;
    private List<DailyActivityItem> days;

    public DashboardActivityDto() {
    }

    public DashboardActivityDto(int totalSubmissions, int activeDays, int currentStreak, int longestStreak, List<DailyActivityItem> days) {
        this.totalSubmissions = totalSubmissions;
        this.activeDays = activeDays;
        this.currentStreak = currentStreak;
        this.longestStreak = longestStreak;
        this.days = days;
    }

    public int getTotalSubmissions() {
        return totalSubmissions;
    }

    public void setTotalSubmissions(int totalSubmissions) {
        this.totalSubmissions = totalSubmissions;
    }

    public int getActiveDays() {
        return activeDays;
    }

    public void setActiveDays(int activeDays) {
        this.activeDays = activeDays;
    }

    public int getCurrentStreak() {
        return currentStreak;
    }

    public void setCurrentStreak(int currentStreak) {
        this.currentStreak = currentStreak;
    }

    public int getLongestStreak() {
        return longestStreak;
    }

    public void setLongestStreak(int longestStreak) {
        this.longestStreak = longestStreak;
    }

    public List<DailyActivityItem> getDays() {
        return days;
    }

    public void setDays(List<DailyActivityItem> days) {
        this.days = days;
    }

    public static class DailyActivityItem {
        private String date; // YYYY-MM-DD
        private int submissions;
        private int problemsSolved;

        public DailyActivityItem() {
        }

        public DailyActivityItem(String date, int submissions, int problemsSolved) {
            this.date = date;
            this.submissions = submissions;
            this.problemsSolved = problemsSolved;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public int getSubmissions() {
            return submissions;
        }

        public void setSubmissions(int submissions) {
            this.submissions = submissions;
        }

        public int getProblemsSolved() {
            return problemsSolved;
        }

        public void setProblemsSolved(int problemsSolved) {
            this.problemsSolved = problemsSolved;
        }
    }
}
