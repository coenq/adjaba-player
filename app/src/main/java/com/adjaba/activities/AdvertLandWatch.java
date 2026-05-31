package com.adjaba.activities;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.adjaba.R;
import com.adjaba.activities.viewmodel.APIImpression;
import com.adjaba.activities.viewmodel.DataHolder;
import com.adjaba.models.DemographicData;
import com.adjaba.models.newmodels.Forecastday;
import com.adjaba.models.newmodels.Hour;
import com.adjaba.models.newmodels.MediaModel;
import com.adjaba.models.newmodels.VideoImageModel;
import com.adjaba.models.newmodels.WatchingModel;
import com.adjaba.models.newmodels.WeatherModel;
import com.adjaba.news.NewsHandler;
import com.adjaba.news.RssItem;
import com.adjaba.news.Utils;
import com.adjaba.others.TargetHours;
import com.adjaba.room.AdDatabase;
import com.adjaba.room.AdEntity;
import com.adjaba.room.ImpressionEntity;
import com.adjaba.utilities.AuthManager;
import com.adjaba.utilities.Config;
import com.adjaba.utilities.MqttManager;
import com.adjaba.utilities.PlaylistSyncManager;
import com.adjaba.utilities.RetrofitBuilder;
import com.bumptech.glide.Glide;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;
import kotlin.Unit;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdvertLandWatch extends AppCompatActivity {
    private List<WatchingModel> adList = new ArrayList<>();
    List<MediaModel> mediaList = new ArrayList<>();
    int[] loadedCount = {0};
    LinearLayout weatherLayout, newsLayout;
    int weatherCurrent;
    private int currentIndex = 0;
    private ExoPlayer exoPlayer;
    private Player.Listener playerListener = null;
    private Handler handler = new Handler();
    ImageView logoImage;
    Map<String, List<Integer>> advertHoursMap; // المفتاح advertId، والقيمة الساعات اللي يتعرض فيها الإعلان

    private RetrofitBuilder retrofitBuilder = new RetrofitBuilder();
    private ImageView adImageView, icon1, icon2, icon3;
    private ImageView weatherImg;
    TextView tvTemp, tvLoc, tvStatus, timeNow, dateNow, wind, rain, humadity, progressText;
    private Runnable mediaSwitcher;
    private PlayerView adPlayerView;
    ConstraintLayout constLayout;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    ProgressBar progressBar;
    Context context;
    String screenLoc;
    String screenId;
    int refreshTime = 0;
    String location;
    int newTime = 2;
    private Handler timeHandler = new Handler();
    private Runnable timeRunnable;
    int qrImageDimension;
    ImageView qrImage;
    private Runnable refreshRunnable;
    // ── 15-minute silent refresh ──────────────────────────────────────────────
    private static final long WEATHER_REFRESH_INTERVAL_MS = 15 * 60 * 1000L;
    private static final long NEWS_REFRESH_INTERVAL_MS    = 15 * 60 * 1000L;
    private final Handler weatherRefreshHandler = new Handler(Looper.getMainLooper());
    private final Handler newsRefreshHandler    = new Handler(Looper.getMainLooper());
    private Runnable weatherRefreshRunnable;
    private Runnable newsRefreshRunnable;
    // ── Playlist sync observer ───────────────────────────────────────────
    private Observer<PlaylistSyncManager.PlaylistUpdate> playlistUpdateObserver;
    // ─────────────────────────────────────────────────────────────────────────
    String mediaFormat = "";
    TextView displayText, newsTitle;
    String orient;
    List<RssItem> getNews;
    int newsIndex = 0;
    NewsHandler newsHandler;
    TextView newsHeader, newsDesc, newsSourceF;
    ShimmerFrameLayout shimmer;
    ImageView waitingLogo, newsImg;


    @SuppressLint({"MissingInflatedId", "UnsafeOptInUsageError"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advert_land_watch);
        getNews = new ArrayList<>();
        newsImg = findViewById(R.id.news_img);
        constLayout = findViewById(R.id.mainConstLayout);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        boolean isDataLoaded = prefs.getBoolean("data_loaded", false);
        orient = DataHolder.getInstance().orient.toLowerCase();
        timeNow = findViewById(R.id.timeNow);
        dateNow = findViewById(R.id.dateNow);
        qrImage = findViewById(R.id.qrCodeImage);
        newsTitle = findViewById(R.id.newsTitle);
        logoImage = findViewById(R.id.logoImage);
        newsHeader = findViewById(R.id.main_headerF);
        newsDesc = findViewById(R.id.news_detailsF);
        newsSourceF = findViewById(R.id.newsSourceF);
        adImageView = findViewById(R.id.adImageView);
        shimmer = findViewById(R.id.shimmer);
        newsLayout = findViewById(R.id.newsLayout);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);


        prefs.edit().remove("data_loaded").apply();

        prefs.edit().remove("data_loaded").apply();


        // --- على مستوى الكلاس (fields) ---
        displayText = findViewById(R.id.displayText);
        context = this;
        advertHoursMap = new HashMap<>();
        rain = findViewById(R.id.rain);
        wind = findViewById(R.id.windW);
        humadity = findViewById(R.id.hamudity);
        progressBar = findViewById(R.id.loadBar);
        progressText = findViewById(R.id.progressText);
        waitingLogo = findViewById(R.id.waitingLogo);
        weatherCurrent = 3;
        weatherLayout = findViewById(R.id.weatherLayout);
        weatherImg = findViewById(R.id.currentWeatherImg);
        tvLoc = findViewById(R.id.weatherLoc);
        tvStatus = findViewById(R.id.currentStatus);
        tvTemp = findViewById(R.id.weatherTemp);
        adPlayerView = findViewById(R.id.adPlayerView);
        screenId = DataHolder.getInstance().screenID;
        location = DataHolder.getInstance().location;
        // Set location label immediately so it shows the correct city even before the weather API responds
        if (tvLoc != null && location != null && !location.isEmpty()) {
            tvLoc.setText(location);
        }
        qrImageDimension = qrCodeImageDimension();


        logoImage.setOnClickListener(new View.OnClickListener() {
            private static final long DOUBLE_CLICK_TIME_DELTA = 300; // 300ms
            long lastClickTime = 0;

            @Override
            public void onClick(View v) {
                long clickTime = System.currentTimeMillis();
                if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {
                    finish();
                }
                lastClickTime = clickTime;
            }
        });
        shimmer.startShimmer();
        View qr = findViewById(R.id.qrCodeImage);
// جلب مقاسات الشاشة
        boolean isTV = getPackageManager().hasSystemFeature("android.software.leanback");

// النسب المناسبة
        int percent = 12;  // 30% TV – 20% Mobile/Tablet

        ConstraintLayout.LayoutParams params =
                (ConstraintLayout.LayoutParams) qr.getLayoutParams();

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

// هنستخدم العرض فقط لأن الصورة مربعة
        int size = (dm.widthPixels * percent) / 100;

        params.width = size;
        params.height = size; // مربع

        qr.setLayoutParams(params);
        // Ensure QR is hidden until an ad slide explicitly shows it
        qrImage.setVisibility(View.GONE);

        refreshTime = Integer.parseInt(DataHolder.getInstance().time);
        handler = new Handler(Looper.getMainLooper());

        refreshRunnable = new Runnable() {
            @Override
            public void run() {
                getWeather(location, context);
                handler.postDelayed(this, (long) newTime * 60 * 1000);
            }
        };


        if (refreshTime == 0) {
            newTime = 1;
        } else if (refreshTime == 1) {
            newTime = 5;
        } else if (refreshTime == 2) {
            newTime = 30;
        } else if (refreshTime == 3) {
            newTime = 60;
        } else if (refreshTime == 4) {
            newTime = 100;
        }
        startLiveClock(timeNow);
        List<MediaModel> mediaModels = new ArrayList<>();
        screenLoc = location;
        if (!isDataLoaded || orient.equals("portrait") || orient.equals("landscape") || orient.equals("forced portrait")) {
            newsHandler = new NewsHandler(0);
            newsHandler.load(DataHolder.getInstance().location, context, (rss, i) -> {
                getNews = new ArrayList<>(rss);
                newsIndex = 0;
                return Unit.INSTANCE;
            }, bar -> {
                if (bar == 1) shimmer.stopShimmer();
                shimmer.setVisibility(View.GONE);
                return Unit.INSTANCE;
            });

            if (DataHolder.getInstance().allAds == null || DataHolder.getInstance().allAds.isEmpty()) {
                getWeather(location, context);
                // No ads — cycle weather and news slides
                List<MediaModel> infoSlides = new ArrayList<>();
                infoSlides.add(new MediaModel("", "", 0, "weather", "", 10000, "", "", "", "", ""));
                infoSlides.add(new MediaModel("", "", 0, "news",    "", 10000, "", "", "", "", ""));
                startMediaRotation(infoSlides, context);
            } else {
                getWeather(location, context);
                startMediaRotation(insertWeatherEveryThreeAds(DataHolder.getInstance().allAds), context);
            }


            prefs.edit().putBoolean("data_loaded", true).apply();
            startWeatherAutoRefresh();
            startNewsAutoRefresh();
            registerPlaylistSyncReceiver();

            // Initialize MQTT for demographic-based ad switching
            initializeMqtt(prefs);
        }
    }

    private void startWeatherAutoRefresh() {
        weatherRefreshRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isFinishing() && !isDestroyed()) {
                    getWeather(location, context);
                }
                weatherRefreshHandler.postDelayed(this, WEATHER_REFRESH_INTERVAL_MS);
            }
        };
        weatherRefreshHandler.postDelayed(weatherRefreshRunnable, WEATHER_REFRESH_INTERVAL_MS);
    }

    private void startNewsAutoRefresh() {
        newsRefreshRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isFinishing() && !isDestroyed()) {
                    Utils.INSTANCE.getNewsList().clear(); // force fresh network fetch
                    newsHandler = new NewsHandler(0);
                    newsHandler.load(DataHolder.getInstance().location, context, (rss, i) -> {
                        getNews = new ArrayList<>(rss);
                        newsIndex = 0;
                        return Unit.INSTANCE;
                    }, bar -> Unit.INSTANCE);
                }
                newsRefreshHandler.postDelayed(this, NEWS_REFRESH_INTERVAL_MS);
            }
        };
        newsRefreshHandler.postDelayed(newsRefreshRunnable, NEWS_REFRESH_INTERVAL_MS);
    }

    /**
     * Register LiveData observer to listen for playlist sync updates from AdSyncWorker.
     * When new ads are synced, reload the playlist from local database and update rotation.
     */
    private void registerPlaylistSyncReceiver() {
        playlistUpdateObserver = playlistUpdate -> {
            if (playlistUpdate == null) return;

            String updatedScreenId = playlistUpdate.screenId;
            int adsCount = playlistUpdate.adsCount;

            android.util.Log.i("AdvertLandWatch", "📡 Playlist sync update received - screenId: " + updatedScreenId + ", ads: " + adsCount);

            // Only reload if this is the current screen
            if (screenId != null && screenId.equals(updatedScreenId)) {
                android.util.Log.i("AdvertLandWatch", "   🔄 Reloading playlist from local database...");
                reloadPlaylistFromDatabase();
            }
        };

        PlaylistSyncManager.getInstance().getPlaylistUpdateLiveData().observe(this, playlistUpdateObserver);
        android.util.Log.i("AdvertLandWatch", "✅ Registered playlist sync observer (LiveData)");
    }

    /**
     * Reload ad playlist from local database and rebuild rotation.
     * Called when AdSyncWorker notifies of playlist changes.
     */
    private void reloadPlaylistFromDatabase() {
        Executors.newSingleThreadExecutor().execute(() -> {
            AdDatabase db = AdDatabase.getInstance(context);
            List<AdEntity> adEntities = db.adDao().getAllAds(screenId);

            if (adEntities == null || adEntities.isEmpty()) {
                android.util.Log.w("AdvertLandWatch", "⚠️ No ads in database after sync");
                return;
            }

            android.util.Log.i("AdvertLandWatch", " Loaded " + adEntities.size() + " ads from database");

            // Build MediaModel list from database
            List<MediaModel> updatedAds = new ArrayList<>();
            for (AdEntity ad : adEntities) {
                if (ad.localPath != null) {
                    updatedAds.add(new MediaModel(
                            ad.contractId, ad.currency, ad.maxBid, ad.format,
                            ad.localPath, ad.duration, ad.textBottom, ad.textTop,
                            "", ad.targetHours, ad.advertId
                    ));
                }
            }

            // Update DataHolder and rebuild rotation on main thread
            new Handler(Looper.getMainLooper()).post(() -> {
                DataHolder.getInstance().allAds = updatedAds;
                android.util.Log.i("AdvertLandWatch", "✅ Updated DataHolder.allAds with " + updatedAds.size() + " ads");

                // Rebuild rotation list
                List<MediaModel> newRotation = insertWeatherEveryThreeAds(updatedAds);
                android.util.Log.i("AdvertLandWatch", "   New rotation has " + (newRotation == null ? 0 : newRotation.size()) + " items");

                // Update mediaList for playback
                // Note: Current playback continues, new ads will appear in next cycle
                mediaList.clear();
                if (newRotation != null) {
                    mediaList.addAll(newRotation);
                }

                Toast.makeText(context, "Playlist updated: " + updatedAds.size() + " ads", Toast.LENGTH_SHORT).show();
            });
        });
    }

    /**
     * Initialize MQTT connection for demographic-based ad switching.
     * Connects to broker and subscribes to store/{screenId} topic.
     */
    private void initializeMqtt(SharedPreferences prefs) {
        // Check if IOT is enabled in settings
        boolean iotEnabled = prefs.getBoolean("iot_enabled", false);

        if (!iotEnabled) {
            android.util.Log.i("AdvertLandWatch", " IOT (MQTT) disabled in settings");
            return;
        }

        if (screenId == null || screenId.isEmpty()) {
            android.util.Log.w("AdvertLandWatch", "⚠️ Cannot start MQTT: screenId is null");
            return;
        }

        android.util.Log.i("AdvertLandWatch", " Initializing MQTT demographics for screen: " + screenId);

        MqttManager.getInstance().connect(this, screenId, new MqttManager.OnDemographicDataListener() {
            @Override
            public void onDemographicDataReceived(DemographicData data) {
                android.util.Log.d("AdvertLandWatch", " Demographic data received: " + data.toString());

                // Select best ad based on demographics
                MediaModel selectedAd = selectBestAdForDemographic(data);

                if (selectedAd != null) {
                    android.util.Log.i("AdvertLandWatch", "✅ Selected ad based on demographics: " + selectedAd.getAdvertId());
                    android.util.Log.i("AdvertLandWatch", "   Age: " + data.getAgeRange() + ", Gender: " + data.getGender());
                    android.util.Log.i("AdvertLandWatch", "   Dominant emotion: " + data.getDominantEmotion());

                    // Schedule the selected ad to play next (queue it instead of interrupting current ad)
                    scheduleNextAd(selectedAd);
                } else {
                    android.util.Log.d("AdvertLandWatch", "   No matching ad found for demographics");
                }
            }

            @Override
            public void onConnected() {
                android.util.Log.i("AdvertLandWatch", "✅ Connected to MQTT broker - listening for demographics");
            }

            @Override
            public void onConnectionLost(Throwable cause) {
                android.util.Log.w("AdvertLandWatch", "⚠️ MQTT connection lost: " +
                    (cause != null ? cause.getMessage() : "unknown"));
            }
        });
    }

    /**
     * Select the best ad from current playlist based on demographic data.
     * Scores each ad by matching age group, gender, and target hours.
     *
     * @param data Demographic data from MQTT
     * @return Best matching MediaModel or null if no match
     */
    private MediaModel selectBestAdForDemographic(DemographicData data) {
        if (DataHolder.getInstance().allAds == null || DataHolder.getInstance().allAds.isEmpty()) {
            return null;
        }

        // Skip if no audience or invalid data
        if (data.getCustomerCount() <= 0) {
            android.util.Log.d("AdvertLandWatch", "⏭️ No audience detected, skipping demographic-based selection");
            return null;
        }

        int currentHour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY);
        int highestScore = 0;
        MediaModel bestAd = null;

        // Map MQTT gender ("M"/"F") → API gender ("MALE"/"FEMALE")
        String viewerGender = data.getGender();
        String viewerGenderApi = "M".equalsIgnoreCase(viewerGender) ? "MALE" : "FEMALE";
        // ageBracket from MQTT already matches API format, e.g. "32-42"
        String viewerAgeBracket = data.getAgeBracket();
        // IOT ad tags, e.g. ["M", "32-42", "neutral"]
        java.util.List<String> viewerTags = data.getAdTags();

        for (MediaModel ad : DataHolder.getInstance().allAds) {
            int score = 0;

            // Skip special content types
            if ("weather".equals(ad.getType()) || "news".equals(ad.getType())) {
                continue;
            }

            // ✅ HOUR MATCHING (+15 pts)
            List<Integer> targetHours = parseTargetHours(ad.getTargetHours());
            if (targetHours != null && !targetHours.isEmpty() && targetHours.contains(currentHour)) {
                score += 15;
                android.util.Log.d("AdvertLandWatch", "  ⏰ Hour match: " + currentHour);
            }

            // ✅ GENDER MATCHING: match MQTT gender against ad's targetGender list (+15 pts)
            String adGenderStr = ad.getTargetGender();
            if (viewerGender != null && !viewerGender.isEmpty()) {
                if (adGenderStr != null && !adGenderStr.isEmpty()) {
                    if (adGenderStr.toUpperCase().contains(viewerGenderApi)) {
                        score += 15;
                        android.util.Log.d("AdvertLandWatch", "   Gender match: " + viewerGenderApi);
                    }
                } else {
                    score += 5;
                    android.util.Log.d("AdvertLandWatch", "   Gender detected (no ad filter): " + viewerGender);
                }
            }

            // ✅ AGE BRACKET MATCHING: match MQTT ageBracket against ad's targetAgeGroup (+15 pts)
            String adAgeStr = ad.getTargetAgeGroup();
            if (viewerAgeBracket != null && !viewerAgeBracket.isEmpty()) {
                if (adAgeStr != null && !adAgeStr.isEmpty()) {
                    if (adAgeStr.contains(viewerAgeBracket)) {
                        score += 15;
                        android.util.Log.d("AdvertLandWatch", "   Age bracket match: " + viewerAgeBracket);
                    }
                } else {
                    String viewerAgeRange = data.getAgeRange();
                    if (viewerAgeRange != null) {
                        if (viewerAgeRange.contains("20, 32")) { score += 8; }
                        else if (viewerAgeRange.contains("32, 43")) { score += 8; }
                        else if (viewerAgeRange.contains("43")) { score += 5; }
                    }
                }
            }

            // ✅ TAG MATCHING: match IOT adTarget.tags against ad's targetTags (+5 pts per match)
            String adTagsStr = ad.getTargetTags();
            if (viewerTags != null && !viewerTags.isEmpty() && adTagsStr != null && !adTagsStr.isEmpty()) {
                for (String tag : viewerTags) {
                    if (adTagsStr.toLowerCase().contains(tag.toLowerCase())) {
                        score += 5;
                        android.util.Log.d("AdvertLandWatch", "  ️ Tag match: " + tag);
                    }
                }
            }

            // ✅ EMOTION MATCHING: Smart emotion-based targeting
            String adEmotionStr = ad.getTargetEmotion();
            String dominantEmotion = data.getDominantEmotion();
            int happyScore = data.getHappy();

            if (adEmotionStr != null && !adEmotionStr.isEmpty()) {
                // Ad HAS emotion targeting — only score if it matches viewer's emotion
                if (adEmotionStr.toLowerCase().contains(dominantEmotion.toLowerCase())) {
                    if ("happy".equals(dominantEmotion)) {
                        score += 25;
                        android.util.Log.d("AdvertLandWatch", "   Happy emotion MATCH: Promo/upbeat ad");
                    } else if ("neutral".equals(dominantEmotion)) {
                        score += 15;
                        android.util.Log.d("AdvertLandWatch", "   Neutral emotion MATCH: Brand/info ad");
                    } else if ("sad".equals(dominantEmotion) || "angry".equals(dominantEmotion)) {
                        score += 20;
                        android.util.Log.d("AdvertLandWatch", "   Negative emotion MATCH: Comfort/support ad");
                    } else if ("surprise".equals(dominantEmotion)) {
                        score += 18;
                        android.util.Log.d("AdvertLandWatch", "   Surprise emotion MATCH");
                    } else {
                        score += 10;
                        android.util.Log.d("AdvertLandWatch", "   Other emotion MATCH: " + dominantEmotion);
                    }
                } else {
                    android.util.Log.d("AdvertLandWatch", "  ⏭️ Emotion filter: ad wants " + adEmotionStr + ", viewer is " + dominantEmotion);
                }
            } else {
                // Ad has NO emotion filter — give baseline emotion bonus to all ads
                if ("happy".equals(dominantEmotion)) {
                    score += 8;
                    android.util.Log.d("AdvertLandWatch", "   Happy viewer: Baseline bonus");
                } else if ("neutral".equals(dominantEmotion)) {
                    score += 5;
                    android.util.Log.d("AdvertLandWatch", "   Neutral viewer: Baseline bonus");
                } else {
                    score += 3;
                    android.util.Log.d("AdvertLandWatch", "   " + dominantEmotion + " viewer: Baseline bonus");
                }
            }

            // Bonus if happiness score is very high (> 60%)
            if (happyScore > 60 && (adEmotionStr == null || adEmotionStr.toLowerCase().contains("happy"))) {
                score += 15;
                android.util.Log.d("AdvertLandWatch", "  ⭐ Very high happiness threshold met");
            }

            // ✅ ENGAGEMENT TIME MATCHING (+8 if long dwell)
            int avgDwellSec = data.getAvgDwellSec();
            int adDurationSec = ad.getDurationInMillis() / 1000;

            if (avgDwellSec > 15 && adDurationSec > 10) {
                score += 8;
                android.util.Log.d("AdvertLandWatch", "  ⏳ High engagement time (" + avgDwellSec + "s): Select longer ads");
            } else if (avgDwellSec <= 5 && adDurationSec <= 5) {
                score += 5;
                android.util.Log.d("AdvertLandWatch", "  ⚡ Quick engagement: Select short ads");
            }

            // ✅ AUDIENCE SIZE BONUS (+5 pts for group of 3+)
            int audienceCount = data.getCustomerCount();
            if (audienceCount >= 3) {
                score += 5;
                android.util.Log.d("AdvertLandWatch", "   Multi-person audience: Select group-appeal ads");
            }

            android.util.Log.d("AdvertLandWatch", "   Ad " + ad.getAdvertId()
                    + " [gender=" + adGenderStr + " age=" + adAgeStr + " tags=" + adTagsStr + " emotion=" + adEmotionStr + "] score: " + score);

            if (score > highestScore) {
                highestScore = score;
                bestAd = ad;
            }
        }

        if (bestAd != null) {
            android.util.Log.i("AdvertLandWatch", " DEMOGRAPHIC MATCH WINNER: " + bestAd.getAdvertId() + " (score: " + highestScore + ")");
            android.util.Log.i("AdvertLandWatch", "   Viewer: " + viewerGender + " " + viewerAgeBracket
                    + " | Emotion: " + data.getDominantEmotion() + " | Audience: " + data.getCustomerCount()
                    + " | Tags: " + viewerTags);
        } else {
            android.util.Log.d("AdvertLandWatch", "⏭️ No suitable ad found for demographics");
        }

        return bestAd;
    }

    /**
     * Parse target hours from slash-separated string.
     * @param targetHoursStr String like "9/10/11/14/15/16" (stored by SelectScreens.listToString)
     * @return List of Integer hours or empty list
     */
    private List<Integer> parseTargetHours(String targetHoursStr) {
        List<Integer> hours = new ArrayList<>();
        if (targetHoursStr == null || targetHoursStr.isEmpty()) {
            return hours;
        }
        try {
            // targetHours are stored with "/" separator (see SelectScreens.listToString)
            String[] parts = targetHoursStr.split("/");
            for (String part : parts) {
                hours.add(Integer.parseInt(part.trim()));
            }
        } catch (NumberFormatException e) {
            android.util.Log.w("AdvertLandWatch", "Failed to parse target hours: " + targetHoursStr);
        }
        return hours;
    }

    /**
     * Schedule the selected ad to play next in the rotation.
     * Inserts the ad at currentIndex+1 so it plays after the current slot finishes.
     * Must be called on the UI thread (or posts to it).
     *
     * @param ad The ad to schedule
     */
    private void scheduleNextAd(MediaModel ad) {
        // The MQTT callback fires on a background thread — post to UI thread
        runOnUiThread(() -> {
            if (mediaList == null || mediaList.isEmpty()) {
                android.util.Log.w("AdvertLandWatch", " Cannot schedule ad — mediaList is empty");
                return;
            }

            // Remove any previous occurrence to avoid duplicates
            mediaList.remove(ad);

            // Insert immediately after the currently-playing slot
            int insertAt = Math.min(currentIndex + 1, mediaList.size());
            mediaList.add(insertAt, ad);

            android.util.Log.i("AdvertLandWatch", " Queued demographic ad at position "
                    + insertAt + "/" + mediaList.size() + ": " + ad.getAdvertId());
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Disconnect MQTT
        MqttManager.getInstance().disconnect();
        android.util.Log.i("AdvertLandWatch", "✅ Disconnected from MQTT broker");

        // LiveData observer automatically unregistered on lifecycle destroy
        android.util.Log.i("AdvertLandWatch", "✅ Playlist sync observer auto-cleanup (LiveData)");

        // Stop auto-refresh handlers
        if (weatherRefreshHandler != null && weatherRefreshRunnable != null) {
            weatherRefreshHandler.removeCallbacks(weatherRefreshRunnable);
        }
        if (newsRefreshHandler != null && newsRefreshRunnable != null) {
            newsRefreshHandler.removeCallbacks(newsRefreshRunnable);
        }
        if (handler != null && refreshRunnable != null) {
            handler.removeCallbacks(refreshRunnable);
        }

        // Cleanup media and handlers
        if (handler != null && mediaSwitcher != null) {
            handler.removeCallbacks(mediaSwitcher);
        }
        releaseExoPlayer();
        if (executorService != null) {
            executorService.shutdownNow();
        }
        stopLiveClock();
    }

    void getAds(int flag) {
        retrofitBuilder.apiCalls().getAdsByScreen(screenId, "Bearer " + AuthManager.getToken(this)).enqueue(new Callback<List<WatchingModel>>() {
            @Override
            public void onResponse(Call<List<WatchingModel>> call, Response<List<WatchingModel>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    android.util.Log.e("AdvertLandWatch", "❌ getAds API error: " + response.code());
                    return;
                }
                adList = response.body();
                if (adList.isEmpty()) {
                    return;
                }

                if (executorService == null || executorService.isShutdown()) {
                    executorService = Executors.newSingleThreadExecutor();
                }
                AtomicInteger remainingAds = new AtomicInteger(adList.size());
                TargetHours targetHours;
                List<TargetHours> targetHoursList = new ArrayList<>();
                for (int i = 0; i < adList.size(); i++) {
                    if (!DataHolder.getInstance().advertIds.contains(response.body().get(i).adContractData.advertId)) {
                        String format = adList.get(i).adContractData.format.toLowerCase();
                        String videoUrl = adList.get(i).adContractData.videoUrl;
                        int duration = adList.get(i).duration;
                        //SelectScreens.showDownloadDialog(context, adList.size());
                        targetHours = new TargetHours(response.body().get(i).adContractData.advertId, response.body().get(i).adContractData.targetHours);
                        targetHoursList.add(targetHours);
                        getUrl(response.body().get(i).contractId, response.body().get(i).currency, response.body().get(i).maxBid, i, listToString(response.body().get(i).adContractData.targetHours),
                                response.body().get(i).adContractData.textTop,
                                response.body().get(i).adContractData.textRight,
                                response.body().get(i).adContractData.textLeft,
                                response.body().get(i).adContractData.textBottom,
                                response.body().get(i).adContractData.advertId,
                                response.body().get(i).screenId,
                                videoUrl,
                                format,
                                loadedCount,
                                adList.size(),
                                duration,
                                context,
                                flag,
                                () -> { // ده كول باك بيتنفذ لما الإعلان يخلص التحميل والحفظ
                                    int remaining = remainingAds.decrementAndGet();  // نقص مرة واحدة بس
                                    int loaded = adList.size() - remaining;
                                    int percent = (loaded * 100) / adList.size();
                                    /*new Handler(Looper.getMainLooper()).post(() -> {
                                        SelectScreens.updateDownloadDialogProgress(percent, loaded, adList.size());
                                    });*/
                                    if (remaining == 0) {
                                       /* new Handler(Looper.getMainLooper()).post(() -> {
                                            SelectScreens.dismissDownloadDialog();
                                        });*/
                                        Executors.newSingleThreadExecutor().execute(() -> {
                                            AdDatabase adDatabase = AdDatabase.getInstance(context);
                                            List<AdEntity> adEntities = adDatabase.adDao().getAll();
                                            List<MediaModel> mediaModels = new ArrayList<>();
                                            for (AdEntity ad : adEntities) {
                                                if (ad.localPath != null) {
                                                    mediaModels.add(new MediaModel(ad.contractId, ad.currency, ad.maxBid, ad.format, ad.localPath, ad.duration, ad.textBottom, ad.textTop, "", ad.targetHours, ad.advertId));
                                                }
                                            }
                                            new Handler(Looper.getMainLooper()).post(() -> {
                                                getWeather(location, context);
                                                startMediaRotation(insertWeatherEveryThreeAds(mediaModels), context);
                                            });
                                        });
                                    }
                                }
                        );
                    }
                }

            }

            @Override
            public void onFailure(Call<List<WatchingModel>> call, Throwable t) {
            }
        });
    }

    private void getUrl(String contractId, String currency, int maxBid, int serverOrder, String targetHours, String txtTop, String txtRight, String txtLeft, String info, String advertId, String screenId, String path, String type, int[] loadedCount, int totalCount, int duration, Context context, int flag, Runnable onComplete) {
        if (path == null || path.isEmpty()) {
            if (onComplete != null) new Handler(Looper.getMainLooper()).post(onComplete);
            return;
        }
        // /media/{path} streams the file directly — download with auth header
        String downloadUrl = com.adjaba.utilities.Config.BASE_URL + "/media/" + path;
        String token = AuthManager.getToken(this);
        String extension;
        try {
            int dot = path.lastIndexOf('.');
            extension = dot >= 0 ? path.substring(dot + 1) : "mp4";
        } catch (Exception e) {
            extension = "mp4";
        }
        String fileName = UUID.randomUUID().toString() + "." + extension;

        Executors.newSingleThreadExecutor().execute(() -> {
            String localPath = downloadFileToInternalStorage(context, downloadUrl, fileName, token);
            if (localPath != null) {
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
                        contractId, targetHours, serverOrder, currency, maxBid,
                        null, null, null, null  // targetGender/AgeGroup/Tags/Emotion not available here
                );
                AdDatabase db = AdDatabase.getInstance(context);
                db.adDao().insertAd(ad);
            }
            if (onComplete != null) {
                new Handler(Looper.getMainLooper()).post(onComplete);
            }
        });
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

    private List<MediaModel> insertWeatherEveryThreeAds(List<MediaModel> originalList) {
        List<MediaModel> newList = new ArrayList<>();
        if (originalList == null || originalList.isEmpty()) {
            progressBar.setVisibility(View.GONE);
            return newList;
        }
        int count = 0;
        for (MediaModel media : originalList) {
            newList.add(media);
            count++;
            boolean cycleComplete = (originalList.size() == 1) || (count == originalList.size());
            if (cycleComplete) {
                // After each full ad cycle: weather slide then news slide (if enabled)
                if (DataHolder.getInstance().weatherFlag == 1) {
                    newList.add(new MediaModel("", "", 0, "weather", "", 6000, "", "", "", "", ""));
                }
                if (DataHolder.getInstance().newsFlag == 1) {
                    newList.add(new MediaModel("", "", 0, "news", "", 6000, "", "", "", "", ""));
                }
                count = 0;
            }
        }
        progressBar.setVisibility(View.GONE);
        return newList;
    }

    void getWeather(String loc, Context context) {

        retrofitBuilder.apiCalls2().getWeather(Config.weatherKey, loc).enqueue(new Callback<WeatherModel>() {
            @Override
            public void onResponse(Call<WeatherModel> call, Response<WeatherModel> response) {
                if (response.isSuccessful() && response.body() != null
                        && response.body().current != null && response.body().current.condition != null) {
                    if (!isFinishing() && !isDestroyed()) {
                        String iconUrl = "https:" + response.body().current.condition.icon
                                .replace("/64x64/", "/128x128/");
                        Glide.with(AdvertLandWatch.this).load(iconUrl).into(weatherImg);
                    }
                    List<Hour> nextThreeHours = new ArrayList<>();
                    List<String> nextTimes = new ArrayList<>();

                    SimpleDateFormat fullFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                    SimpleDateFormat hourFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

                    Date currentTime = new Date();

                    for (Forecastday day : response.body().forecast.forecastday) {
                        for (Hour hour : day.hour) {
                            try {
                                Date hourTime = fullFormat.parse(hour.time);
                                if (hourTime != null && hourTime.after(currentTime)) {
                                    nextThreeHours.add(hour);
                                    nextTimes.add(hourFormat.format(hourTime));
                                }

                                if (nextThreeHours.size() == 3) break;

                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }

                        if (nextThreeHours.size() == 3) break;
                    }
                    humadity.setText(response.body().current.humidity + "%");
                    wind.setText(Math.round(response.body().current.wind_kph) + "");
                    rain.setText(Math.round(response.body().current.feelslike_c) + "°");
                    tvLoc.setText(DataHolder.getInstance().location);
                    tvTemp.setText(Math.round(response.body().current.temp_c) + "°C");
                    tvStatus.setText(response.body().current.condition.text);

                } else {
                    // Response came back but body/current is null — still update location label
                    if (tvLoc != null) {
                        tvLoc.setText(DataHolder.getInstance().location != null ? DataHolder.getInstance().location : "");
                    }
                }
            }

            @Override
            public void onFailure(Call<WeatherModel> call, Throwable t) {
                android.util.Log.e("AdvertLandWatch", "❌ Weather API failed: " + (t != null ? t.getMessage() : "unknown error"));
                // Set default/fallback weather values
                tvTemp.setText("N/A");
                tvStatus.setText("Weather unavailable");
                tvLoc.setText(DataHolder.getInstance().location != null ? DataHolder.getInstance().location : "Unknown");
                humadity.setText("--");
                wind.setText("--");
                rain.setText("--");
                android.util.Log.i("AdvertLandWatch", "✅ Set fallback weather values");
            }
        });
    }

    public List<Integer> stringToList(String str) {
        List<Integer> list = new ArrayList<>();
        if (str == null || str.isEmpty()) return list;

        String[] parts = str.split("/");
        for (String part : parts) {
            try {
                list.add(Integer.parseInt(part));
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return list;

    }

    private void startLiveClock(TextView timeTextView) {
        timeRunnable = new Runnable() {
            @Override
            public void run() {
                Date now = new Date();

                //  Get timezone for screen location
                String location = DataHolder.getInstance().location;
                TimeZone tz = getTimeZoneForLocation(location);

                String currentTime = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(now);
                SpannableString spannable = new SpannableString(currentTime);

                // Apply timezone if available
                if (tz != null) {
                    SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                    sdf.setTimeZone(tz);
                    currentTime = sdf.format(now);
                    spannable = new SpannableString(currentTime);
                }

                // ✨ Premium styling: Netflix red colon
                int colon = currentTime.indexOf(':');
                if (colon >= 0) {
                    spannable.setSpan(
                        new ForegroundColorSpan(0xFFE50914),
                        colon, colon + 1,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                //  Apply smooth time digit fade animation
                if (timeTextView != null) {
                    Animation timeFadeAnim = AnimationUtils.loadAnimation(context, R.anim.time_digit_fade);
                    if (timeFadeAnim != null) {
                        timeTextView.clearAnimation();
                        timeTextView.startAnimation(timeFadeAnim);
                    }
                }

                timeTextView.setText(spannable);

                if (dateNow != null) {
                    String dateText = new SimpleDateFormat("EEE, d MMM", Locale.getDefault()).format(now).toUpperCase(Locale.getDefault());

                    //  Apply smooth date update animation
                    Animation dateFadeAnim = AnimationUtils.loadAnimation(context, R.anim.time_digit_fade);
                    if (dateFadeAnim != null) {
                        dateNow.clearAnimation();
                        dateNow.startAnimation(dateFadeAnim);
                    }
                    dateNow.setText(dateText);
                }

                timeHandler.postDelayed(this, 1000);
            }
        };
        timeHandler.post(timeRunnable);
    }

    private void stopLiveClock() {
        timeHandler.removeCallbacks(timeRunnable);
    }

    //  Helper: Get timezone based on screen location
    private TimeZone getTimeZoneForLocation(String location) {
        if (location == null || location.isEmpty()) {
            return null;  // Use device default
        }

        String locationLower = location.toLowerCase();

        // Check if location contains any city/country from mapping
        java.util.Map<String, String> cityTimeZones = new java.util.HashMap<String, String>();
        cityTimeZones.put("kolkata", "Asia/Kolkata");
        cityTimeZones.put("mumbai", "Asia/Kolkata");
        cityTimeZones.put("delhi", "Asia/Kolkata");
        cityTimeZones.put("bangalore", "Asia/Kolkata");
        cityTimeZones.put("india", "Asia/Kolkata");
        cityTimeZones.put("london", "Europe/London");
        cityTimeZones.put("unitedkingdom", "Europe/London");
        cityTimeZones.put("newyork", "America/New_York");
        cityTimeZones.put("losangeles", "America/Los_Angeles");
        cityTimeZones.put("chicago", "America/Chicago");
        cityTimeZones.put("toronto", "America/Toronto");
        cityTimeZones.put("sydney", "Australia/Sydney");
        cityTimeZones.put("tokyo", "Asia/Tokyo");
        cityTimeZones.put("dubai", "Asia/Dubai");
        cityTimeZones.put("singapore", "Asia/Singapore");
        cityTimeZones.put("hongkong", "Asia/Hong_Kong");
        cityTimeZones.put("bangkok", "Asia/Bangkok");
        cityTimeZones.put("paris", "Europe/Paris");
        cityTimeZones.put("berlin", "Europe/Berlin");

        for (java.util.Map.Entry<String, String> entry : cityTimeZones.entrySet()) {
            if (locationLower.contains(entry.getKey())) {
                android.util.Log.d("AdvertLandWatch", " Timezone for '" + location + "': " + entry.getValue());
                return TimeZone.getTimeZone(entry.getValue());
            }
        }

        android.util.Log.d("AdvertLandWatch", " No timezone match for '" + location + "', using device default");
        return null;
    }

    private void startMediaRotation(List<MediaModel> mediaList, Context context) {
        this.mediaList = mediaList;
        this.currentIndex = 0;

        if (mediaSwitcher != null) {
            handler.removeCallbacks(mediaSwitcher);
        }
        mediaSwitcher = new Runnable() {
            @Override
            public void run() {
                if (mediaList == null || mediaList.isEmpty()) return;
                int currentHour = Integer.parseInt(getCurrentHourFormatted());

                View currentVisible = null;
                if (adImageView.getVisibility() == View.VISIBLE)     currentVisible = adImageView;
                else if (adPlayerView.getVisibility() == View.VISIBLE) currentVisible = adPlayerView;
                else if (weatherLayout.getVisibility() == View.VISIBLE) currentVisible = weatherLayout;
                else if (newsLayout.getVisibility() == View.VISIBLE)  currentVisible = newsLayout;

                releaseExoPlayer();
                adPlayerView.setVisibility(View.GONE);
                if (currentVisible == adPlayerView) currentVisible = null;
                if (currentVisible != adImageView)   adImageView.setVisibility(View.GONE);
                if (currentVisible != weatherLayout) weatherLayout.setVisibility(View.GONE);
                if (currentVisible != newsLayout)    newsLayout.setVisibility(View.GONE);

                // Default: hide logo/QR for every slide; only ads (IMAGE/VIDEO) will re-show them
                logoImage.setVisibility(View.GONE);
                qrImage.setVisibility(View.GONE);

                MediaModel media = mediaList.get(currentIndex);
                if (DataHolder.getInstance().targetHoursFlag == 1) {
                    String type = media.getType();
                    if (!stringToList(media.getTargetHours()).contains(currentHour)
                            && !type.equals("weather") && !type.equals("news")) {
                        currentIndex = (currentIndex + 1) % mediaList.size();
                        handler.post(this);
                        return;
                    }
                }
                long durationMs = media.getDurationInMillis();
                // Default to 6 seconds if the ad has no display time set
                if (durationMs <= 0) { durationMs = 6000; }

                if (media.getType().equals("IMAGE") || media.getType().equals("")) {
                    waitingLogo.setVisibility(View.GONE);
                    Glide.with(getApplicationContext()).load(media.getUrl()).into(adImageView);
                    QRCodeMaker(media.getInfo());
                    displayText.setText(media.getDisplayText());
                    qrImage.setVisibility(View.VISIBLE);
                    logoImage.setVisibility(View.VISIBLE);
                    if (DataHolder.getInstance().displayFlag == 1) {
                        displayText.setSelected(true);
                        displayText.setVisibility(View.VISIBLE);
                    }
                    slideTransition(adImageView, currentVisible);
                    handler.postDelayed(this, durationMs);
                    saveAndSendImpression(media, durationMs, context);

                } else if (media.getType().equals("VIDEO")) {
                    waitingLogo.setVisibility(View.GONE);
                    displayText.setText(media.getDisplayText());
                    adPlayerView.setVisibility(View.INVISIBLE);
                    logoImage.setVisibility(View.VISIBLE);
                    QRCodeMaker(media.getInfo());
                    qrImage.setVisibility(View.VISIBLE);
                    if (DataHolder.getInstance().displayFlag == 1) {
                        displayText.setSelected(true);
                        displayText.setVisibility(View.VISIBLE);
                    }
                    setupExoPlayer(media.getUrl(), null, null);
                    handler.postDelayed(this, durationMs);
                    saveAndSendImpression(media, durationMs, context);

                } else if (media.getType().equals("weather")) {
                    waitingLogo.setVisibility(View.GONE);
                    logoImage.setVisibility(View.GONE);
                    qrImage.setVisibility(View.GONE);
                    displayText.setVisibility(View.GONE);
                    slideTransition(weatherLayout, currentVisible);
                    handler.postDelayed(this, durationMs);

                } else if (media.getType().equals("news")) {
                    updateDebugText("NEWS " + (newsIndex + 1) + " | 10s");
                    waitingLogo.setVisibility(View.GONE);
                    logoImage.setVisibility(View.GONE);
                    qrImage.setVisibility(View.GONE);
                    displayText.setVisibility(View.GONE);

                    if (newsIndex >= getNews.size()) {
                        newsIndex = 0;
                    }
                    if (getNews.isEmpty()) {
                        shimmer.startShimmer();
                        shimmer.setVisibility(View.VISIBLE);
                        newsHandler = new NewsHandler(0);
                        try {
                            newsHandler.load(DataHolder.getInstance().location, context, (rss, i) -> {
                                getNews = new ArrayList<>(rss);
                                newsIndex = 0;
                                shimmer.stopShimmer();
                                shimmer.setVisibility(View.GONE);
                                return Unit.INSTANCE;
                            }, bar -> {
                                if (bar == 1) shimmer.stopShimmer();
                                shimmer.setVisibility(View.GONE);
                                return Unit.INSTANCE;
                            });
                        } catch (Exception e) {
                            shimmer.stopShimmer();
                            shimmer.setVisibility(View.GONE);
                        }
                    }

                    if (!getNews.isEmpty() && newsIndex < getNews.size()) {
                        if (getNews.get(newsIndex).getThumbnailUrl().endsWith(".gif")) {
                            Glide.with(context).asGif()
                                    .load(getNews.get(newsIndex).getThumbnailUrl())
                                    .into(newsImg);
                        } else {
                            Glide.with(getApplicationContext())
                                    .load(getNews.get(newsIndex).getThumbnailUrl())
                                    .into(newsImg);
                        }
                        newsHeader.setText(getNews.get(newsIndex).getTitle());
                        // Strip HTML and use fallback if description is empty
                        String description = getNews.get(newsIndex).getDescription();
                        if (description == null || description.trim().isEmpty()) {
                            description = "Breaking news from " + DataHolder.getInstance().location;
                        }
                        newsDesc.setText(description);
                        // Show source label if available
                        String src = getNews.get(newsIndex).getSource();
                        if (newsSourceF != null) {
                            if (src != null && !src.isEmpty()) {
                                newsSourceF.setText(src);
                                newsSourceF.setVisibility(View.VISIBLE);
                            } else {
                                newsSourceF.setVisibility(View.GONE);
                            }
                        }
                        newsIndex++;
                    }

                    newsHeader.setVisibility(View.VISIBLE);
                    newsDesc.setVisibility(View.VISIBLE);
                    newsImg.setVisibility(View.VISIBLE);

                    //  Apply Ken Burns zoom animation to hero image
                    if (newsImg != null) {
                        newsImg.clearAnimation();
                        Animation kenBurnsZoom = AnimationUtils.loadAnimation(context, R.anim.ken_burns_zoom);
                        if (kenBurnsZoom != null) {
                            newsImg.startAnimation(kenBurnsZoom);
                        }
                    }

                    // ✨ Apply fade-up animation to headline
                    if (newsHeader != null) {
                        newsHeader.clearAnimation();
                        newsHeader.setAlpha(0f);
                        Animation headlineFadeUp = AnimationUtils.loadAnimation(context, R.anim.headline_fade_up);
                        if (headlineFadeUp != null) {
                            headlineFadeUp.setAnimationListener(new Animation.AnimationListener() {
                                @Override
                                public void onAnimationStart(Animation animation) {}
                                @Override
                                public void onAnimationEnd(Animation animation) {
                                    newsHeader.setAlpha(1f);
                                }
                                @Override
                                public void onAnimationRepeat(Animation animation) {}
                            });
                            newsHeader.startAnimation(headlineFadeUp);
                        } else {
                            newsHeader.setAlpha(1f);
                        }
                    }

                    slideTransition(newsLayout, currentVisible);
                    handler.postDelayed(this, durationMs);
                }

                currentIndex = (currentIndex + 1) % mediaList.size();
            }
        }

        ;
        handler.removeCallbacks(mediaSwitcher);
        handler.post(mediaSwitcher);

        handler.removeCallbacks(refreshRunnable);

        if (

                isInternetAvailable()) {
            if (newTime <= 60) {
                handler.postDelayed(refreshRunnable, (long) newTime * 60 * 1000);

            }
        }

    }

    public boolean isInternetAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            @SuppressLint({"NewApi", "LocalSuppress"})
            NetworkCapabilities capabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());
            return capabilities != null &&
                    (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
        }
        return false;
    }

    /*
        @OptIn(markerClass = {UnstableApi.class, UnstableApi.class, UnstableApi.class})
    */
    private void setupExoPlayer(String url, Animation inAnim, Animation outAnim) {

        releaseExoPlayer();

        exoPlayer = new ExoPlayer.Builder(AdvertLandWatch.this).build();
        adPlayerView.setPlayer(exoPlayer);

        adPlayerView.setUseController(false);

        adPlayerView.setVisibility(View.VISIBLE);
        adPlayerView.setAlpha(0f);
        exoPlayer.setPlayWhenReady(false);

        MediaItem mediaItem = MediaItem.fromUri(Uri.parse(url));
        exoPlayer.setMediaItem(mediaItem);
        exoPlayer.prepare();

        playerListener = new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int state) {
                switch (state) {
                    case Player.STATE_BUFFERING:
                        break;

                    case Player.STATE_READY:
                        exoPlayer.setPlayWhenReady(true);

                        adPlayerView.animate().alpha(1f).setDuration(300).start(); // fade in player
                        adImageView.animate().alpha(0f).setDuration(300).withEndAction(() -> {
                            adImageView.setVisibility(View.GONE);
                            adImageView.setAlpha(1f); // reset alpha لو حنستخدمها بعدين
                        }).start();

                        long duration = exoPlayer.getDuration();
                        if (duration != C.TIME_UNSET && duration > 0) {
                            handler.removeCallbacks(mediaSwitcher);
                            handler.postDelayed(mediaSwitcher, duration);
                        }
                        break;

                    case Player.STATE_ENDED:
                        adPlayerView.animate().alpha(0f).setDuration(250).withEndAction(() -> {
                            adPlayerView.setVisibility(View.INVISIBLE);
                        }).start();

                        adImageView.setVisibility(View.VISIBLE);
                        adImageView.setAlpha(0f);
                        adImageView.animate().alpha(1f).setDuration(250).start();

                        handler.removeCallbacks(mediaSwitcher);
                        handler.postDelayed(mediaSwitcher, 500);
                        break;
                }
            }

            @Override
            public void onPlayerError(PlaybackException error) {
                handler.removeCallbacks(mediaSwitcher);
                handler.post(mediaSwitcher);
            }
        };

        exoPlayer.addListener(playerListener);
    }

   /* public String getCurrentHourFormatted() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);  // 0-23
        return String.valueOf(hour);
    }*/

    private int qrCodeImageDimension() {
        WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        int width = point.x;
        int height = point.y;
        int smallerDimension = width < height ? width : height;
        return smallerDimension = smallerDimension * 3 / 4;
    }

    private void QRCodeMaker(String inputValue) {

        // Initializing the QR Encoder with your value to be encoded, type you required and Dimension
        QRGEncoder qrgEncoder = new QRGEncoder(inputValue, null, QRGContents.Type.TEXT, qrImageDimension);
        qrgEncoder.setColorBlack(Color.WHITE);
        qrgEncoder.setColorWhite(Color.BLACK);
        try {
            // Getting QR-Code as Bitmap
            Bitmap bitmap = qrgEncoder.getBitmap();
            // Setting Bitmap to ImageView
            qrImage.setImageBitmap(bitmap);
        } catch (Exception e) {
            System.out.println("Exception occored: " + e.toString());
        }
    }

    private void releaseExoPlayer() {
        if (exoPlayer != null) {
            if (playerListener != null) {
                exoPlayer.removeListener(playerListener);
            }
            exoPlayer.release();
            exoPlayer = null;
        }
    }

    public void saveAndSendImpression(MediaModel media, long durationMs, Context context) {
        ImpressionEntity impression = new ImpressionEntity();

        impression.impressionId = UUID.randomUUID().toString();

        impression.advertId = media.getAdvertId();
        impression.amountSettled = false;
        impression.contractId = media.getContractId(); // ممكن تغيرها لو عندك بيانات ديناميكية
        impression.currency = media.getCurrency();      // ممكن تغيرها لو عندك بيانات من JSON
        impression.dayHour = Integer.parseInt(getCurrentHourFormatted());
        impression.playSec = (int) (durationMs / 1000);
        impression.format = media.getType();
        impression.locationType = DataHolder.getInstance().locationTypes; // ممكن تغيرها لو عندك بيانات من JSON
        impression.maxBid = media.getMaxBid();             // ممكن تتجاهلها أو تغيرها
        impression.orientation = orient;
        impression.playTimeStamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).format(new Date());
        impression.screenDevice = DataHolder.getInstance().screenDevice;
        impression.screenPlayer = DataHolder.getInstance().screenPlayer;
        impression.screenId = DataHolder.getInstance().screenID;

        // لو tags عندك List<String> بدل String، عدّل هنا
        impression.tags = DataHolder.getInstance().tags;

        // حفظ في Room (تأكد من ان ImpressionDatabase معرف بشكل صحيح)
        AdDatabase db = AdDatabase.getInstance(context);
        new Thread(() -> {
            db.impDao().insertImpression(impression);

            // رفع البيانات لو فيه إنترنت
            if (isInternetAvailable(context)) {
                APIImpression.sendImpression(context, impression);
               /* AdDatabase adDatabase = AdDatabase.getInstance(context);
                List<ImpressionEntity> impressions = adDatabase.impDao().getAllImpressions();
                for (ImpressionEntity impression1 : impressions) {

                }*/
            }
        }).start();
    }

    // دالة مساعدة ترجع الساعة الحالية كـ String (مثال)
    private String getCurrentHourFormatted() {
        return new SimpleDateFormat("H", Locale.getDefault()).format(new Date());
    }

    private boolean isInternetAvailable(Context context) {
        return isInternetAvailable();
    }

    /*
        @OptIn(markerClass = UnstableApi.class)
    */
    private void setupPlayerResizeMode(PlayerView playerView) {
        int uiMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_TYPE_MASK;

        if (uiMode == Configuration.UI_MODE_TYPE_TELEVISION) {
            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL); // أو ZOOM
        } else {
            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
        }
    }

    public static String downloadFileToInternalStorage(Context context, String fileUrl, String fileName, String token) {
        OkHttpClient client = new OkHttpClient();
        Request.Builder builder = new Request.Builder().url(fileUrl);
        if (token != null) builder.addHeader("Authorization", "Bearer " + token);
        try (okhttp3.Response response = client.newCall(builder.build()).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                InputStream inputStream = response.body().byteStream();
                File file = new File(context.getFilesDir(), fileName);
                FileOutputStream outputStream = new FileOutputStream(file);
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) outputStream.write(buffer, 0, bytesRead);
                outputStream.close();
                inputStream.close();
                return file.getAbsolutePath();
            }
        } catch (IOException e) { e.printStackTrace(); }
        return null;
    }

    public static String downloadFileToInternalStorage(Context context, String fileUrl, String fileName) {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(fileUrl)
                .build();

        try (okhttp3.Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
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

                return file.getAbsolutePath();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
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

    private void slideTransition(final View showView, final View hideView) {
        float offsetPx = getResources().getDisplayMetrics().density * 60;

        if (hideView != null && hideView.getVisibility() == View.VISIBLE) {
            hideView.animate()
                    .alpha(0f)
                    .translationXBy(-offsetPx)
                    .setDuration(450)
                    .setInterpolator(new AccelerateInterpolator())
                    .withEndAction(() -> {
                        hideView.setVisibility(View.GONE);
                        hideView.setTranslationX(0f);
                        hideView.setAlpha(1f);
                    })
                    .start();
        }

        showView.setAlpha(0f);
        showView.setTranslationX(offsetPx);
        showView.setVisibility(View.VISIBLE);
        showView.animate()
                .alpha(1f)
                .translationX(0f)
                .setDuration(450)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    private void crossfade(final View showView, final View hideView, long duration) {
        if (hideView != null) {
            hideView.animate()
                    .alpha(0f)
                    .setDuration(duration)
                    .withEndAction(() -> hideView.setVisibility(View.GONE))
                    .start();
        }
        showView.setAlpha(0f);
        showView.setVisibility(View.VISIBLE);
        showView.animate()
                .alpha(1f)
                .setDuration(duration)
                .start();
    }

    /**
     * Updates debug overlay to show current playback status.
     * TODO: Remove before production release
     */
    private void updateDebugText(String message) {
        // Debug text would go here if debugOverlay was available
        // For now, just log it
        android.util.Log.d("AdvertLandWatch", " DEBUG: " + message);
    }
}

