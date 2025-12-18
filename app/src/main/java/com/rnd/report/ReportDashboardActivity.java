package com.rnd.report;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.rnd.R;
import com.rnd.room.AdDatabase;
import com.rnd.room.ReportDataBase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ReportDashboardActivity extends AppCompatActivity {
    LinearLayout container, sentimentBar;
    PieChart genderChart;
    ProgressBar progressBar;
    HorizontalBarChart sentimentChart;
    Spinner daySpinner;
    View happyView, neutralView, sadView, childView, teenView, adultView, seniorView;
    TextView tvTotalVisitors;
    long visitors, happy = 0, sad = 0, neutral = 0, male, child, teen, adult, senior;
    private BarChart barChart;
    List<String> hours = new ArrayList<>();
    private LinearLayout.LayoutParams happyParams;
    private LinearLayout.LayoutParams neutralParams;
    private LinearLayout.LayoutParams sadParams;
    List<Long> counts = new ArrayList<>();
    int day = 0, today = 0;
    long all = 0;

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
        barChart = findViewById(R.id.barChart);
        sentimentBar = findViewById(R.id.sentimentBar);
        daySpinner = findViewById(R.id.daySpinner);
        day = Integer.parseInt(new SimpleDateFormat("dd", Locale.getDefault()).format(new Date()));
        today = day;

        String[] days = {
                "day", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
                "11", "12", "13", "14", "15", "16", "17", "18", "19", "20",
                "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31"
        };
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                days
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        daySpinner.setAdapter(spinnerAdapter);
        daySpinner.setSelection(day);
        daySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                progressBar.setVisibility(View.VISIBLE);
                if (position == 0) {
                    return;
                }
                day = Integer.parseInt(spinnerAdapter.getItem(position));
                setReport(day);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    private void setReport(int h) {
        ReportDataBase adDatabase = ReportDataBase.getInstance(this);
        new

                Thread(new Runnable() {
            @Override
            public void run() {
                happy = 0;
                sad = 0;
                neutral = 0;
                visitors = 0;
                male = 0;
                child = 0;
                adult = 0;
                teen = 0;
                senior = 0;
                for (int i = 1; i < 25; i++) {
                    if (i < 12) {
                        hours.add(i + " am");
                    } else hours.add(i + " pm");
                    counts.add(Long.parseLong("0"));
                }
                for (int i = 0; i < adDatabase.reportDao().getAllReports(h).size(); i++) {
                    happy += adDatabase.reportDao().getAllReports(h).get(i).happy;
                    sad += adDatabase.reportDao().getAllReports(h).get(i).sad;
                    neutral += adDatabase.reportDao().getAllReports(h).get(i).neutral;
                    visitors += adDatabase.reportDao().getAllReports(h).get(i).female20 + adDatabase.reportDao().getAllReports(h).get(i).female32 + adDatabase.reportDao().getAllReports(h).get(i).female40 + adDatabase.reportDao().getAllReports(h).get(i).female50 + adDatabase.reportDao().getAllReports(h).get(i).female50plus + adDatabase.reportDao().getAllReports(h).get(i).male20 + adDatabase.reportDao().getAllReports(h).get(i).male32 + adDatabase.reportDao().getAllReports(h).get(i).male40 + adDatabase.reportDao().getAllReports(h).get(i).male50 + adDatabase.reportDao().getAllReports(h).get(i).male50plus;

                    male += adDatabase.reportDao().getAllReports(h).get(i).male20 + adDatabase.reportDao().getAllReports(h).get(i).male32 + adDatabase.reportDao().getAllReports(h).get(i).male40 + adDatabase.reportDao().getAllReports(h).get(i).male50 + adDatabase.reportDao().getAllReports(h).get(i).male50plus;
                    ;
                    child += adDatabase.reportDao().getAllReports(h).get(i).child;
                    adult += adDatabase.reportDao().getAllReports(h).get(i).adult;
                    teen += adDatabase.reportDao().getAllReports(h).get(i).middle;
                    senior += adDatabase.reportDao().getAllReports(h).get(i).senior;
                    all = counts.get(adDatabase.reportDao().getAllReports(h).get(i).hour) + adDatabase.reportDao().getAllReports(h).get(i).senior + adDatabase.reportDao().getAllReports(h).get(i).child + adDatabase.reportDao().getAllReports(h).get(i).adult + adDatabase.reportDao().getAllReports(h).get(i).middle;
                    counts.set(adDatabase.reportDao().getAllReports(h).get(i).hour, all);
                    all = 0;
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        long vis = (senior + adult + teen + child);
                        ReportData report = ReportUtils.generateReport(visitors, happy, sad, neutral, male, child, teen, adult, senior);


                        tvTotalVisitors.setText(vis+"");

                        setupGenderChart(report);
                        container.setOrientation(LinearLayout.VERTICAL);
                        showBarChart(hours, counts);

                        updateAgeBar(child, teen, adult, senior);
                        updateSentimentBar((int) happy, (int) neutral, (int) sad);


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
        happyParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, happy);
        neutralParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, neutral);
        sadParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, sad);

        happyView.setLayoutParams(happyParams);
        neutralView.setLayoutParams(neutralParams);
        sadView.setLayoutParams(sadParams);
    }

    @SuppressLint("SetTextI18n")
    private void updateAgeBar(long child, long teen, long adult, long senior) {
        container.removeAllViews();
        container.post(() -> {

            int fullWidth = container.getWidth();
            FrameLayout childLayout = new FrameLayout(getApplicationContext());
            childLayout.setBackgroundColor(Color.parseColor("#4CAF50"));

            FrameLayout.LayoutParams barParams =
                    new FrameLayout.LayoutParams(
                            (int) (fullWidth * ((float) child / 100)),
                            50
                    );
            barParams.setMargins(0, 0, 0, 16);

// TextView
            TextView textView = new TextView(getApplicationContext());
            textView.setText(child + "");
            textView.setTextColor(Color.WHITE);
            textView.setTextSize(10);
            FrameLayout.LayoutParams textParams =
                    new FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            Gravity.END | Gravity.CENTER_VERTICAL
                    );
            textParams.setMargins(0, 0, 8, 0);

            childLayout.addView(textView, textParams);
            container.addView(childLayout, barParams);

            // Teen
            FrameLayout teenLayout = new FrameLayout(getApplicationContext());
            teenLayout.setBackgroundColor(Color.parseColor("#2196F3"));

            FrameLayout.LayoutParams teenParams =
                    new FrameLayout.LayoutParams(
                            (int) (fullWidth * ((float) teen / 100)),
                            50
                    );
            teenParams.setMargins(0, 0, 0, 16);

// TextView
            TextView teenText = new TextView(getApplicationContext());
            teenText.setText(teen + "");
            teenText.setTextColor(Color.WHITE);
            teenText.setTextSize(10);
            FrameLayout.LayoutParams textParamsTeen =
                    new FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            Gravity.END | Gravity.CENTER_VERTICAL
                    );
            textParamsTeen.setMargins(0, 0, 8, 0);

            teenLayout.addView(teenText, textParamsTeen);
            container.addView(teenLayout, teenParams);


            // Adult
            FrameLayout adultLayout = new FrameLayout(getApplicationContext());
            adultLayout.setBackgroundColor(Color.parseColor("#FF9800"));
            FrameLayout.LayoutParams adultParams =
                    new FrameLayout.LayoutParams(
                            (int) (fullWidth * ((float) teen / 100)),
                            50
                    );
            adultParams.setMargins(0, 0, 0, 16);
            TextView adultText = new TextView(getApplicationContext());
            adultText.setText(adult + "");
            adultText.setTextColor(Color.WHITE);
            adultText.setTextSize(10);
            FrameLayout.LayoutParams textParamsAdult =
                    new FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            Gravity.END | Gravity.CENTER_VERTICAL
                    );
            textParamsAdult.setMargins(0, 0, 8, 0);

            adultLayout.addView(adultText, textParamsAdult);
            container.addView(adultLayout, adultParams);

            // Senior
            FrameLayout seniorLayout = new FrameLayout(getApplicationContext());
            seniorLayout.setBackgroundColor(Color.parseColor("#9C27B0"));
            FrameLayout.LayoutParams seniorParams =
                    new FrameLayout.LayoutParams(
                            (int) (fullWidth * ((float) senior / 100)),
                            50
                    );
            seniorParams.setMargins(0, 0, 0, 16);
            TextView seniorText = new TextView(getApplicationContext());
            seniorText.setText(senior + "");
            seniorText.setTextColor(Color.WHITE);
            seniorText.setTextSize(10);
            FrameLayout.LayoutParams textParamsSenior =
                    new FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            Gravity.END | Gravity.CENTER_VERTICAL
                    );
            textParamsSenior.setMargins(0, 0, 8, 0);

            seniorLayout.addView(seniorText, textParamsSenior);
            container.addView(seniorLayout, seniorParams);


        });
    }


    private void showBarChart(List<String> hours, List<Long> counts) {
        barChart.clear();
        barChart.invalidate();
        ArrayList<BarEntry> entries = new ArrayList<>();

        for (int i = 0; i < counts.size(); i++) {
            entries.add(new BarEntry(i, counts.get(i)));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Number of people per hour");
        dataSet.setValueTextSize(8f);

        BarData data = new BarData(dataSet);
        barChart.setData(data);

        // إعداد المحور X
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(hours));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        xAxis.setDrawGridLines(false);
        xAxis.setTextSize(10f);
        xAxis.setTextColor(Color.DKGRAY);
        xAxis.setAxisMinimum(0f); // يبدأ من صفر
        xAxis.setAvoidFirstLastClipping(true);

        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(Color.parseColor("#E0E0E0"));
        leftAxis.setTextColor(Color.DKGRAY);
        leftAxis.setTextSize(10f);
        leftAxis.setAxisMinimum(0f);

        YAxis rightAxis = barChart.getAxisRight();
        rightAxis.setEnabled(false);
        barChart.getDescription().setEnabled(false);
        barChart.animateY(1000);
        barChart.invalidate();
        counts.clear();
    }
}

