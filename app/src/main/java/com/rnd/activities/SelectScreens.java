package com.rnd.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;

import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;


import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.rnd.R;
import com.rnd.newmodels.MediaModel;
import com.rnd.newmodels.Root;
import com.rnd.newmodels.WatchingModel;
import com.rnd.others.APIImpression;
import com.rnd.others.DataHolder;
import com.rnd.report.ReportDashboardActivity;
import com.rnd.room.AdDatabase;
import com.rnd.room.ImpressionEntity;
import com.rnd.utilities.AuthManager;
import com.rnd.utilities.RetrofitBuilder;
import com.rnd.utilities.TinyDB;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.CompletableObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SelectScreens extends AppCompatActivity {
    List<WatchingModel> adList;
    RetrofitBuilder retrofitBuilder;
    Toolbar topAppBar;
    List<MediaModel> mediaModels = new ArrayList<>();
    Context context;
    Activity ac;
    TinyDB tinyDb;
    ImageView waitingLogo;
    Map<String, String> screenPlayerMap, screenLocationMap, screenDeviceMap, screenLocation;
    Map<String, List<String>> screenTags;
    RelativeLayout loginrootlayout;
    Spinner spinner1, spinner2, spinnerID;
    ProgressBar loadingBar;
    CheckBox rememberMe, displayText, businessRules;
    List<String> screenOptions1;
    SharedPreferences prefs;
    SharedPreferences.Editor editor;

    List<String> screenOptions = new ArrayList<>();
    List<String> screenCurrency = new ArrayList<>();

    ArrayAdapter<String> spinnerAdapter;
    Button play, logOut, syncData, reportBt;
    String orient, screen_id, currency = "";
    String screenDirection = "0";


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_screen);
        syncData = findViewById(R.id.syncData);
        context = this;
        ac = this;
        screenOptions = new ArrayList<>();
        findViews();
        screenOptions.add("Select Screen"); // العنصر الأول الثابت
        //screenOptions1.add("Select Screen");
        spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, screenOptions);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerID.setAdapter(spinnerAdapter);
        tinyDb = new TinyDB(ac);
        reportBt = findViewById(R.id.report_bt);
        prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        editor = prefs.edit();
        context = this;
        ac = this;
        waitingLogo = findViewById(R.id.waitingLogo);
        loadingBar = findViewById(R.id.loadingBar);
        logOut = findViewById(R.id.logOut);
        screenOptions1 = new ArrayList<>();
        retrofitBuilder = new RetrofitBuilder();
        adList = new ArrayList<>();
        screenPlayerMap = new HashMap<>();
        mediaModels = new ArrayList<>();
        screenDeviceMap = new HashMap<>();
        screenLocation = new HashMap<>();
        screenLocationMap = new HashMap<>();
        screenTags = new HashMap<>();
        String[] directionOptions = {"Direction", "Front", "Back"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                directionOptions
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner2.setAdapter(spinnerAdapter);
        String[] orientationOptions = {"Orientation", "Landscape", "Portrait"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                orientationOptions
        );
        AdDatabase adDatabase = AdDatabase.getInstance(context);

        new Thread(() -> {
            int impressionsCount = adDatabase.impDao().getAllImpressions().size();
            runOnUiThread(() -> {
                if (impressionsCount > 0) {
                    syncData.setVisibility(View.VISIBLE);
                } else {
                    syncData.setVisibility(View.GONE);
                }
            });
        }).start();


        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        int spinner1Pos = prefs.getInt("spinner1_position", 0);
        int spinner2Pos = prefs.getInt("spinner2_position", 0);

        rememberMe.setChecked(true);
        SharedPreferences prefsw = getSharedPreferences("SpinnerPrefs", MODE_PRIVATE);
        int savedPositionw = prefsw.getInt("spinner2_position", 0);


        spinner1.setAdapter(adapter);
        getIDs(context, () -> {

            if (rememberMe.isChecked()) {
                spinner1.setSelection(spinner1Pos);
                if (savedPositionw < screenOptions.size()) {
                    spinnerID.setSelection(savedPositionw);
                }
                spinner2.setSelection(spinner2Pos);
            }
        });
        logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
// Spinner 1
        spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (rememberMe.isChecked()) {
                    editor.putInt("spinner1_position", position);
                    editor.apply();
                }
                orient = orientationOptions[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        spinnerID.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (rememberMe.isChecked()) {
                    getSharedPreferences("SpinnerPrefs", MODE_PRIVATE)
                            .edit()
                            .putInt("spinner2_position", position)
                            .apply();
                }
                screen_id = screenOptions.get(position);
                //currency = screenCurrency.get(position);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
// Spinner 2
        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (rememberMe.isChecked()) {
                    editor.putInt("spinner2_position", position);
                    editor.apply();
                }
                screenDirection = directionOptions[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        reportBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(context, ReportDashboardActivity.class));
            }
        });
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!screenDirection.equals("Direction") && !orient.equals("Orientation") && !screen_id.equals("Select Screen")) {
                    findViewById(R.id.selectScreen).setPadding(dpToPx(0), dpToPx(0), dpToPx(0), dpToPx(0));
                    findViewById(R.id.nestedScrollView).setVisibility(View.GONE);
                    findViewById(R.id.logo).setVisibility(View.GONE);
                    reportBt.setVisibility(View.GONE);
                    syncData.setVisibility(View.GONE);
                    waitingLogo.setVisibility(View.VISIBLE);
                    if (screenDirection.equals("Back")) {
                        tinyDb.putBoolean("BackCamera", true);
                    } else {
                        tinyDb.putBoolean("BackCamera", false);
                    }
                    if (orient.equals("Portrait")) {
                        tinyDb.putInt("Orientation", 0);
                    } else {
                        tinyDb.putInt("Orientation", 1);
                    }
                    DataHolder.getInstance().screenID = screen_id;
                    DataHolder.getInstance().currency = currency;

                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            setPermissions(Manifest.permission.READ_MEDIA_IMAGES);
                        } else {
                            setPermissions(Manifest.permission.READ_EXTERNAL_STORAGE);
                        }
                    } else {
                        waitingLogo.setVisibility(View.VISIBLE);
                        waitingLogo.animate()
                                .scaleX(2.2f)
                                .scaleY(2.2f)
                                .alpha(0f)
                                .setDuration(2000)
                                .setInterpolator(new DecelerateInterpolator())
                                .withEndAction(() -> {
                                    Intent intent = new Intent(getApplicationContext(), TestCamera.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);
                                })
                                .start();

                    }
                } else {
                    Toast.makeText(context, "Please select Orientation and Screen ,Direction", Toast.LENGTH_LONG).show();
                }

            }
        });
        rememberMe.setOnCheckedChangeListener((buttonView, isChecked) ->

        {

            if (isChecked) {
                if (savedPositionw < directionOptions.length) {
                    spinnerID.setSelection(savedPositionw);
                }
                spinner1.setSelection(spinner1Pos);
                spinner2.setSelection(spinner2Pos);

            } else {
                spinner1.setSelection(0);
                spinner2.setSelection(0);
                spinnerID.setSelection(0);

            }
        });

        syncData.setOnClickListener(v -> {
            if (isInternetAvailable(context)) {
                Toast.makeText(context, "Uploading...", Toast.LENGTH_LONG).show();
                loadingBar.setVisibility(View.VISIBLE);
                new Thread(() -> {
                    try {
                        List<ImpressionEntity> impressions = adDatabase.impDao().getAllImpressions();
                        for (ImpressionEntity impression1 : impressions) {
                            APIImpression.sendImpression(context, impression1);
                            adDatabase.impDao().deleteAdById(impression1.screenViewId);
                        }

                        runOnUiThread(() -> {
                                    Toast.makeText(context, "Successfully Uploaded", Toast.LENGTH_LONG).show();
                                    syncData.setVisibility(View.GONE);
                                    loadingBar.setVisibility(View.GONE);

                                }
                        );
                    } catch (Exception e) {
                    }

                }).start();
            } else {
                runOnUiThread(() -> {
                            Toast.makeText(context, "Check Internet !", Toast.LENGTH_LONG).show();
                        }
                );
            }
        });
    }


    private boolean isInternetAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    List<String> getIDs(Context context, Runnable onFinish) {
        retrofitBuilder.apiCalls().getScreenResponse("Bearer " + AuthManager.getToken(this)).enqueue(new Callback<List<Root>>() {
            @Override
            public void onResponse(Call<List<Root>> call, Response<List<Root>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (int screen = 0; screen < response.body().size(); screen++) {
                        screenCurrency.add(response.body().get(screen).currency);
                        screenOptions.add(response.body().get(screen).getScreenId());
                    }
                    spinnerAdapter.notifyDataSetChanged();

                    // تحميل القيمة المحفوظة لو موجودة
                    SharedPreferences prefs = getSharedPreferences("SpinnerPrefs", MODE_PRIVATE);
                    boolean rememberState = prefs.getBoolean("remember_state", false);
                    if (rememberState) {
                        int savedPosition = prefs.getInt("spinner2_position", 0);
                        if (savedPosition < screenOptions.size()) {
                            if (rememberMe.isChecked()) {
                                spinnerID.setSelection(savedPosition);

                            }
                        }
                    }
                }
                onFinish.run();
            }

            @Override
            public void onFailure(Call<List<Root>> call, Throwable t) {
                onFinish.run();
            }
        });
        return screenOptions;
    }

    private void findViews() {
        businessRules = findViewById(R.id.business_rule);
        displayText = findViewById(R.id.text_display);
        rememberMe = findViewById(R.id.rememberMeCh);
        topAppBar = findViewById(R.id.topAppBar);
        spinner1 = findViewById(R.id.spinner1);
        spinner2 = findViewById(R.id.spinner2);
        spinnerID = findViewById(R.id.spinnerID);
//        spinner_select_screen=findViewById( R.id.spinner_select_screen);
        play = findViewById(R.id.loginbtn);
        loginrootlayout = findViewById(R.id.loginrootlayout);
    }

    public void setPermissions(String mediaPermission) {
        Dexter.withActivity(ac)
                .withPermissions(
                        Manifest.permission.CAMERA,
                        mediaPermission
                ).withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        // check if all permissions are granted
                        if (report.areAllPermissionsGranted()) {
                            waitingLogo.animate()
                                    .scaleX(2.2f)
                                    .scaleY(2.2f)
                                    .alpha(0f)
                                    .setDuration(2000)
                                    .setInterpolator(new DecelerateInterpolator())
                                    .withEndAction(() -> {
                                        Intent intent = new Intent(getApplicationContext(), TestCamera.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(intent);

                                    })
                                    .start();

                        }

                        // check for permanent denial of any permission
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            // permission is denied permenantly, navigate user to app settings
                            AlertDialog.Builder builder1 = new AlertDialog.Builder(ac);
                            builder1.setMessage("App will not work properly, please allow all permissions");
                            builder1.setCancelable(true);

                            builder1.setPositiveButton(
                                    "OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    });
                            AlertDialog alert11 = builder1.create();
                            alert11.show();
                        }

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }

                }).check();
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        AdDatabase adDatabase = AdDatabase.getInstance(context);

        new Thread(() -> {
            int impressionsCount = adDatabase.impDao().getAllImpressions().size();
            runOnUiThread(() -> {
                if (impressionsCount > 0) {
                    syncData.setVisibility(View.VISIBLE);
                } else {
                    syncData.setVisibility(View.GONE);
                }
                findViewById(R.id.selectScreen).setPadding(dpToPx(15), dpToPx(15), dpToPx(15), dpToPx(15));
                findViewById(R.id.nestedScrollView).setVisibility(View.VISIBLE);
                findViewById(R.id.logo).setVisibility(View.VISIBLE);
                reportBt.setVisibility(View.VISIBLE);
                //syncData.setVisibility(View.VISIBLE);
                waitingLogo.setVisibility(View.GONE);
                waitingLogo.setScaleX(0.6f);
                waitingLogo.setScaleY(0.6f);
                waitingLogo.setAlpha(1f); // لازم 1

            });
        }).start();

    }

}
