package com.tasknag.app.util;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;

public class AudioVibrationHelper {

    public static final long[] VIBRATE_DEFAULT = {0, 350, 200, 350};
    public static final long[] VIBRATE_URGENT = {0, 500, 150, 500, 150, 500};
    public static final long[] VIBRATE_LONG = {0, 1000, 300, 1000};
    public static final long[] VIBRATE_GENTLE = {0, 150, 100, 150};

    public static long[] getVibrationPattern(String patternName) {
        if ("URGENT_PULSE".equalsIgnoreCase(patternName) || "URGENT".equalsIgnoreCase(patternName)) {
            return VIBRATE_URGENT;
        } else if ("LONG_BUZZ".equalsIgnoreCase(patternName)) {
            return VIBRATE_LONG;
        } else if ("GENTLE".equalsIgnoreCase(patternName)) {
            return VIBRATE_GENTLE;
        } else if ("OFF".equalsIgnoreCase(patternName) || "NONE".equalsIgnoreCase(patternName)) {
            return new long[]{0};
        }
        return VIBRATE_DEFAULT;
    }

    public static void vibrate(Context context, String patternName) {
        long[] pattern = getVibrationPattern(patternName);
        if (pattern.length <= 1 && pattern[0] == 0) return;

        try {
            Vibrator vibrator;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                VibratorManager vm = (VibratorManager) context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
                vibrator = vm != null ? vm.getDefaultVibrator() : null;
            } else {
                vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            }

            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1));
                } else {
                    vibrator.vibrate(pattern, -1);
                }
            }
        } catch (Exception ignored) {}
    }

    public static void playNotificationSound(Context context, String soundType) {
        if ("SILENT".equalsIgnoreCase(soundType)) return;
        try {
            Uri soundUri;
            if ("ALARM".equalsIgnoreCase(soundType) || "URGENT".equalsIgnoreCase(soundType)) {
                soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
                if (soundUri == null) {
                    soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                }
            } else {
                soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            }

            if (soundUri != null) {
                Ringtone r = RingtoneManager.getRingtone(context.getApplicationContext(), soundUri);
                if (r != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        r.setAudioAttributes(new AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                .build());
                    }
                    r.play();
                }
            }
        } catch (Exception ignored) {}
    }
}
