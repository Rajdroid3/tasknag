package com.tasknag.app.reminder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.tasknag.app.data.TaskRepository;
import com.tasknag.app.model.Task;
import com.tasknag.app.model.TaskOccurrence;

import java.util.List;

public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (Intent.ACTION_BOOT_COMPLETED.equals(action) || Intent.ACTION_MY_PACKAGE_REPLACED.equals(action)) {
            Log.d(TAG, "Device rebooted or app updated. Restoring all nag reminder alarms...");

            NagNotificationHelper.createNotificationChannels(context);

            TaskRepository repo = TaskRepository.getInstance(context);
            List<Task> tasks = repo.getAllTasks();

            for (Task task : tasks) {
                if (task.isEnabled()) {
                    repo.scheduleNextAlarmForTask(task.getId());
                }
            }
        }
    }
}
