package com.adjaba.workers;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.adjaba.utilities.SlideshowManager;

import java.util.concurrent.TimeUnit;

/**
 * Background sync for the Cloud Slideshow feature: refreshes the linked Google Drive
 * folder's image list on a schedule. No-ops instantly when the feature is disabled
 * (see {@link SlideshowManager#sync}), so scheduling this unconditionally is safe and
 * costs nothing when the feature isn't in use.
 */
public class SlideshowSyncWorker extends Worker {
    private static final String PERIODIC_WORK_NAME = "slideshow_sync_work";
    private static final String ONE_TIME_WORK_NAME = "slideshow_sync_immediate";

    public SlideshowSyncWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        boolean success = SlideshowManager.sync(getApplicationContext());
        return success ? Result.success() : Result.retry();
    }

    /** Schedules the periodic background sync. Call once from App.onCreate. */
    public static void schedulePeriodic(Context context) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(
                SlideshowSyncWorker.class, 30, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                PERIODIC_WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request);
    }

    /** Triggers an immediate one-off sync (e.g. right after the user saves slideshow settings). */
    public static void triggerImmediateSync(Context context) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(SlideshowSyncWorker.class)
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance(context).enqueueUniqueWork(
                ONE_TIME_WORK_NAME, ExistingWorkPolicy.REPLACE, request);
    }
}
