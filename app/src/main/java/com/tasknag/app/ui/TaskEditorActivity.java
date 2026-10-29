package com.tasknag.app.ui;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.tasknag.app.R;
import com.tasknag.app.data.TaskRepository;
import com.tasknag.app.model.CategoryDef;
import com.tasknag.app.model.CustomIntervalUnit;
import com.tasknag.app.model.Priority;
import com.tasknag.app.model.RecurrenceType;
import com.tasknag.app.model.Task;
import com.tasknag.app.util.DateTimeUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class TaskEditorActivity extends AppCompatActivity {

    public static final String EXTRA_TASK_ID = "extra_task_id";

    private Task existingTask;
    private TaskRepository repository;

    private TextInputEditText etTitle;
    private TextInputEditText etDescription;
    private TextInputEditText etNotes;

    private ChipGroup chipGroupCategory;
    private Chip chipHealth, chipStudy, chipWork, chipPersonal, chipExercise, chipHousehold, chipCustomCat;

    private ChipGroup chipGroupPriority;
    private Chip chipPriorityLow, chipPriorityMedium, chipPriorityHigh, chipPriorityUrgent;

    private MaterialButton btnPickDate, btnPickTime;
    private MaterialButton btnPickStartDate, btnPickEndDate;

    private ChipGroup chipGroupRecurrence;
    private Chip chipRecurOneTime, chipRecurDaily, chipRecurWeekly, chipRecurMonthly, chipRecurCustom;

    private LinearLayout layoutWeeklyDays;
    private Chip chipDayMon, chipDayTue, chipDayWed, chipDayThu, chipDayFri, chipDaySat, chipDaySun;

    private LinearLayout layoutCustomInterval;
    private EditText etCustomIntervalValue;
    private Spinner spinnerCustomIntervalUnit;

    private ChipGroup chipGroupNagInterval;
    private Chip chipNag5m, chipNag10m, chipNag15m, chipNag30m, chipNag60m, chipNagCustom;
    private LinearLayout layoutCustomNag;
    private EditText etCustomNagMinutes;
    private Spinner spinnerMaxNagCount;

    private Spinner spinnerSound;
    private Spinner spinnerVibration;

    private MaterialButton btnSaveTask;

    // State variables
    private String selectedDate;
    private int selectedHour = 9;
    private int selectedMinute = 0;
    private String selectedStartDate;
    private String selectedEndDate;
    private String selectedCategory = "PERSONAL";
    private Priority selectedPriority = Priority.MEDIUM;
    private RecurrenceType selectedRecurrence = RecurrenceType.ONE_TIME;
    private int selectedNagInterval = 15;
    private int selectedMaxNagCount = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_editor);

        repository = TaskRepository.getInstance(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        bindViews();
        initDefaults();

        String taskId = getIntent().getStringExtra(EXTRA_TASK_ID);
        if (taskId != null) {
            existingTask = repository.getTaskById(taskId);
            if (existingTask != null) {
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle("Edit Task");
                }
                populateTask(existingTask);
            }
        }

        setupListeners();
    }

    private void bindViews() {
        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        etNotes = findViewById(R.id.etNotes);

        chipGroupCategory = findViewById(R.id.chipGroupCategory);
        chipHealth = findViewById(R.id.chipHealth);
        chipStudy = findViewById(R.id.chipStudy);
        chipWork = findViewById(R.id.chipWork);
        chipPersonal = findViewById(R.id.chipPersonal);
        chipExercise = findViewById(R.id.chipExercise);
        chipHousehold = findViewById(R.id.chipHousehold);
        chipCustomCat = findViewById(R.id.chipCustomCat);

        chipGroupPriority = findViewById(R.id.chipGroupPriority);
        chipPriorityLow = findViewById(R.id.chipPriorityLow);
        chipPriorityMedium = findViewById(R.id.chipPriorityMedium);
        chipPriorityHigh = findViewById(R.id.chipPriorityHigh);
        chipPriorityUrgent = findViewById(R.id.chipPriorityUrgent);

        btnPickDate = findViewById(R.id.btnPickDate);
        btnPickTime = findViewById(R.id.btnPickTime);
        btnPickStartDate = findViewById(R.id.btnPickStartDate);
        btnPickEndDate = findViewById(R.id.btnPickEndDate);

        chipGroupRecurrence = findViewById(R.id.chipGroupRecurrence);
        chipRecurOneTime = findViewById(R.id.chipRecurOneTime);
        chipRecurDaily = findViewById(R.id.chipRecurDaily);
        chipRecurWeekly = findViewById(R.id.chipRecurWeekly);
        chipRecurMonthly = findViewById(R.id.chipRecurMonthly);
        chipRecurCustom = findViewById(R.id.chipRecurCustom);

        layoutWeeklyDays = findViewById(R.id.layoutWeeklyDays);
        chipDayMon = findViewById(R.id.chipDayMon);
        chipDayTue = findViewById(R.id.chipDayTue);
        chipDayWed = findViewById(R.id.chipDayWed);
        chipDayThu = findViewById(R.id.chipDayThu);
        chipDayFri = findViewById(R.id.chipDayFri);
        chipDaySat = findViewById(R.id.chipDaySat);
        chipDaySun = findViewById(R.id.chipDaySun);

        layoutCustomInterval = findViewById(R.id.layoutCustomInterval);
        etCustomIntervalValue = findViewById(R.id.etCustomIntervalValue);
        spinnerCustomIntervalUnit = findViewById(R.id.spinnerCustomIntervalUnit);

        chipGroupNagInterval = findViewById(R.id.chipGroupNagInterval);
        chipNag5m = findViewById(R.id.chipNag5m);
        chipNag10m = findViewById(R.id.chipNag10m);
        chipNag15m = findViewById(R.id.chipNag15m);
        chipNag30m = findViewById(R.id.chipNag30m);
        chipNag60m = findViewById(R.id.chipNag60m);
        chipNagCustom = findViewById(R.id.chipNagCustom);
        layoutCustomNag = findViewById(R.id.layoutCustomNag);
        etCustomNagMinutes = findViewById(R.id.etCustomNagMinutes);
        spinnerMaxNagCount = findViewById(R.id.spinnerMaxNagCount);

        spinnerSound = findViewById(R.id.spinnerSound);
        spinnerVibration = findViewById(R.id.spinnerVibration);

        btnSaveTask = findViewById(R.id.btnSaveTask);
    }

    private void initDefaults() {
        selectedDate = DateTimeUtils.getTodayDateString();
        selectedStartDate = selectedDate;
        selectedEndDate = null;

        Calendar c = Calendar.getInstance();
        c.add(Calendar.HOUR_OF_DAY, 1);
        selectedHour = c.get(Calendar.HOUR_OF_DAY);
        selectedMinute = 0;

        updateDateButtonText();
        updateTimeButtonText();

        // Spinners
        String[] maxNagOptions = {"Repeat until completed (No limit)", "Max 3 reminders", "Max 5 reminders", "Max 10 reminders"};
        ArrayAdapter<String> maxNagAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, maxNagOptions);
        spinnerMaxNagCount.setAdapter(maxNagAdapter);

        String[] soundOptions = {"Default Ringtone", "Urgent Alarm Siren", "Gentle Chime", "Silent"};
        ArrayAdapter<String> soundAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, soundOptions);
        spinnerSound.setAdapter(soundAdapter);

        String[] vibrationOptions = {"Default Vibration", "Urgent Pulse (Strong)", "Long Buzz", "Gentle Vibrate", "Off"};
        ArrayAdapter<String> vibAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, vibrationOptions);
        spinnerVibration.setAdapter(vibAdapter);

        String[] unitOptions = {"Minutes", "Hours", "Days", "Weeks"};
        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, unitOptions);
        spinnerCustomIntervalUnit.setAdapter(unitAdapter);
        spinnerCustomIntervalUnit.setSelection(1); // default Hours

        chipPersonal.setChecked(true);
        chipPriorityMedium.setChecked(true);
        chipRecurOneTime.setChecked(true);
        chipNag15m.setChecked(true);
    }

    private void populateTask(Task task) {
        etTitle.setText(task.getTitle());
        etDescription.setText(task.getDescription());
        etNotes.setText(task.getNotes());

        selectedCategory = task.getCategory();
        setCategoryChipChecked(selectedCategory);

        selectedPriority = task.getPriority();
        setPriorityChipChecked(selectedPriority);

        selectedDate = task.getScheduledDate();
        if (task.getScheduledTime() != null && task.getScheduledTime().contains(":")) {
            String[] parts = task.getScheduledTime().split(":");
            try {
                selectedHour = Integer.parseInt(parts[0]);
                selectedMinute = Integer.parseInt(parts[1]);
            } catch (Exception ignored) {}
        }
        updateDateButtonText();
        updateTimeButtonText();

        selectedStartDate = task.getStartDate();
        selectedEndDate = task.getEndDate();
        btnPickStartDate.setText("Start: " + (selectedStartDate != null ? selectedStartDate : "Today"));
        btnPickEndDate.setText("End: " + (selectedEndDate != null ? selectedEndDate : "Indefinite"));

        selectedRecurrence = task.getRecurrenceType();
        setRecurrenceChipChecked(selectedRecurrence);

        if (selectedRecurrence == RecurrenceType.WEEKLY) {
            List<Integer> days = task.getRecurrenceRule().getWeeklyDays();
            chipDayMon.setChecked(days.contains(Calendar.MONDAY));
            chipDayTue.setChecked(days.contains(Calendar.TUESDAY));
            chipDayWed.setChecked(days.contains(Calendar.WEDNESDAY));
            chipDayThu.setChecked(days.contains(Calendar.THURSDAY));
            chipDayFri.setChecked(days.contains(Calendar.FRIDAY));
            chipDaySat.setChecked(days.contains(Calendar.SATURDAY));
            chipDaySun.setChecked(days.contains(Calendar.SUNDAY));
        } else if (selectedRecurrence == RecurrenceType.CUSTOM_INTERVAL) {
            etCustomIntervalValue.setText(String.valueOf(task.getRecurrenceRule().getCustomValue()));
            CustomIntervalUnit unit = task.getRecurrenceRule().getCustomUnit();
            if (unit == CustomIntervalUnit.MINUTES) spinnerCustomIntervalUnit.setSelection(0);
            else if (unit == CustomIntervalUnit.HOURS) spinnerCustomIntervalUnit.setSelection(1);
            else if (unit == CustomIntervalUnit.DAYS) spinnerCustomIntervalUnit.setSelection(2);
            else if (unit == CustomIntervalUnit.WEEKS) spinnerCustomIntervalUnit.setSelection(3);
        }

        selectedNagInterval = task.getNagIntervalMinutes();
        setNagChipChecked(selectedNagInterval);

        int max = task.getNagMaxCount();
        if (max == 3) spinnerMaxNagCount.setSelection(1);
        else if (max == 5) spinnerMaxNagCount.setSelection(2);
        else if (max == 10) spinnerMaxNagCount.setSelection(3);
        else spinnerMaxNagCount.setSelection(0);

        btnSaveTask.setText("✓ Update Task & Refresh Reminders");
    }

    private void setupListeners() {
        // Date & Time pickers
        btnPickDate.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                updateDateButtonText();
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
        });

        btnPickTime.setOnClickListener(v -> {
            new TimePickerDialog(this, (view, hourOfDay, minute) -> {
                selectedHour = hourOfDay;
                selectedMinute = minute;
                updateTimeButtonText();
            }, selectedHour, selectedMinute, false).show();
        });

        btnPickStartDate.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                selectedStartDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                btnPickStartDate.setText("Start: " + selectedStartDate);
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
        });

        btnPickEndDate.setOnClickListener(v -> {
            String[] options = {"Pick Specific End Date", "Clear (Indefinite)"};
            new AlertDialog.Builder(this)
                    .setTitle("End Date Setting")
                    .setItems(options, (dialog, which) -> {
                        if (which == 0) {
                            Calendar cal = Calendar.getInstance();
                            new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                                selectedEndDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                                btnPickEndDate.setText("End: " + selectedEndDate);
                            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
                        } else {
                            selectedEndDate = null;
                            btnPickEndDate.setText("End: Indefinite");
                        }
                    }).show();
        });

        // Category chips
        chipGroupCategory.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chipHealth) selectedCategory = "HEALTH";
            else if (checkedId == R.id.chipStudy) selectedCategory = "STUDY";
            else if (checkedId == R.id.chipWork) selectedCategory = "WORK";
            else if (checkedId == R.id.chipPersonal) selectedCategory = "PERSONAL";
            else if (checkedId == R.id.chipExercise) selectedCategory = "EXERCISE";
            else if (checkedId == R.id.chipHousehold) selectedCategory = "HOUSEHOLD";
            else if (checkedId == R.id.chipCustomCat) {
                showAddCustomCategoryDialog();
            }
        });

        // Priority chips
        chipGroupPriority.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chipPriorityLow) selectedPriority = Priority.LOW;
            else if (checkedId == R.id.chipPriorityMedium) selectedPriority = Priority.MEDIUM;
            else if (checkedId == R.id.chipPriorityHigh) selectedPriority = Priority.HIGH;
            else if (checkedId == R.id.chipPriorityUrgent) selectedPriority = Priority.URGENT;
        });

        // Recurrence chips
        chipGroupRecurrence.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chipRecurOneTime) {
                selectedRecurrence = RecurrenceType.ONE_TIME;
                layoutWeeklyDays.setVisibility(View.GONE);
                layoutCustomInterval.setVisibility(View.GONE);
            } else if (checkedId == R.id.chipRecurDaily) {
                selectedRecurrence = RecurrenceType.DAILY;
                layoutWeeklyDays.setVisibility(View.GONE);
                layoutCustomInterval.setVisibility(View.GONE);
            } else if (checkedId == R.id.chipRecurWeekly) {
                selectedRecurrence = RecurrenceType.WEEKLY;
                layoutWeeklyDays.setVisibility(View.VISIBLE);
                layoutCustomInterval.setVisibility(View.GONE);
            } else if (checkedId == R.id.chipRecurMonthly) {
                selectedRecurrence = RecurrenceType.MONTHLY;
                layoutWeeklyDays.setVisibility(View.GONE);
                layoutCustomInterval.setVisibility(View.GONE);
            } else if (checkedId == R.id.chipRecurCustom) {
                selectedRecurrence = RecurrenceType.CUSTOM_INTERVAL;
                layoutWeeklyDays.setVisibility(View.GONE);
                layoutCustomInterval.setVisibility(View.VISIBLE);
            }
        });

        // Nag repeat chips
        chipGroupNagInterval.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chipNag5m) {
                selectedNagInterval = 5;
                layoutCustomNag.setVisibility(View.GONE);
            } else if (checkedId == R.id.chipNag10m) {
                selectedNagInterval = 10;
                layoutCustomNag.setVisibility(View.GONE);
            } else if (checkedId == R.id.chipNag15m) {
                selectedNagInterval = 15;
                layoutCustomNag.setVisibility(View.GONE);
            } else if (checkedId == R.id.chipNag30m) {
                selectedNagInterval = 30;
                layoutCustomNag.setVisibility(View.GONE);
            } else if (checkedId == R.id.chipNag60m) {
                selectedNagInterval = 60;
                layoutCustomNag.setVisibility(View.GONE);
            } else if (checkedId == R.id.chipNagCustom) {
                layoutCustomNag.setVisibility(View.VISIBLE);
            }
        });

        // Save Button
        btnSaveTask.setOnClickListener(v -> saveTask());
    }

    private void updateDateButtonText() {
        btnPickDate.setText("📅 " + selectedDate);
    }

    private void updateTimeButtonText() {
        String amPm = selectedHour >= 12 ? "PM" : "AM";
        int h = selectedHour % 12;
        if (h == 0) h = 12;
        btnPickTime.setText(String.format(Locale.getDefault(), "🕒 %d:%02d %s", h, selectedMinute, amPm));
    }

    private void setCategoryChipChecked(String category) {
        if ("HEALTH".equalsIgnoreCase(category)) chipHealth.setChecked(true);
        else if ("STUDY".equalsIgnoreCase(category)) chipStudy.setChecked(true);
        else if ("WORK".equalsIgnoreCase(category)) chipWork.setChecked(true);
        else if ("PERSONAL".equalsIgnoreCase(category)) chipPersonal.setChecked(true);
        else if ("EXERCISE".equalsIgnoreCase(category)) chipExercise.setChecked(true);
        else if ("HOUSEHOLD".equalsIgnoreCase(category)) chipHousehold.setChecked(true);
        else chipPersonal.setChecked(true);
    }

    private void setPriorityChipChecked(Priority p) {
        if (p == Priority.LOW) chipPriorityLow.setChecked(true);
        else if (p == Priority.MEDIUM) chipPriorityMedium.setChecked(true);
        else if (p == Priority.HIGH) chipPriorityHigh.setChecked(true);
        else if (p == Priority.URGENT) chipPriorityUrgent.setChecked(true);
    }

    private void setRecurrenceChipChecked(RecurrenceType type) {
        if (type == RecurrenceType.ONE_TIME) chipRecurOneTime.setChecked(true);
        else if (type == RecurrenceType.DAILY) chipRecurDaily.setChecked(true);
        else if (type == RecurrenceType.WEEKLY) chipRecurWeekly.setChecked(true);
        else if (type == RecurrenceType.MONTHLY) chipRecurMonthly.setChecked(true);
        else if (type == RecurrenceType.CUSTOM_INTERVAL) chipRecurCustom.setChecked(true);
    }

    private void setNagChipChecked(int minutes) {
        if (minutes == 5) chipNag5m.setChecked(true);
        else if (minutes == 10) chipNag10m.setChecked(true);
        else if (minutes == 15) chipNag15m.setChecked(true);
        else if (minutes == 30) chipNag30m.setChecked(true);
        else if (minutes == 60) chipNag60m.setChecked(true);
        else {
            chipNagCustom.setChecked(true);
            layoutCustomNag.setVisibility(View.VISIBLE);
            etCustomNagMinutes.setText(String.valueOf(minutes));
        }
    }

    private void showAddCustomCategoryDialog() {
        EditText input = new EditText(this);
        input.setHint("Category name (e.g. Finance, Medication)");
        new AlertDialog.Builder(this)
                .setTitle("Create Custom Category")
                .setView(input)
                .setPositiveButton("Add", (dialog, which) -> {
                    String name = input.getText().toString().trim();
                    if (!name.isEmpty()) {
                        selectedCategory = name.toUpperCase();
                        repository.addCustomCategory(name, "#8E24AA", "ic_custom");
                        chipCustomCat.setText(name);
                        chipCustomCat.setChecked(true);
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> chipPersonal.setChecked(true))
                .show();
    }

    private void saveTask() {
        String title = etTitle.getText() != null ? etTitle.getText().toString().trim() : "";
        if (title.isEmpty()) {
            etTitle.setError("Please enter a task name");
            etTitle.requestFocus();
            return;
        }

        Task task = existingTask != null ? existingTask : new Task();
        task.setTitle(title);
        task.setDescription(etDescription.getText() != null ? etDescription.getText().toString().trim() : "");
        task.setNotes(etNotes.getText() != null ? etNotes.getText().toString().trim() : "");
        task.setCategory(selectedCategory);
        task.setPriority(selectedPriority);

        task.setScheduledDate(selectedDate);
        task.setScheduledTime(String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute));

        task.setStartDate(selectedStartDate != null ? selectedStartDate : selectedDate);
        task.setEndDate(selectedEndDate);
        task.setRecurrenceType(selectedRecurrence);

        // Recurrence rule setup
        if (selectedRecurrence == RecurrenceType.WEEKLY) {
            List<Integer> days = new ArrayList<>();
            if (chipDayMon.isChecked()) days.add(Calendar.MONDAY);
            if (chipDayTue.isChecked()) days.add(Calendar.TUESDAY);
            if (chipDayWed.isChecked()) days.add(Calendar.WEDNESDAY);
            if (chipDayThu.isChecked()) days.add(Calendar.THURSDAY);
            if (chipDayFri.isChecked()) days.add(Calendar.FRIDAY);
            if (chipDaySat.isChecked()) days.add(Calendar.SATURDAY);
            if (chipDaySun.isChecked()) days.add(Calendar.SUNDAY);
            if (days.isEmpty()) days.add(Calendar.MONDAY);
            task.getRecurrenceRule().setWeeklyDays(days);
        } else if (selectedRecurrence == RecurrenceType.MONTHLY) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(DateTimeUtils.parseDateTimeToEpoch(selectedDate, "00:00"));
            task.getRecurrenceRule().setMonthlyDay(cal.get(Calendar.DAY_OF_MONTH));
        } else if (selectedRecurrence == RecurrenceType.CUSTOM_INTERVAL) {
            int val = 1;
            try {
                val = Integer.parseInt(etCustomIntervalValue.getText().toString().trim());
            } catch (Exception ignored) {}
            task.getRecurrenceRule().setCustomValue(Math.max(1, val));
            int unitPos = spinnerCustomIntervalUnit.getSelectedItemPosition();
            if (unitPos == 0) task.getRecurrenceRule().setCustomUnit(CustomIntervalUnit.MINUTES);
            else if (unitPos == 1) task.getRecurrenceRule().setCustomUnit(CustomIntervalUnit.HOURS);
            else if (unitPos == 2) task.getRecurrenceRule().setCustomUnit(CustomIntervalUnit.DAYS);
            else task.getRecurrenceRule().setCustomUnit(CustomIntervalUnit.WEEKS);
        }

        // Persistent nag interval
        if (chipNagCustom.isChecked()) {
            int customMinutes = 15;
            try {
                customMinutes = Integer.parseInt(etCustomNagMinutes.getText().toString().trim());
            } catch (Exception ignored) {}
            task.setNagIntervalMinutes(Math.max(1, customMinutes));
        } else {
            task.setNagIntervalMinutes(selectedNagInterval);
        }

        // Max nag limit
        int maxPos = spinnerMaxNagCount.getSelectedItemPosition();
        if (maxPos == 1) task.setNagMaxCount(3);
        else if (maxPos == 2) task.setNagMaxCount(5);
        else if (maxPos == 3) task.setNagMaxCount(10);
        else task.setNagMaxCount(0); // unlimited

        // Sound & Vibration
        int soundPos = spinnerSound.getSelectedItemPosition();
        if (soundPos == 1) task.setSoundType("ALARM");
        else if (soundPos == 2) task.setSoundType("GENTLE");
        else if (soundPos == 3) task.setSoundType("SILENT");
        else task.setSoundType("DEFAULT");

        int vibPos = spinnerVibration.getSelectedItemPosition();
        if (vibPos == 1) task.setVibrationPattern("URGENT_PULSE");
        else if (vibPos == 2) task.setVibrationPattern("LONG_BUZZ");
        else if (vibPos == 3) task.setVibrationPattern("GENTLE");
        else if (vibPos == 4) task.setVibrationPattern("OFF");
        else task.setVibrationPattern("DEFAULT");

        // Color
        CategoryDef catDef = CategoryDef.findCategory(repository.getCategories(), selectedCategory);
        task.setColorHex(catDef.getColorHex());

        repository.saveTask(task);

        Toast.makeText(this, "✓ Task saved! Reminders will keep nagging until finished.", Toast.LENGTH_LONG).show();
        finish();
    }
}
