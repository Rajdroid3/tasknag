package com.tasknag.app.reminder;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.tasknag.app.R;
import com.tasknag.app.model.Priority;
import com.tasknag.app.model.Task;
import com.tasknag.app.model.TaskOccurrence;
import com.tasknag.app.ui.MainActivity;
import com.tasknag.app.util.AudioVibrationHelper;
import com.tasknag.app.util.ColorUtils;
import com.tasknag.app.util.DateTimeUtils;

public class NagNotificationHelper {

    public static final String CHANNEL_CRITICAL_REMINDERS = "channel_tasknag_critical";
    public static final String CHANNEL_STANDARD_REMINDERS = "channel_tasknag_standard";

    public static void createNotificationChannels(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm == null) return;

            // Critical Nagging Channel (Urgent / High priority)
            NotificationChannel criticalChannel = new NotificationChannel(
                    CHANNEL_CRITICAL_REMINDERS,
                    "Persistent Task Reminders",
                    NotificationManager.IMPORTANCE_HIGH
            );
            criticalChannel.setDescription("Persistent repeated reminders for urgent & due tasks until marked completed");
            criticalChannel.enableLights(true);
            criticalChannel.setLightColor(Color.RED);
            criticalChannel.enableVibration(true);
            criticalChannel.setVibrationPattern(AudioVibrationHelper.VIBRATE_URGENT);
            criticalChannel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);
            criticalChannel.setBypassDnd(true);

            // Standard Channel
            NotificationChannel standardChannel = new NotificationChannel(
                    CHANNEL_STANDARD_REMINDERS,
                    "General Reminders",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            standardChannel.setDescription("Standard reminder notifications for scheduled tasks");
            standardChannel.enableVibration(true);
            standardChannel.setVibrationPattern(AudioVibrationHelper.VIBRATE_DEFAULT);

            nm.createNotificationChannel(criticalChannel);
            nm.createNotificationChannel(standardChannel);
        }
    }

    public static void showNagNotification(Context context, TaskOccurrence occurrence, Task task) {
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm == null) return;

        createNotificationChannels(context);

        long now = System.currentTimeMillis();
        long overdueMinutes = occurrence.getOverdueMinutes(now);

        String channelId = (occurrence.getPriority() == Priority.URGENT || occurrence.getPriority() == Priority.HIGH)
                ? CHANNEL_CRITICAL_REMINDERS : CHANNEL_STANDARD_REMINDERS;

        // Content text
        String contentText;
        if (overdueMinutes > 0) {
            contentText = "⚠️ Overdue by " + overdueMinutes + "m • Reminder #" + occurrence.getNagCount();
        } else {
            contentText = "Due now (" + occurrence.getFormattedTime() + ")";
            if (occurrence.getNagCount() > 1) {
                contentText += " • Reminder #" + occurrence.getNagCount();
            }
        }

        String subText = task != null ? task.getNagSummary() : "Persistent reminder";

        // Open App Intent
        Intent openIntent = new Intent(context, MainActivity.class);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        openIntent.putExtra("EXTRA_OCCURRENCE_ID", occurrence.getId());
        PendingIntent openPendingIntent = PendingIntent.getActivity(
                context,
                occurrence.getId().hashCode(),
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Action 1: Complete Task
        Intent completeIntent = new Intent(context, NagActionReceiver.class);
        completeIntent.setAction(NagActionReceiver.ACTION_COMPLETE);
        completeIntent.putExtra(NagActionReceiver.EXTRA_OCCURRENCE_ID, occurrence.getId());
        PendingIntent completePendingIntent = PendingIntent.getBroadcast(
                context,
                (occurrence.getId() + "_complete").hashCode(),
                completeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Action 2: Snooze (default 10 mins or parent nag interval)
        int snoozeMin = (task != null && task.getNagIntervalMinutes() > 0) ? task.getNagIntervalMinutes() : 10;
        Intent snoozeIntent = new Intent(context, NagActionReceiver.class);
        snoozeIntent.setAction(NagActionReceiver.ACTION_SNOOZE);
        snoozeIntent.putExtra(NagActionReceiver.EXTRA_OCCURRENCE_ID, occurrence.getId());
        snoozeIntent.putExtra(NagActionReceiver.EXTRA_SNOOZE_MINUTES, snoozeMin);
        PendingIntent snoozePendingIntent = PendingIntent.getBroadcast(
                context,
                (occurrence.getId() + "_snooze").hashCode(),
                snoozeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Action 3: Skip Task
        Intent skipIntent = new Intent(context, NagActionReceiver.class);
        skipIntent.setAction(NagActionReceiver.ACTION_SKIP);
        skipIntent.putExtra(NagActionReceiver.EXTRA_OCCURRENCE_ID, occurrence.getId());
        PendingIntent skipPendingIntent = PendingIntent.getBroadcast(
                context,
                (occurrence.getId() + "_skip").hashCode(),
                skipIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        int accentColor = ColorUtils.parseColor(occurrence.getColorHex(), Color.parseColor("#1976D2"));

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle(occurrence.getTaskTitle())
                .setContentText(contentText)
                .setSubText(subText)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(contentText + (task != null && !task.getDescription().isEmpty() ? "\n" + task.getDescription() : "")
                                + (task != null && !task.getNotes().isEmpty() ? "\n📝 " + task.getNotes() : "")))
                .setColor(accentColor)
                .setColorized(true)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setContentIntent(openPendingIntent)
                .setAutoCancel(false)
                .setOngoing(false) // Dismissing notification does NOT cancel task! Next nag alarm will re-notify
                .addAction(android.R.drawable.checkbox_on_background, "✓ Complete", completePendingIntent)
                .addAction(android.R.drawable.ic_popup_reminder, "⏰ Snooze (" + snoozeMin + "m)", snoozePendingIntent)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, "✕ Skip", skipPendingIntent);

        // Sound and Vibration
        if (task != null) {
            long[] vib = AudioVibrationHelper.getVibrationPattern(task.getVibrationPattern());
            builder.setVibrate(vib);
        }

        int notificationId = Math.abs(occurrence.getId().hashCode());
        nm.notify(notificationId, builder.build());
    }

    public static void cancelNotification(Context context, String occurrenceId) {
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) {
            nm.cancel(Math.abs(occurrenceId.hashCode()));
        }
    }
}
