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
    CheckBox rememberMe, displayText, businessRules, weatherCheckbox, newsCheckbox, iotCheckbox, slideshowCheckbox;
    LinearLayout slideshowConfigContainer;
    android.widget.EditText slideshowFolderUrlInput, slideshowIntervalInput;
    ImageView adsInfo, picture, logo, waitingLogo;
    List<String> screenOptions1;
    SharedPreferences prefs;
    SharedPreferences.Editor editor;

    private final ExecutorService dbExecutor = Executors.newSingleThreadExecutor();
    List<String> screenOptions = new ArrayList<>();
    List<String> screenIdForDisplay = new ArrayList<>();
    ArrayAdapter<String> spinnerAdapter;
    Button play, logOut;
    String mediaFormat = "";
    String orient = "Orientation", screen_id = "Select Screen";
    String timeRefresh = "0";
    int[] loadedCount = {0};
    /** Guards against re-auth retry loop: only attempt one token refresh per getAds() session. */
    private volatile boolean authRetried = false;
    /** True when launched by BootReceiver — DataHolder is restored from prefs, not from the UI. */
    private boolean autoPlayMode = false;

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
        screenIdForDisplay = new ArrayList<>();
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
        screenIdForDisplay.add("Select Screen");
        //screenOptions1.add("Select Screen");
        spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, screenOptions);
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerID.setAdapter(spinnerAdapter);
        logo = findViewById(R.id.loadingLogo);
        String[] orientationOptions = {"Orientation", "Landscape", "Portrait", "Forced Portrait", "Split Screen"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                orientationOptions
        );
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        int spinner1Pos = prefs.getInt("spinner1_position", 0);
        int spinner2Pos = prefs.getInt("spinner2_position", 0);
        rememberMe.setChecked(true);
        
        // Restore IOT preference (default: false - unchecked)
        boolean iotEnabled = prefs.getBoolean("iot_enabled", false);
        if (iotCheckbox != null) {
            iotCheckbox.setChecked(iotEnabled);
        } else {
            android.util.Log.w("SelectScreens", "⚠️ Warning: iotCheckbox not found in layout");
        }

        // Restore Cloud Slideshow config — stored independently of the CMS/backend session,
        // so it survives regardless of screen/orientation selection or offline state.
        if (slideshowCheckbox != null) {
            boolean slideshowEnabled = com.adjaba.utilities.SlideshowManager.isEnabled(context);
            slideshowCheckbox.setChecked(slideshowEnabled);
            slideshowFolderUrlInput.setText(com.adjaba.utilities.SlideshowManager.getFolderUrl(context));
            slideshowIntervalInput.setText(String.valueOf(com.adjaba.utilities.SlideshowManager.getIntervalSeconds(context)));
            slideshowConfigContainer.setVisibility(slideshowEnabled ? View.VISIBLE : View.GONE);
            slideshowCheckbox.setOnCheckedChangeListener((buttonView, isChecked) ->
                    slideshowConfigContainer.setVisibility(isChecked ? View.VISIBLE : View.GONE));
        }

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
                // Explicit logout: stop boot auto-resume and clear the offline ad + slideshow
                // caches. (The cache wipe used to live in LoginActivity.onCreate, which
                // destroyed offline playback on every app start — now it only happens here.)
                prefs.edit().putBoolean("resume_enabled", false).apply();
                Executors.newSingleThreadExecutor().execute(() -> {
                    // deleteAllAds() returns an RxJava Completable, which is lazy and does
                    // NOT run its query unless subscribed — blockingAwait() here actually
                    // executes the delete (safe: already off the main thread).
                    AdDatabase.getInstance(context).adDao().deleteAllAds().blockingAwait();
                    com.adjaba.utilities.SlideshowManager.clearCache(context);
                });
                Intent intent = new Intent(view.getContext(), LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                view.getContext().startActivity(intent);


            }
        });
        spinner1.setAdapter(adapter);

        ArrayAdapter<String> intervalAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                getResources().getStringArray(R.array.interval_arrays)
        );
        intervalAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinner2.setAdapter(intervalAdapter);

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
                screen_id = screenIdForDisplay.get(position);


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
                    if (weatherCheckbox.isChecked()) {
                        DataHolder.getInstance().weatherFlag = 1;
                    }
                    if (!weatherCheckbox.isChecked()) {
                        DataHolder.getInstance().weatherFlag = 0;
                    }
                    if (newsCheckbox.isChecked()) {
                        DataHolder.getInstance().newsFlag = 1;
                    }
                    if (!newsCheckbox.isChecked()) {
                        DataHolder.getInstance().newsFlag = 0;
                    }

                    // Save IOT preference
                    if (iotCheckbox != null) {
                        editor.putBoolean("iot_enabled", iotCheckbox.isChecked());
                    }
                    editor.apply();

                    // Save Cloud Slideshow config and kick off a background refresh. Never
                    // blocks PLAY — the rotation uses whatever is already cached, if anything.
                    if (slideshowCheckbox != null) {
                        int intervalSeconds;
                        try {
                            intervalSeconds = Integer.parseInt(slideshowIntervalInput.getText().toString().trim());
                        } catch (NumberFormatException e) {
                            intervalSeconds = 5;
                        }
                        com.adjaba.utilities.SlideshowManager.saveConfig(context,
                                slideshowCheckbox.isChecked(),
                                slideshowFolderUrlInput.getText().toString(),
                                intervalSeconds);
                        if (slideshowCheckbox.isChecked()) {
                            com.adjaba.workers.SlideshowSyncWorker.triggerImmediateSync(context);
                        }
                    }

                    // Persist session so BootReceiver can auto-resume playback after reboot
                    saveResumeState();

                    Executors.newSingleThreadExecutor().execute(() -> {
                        new Handler(Looper.getMainLooper()).post(() -> {
                            // Set current screen ID for background sync worker
                            com.adjaba.workers.AdSyncWorker.setCurrentScreenId(context, screen_id);
                            authRetried = false; // reset for this fresh PLAY press
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

        // Auto-resume after reboot (launched by BootReceiver): skip the UI and start
        // playback with the saved configuration. getAds() handles both cases — online
        // it syncs with the backend, offline it plays the locally cached ads.
        if (getIntent().getBooleanExtra("auto_play", false) && restoreResumeState()) {
            android.util.Log.i("SelectScreens", " AUTO-PLAY: resuming screen " + screen_id + " (" + orient + ")");
            autoPlayMode = true;
            com.adjaba.workers.AdSyncWorker.setCurrentScreenId(context, screen_id);
            authRetried = false;
            setWaitingLogo();
            getAds(0);
        }
    }

    /**
     * Persists everything needed to restart playback unattended (after reboot).
     * Screen metadata may be null when PLAY was pressed offline — in that case the
     * previously saved value is kept instead of being overwritten with null.
     */
    private void saveResumeState() {
        DataHolder d = DataHolder.getInstance();
        SharedPreferences.Editor e = prefs.edit();
        e.putBoolean("resume_enabled", true);
        e.putString("resume_screen_id", screen_id);
        e.putString("resume_orient", orient);
        e.putString("resume_time", timeRefresh);
        e.putInt("resume_display_flag", d.displayFlag);
        e.putInt("resume_target_hours_flag", d.targetHoursFlag);
        e.putInt("resume_weather_flag", d.weatherFlag);
        e.putInt("resume_news_flag", d.newsFlag);
        if (d.screenDevice != null)  e.putString("resume_screen_device", d.screenDevice);
        if (d.screenPlayer != null)  e.putString("resume_screen_player", d.screenPlayer);
        if (d.locationTypes != null) e.putString("resume_location_types", d.locationTypes);
        if (d.location != null)      e.putString("resume_location", d.location);
        if (d.tags != null)          e.putString("resume_tags", android.text.TextUtils.join(",", d.tags));
        e.apply();
    }

    /**
     * Restores the saved playback session into this activity's fields and DataHolder.
     *
     * @return true if a complete session was restored and playback can start.
     */
    private boolean restoreResumeState() {
        if (!prefs.getBoolean("resume_enabled", false)) return false;
        String savedScreen = prefs.getString("resume_screen_id", null);
        String savedOrient = prefs.getString("resume_orient", null);
        if (savedScreen == null || savedOrient == null) return false;

        screen_id = savedScreen;
        orient = savedOrient;
        timeRefresh = prefs.getString("resume_time", "0");

        DataHolder d = DataHolder.getInstance();
        d.screenID = screen_id;
        d.orient = orient;
        d.time = timeRefresh;
        d.displayFlag     = prefs.getInt("resume_display_flag", 0);
        d.targetHoursFlag = prefs.getInt("resume_target_hours_flag", 0);
        d.weatherFlag     = prefs.getInt("resume_weather_flag", 1);
        d.newsFlag        = prefs.getInt("resume_news_flag", 1);
        d.screenDevice    = prefs.getString("resume_screen_device", null);
        d.screenPlayer    = prefs.getString("resume_screen_player", null);
        d.locationTypes   = prefs.getString("resume_location_types", null);
        d.location        = prefs.getString("resume_location", null);
        String tags = prefs.getString("resume_tags", null);
        d.tags = (tags != null && !tags.isEmpty())
                ? new ArrayList<>(Arrays.asList(tags.split(",")))
                : new ArrayList<>();
        return true;
    }

    void getAds(int flag) {
        AdDatabase adDatabase = AdDatabase.getInstance(context);

        //  LOG: Starting ad fetching process
        android.util.Log.i("SelectScreens", " getAds() started - screenID: " + screen_id);

        // Clear memory cache
        if (adList != null) {
            adList.clear();
        }
        DataHolder.getInstance().advertIds.clear();

        // نشتغل على Background Thread
        new Thread(() -> {
            //  SMART SYNC: Compare backend playlist with local database
            String screenIdForApi = screen_id.contains("/") ? screen_id.split("/")[0] : screen_id;

            // Get existing local ad IDs for this screen
            List<String> localAdIds = adDatabase.adDao().getAdIdsByScreen(screen_id);
            android.util.Log.i("SelectScreens", " Local database has " + (localAdIds == null ? 0 : localAdIds.size()) + " ads for screen " + screen_id);

            // Fetch backend playlist
            new Handler(Looper.getMainLooper()).post(() -> {
                android.util.Log.i("SelectScreens", " API call - endpoint: get_screen_playlists/" + screenIdForApi);

                retrofitBuilder.apiCalls()
                        .getAdsByScreen(screenIdForApi, "Bearer " + AuthManager.getToken(this))
                        .enqueue(new Callback<List<WatchingModel>>() {
                            @Override
                            public void onResponse(Call<List<WatchingModel>> call, Response<List<WatchingModel>> response) {
                                //  LOG: API response received
                                android.util.Log.i("SelectScreens", " API response code: " + response.code());

                                if (response.code() == 200) {
                                    adList = response.body();
                                    android.util.Log.i("SelectScreens", " Ads received from API: " + (adList == null ? "NULL" : adList.size() + " ads"));

                                    if (adList == null || adList.isEmpty() || adList.size() == 0) {
                                        android.util.Log.w("SelectScreens", "⚠️ No ads returned from API for screenID: " + screen_id);

                                        // ── OFFLINE MODE: Use cached ads if available ──
                                        Executors.newSingleThreadExecutor().execute(() -> {
                                            AdDatabase db = AdDatabase.getInstance(context);
                                            List<AdEntity> cachedAds = db.adDao().getAllAds(screen_id);

                                            if (cachedAds != null && !cachedAds.isEmpty()) {
                                                 android.util.Log.i("SelectScreens", " OFFLINE MODE: Using " + cachedAds.size() + " cached ads");

                                                 // Build media models from cached ads
                                                 mediaModels.clear();
                                                 for (AdEntity ada : cachedAds) {
                                                     if (ada.localPath != null) {
                                                         mediaModels.add(mediaModelFromAdEntity(ada));
                                                         DataHolder.getInstance().advertIds.add(ada.advertId);
                                                     }
                                                 }

                                                new Handler(Looper.getMainLooper()).post(() -> {
                                                    DataHolder.getInstance().allAds = withSlideshowImages(mediaModels);
                                                    launchAdvertWatchingActivity(orient, context);
                                                });
                                            } else {
                                                // No cached ads - show weather/news only
                                                android.util.Log.w("SelectScreens", "⚠️ No cached ads available - launching with weather/news only");
                                                new Handler(Looper.getMainLooper()).post(() -> {
                                                    setupDataHolderAndLaunch(orient, context);
                                                });
                                            }
                                        });
                                    } else {
                                        // ── SMART SYNC: Compare backend vs local ──
                                        android.util.Log.i("SelectScreens", " SMART SYNC: Comparing backend playlist with local database");

                                        // Get backend ad IDs
                                        List<String> backendAdIds = new ArrayList<>();
                                        for (WatchingModel ad : adList) {
                                            backendAdIds.add(ad.adContractData.advertId);
                                        }

                                        // Determine NEW ads (in backend but not in local)
                                        List<String> newAdIds = new ArrayList<>();
                                        for (String backendId : backendAdIds) {
                                            if (!localAdIds.contains(backendId)) {
                                                newAdIds.add(backendId);
                                            }
                                        }

                                        // Determine REMOVED ads (in local but not in backend)
                                        List<String> removedAdIds = new ArrayList<>();
                                        for (String localId : localAdIds) {
                                            if (!backendAdIds.contains(localId)) {
                                                removedAdIds.add(localId);
                                            }
                                        }

                                        android.util.Log.i("SelectScreens", "    NEW ads to download: " + newAdIds.size());
                                        android.util.Log.i("SelectScreens", "   ️ REMOVED ads to delete: " + removedAdIds.size());
                                        android.util.Log.i("SelectScreens", "   ✅ EXISTING ads (keep): " + (localAdIds.size() - removedAdIds.size()));

                                        // Delete removed ads and their media files
                                        Executors.newSingleThreadExecutor().execute(() -> {
                                            AdDatabase db = AdDatabase.getInstance(context);
                                            for (String removedId : removedAdIds) {
                                                AdEntity removedAd = db.adDao().getAdById(removedId);
                                                if (removedAd != null && removedAd.localPath != null) {
                                                    File mediaFile = new File(removedAd.localPath);
                                                    if (mediaFile.exists()) {
                                                        boolean deleted = mediaFile.delete();
                                                        android.util.Log.i("SelectScreens", "   ️ Deleted media file: " + mediaFile.getName() + " (success=" + deleted + ")");
                                                    }
                                                }
                                                db.adDao().deleteAdById(removedId);
                                                android.util.Log.i("SelectScreens", "   ️ Deleted ad from DB: " + removedId);
                                            }
                                        });

                                        // Download only NEW ads
                                        if (newAdIds.isEmpty()) {
                                            android.util.Log.i("SelectScreens", "✅ No new ads to download - playlist is up to date");

                                            // Load existing ads from database and launch
                                            Executors.newSingleThreadExecutor().execute(() -> {
                                                AdDatabase db = AdDatabase.getInstance(context);
                                                List<AdEntity> existingAds = db.adDao().getAllAds(screen_id);

                                                 mediaModels.clear();
                                                for (AdEntity ada : existingAds) {
                                                    if (ada.localPath != null) {
                                                        mediaModels.add(mediaModelFromAdEntity(ada));
                                                        DataHolder.getInstance().advertIds.add(ada.advertId);
                                                    }
                                                }

                                                new Handler(Looper.getMainLooper()).post(() -> {
                                                    DataHolder.getInstance().targetHours = targetHoursList;
                                                    DataHolder.getInstance().allAds = withSlideshowImages(mediaModels);
                                                    launchAdvertWatchingActivity(orient, context);
                                                });
                                            });
                                        } else {
                                            android.util.Log.i("SelectScreens", "✨ Starting to download " + newAdIds.size() + " new ads");
                                            waitingData = 1;
                                            if (executorService == null || executorService.isShutdown()) {
                                                executorService = Executors.newSingleThreadExecutor();
                                            }

                                            // Download only new ads
                                            executorService.execute(() -> {
                                                for (int i = 0; i < adList.size(); i++) {
                                                    WatchingModel ad = adList.get(i);
                                                    String advertId = ad.adContractData.advertId;

                                                    // Skip ads already in local database
                                                    if (!newAdIds.contains(advertId)) {
                                                        android.util.Log.d("SelectScreens", "  ⏭️ Ad " + (i+1) + "/" + adList.size() + " - ID: " + advertId + " (already in DB, skipping)");
                                                        DataHolder.getInstance().advertIds.add(advertId);
                                                        continue;
                                                    }

                                                    android.util.Log.d("SelectScreens", "   Ad " + (i+1) + "/" + adList.size() + " - ID: " + advertId + " (NEW - downloading)");
                                                    DataHolder.getInstance().advertIds.add(advertId);
                                                    String format = ad.adContractData.format.toLowerCase();
                                                                    String videoUrl = ad.adContractData.videoUrl;
                                                                    int duration = ad.duration;

                                                    getUrl(
                                                            ad.contractId,
                                                            ad.currency,
                                                            ad.maxBid,
                                                            ad.adContractData.targetHours,
                                                            ad.adContractData.textTop,
                                                            ad.adContractData.textRight,
                                                            ad.adContractData.textLeft,
                                                            ad.adContractData.textBottom,
                                                            ad.adContractData.advertId,
                                                            ad.screenId,
                                                            videoUrl,
                                                            format,
                                                            loadedCount,
                                                            newAdIds.size(),  // Only count NEW ads for progress
                                                            duration,
                                                            context,
                                                            flag,
                                                            i,
                                                            ad.adContractData.targetGender,
                                                            ad.adContractData.targetAgeGroup,
                                                            ad.adContractData.targetTags,
                                                            ad.adContractData.targetEmotion,
                                                            ad.adContractData.streamType,
                                                            ad.adContractData.socialPlatform,
                                                            ad.adContractData.socialHashtag
                                                    );
                                                }
                                            });
                                        }
                                    }
                                } else {
                                    android.util.Log.e("SelectScreens", "❌ API error - response code: " + response.code());

                                    // ── 401: token expired → re-authenticate and retry once ──
                                    if (response.code() == 401 && !authRetried) {
                                        authRetried = true;
                                        android.util.Log.w("SelectScreens", "⚠️ 401 Unauthorized — refreshing token and retrying...");
                                        Executors.newSingleThreadExecutor().execute(() -> {
                                            AuthManager.reAuthenticateSync(context);   // saves new token internally
                                            new Handler(Looper.getMainLooper()).post(() -> getAds(flag));
                                        });
                                        return;
                                    }

                                    // ── OFFLINE MODE: API error, try to use cached ads ──
                                    Executors.newSingleThreadExecutor().execute(() -> {
                                        AdDatabase db = AdDatabase.getInstance(context);
                                        List<AdEntity> cachedAds = db.adDao().getAllAds(screen_id);

                                        if (cachedAds != null && !cachedAds.isEmpty()) {
                                            android.util.Log.i("SelectScreens", " API ERROR - Using " + cachedAds.size() + " cached ads from offline storage");

                                            mediaModels.clear();
                                            for (AdEntity ada : cachedAds) {
                                                if (ada.localPath != null) {
                                                    mediaModels.add(mediaModelFromAdEntity(ada));
                                                    DataHolder.getInstance().advertIds.add(ada.advertId);
                                                }
                                            }

                                            new Handler(Looper.getMainLooper()).post(() -> {
                                                DataHolder.getInstance().allAds = withSlideshowImages(mediaModels);
                                                launchAdvertWatchingActivity(orient, context);
                                            });
                                        } else {
                                            // No cached ads - show error and return to SelectScreens
                                            android.util.Log.e("SelectScreens", "❌ No cached ads available - cannot launch player");
                                            new Handler(Looper.getMainLooper()).post(() -> {
                                                setupDataHolderAndLaunch(orient, context);
                                            });
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onFailure(Call<List<WatchingModel>> call, Throwable t) {
                                String errorMsg = t != null ? t.getMessage() : "Unknown error";
                                android.util.Log.e("SelectScreens", "❌ Network error in getAds: " + errorMsg);
                                android.util.Log.e("SelectScreens", " FULL ERROR: ", t);

                                // ── OFFLINE MODE: Network failure, try cached ads ──
                                Executors.newSingleThreadExecutor().execute(() -> {
                                    AdDatabase db = AdDatabase.getInstance(context);
                                    List<AdEntity> cachedAds = db.adDao().getAllAds(screen_id);

                                    if (cachedAds != null && !cachedAds.isEmpty()) {
                                        android.util.Log.i("SelectScreens", " NETWORK ERROR - Using " + cachedAds.size() + " cached ads from offline storage");

                                        mediaModels.clear();
                                        for (AdEntity ada : cachedAds) {
                                            if (ada.localPath != null) {
                                                mediaModels.add(mediaModelFromAdEntity(ada));
                                                DataHolder.getInstance().advertIds.add(ada.advertId);
                                            }
                                        }

                                        new Handler(Looper.getMainLooper()).post(() -> {
                                            DataHolder.getInstance().allAds = withSlideshowImages(mediaModels);
                                            Toast.makeText(context, "Offline mode: Using cached ads", Toast.LENGTH_LONG).show();
                                            launchAdvertWatchingActivity(orient, context);
                                        });
                                    } else {
                                        // No cached ads - show error
                                        android.util.Log.e("SelectScreens", "❌ Network failed and no cached ads available");
                                        new Handler(Looper.getMainLooper()).post(() -> {
                                            Toast.makeText(context, "Network error and no cached ads: " + errorMsg, Toast.LENGTH_LONG).show();

                                            waitingLogo.animate()
                                                    .scaleX(2.2f).scaleY(2.2f).alpha(0f)
                                                    .setDuration(1000)
                                                    .setInterpolator(new DecelerateInterpolator())
                                                    .withEndAction(() -> {
                                                        nestedScrollView.setVisibility(View.VISIBLE);
                                                        logosLayout.setVisibility(View.VISIBLE);
                                                        waitingLogo.setVisibility(View.GONE);
                                                    }).start();
                                        });
                                    }
                                });
                            }
                        });
            });
        }).start();
    }

    /**
     * Helper method to set up DataHolder and launch AdvertWatching activity
     */
    private void setupDataHolderAndLaunch(String orient, Context context) {
        // In auto-play mode DataHolder was already restored from prefs — the UI maps and
        // checkboxes are empty/default here and would overwrite it with wrong values.
        if (autoPlayMode) {
            DataHolder.getInstance().allAds = withSlideshowImages(new ArrayList<>());
            launchAdvertWatchingActivity(orient, context);
            return;
        }
        if (!screen_id.equals("Select Screen") && !orient.equals("Orientation")) {
            DataHolder.getInstance().screenID = screen_id;
            DataHolder.getInstance().screenDevice = screenDeviceMap.get(screen_id);
            DataHolder.getInstance().screenPlayer = screenPlayerMap.get(screen_id);
            DataHolder.getInstance().locationTypes = screenLocationMap.get(screen_id);
            DataHolder.getInstance().location = screenLocation.get(screen_id);
            DataHolder.getInstance().tags = screenTags.get(screen_id);
            DataHolder.getInstance().allAds = withSlideshowImages(new ArrayList<>());
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
            if (weatherCheckbox.isChecked()) {
                DataHolder.getInstance().weatherFlag = 1;
            }
            if (!weatherCheckbox.isChecked()) {
                DataHolder.getInstance().weatherFlag = 0;
            }
            if (newsCheckbox.isChecked()) {
                DataHolder.getInstance().newsFlag = 1;
            }
            if (!newsCheckbox.isChecked()) {
                DataHolder.getInstance().newsFlag = 0;
            }

            launchAdvertWatchingActivity(orient, context);
        } else {
            Toast.makeText(context, "Please select orientation and screen id", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Helper method to launch AdvertWatching or AdvertLandWatch activity with animation
     */
    private void launchAdvertWatchingActivity(String orient, Context context) {
        waitingLogo.animate()
                .scaleX(2.2f).scaleY(2.2f).alpha(0f)
                .setDuration(1000)
                .setInterpolator(new DecelerateInterpolator())
                .withEndAction(() -> {
                    android.util.Log.i("SelectScreens", " Launching AdvertWatching - orientation: " + orient);
                    if (orient.equalsIgnoreCase("forced portrait")) {
                        startActivity(new Intent(context, AdvertLandWatch.class));
                    } else if (orient.equalsIgnoreCase("split screen")) {
                        startActivity(new Intent(context, AdvertSplitScreen.class));
                    } else {
                        startActivity(new Intent(context, AdvertWatching.class));
                    }
                }).start();
    }

    /**
     * Appends any cached Cloud Slideshow images to the ad rotation. A pure, synchronous,
     * in-memory operation (see {@link com.adjaba.utilities.SlideshowManager#getCachedSlideshowMediaModels})
     * — safe to call from any thread, including the main thread, and a no-op when the feature
     * is disabled or unconfigured, so it never disturbs the normal ad flow.
     */
    private List<MediaModel> withSlideshowImages(List<MediaModel> ads) {
        List<MediaModel> slideshowImages = com.adjaba.utilities.SlideshowManager.getCachedSlideshowMediaModels(context);
        if (slideshowImages.isEmpty()) return ads;
        return com.adjaba.utilities.SlideshowManager.interleave(ads, slideshowImages);
    }

    /** Builds a MediaModel from a stored AdEntity, including streamType/socialPlatform/socialHashtag. */
    private MediaModel mediaModelFromAdEntity(AdEntity ada) {
        MediaModel m = new MediaModel(ada.contractId, ada.currency, ada.maxBid, ada.format, ada.localPath,
                ada.duration, ada.textBottom, ada.textTop, "", ada.targetHours, ada.advertId,
                ada.targetGender, ada.targetAgeGroup, ada.targetTags, ada.targetEmotion);
        m.setTextLeft(ada.textLeft);
        m.setTextRight(ada.textRight);
        copySocialAndStreamFields(ada, m);
        return m;
    }

    /** Copies the v1.1.0 content-type fields (streamType/socialPlatform/socialHashtag) from DB to model. */
    private void copySocialAndStreamFields(AdEntity ada, MediaModel m) {
        if (ada.streamType != null)     m.setStreamType(ada.streamType);
        if (ada.socialPlatform != null) m.setSocialPlatform(ada.socialPlatform);
        if (ada.socialHashtag != null)  m.setSocialHashtag(ada.socialHashtag);
    }

    private void getUrl(String contractId, String currency, int maxBid, List<Integer> targetHours, String txtTop, String txtRight, String txtLeft, String info, String advertId, String screenId, String path, String type, int[] loadedCount, int totalCount, int duration, Context context, int flag, int serverOrder, List<String> targetGender, List<String> targetAgeGroup, List<String> targetTags, List<String> targetEmotion, String streamType, String socialPlatform, String socialHashtag) {
        // LIVE_STREAM / WEB_CONTENT / SOCIAL_FEED have no downloadable file — SOCIAL_FEED has no
        // path at all, the others carry their URL in `path`. Persist them directly, no download.
        String normalizedType = type != null ? type.toUpperCase() : "";
        boolean isStreamingType = "SOCIAL_FEED".equals(normalizedType)
                || "WEB_CONTENT".equals(normalizedType)
                || "LIVE_STREAM".equals(normalizedType);

        if (isStreamingType) {
            String contentUrl = "SOCIAL_FEED".equals(normalizedType) ? "" : (path != null ? path : "");
            if (!"SOCIAL_FEED".equals(normalizedType) && contentUrl.isEmpty()) {
                android.util.Log.w("SelectScreens", "⚠️ " + normalizedType + " ad " + advertId + " has no URL - skipping");
                loadedCount[0]++;
                checkAndLaunchAdvertWatchingIfAllProcessed(loadedCount[0], totalCount, screenId, contractId, maxBid, orient, context);
                return;
            }

            Executors.newSingleThreadExecutor().execute(() -> {
                AdEntity ad = new AdEntity(
                        advertId, normalizedType, contentUrl,
                        txtTop, info, txtLeft, txtRight,
                        duration * 1000, "Landscape", screenId,
                        contractId, listToString(targetHours), serverOrder, currency, maxBid,
                        listStrToString(targetGender), listStrToString(targetAgeGroup),
                        listStrToString(targetTags), listStrToString(targetEmotion),
                        streamType, socialPlatform, socialHashtag
                );
                AdDatabase db = AdDatabase.getInstance(context);
                db.adDao().insertAd(ad);
                android.util.Log.d("SelectScreens", "   Inserted " + normalizedType + " ad " + advertId + " to Room DB");

                loadedCount[0]++;
                targetHoursList.add(new TargetHours(advertId, targetHours));
                updateDownloadProgress(loadedCount[0], totalCount);
                checkAndLaunchAdvertWatchingIfAllProcessed(loadedCount[0], totalCount, screenId, contractId, maxBid, orient, context);
            });
            return;
        }

        if (path == null || path.isEmpty()) {
            android.util.Log.w("SelectScreens", "⚠️ Ad " + advertId + " has empty path - skipping");
            loadedCount[0]++;
            checkAndLaunchAdvertWatchingIfAllProcessed(loadedCount[0], totalCount, screenId, contractId, maxBid, orient, context);
            return;
        }

        // /media/{path} streams the file directly (no JSON wrapper) — download with auth header
        String downloadUrl = Config.BASE_URL + "/media/" + path;
        String token = AuthManager.getToken(this);
        String extension;
        try {
            int dot = path.lastIndexOf('.');
            extension = dot >= 0 ? path.substring(dot + 1) : "mp4";
        } catch (Exception e) {
            extension = "mp4";
        }
        String fileName = UUID.randomUUID().toString() + "." + extension;
        android.util.Log.d("SelectScreens", " Downloading ad " + advertId + " → " + downloadUrl);

        Executors.newSingleThreadExecutor().execute(() -> {
            String localPath = downloadFileWithAuth(context, downloadUrl, fileName, token);
            if (localPath != null) {
                android.util.Log.d("SelectScreens", "   Saved ad " + advertId + " locally: " + fileName);
                if (isImage(path)) {
                    mediaFormat = "IMAGE";
                } else if (isVideo(path)) {
                    mediaFormat = "VIDEO";
                } else {
                    mediaFormat = "IMAGE";
                }
                AdEntity ad = new AdEntity(
                        advertId,
                        mediaFormat,
                        localPath,
                        txtTop, info, txtLeft, txtRight,
                        duration * 1000,
                        "Landscape",
                        screenId,
                        contractId, listToString(targetHours), serverOrder, currency, maxBid,
                        listStrToString(targetGender),
                        listStrToString(targetAgeGroup),
                        listStrToString(targetTags),
                        listStrToString(targetEmotion),
                        null, null, null
                );
                AdDatabase db = AdDatabase.getInstance(context);
                db.adDao().insertAd(ad);
                android.util.Log.d("SelectScreens", "   Inserted ad " + advertId + " to Room DB");

                loadedCount[0]++;
                targetHoursList.add(new TargetHours(advertId, targetHours));
                android.util.Log.i("SelectScreens", " Progress: " + loadedCount[0] + "/" + totalCount + " ads loaded");
                updateDownloadProgress(loadedCount[0], totalCount);
                checkAndLaunchAdvertWatchingIfAllProcessed(loadedCount[0], totalCount, screenId, contractId, maxBid, orient, context);
            } else {
                android.util.Log.e("SelectScreens", "❌ Failed to download ad " + advertId);
                loadedCount[0]++;
                updateDownloadProgress(loadedCount[0], totalCount);
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
                android.util.Log.d("SelectScreens", " UI Progress: " + loaded + "/" + total);
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
                            MediaModel m = new MediaModel(contractId, "", maxBid, ada.format, ada.localPath, ada.duration, ada.textBottom, ada.textTop, "", ada.targetHours, ada.advertId, ada.targetGender, ada.targetAgeGroup, ada.targetTags, ada.targetEmotion);
                            m.setTextLeft(ada.textLeft);
                            m.setTextRight(ada.textRight);
                            copySocialAndStreamFields(ada, m);
                            mediaModels.add(m);
                        }
                    }
                }
                android.util.Log.i("SelectScreens", "   Created MediaModel list with " + mediaModels.size() + " items");

                // Post UI updates back to main thread
                new Handler(Looper.getMainLooper()).post(() -> {
                    DataHolder.getInstance().targetHours = targetHoursList;
                    DataHolder.getInstance().allAds = withSlideshowImages(mediaModels);
                    android.util.Log.i("SelectScreens", "   ✅ Updated DataHolder.allAds with " + mediaModels.size() + " MediaModels");

                    waitingLogo.animate()
                            .scaleX(2.2f)
                            .scaleY(2.2f)
                            .alpha(0f)
                            .setDuration(1000)
                            .setInterpolator(new DecelerateInterpolator())
                            .withEndAction(() -> {
                                android.util.Log.i("SelectScreens", " Launching AdvertWatching with " + mediaModels.size() + " ads");
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

    /** Convert a List<String> to a comma-separated string for DB storage. */
    public String listStrToString(List<String> list) {
        if (list == null || list.isEmpty()) return "";
        return android.text.TextUtils.join(",", list);
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

                        String rawId = response.body().get(screen).getScreenId();
                        String rawName = response.body().get(screen).screenName;
                        String label = (rawName != null && !rawName.isEmpty()) ? rawName + " — " + rawId : rawId;
                        screenOptions.add(label);
                        screenIdForDisplay.add(rawId);
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
                if (screenIdForDisplay.size() <= 1) addSavedScreenFallback();
            }

            @Override
            public void onFailure(Call<List<Root>> call, Throwable t) {
                onFinish.run();
                // Offline: screen list is unreachable — offer the last-played screen
                // so cached ads can still be started manually.
                addSavedScreenFallback();
            }
        });
        return screenOptions;
    }

    /**
     * Adds the last-played screen to the spinner when the backend screen list is
     * unavailable (offline), and pre-selects it. Runs after onFinish so its
     * selection wins over the remembered spinner position.
     */
    private void addSavedScreenFallback() {
        String savedId = prefs.getString("resume_screen_id", null);
        if (savedId == null || screenIdForDisplay.contains(savedId)) return;
        screenOptions.add(savedId + " (offline)");
        screenIdForDisplay.add(savedId);
        spinnerAdapter.notifyDataSetChanged();
        spinnerID.setSelection(screenIdForDisplay.indexOf(savedId));
    }

    public static String downloadFileWithAuth(Context context, String fileUrl, String fileName, String token) {
        OkHttpClient client = new OkHttpClient();
        Request.Builder builder = new Request.Builder().url(fileUrl);
        if (token != null) {
            builder.addHeader("Authorization", "Bearer " + token);
        }
        try (okhttp3.Response response = client.newCall(builder.build()).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                InputStream inputStream = response.body().byteStream();
                File file = new File(context.getFilesDir(), fileName);
                FileOutputStream outputStream = new FileOutputStream(file);
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                outputStream.close();
                inputStream.close();
                android.util.Log.d("SelectScreens", "  ✅ Downloaded " + file.length() + " bytes");
                return file.getAbsolutePath();
            } else {
                android.util.Log.e("SelectScreens", "  ❌ HTTP " + response.code() + " downloading " + fileUrl);
            }
        } catch (IOException e) {
            android.util.Log.e("SelectScreens", "  ❌ Download error: " + e.getMessage());
        }
        return null;
    }

    public static String downloadFileToInternalStorage(Context context, String fileUrl, String fileName) {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(fileUrl)
                .build();

        try (okhttp3.Response response = client.newCall(request).execute()) {
            android.util.Log.d("SelectScreens", "    HTTP Response code: " + response.code() + " for URL: " + fileUrl);

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
            android.util.Log.e("SelectScreens", "    IOException downloading " + fileUrl);
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
        weatherCheckbox = findViewById(R.id.weather_checkbox);
        newsCheckbox = findViewById(R.id.news_checkbox);
        iotCheckbox = findViewById(R.id.iot_checkbox);
        slideshowCheckbox = findViewById(R.id.slideshow_checkbox);
        slideshowConfigContainer = findViewById(R.id.slideshowConfigContainer);
        slideshowFolderUrlInput = findViewById(R.id.slideshow_folder_url);
        slideshowIntervalInput = findViewById(R.id.slideshow_interval);
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
