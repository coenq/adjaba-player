package com.adjaba;

import android.app.Application;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.adjaba.workers.ImpressionRetryWorker;
import com.adjaba.workers.AdSyncWorker;
import com.adjaba.workers.SlideshowSyncWorker;
import com.adjaba.utilities.AuthManager;
import com.adjaba.utilities.SlideshowManager;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class App extends Application {

    private static final String IMPRESSION_RETRY_WORK = "impression_retry_work";
    private static final String AD_SYNC_WORK = "ad_sync_work";

    @Override
    public void onCreate() {
        super.onCreate();
        // Proactively refresh token in background if it is near expiry (> 6 days old)
        Executors.newSingleThreadExecutor().execute(() -> {
            if (AuthManager.isTokenNearExpiry(this)) {
                android.util.Log.i("App", "🔄 Token near expiry — proactive re-authentication...");
                AuthManager.reAuthenticateSync(this);
            }
        });
        scheduleImpressionRetryWorker();
        scheduleAdSyncWorker();
        SlideshowSyncWorker.schedulePeriodic(this);
        // Warm the in-memory slideshow cache off the main thread so it's ready before the
        // first rotation is built (no-ops instantly if the feature is disabled/unconfigured).
        Executors.newSingleThreadExecutor().execute(() -> SlideshowManager.warmFromDatabase(this));
    }

    private void scheduleImpressionRetryWorker() {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest retryRequest = new PeriodicWorkRequest.Builder(
                ImpressionRetryWorker.class,
                15, TimeUnit.MINUTES
        )
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                IMPRESSION_RETRY_WORK,
                ExistingPeriodicWorkPolicy.KEEP,
                retryRequest
        );
    }

    private void scheduleAdSyncWorker() {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest syncRequest = new PeriodicWorkRequest.Builder(
                AdSyncWorker.class,
                15, TimeUnit.MINUTES  // 15-minute minimum interval for WorkManager
        )
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                AD_SYNC_WORK,
                ExistingPeriodicWorkPolicy.REPLACE,  // REPLACE to allow interval updates
                syncRequest
        );
    }
}
