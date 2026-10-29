package com.tasknag.app.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Task implements Serializable {
    private String id;
    private String title;
    private String description;
    private String category; // CategoryDef ID or name
    private String customCategoryName;
    private Priority priority = Priority.MEDIUM;
    private String notes;

    // Scheduling
    private String scheduledDate; // YYYY-MM-DD
    private String scheduledTime; // HH:mm (24-hour)
    private List<String> multipleTimes = new ArrayList<>(); // e.g. for multiple times per day
    private String startDate; // YYYY-MM-DD (optional active start)
    private String endDate; // YYYY-MM-DD (optional active end, null = indefinite)
    private RecurrenceType recurrenceType = RecurrenceType.ONE_TIME;
    private RecurrenceRule recurrenceRule = new RecurrenceRule();

    // Persistent Reminder Settings ("Nag Engine")
    // Keep reminding until marked as completed
    private int nagIntervalMinutes = 15; // default nag every 15 min (5, 10, 15, 30, 60 or custom)
    private int nagMaxCount = 0; // 0 = repeat until completed/indefinite, >0 = stop after N reminders
    private int nagTimeWindowMinutes = 0; // 0 = no limit, >0 = limit nag window (e.g. 240 mins)

    // Notification Sound & Vibration
    private String soundType = "DEFAULT"; // DEFAULT, ALARM, CHIME, URGENT, GENTLE, SILENT
    private String vibrationPattern = "DEFAULT"; // DEFAULT, URGENT_PULSE, LONG_BUZZ, GENTLE, OFF
    private String colorHex = "#1976D2";
    private String iconName = "ic_alarm";

    // Tags & State
    private List<String> tags = new ArrayList<>();
    private boolean isEnabled = true; // can pause/disable without deleting history
    private long createdAt;
    private long updatedAt;

    public Task() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    public Task(String title, String category, String scheduledDate, String scheduledTime) {
        this();
        this.title = title;
        this.category = category;
        this.scheduledDate = scheduledDate;
        this.scheduledTime = scheduledTime;
        this.startDate = scheduledDate;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title != null ? title : "";
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description != null ? description : "";
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category != null ? category : "PERSONAL";
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCustomCategoryName() {
        return customCategoryName;
    }

    public void setCustomCategoryName(String customCategoryName) {
        this.customCategoryName = customCategoryName;
    }

    public Priority getPriority() {
        return priority != null ? priority : Priority.MEDIUM;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public String getNotes() {
        return notes != null ? notes : "";
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getScheduledDate() {
        return scheduledDate;
    }

    public void setScheduledDate(String scheduledDate) {
        this.scheduledDate = scheduledDate;
    }

    public String getScheduledTime() {
        return scheduledTime != null ? scheduledTime : "09:00";
    }

    public void setScheduledTime(String scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public List<String> getMultipleTimes() {
        return multipleTimes != null ? multipleTimes : new ArrayList<>();
    }

    public void setMultipleTimes(List<String> multipleTimes) {
        this.multipleTimes = multipleTimes;
    }

    public String getStartDate() {
        return startDate != null ? startDate : scheduledDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public RecurrenceType getRecurrenceType() {
        return recurrenceType != null ? recurrenceType : RecurrenceType.ONE_TIME;
    }

    public void setRecurrenceType(RecurrenceType recurrenceType) {
        this.recurrenceType = recurrenceType;
    }

    public RecurrenceRule getRecurrenceRule() {
        if (recurrenceRule == null) {
            recurrenceRule = new RecurrenceRule();
        }
        return recurrenceRule;
    }

    public void setRecurrenceRule(RecurrenceRule recurrenceRule) {
        this.recurrenceRule = recurrenceRule;
    }

    public int getNagIntervalMinutes() {
        return nagIntervalMinutes > 0 ? nagIntervalMinutes : 15;
    }

    public void setNagIntervalMinutes(int nagIntervalMinutes) {
        this.nagIntervalMinutes = Math.max(1, nagIntervalMinutes);
    }

    public int getNagMaxCount() {
        return nagMaxCount;
    }

    public void setNagMaxCount(int nagMaxCount) {
        this.nagMaxCount = Math.max(0, nagMaxCount);
    }

    public int getNagTimeWindowMinutes() {
        return nagTimeWindowMinutes;
    }

    public void setNagTimeWindowMinutes(int nagTimeWindowMinutes) {
        this.nagTimeWindowMinutes = Math.max(0, nagTimeWindowMinutes);
    }

    public String getSoundType() {
        return soundType != null ? soundType : "DEFAULT";
    }

    public void setSoundType(String soundType) {
        this.soundType = soundType;
    }

    public String getVibrationPattern() {
        return vibrationPattern != null ? vibrationPattern : "DEFAULT";
    }

    public void setVibrationPattern(String vibrationPattern) {
        this.vibrationPattern = vibrationPattern;
    }

    public String getColorHex() {
        return colorHex != null ? colorHex : "#1976D2";
    }

    public void setColorHex(String colorHex) {
        this.colorHex = colorHex;
    }

    public String getIconName() {
        return iconName != null ? iconName : "ic_alarm";
    }

    public void setIconName(String iconName) {
        this.iconName = iconName;
    }

    public List<String> getTags() {
        return tags != null ? tags : new ArrayList<>();
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isRecurring() {
        return recurrenceType != null && recurrenceType != RecurrenceType.ONE_TIME;
    }

    public String getScheduleSummary() {
        if (recurrenceType == RecurrenceType.ONE_TIME) {
            return "One-time: " + (scheduledDate != null ? scheduledDate : "") + " at " + scheduledTime;
        }
        return getRecurrenceRule().toReadableDescription(recurrenceType, scheduledTime);
    }

    public String getNagSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("Remind every ").append(nagIntervalMinutes).append("m");
        if (nagMaxCount > 0) {
            sb.append(" (max ").append(nagMaxCount).append("x)");
        } else {
            sb.append(" until completed");
        }
        if (nagTimeWindowMinutes > 0) {
            sb.append(" for ").append(nagTimeWindowMinutes).append("m");
        }
        return sb.toString();
    }
}
