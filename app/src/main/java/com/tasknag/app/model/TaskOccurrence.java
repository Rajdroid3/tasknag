package com.tasknag.app.model;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TaskOccurrence implements Serializable {
    private String id;
    private String taskId;
    private String taskTitle;
    private String category;
    private Priority priority = Priority.MEDIUM;
    private String colorHex = "#1976D2";

    private long scheduledEpoch; // Milliseconds timestamp of when this is due
    private OccurrenceStatus status = OccurrenceStatus.UPCOMING;

    private Long completedEpoch;
    private Long skippedEpoch;

    // Nagging tracking
    private int nagCount = 0; // how many nag alarms have fired for this occurrence
    private Long lastNagEpoch;
    private Long nextNagEpoch;
    private Long snoozedUntilEpoch;
    private String notes;

    public TaskOccurrence() {}

    public TaskOccurrence(String taskId, String taskTitle, String category, Priority priority,
                          String colorHex, long scheduledEpoch) {
        this.id = taskId + "_" + scheduledEpoch;
        this.taskId = taskId;
        this.taskTitle = taskTitle;
        this.category = category;
        this.priority = priority;
        this.colorHex = colorHex;
        this.scheduledEpoch = scheduledEpoch;
        this.status = OccurrenceStatus.UPCOMING;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getTaskTitle() {
        return taskTitle != null ? taskTitle : "";
    }

    public void setTaskTitle(String taskTitle) {
        this.taskTitle = taskTitle;
    }

    public String getCategory() {
        return category != null ? category : "PERSONAL";
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Priority getPriority() {
        return priority != null ? priority : Priority.MEDIUM;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public String getColorHex() {
        return colorHex != null ? colorHex : "#1976D2";
    }

    public void setColorHex(String colorHex) {
        this.colorHex = colorHex;
    }

    public long getScheduledEpoch() {
        return scheduledEpoch;
    }

    public void setScheduledEpoch(long scheduledEpoch) {
        this.scheduledEpoch = scheduledEpoch;
    }

    public OccurrenceStatus getStatus() {
        return status != null ? status : OccurrenceStatus.UPCOMING;
    }

    public void setStatus(OccurrenceStatus status) {
        this.status = status;
    }

    public Long getCompletedEpoch() {
        return completedEpoch;
    }

    public void setCompletedEpoch(Long completedEpoch) {
        this.completedEpoch = completedEpoch;
    }

    public Long getSkippedEpoch() {
        return skippedEpoch;
    }

    public void setSkippedEpoch(Long skippedEpoch) {
        this.skippedEpoch = skippedEpoch;
    }

    public int getNagCount() {
        return nagCount;
    }

    public void setNagCount(int nagCount) {
        this.nagCount = nagCount;
    }

    public void incrementNagCount() {
        this.nagCount++;
    }

    public Long getLastNagEpoch() {
        return lastNagEpoch;
    }

    public void setLastNagEpoch(Long lastNagEpoch) {
        this.lastNagEpoch = lastNagEpoch;
    }

    public Long getNextNagEpoch() {
        return nextNagEpoch;
    }

    public void setNextNagEpoch(Long nextNagEpoch) {
        this.nextNagEpoch = nextNagEpoch;
    }

    public Long getSnoozedUntilEpoch() {
        return snoozedUntilEpoch;
    }

    public void setSnoozedUntilEpoch(Long snoozedUntilEpoch) {
        this.snoozedUntilEpoch = snoozedUntilEpoch;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public boolean isTerminal() {
        return status == OccurrenceStatus.COMPLETED || status == OccurrenceStatus.SKIPPED || status == OccurrenceStatus.MISSED;
    }

    public boolean isCompleted() {
        return status == OccurrenceStatus.COMPLETED;
    }

    public boolean isOverdueNow(long now) {
        if (isTerminal()) return false;
        if (snoozedUntilEpoch != null && snoozedUntilEpoch > now) return false;
        return now > scheduledEpoch;
    }

    public boolean isDueOrOverdue(long now) {
        if (isTerminal()) return false;
        if (snoozedUntilEpoch != null && snoozedUntilEpoch > now) return false;
        // Due within 2 minutes or overdue
        return now >= (scheduledEpoch - 120000L);
    }

    public long getOverdueMinutes(long now) {
        if (now <= scheduledEpoch) return 0;
        return (now - scheduledEpoch) / (60 * 1000);
    }

    public String getFormattedTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
        return sdf.format(new Date(scheduledEpoch));
    }

    public String getFormattedDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM d", Locale.getDefault());
        return sdf.format(new Date(scheduledEpoch));
    }

    public String getFormattedCompletedTime() {
        if (completedEpoch == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
        return sdf.format(new Date(completedEpoch));
    }
}
