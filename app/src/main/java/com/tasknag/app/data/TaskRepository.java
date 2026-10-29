package com.tasknag.app.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tasknag.app.model.CategoryDef;
import com.tasknag.app.model.CustomIntervalUnit;
import com.tasknag.app.model.OccurrenceStatus;
import com.tasknag.app.model.Priority;
import com.tasknag.app.model.ProductivitySummary;
import com.tasknag.app.model.RecurrenceType;
import com.tasknag.app.model.Task;
import com.tasknag.app.model.TaskOccurrence;
import com.tasknag.app.reminder.NagAlarmScheduler;
import com.tasknag.app.util.DateTimeUtils;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class TaskRepository {

    private static final String PREF_NAME = "tasknag_data_prefs";
    private static final String KEY_TASKS = "key_tasks_json";
    private static final String KEY_OCCURRENCES = "key_occurrences_json";
    private static final String KEY_CATEGORIES = "key_categories_json";
    private static final String KEY_INITIALIZED = "key_sample_initialized";

    private static TaskRepository instance;

    private final Context context;
    private final SharedPreferences prefs;
    private final Gson gson;

    private final List<Task> tasksCache = new ArrayList<>();
    private final List<TaskOccurrence> occurrencesCache = new ArrayList<>();
    private final List<CategoryDef> categoriesCache = new ArrayList<>();

    private final List<RepositoryChangeListener> listeners = new ArrayList<>();

    public interface RepositoryChangeListener {
        void onDataChanged();
    }

    private TaskRepository(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = this.context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
        loadAll();
    }

    public static synchronized TaskRepository getInstance(Context context) {
        if (instance == null) {
            instance = new TaskRepository(context);
        }
        return instance;
    }

    public void addListener(RepositoryChangeListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(RepositoryChangeListener listener) {
        listeners.remove(listener);
    }

    private void notifyChanged() {
        for (RepositoryChangeListener l : new ArrayList<>(listeners)) {
            try {
                l.onDataChanged();
            } catch (Exception ignored) {}
        }
    }

    private synchronized void loadAll() {
        // Load categories
        String catJson = prefs.getString(KEY_CATEGORIES, null);
        if (catJson != null) {
            Type type = new TypeToken<List<CategoryDef>>(){}.getType();
            List<CategoryDef> list = gson.fromJson(catJson, type);
            if (list != null) {
                categoriesCache.clear();
                categoriesCache.addAll(list);
            }
        }
        if (categoriesCache.isEmpty()) {
            categoriesCache.addAll(CategoryDef.getDefaultCategories());
        }

        // Load tasks
        String tasksJson = prefs.getString(KEY_TASKS, null);
        if (tasksJson != null) {
            Type type = new TypeToken<List<Task>>(){}.getType();
            List<Task> list = gson.fromJson(tasksJson, type);
            if (list != null) {
                tasksCache.clear();
                tasksCache.addAll(list);
            }
        }

        // Load occurrences
        String occJson = prefs.getString(KEY_OCCURRENCES, null);
        if (occJson != null) {
            Type type = new TypeToken<List<TaskOccurrence>>(){}.getType();
            List<TaskOccurrence> list = gson.fromJson(occJson, type);
            if (list != null) {
                occurrencesCache.clear();
                occurrencesCache.addAll(list);
            }
        }

        // If first launch, populate rich sample data
        if (!prefs.getBoolean(KEY_INITIALIZED, false)) {
            initSampleData();
            prefs.edit().putBoolean(KEY_INITIALIZED, true).apply();
        }

        refreshStatuses(System.currentTimeMillis());
    }

    private synchronized void saveTasksToDisk() {
        String json = gson.toJson(tasksCache);
        prefs.edit().putString(KEY_TASKS, json).apply();
    }

    private synchronized void saveOccurrencesToDisk() {
        String json = gson.toJson(occurrencesCache);
        prefs.edit().putString(KEY_OCCURRENCES, json).apply();
    }

    private synchronized void saveCategoriesToDisk() {
        String json = gson.toJson(categoriesCache);
        prefs.edit().putString(KEY_CATEGORIES, json).apply();
    }

    private void initSampleData() {
        String today = DateTimeUtils.getTodayDateString();

        // 1. Take Vitamin B12 at 11:00 AM (Health, Daily, nag every 15m)
        Task t1 = new Task("Take Vitamin B12", "HEALTH", today, "11:00");
        t1.setDescription("Take with a glass of water after breakfast");
        t1.setPriority(Priority.URGENT);
        t1.setRecurrenceType(RecurrenceType.DAILY);
        t1.setNagIntervalMinutes(15);
        t1.setColorHex("#E53935");
        t1.setSoundType("ALARM");
        t1.setVibrationPattern("URGENT_PULSE");

        // 2. Study pharmacology at 2:00 PM (Study, High, nag every 10m)
        Task t2 = new Task("Study pharmacology", "STUDY", today, "14:00");
        t2.setDescription("Review Chapter 4: Drug Receptors and Pharmacodynamics");
        t2.setPriority(Priority.HIGH);
        t2.setRecurrenceType(RecurrenceType.DAILY);
        t2.setNagIntervalMinutes(10);
        t2.setColorHex("#3949AB");
        t2.setNotes("Flashcards deck #3, 40 cards total");

        // 3. Drink water every 2 hours (Health, Custom Interval 2 Hours, nag every 15m)
        Task t3 = new Task("Drink water", "HEALTH", today, "09:00");
        t3.setDescription("Stay hydrated - at least 350ml");
        t3.setPriority(Priority.MEDIUM);
        t3.setRecurrenceType(RecurrenceType.CUSTOM_INTERVAL);
        t3.getRecurrenceRule().setCustomValue(2);
        t3.getRecurrenceRule().setCustomUnit(CustomIntervalUnit.HOURS);
        t3.setNagIntervalMinutes(15);
        t3.setColorHex("#0288D1");

        // 4. Exercise workout at 6:00 PM (Exercise, Mon/Wed/Fri, nag every 30m)
        Task t4 = new Task("Evening Workout", "EXERCISE", today, "18:00");
        t4.setDescription("Core workout + 30 min cardio");
        t4.setPriority(Priority.HIGH);
        t4.setRecurrenceType(RecurrenceType.WEEKLY);
        List<Integer> days = new ArrayList<>();
        days.add(Calendar.MONDAY);
        days.add(Calendar.WEDNESDAY);
        days.add(Calendar.FRIDAY);
        t4.getRecurrenceRule().setWeeklyDays(days);
        t4.setNagIntervalMinutes(30);
        t4.setColorHex("#43A047");

        // 5. Pay a bill on the 15th of every month (Household, Monthly, nag every 60m)
        Task t5 = new Task("Pay Electricity & Internet Bill", "HOUSEHOLD", today, "10:00");
        t5.setDescription("Check auto-debit confirmation and bill statement");
        t5.setPriority(Priority.URGENT);
        t5.setRecurrenceType(RecurrenceType.MONTHLY);
        t5.getRecurrenceRule().setMonthlyDay(15);
        t5.setNagIntervalMinutes(60);
        t5.setColorHex("#00897B");

        // 6. Complete project milestone before Friday (Work, High, nag every 30m)
        Task t6 = new Task("Complete Project Milestone", "WORK", today, "17:00");
        t6.setDescription("Finalize Android smart reminder module and run test suite");
        t6.setPriority(Priority.HIGH);
        t6.setRecurrenceType(RecurrenceType.ONE_TIME);
        t6.setNagIntervalMinutes(30);
        t6.setColorHex("#FB8C00");

        tasksCache.add(t1);
        tasksCache.add(t2);
        tasksCache.add(t3);
        tasksCache.add(t4);
        tasksCache.add(t5);
        tasksCache.add(t6);

        saveTasksToDisk();

        // Generate initial occurrences for next 14 days
        long now = System.currentTimeMillis();
        long windowStart = DateTimeUtils.getStartOfDayEpoch(now);
        long windowEnd = windowStart + (14L * 86400000L);

        for (Task t : tasksCache) {
            generateOccurrencesForTask(t, windowStart, windowEnd);
        }

        saveOccurrencesToDisk();
    }

    public synchronized void generateOccurrencesForTask(Task task, long windowStart, long windowEnd) {
        if (!task.isEnabled()) return;
        List<Long> times = DateTimeUtils.generateOccurrences(task, windowStart, windowEnd);

        Set<Long> existingTimes = new HashSet<>();
        for (TaskOccurrence occ : occurrencesCache) {
            if (occ.getTaskId().equals(task.getId())) {
                existingTimes.add(occ.getScheduledEpoch());
            }
        }

        for (Long t : times) {
            if (!existingTimes.contains(t)) {
                TaskOccurrence occ = new TaskOccurrence(
                        task.getId(),
                        task.getTitle(),
                        task.getCategory(),
                        task.getPriority(),
                        task.getColorHex(),
                        t
                );
                occ.setNotes(task.getNotes());
                occurrencesCache.add(occ);
            }
        }
    }

    public synchronized void refreshStatuses(long now) {
        boolean changed = false;
        for (TaskOccurrence occ : occurrencesCache) {
            if (occ.isTerminal()) continue;

            if (occ.getSnoozedUntilEpoch() != null && occ.getSnoozedUntilEpoch() > now) {
                continue;
            }

            if (now > occ.getScheduledEpoch()) {
                long overdueMinutes = (now - occ.getScheduledEpoch()) / 60000;
                if (overdueMinutes > 15) {
                    if (occ.getStatus() != OccurrenceStatus.OVERDUE) {
                        occ.setStatus(OccurrenceStatus.OVERDUE);
                        changed = true;
                    }
                } else {
                    if (occ.getStatus() != OccurrenceStatus.DUE_NOW) {
                        occ.setStatus(OccurrenceStatus.DUE_NOW);
                        changed = true;
                    }
                }
            } else if (now >= (occ.getScheduledEpoch() - 120000L)) {
                if (occ.getStatus() != OccurrenceStatus.DUE_NOW) {
                    occ.setStatus(OccurrenceStatus.DUE_NOW);
                    changed = true;
                }
            } else {
                if (occ.getStatus() != OccurrenceStatus.UPCOMING) {
                    occ.setStatus(OccurrenceStatus.UPCOMING);
                    changed = true;
                }
            }
        }
        if (changed) {
            saveOccurrencesToDisk();
        }
    }

    // Task CRUD
    public synchronized List<Task> getAllTasks() {
        return new ArrayList<>(tasksCache);
    }

    public synchronized Task getTaskById(String taskId) {
        for (Task t : tasksCache) {
            if (t.getId().equals(taskId)) return t;
        }
        return null;
    }

    public synchronized void saveTask(Task task) {
        task.setUpdatedAt(System.currentTimeMillis());
        int index = -1;
        for (int i = 0; i < tasksCache.size(); i++) {
            if (tasksCache.get(i).getId().equals(task.getId())) {
                index = i;
                break;
            }
        }
        if (index >= 0) {
            tasksCache.set(index, task);
        } else {
            tasksCache.add(task);
        }
        saveTasksToDisk();

        // Update / regenerate occurrences
        long now = System.currentTimeMillis();
        long windowStart = DateTimeUtils.getStartOfDayEpoch(now);
        long windowEnd = windowStart + (14L * 86400000L);
        generateOccurrencesForTask(task, windowStart, windowEnd);
        saveOccurrencesToDisk();

        // Schedule next alarm for this task
        scheduleNextAlarmForTask(task.getId());

        notifyChanged();
    }

    public synchronized void deleteTask(String taskId) {
        Task task = getTaskById(taskId);
        if (task != null) {
            tasksCache.remove(task);
            saveTasksToDisk();
        }

        // Remove occurrences and cancel alarms
        List<TaskOccurrence> toRemove = new ArrayList<>();
        for (TaskOccurrence occ : occurrencesCache) {
            if (occ.getTaskId().equals(taskId)) {
                NagAlarmScheduler.cancelNagAlarm(context, occ.getId());
                toRemove.add(occ);
            }
        }
        occurrencesCache.removeAll(toRemove);
        saveOccurrencesToDisk();

        notifyChanged();
    }

    public synchronized void toggleTaskEnabled(String taskId, boolean enabled) {
        Task task = getTaskById(taskId);
        if (task != null) {
            task.setEnabled(enabled);
            task.setUpdatedAt(System.currentTimeMillis());
            saveTasksToDisk();

            if (!enabled) {
                // Cancel pending alarms for non-completed occurrences
                for (TaskOccurrence occ : occurrencesCache) {
                    if (occ.getTaskId().equals(taskId) && !occ.isTerminal()) {
                        NagAlarmScheduler.cancelNagAlarm(context, occ.getId());
                    }
                }
            } else {
                long now = System.currentTimeMillis();
                long windowStart = DateTimeUtils.getStartOfDayEpoch(now);
                long windowEnd = windowStart + (14L * 86400000L);
                generateOccurrencesForTask(task, windowStart, windowEnd);
                saveOccurrencesToDisk();
                scheduleNextAlarmForTask(taskId);
            }

            notifyChanged();
        }
    }

    // Occurrence Actions
    public synchronized List<TaskOccurrence> getAllOccurrences() {
        return new ArrayList<>(occurrencesCache);
    }

    public synchronized TaskOccurrence getOccurrenceById(String occurrenceId) {
        for (TaskOccurrence occ : occurrencesCache) {
            if (occ.getId().equals(occurrenceId)) return occ;
        }
        return null;
    }

    public synchronized void saveOccurrence(TaskOccurrence occ) {
        int index = -1;
        for (int i = 0; i < occurrencesCache.size(); i++) {
            if (occurrencesCache.get(i).getId().equals(occ.getId())) {
                index = i;
                break;
            }
        }
        if (index >= 0) {
            occurrencesCache.set(index, occ);
        } else {
            occurrencesCache.add(occ);
        }
        saveOccurrencesToDisk();
        notifyChanged();
    }

    public synchronized void markOccurrenceCompleted(String occurrenceId, String notes) {
        TaskOccurrence occ = getOccurrenceById(occurrenceId);
        if (occ != null) {
            long now = System.currentTimeMillis();
            occ.setStatus(OccurrenceStatus.COMPLETED);
            occ.setCompletedEpoch(now);
            if (notes != null && !notes.trim().isEmpty()) {
                occ.setNotes(notes);
            }
            saveOccurrencesToDisk();

            // Stop all nagging alarms for this occurrence immediately
            NagAlarmScheduler.cancelNagAlarm(context, occurrenceId);
            NagAlarmScheduler.dismissNotification(context, occurrenceId);

            // Schedule the next occurrence if recurring
            scheduleNextAlarmForTask(occ.getTaskId());

            notifyChanged();
        }
    }

    public synchronized void markOccurrenceSkipped(String occurrenceId) {
        TaskOccurrence occ = getOccurrenceById(occurrenceId);
        if (occ != null) {
            long now = System.currentTimeMillis();
            occ.setStatus(OccurrenceStatus.SKIPPED);
            occ.setSkippedEpoch(now);
            saveOccurrencesToDisk();

            NagAlarmScheduler.cancelNagAlarm(context, occurrenceId);
            NagAlarmScheduler.dismissNotification(context, occurrenceId);

            scheduleNextAlarmForTask(occ.getTaskId());

            notifyChanged();
        }
    }

    public synchronized void snoozeOccurrence(String occurrenceId, int snoozeMinutes) {
        TaskOccurrence occ = getOccurrenceById(occurrenceId);
        if (occ != null) {
            long now = System.currentTimeMillis();
            long snoozeUntil = now + (snoozeMinutes * 60L * 1000L);
            occ.setSnoozedUntilEpoch(snoozeUntil);
            saveOccurrencesToDisk();

            // Reschedule nag alarm to the snooze target
            Task parentTask = getTaskById(occ.getTaskId());
            NagAlarmScheduler.scheduleExactAlarm(context, occ, parentTask, snoozeUntil);
            NagAlarmScheduler.dismissNotification(context, occurrenceId);

            notifyChanged();
        }
    }

    public synchronized void scheduleNextAlarmForTask(String taskId) {
        Task task = getTaskById(taskId);
        if (task == null || !task.isEnabled()) return;

        long now = System.currentTimeMillis();
        TaskOccurrence nextCandidate = null;

        for (TaskOccurrence occ : occurrencesCache) {
            if (occ.getTaskId().equals(taskId) && !occ.isTerminal()) {
                long targetTime = occ.getSnoozedUntilEpoch() != null ? occ.getSnoozedUntilEpoch() : occ.getScheduledEpoch();
                if (nextCandidate == null || targetTime < (nextCandidate.getSnoozedUntilEpoch() != null ? nextCandidate.getSnoozedUntilEpoch() : nextCandidate.getScheduledEpoch())) {
                    nextCandidate = occ;
                }
            }
        }

        if (nextCandidate != null) {
            long targetTime = nextCandidate.getSnoozedUntilEpoch() != null ? nextCandidate.getSnoozedUntilEpoch() : nextCandidate.getScheduledEpoch();
            if (targetTime < now) {
                // If it's already past scheduled time and not snoozed, schedule immediate nag or next nag interval
                int intervalMs = task.getNagIntervalMinutes() * 60 * 1000;
                long nextNag = nextCandidate.getLastNagEpoch() != null ? nextCandidate.getLastNagEpoch() + intervalMs : now + 5000L;
                targetTime = Math.max(now + 2000L, nextNag);
            }
            NagAlarmScheduler.scheduleExactAlarm(context, nextCandidate, task, targetTime);
        }
    }

    // Filter Queries
    public synchronized List<TaskOccurrence> getOccurrencesForDate(String dateStr) {
        refreshStatuses(System.currentTimeMillis());
        List<TaskOccurrence> result = new ArrayList<>();
        long targetStart = DateTimeUtils.parseDateTimeToEpoch(dateStr, "00:00");
        long targetEnd = DateTimeUtils.parseDateTimeToEpoch(dateStr, "23:59");

        for (TaskOccurrence occ : occurrencesCache) {
            if (occ.getScheduledEpoch() >= targetStart && occ.getScheduledEpoch() <= targetEnd) {
                result.add(occ);
            }
        }
        sortOccurrences(result);
        return result;
    }

    public synchronized List<TaskOccurrence> getDueNowOccurrences(long now) {
        refreshStatuses(now);
        List<TaskOccurrence> result = new ArrayList<>();
        for (TaskOccurrence occ : occurrencesCache) {
            if (!occ.isTerminal() && occ.getStatus() == OccurrenceStatus.DUE_NOW) {
                result.add(occ);
            }
        }
        sortOccurrences(result);
        return result;
    }

    public synchronized List<TaskOccurrence> getOverdueOccurrences(long now) {
        refreshStatuses(now);
        List<TaskOccurrence> result = new ArrayList<>();
        for (TaskOccurrence occ : occurrencesCache) {
            if (!occ.isTerminal() && occ.getStatus() == OccurrenceStatus.OVERDUE) {
                result.add(occ);
            }
        }
        sortOccurrences(result);
        return result;
    }

    public synchronized List<TaskOccurrence> getUpcomingToday(long now) {
        refreshStatuses(now);
        List<TaskOccurrence> result = new ArrayList<>();
        long endOfDay = DateTimeUtils.getEndOfDayEpoch(now);

        for (TaskOccurrence occ : occurrencesCache) {
            if (!occ.isTerminal() && occ.getStatus() == OccurrenceStatus.UPCOMING && occ.getScheduledEpoch() <= endOfDay) {
                result.add(occ);
            }
        }
        sortOccurrences(result);
        return result;
    }

    public synchronized List<TaskOccurrence> getCompletedToday(long now) {
        long startOfDay = DateTimeUtils.getStartOfDayEpoch(now);
        long endOfDay = DateTimeUtils.getEndOfDayEpoch(now);
        List<TaskOccurrence> result = new ArrayList<>();

        for (TaskOccurrence occ : occurrencesCache) {
            if (occ.getStatus() == OccurrenceStatus.COMPLETED && occ.getCompletedEpoch() != null
                    && occ.getCompletedEpoch() >= startOfDay && occ.getCompletedEpoch() <= endOfDay) {
                result.add(occ);
            }
        }
        result.sort((a, b) -> Long.compare(b.getCompletedEpoch() != null ? b.getCompletedEpoch() : 0,
                a.getCompletedEpoch() != null ? a.getCompletedEpoch() : 0));
        return result;
    }

    private void sortOccurrences(List<TaskOccurrence> list) {
        list.sort((a, b) -> {
            // Overdue and Due Now first
            int aWeight = a.getStatus() == OccurrenceStatus.OVERDUE ? 0 : (a.getStatus() == OccurrenceStatus.DUE_NOW ? 1 : 2);
            int bWeight = b.getStatus() == OccurrenceStatus.OVERDUE ? 0 : (b.getStatus() == OccurrenceStatus.DUE_NOW ? 1 : 2);
            if (aWeight != bWeight) {
                return Integer.compare(aWeight, bWeight);
            }
            // Urgent priority next
            int pComp = Integer.compare(a.getPriority().getSortOrder(), b.getPriority().getSortOrder());
            if (pComp != 0) return pComp;
            // Chronological
            return Long.compare(a.getScheduledEpoch(), b.getScheduledEpoch());
        });
    }

    // Productivity & Streak Calculation
    public synchronized ProductivitySummary calculateProductivitySummary(long now) {
        refreshStatuses(now);
        ProductivitySummary summary = new ProductivitySummary();

        int totalCompleted = 0;
        int totalScheduled = 0;
        int totalOverdue = 0;
        int totalSkipped = 0;
        int totalMissed = 0;

        long startOfToday = DateTimeUtils.getStartOfDayEpoch(now);
        long endOfToday = DateTimeUtils.getEndOfDayEpoch(now);

        int todaySched = 0;
        int todayComp = 0;
        int todayOver = 0;

        for (TaskOccurrence occ : occurrencesCache) {
            // Count total occurrences scheduled up to now or completed
            if (occ.getScheduledEpoch() <= endOfToday || occ.isTerminal()) {
                totalScheduled++;
                if (occ.getStatus() == OccurrenceStatus.COMPLETED) {
                    totalCompleted++;
                } else if (occ.getStatus() == OccurrenceStatus.SKIPPED) {
                    totalSkipped++;
                } else if (occ.getStatus() == OccurrenceStatus.MISSED) {
                    totalMissed++;
                } else if (occ.getStatus() == OccurrenceStatus.OVERDUE) {
                    totalOverdue++;
                }
            }

            // Today's metrics
            if (occ.getScheduledEpoch() >= startOfToday && occ.getScheduledEpoch() <= endOfToday) {
                todaySched++;
                if (occ.getStatus() == OccurrenceStatus.COMPLETED) {
                    todayComp++;
                } else if (occ.getStatus() == OccurrenceStatus.OVERDUE) {
                    todayOver++;
                }
            }
        }

        summary.setTotalCompletedAllTime(totalCompleted);
        summary.setTotalScheduledPastAndPresent(totalScheduled);
        summary.setTotalOverdueCurrently(totalOverdue);
        summary.setTotalSkipped(totalSkipped);
        summary.setTotalMissed(totalMissed);

        summary.setTodayScheduled(todaySched);
        summary.setTodayCompleted(todayComp);
        summary.setTodayOverdue(todayOver);
        summary.setTodayRemaining(Math.max(0, todaySched - todayComp));

        // 7-day metric breakdown
        List<ProductivitySummary.DailyMetric> last7 = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(now);
        cal.add(Calendar.DAY_OF_YEAR, -6);

        SimpleDateFormat dayNameFmt = new SimpleDateFormat("EEE d", Locale.getDefault());
        for (int i = 0; i < 7; i++) {
            long dayStart = DateTimeUtils.getStartOfDayEpoch(cal.getTimeInMillis());
            long dayEnd = DateTimeUtils.getEndOfDayEpoch(cal.getTimeInMillis());
            String dateLabel = dayNameFmt.format(cal.getTime());
            String fullDate = DateTimeUtils.formatEpochToDate(dayStart);

            int dComp = 0;
            int dSched = 0;
            for (TaskOccurrence occ : occurrencesCache) {
                if (occ.getScheduledEpoch() >= dayStart && occ.getScheduledEpoch() <= dayEnd) {
                    dSched++;
                    if (occ.getStatus() == OccurrenceStatus.COMPLETED) {
                        dComp++;
                    }
                }
            }
            last7.add(new ProductivitySummary.DailyMetric(dateLabel, fullDate, dComp, dSched));
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }
        summary.setLast7Days(last7);

        // Streaks for recurring tasks
        List<ProductivitySummary.StreakItem> streakList = new ArrayList<>();
        for (Task t : tasksCache) {
            if (t.isRecurring()) {
                int streak = calculateTaskStreak(t.getId());
                streakList.add(new ProductivitySummary.StreakItem(t.getId(), t.getTitle(), t.getCategory(), streak, streak));
            }
        }
        summary.setStreaks(streakList);

        return summary;
    }

    private int calculateTaskStreak(String taskId) {
        List<TaskOccurrence> taskOccs = new ArrayList<>();
        for (TaskOccurrence occ : occurrencesCache) {
            if (occ.getTaskId().equals(taskId)) {
                taskOccs.add(occ);
            }
        }
        // sort descending
        taskOccs.sort((a, b) -> Long.compare(b.getScheduledEpoch(), a.getScheduledEpoch()));

        int streak = 0;
        long now = System.currentTimeMillis();
        for (TaskOccurrence occ : taskOccs) {
            if (occ.getScheduledEpoch() > now && occ.getStatus() == OccurrenceStatus.UPCOMING) {
                continue; // future occurrence doesn't break streak
            }
            if (occ.getStatus() == OccurrenceStatus.COMPLETED) {
                streak++;
            } else if (occ.getStatus() == OccurrenceStatus.SKIPPED || occ.getStatus() == OccurrenceStatus.MISSED || occ.getStatus() == OccurrenceStatus.OVERDUE) {
                break;
            }
        }
        return streak;
    }

    // Categories
    public synchronized List<CategoryDef> getCategories() {
        return new ArrayList<>(categoriesCache);
    }

    public synchronized void addCustomCategory(String name, String colorHex, String iconName) {
        String id = "CUSTOM_" + System.currentTimeMillis();
        CategoryDef cat = new CategoryDef(id, name, colorHex, iconName, false);
        categoriesCache.add(cat);
        saveCategoriesToDisk();
        notifyChanged();
    }
}
