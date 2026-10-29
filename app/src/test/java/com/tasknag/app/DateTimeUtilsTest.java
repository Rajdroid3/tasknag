package com.tasknag.app;

import com.tasknag.app.model.CustomIntervalUnit;
import com.tasknag.app.model.RecurrenceType;
import com.tasknag.app.model.Task;
import com.tasknag.app.util.DateTimeUtils;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static org.junit.Assert.*;

public class DateTimeUtilsTest {

    @Test
    public void testDateParsingAndFormatting() {
        String dateStr = "2026-10-28";
        String timeStr = "14:30";
        long epoch = DateTimeUtils.parseDateTimeToEpoch(dateStr, timeStr);
        assertTrue(epoch > 0);

        String formattedDate = DateTimeUtils.formatEpochToDate(epoch);
        assertEquals("2026-10-28", formattedDate);
    }

    @Test
    public void testStartAndEndOfDay() {
        long now = System.currentTimeMillis();
        long start = DateTimeUtils.getStartOfDayEpoch(now);
        long end = DateTimeUtils.getEndOfDayEpoch(now);

        assertTrue(start <= now);
        assertTrue(end >= now);
        assertTrue(end > start);
        assertEquals(86399999L, end - start);
    }

    @Test
    public void testRelativeOverdueFormatting() {
        long now = 1000000000L;
        // 15 minutes overdue
        long past15 = now - (15 * 60 * 1000L);
        String s1 = DateTimeUtils.formatRelativeOverdue(past15, now);
        assertEquals("Overdue by 15m", s1);

        // 30 minutes in future
        long future30 = now + (30 * 60 * 1000L);
        String s2 = DateTimeUtils.formatRelativeOverdue(future30, now);
        assertEquals("Due in 30m", s2);
    }

    @Test
    public void testDailyOccurrencesGeneration() {
        Task dailyTask = new Task("Daily Meds", "HEALTH", "2026-10-28", "09:00");
        dailyTask.setRecurrenceType(RecurrenceType.DAILY);
        dailyTask.setStartDate("2026-10-28");
        dailyTask.setEndDate("2026-10-30");

        long start = DateTimeUtils.parseDateTimeToEpoch("2026-10-28", "00:00");
        long end = DateTimeUtils.parseDateTimeToEpoch("2026-10-30", "23:59");

        List<Long> occs = DateTimeUtils.generateOccurrences(dailyTask, start, end);
        // Oct 28, Oct 29, Oct 30 -> 3 occurrences
        assertEquals(3, occs.size());
    }

    @Test
    public void testWeeklyOccurrencesGeneration() {
        Task weeklyTask = new Task("Workout", "EXERCISE", "2026-10-26", "18:00");
        weeklyTask.setRecurrenceType(RecurrenceType.WEEKLY);
        List<Integer> days = new ArrayList<>();
        days.add(Calendar.MONDAY);
        days.add(Calendar.FRIDAY);
        weeklyTask.getRecurrenceRule().setWeeklyDays(days);
        weeklyTask.setStartDate("2026-10-26"); // Monday
        weeklyTask.setEndDate("2026-11-01");   // Sunday

        long start = DateTimeUtils.parseDateTimeToEpoch("2026-10-26", "00:00");
        long end = DateTimeUtils.parseDateTimeToEpoch("2026-11-01", "23:59");

        List<Long> occs = DateTimeUtils.generateOccurrences(weeklyTask, start, end);
        // Monday and Friday -> 2 occurrences
        assertEquals(2, occs.size());
    }

    @Test
    public void testMonthlyClampOccurrenceGeneration() {
        // Task set on day 31
        Task monthlyTask = new Task("Bill", "HOUSEHOLD", "2026-02-01", "10:00");
        monthlyTask.setRecurrenceType(RecurrenceType.MONTHLY);
        monthlyTask.getRecurrenceRule().setMonthlyDay(31);
        monthlyTask.setStartDate("2026-02-01");
        monthlyTask.setEndDate("2026-02-28");

        long start = DateTimeUtils.parseDateTimeToEpoch("2026-02-01", "00:00");
        long end = DateTimeUtils.parseDateTimeToEpoch("2026-02-28", "23:59");

        List<Long> occs = DateTimeUtils.generateOccurrences(monthlyTask, start, end);
        assertEquals(1, occs.size());
        // For Feb, day 31 must clamp to last day of Feb (28)
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(occs.get(0));
        assertEquals(28, c.get(Calendar.DAY_OF_MONTH));
    }

    @Test
    public void testCustomIntervalOccurrenceGeneration() {
        Task waterTask = new Task("Drink Water", "HEALTH", "2026-10-28", "08:00");
        waterTask.setRecurrenceType(RecurrenceType.CUSTOM_INTERVAL);
        waterTask.getRecurrenceRule().setCustomValue(2);
        waterTask.getRecurrenceRule().setCustomUnit(CustomIntervalUnit.HOURS);
        waterTask.setStartDate("2026-10-28");
        waterTask.setEndDate("2026-10-28");

        long start = DateTimeUtils.parseDateTimeToEpoch("2026-10-28", "08:00");
        long end = DateTimeUtils.parseDateTimeToEpoch("2026-10-28", "16:00");

        List<Long> occs = DateTimeUtils.generateOccurrences(waterTask, start, end);
        // 08:00, 10:00, 12:00, 14:00, 16:00 -> 5 occurrences
        assertEquals(5, occs.size());
    }
}
