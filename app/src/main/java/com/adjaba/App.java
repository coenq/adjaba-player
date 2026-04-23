package com.adjaba;

import android.app.Application;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.adjaba.workers.ImpressionRetryWorker;

import java.util.concurrent.TimeUnit;

public class App extends Application {

    private static final String IMPRESSION_RETRY_WORK = "impression_retry_work";

    @Override
    public void onCreate() {
        super.onCreate();
        scheduleImpressionRetryWorker();
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
}
