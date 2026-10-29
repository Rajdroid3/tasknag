package com.tasknag.app.model;

public enum RecurrenceType {
    ONE_TIME("One-time"),
    DAILY("Daily"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly"),
    CUSTOM_INTERVAL("Custom Interval");

    private final String displayName;

    RecurrenceType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static RecurrenceType fromString(String name) {
        if (name == null) return ONE_TIME;
        for (RecurrenceType r : values()) {
            if (r.name().equalsIgnoreCase(name) || r.displayName.equalsIgnoreCase(name)) {
                return r;
            }
        }
        return ONE_TIME;
    }
}
