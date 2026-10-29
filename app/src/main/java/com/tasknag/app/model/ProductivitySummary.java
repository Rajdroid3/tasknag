package com.tasknag.app.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductivitySummary implements Serializable {
    private int totalCompletedAllTime = 0;
    private int totalScheduledPastAndPresent = 0;
    private int totalOverdueCurrently = 0;
    private int totalSkipped = 0;
    private int totalMissed = 0;

    private int todayScheduled = 0;
    private int todayCompleted = 0;
    private int todayOverdue = 0;
    private int todayRemaining = 0;

    private List<DailyMetric> last7Days = new ArrayList<>();
    private List<StreakItem> streaks = new ArrayList<>();

    public ProductivitySummary() {}

    public int getTotalCompletedAllTime() {
        return totalCompletedAllTime;
    }

    public void setTotalCompletedAllTime(int totalCompletedAllTime) {
        this.totalCompletedAllTime = totalCompletedAllTime;
    }

    public int getTotalScheduledPastAndPresent() {
        return totalScheduledPastAndPresent;
    }

    public void setTotalScheduledPastAndPresent(int totalScheduledPastAndPresent) {
        this.totalScheduledPastAndPresent = totalScheduledPastAndPresent;
    }

    public int getTotalOverdueCurrently() {
        return totalOverdueCurrently;
    }

    public void setTotalOverdueCurrently(int totalOverdueCurrently) {
        this.totalOverdueCurrently = totalOverdueCurrently;
    }

    public int getTotalSkipped() {
        return totalSkipped;
    }

    public void setTotalSkipped(int totalSkipped) {
        this.totalSkipped = totalSkipped;
    }

    public int getTotalMissed() {
        return totalMissed;
    }

    public void setTotalMissed(int totalMissed) {
        this.totalMissed = totalMissed;
    }

    public int getTodayScheduled() {
        return todayScheduled;
    }

    public void setTodayScheduled(int todayScheduled) {
        this.todayScheduled = todayScheduled;
    }

    public int getTodayCompleted() {
        return todayCompleted;
    }

    public void setTodayCompleted(int todayCompleted) {
        this.todayCompleted = todayCompleted;
    }

    public int getTodayOverdue() {
        return todayOverdue;
    }

    public void setTodayOverdue(int todayOverdue) {
        this.todayOverdue = todayOverdue;
    }

    public int getTodayRemaining() {
        if (todayRemaining > 0) return todayRemaining;
        return Math.max(0, todayScheduled - todayCompleted);
    }

    public void setTodayRemaining(int todayRemaining) {
        this.todayRemaining = todayRemaining;
    }

    public int getTodayPercentage() {
        if (todayScheduled == 0) return 0;
        return (int) Math.round((todayCompleted * 100.0) / todayScheduled);
    }

    public List<DailyMetric> getLast7Days() {
        return last7Days != null ? last7Days : new ArrayList<>();
    }

    public void setLast7Days(List<DailyMetric> last7Days) {
        this.last7Days = last7Days;
    }

    public List<StreakItem> getStreaks() {
        return streaks != null ? streaks : new ArrayList<>();
    }

    public void setStreaks(List<StreakItem> streaks) {
        this.streaks = streaks;
    }

    public static class DailyMetric implements Serializable {
        public String dateStr; // e.g. "Mon 28"
        public String fullDate; // YYYY-MM-DD
        public int completed;
        public int scheduled;

        public DailyMetric(String dateStr, String fullDate, int completed, int scheduled) {
            this.dateStr = dateStr;
            this.fullDate = fullDate;
            this.completed = completed;
            this.scheduled = scheduled;
        }

        public int getPercentage() {
            if (scheduled == 0) return 0;
            return (int) Math.round((completed * 100.0) / scheduled);
        }
    }

    public static class StreakItem implements Serializable {
        public String taskId;
        public String taskTitle;
        public String category;
        public int currentStreak;
        public int bestStreak;

        public StreakItem(String taskId, String taskTitle, String category, int currentStreak, int bestStreak) {
            this.taskId = taskId;
            this.taskTitle = taskTitle;
            this.category = category;
            this.currentStreak = currentStreak;
            this.bestStreak = bestStreak;
        }
    }
}
