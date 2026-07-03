package com.adjaba.workers;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.adjaba.utilities.SlideshowManager;

/**
 * Sync for the Cloud Slideshow feature: refreshes the linked Google Drive folder's image
 * list. No periodic schedule — deliberately manual-only, triggered by the "Sync Now" button
 * and by pressing Play, so slideshow syncing never competes with ad syncing for bandwidth on
 * its own timer. No-ops instantly when the feature is disabled (see
 * {@link SlideshowManager#sync}).
 */
public class SlideshowSyncWorker extends Worker {
    /** Formerly used for a periodic schedule (removed) — kept only so App.onCreate can cancel
     *  any stale periodic work left over from an older app version already installed on a device. */
    public static final String LEGACY_PERIODIC_WORK_NAME = "slideshow_sync_work";
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

    /** Triggers an immediate one-off sync — from the "Sync Now" button, or right before Play. */
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
