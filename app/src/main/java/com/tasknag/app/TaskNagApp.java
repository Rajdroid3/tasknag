package com.tasknag.app;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

import com.tasknag.app.data.TaskRepository;
import com.tasknag.app.reminder.NagNotificationHelper;

public class TaskNagApp extends Application {

    public static final String PREF_NAME_THEME = "tasknag_theme_prefs";
    public static final String KEY_THEME_MODE = "key_theme_mode";

    @Override
    public void onCreate() {
        super.onCreate();

        // Apply saved theme mode (Default = follow system theme)
        SharedPreferences sp = getSharedPreferences(PREF_NAME_THEME, Context.MODE_PRIVATE);
        int mode = sp.getInt(KEY_THEME_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        AppCompatDelegate.setDefaultNightMode(mode);

        // Initialize notification channels
        NagNotificationHelper.createNotificationChannels(this);

        // Warm up repository
        TaskRepository.getInstance(this);
    }
}
