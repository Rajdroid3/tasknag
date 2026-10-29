package com.tasknag.app.reminder;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.tasknag.app.model.Task;
import com.tasknag.app.model.TaskOccurrence;

public class NagAlarmScheduler {

    private static final String TAG = "NagAlarmScheduler";
    public static final String EXTRA_OCCURRENCE_ID = "extra_occurrence_id";
    public static final String EXTRA_TASK_ID = "extra_task_id";

    public static void scheduleExactAlarm(Context context, TaskOccurrence occurrence, Task task, long targetEpoch) {
        if (occurrence == null || context == null) return;

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;

        Intent intent = new Intent(context, NagAlarmReceiver.class);
        intent.setAction("com.tasknag.app.ACTION_NAG_ALARM");
        intent.putExtra(EXTRA_OCCURRENCE_ID, occurrence.getId());
        intent.putExtra(EXTRA_TASK_ID, occurrence.getTaskId());

        int requestCode = Math.abs(occurrence.getId().hashCode());
        PendingIntent pi = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        long now = System.currentTimeMillis();
        long triggerTime = Math.max(now + 1000L, targetEpoch);

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pi);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                am.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pi);
            } else {
                am.set(AlarmManager.RTC_WAKEUP, triggerTime, pi);
            }
            Log.d(TAG, "Scheduled nag alarm for " + occurrence.getTaskTitle() + " at " + triggerTime);
        } catch (SecurityException e) {
            // Android 12+ exact alarm permission not granted, fallback to inexact window
            Log.w(TAG, "Exact alarm permission missing, falling back to inexact alarm", e);
            am.set(AlarmManager.RTC_WAKEUP, triggerTime, pi);
        }
    }

    public static void cancelNagAlarm(Context context, String occurrenceId) {
        if (occurrenceId == null || context == null) return;
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;

        Intent intent = new Intent(context, NagAlarmReceiver.class);
        intent.setAction("com.tasknag.app.ACTION_NAG_ALARM");
        int requestCode = Math.abs(occurrenceId.hashCode());

        PendingIntent pi = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
        );
        if (pi != null) {
            am.cancel(pi);
            pi.cancel();
        }
    }

    public static void dismissNotification(Context context, String occurrenceId) {
        NagNotificationHelper.cancelNotification(context, occurrenceId);
    }
}
