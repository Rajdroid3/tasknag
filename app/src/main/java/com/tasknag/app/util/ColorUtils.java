package com.tasknag.app.util;

import android.graphics.Color;

import com.tasknag.app.model.Priority;

public class ColorUtils {

    public static int parseColor(String hex, int defaultColor) {
        if (hex == null || hex.trim().isEmpty()) {
            return defaultColor;
        }
        try {
            return Color.parseColor(hex.trim());
        } catch (IllegalArgumentException e) {
            return defaultColor;
        }
    }

    public static int getPriorityColor(Priority priority) {
        if (priority == null) return Color.parseColor("#1976D2");
        return parseColor(priority.getColorHex(), Color.parseColor("#1976D2"));
    }

    public static int adjustAlpha(int color, float factor) {
        int alpha = Math.round(Color.alpha(color) * factor);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }
}
