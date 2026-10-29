package com.tasknag.app;

import com.tasknag.app.model.OccurrenceStatus;
import com.tasknag.app.model.Priority;
import com.tasknag.app.model.ProductivitySummary;
import com.tasknag.app.model.RecurrenceType;
import com.tasknag.app.model.Task;
import com.tasknag.app.model.TaskOccurrence;

import org.junit.Test;

import static org.junit.Assert.*;

public class NagEngineLogicTest {

    @Test
    public void testPersistentNagIntervalSeparateFromRecurrence() {
        Task task = new Task("Take Vitamin B12", "HEALTH", "2026-10-28", "11:00");
        task.setRecurrenceType(RecurrenceType.DAILY); // Recur daily
        task.setNagIntervalMinutes(15);               // Nag every 15 min if incomplete

        // Verify task recurrence is separate from nag interval
        assertEquals(RecurrenceType.DAILY, task.getRecurrenceType());
        assertEquals(15, task.getNagIntervalMinutes());
        assertTrue(task.isRecurring());
        assertEquals("Remind every 15m until completed", task.getNagSummary());
    }

    @Test
    public void testOccurrenceStatusTransitions() {
        long now = 1000000000L;
        long scheduledTime = now - (20 * 60 * 1000L); // 20 min ago

        TaskOccurrence occ = new TaskOccurrence("task_1", "Vitamin B12", "HEALTH", Priority.URGENT, "#E53935", scheduledTime);
        assertEquals(OccurrenceStatus.UPCOMING, occ.getStatus());

        // Check overdue detection
        assertTrue(occ.isOverdueNow(now));
        assertEquals(20, occ.getOverdueMinutes(now));

        // Mark completed
        occ.setStatus(OccurrenceStatus.COMPLETED);
        occ.setCompletedEpoch(now);
        assertTrue(occ.isCompleted());
        assertTrue(occ.isTerminal());
        assertFalse(occ.isOverdueNow(now));
    }

    @Test
    public void testSnoozeLogic() {
        long now = 1000000000L;
        long scheduledTime = now - (5 * 60 * 1000L);

        TaskOccurrence occ = new TaskOccurrence("task_1", "Study", "STUDY", Priority.HIGH, "#3949AB", scheduledTime);
        assertTrue(occ.isDueOrOverdue(now));

        // Snooze for 15 minutes
        long snoozeUntil = now + (15 * 60 * 1000L);
        occ.setSnoozedUntilEpoch(snoozeUntil);

        // While snoozed, it should NOT trigger as overdue now
        assertFalse(occ.isOverdueNow(now));
        assertFalse(occ.isDueOrOverdue(now));

        // After snooze time expires (now + 16 mins)
        long later = now + (16 * 60 * 1000L);
        assertTrue(occ.isDueOrOverdue(later));
    }

    @Test
    public void testIndependentRecurringOccurrences() {
        // Occurrence for Oct 28
        TaskOccurrence occToday = new TaskOccurrence("task_meds", "Daily Meds", "HEALTH", Priority.URGENT, "#E53935", 100000L);
        // Occurrence for Oct 29
        TaskOccurrence occTomorrow = new TaskOccurrence("task_meds", "Daily Meds", "HEALTH", Priority.URGENT, "#E53935", 200000L);

        // Completing today's occurrence
        occToday.setStatus(OccurrenceStatus.COMPLETED);
        occToday.setCompletedEpoch(105000L);

        // Verify tomorrow's occurrence is unaffected!
        assertTrue(occToday.isCompleted());
        assertEquals(OccurrenceStatus.UPCOMING, occTomorrow.getStatus());
        assertNull(occTomorrow.getCompletedEpoch());
    }

    @Test
    public void testProductivitySummaryAccuracy() {
        ProductivitySummary summary = new ProductivitySummary();
        summary.setTodayScheduled(5);
        summary.setTodayCompleted(4);
        summary.setTodayOverdue(1);

        assertEquals(80, summary.getTodayPercentage());
        assertEquals(1, summary.getTodayRemaining());
    }

    @Test
    public void testNagCountIncrement() {
        TaskOccurrence occ = new TaskOccurrence("task_1", "Study", "STUDY", Priority.HIGH, "#3949AB", 1000L);
        assertEquals(0, occ.getNagCount());

        occ.incrementNagCount();
        assertEquals(1, occ.getNagCount());

        occ.incrementNagCount();
        assertEquals(2, occ.getNagCount());
    }
}
