package com.tasknag.app.model;

public enum OccurrenceStatus {
    UPCOMING("Upcoming", "#757575"),
    DUE_NOW("Due Now", "#E65100"),
    OVERDUE("Overdue", "#C62828"),
    COMPLETED("Completed", "#2E7D32"),
    SKIPPED("Skipped", "#616161"),
    MISSED("Missed", "#B71C1C");

    private final String displayName;
    private final String colorHex;

    OccurrenceStatus(String displayName, String colorHex) {
        this.displayName = displayName;
        this.colorHex = colorHex;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getColorHex() {
        return colorHex;
    }

    public boolean isTerminal() {
        return this == COMPLETED || this == SKIPPED || this == MISSED;
    }

    public boolean isActionable() {
        return this == DUE_NOW || this == OVERDUE || this == UPCOMING;
    }
}
