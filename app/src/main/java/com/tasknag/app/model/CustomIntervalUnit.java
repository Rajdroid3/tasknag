package com.tasknag.app.model;

public enum CustomIntervalUnit {
    MINUTES("Minutes", 60L * 1000L),
    HOURS("Hours", 3600L * 1000L),
    DAYS("Days", 86400L * 1000L),
    WEEKS("Weeks", 7L * 86400L * 1000L);

    private final String displayName;
    private final long millis;

    CustomIntervalUnit(String displayName, long millis) {
        this.displayName = displayName;
        this.millis = millis;
    }

    public String getDisplayName() {
        return displayName;
    }

    public long getMillis() {
        return millis;
    }
}
