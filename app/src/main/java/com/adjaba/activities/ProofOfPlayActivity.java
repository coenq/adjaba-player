package com.adjaba.activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.adjaba.R;
import com.adjaba.activities.adapters.ProofOfPlayAdapter;
import com.adjaba.activities.viewmodel.DataHolder;
import com.adjaba.room.AdDatabase;
import com.adjaba.room.ImpressionDao;

import java.util.List;
import java.util.concurrent.Executors;

/**
 * On-device proof-of-play report, built from the same `impressions` table the single-/multi-zone
 * playback activities already log to (see ImpressionEntity). Reachable from SelectScreens — lets
 * an installer or reseller verify a screen is actually playing without needing the CMS/backend,
 * which is useful on its own and necessary while the backend side of this story doesn't exist yet.
 *
 * Scope for this first increment: on-screen only. Exporting this as a CSV to hand an advertiser
 * directly (the actual "proof" hand-off) is the natural next step, needs a FileProvider + share
 * intent, and is intentionally left for a follow-up rather than built half-done here.
 */
public class ProofOfPlayActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_proof_of_play);

        TextView screenIdLabel = findViewById(R.id.screenIdLabel);
        TextView statTotalPlays = findViewById(R.id.statTotalPlays);
        TextView statPendingUploads = findViewById(R.id.statPendingUploads);
        RecyclerView recyclerView = findViewById(R.id.summaryRecyclerView);
        View emptyState = findViewById(R.id.emptyState);
        findViewById(R.id.backButton).setOnClickListener(v -> finish());

        String screenId = DataHolder.getInstance().screenID;
        screenIdLabel.setText(screenId != null ? "SCREEN " + screenId : "NO SCREEN CONFIGURED");

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        Executors.newSingleThreadExecutor().execute(() -> {
            ImpressionDao dao = AdDatabase.getInstance(this).impDao();
            int total = dao.getTotalCount();
            int pending = dao.getPendingCount();
            List<ImpressionDao.AdPlaySummary> summary = dao.getSummaryByAd();

            new Handler(Looper.getMainLooper()).post(() -> {
                statTotalPlays.setText(String.valueOf(total));
                statPendingUploads.setText(String.valueOf(pending));
                if (summary.isEmpty()) {
                    recyclerView.setVisibility(View.GONE);
                    emptyState.setVisibility(View.VISIBLE);
                } else {
                    recyclerView.setAdapter(new ProofOfPlayAdapter(summary));
                }
            });
        });
    }
}
