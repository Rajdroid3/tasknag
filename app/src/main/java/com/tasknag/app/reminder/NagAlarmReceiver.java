package com.tasknag.app.reminder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.tasknag.app.data.TaskRepository;
import com.tasknag.app.model.OccurrenceStatus;
import com.tasknag.app.model.Task;
import com.tasknag.app.model.TaskOccurrence;
import com.tasknag.app.util.AudioVibrationHelper;

public class NagAlarmReceiver extends BroadcastReceiver {

    private static final String TAG = "NagAlarmReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String occurrenceId = intent.getStringExtra(NagAlarmScheduler.EXTRA_OCCURRENCE_ID);
        String taskId = intent.getStringExtra(NagAlarmScheduler.EXTRA_TASK_ID);

        if (occurrenceId == null || taskId == null) {
            Log.w(TAG, "Missing occurrenceId or taskId in alarm intent");
            return;
        }

        TaskRepository repo = TaskRepository.getInstance(context);
        TaskOccurrence occurrence = repo.getOccurrenceById(occurrenceId);
        Task task = repo.getTaskById(taskId);

        if (occurrence == null || task == null) {
            Log.w(TAG, "Occurrence or Task not found for ID: " + occurrenceId);
            return;
        }

        // Rule: If task is disabled, abort
        if (!task.isEnabled()) {
            Log.d(TAG, "Task is disabled, canceling nag");
            NagAlarmScheduler.cancelNagAlarm(context, occurrenceId);
            return;
        }

        // Rule: If task already completed or skipped, abort
        if (occurrence.isTerminal()) {
            Log.d(TAG, "Occurrence is already finished (" + occurrence.getStatus() + "), canceling nag");
            NagAlarmScheduler.cancelNagAlarm(context, occurrenceId);
            return;
        }

        long now = System.currentTimeMillis();

        // Check if currently snoozed and snooze period hasn't arrived yet
        if (occurrence.getSnoozedUntilEpoch() != null && occurrence.getSnoozedUntilEpoch() > (now + 1000L)) {
            Log.d(TAG, "Occurrence still snoozed until " + occurrence.getSnoozedUntilEpoch());
            return;
        }

        // Clear snooze status once alarm triggers
        occurrence.setSnoozedUntilEpoch(null);

        // Check nag max count
        int maxCount = task.getNagMaxCount();
        if (maxCount > 0 && occurrence.getNagCount() >= maxCount) {
            Log.d(TAG, "Max nag count (" + maxCount + ") reached for occurrence " + occurrenceId);
            occurrence.setStatus(OccurrenceStatus.MISSED);
            repo.saveOccurrence(occurrence);
            NagAlarmScheduler.cancelNagAlarm(context, occurrenceId);
            return;
        }

        // Check nag time window
        int timeWindowMin = task.getNagTimeWindowMinutes();
        if (timeWindowMin > 0) {
            long windowMs = timeWindowMin * 60L * 1000L;
            if ((now - occurrence.getScheduledEpoch()) > windowMs) {
                Log.d(TAG, "Nag time window (" + timeWindowMin + "m) expired for occurrence " + occurrenceId);
                occurrence.setStatus(OccurrenceStatus.MISSED);
                repo.saveOccurrence(occurrence);
                NagAlarmScheduler.cancelNagAlarm(context, occurrenceId);
                return;
            }
        }

        // Increment nag count
        occurrence.incrementNagCount();
        occurrence.setLastNagEpoch(now);

        // Status update to DUE_NOW or OVERDUE
        if (now > occurrence.getScheduledEpoch()) {
            long overdueMinutes = (now - occurrence.getScheduledEpoch()) / 60000;
            occurrence.setStatus(overdueMinutes > 10 ? OccurrenceStatus.OVERDUE : OccurrenceStatus.DUE_NOW);
        } else {
            occurrence.setStatus(OccurrenceStatus.DUE_NOW);
        }

        // Calculate next nag target
        int nagIntervalMinutes = task.getNagIntervalMinutes();
        long nextNagEpoch = now + (nagIntervalMinutes * 60L * 1000L);
        occurrence.setNextNagEpoch(nextNagEpoch);

        repo.saveOccurrence(occurrence);

        // Schedule next persistent repeating nag alarm
        NagAlarmScheduler.scheduleExactAlarm(context, occurrence, task, nextNagEpoch);

        // Display high-priority notification with Complete, Snooze, Skip
        NagNotificationHelper.showNagNotification(context, occurrence, task);

        // Play alert sound & vibrate
        AudioVibrationHelper.playNotificationSound(context, task.getSoundType());
        AudioVibrationHelper.vibrate(context, task.getVibrationPattern());
    }
}
