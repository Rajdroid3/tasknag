package com.tasknag.app.reminder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.tasknag.app.data.TaskRepository;
import com.tasknag.app.model.TaskOccurrence;

public class NagActionReceiver extends BroadcastReceiver {

    public static final String ACTION_COMPLETE = "com.tasknag.app.ACTION_COMPLETE";
    public static final String ACTION_SNOOZE = "com.tasknag.app.ACTION_SNOOZE";
    public static final String ACTION_SKIP = "com.tasknag.app.ACTION_SKIP";

    public static final String EXTRA_OCCURRENCE_ID = "extra_occurrence_id";
    public static final String EXTRA_SNOOZE_MINUTES = "extra_snooze_minutes";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        String occurrenceId = intent.getStringExtra(EXTRA_OCCURRENCE_ID);

        if (action == null || occurrenceId == null) return;

        TaskRepository repo = TaskRepository.getInstance(context);
        TaskOccurrence occ = repo.getOccurrenceById(occurrenceId);

        if (ACTION_COMPLETE.equals(action)) {
            repo.markOccurrenceCompleted(occurrenceId, null);
            String title = occ != null ? occ.getTaskTitle() : "Task";
            Toast.makeText(context, "✓ " + title + " completed!", Toast.LENGTH_SHORT).show();
        } else if (ACTION_SNOOZE.equals(action)) {
            int snoozeMinutes = intent.getIntExtra(EXTRA_SNOOZE_MINUTES, 10);
            repo.snoozeOccurrence(occurrenceId, snoozeMinutes);
            String title = occ != null ? occ.getTaskTitle() : "Task";
            Toast.makeText(context, "⏰ " + title + " snoozed for " + snoozeMinutes + "m", Toast.LENGTH_SHORT).show();
        } else if (ACTION_SKIP.equals(action)) {
            repo.markOccurrenceSkipped(occurrenceId);
            String title = occ != null ? occ.getTaskTitle() : "Task";
            Toast.makeText(context, "✕ " + title + " skipped", Toast.LENGTH_SHORT).show();
        }
    }
}
