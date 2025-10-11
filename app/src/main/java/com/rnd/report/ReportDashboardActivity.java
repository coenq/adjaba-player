package com.rnd.report;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.rnd.R;
import com.rnd.room.AdDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ReportDashboardActivity extends AppCompatActivity {
    LinearLayout container;
    PieChart genderChart;
    ProgressBar progressBar;
    HorizontalBarChart sentimentChart;
    LinearLayout sentimentBar;
    View happyView, neutralView, sadView, childView, teenView, adultView, seniorView;
    TextView tvTotalVisitors;
    long visitors, happy = 0, sad = 0, neutral = 0, male, child, teen, adult, senior;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_dashboard);
        tvTotalVisitors = findViewById(R.id.tvTotalVisitors);
        genderChart = findViewById(R.id.genderChart);
        happyView = findViewById(R.id.happyView);
        neutralView = findViewById(R.id.neutralView);
        sadView = findViewById(R.id.sadView);
        progressBar = findViewById(R.id.loadReport_bar);
        container = findViewById(R.id.ageContainer);

        AdDatabase adDatabase = AdDatabase.getInstance(this);
        new Thread(new Runnable() {
            @Override
            public void run() {

                for (int i = 0; i < adDatabase.impDao().getAllImpressions().size(); i++) {
                    happy += adDatabase.impDao().getAllImpressions().get(i).happy;
                    sad += adDatabase.impDao().getAllImpressions().get(i).sad;
                    neutral += adDatabase.impDao().getAllImpressions().get(i).neutral;
                    visitors += adDatabase.impDao().getAllImpressions().get(i).viewCount;
                    male += adDatabase.impDao().getAllImpressions().get(i).male20 +
                            adDatabase.impDao().getAllImpressions().get(i).male32 +
                            adDatabase.impDao().getAllImpressions().get(i).male40 +
                            adDatabase.impDao().getAllImpressions().get(i).male50 +
                            adDatabase.impDao().getAllImpressions().get(i).male50plus;
                    child += adDatabase.impDao().getAllImpressions().get(i).male20 + adDatabase.impDao().getAllImpressions().get(i).female20;
                    adult += adDatabase.impDao().getAllImpressions().get(i).male32 + adDatabase.impDao().getAllImpressions().get(i).female32;
                    teen += adDatabase.impDao().getAllImpressions().get(i).male40 + adDatabase.impDao().getAllImpressions().get(i).female40;
                    senior += adDatabase.impDao().getAllImpressions().get(i).male50 + adDatabase.impDao().getAllImpressions().get(i).male50plus +
                            adDatabase.impDao().getAllImpressions().get(i).female50 + adDatabase.impDao().getAllImpressions().get(i).female50plus;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ReportData report = ReportUtils.generateReport(visitors, happy, sad, neutral, male, child, teen, adult, senior);


                        tvTotalVisitors.setText(String.valueOf(report.totalVisitors));

                        setupGenderChart(report);
                        updateSentimentBar((int) happy, (int) neutral, (int) sad);
                        container.setOrientation(LinearLayout.VERTICAL);

                        /*float child = 5f;
                        float teen = 30f;
                        float adult = 50f;
                        float senior = 15f;*/

                        container.post(() -> {
                            int fullWidth = container.getWidth();

                            // Child
                            View childView = new View(getApplicationContext());
                            childView.setBackgroundColor(Color.parseColor("#4CAF50"));
                            LinearLayout.LayoutParams childParams = new LinearLayout.LayoutParams((int) (fullWidth * ((float) child / 100)), 30);
                            childParams.setMargins(0, 0, 0, 16);
                            container.addView(childView, childParams);

                            // Teen
                            View teenView = new View(getApplicationContext());
                            teenView.setBackgroundColor(Color.parseColor("#2196F3"));
                            LinearLayout.LayoutParams teenParams = new LinearLayout.LayoutParams((int) (fullWidth * ((float) teen / 100)), 30);
                            teenParams.setMargins(0, 0, 0, 16);
                            container.addView(teenView, teenParams);

                            // Adult
                            View adultView = new View(getApplicationContext());
                            adultView.setBackgroundColor(Color.parseColor("#FF9800"));
                            LinearLayout.LayoutParams adultParams = new LinearLayout.LayoutParams((int) (fullWidth * ((float) adult / 100)), 30);
                            adultParams.setMargins(0, 0, 0, 16);
                            container.addView(adultView, adultParams);

                            // Senior
                            View seniorView = new View(getApplicationContext());
                            seniorView.setBackgroundColor(Color.parseColor("#9C27B0"));
                            LinearLayout.LayoutParams seniorParams = new LinearLayout.LayoutParams((int) (fullWidth * ((float) senior / 100)), 30);
                            container.addView(seniorView, seniorParams);
                        });
                        progressBar.setVisibility(View.GONE);
                    }
                });

            }
        }).start();

    }

    private void setupGenderChart(ReportData report) {
        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(report.malePercent, "Male"));
        entries.add(new PieEntry(report.femalePercent, "Female"));

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(new int[]{Color.parseColor("#4285F4"), Color.parseColor("#00C49F")});
        dataSet.setValueTextSize(14f);
        dataSet.setSliceSpace(3f);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter());

        genderChart.setData(data);
        genderChart.setDrawHoleEnabled(true);
        genderChart.setHoleRadius(40f);
        genderChart.setTransparentCircleRadius(45f);
        genderChart.setUsePercentValues(true);
        genderChart.getDescription().setEnabled(false);
        genderChart.getLegend().setEnabled(false);
        genderChart.invalidate(); // Refresh
    }

    private void updateSentimentBar(int happy, int neutral, int sad) {
        LinearLayout.LayoutParams happyParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, happy);
        LinearLayout.LayoutParams neutralParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, neutral);
        LinearLayout.LayoutParams sadParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, sad);

        happyView.setLayoutParams(happyParams);
        neutralView.setLayoutParams(neutralParams);
        sadView.setLayoutParams(sadParams);
    }

    private void updateAgeBar(int child, int teen, int adult, int senior) {
        LinearLayout.LayoutParams childParams = new LinearLayout.LayoutParams(child / 100, LinearLayout.LayoutParams.MATCH_PARENT, child);
        LinearLayout.LayoutParams teenParams = new LinearLayout.LayoutParams(teen / 100, LinearLayout.LayoutParams.MATCH_PARENT, teen);
        LinearLayout.LayoutParams adultParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, adult);
        LinearLayout.LayoutParams seniorParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, senior);

        childView.setLayoutParams(childParams);
        teenView.setLayoutParams(teenParams);
        adultView.setLayoutParams(adultParams);
        seniorView.setLayoutParams(seniorParams);

    }

    private void setupSentimentChart(ReportData report) {
        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0, report.happyPercent));
        entries.add(new BarEntry(1, report.neutralPercent));
        entries.add(new BarEntry(2, report.sadPercent));

        BarDataSet dataSet = new BarDataSet(entries, "Sentiment");
        dataSet.setColors(new int[]{
                Color.parseColor("#00C49F"), // Happy
                Color.parseColor("#FFBB28"), // Neutral
                Color.parseColor("#FF8042")  // Sad
        });
        dataSet.setValueTextSize(14f);

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.6f);

        sentimentChart.setData(data);
        sentimentChart.setFitBars(true);
        sentimentChart.getDescription().setEnabled(false);
        sentimentChart.getLegend().setEnabled(false);
        sentimentChart.getAxisLeft().setAxisMinimum(0);
        sentimentChart.getAxisRight().setEnabled(false);
        sentimentChart.getXAxis().setEnabled(false);

        sentimentChart.invalidate(); // Refresh
    }


}