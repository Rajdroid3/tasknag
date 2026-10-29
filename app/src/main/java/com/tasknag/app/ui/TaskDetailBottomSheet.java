package com.tasknag.app.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.tasknag.app.R;
import com.tasknag.app.data.TaskRepository;
import com.tasknag.app.model.OccurrenceStatus;
import com.tasknag.app.model.Task;
import com.tasknag.app.model.TaskOccurrence;
import com.tasknag.app.util.ColorUtils;
import com.tasknag.app.util.DateTimeUtils;

public class TaskDetailBottomSheet {

    public static void show(Context context, TaskOccurrence occurrence) {
        if (occurrence == null || context == null) return;

        BottomSheetDialog dialog = new BottomSheetDialog(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_task_detail, null);
        dialog.setContentView(view);

        TaskRepository repo = TaskRepository.getInstance(context);
        Task task = repo.getTaskById(occurrence.getTaskId());

        TextView tvCategory = view.findViewById(R.id.tvDetailCategory);
        TextView tvPriority = view.findViewById(R.id.tvDetailPriority);
        TextView tvStatus = view.findViewById(R.id.tvDetailStatus);
        TextView tvTitle = view.findViewById(R.id.tvDetailTitle);
        TextView tvTime = view.findViewById(R.id.tvDetailTime);
        TextView tvNagRule = view.findViewById(R.id.tvDetailNagRule);
        TextView tvNagCount = view.findViewById(R.id.tvDetailNagCount);
        TextView tvDescription = view.findViewById(R.id.tvDetailDescription);
        TextView tvNotes = view.findViewById(R.id.tvDetailNotes);

        MaterialButton btnComplete = view.findViewById(R.id.btnModalComplete);
        MaterialButton btnSnooze = view.findViewById(R.id.btnModalSnooze);
        MaterialButton btnSkip = view.findViewById(R.id.btnModalSkip);
        MaterialButton btnEdit = view.findViewById(R.id.btnModalEdit);
        MaterialButton btnDelete = view.findViewById(R.id.btnModalDelete);

        tvTitle.setText(occurrence.getTaskTitle());
        tvCategory.setText(occurrence.getCategory());
        tvPriority.setText(occurrence.getPriority().getDisplayName());
        tvPriority.setTextColor(ColorUtils.getPriorityColor(occurrence.getPriority()));

        tvTime.setText("🕒 " + occurrence.getFormattedDate() + " at " + occurrence.getFormattedTime());

        if (task != null) {
            tvNagRule.setText(task.getNagSummary());
        } else {
            tvNagRule.setText("Persistent reminder until completed");
        }

        tvNagCount.setText("Reminders triggered: " + occurrence.getNagCount() + " times");

        if (task != null && !task.getDescription().isEmpty()) {
            tvDescription.setVisibility(View.VISIBLE);
            tvDescription.setText(task.getDescription());
        } else {
            tvDescription.setVisibility(View.GONE);
        }

        if (task != null && !task.getNotes().isEmpty()) {
            tvNotes.setVisibility(View.VISIBLE);
            tvNotes.setText("📝 Notes: " + task.getNotes());
        } else {
            tvNotes.setVisibility(View.GONE);
        }

        OccurrenceStatus status = occurrence.getStatus();
        tvStatus.setText(status.getDisplayName());
        int statusColorRes = R.color.status_upcoming_text;
        if (status == OccurrenceStatus.COMPLETED) statusColorRes = R.color.status_completed_text;
        else if (status == OccurrenceStatus.OVERDUE) statusColorRes = R.color.status_overdue_text;
        else if (status == OccurrenceStatus.DUE_NOW) statusColorRes = R.color.status_due_text;
        else if (status == OccurrenceStatus.SKIPPED) statusColorRes = R.color.status_skipped_text;
        tvStatus.setTextColor(ContextCompat.getColor(context, statusColorRes));

        if (occurrence.isTerminal()) {
            btnComplete.setVisibility(View.GONE);
            btnSnooze.setVisibility(View.GONE);
            btnSkip.setVisibility(View.GONE);
        } else {
            btnComplete.setVisibility(View.VISIBLE);
            btnSnooze.setVisibility(View.VISIBLE);
            btnSkip.setVisibility(View.VISIBLE);
        }

        btnComplete.setOnClickListener(v -> {
            repo.markOccurrenceCompleted(occurrence.getId(), null);
            Toast.makeText(context, "✓ Task marked as completed!", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        btnSnooze.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(context, btnSnooze);
            popup.getMenu().add(0, 5, 0, "⏰ Snooze 5 minutes");
            popup.getMenu().add(0, 10, 1, "⏰ Snooze 10 minutes");
            popup.getMenu().add(0, 15, 2, "⏰ Snooze 15 minutes");
            popup.getMenu().add(0, 30, 3, "⏰ Snooze 30 minutes");
            popup.getMenu().add(0, 60, 4, "⏰ Snooze 1 hour");
            popup.setOnMenuItemClickListener(item -> {
                repo.snoozeOccurrence(occurrence.getId(), item.getItemId());
                Toast.makeText(context, "⏰ Snoozed for " + item.getItemId() + "m", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                return true;
            });
            popup.show();
        });

        btnSkip.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Skip Task Occurrence")
                    .setMessage("Are you sure you want to skip this occurrence? Nagging reminders will stop for this instance.")
                    .setPositiveButton("Skip", (d, which) -> {
                        repo.markOccurrenceSkipped(occurrence.getId());
                        Toast.makeText(context, "✕ Task occurrence skipped", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        btnEdit.setOnClickListener(v -> {
            dialog.dismiss();
            Intent intent = new Intent(context, TaskEditorActivity.class);
            intent.putExtra(TaskEditorActivity.EXTRA_TASK_ID, occurrence.getTaskId());
            context.startActivity(intent);
        });

        btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Delete Task")
                    .setMessage("Are you sure you want to delete this task? All scheduled occurrences and reminders will be removed.")
                    .setPositiveButton("Delete", (d, which) -> {
                        repo.deleteTask(occurrence.getTaskId());
                        Toast.makeText(context, "Task deleted", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        dialog.show();
    }
}
