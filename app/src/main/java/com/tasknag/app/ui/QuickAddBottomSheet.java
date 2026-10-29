package com.tasknag.app.ui;

import android.app.TimePickerDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.widget.PopupMenu;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.textfield.TextInputEditText;
import com.tasknag.app.R;
import com.tasknag.app.data.TaskRepository;
import com.tasknag.app.model.Priority;
import com.tasknag.app.model.RecurrenceType;
import com.tasknag.app.model.Task;
import com.tasknag.app.util.DateTimeUtils;

import java.util.Calendar;
import java.util.Locale;

public class QuickAddBottomSheet {

    private static int selectedHour = 9;
    private static int selectedMinute = 0;
    private static int selectedNagInterval = 15;
    private static String selectedCategory = "PERSONAL";

    public static void show(Context context) {
        if (context == null) return;

        BottomSheetDialog dialog = new BottomSheetDialog(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_quick_add, null);
        dialog.setContentView(view);

        TextInputEditText etTitle = view.findViewById(R.id.etQuickTitle);
        Chip chipMeds = view.findViewById(R.id.chipPresetMeds);
        Chip chipWater = view.findViewById(R.id.chipPresetWater);
        Chip chipWorkout = view.findViewById(R.id.chipPresetWorkout);
        Chip chip15m = view.findViewById(R.id.chipPreset15m);
        MaterialButton btnPickTime = view.findViewById(R.id.btnQuickPickTime);
        MaterialButton btnPickNag = view.findViewById(R.id.btnQuickPickNag);
        MaterialButton btnSave = view.findViewById(R.id.btnQuickSave);

        // Default time: now + 30 minutes
        Calendar now = Calendar.getInstance();
        now.add(Calendar.MINUTE, 30);
        selectedHour = now.get(Calendar.HOUR_OF_DAY);
        selectedMinute = now.get(Calendar.MINUTE);
        selectedNagInterval = 15;
        selectedCategory = "PERSONAL";

        updateTimeButton(btnPickTime);
        updateNagButton(btnPickNag);

        // Presets
        chipMeds.setOnClickListener(v -> {
            etTitle.setText("Take Medicine");
            selectedCategory = "HEALTH";
            selectedHour = 11;
            selectedMinute = 0;
            selectedNagInterval = 15;
            updateTimeButton(btnPickTime);
            updateNagButton(btnPickNag);
        });

        chipWater.setOnClickListener(v -> {
            etTitle.setText("Drink Water (350ml)");
            selectedCategory = "HEALTH";
            Calendar c = Calendar.getInstance();
            c.add(Calendar.HOUR_OF_DAY, 2);
            selectedHour = c.get(Calendar.HOUR_OF_DAY);
            selectedMinute = c.get(Calendar.MINUTE);
            selectedNagInterval = 15;
            updateTimeButton(btnPickTime);
            updateNagButton(btnPickNag);
        });

        chipWorkout.setOnClickListener(v -> {
            etTitle.setText("Evening Workout");
            selectedCategory = "EXERCISE";
            selectedHour = 18;
            selectedMinute = 0;
            selectedNagInterval = 30;
            updateTimeButton(btnPickTime);
            updateNagButton(btnPickNag);
        });

        chip15m.setOnClickListener(v -> {
            etTitle.setText("Quick Task Reminder");
            selectedCategory = "PERSONAL";
            Calendar c = Calendar.getInstance();
            c.add(Calendar.MINUTE, 15);
            selectedHour = c.get(Calendar.HOUR_OF_DAY);
            selectedMinute = c.get(Calendar.MINUTE);
            selectedNagInterval = 5;
            updateTimeButton(btnPickTime);
            updateNagButton(btnPickNag);
        });

        btnPickTime.setOnClickListener(v -> {
            new TimePickerDialog(context, (view1, hourOfDay, minute) -> {
                selectedHour = hourOfDay;
                selectedMinute = minute;
                updateTimeButton(btnPickTime);
            }, selectedHour, selectedMinute, false).show();
        });

        btnPickNag.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(context, btnPickNag);
            popup.getMenu().add(0, 5, 0, "Nag every 5 min");
            popup.getMenu().add(0, 10, 1, "Nag every 10 min");
            popup.getMenu().add(0, 15, 2, "Nag every 15 min");
            popup.getMenu().add(0, 30, 3, "Nag every 30 min");
            popup.getMenu().add(0, 60, 4, "Nag every 60 min");
            popup.setOnMenuItemClickListener(item -> {
                selectedNagInterval = item.getItemId();
                updateNagButton(btnPickNag);
                return true;
            });
            popup.show();
        });

        btnSave.setOnClickListener(v -> {
            String title = etTitle.getText() != null ? etTitle.getText().toString().trim() : "";
            if (title.isEmpty()) {
                etTitle.setError("Please enter a task name");
                return;
            }

            String today = DateTimeUtils.getTodayDateString();
            String timeStr = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute);

            Task task = new Task(title, selectedCategory, today, timeStr);
            task.setPriority(Priority.HIGH);
            task.setRecurrenceType(RecurrenceType.ONE_TIME);
            task.setNagIntervalMinutes(selectedNagInterval);

            TaskRepository.getInstance(context).saveTask(task);
            Toast.makeText(context, "✓ Task added! Persistent reminders active.", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }

    private static void updateTimeButton(MaterialButton btn) {
        String amPm = selectedHour >= 12 ? "PM" : "AM";
        int h = selectedHour % 12;
        if (h == 0) h = 12;
        btn.setText(String.format(Locale.getDefault(), "🕒 %d:%02d %s", h, selectedMinute, amPm));
    }

    private static void updateNagButton(MaterialButton btn) {
        btn.setText("🔔 Nag every " + selectedNagInterval + "m");
    }
}
