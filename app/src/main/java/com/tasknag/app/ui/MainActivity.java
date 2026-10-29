package com.tasknag.app.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tasknag.app.TaskNagApp;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.tasknag.app.R;
import com.tasknag.app.data.TaskRepository;
import com.tasknag.app.model.OccurrenceStatus;
import com.tasknag.app.model.Priority;
import com.tasknag.app.model.ProductivitySummary;
import com.tasknag.app.model.RecurrenceType;
import com.tasknag.app.model.Task;
import com.tasknag.app.model.TaskOccurrence;
import com.tasknag.app.reminder.NagAlarmScheduler;
import com.tasknag.app.reminder.NagNotificationHelper;
import com.tasknag.app.ui.adapters.DateStripAdapter;
import com.tasknag.app.ui.adapters.HistoryAdapter;
import com.tasknag.app.ui.adapters.RecurringTaskAdapter;
import com.tasknag.app.ui.adapters.TaskOccurrenceAdapter;
import com.tasknag.app.util.DateTimeUtils;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements TaskRepository.RepositoryChangeListener {

    private static final int PERMISSION_REQ_NOTIFICATIONS = 1001;

    private TaskRepository repository;

    // View Containers for 4 pages
    private View scrollDashboard;
    private View layoutAgendaView;
    private View layoutRecurringView;
    private View layoutProductivityView;

    // Page 1: Dashboard
    private CardView cardActiveNagBanner;
    private TextView tvNagBannerHeader;
    private TextView tvNagBannerCount;
    private TextView tvNagBannerTitle;
    private TextView tvNagBannerSubtitle;
    private MaterialButton btnBannerComplete, btnBannerSnooze, btnBannerSkip;
    private TaskOccurrence activeNagOccurrence;

    private TextView tvProgressFraction;
    private ProgressBar progressBarToday;
    private TextView tvCompletedStat, tvOverdueStat, tvRemainingStat;
    private TextView btnOpenQuickAddModal;
    private ChipGroup chipGroupDashboardFilters;
    private RecyclerView recyclerDashboard;
    private TextView tvDashboardEmpty;
    private TaskOccurrenceAdapter dashboardAdapter;

    // Page 2: Agenda
    private RecyclerView recyclerDateStrip;
    private TextView tvAgendaDateHeader;
    private RecyclerView recyclerAgenda;
    private TextView tvAgendaEmpty;
    private DateStripAdapter dateStripAdapter;
    private TaskOccurrenceAdapter agendaAdapter;

    // Page 3: Recurring
    private RecyclerView recyclerRecurring;
    private TextView tvRecurringEmpty;
    private RecurringTaskAdapter recurringAdapter;

    // Page 4: Productivity
    private TextView tvProdTodaySubtitle, tvProdMotivational, tvProdPercentage;
    private LinearLayout layoutWeeklyBars;
    private TextView tvStatTotalDone, tvStatTotalOverdue, tvStatTotalSkipped, tvStatTotalMissed;
    private LinearLayout layoutStreakList;
    private RecyclerView recyclerHistory;
    private HistoryAdapter historyAdapter;

    // Navigation & FAB
    private BottomNavigationView bottomNav;
    private FloatingActionButton fabAddTask;

    private int currentFilterId = R.id.chipFilterToday;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        repository = TaskRepository.getInstance(this);
        repository.addListener(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        bindViews();
        setupAdapters();
        setupNavigation();
        setupDashboardFilters();
        setupBannerActions();
        checkPermissions();

        refreshAll();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshAll();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        repository.removeListener(this);
    }

    @Override
    public void onDataChanged() {
        runOnUiThread(this::refreshAll);
    }

    private void bindViews() {
        scrollDashboard = findViewById(R.id.scrollDashboard);
        layoutAgendaView = findViewById(R.id.layoutAgendaView);
        layoutRecurringView = findViewById(R.id.layoutRecurringView);
        layoutProductivityView = findViewById(R.id.layoutProductivityView);

        // Dashboard
        cardActiveNagBanner = findViewById(R.id.cardActiveNagBanner);
        tvNagBannerHeader = findViewById(R.id.tvNagBannerHeader);
        tvNagBannerCount = findViewById(R.id.tvNagBannerCount);
        tvNagBannerTitle = findViewById(R.id.tvNagBannerTitle);
        tvNagBannerSubtitle = findViewById(R.id.tvNagBannerSubtitle);
        btnBannerComplete = findViewById(R.id.btnBannerComplete);
        btnBannerSnooze = findViewById(R.id.btnBannerSnooze);
        btnBannerSkip = findViewById(R.id.btnBannerSkip);

        tvProgressFraction = findViewById(R.id.tvProgressFraction);
        progressBarToday = findViewById(R.id.progressBarToday);
        tvCompletedStat = findViewById(R.id.tvCompletedStat);
        tvOverdueStat = findViewById(R.id.tvOverdueStat);
        tvRemainingStat = findViewById(R.id.tvRemainingStat);
        btnOpenQuickAddModal = findViewById(R.id.btnOpenQuickAddModal);
        chipGroupDashboardFilters = findViewById(R.id.chipGroupDashboardFilters);
        recyclerDashboard = findViewById(R.id.recyclerDashboard);
        tvDashboardEmpty = findViewById(R.id.tvDashboardEmpty);

        // Agenda
        recyclerDateStrip = findViewById(R.id.recyclerDateStrip);
        tvAgendaDateHeader = findViewById(R.id.tvAgendaDateHeader);
        recyclerAgenda = findViewById(R.id.recyclerAgenda);
        tvAgendaEmpty = findViewById(R.id.tvAgendaEmpty);

        // Recurring
        recyclerRecurring = findViewById(R.id.recyclerRecurring);
        tvRecurringEmpty = findViewById(R.id.tvRecurringEmpty);

        // Productivity
        tvProdTodaySubtitle = findViewById(R.id.tvProdTodaySubtitle);
        tvProdMotivational = findViewById(R.id.tvProdMotivational);
        tvProdPercentage = findViewById(R.id.tvProdPercentage);
        layoutWeeklyBars = findViewById(R.id.layoutWeeklyBars);
        tvStatTotalDone = findViewById(R.id.tvStatTotalDone);
        tvStatTotalOverdue = findViewById(R.id.tvStatTotalOverdue);
        tvStatTotalSkipped = findViewById(R.id.tvStatTotalSkipped);
        tvStatTotalMissed = findViewById(R.id.tvStatTotalMissed);
        layoutStreakList = findViewById(R.id.layoutStreakList);
        recyclerHistory = findViewById(R.id.recyclerHistory);

        // Navigation
        bottomNav = findViewById(R.id.bottomNav);
        fabAddTask = findViewById(R.id.fabAddTask);

        btnOpenQuickAddModal.setOnClickListener(v -> QuickAddBottomSheet.show(this));

        fabAddTask.setOnClickListener(v -> {
            Intent intent = new Intent(this, TaskEditorActivity.class);
            startActivity(intent);
        });
    }

    private void setupAdapters() {
        TaskOccurrenceAdapter.OnOccurrenceActionListener occListener = new TaskOccurrenceAdapter.OnOccurrenceActionListener() {
            @Override
            public void onComplete(TaskOccurrence occurrence) {
                repository.markOccurrenceCompleted(occurrence.getId(), null);
                Toast.makeText(MainActivity.this, "✓ " + occurrence.getTaskTitle() + " completed!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSnooze(TaskOccurrence occurrence, int snoozeMinutes) {
                repository.snoozeOccurrence(occurrence.getId(), snoozeMinutes);
                Toast.makeText(MainActivity.this, "⏰ Snoozed for " + snoozeMinutes + "m", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSkip(TaskOccurrence occurrence) {
                repository.markOccurrenceSkipped(occurrence.getId());
                Toast.makeText(MainActivity.this, "✕ " + occurrence.getTaskTitle() + " skipped", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onItemClick(TaskOccurrence occurrence) {
                TaskDetailBottomSheet.show(MainActivity.this, occurrence);
            }
        };

        // Dashboard adapter
        dashboardAdapter = new TaskOccurrenceAdapter(this, occListener);
        recyclerDashboard.setLayoutManager(new LinearLayoutManager(this));
        recyclerDashboard.setAdapter(dashboardAdapter);

        // Agenda adapter
        agendaAdapter = new TaskOccurrenceAdapter(this, occListener);
        recyclerAgenda.setLayoutManager(new LinearLayoutManager(this));
        recyclerAgenda.setAdapter(agendaAdapter);

        // Date strip adapter
        dateStripAdapter = new DateStripAdapter(this, dateStr -> {
            tvAgendaDateHeader.setText("Tasks for " + dateStr);
            loadAgendaForDate(dateStr);
        });
        recyclerDateStrip.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerDateStrip.setAdapter(dateStripAdapter);

        // Recurring adapter
        recurringAdapter = new RecurringTaskAdapter(this, new RecurringTaskAdapter.OnRecurringActionListener() {
            @Override
            public void onToggleEnabled(Task task, boolean isEnabled) {
                repository.toggleTaskEnabled(task.getId(), isEnabled);
                Toast.makeText(MainActivity.this, (isEnabled ? "Resumed " : "Paused ") + task.getTitle(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onEdit(Task task) {
                Intent intent = new Intent(MainActivity.this, TaskEditorActivity.class);
                intent.putExtra(TaskEditorActivity.EXTRA_TASK_ID, task.getId());
                startActivity(intent);
            }

            @Override
            public void onDelete(Task task) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Delete Recurring Task")
                        .setMessage("Are you sure you want to delete '" + task.getTitle() + "'? Future reminders will be canceled.")
                        .setPositiveButton("Delete", (d, which) -> {
                            repository.deleteTask(task.getId());
                            Toast.makeText(MainActivity.this, "Deleted " + task.getTitle(), Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });
        recyclerRecurring.setLayoutManager(new LinearLayoutManager(this));
        recyclerRecurring.setAdapter(recurringAdapter);

        // History adapter
        historyAdapter = new HistoryAdapter(this);
        recyclerHistory.setLayoutManager(new LinearLayoutManager(this));
        recyclerHistory.setAdapter(historyAdapter);
    }

    private void setupNavigation() {
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_dashboard) {
                scrollDashboard.setVisibility(View.VISIBLE);
                layoutAgendaView.setVisibility(View.GONE);
                layoutRecurringView.setVisibility(View.GONE);
                layoutProductivityView.setVisibility(View.GONE);
                fabAddTask.show();
                return true;
            } else if (id == R.id.nav_agenda) {
                scrollDashboard.setVisibility(View.GONE);
                layoutAgendaView.setVisibility(View.VISIBLE);
                layoutRecurringView.setVisibility(View.GONE);
                layoutProductivityView.setVisibility(View.GONE);
                fabAddTask.show();
                loadAgendaForDate(dateStripAdapter.getSelectedDate());
                return true;
            } else if (id == R.id.nav_recurring) {
                scrollDashboard.setVisibility(View.GONE);
                layoutAgendaView.setVisibility(View.GONE);
                layoutRecurringView.setVisibility(View.VISIBLE);
                layoutProductivityView.setVisibility(View.GONE);
                fabAddTask.show();
                loadRecurringTasks();
                return true;
            } else if (id == R.id.nav_analytics) {
                scrollDashboard.setVisibility(View.GONE);
                layoutAgendaView.setVisibility(View.GONE);
                layoutRecurringView.setVisibility(View.GONE);
                layoutProductivityView.setVisibility(View.VISIBLE);
                fabAddTask.hide();
                loadProductivity();
                return true;
            }
            return false;
        });
    }

    private void setupDashboardFilters() {
        chipGroupDashboardFilters.setOnCheckedChangeListener((group, checkedId) -> {
            currentFilterId = checkedId;
            loadDashboardList();
        });
    }

    private void setupBannerActions() {
        btnBannerComplete.setOnClickListener(v -> {
            if (activeNagOccurrence != null) {
                repository.markOccurrenceCompleted(activeNagOccurrence.getId(), null);
                Toast.makeText(this, "✓ " + activeNagOccurrence.getTaskTitle() + " completed!", Toast.LENGTH_SHORT).show();
            }
        });

        btnBannerSnooze.setOnClickListener(v -> {
            if (activeNagOccurrence != null) {
                repository.snoozeOccurrence(activeNagOccurrence.getId(), 10);
                Toast.makeText(this, "⏰ Snoozed for 10m", Toast.LENGTH_SHORT).show();
            }
        });

        btnBannerSkip.setOnClickListener(v -> {
            if (activeNagOccurrence != null) {
                repository.markOccurrenceSkipped(activeNagOccurrence.getId());
                Toast.makeText(this, "✕ " + activeNagOccurrence.getTaskTitle() + " skipped", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void refreshAll() {
        long now = System.currentTimeMillis();
        repository.refreshStatuses(now);

        updateActiveNagBanner(now);
        updateTodayProgress(now);
        loadDashboardList();

        if (layoutAgendaView.getVisibility() == View.VISIBLE) {
            loadAgendaForDate(dateStripAdapter.getSelectedDate());
        } else if (layoutRecurringView.getVisibility() == View.VISIBLE) {
            loadRecurringTasks();
        } else if (layoutProductivityView.getVisibility() == View.VISIBLE) {
            loadProductivity();
        }
    }

    private void updateActiveNagBanner(long now) {
        List<TaskOccurrence> overdues = repository.getOverdueOccurrences(now);
        List<TaskOccurrence> dueNows = repository.getDueNowOccurrences(now);

        TaskOccurrence target = null;
        if (!overdues.isEmpty()) {
            target = overdues.get(0);
        } else if (!dueNows.isEmpty()) {
            target = dueNows.get(0);
        }

        if (target != null) {
            activeNagOccurrence = target;
            cardActiveNagBanner.setVisibility(View.VISIBLE);
            tvNagBannerTitle.setText(target.getTaskTitle());

            long overdueMin = target.getOverdueMinutes(now);
            if (overdueMin > 0) {
                tvNagBannerHeader.setText("🚨 TASK OVERDUE • NAGGING ACTIVE");
                tvNagBannerHeader.setTextColor(ContextCompat.getColor(this, R.color.banner_title));
                tvNagBannerSubtitle.setText("Overdue by " + overdueMin + "m • Reminding until completed");
                tvNagBannerSubtitle.setTextColor(ContextCompat.getColor(this, R.color.banner_subtitle));
            } else {
                tvNagBannerHeader.setText("⚡ TASK DUE NOW • ACTION REQUIRED");
                tvNagBannerHeader.setTextColor(ContextCompat.getColor(this, R.color.banner_subtitle));
                tvNagBannerSubtitle.setText("Due right now (" + target.getFormattedTime() + ")");
                tvNagBannerSubtitle.setTextColor(ContextCompat.getColor(this, R.color.banner_subtitle));
            }

            int nags = target.getNagCount();
            if (nags > 0) {
                tvNagBannerCount.setVisibility(View.VISIBLE);
                tvNagBannerCount.setText("Reminder #" + nags);
            } else {
                tvNagBannerCount.setVisibility(View.GONE);
            }
        } else {
            activeNagOccurrence = null;
            cardActiveNagBanner.setVisibility(View.GONE);
        }
    }

    private void updateTodayProgress(long now) {
        ProductivitySummary summary = repository.calculateProductivitySummary(now);
        int sched = summary.getTodayScheduled();
        int comp = summary.getTodayCompleted();
        int pct = summary.getTodayPercentage();

        tvProgressFraction.setText(comp + " / " + sched + " Done (" + pct + "%)");
        progressBarToday.setProgress(pct);

        tvCompletedStat.setText("✅ " + comp + " Completed");
        tvOverdueStat.setText("⚠️ " + summary.getTodayOverdue() + " Overdue");
        tvRemainingStat.setText("🕒 " + summary.getTodayRemaining() + " Remaining");
    }

    private void loadDashboardList() {
        long now = System.currentTimeMillis();
        List<TaskOccurrence> list;

        if (currentFilterId == R.id.chipFilterDueOverdue) {
            list = new ArrayList<>(repository.getOverdueOccurrences(now));
            list.addAll(repository.getDueNowOccurrences(now));
        } else if (currentFilterId == R.id.chipFilterUpcoming) {
            list = repository.getUpcomingToday(now);
        } else if (currentFilterId == R.id.chipFilterCompleted) {
            list = repository.getCompletedToday(now);
        } else if (currentFilterId == R.id.chipFilterAll) {
            list = repository.getAllOccurrences();
        } else {
            // Default: Today
            list = repository.getOccurrencesForDate(DateTimeUtils.getTodayDateString());
        }

        dashboardAdapter.setItems(list);
        tvDashboardEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void loadAgendaForDate(String dateStr) {
        List<TaskOccurrence> list = repository.getOccurrencesForDate(dateStr);
        agendaAdapter.setItems(list);
        tvAgendaEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void loadRecurringTasks() {
        List<Task> tasks = repository.getAllTasks();
        recurringAdapter.setTasks(tasks);
        tvRecurringEmpty.setVisibility(recurringAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

    private void loadProductivity() {
        long now = System.currentTimeMillis();
        ProductivitySummary summary = repository.calculateProductivitySummary(now);

        tvProdTodaySubtitle.setText(summary.getTodayCompleted() + " of " + summary.getTodayScheduled() + " tasks finished");
        tvProdPercentage.setText(summary.getTodayPercentage() + "%");

        if (summary.getTodayPercentage() == 100 && summary.getTodayScheduled() > 0) {
            tvProdMotivational.setText("🏆 Flawless victory! All scheduled tasks completed!");
            tvProdMotivational.setTextColor(ContextCompat.getColor(this, R.color.status_completed_text));
        } else if (summary.getTodayOverdue() > 0) {
            tvProdMotivational.setText("⚠️ " + summary.getTodayOverdue() + " task(s) overdue. Persistent reminders will keep nagging!");
            tvProdMotivational.setTextColor(ContextCompat.getColor(this, R.color.status_overdue_text));
        } else {
            tvProdMotivational.setText("🔥 Good momentum! Keep knocking them out.");
            tvProdMotivational.setTextColor(ContextCompat.getColor(this, R.color.primary));
        }

        // Stats grid
        tvStatTotalDone.setText("Total Done: " + summary.getTotalCompletedAllTime());
        tvStatTotalOverdue.setText("Active Overdue: " + summary.getTotalOverdueCurrently());
        tvStatTotalSkipped.setText("Skipped: " + summary.getTotalSkipped());
        tvStatTotalMissed.setText("Missed: " + summary.getTotalMissed());

        // 7-day bars
        layoutWeeklyBars.removeAllViews();
        List<ProductivitySummary.DailyMetric> metrics = summary.getLast7Days();
        for (ProductivitySummary.DailyMetric m : metrics) {
            LinearLayout col = new LinearLayout(this);
            col.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));
            col.setOrientation(LinearLayout.VERTICAL);
            col.setGravity(Gravity.CENTER_HORIZONTAL);

            TextView tvPct = new TextView(this);
            tvPct.setText(m.getPercentage() + "%");
            tvPct.setTextSize(10);
            tvPct.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
            col.addView(tvPct);

            ProgressBar bar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(28, 120);
            lp.topMargin = 4;
            lp.bottomMargin = 4;
            bar.setLayoutParams(lp);
            bar.setRotation(-90);
            bar.setProgress(m.getPercentage());
            col.addView(bar);

            TextView tvDay = new TextView(this);
            tvDay.setText(m.dateStr);
            tvDay.setTextSize(10);
            tvDay.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
            col.addView(tvDay);

            layoutWeeklyBars.addView(col);
        }

        // Streaks
        layoutStreakList.removeAllViews();
        List<ProductivitySummary.StreakItem> streaks = summary.getStreaks();
        if (streaks.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("No recurring habits tracked yet.");
            empty.setTextSize(13);
            empty.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
            layoutStreakList.addView(empty);
        } else {
            for (ProductivitySummary.StreakItem s : streaks) {
                LinearLayout row = new LinearLayout(this);
                row.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setPadding(0, 8, 0, 8);

                TextView tvName = new TextView(this);
                tvName.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));
                tvName.setText(s.taskTitle);
                tvName.setTextSize(14);
                tvName.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
                row.addView(tvName);

                TextView tvCount = new TextView(this);
                tvCount.setText("🔥 " + s.currentStreak + " in a row");
                tvCount.setTextSize(13);
                tvCount.setTextColor(ContextCompat.getColor(this, R.color.status_due_text));
                row.addView(tvCount);

                layoutStreakList.addView(row);
            }
        }

        // History list
        historyAdapter.setItems(repository.getAllOccurrences());
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, PERMISSION_REQ_NOTIFICATIONS);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_toggle_theme) {
            showThemeSelectionDialog();
            return true;
        } else if (id == R.id.action_test_nag) {
            triggerTestNagDemo();
            return true;
        } else if (id == R.id.action_refresh) {
            refreshAll();
            Toast.makeText(this, "Refreshed", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showThemeSelectionDialog() {
        SharedPreferences sp = getSharedPreferences(TaskNagApp.PREF_NAME_THEME, Context.MODE_PRIVATE);
        int currentMode = sp.getInt(TaskNagApp.KEY_THEME_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

        int checkedItem = 0;
        if (currentMode == AppCompatDelegate.MODE_NIGHT_YES) checkedItem = 1;
        else if (currentMode == AppCompatDelegate.MODE_NIGHT_NO) checkedItem = 2;

        String[] themes = new String[]{"⚙️ System Default", "🌙 Dark Mode", "☀️ Light Mode"};

        new AlertDialog.Builder(this)
                .setTitle("Select Theme")
                .setSingleChoiceItems(themes, checkedItem, (dialog, which) -> {
                    int selectedMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
                    if (which == 1) selectedMode = AppCompatDelegate.MODE_NIGHT_YES;
                    else if (which == 2) selectedMode = AppCompatDelegate.MODE_NIGHT_NO;

                    sp.edit().putInt(TaskNagApp.KEY_THEME_MODE, selectedMode).apply();
                    AppCompatDelegate.setDefaultNightMode(selectedMode);
                    dialog.dismiss();
                    Toast.makeText(this, "Theme updated: " + themes[which], Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void triggerTestNagDemo() {
        long now = System.currentTimeMillis();
        String today = DateTimeUtils.getTodayDateString();

        Task testTask = new Task("⚡ Quick Nag Demo: Take Medication", "HEALTH", today, DateTimeUtils.getCurrentTimeString());
        testTask.setDescription("Persistent nagging verification test. This alert repeats every 10 seconds until completed!");
        testTask.setPriority(Priority.URGENT);
        testTask.setRecurrenceType(RecurrenceType.ONE_TIME);
        testTask.setNagIntervalMinutes(1); // 1 minute in real mode
        testTask.setColorHex("#E53935");
        testTask.setSoundType("ALARM");
        testTask.setVibrationPattern("URGENT_PULSE");

        repository.saveTask(testTask);

        // Find the occurrence created
        List<TaskOccurrence> occs = repository.getOccurrencesForDate(today);
        TaskOccurrence demoOcc = null;
        for (TaskOccurrence o : occs) {
            if (o.getTaskId().equals(testTask.getId())) {
                demoOcc = o;
                break;
            }
        }

        if (demoOcc != null) {
            // Force immediate reminder
            demoOcc.setStatus(OccurrenceStatus.DUE_NOW);
            demoOcc.incrementNagCount();
            repository.saveOccurrence(demoOcc);
            NagNotificationHelper.showNagNotification(this, demoOcc, testTask);
            NagAlarmScheduler.scheduleExactAlarm(this, demoOcc, testTask, now + 15000L); // repeats in 15 seconds!
        }

        refreshAll();

        new AlertDialog.Builder(this)
                .setTitle("⚡ Persistent Nag Alert Triggered!")
                .setMessage("A persistent reminder notification has been triggered with sound & vibration.\n\nActions:\n• Check notification drawer for 'Complete', 'Snooze', 'Skip' actions.\n• Notice: Dismissing the notification does NOT stop it; it will repeat until you press Complete!\n• In-app alert banner is now active at top of Dashboard.")
                .setPositiveButton("Awesome", null)
                .show();
    }
}
