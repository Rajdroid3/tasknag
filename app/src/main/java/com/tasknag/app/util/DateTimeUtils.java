package com.tasknag.app.util;

import com.tasknag.app.model.CustomIntervalUnit;
import com.tasknag.app.model.RecurrenceRule;
import com.tasknag.app.model.RecurrenceType;
import com.tasknag.app.model.Task;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DateTimeUtils {

    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String TIME_FORMAT = "HH:mm";

    public static String getTodayDateString() {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
        return sdf.format(new Date());
    }

    public static String getCurrentTimeString() {
        SimpleDateFormat sdf = new SimpleDateFormat(TIME_FORMAT, Locale.getDefault());
        return sdf.format(new Date());
    }

    public static long getStartOfDayEpoch(long epoch) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(epoch);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    public static long getEndOfDayEpoch(long epoch) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(epoch);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTimeInMillis();
    }

    public static long parseDateTimeToEpoch(String dateStr, String timeStr) {
        if (dateStr == null || timeStr == null) return System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT + " " + TIME_FORMAT, Locale.getDefault());
        try {
            Date d = sdf.parse(dateStr + " " + timeStr);
            return d != null ? d.getTime() : System.currentTimeMillis();
        } catch (ParseException e) {
            return System.currentTimeMillis();
        }
    }

    public static String formatEpochToDate(long epoch) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
        return sdf.format(new Date(epoch));
    }

    public static String formatEpochToTime(long epoch) {
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
        return sdf.format(new Date(epoch));
    }

    public static String formatEpochToFriendlyDate(long epoch) {
        long todayStart = getStartOfDayEpoch(System.currentTimeMillis());
        long targetStart = getStartOfDayEpoch(epoch);
        long diffDays = (targetStart - todayStart) / (24 * 3600 * 1000);

        if (diffDays == 0) return "Today";
        if (diffDays == 1) return "Tomorrow";
        if (diffDays == -1) return "Yesterday";

        SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM d", Locale.getDefault());
        return sdf.format(new Date(epoch));
    }

    public static String formatRelativeOverdue(long scheduledEpoch, long now) {
        long diff = now - scheduledEpoch;
        if (diff <= 0) {
            long remaining = -diff / 60000;
            if (remaining <= 1) return "Due right now";
            return "Due in " + remaining + "m";
        }

        long minutes = diff / 60000;
        if (minutes < 60) {
            return "Overdue by " + minutes + "m";
        }
        long hours = minutes / 60;
        long remMinutes = minutes % 60;
        if (hours < 24) {
            return "Overdue by " + hours + "h " + (remMinutes > 0 ? remMinutes + "m" : "");
        }
        long days = hours / 24;
        return "Overdue by " + days + "d";
    }

    /**
     * Compute next occurrences for a task within a window [windowStartEpoch, windowEndEpoch]
     */
    public static List<Long> generateOccurrences(Task task, long windowStartEpoch, long windowEndEpoch) {
        List<Long> result = new ArrayList<>();
        if (!task.isEnabled()) return result;

        long taskStartEpoch = 0;
        if (task.getStartDate() != null && !task.getStartDate().isEmpty()) {
            taskStartEpoch = parseDateTimeToEpoch(task.getStartDate(), "00:00");
        }
        long taskEndEpoch = Long.MAX_VALUE;
        if (task.getEndDate() != null && !task.getEndDate().isEmpty()) {
            taskEndEpoch = parseDateTimeToEpoch(task.getEndDate(), "23:59");
        }

        long effectiveStart = Math.max(windowStartEpoch, taskStartEpoch);
        long effectiveEnd = Math.min(windowEndEpoch, taskEndEpoch);
        if (effectiveStart > effectiveEnd) return result;

        RecurrenceType type = task.getRecurrenceType();
        if (type == RecurrenceType.ONE_TIME) {
            long oneTimeEpoch = parseDateTimeToEpoch(task.getScheduledDate(), task.getScheduledTime());
            if (oneTimeEpoch >= taskStartEpoch && oneTimeEpoch <= taskEndEpoch) {
                result.add(oneTimeEpoch);
            }
            return result;
        }

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(effectiveStart);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        List<String> times = new ArrayList<>();
        if (task.getMultipleTimes() != null && !task.getMultipleTimes().isEmpty()) {
            times.addAll(task.getMultipleTimes());
        } else {
            times.add(task.getScheduledTime());
        }

        if (type == RecurrenceType.DAILY) {
            while (cal.getTimeInMillis() <= effectiveEnd) {
                String curDate = formatEpochToDate(cal.getTimeInMillis());
                for (String t : times) {
                    long occ = parseDateTimeToEpoch(curDate, t);
                    if (occ >= taskStartEpoch && occ <= taskEndEpoch && occ <= effectiveEnd) {
                        result.add(occ);
                    }
                }
                cal.add(Calendar.DAY_OF_YEAR, 1);
            }
        } else if (type == RecurrenceType.WEEKLY) {
            RecurrenceRule rule = task.getRecurrenceRule();
            List<Integer> days = rule.getWeeklyDays();
            if (days.isEmpty()) {
                days.add(Calendar.MONDAY);
            }
            while (cal.getTimeInMillis() <= effectiveEnd) {
                int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
                if (days.contains(dayOfWeek)) {
                    String curDate = formatEpochToDate(cal.getTimeInMillis());
                    for (String t : times) {
                        long occ = parseDateTimeToEpoch(curDate, t);
                        if (occ >= taskStartEpoch && occ <= taskEndEpoch && occ <= effectiveEnd) {
                            result.add(occ);
                        }
                    }
                }
                cal.add(Calendar.DAY_OF_YEAR, 1);
            }
        } else if (type == RecurrenceType.MONTHLY) {
            int targetDay = task.getRecurrenceRule().getMonthlyDay();
            while (cal.getTimeInMillis() <= effectiveEnd) {
                int maxDayInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
                int dayToUse = Math.min(targetDay, maxDayInMonth); // clamp for Feb/30-day months!
                Calendar monthCal = (Calendar) cal.clone();
                monthCal.set(Calendar.DAY_OF_MONTH, dayToUse);
                String curDate = formatEpochToDate(monthCal.getTimeInMillis());
                for (String t : times) {
                    long occ = parseDateTimeToEpoch(curDate, t);
                    if (occ >= taskStartEpoch && occ <= taskEndEpoch && occ <= effectiveEnd) {
                        result.add(occ);
                    }
                }
                cal.add(Calendar.MONTH, 1);
                cal.set(Calendar.DAY_OF_MONTH, 1);
            }
        } else if (type == RecurrenceType.CUSTOM_INTERVAL) {
            RecurrenceRule rule = task.getRecurrenceRule();
            int val = rule.getCustomValue();
            CustomIntervalUnit unit = rule.getCustomUnit();
            long baseEpoch = parseDateTimeToEpoch(task.getScheduledDate(), task.getScheduledTime());
            long stepMs = val * unit.getMillis();
            if (stepMs <= 0) stepMs = 86400000L;

            long cur = baseEpoch;
            // advance to effectiveStart if base is far behind
            if (cur < effectiveStart) {
                long steps = (effectiveStart - cur) / stepMs;
                cur += steps * stepMs;
            }
            while (cur <= effectiveEnd) {
                if (cur >= taskStartEpoch && cur <= taskEndEpoch) {
                    result.add(cur);
                }
                cur += stepMs;
            }
        }

        return result;
    }
}
