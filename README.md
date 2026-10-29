# TaskNag — Smart Task Reminder & Persistent Completion App

[![Release](https://img.shields.io/github/v/release/Rajdroid3/tasknag?color=blue&label=Latest%20APK)](https://github.com/Rajdroid3/tasknag/releases/latest)
[![Platform](https://img.shields.io/badge/Platform-Android%208.0%2B%20%28API%2026--34%29-green)](https://developer.android.com)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

> **“Don't just remind me. Keep reminding me until I finish the task.”**

TaskNag is a modern, reliable Android application engineered specifically around task execution and relentless follow-through. Unlike passive to-do apps or basic calendar alerts that fire once and disappear, TaskNag continuously nags and re-alerts the user at custom repeat intervals until the task occurrence is marked completed, snoozed, or explicitly skipped.

---

## 📱 Download & Install

Download the latest pre-compiled APK directly from GitHub Releases:

👉 **[Download TaskNag-v1.0.0.apk](https://github.com/Rajdroid3/tasknag/releases/latest/download/TaskNag-v1.0.0.apk)**

Or install via ADB to a connected device:
```powershell
adb install -r "TaskNag.apk"
```

---

## 🌟 Key Features & Implementation Details

### 1. Core Task Management
- **Task Metadata**: Title, optional description, detailed notes and checklist instructions.
- **Rich Categories**: Pre-configured categories (Health, Study, Work, Personal, Exercise, Household) with custom hex color accents and icon badges, plus the ability to create unlimited **Custom Categories**.
- **Priority Matrix**: Low, Medium, High, and Urgent (visualized through color badges and sorting weights).
- **Scheduling**: Specific date and time, with optional **Active Start Date** and **End Date** windows.
- **Custom Sound & Vibration**: Configure sound (Default ringtone, Urgent alarm siren, Gentle chime, Silent) and vibration patterns (Urgent pulse, Long buzz, Gentle, Off).
- **Pause/Resume Without Deletion**: Toggle tasks active or paused without erasing past occurrence history.

### 2. Persistent Reminder System ("Nag Engine")
- **Independent Nagging Interval**: Keeps alerting the user at custom intervals (5 min, 10 min, 15 min, 30 min, 60 min, or custom minutes).
- **Separation of Recurrence vs Nagging**: Recurrence (e.g., daily at 11:00 AM) is kept completely distinct from overdue nagging (e.g., repeat alert every 15 minutes until finished).
- **Heads-Up & High-Priority Alerts**: Utilizes Android `AlarmManager.setExactAndAllowWhileIdle()` and high-importance notification channels.
- **Notification Actions**:
  - **Complete**: Immediately records completion timestamp, terminates nagging alarms for this occurrence, and updates the dashboard.
  - **Snooze**: Postpones next reminder by selected duration (5m, 10m, 15m, 30m, 1h).
  - **Skip**: Marks occurrence as skipped and terminates reminders for this occurrence.
- **Dismiss Protection**: Dismissing/swiping away a notification does **not** cancel the task. The next nagging alarm will still trigger at the scheduled interval!
- **Active In-App Nag Banner**: When inside the app, overdue or due tasks are surfaced in a prominent top alert card with real-time countdown, reminder counter ("Nag #2"), and 1-tap Complete/Snooze/Skip buttons.
- **Device Reboot Resilience**: `BootReceiver` restores all alarms across device reboots and package updates.

### 3. Flexible Scheduling & Recurrence
- **One-time**: Scheduled for a specific date and time.
- **Daily**: Repeat every day at specified time(s).
- **Weekly**: Select specific weekdays (e.g., Mon, Wed, Fri) with assigned time.
- **Monthly**: Repeat on a specific day of the month (e.g., 15th), with smart clamp fallback for months that do not have that day (e.g., Feb 28/29 for 31st).
- **Custom Recurrence**: Every X minutes, hours, days, or weeks (e.g., "Drink water every 2 hours").

### 4. Task Duration and Active Date Range
- Configurable **Start Date** and **End Date**.
- Tasks run indefinitely if no end date is set.
- Automatically stops generating occurrences past the end date while preserving all past completion history.

### 5. Task Completion Workflow
- **Occurrence Model**: Independent completion record for each occurrence. Completing today's task does not mark tomorrow's as completed.
- **States**: `UPCOMING`, `DUE_NOW`, `OVERDUE`, `COMPLETED`, `SKIPPED`, `MISSED`.
- **Late Completion**: Overdue tasks can be completed late (with exact completion timestamps retained).

### 6. Home Dashboard
- **Active Nag Banner**: Displays overdue and due tasks with high visibility.
- **Today's Progress Card**: Completion progress bar, percentage, and fractional count (e.g. "3 / 5 Done").
- **Quick Add**: Speed-dial bar with presets ("Take Medicine", "Drink Water +2h", "Workout", "Reminder in 15m").
- **Filter Tabs**: All Tasks, Today, Due & Overdue, Upcoming, Completed.
- **Horizontal 15-Day Date Strip**: Browse tasks across days with status dots.

### 7. Dedicated Recurring Tasks View
- Dedicated screen to manage recurring schedules.
- Card view showing schedule rule, nag rule, date range, and current streak.
- One-tap switch to pause or resume without deleting history.

### 8. Productivity & Progress Tracking
- **Today's Completion %**: Dynamic percentage ring with motivational coaching.
- **Weekly 7-Day Performance Breakdown**: Vertical progress bar columns comparing completed vs scheduled tasks across each day.
- **Accurate Metric Calculations**: Future occurrences do **not** falsely skew completion or missed statistics.
- **Habit Streaks Leaderboard**: Consecutive completion streaks for recurring habits.
- **History Timeline Log**: Expandable audit log with exact timestamps of completions and skips.

---

## ⚡ Interactive Test Mode
TaskNag includes an instant demo feature:
1. Tap the **⚡ Test Nag Alert** button in the top toolbar.
2. An instant demo task ("Take Medication") will be created and triggered immediately.
3. Observe the notification drawer with action buttons (`Complete`, `Snooze`, `Skip`) and notice that swiping away the notification does not stop it from nagging again in 15 seconds!

---

## 🧪 Verified Automated Test Suite

Run unit tests via Gradle:
```powershell
cd C:\Users\Kavinraj\.gemini\antigravity\scratch\tasknag
.\gradlew.bat test
```
The test suite validates:
- `DateTimeUtilsTest`: Date parsing/formatting, start/end of day epochs, relative overdue formatting, daily, weekly, monthly clamp, and custom interval generation.
- `NagEngineLogicTest`: Separation of task recurrence and nagging interval, occurrence status transitions, snooze calculations, occurrence independence, and progress calculation accuracy.

---

## 📂 Project Architecture

```
tasknag/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── AndroidManifest.xml
│   │   │   ├── java/com/tasknag/app/
│   │   │   │   ├── TaskNagApp.java
│   │   │   │   ├── data/
│   │   │   │   │   └── TaskRepository.java
│   │   │   │   ├── model/
│   │   │   │   │   ├── CategoryDef.java
│   │   │   │   │   ├── CustomIntervalUnit.java
│   │   │   │   │   ├── OccurrenceStatus.java
│   │   │   │   │   ├── Priority.java
│   │   │   │   │   ├── ProductivitySummary.java
│   │   │   │   │   ├── RecurrenceRule.java
│   │   │   │   │   ├── RecurrenceType.java
│   │   │   │   │   ├── Task.java
│   │   │   │   │   └── TaskOccurrence.java
│   │   │   │   ├── reminder/
│   │   │   │   │   ├── BootReceiver.java
│   │   │   │   │   ├── NagActionReceiver.java
│   │   │   │   │   ├── NagAlarmReceiver.java
│   │   │   │   │   ├── NagAlarmScheduler.java
│   │   │   │   │   └── NagNotificationHelper.java
│   │   │   │   ├── ui/
│   │   │   │   │   ├── MainActivity.java
│   │   │   │   │   ├── QuickAddBottomSheet.java
│   │   │   │   │   ├── TaskDetailBottomSheet.java
│   │   │   │   │   ├── TaskEditorActivity.java
│   │   │   │   │   └── adapters/
│   │   │   │   │       ├── DateStripAdapter.java
│   │   │   │   │       ├── HistoryAdapter.java
│   │   │   │   │       ├── RecurringTaskAdapter.java
│   │   │   │   │       └── TaskOccurrenceAdapter.java
│   │   │   │   └── util/
│   │   │   │       ├── AudioVibrationHelper.java
│   │   │   │       ├── ColorUtils.java
│   │   │   │       └── DateTimeUtils.java
│   │   │   └── res/
│   │   │       ├── drawable/
│   │   │       ├── layout/
│   │   │       ├── menu/
│   │   │       └── values/
│   │   └── test/java/com/tasknag/app/
│   │       ├── DateTimeUtilsTest.java
│   │       └── NagEngineLogicTest.java
│   └── build.gradle
├── build.gradle
├── settings.gradle
├── gradle.properties
└── local.properties
```
