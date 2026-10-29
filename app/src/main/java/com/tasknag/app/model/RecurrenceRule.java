package com.tasknag.app.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class RecurrenceRule implements Serializable {
    private List<Integer> weeklyDays = new ArrayList<>();
    private int monthlyDay = 1; // 1-31
    private CustomIntervalUnit customUnit = CustomIntervalUnit.DAYS;
    private int customValue = 1;

    public RecurrenceRule() {}

    public List<Integer> getWeeklyDays() {
        return weeklyDays != null ? weeklyDays : new ArrayList<>();
    }

    public void setWeeklyDays(List<Integer> weeklyDays) {
        this.weeklyDays = weeklyDays;
    }

    public int getMonthlyDay() {
        return monthlyDay;
    }

    public void setMonthlyDay(int monthlyDay) {
        this.monthlyDay = Math.max(1, Math.min(31, monthlyDay));
    }

    public CustomIntervalUnit getCustomUnit() {
        return customUnit != null ? customUnit : CustomIntervalUnit.DAYS;
    }

    public void setCustomUnit(CustomIntervalUnit customUnit) {
        this.customUnit = customUnit;
    }

    public int getCustomValue() {
        return Math.max(1, customValue);
    }

    public void setCustomValue(int customValue) {
        this.customValue = Math.max(1, customValue);
    }

    public String toReadableDescription(RecurrenceType type, String timeStr) {
        if (type == null) return "One-time";
        switch (type) {
            case ONE_TIME:
                return "One-time at " + timeStr;
            case DAILY:
                return "Every day at " + timeStr;
            case WEEKLY:
                if (weeklyDays == null || weeklyDays.isEmpty()) {
                    return "Weekly at " + timeStr;
                }
                List<String> dayNames = new ArrayList<>();
                for (int d : weeklyDays) {
                    dayNames.add(getDayAbbrev(d));
                }
                return String.join(", ", dayNames) + " at " + timeStr;
            case MONTHLY:
                return "Monthly on day " + monthlyDay + " at " + timeStr;
            case CUSTOM_INTERVAL:
                String unit = customUnit != null ? customUnit.getDisplayName().toLowerCase() : "days";
                if (customValue == 1 && unit.endsWith("s")) {
                    unit = unit.substring(0, unit.length() - 1);
                }
                return "Every " + customValue + " " + unit + (timeStr != null ? " starting " + timeStr : "");
            default:
                return "Custom Schedule";
        }
    }

    private String getDayAbbrev(int calDay) {
        switch (calDay) {
            case Calendar.SUNDAY: return "Sun";
            case Calendar.MONDAY: return "Mon";
            case Calendar.TUESDAY: return "Tue";
            case Calendar.WEDNESDAY: return "Wed";
            case Calendar.THURSDAY: return "Thu";
            case Calendar.FRIDAY: return "Fri";
            case Calendar.SATURDAY: return "Sat";
            default: return "";
        }
    }
}
