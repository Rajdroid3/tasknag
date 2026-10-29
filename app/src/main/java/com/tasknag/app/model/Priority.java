package com.tasknag.app.model;

public enum Priority {
    URGENT("Urgent", "#D32F2F", 0),
    HIGH("High", "#F57C00", 1),
    MEDIUM("Medium", "#1976D2", 2),
    LOW("Low", "#388E3C", 3);

    private final String displayName;
    private final String colorHex;
    private final int sortOrder;

    Priority(String displayName, String colorHex, int sortOrder) {
        this.displayName = displayName;
        this.colorHex = colorHex;
        this.sortOrder = sortOrder;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getColorHex() {
        return colorHex;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public static Priority fromString(String name) {
        if (name == null) return MEDIUM;
        for (Priority p : values()) {
            if (p.name().equalsIgnoreCase(name) || p.displayName.equalsIgnoreCase(name)) {
                return p;
            }
        }
        return MEDIUM;
    }
}
