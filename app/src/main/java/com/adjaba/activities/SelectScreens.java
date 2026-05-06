package com.adjaba.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
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
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;
import com.adjaba.R;
import com.adjaba.activities.viewmodel.DataHolder;
import com.adjaba.models.newmodels.MediaModel;
import com.adjaba.models.newmodels.Root;
import com.adjaba.models.newmodels.VideoImageModel;
import com.adjaba.models.newmodels.WatchingModel;
import com.adjaba.others.TargetHours;
import com.adjaba.room.AdDatabase;
import com.adjaba.room.AdEntity;
import com.adjaba.room.InfoEntity;
import com.adjaba.utilities.AuthManager;
import com.adjaba.utilities.Config;
import com.adjaba.utilities.RetrofitBuilder;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SelectScreens extends AppCompatActivity {

    List<WatchingModel> adList;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    NestedScrollView nestedScrollView;
    private ProgressBar progressBar;
    RetrofitBuilder retrofitBuilder;
    Toolbar topAppBar;
    List<MediaModel> mediaModels = new ArrayList<>();
    Context context;
    Activity ac;
    LinearLayout bot_lay,logosLayout;
    private int waitingData=0;
    Map<String, String> screenPlayerMap, screenLocationMap, screenDeviceMap, screenLocation;
    Map<String, List<String>> screenTags;
    List<TargetHours> targetHoursList;
    RelativeLayout loginrootlayout;
    Spinner spinner1, spinner2, spinnerID;
    ProgressBar loadingBar;
    CheckBox rememberMe, displayText, businessRules;
    ImageView adsInfo, picture, logo, waitingLogo;
    List<String> screenOptions1;
    SharedPreferences prefs;
    SharedPreferences.Editor editor;

    private final ExecutorService dbExecutor = Executors.newSingleThreadExecutor();
    List<String> screenOptions = new ArrayList<>();
    ArrayAdapter<String> spinnerAdapter;
    Button play, logOut;
    String mediaFormat = "";
    String orient = "Orientation", screen_id = "Select Screen";
    String timeRefresh = "0";
    int[] loadedCount = {0};

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_screen);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        editor = prefs.edit();
        context = this;
        ac = this;
        logosLayout=findViewById(R.id.logosLayout);
        nestedScrollView = findViewById(R.id.nestedScrollView);
        waitingLogo = findViewById(R.id.waitingLogo);
        loadingBar = findViewById(R.id.loadingBar);
        logOut = findViewById(R.id.logOut);
        screenOptions = new ArrayList<>();
        screenOptions1 = new ArrayList<>();
        retrofitBuilder = new RetrofitBuilder();
        adList = new ArrayList<>();
        targetHoursList = new ArrayList<>();
        findViews();
        screenPlayerMap = new HashMap<>();
        mediaModels = new ArrayList<>();
        screenDeviceMap = new HashMap<>();
        screenLocation = new HashMap<>();
        screenLocationMap = new HashMap<>();
        screenTags = new HashMap<>();
        screenOptions.add("Select Screen"); // العنصر الأول الثابت
        //screenOptions1.add("Select Screen");
        spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, screenOptions);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerID.setAdapter(spinnerAdapter);
        logo = findViewById(R.id.loadingLogo);
        String[] orientationOptions = {"Orientation", "Landscape", "Portrait", "Forced Portrait"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                orientationOptions
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        int spinner1Pos = prefs.getInt("spinner1_position", 0);
        int spinner2Pos = prefs.getInt("spinner2_position", 0);
        rememberMe.setChecked(true);
        SharedPreferences prefsw = getSharedPreferences("SpinnerPrefs", MODE_PRIVATE);
        int savedPositionw = prefsw.getInt("spinner2_position", 0);
        getIDs(context, () -> {

            if (rememberMe.isChecked()) {
                spinner1.setSelection(spinner1Pos);
                if (savedPositionw < screenOptions.size()) {
                    spinnerID.setSelection(savedPositionw);
                }
                spinner2.setSelection(spinner2Pos);
            }
        });
        adsInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(context, InfoActivity.class));
            }
        });
        waitingLogo.setOnClickListener(new View.OnClickListener() {
            private static final long DOUBLE_CLICK_TIME_DELTA = 300; // 300ms
            long lastClickTime = 0;

            @Override
            public void onClick(View v) {
                if (waitingData == 0) {
                    long clickTime = System.currentTimeMillis();
                    if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {
                        nestedScrollView.setVisibility(View.VISIBLE);
                        logosLayout.setVisibility(View.VISIBLE);
                        waitingLogo.setVisibility(View.GONE);
                        int padding = dpToPx(16);
                        nestedScrollView.setPadding(padding, padding, padding, padding);
                    }
                    lastClickTime = clickTime;
                }
            }
        });
        logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                view.getContext().startActivity(intent);


            }
        });
        spinner1.setAdapter(adapter);

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

// Spinner 2
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


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

// Spinner 3
        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (rememberMe.isChecked()) {
                    editor.putInt("spinner2_position", position);
                    editor.apply();
                }
                timeRefresh = position + "";

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!screen_id.equals("Select Screen") && !orient.equals("Orientation")) {
                    DataHolder.getInstance().screenID = screen_id;
                    DataHolder.getInstance().screenDevice = screenDeviceMap.get(screen_id);
                    DataHolder.getInstance().screenPlayer = screenPlayerMap.get(screen_id);
                    DataHolder.getInstance().locationTypes = screenLocationMap.get(screen_id);
                    DataHolder.getInstance().location = screenLocation.get(screen_id);
                    DataHolder.getInstance().tags = screenTags.get(screen_id);

                    DataHolder.getInstance().orient = orient;
                    DataHolder.getInstance().time = timeRefresh;
                    if (displayText.isChecked()) {
                        DataHolder.getInstance().displayFlag = 1;
                    }
                    if (!displayText.isChecked()) {
                        DataHolder.getInstance().displayFlag = 0;
                    }
                    if (businessRules.isChecked()) {
                        DataHolder.getInstance().targetHoursFlag = 1;
                    }
                    if (!businessRules.isChecked()) {
                        DataHolder.getInstance().targetHoursFlag = 0;
                    }

                    Executors.newSingleThreadExecutor().execute(() -> {
                        new Handler(Looper.getMainLooper()).post(() -> {
                            setWaitingLogo();
                            getAds(0);
                        });
                    });
                } else {
                    Toast.makeText(context, "Please select orientation and screen id", Toast.LENGTH_LONG).show();
                }
            }


        });
        rememberMe.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (savedPositionw < screenOptions.size()) {
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
    }

    void getAds(int flag) {
        AdDatabase adDatabase = AdDatabase.getInstance(context);

        // 📌 LOG: Starting ad fetching process
        android.util.Log.i("SelectScreens", "🎬 getAds() started - screenID: " + screen_id);

        // نشتغل على Background Thread
        new Thread(() -> {
            // 1️⃣ مسح قاعدة البيانات
            adDatabase.adDao().deleteAllAds().subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread()).subscribe(new CompletableObserver() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onComplete() {
                            android.util.Log.i("SelectScreens", "✅ Database cleared - All old ads deleted");
                        }

                        @Override
                        public void onError(Throwable e) {
                            android.util.Log.e("SelectScreens", "❌ Error clearing database: " + e.getMessage());
                        }
                    });

            // 2️⃣ مسح البيانات من الذاكرة
            if (adList != null) {
                adList.clear();
            }
            DataHolder.getInstance().advertIds.clear();
            // 3️⃣ نكمل تحميل الإعلانات بعد المسح
            new Handler(Looper.getMainLooper()).post(() -> {

                // 📌 LOG: API call details
                String screenIdForApi = screen_id.contains("/") ? screen_id.split("/")[0] : screen_id;
                android.util.Log.i("SelectScreens", "🔗 API call - endpoint: get_screen_playlists/" + screenIdForApi);

                retrofitBuilder.apiCalls()
                        .getAdsByScreen(screenIdForApi, "Bearer " + AuthManager.getToken(this))
                        .enqueue(new Callback<List<WatchingModel>>() {
                            @Override
                            public void onResponse(Call<List<WatchingModel>> call, Response<List<WatchingModel>> response) {
                                // 📌 LOG: API response received
                                android.util.Log.i("SelectScreens", "📨 API response code: " + response.code());

                                if (response.code() == 200) {
                                    adList = response.body();
                                    android.util.Log.i("SelectScreens", "📦 Ads received from API: " + (adList == null ? "NULL" : adList.size() + " ads"));
                                    if (adList == null || adList.isEmpty() || adList.size() == 0) {
                                        android.util.Log.w("SelectScreens", "⚠️ No ads returned from API for screenID: " + screen_id);
                                        if (!screen_id.equals("Select Screen") && !orient.equals("Orientation")) {
                                            DataHolder.getInstance().screenID = screen_id;
                                            DataHolder.getInstance().screenDevice = screenDeviceMap.get(screen_id);
                                            DataHolder.getInstance().screenPlayer = screenPlayerMap.get(screen_id);
                                            DataHolder.getInstance().locationTypes = screenLocationMap.get(screen_id);
                                            DataHolder.getInstance().location = screenLocation.get(screen_id);
                                            DataHolder.getInstance().tags = screenTags.get(screen_id);
                                            DataHolder.getInstance().allAds = new ArrayList<>();
                                            DataHolder.getInstance().orient = orient;
                                            DataHolder.getInstance().time = timeRefresh;
                                            if (displayText.isChecked()) {
                                                DataHolder.getInstance().displayFlag = 1;
                                            }
                                            if (!displayText.isChecked()) {
                                                DataHolder.getInstance().displayFlag = 0;
                                            }
                                            if (businessRules.isChecked()) {
                                                DataHolder.getInstance().targetHoursFlag = 1;
                                            }
                                            if (!businessRules.isChecked()) {
                                                DataHolder.getInstance().targetHoursFlag = 0;
                                            }
                                            // No ads — launch player to show weather + news only
                                            new Handler(Looper.getMainLooper()).post(() -> {
                                                waitingLogo.animate()
                                                        .scaleX(2.2f).scaleY(2.2f).alpha(0f)
                                                        .setDuration(1000)
                                                        .setInterpolator(new DecelerateInterpolator())
                                                        .withEndAction(() -> {
                                                            android.util.Log.i("SelectScreens", "🚀 Launching AdvertWatching (NO ADS) - orientation: " + orient);
                                                            if (orient.toLowerCase().equalsIgnoreCase("forced portrait")) {
                                                                startActivity(new Intent(context, AdvertLandWatch.class));
                                                            } else {
                                                                startActivity(new Intent(context, AdvertWatching.class));
                                                            }
                                                        }).start();
                                            });
                                        } else {
                                            Toast.makeText(context, "Please select orientation and screen id", Toast.LENGTH_LONG).show();
                                        }
                                    } else {
                                        android.util.Log.i("SelectScreens", "✨ Starting to download " + adList.size() + " ads");
                                        waitingData=1;
                                        if (executorService == null || executorService.isShutdown()) {
                                            executorService = Executors.newSingleThreadExecutor();
                                        }
                                        executorService.execute(() -> {
                                            for (int i = 0; i < adList.size(); i++) {
                                                android.util.Log.d("SelectScreens", "  📥 Ad " + (i+1) + "/" + adList.size() + " - ID: " + adList.get(i).adContractData.advertId);
                                                DataHolder.getInstance().advertIds.add(adList.get(i).adContractData.advertId);
                                                String format = adList.get(i).adContractData.format.toLowerCase();
                                                String videoUrl = adList.get(i).adContractData.videoUrl;
                                                int duration = adList.get(i).duration;

                                                getUrl(
                                                        adList.get(i).contractId,
                                                        adList.get(i).currency,
                                                        adList.get(i).maxBid,
                                                        adList.get(i).adContractData.targetHours,
                                                        adList.get(i).adContractData.textTop,
                                                        adList.get(i).adContractData.textRight,
                                                        adList.get(i).adContractData.textLeft,
                                                        adList.get(i).adContractData.textBottom,
                                                        adList.get(i).adContractData.advertId,
                                                        adList.get(i).screenId,
                                                        videoUrl,
                                                        format,
                                                        loadedCount,
                                                        adList.size(),
                                                        duration,
                                                        context,
                                                        flag,
                                                        i
                                                );
                                            }
                                        });
                                    }
                                } else {
                                    android.util.Log.e("SelectScreens", "❌ API error - response code: " + response.code());
                                    if (!screen_id.equals("Select Screen") && !orient.equals("Orientation")) {
                                        DataHolder.getInstance().screenID = screen_id;
                                        DataHolder.getInstance().screenDevice = screenDeviceMap.get(screen_id);
                                        DataHolder.getInstance().screenPlayer = screenPlayerMap.get(screen_id);
                                        DataHolder.getInstance().locationTypes = screenLocationMap.get(screen_id);
                                        DataHolder.getInstance().location = screenLocation.get(screen_id);
                                        DataHolder.getInstance().tags = screenTags.get(screen_id);
                                        DataHolder.getInstance().allAds = new ArrayList<>();
                                        DataHolder.getInstance().orient = orient;
                                        DataHolder.getInstance().time = timeRefresh;
                                        if (displayText.isChecked()) { DataHolder.getInstance().displayFlag = 1; }
                                        if (!displayText.isChecked()) { DataHolder.getInstance().displayFlag = 0; }
                                        if (businessRules.isChecked()) { DataHolder.getInstance().targetHoursFlag = 1; }
                                        if (!businessRules.isChecked()) { DataHolder.getInstance().targetHoursFlag = 0; }
                                        // API error — still show weather + news
                                        new Handler(Looper.getMainLooper()).post(() -> {
                                            waitingLogo.animate()
                                                    .scaleX(2.2f).scaleY(2.2f).alpha(0f)
                                                    .setDuration(1000)
                                                    .setInterpolator(new DecelerateInterpolator())
                                                    .withEndAction(() -> {
                                                        android.util.Log.i("SelectScreens", "🚀 Launching AdvertWatching (API ERROR) - showing weather/news only");
                                                        if (orient.toLowerCase().equalsIgnoreCase("forced portrait")) {
                                                            startActivity(new Intent(context, AdvertLandWatch.class));
                                                        } else {
                                                            startActivity(new Intent(context, AdvertWatching.class));
                                                        }
                                                    }).start();
                                        });
                                    } else {
                                        Toast.makeText(context, "Please select orientation and screen id", Toast.LENGTH_LONG).show();
                                    }
                                }
                            }

                            @Override
                            public void onFailure(Call<List<WatchingModel>> call, Throwable t) {
                                String errorMsg = t != null ? t.getMessage() : "Unknown error";
                                android.util.Log.e("SelectScreens", "❌ Network error in getAds: " + errorMsg);
                                android.util.Log.e("SelectScreens", "🔥 FULL ERROR: ", t);
                                Toast.makeText(context, "Failed to load ads: " + errorMsg, Toast.LENGTH_LONG).show();

                                // Show waiting logo error state
                                new Handler(Looper.getMainLooper()).post(() -> {
                                    waitingLogo.animate()
                                            .scaleX(2.2f).scaleY(2.2f).alpha(0f)
                                            .setDuration(1000)
                                            .setInterpolator(new DecelerateInterpolator())
                                            .withEndAction(() -> {
                                                nestedScrollView.setVisibility(View.VISIBLE);
                                                logosLayout.setVisibility(View.VISIBLE);
                                                waitingLogo.setVisibility(View.GONE);
                                                Toast.makeText(context, "Error: " + errorMsg + " - Going back to screen selection", Toast.LENGTH_LONG).show();
                                            }).start();
                                });
                            }
                        });
            });
        }).start();
    }

    private void getUrl(String contractId, String currency, int maxBid, List<Integer> targetHours, String txtTop, String txtRight, String txtLeft, String info, String advertId, String screenId, String path, String type, int[] loadedCount, int totalCount, int duration, Context context, int flag, int serverOrder) {
        if (path == null || path.isEmpty()) {
            android.util.Log.w("SelectScreens", "⚠️ Ad " + advertId + " has empty path - skipping");
            loadedCount[0]++;
            checkAndLaunchAdvertWatchingIfAllProcessed(loadedCount[0], totalCount, screenId, contractId, maxBid, orient, context);
            return;
        }

        android.util.Log.d("SelectScreens", "🌐 Calling /media/{path} API for ad " + advertId);
        android.util.Log.d("SelectScreens", "   Path: " + path);

        retrofitBuilder.apiCalls().getUrl("Bearer " + AuthManager.getToken(this), path).enqueue(new Callback<VideoImageModel>() {
            @Override
            public void onResponse(Call<VideoImageModel> call, Response<VideoImageModel> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String resolvedUrl = response.body().url;
                    android.util.Log.d("SelectScreens", "  ✅ Got URL from API: " + resolvedUrl.substring(0, Math.min(100, resolvedUrl.length())));

                    String extension;
                    try {
                        int start = resolvedUrl.lastIndexOf('.') + 1;
                        int end = resolvedUrl.contains("?") ? resolvedUrl.indexOf("?") : resolvedUrl.length();
                        extension = resolvedUrl.substring(start, end);
                    } catch (Exception e) {
                        extension = "mp4";
                    }
                    String fileName = UUID.randomUUID().toString() + "." + extension;

                    Executors.newSingleThreadExecutor().execute(() -> {
                        String localPath = downloadFileToInternalStorage(context, resolvedUrl, fileName);
                        if (localPath != null) {
                            android.util.Log.d("SelectScreens", "  💾 Saved ad " + advertId + " locally: " + fileName);
                            if (Objects.equals(path, "") || isImage(path)) {
                                mediaFormat = "Image";
                            } else if (isVideo(path)) {
                                mediaFormat = "Video";
                            }
                            AdEntity ad = new AdEntity(
                                    advertId,
                                    mediaFormat.toUpperCase(),
                                    localPath,
                                    txtTop, info, txtLeft, txtRight,
                                    duration * 1000,
                                    "Landscape",
                                    screenId,
                                    contractId, listToString(targetHours), serverOrder, currency, maxBid
                            );
                            AdDatabase db = AdDatabase.getInstance(context);
                            db.adDao().insertAd(ad);
                            android.util.Log.d("SelectScreens", "  📊 Inserted ad " + advertId + " to Room DB");

                            loadedCount[0]++;
                            targetHoursList.add(new TargetHours(advertId, targetHours));
                            android.util.Log.i("SelectScreens", "📈 Progress: " + loadedCount[0] + "/" + totalCount + " ads loaded");

                            if (loadedCount[0] == totalCount) {
                                android.util.Log.i("SelectScreens", "🎉 ALL ADS DOWNLOADED! Preparing to launch AdvertWatching...");
                                List<AdEntity> ads = db.adDao().getAllAds(screenId);
                                android.util.Log.i("SelectScreens", "   Retrieved " + (ads == null ? "0" : ads.size()) + " ads from DB for screenId: " + screenId);
                                mediaModels.clear();
                                if (ads != null && !ads.isEmpty()) {
                                    for (AdEntity ada : ads) {
                                        if (ada != null && ada.localPath != null) {
                                            mediaModels.add(new MediaModel(contractId, currency, maxBid, ada.format, ada.localPath, ada.duration, ada.textBottom, ada.textTop, "", ada.targetHours, ada.advertId));
                                        }
                                    }
                                }
                                android.util.Log.i("SelectScreens", "   Created MediaModel list with " + mediaModels.size() + " items");

                                new Handler(Looper.getMainLooper()).post(() -> {
                                    DataHolder.getInstance().targetHours = targetHoursList;
                                    DataHolder.getInstance().allAds = mediaModels;
                                    android.util.Log.i("SelectScreens", "   ✅ Updated DataHolder.allAds with " + mediaModels.size() + " MediaModels");

                                    waitingLogo.animate()
                                            .scaleX(2.2f)
                                            .scaleY(2.2f)
                                            .alpha(0f)
                                            .setDuration(1000)
                                            .setInterpolator(new DecelerateInterpolator())
                                            .withEndAction(() -> {
                                                android.util.Log.i("SelectScreens", "🚀 Launching AdvertWatching with " + mediaModels.size() + " ads");
                                                if (orient.toLowerCase().equalsIgnoreCase("forced portrait")) {
                                                    Intent intent = new Intent(context, AdvertLandWatch.class);
                                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                    context.startActivity(intent);
                                                } else {
                                                    Intent intent = new Intent(context, AdvertWatching.class);
                                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                    context.startActivity(intent);
                                                }
                                            })
                                            .start();
                                });
                            }
                        } else {
                            android.util.Log.e("SelectScreens", "❌ Failed to download ad " + advertId + " from resolved URL");
                            loadedCount[0]++;
                            updateDownloadProgress(loadedCount[0], totalCount);
                            AdDatabase db = AdDatabase.getInstance(context);
                            db.infoDao().insertInfo(new InfoEntity("Failed to download media: " + path));
                            // ✅ Check if ALL ads processed (success or failure) and launch if done
                            checkAndLaunchAdvertWatchingIfAllProcessed(loadedCount[0], totalCount, screenId, contractId, maxBid, orient, context);
                        }
                    });
                } else {
                    android.util.Log.e("SelectScreens", "❌ API /media/{path} returned error code: " + response.code());
                    if (response.errorBody() != null) {
                        try {
                            String errorBody = response.errorBody().string();
                            android.util.Log.e("SelectScreens", "   Error body: " + errorBody.substring(0, Math.min(200, errorBody.length())));
                        } catch (Exception e) {
                            android.util.Log.e("SelectScreens", "   Could not read error: " + e.getMessage());
                        }
                    }
                    loadedCount[0]++;
                    updateDownloadProgress(loadedCount[0], totalCount);
                    // ✅ Check if ALL ads processed and launch if done
                    checkAndLaunchAdvertWatchingIfAllProcessed(loadedCount[0], totalCount, screenId, contractId, maxBid, orient, context);
                }
            }

            @Override
            public void onFailure(Call<VideoImageModel> call, Throwable t) {
                String errorMsg = t != null ? t.getMessage() : "unknown";
                android.util.Log.e("SelectScreens", "❌ Network error calling /media/{path} API for ad " + advertId);
                android.util.Log.e("SelectScreens", "   Error: " + (t != null ? t.getClass().getSimpleName() : "null") + " - " + errorMsg);
                if (t != null) {
                    android.util.Log.e("SelectScreens", "   Stack: ", t);
                }
                loadedCount[0]++;
                updateDownloadProgress(loadedCount[0], totalCount);
                // ✅ Check if ALL ads processed and launch if done
                checkAndLaunchAdvertWatchingIfAllProcessed(loadedCount[0], totalCount, screenId, contractId, maxBid, orient, context);
            }
        });
    }
    
    /**
     * Updates the download progress bar on UI thread
     */
    private void updateDownloadProgress(int loaded, int total) {
        new Handler(Looper.getMainLooper()).post(() -> {
            if (loadingBar != null) {
                loadingBar.setProgress(loaded);
                android.util.Log.d("SelectScreens", "🔄 UI Progress: " + loaded + "/" + total);
            }
        });
    }

    /**
     * Checks if all ads have been downloaded/processed and launches AdvertWatching if so
     */
    private void checkAndLaunchAdvertWatchingIfAllProcessed(int loadedCount, int totalCount, String screenId, String contractId, int maxBid, String orient, Context context) {
        if (loadedCount >= totalCount) {
            android.util.Log.i("SelectScreens", "✅ ALL ADS PROCESSED (loaded: " + loadedCount + "/" + totalCount + ")");

            // Execute database access on background thread to avoid crash
            dbExecutor.execute(() -> {
                AdDatabase db = AdDatabase.getInstance(context);
                List<AdEntity> ads = db.adDao().getAllAds(screenId);
                android.util.Log.i("SelectScreens", "   Retrieved " + (ads == null ? "0" : ads.size()) + " ads from DB for screenId: " + screenId);

                mediaModels.clear();
                if (ads != null && !ads.isEmpty()) {
                    for (AdEntity ada : ads) {
                        if (ada != null && ada.localPath != null) {
                            mediaModels.add(new MediaModel(contractId, "", maxBid, ada.format, ada.localPath, ada.duration, ada.textBottom, ada.textTop, "", ada.targetHours, ada.advertId));
                        }
                    }
                }
                android.util.Log.i("SelectScreens", "   Created MediaModel list with " + mediaModels.size() + " items");

                // Post UI updates back to main thread
                new Handler(Looper.getMainLooper()).post(() -> {
                    DataHolder.getInstance().targetHours = targetHoursList;
                    DataHolder.getInstance().allAds = mediaModels;
                    android.util.Log.i("SelectScreens", "   ✅ Updated DataHolder.allAds with " + mediaModels.size() + " MediaModels");

                    waitingLogo.animate()
                            .scaleX(2.2f)
                            .scaleY(2.2f)
                            .alpha(0f)
                            .setDuration(1000)
                            .setInterpolator(new DecelerateInterpolator())
                            .withEndAction(() -> {
                                android.util.Log.i("SelectScreens", "🚀 Launching AdvertWatching with " + mediaModels.size() + " ads");
                                if (orient.toLowerCase().equalsIgnoreCase("forced portrait")) {
                                    Intent intent = new Intent(context, AdvertLandWatch.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    context.startActivity(intent);
                                } else {
                                    Intent intent = new Intent(context, AdvertWatching.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    context.startActivity(intent);
                                }
                            })
                            .start();
                });
            });
        }
    }


    public String listToString(List<Integer> list) {
        if (list == null || list.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i));
            if (i != list.size() - 1) {
                sb.append("/");
            }
        }
        return sb.toString();
    }

    public String listStringToString(List<String> list) {
        if (list == null || list.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i));
            if (i != list.size() - 1) {
                sb.append("/");
            }
        }
        return sb.toString();
    }

    List<String> getIDs(Context context, Runnable onFinish) {
        retrofitBuilder.apiCalls().getScreenResponse("Bearer " + AuthManager.getToken(this)).enqueue(new Callback<List<Root>>() {
            @Override
            public void onResponse(Call<List<Root>> call, Response<List<Root>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (int screen = 0; screen < response.body().size(); screen++) {
                        //screenOptions1.add(response.body().get(screen).screenId);
                        screenPlayerMap.put(response.body().get(screen).screenId, response.body().get(screen).screenPlayer);
                        screenDeviceMap.put(response.body().get(screen).screenId, response.body().get(screen).screenDevice);
                        screenLocationMap.put(response.body().get(screen).screenId, listStringToString(response.body().get(screen).locationType));
                        screenTags.put(response.body().get(screen).screenId, response.body().get(screen).screenTags);
                        screenLocation.put(response.body().get(screen).screenId, response.body().get(screen).location);

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

    public static String downloadFileToInternalStorage(Context context, String fileUrl, String fileName) {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(fileUrl)
                .build();

        try (okhttp3.Response response = client.newCall(request).execute()) {
            android.util.Log.d("SelectScreens", "   📡 HTTP Response code: " + response.code() + " for URL: " + fileUrl);

            if (response.isSuccessful()) {
                InputStream inputStream = response.body().byteStream();
                File file = new File(context.getFilesDir(), fileName);
                FileOutputStream outputStream = new FileOutputStream(file);
                byte[] buffer = new byte[4096];
                int bytesRead;
                long totalBytes = 0;

                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                    totalBytes += bytesRead;
                }

                outputStream.close();
                inputStream.close();

                android.util.Log.d("SelectScreens", "   ✅ Downloaded " + totalBytes + " bytes to: " + file.getAbsolutePath());
                return file.getAbsolutePath();
            } else {
                android.util.Log.e("SelectScreens", "   ❌ HTTP Error " + response.code() + ": " + response.message());
                if (response.body() != null) {
                    try {
                        String errorBody = response.body().string();
                        android.util.Log.e("SelectScreens", "   Error response body: " + errorBody.substring(0, Math.min(500, errorBody.length())));
                    } catch (Exception e) {
                        android.util.Log.e("SelectScreens", "   Could not read error body: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            android.util.Log.e("SelectScreens", "   🔥 IOException downloading " + fileUrl);
            android.util.Log.e("SelectScreens", "   Error: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            if (e.getCause() != null) {
                android.util.Log.e("SelectScreens", "   Caused by: " + e.getCause().getMessage());
            }
            e.printStackTrace();
        }

        return null;
    }

    private void findViews() {
        businessRules = findViewById(R.id.business_rule);
        displayText = findViewById(R.id.text_display);
        rememberMe = findViewById(R.id.rememberMeCh);
        topAppBar = findViewById(R.id.topAppBar);
        bot_lay = findViewById(R.id.bot_lay);
        spinner1 = findViewById(R.id.spinner1);
        adsInfo = findViewById(R.id.ads_info);
        spinnerID = findViewById(R.id.spinnerID);
        progressBar = findViewById(R.id.progressSignOut);
        spinner2 = findViewById(R.id.spinner2);
        play = findViewById(R.id.loginbtn);
        loginrootlayout = findViewById(R.id.loginrootlayout);
        picture = findViewById(R.id.picture);

    }

    @Override
    protected void onResume() {
        super.onResume();
        /*logo.setVisibility(View.GONE);
        logo.clearAnimation();*/
        adList.clear();
        Arrays.fill(loadedCount, 0);
    }


    public static String getFileExtensionFromUrl(String url) {
        if (url != null) {
            return MimeTypeMap.getFileExtensionFromUrl(url);
        }
        return null;
    }

    public static boolean isImage(String url) {
        String ext = getFileExtensionFromUrl(url);
        return ext != null && (ext.equalsIgnoreCase("jpg") || ext.equalsIgnoreCase("jpeg") ||
                ext.equalsIgnoreCase("png") || ext.equalsIgnoreCase("gif") ||
                ext.equalsIgnoreCase("bmp") || ext.equalsIgnoreCase("webp"));
    }

    public static boolean isVideo(String url) {
        String ext = getFileExtensionFromUrl(url);
        return ext != null && (ext.equalsIgnoreCase("mp4") || ext.equalsIgnoreCase("mkv") ||
                ext.equalsIgnoreCase("3gp") || ext.equalsIgnoreCase("avi") ||
                ext.equalsIgnoreCase("mov"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbExecutor.shutdownNow();

    }

    private void setWaitingLogo() {
        logosLayout.setVisibility(View.GONE);
        nestedScrollView.setVisibility(View.GONE);
        waitingLogo.setVisibility(View.VISIBLE);

        findViewById(R.id.mainSelectedScreenLayout).setPadding(0, 0, 0, 0);
        waitingLogo.setAlpha(0f);
        waitingLogo.setScaleX(0.6f);
        waitingLogo.setScaleY(0.6f);

        waitingLogo.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(1500)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(() -> {
                    animateLogoIdle();
                })
                .start();
    }

    // دالة الحركة البسيطة المستمرة
    private void animateLogoIdle() {
        waitingLogo.animate()
                .translationYBy(-10f) // تحريك لأعلى
                .setDuration(500)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(() -> waitingLogo.animate()
                        .translationYBy(10f) // العودة للوضع الطبيعي
                        .setDuration(500)
                        .setInterpolator(new AccelerateDecelerateInterpolator())
                        .withEndAction(this::animateLogoIdle) // تكرار الحركة
                        .start())
                .start();
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        waitingLogo.setVisibility(View.GONE);
        int padding = dpToPx(16);
        nestedScrollView.setPadding(padding, padding, padding, padding);
        nestedScrollView.setVisibility(View.VISIBLE);
        logosLayout.setVisibility(View.VISIBLE);
    }
}
