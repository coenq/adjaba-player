package com.adjaba.utilities;

import android.content.Context;
import android.content.SharedPreferences;

import com.adjaba.models.newmodels.TvPollAuthResponse;

/**
 * Applies a {@link TvPollAuthResponse.ScreenSetup} (screen configured remotely from a phone
 * during TV login, once the backend supports it — see that class's docs) so the TV can start
 * playing without any manual setup on the remote.
 *
 * Writes the exact same "resume_*" keys in "MyPrefs" that SelectScreens's own
 * saveResumeState()/restoreResumeState() (built for boot/power-cut auto-resume) already
 * use — so calling this and then launching SelectScreens with the "auto_play" intent extra
 * reuses that existing, already-tested path instead of duplicating it.
 */
public class RemoteScreenSetup {
    private RemoteScreenSetup() {}

    public static void apply(Context context, TvPollAuthResponse.ScreenSetup setup) {
        SharedPreferences prefs = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        prefs.edit()
                .putBoolean("resume_enabled", true)
                .putString("resume_screen_id", setup.screenId)
                .putString("resume_orient", setup.orientation)
                .putString("resume_time", setup.refreshInterval != null ? setup.refreshInterval : "0")
                .putInt("resume_display_flag", boolToFlag(setup.displayTextEnabled, false))
                .putInt("resume_target_hours_flag", boolToFlag(setup.businessRulesEnabled, false))
                .putInt("resume_weather_flag", boolToFlag(setup.weatherEnabled, true))
                .putInt("resume_news_flag", boolToFlag(setup.newsEnabled, true))
                .putBoolean("iot_enabled", setup.iotEnabled != null && setup.iotEnabled)
                .apply();

        if (setup.slideshowEnabled != null) {
            SlideshowManager.saveConfig(context, setup.slideshowEnabled,
                    setup.slideshowFolderUrl,
                    setup.slideshowIntervalSeconds != null ? setup.slideshowIntervalSeconds : 5);
            // Slideshow has no periodic background sync (manual-only, by design — see
            // SlideshowSyncWorker) and this is the only place a remotely-configured screen's
            // slideshow gets set up at all — without this call, a screen configured entirely
            // from a phone would never download a single photo, since it never goes through
            // SelectScreens's own PLAY-button/Sync-Now paths that normally trigger this.
            if (setup.slideshowEnabled) {
                com.adjaba.workers.SlideshowSyncWorker.triggerImmediateSync(context);
            }
        }
    }

    private static int boolToFlag(Boolean value, boolean defaultValue) {
        boolean resolved = value != null ? value : defaultValue;
        return resolved ? 1 : 0;
    }
}
