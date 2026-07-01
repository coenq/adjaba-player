package com.adjaba.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.adjaba.activities.SelectScreens;

/**
 * Relaunches playback after the device reboots (power outage recovery).
 *
 * Only fires when the user previously pressed PLAY (resume_enabled flag saved by
 * SelectScreens). Launches SelectScreens with auto_play=true, which restores the
 * saved configuration and starts playback — from the backend when online, or from
 * locally cached ads when offline.
 *
 * NOTE: On Android 10+ starting an activity from a receiver requires the
 * "Display over other apps" (SYSTEM_ALERT_WINDOW) permission to be granted,
 * otherwise the launch is silently ignored by the system.
 */
public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent != null ? intent.getAction() : null;
        if (!Intent.ACTION_BOOT_COMPLETED.equals(action)
                && !"android.intent.action.QUICKBOOT_POWERON".equals(action)) {
            return;
        }

        SharedPreferences prefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        if (!prefs.getBoolean("resume_enabled", false)) {
            Log.i(TAG, "Boot detected but no saved playback session — not auto-starting");
            return;
        }

        Log.i(TAG, "Boot detected — resuming playback for screen: "
                + prefs.getString("resume_screen_id", "?"));
        Intent launch = new Intent(context, SelectScreens.class);
        launch.putExtra("auto_play", true);
        launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        try {
            context.startActivity(launch);
        } catch (Exception e) {
            Log.e(TAG, "Failed to auto-start playback after boot: " + e.getMessage());
        }
    }
}
