package com.adjaba.activities;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.text.SpannableString;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.adjaba.news.NewsHandler;
import com.adjaba.news.RssItem;
import com.adjaba.news.Utils;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.PlayerView;


import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.webkit.MimeTypeMap;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

import com.adjaba.R;
import com.adjaba.activities.viewmodel.APIImpression;
import com.adjaba.content.SecureSignageWebView;
import com.adjaba.social.SocialMediaManager;
import com.adjaba.social.SocialPostAdapter;
import com.adjaba.activities.viewmodel.DataHolder;
import com.adjaba.models.DemographicData;
import com.adjaba.models.newmodels.Forecastday;
import com.adjaba.models.newmodels.Hour;
import com.adjaba.models.newmodels.MediaModel;
import com.adjaba.models.newmodels.VideoImageModel;
import com.adjaba.models.newmodels.WatchingModel;
import com.adjaba.models.newmodels.WeatherModel;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import kotlin.Unit;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class AdvertWatching extends AppCompatActivity {
    private List<WatchingModel> adList = new ArrayList<>();
    List<MediaModel> mediaList = new ArrayList<>();
    int[] loadedCount = {0};
    ViewGroup weatherLayout;  // ✅ FIXED: ViewGroup accepts both LinearLayout (portrait) and ConstraintLayout (landscape)
    FrameLayout newsLayout;
    ImageView waitingLogo, newsImg;
    int weatherCurrent;
    Runnable runnableLogo;
    private int currentIndex = 0;
    private ExoPlayer exoPlayer;
    float dx = 6f; // سرعة الاتجاه الأفقي
    float dy = 6f; // سرعة الاتجاه الرأسي
    private Handler handler1 = new Handler(Looper.getMainLooper());
    private Player.Listener playerListener = null;
    private Handler handler = new Handler();
    ImageView logoImage;
    Map<String, List<Integer>> advertHoursMap; // المفتاح advertId، والقيمة الساعات اللي يتعرض فيها الإعلان
    RotateAnimation rotate;
    ShimmerFrameLayout shimmer;
    private RetrofitBuilder retrofitBuilder = new RetrofitBuilder();
    private ImageView adImageView, noAdsLogo;
    private ImageView weatherImg;
    private ObjectAnimator breatheAnimator;
    TextView tvTemp, tvLoc, tvStatus, timeNow, dateNow, wind, rain, humadity, pressureView, progressText;
    private Runnable mediaSwitcher;
    private PlayerView adPlayerView;
    ConstraintLayout constLayout;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    ProgressBar progressBar;
    Context context;
    String screenLoc;
    Handler handlerLogo;
    String screenId;
    int refreshTime = 0, newsIndex = 0;
    String location;
    int newTime = 2;
    private Handler timeHandler = new Handler();
    private Runnable timeRunnable;
    int qrImageDimension;
    ImageView qrImage;
    List<RssItem> getNews,getBackupNews;
    private Runnable refreshRunnable;
    // ── 15-minute silent refresh ──────────────────────────────────────────────
    private static final long WEATHER_REFRESH_INTERVAL_MS = 15 * 60 * 1000L; // 15 min
    private static final long NEWS_REFRESH_INTERVAL_MS    = 15 * 60 * 1000L; // 15 min
    private final Handler weatherRefreshHandler = new Handler(Looper.getMainLooper());
    private final Handler newsRefreshHandler    = new Handler(Looper.getMainLooper());
    private Runnable weatherRefreshRunnable;
    private Runnable newsRefreshRunnable;
    // ── Playlist sync observer ───────────────────────────────────────────
    private Observer<PlaylistSyncManager.PlaylistUpdate> playlistUpdateObserver;
    // ─────────────────────────────────────────────────────────────────────────
    String mediaFormat = "";
    TextView displayText, newsHeader, newsDesc, newsTitle, newsSource;
    TextView debugOverlay; // Debug overlay for playback status
    String orient;
    SecureSignageWebView webContentView;
    FrameLayout socialFeedLayout;
    TextView socialPlatformLabel, socialHashtagLabel;
    RecyclerView socialPostsRecyclerView;
    SocialMediaManager socialMediaManager;
    NewsHandler newsHandler;

    @SuppressLint({"MissingInflatedId", "UnsafeOptInUsageError"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //  EXPLICIT ORIENTATION LOCK & LAYOUT SELECTION - APPROACH A
        // Decouple layout selection from device physical orientation
        // Layout variant ONLY depends on user's selected orientation in DataHolder
        orient = DataHolder.getInstance().orient.toLowerCase();

        if ("landscape".equalsIgnoreCase(orient)) {
            // Landscape mode: lock to landscape, load landscape layout variant
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            setContentView(R.layout.fragment_advert_watching);  // Will load layout-land/ variant

        } else if ("portrait".equalsIgnoreCase(orient)) {
            // Portrait mode: lock to portrait, load portrait layout variant
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            setContentView(R.layout.fragment_advert_watching);  // Will load layout/ variant

        } else if ("forced portrait".equalsIgnoreCase(orient)) {
            // Forced portrait uses AdvertLandWatch activity, shouldn't reach here
            // But set landscape base for consistency
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            setContentView(R.layout.fragment_advert_watching);
            finish();
            return;
        } else {
            // Default fallback to portrait
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            setContentView(R.layout.fragment_advert_watching);
        }
        constLayout = findViewById(R.id.mainConstLayout);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        boolean isDataLoaded = prefs.getBoolean("data_loaded", false);
        getNews = new ArrayList<>();
        getBackupNews=new ArrayList<>();
        qrImage = findViewById(R.id.qrCodeImage);
        logoImage = findViewById(R.id.logoImage);
        adImageView = findViewById(R.id.adImageView);
        newsHeader = findViewById(R.id.main_header);
        tvStatus = findViewById(R.id.currentStatus);
        newsTitle = findViewById(R.id.newsTitle);
        newsImg = findViewById(R.id.news_img);
        timeNow = findViewById(R.id.timeNow);
        dateNow = findViewById(R.id.dateNow);
        shimmer = findViewById(R.id.shimmer);
        waitingLogo = findViewById(R.id.waitingLogo);
        tvTemp = findViewById(R.id.weatherTemp);
        newsDesc = findViewById(R.id.news_details);
        newsLayout = findViewById(R.id.newsLayout);
        newsSource = findViewById(R.id.newsSource);
        // Orientation already locked at start of onCreate
        prefs.edit().remove("data_loaded").apply();
        displayText = findViewById(R.id.displayText);
        debugOverlay = findViewById(R.id.debugOverlay);
        context = this;
        advertHoursMap = new HashMap<>();
        rain = findViewById(R.id.rain);
        wind = findViewById(R.id.windW);
        humadity = findViewById(R.id.hamudity);
        pressureView = findViewById(R.id.pressure);
        progressBar = findViewById(R.id.loadBar);
        progressText = findViewById(R.id.progressText);
        weatherCurrent = 3;
        weatherLayout = findViewById(R.id.weatherLayout);
        weatherImg = findViewById(R.id.currentWeatherImg);
        tvLoc = findViewById(R.id.weatherLoc);
        adPlayerView = findViewById(R.id.adPlayerView);
        webContentView = findViewById(R.id.webContentView);
        socialFeedLayout = findViewById(R.id.socialFeedLayout);
        socialPlatformLabel = findViewById(R.id.socialPlatformLabel);
        socialHashtagLabel = findViewById(R.id.socialHashtagLabel);
        socialPostsRecyclerView = findViewById(R.id.socialPostsRecyclerView);
        if (socialPostsRecyclerView != null) {
            socialPostsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        }
        socialMediaManager = new SocialMediaManager(this);
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
// جلب مقاسات الشاشة
        View qr = findViewById(R.id.qrCodeImage);

        boolean isTV = getPackageManager().hasSystemFeature("android.software.leanback");
        int percent=0;
        // Use selected orientation, NOT device physical orientation
        if ("landscape".equalsIgnoreCase(orient)) {
            percent = 12;
        } else {
            percent = isTV?12:20;
        }
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
                // FIXED: Don't delete ads or re-fetch from API
                // Ads come from SelectScreens and are managed locally
                // Only refresh weather data periodically
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
        startBreatheAnimation();
        List<MediaModel> mediaModels = new ArrayList<>();
        screenLoc = location;
        if (!isDataLoaded || orient.equals("portrait") || orient.equals("landscape") || orient.equals("forced portrait")) {
            android.util.Log.i("AdvertWatching", " onCreate() - Initializing playback");
            android.util.Log.i("AdvertWatching", "   isDataLoaded: " + isDataLoaded);
            android.util.Log.i("AdvertWatching", "   DataHolder.allAds: " + (DataHolder.getInstance().allAds == null ? "NULL" : DataHolder.getInstance().allAds.size() + " ads"));

            newsHandler = new NewsHandler(0);
            newsHandler.load(DataHolder.getInstance().location, context, (rss, i) -> {
                getNews = new ArrayList<>(rss);
                getBackupNews = new ArrayList<>(rss);
                newsIndex = 0;
                android.util.Log.d("AdvertWatching", "    News loaded: " + (rss == null ? "0" : rss.size()) + " articles");
                updateDebugText("News loaded: " + (rss == null ? "0" : rss.size()) + " articles");
                return Unit.INSTANCE;
            }, bar -> {
                if (bar == 1) shimmer.stopShimmer();
                shimmer.setVisibility(View.GONE);
                return Unit.INSTANCE;
            });

            if (DataHolder.getInstance().allAds == null || DataHolder.getInstance().allAds.isEmpty()) {
                android.util.Log.w("AdvertWatching", "⚠️ NO ADS AVAILABLE - Showing weather and news only");
                android.util.Log.i("AdvertWatching", "   Reason: allAds=" + (DataHolder.getInstance().allAds == null ? "NULL" : "EMPTY"));
                updateDebugText("NO ADS - Weather/News only mode");
                getWeather(location, context);
                // No ads — cycle weather and news slides
                List<MediaModel> infoSlides = new ArrayList<>();
                infoSlides.add(new MediaModel("", "", 0, "weather", "", 10000, "", "", "", "", ""));
                infoSlides.add(new MediaModel("", "", 0, "news", "", 10000, "", "", "", "", ""));
                android.util.Log.e("AdvertWatching", " STARTING WEATHER+NEWS ROTATION (no ads)");
                startMediaRotation(infoSlides, context);
            } else {
                android.util.Log.i("AdvertWatching", "✨ Starting playback with " + DataHolder.getInstance().allAds.size() + " ads");
                updateDebugText("Playing " + DataHolder.getInstance().allAds.size() + " ads");
                getWeather(location, context);
                List<MediaModel> rotationList = insertWeatherEveryThreeAds(DataHolder.getInstance().allAds);
                android.util.Log.i("AdvertWatching", "   Total items in rotation: " + (rotationList == null ? "0" : rotationList.size()) + " (ads + weather + news)");
                updateDebugText("Rotation: " + (rotationList == null ? "0" : rotationList.size()) + " items (ads+weather+news)");
                android.util.Log.e("AdvertWatching", " STARTING AD+WEATHER+NEWS ROTATION with " + (rotationList == null ? 0 : rotationList.size()) + " items");
                startMediaRotation(rotationList, context);
            }


            prefs.edit().putBoolean("data_loaded", true).apply();
            startWeatherAutoRefresh();
            startNewsAutoRefresh();
            registerPlaylistSyncReceiver();

            // Initialize MQTT for demographic-based ad switching
            initializeMqtt(prefs);
        }
    }

    /**
     * Silently refreshes weather data every 15 minutes.
     * Updates text/image views in-place — no slide transition, no flicker.
     */
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

    /**
     * Silently refreshes news RSS every 15 minutes.
     * Replaces the in-memory list; the next time the news slide shows, it uses the fresh data.
     */
    private void startNewsAutoRefresh() {
        newsRefreshRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isFinishing() && !isDestroyed()) {
                    Utils.INSTANCE.getNewsList().clear(); // force fresh network fetch
                    newsHandler = new NewsHandler(0);
                    newsHandler.load(DataHolder.getInstance().location, context, (rss, i) -> {
                        getNews = new ArrayList<>(rss);
                        getBackupNews = new ArrayList<>(rss);
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

            android.util.Log.i("AdvertWatching", "📡 Playlist sync update received - screenId: " + updatedScreenId + ", ads: " + adsCount);

            // Only reload if this is the current screen
            if (screenId != null && screenId.equals(updatedScreenId)) {
                android.util.Log.i("AdvertWatching", "   🔄 Reloading playlist from local database...");
                reloadPlaylistFromDatabase();
            }
        };

        PlaylistSyncManager.getInstance().getPlaylistUpdateLiveData().observe(this, playlistUpdateObserver);
        android.util.Log.i("AdvertWatching", "✅ Registered playlist sync observer (LiveData)");
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
                android.util.Log.w("AdvertWatching", "⚠️ No ads in database after sync");
                return;
            }

            android.util.Log.i("AdvertWatching", " Loaded " + adEntities.size() + " ads from database");

            // Build MediaModel list from database
            List<MediaModel> updatedAds = new ArrayList<>();
            for (AdEntity ad : adEntities) {
                boolean isStreamingType = "LIVE_STREAM".equals(ad.format)
                        || "WEB_CONTENT".equals(ad.format)
                        || "SOCIAL_FEED".equals(ad.format);
                if ((ad.localPath != null && !ad.localPath.isEmpty()) || isStreamingType) {
                    MediaModel m = new MediaModel(
                            ad.contractId, ad.currency, ad.maxBid, ad.format,
                            ad.localPath != null ? ad.localPath : "",
                            ad.duration, ad.textBottom, ad.textTop,
                            "", ad.targetHours, ad.advertId,
                            ad.targetGender, ad.targetAgeGroup, ad.targetTags, ad.targetEmotion
                    );
                    if (ad.streamType != null)     m.setStreamType(ad.streamType);
                    if (ad.socialPlatform != null) m.setSocialPlatform(ad.socialPlatform);
                    if (ad.socialHashtag != null)  m.setSocialHashtag(ad.socialHashtag);
                    updatedAds.add(m);
                }
            }

            // Update DataHolder and rebuild rotation on main thread
            new Handler(Looper.getMainLooper()).post(() -> {
                DataHolder.getInstance().allAds = updatedAds;
                android.util.Log.i("AdvertWatching", "✅ Updated DataHolder.allAds with " + updatedAds.size() + " ads");

                // Rebuild rotation list
                List<MediaModel> newRotation = insertWeatherEveryThreeAds(updatedAds);
                android.util.Log.i("AdvertWatching", "   New rotation has " + (newRotation == null ? 0 : newRotation.size()) + " items");

                // Update mediaList for playback
                boolean wasEmpty = mediaList.isEmpty();
                mediaList.clear();
                if (newRotation != null) {
                    mediaList.addAll(newRotation);
                }
                // Keep currentIndex in bounds after a playlist shrink
                if (currentIndex >= mediaList.size()) {
                    currentIndex = 0;
                }
                // Restart rotation if it had stalled on an empty list
                if (!mediaList.isEmpty() && wasEmpty) {
                    handler.removeCallbacks(mediaSwitcher);
                    handler.post(mediaSwitcher);
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
            android.util.Log.i("AdvertWatching", " IOT (MQTT) disabled in settings");
            return;
        }

        if (screenId == null || screenId.isEmpty()) {
            android.util.Log.w("AdvertWatching", "⚠️ Cannot start MQTT: screenId is null");
            return;
        }

        android.util.Log.i("AdvertWatching", " Initializing MQTT demographics for screen: " + screenId);

        MqttManager.getInstance().connect(this, screenId, new MqttManager.OnDemographicDataListener() {
            @Override
            public void onDemographicDataReceived(DemographicData data) {
                android.util.Log.d("AdvertWatching", " Demographic data received: " + data.toString());

                // Select best ad based on demographics
                MediaModel selectedAd = selectBestAdForDemographic(data);

                if (selectedAd != null) {
                    android.util.Log.i("AdvertWatching", "✅ Selected ad based on demographics: " + selectedAd.getAdvertId());
                    android.util.Log.i("AdvertWatching", "   Age: " + data.getAgeRange() + ", Gender: " + data.getGender());
                    android.util.Log.i("AdvertWatching", "   Dominant emotion: " + data.getDominantEmotion());

                    // Schedule the selected ad to play next (queue it instead of interrupting current ad)
                    scheduleNextAd(selectedAd);
                } else {
                    android.util.Log.d("AdvertWatching", "   No matching ad found for demographics");
                }
            }

            @Override
            public void onConnected() {
                android.util.Log.i("AdvertWatching", "✅ Connected to MQTT broker - listening for demographics");
            }

            @Override
            public void onConnectionLost(Throwable cause) {
                android.util.Log.w("AdvertWatching", "⚠️ MQTT connection lost: " +
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
            android.util.Log.d("AdvertWatching", "⏭️ No audience detected, skipping demographic-based selection");
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
                android.util.Log.d("AdvertWatching", "  ⏰ Hour match: " + currentHour);
            }

            // ✅ GENDER MATCHING: match MQTT gender against ad's targetGender list (+15 pts)
            String adGenderStr = ad.getTargetGender();
            if (viewerGender != null && !viewerGender.isEmpty()) {
                if (adGenderStr != null && !adGenderStr.isEmpty()) {
                    // Ad has explicit gender targeting — reward a real match
                    if (adGenderStr.toUpperCase().contains(viewerGenderApi)) {
                        score += 15;
                        android.util.Log.d("AdvertWatching", "   Gender match: " + viewerGenderApi);
                    }
                } else {
                    // Ad targets all genders — small baseline bonus
                    score += 5;
                    android.util.Log.d("AdvertWatching", "   Gender detected (no ad filter): " + viewerGender);
                }
            }

            // ✅ AGE BRACKET MATCHING: match MQTT ageBracket against ad's targetAgeGroup (+15 pts)
            String adAgeStr = ad.getTargetAgeGroup();
            if (viewerAgeBracket != null && !viewerAgeBracket.isEmpty()) {
                if (adAgeStr != null && !adAgeStr.isEmpty()) {
                    if (adAgeStr.contains(viewerAgeBracket)) {
                        score += 15;
                        android.util.Log.d("AdvertWatching", "   Age bracket match: " + viewerAgeBracket);
                    }
                } else {
                    // Rough bracket detection fallback if no stored targeting
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
                        android.util.Log.d("AdvertWatching", "  ️ Tag match: " + tag);
                    }
                }
            }

            // ✅ EMOTION MATCHING: Smart emotion-based targeting
            String adEmotionStr = ad.getTargetEmotion();
            String dominantEmotion = data.getDominantEmotion();
            if (dominantEmotion == null) dominantEmotion = "unknown";
            int happyScore = data.getHappy();

            if (adEmotionStr != null && !adEmotionStr.isEmpty()) {
                // Ad HAS emotion targeting — only score if it matches viewer's emotion
                if (adEmotionStr.toLowerCase().contains(dominantEmotion.toLowerCase())) {
                    if ("happy".equals(dominantEmotion)) {
                        score += 25;
                        android.util.Log.d("AdvertWatching", "   Happy emotion MATCH: Promo/upbeat ad");
                    } else if ("neutral".equals(dominantEmotion)) {
                        score += 15;
                        android.util.Log.d("AdvertWatching", "   Neutral emotion MATCH: Brand/info ad");
                    } else if ("sad".equals(dominantEmotion) || "angry".equals(dominantEmotion)) {
                        score += 20;
                        android.util.Log.d("AdvertWatching", "   Negative emotion MATCH: Comfort/support ad");
                    } else if ("surprise".equals(dominantEmotion)) {
                        score += 18;
                        android.util.Log.d("AdvertWatching", "   Surprise emotion MATCH");
                    } else {
                        score += 10;
                        android.util.Log.d("AdvertWatching", "   Other emotion MATCH: " + dominantEmotion);
                    }
                } else {
                    android.util.Log.d("AdvertWatching", "  ⏭️ Emotion filter: ad wants " + adEmotionStr + ", viewer is " + dominantEmotion);
                }
            } else {
                // Ad has NO emotion filter — give baseline emotion bonus to all ads
                if ("happy".equals(dominantEmotion)) {
                    score += 8;
                    android.util.Log.d("AdvertWatching", "   Happy viewer: Baseline bonus");
                } else if ("neutral".equals(dominantEmotion)) {
                    score += 5;
                    android.util.Log.d("AdvertWatching", "   Neutral viewer: Baseline bonus");
                } else {
                    score += 3;
                    android.util.Log.d("AdvertWatching", "   " + dominantEmotion + " viewer: Baseline bonus");
                }
            }

            // Bonus if happiness score is very high (> 60%)
            if (happyScore > 60 && (adEmotionStr == null || adEmotionStr.toLowerCase().contains("happy"))) {
                score += 15;
                android.util.Log.d("AdvertWatching", "  ⭐ Very high happiness threshold met");
            }

            // ✅ ENGAGEMENT TIME MATCHING (+8 if long dwell)
            int avgDwellSec = data.getAvgDwellSec();
            int adDurationSec = ad.getDurationInMillis() / 1000;

            if (avgDwellSec > 15 && adDurationSec > 10) {
                score += 8;
                android.util.Log.d("AdvertWatching", "  ⏳ High engagement time (" + avgDwellSec + "s): Select longer ads");
            } else if (avgDwellSec <= 5 && adDurationSec <= 5) {
                score += 5;
                android.util.Log.d("AdvertWatching", "  ⚡ Quick engagement: Select short ads");
            }

            // ✅ AUDIENCE SIZE BONUS (+5 pts for group of 3+)
            int audienceCount = data.getCustomerCount();
            if (audienceCount >= 3) {
                score += 5;
                android.util.Log.d("AdvertWatching", "   Multi-person audience: Select group-appeal ads");
            }

            android.util.Log.d("AdvertWatching", "   Ad " + ad.getAdvertId()
                    + " [gender=" + adGenderStr + " age=" + adAgeStr + " tags=" + adTagsStr + " emotion=" + adEmotionStr + "] score: " + score);

            if (score > highestScore) {
                highestScore = score;
                bestAd = ad;
            }
        }

        if (bestAd != null) {
            android.util.Log.i("AdvertWatching", " DEMOGRAPHIC MATCH WINNER: " + bestAd.getAdvertId() + " (score: " + highestScore + ")");
            android.util.Log.i("AdvertWatching", "   Viewer: " + viewerGender + " " + viewerAgeBracket
                    + " | Emotion: " + data.getDominantEmotion() + " | Audience: " + data.getCustomerCount()
                    + " | Tags: " + viewerTags);
        } else {
            android.util.Log.d("AdvertWatching", "⏭️ No suitable ad found for demographics");
        }

        return bestAd;
    }

    /**
     * Parse target hours from comma-separated string.
     * @param targetHoursStr String like "9,10,11,14,15,16"
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
            android.util.Log.w("AdvertWatching", "Failed to parse target hours: " + targetHoursStr);
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
                android.util.Log.w("AdvertWatching", " Cannot schedule ad — mediaList is empty");
                return;
            }

            // Remove any previous occurrence to avoid duplicates
            mediaList.remove(ad);

            // Insert immediately after the currently-playing slot
            int insertAt = Math.min(currentIndex + 1, mediaList.size());
            mediaList.add(insertAt, ad);

            android.util.Log.i("AdvertWatching", " Queued demographic ad at position "
                    + insertAt + "/" + mediaList.size() + ": " + ad.getAdvertId());
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Cancel pending mediaSwitcher callbacks and release ExoPlayer before tearing down views
        if (handler != null && mediaSwitcher != null) {
            handler.removeCallbacks(mediaSwitcher);
        }
        releaseExoPlayer();

        if (webContentView != null) webContentView.cleanup();
        if (socialMediaManager != null) socialMediaManager.shutdown();

        // Disconnect MQTT
        MqttManager.getInstance().disconnect();
        android.util.Log.i("AdvertWatching", "✅ Disconnected from MQTT broker");

        // LiveData observer automatically unregistered on lifecycle destroy
        android.util.Log.i("AdvertWatching", "✅ Playlist sync observer auto-cleanup (LiveData)");

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
    }

    void getAds(int flag) {
        retrofitBuilder.apiCalls().getAdsByScreen(screenId, "Bearer " + AuthManager.getToken(this)).enqueue(new Callback<List<WatchingModel>>() {
            @Override
            public void onResponse(Call<List<WatchingModel>> call, Response<List<WatchingModel>> response) {
                adList = response.body();
                if (adList == null || adList.isEmpty()) {
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

                                    if (remaining == 0) {
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
                        Glide.with(AdvertWatching.this).load(iconUrl).into(weatherImg);
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
                    if (pressureView != null) pressureView.setText(Math.round(response.body().current.pressure_mb) + "");
                    tvLoc.setText(DataHolder.getInstance().location);
                    tvTemp.setText(Math.round(response.body().current.temp_c) + "°C");
                    tvStatus.setText(response.body().current.condition.text);

                } else {
                    // Response came back but body/current is null — still update location label
                    if (tvLoc != null) {
                        tvLoc.setText(DataHolder.getInstance().location != null ? DataHolder.getInstance().location : "");
                    }
                }
                //progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<WeatherModel> call, Throwable t) {
                android.util.Log.e("AdvertWatching", "❌ Weather API failed: " + (t != null ? t.getMessage() : "unknown error"));
                // Set default/fallback weather values
                tvTemp.setText("N/A");
                tvStatus.setText("Weather unavailable");
                tvLoc.setText(DataHolder.getInstance().location != null ? DataHolder.getInstance().location : "Unknown");
                humadity.setText("--");
                wind.setText("--");
                rain.setText("--");
                if (pressureView != null) pressureView.setText("--");
                android.util.Log.i("AdvertWatching", "✅ Set fallback weather values");
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
                // ممكن تتجاهل أو تتعامل مع الخطأ حسب حاجتك
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

    private void startBreatheAnimation() {
        View pinIcon = findViewById(R.id.loc_pin_icon);
        if (pinIcon == null) return;
        breatheAnimator = ObjectAnimator.ofFloat(pinIcon, "alpha", 1.0f, 0.35f);
        breatheAnimator.setDuration(2000);
        breatheAnimator.setRepeatMode(ValueAnimator.REVERSE);
        breatheAnimator.setRepeatCount(ValueAnimator.INFINITE);
        breatheAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        breatheAnimator.start();
    }

    private void stopLiveClock() {
        timeHandler.removeCallbacks(timeRunnable);
    }

    //  Helper: Get timezone based on screen location
    private TimeZone getTimeZoneForLocation(String location) {
        if (location == null || location.isEmpty()) {
            return null;  // Use device default
        }
        
        // Import Utils to access cityTimeZones
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
                android.util.Log.d("AdvertWatching", " Timezone for '" + location + "': " + entry.getValue());
                return TimeZone.getTimeZone(entry.getValue());
            }
        }
        
        android.util.Log.d("AdvertWatching", " No timezone match for '" + location + "', using device default");
        return null;
    }

    private void startMediaRotation(List<MediaModel> mediaList, Context context) {
        this.mediaList = mediaList;
        this.currentIndex = 0;

        android.util.Log.e("AdvertWatching", " startMediaRotation() CALLED - Total items: " + (mediaList == null ? "0" : mediaList.size()));

        if (mediaSwitcher != null) {
            handler.removeCallbacks(mediaSwitcher);
        }
        mediaSwitcher = new Runnable() {
            @Override
            public void run() {
                if (mediaList == null || mediaList.isEmpty()) {
                    android.util.Log.e("AdvertWatching", " Media list is empty or null");
                    return;
                }
                android.util.Log.e("AdvertWatching", " mediaSwitcher.run() - currentIndex=" + currentIndex + ", total=" + mediaList.size());
                int currentHour = Integer.parseInt(getCurrentHourFormatted());

                // ...existing mediaSwitcher logic...
                android.util.Log.d("AdvertWatching", "▶️  Playing item " + (currentIndex + 1) + "/" + mediaList.size() + " (Hour: " + currentHour + ")");
                if (currentIndex < mediaList.size()) {
                    MediaModel media = mediaList.get(currentIndex);
                    android.util.Log.d("AdvertWatching", "   Type: " + media.getType() + ", Duration: " + (media.getDurationInMillis() / 1000) + "s");
                    if (media.getAdvertId() != null && !media.getAdvertId().isEmpty()) {
                        android.util.Log.d("AdvertWatching", "   Ad ID: " + media.getAdvertId());
                    }
                }

                // Determine the view currently visible so we can slide it out
                View currentVisible = null;
                if (adImageView.getVisibility() == View.VISIBLE) currentVisible = adImageView;
                else if (adPlayerView.getVisibility() == View.VISIBLE) currentVisible = adPlayerView;
                else if (weatherLayout.getVisibility() == View.VISIBLE) currentVisible = weatherLayout;
                else if (newsLayout.getVisibility() == View.VISIBLE) currentVisible = newsLayout;
                else if (webContentView != null && webContentView.getVisibility() == View.VISIBLE) currentVisible = webContentView;
                else if (socialFeedLayout != null && socialFeedLayout.getVisibility() == View.VISIBLE) currentVisible = socialFeedLayout;

                releaseExoPlayer();
                adPlayerView.setVisibility(View.GONE);
                if (currentVisible == adPlayerView) currentVisible = null;
                if (currentVisible != adImageView)   adImageView.setVisibility(View.GONE);
                if (currentVisible != weatherLayout) weatherLayout.setVisibility(View.GONE);
                if (currentVisible != newsLayout)    newsLayout.setVisibility(View.GONE);
                if (webContentView != null && currentVisible != webContentView) webContentView.setVisibility(View.GONE);
                if (socialFeedLayout != null && currentVisible != socialFeedLayout) socialFeedLayout.setVisibility(View.GONE);

                // Default: hide logo/QR for every slide; only ads (IMAGE/VIDEO) will re-show them
                logoImage.setVisibility(View.GONE);
                qrImage.setVisibility(View.GONE);

                MediaModel media = mediaList.get(currentIndex);
                if (DataHolder.getInstance().targetHoursFlag == 1) {
                    String type = media.getType();
                    if (!stringToList(media.getTargetHours()).contains(currentHour)
                            && !type.equals("weather") && !type.equals("news")) {
                        android.util.Log.d("AdvertWatching", "   ⏭️  Skipping - target hours don't match current hour");
                        currentIndex = (currentIndex + 1) % mediaList.size();
                        handler.post(this);
                        return;
                    }
                }
                long durationMs = media.getDurationInMillis();
                // Default to 6 seconds if the ad has no display time set
                if (durationMs <= 0) { durationMs = 6000; }

                if (media.getType().equals("IMAGE") || media.getType().equals("")) {
                    android.util.Log.d("AdvertWatching", "   ️  Displaying IMAGE");
                    updateDebugText("Item " + (currentIndex + 1) + "/" + mediaList.size() + " | IMAGE | " + (durationMs/1000) + "s");
                    waitingLogo.setVisibility(View.GONE);
                    Glide.with(getApplicationContext()).load(media.getUrl()).into(adImageView);
                    displayText.setText(media.getDisplayText());
                    // Only show the QR code when the ad actually has a target URL
                    String targetUrlImg = media.getInfo();
                    if (targetUrlImg != null && !targetUrlImg.trim().isEmpty()) {
                        QRCodeMaker(targetUrlImg);
                        qrImage.setVisibility(View.VISIBLE);
                    } else {
                        qrImage.setVisibility(View.GONE);
                    }
                    logoImage.setVisibility(View.VISIBLE);
                    if (DataHolder.getInstance().displayFlag == 1) {
                        displayText.setSelected(true);
                        displayText.setVisibility(View.VISIBLE);
                    }
                    slideTransition(adImageView, currentVisible);
                    handler.postDelayed(this, durationMs);
                    saveAndSendImpression(media, durationMs, context);

                } else if (media.getType().equals("VIDEO")) {
                    android.util.Log.d("AdvertWatching", "    Playing VIDEO");
                    updateDebugText("Item " + (currentIndex + 1) + "/" + mediaList.size() + " | VIDEO | " + (durationMs/1000) + "s");
                    waitingLogo.setVisibility(View.GONE);
                    displayText.setText(media.getDisplayText());
                    adPlayerView.setVisibility(View.INVISIBLE);
                    logoImage.setVisibility(View.VISIBLE);
                    // Only show the QR code when the ad actually has a target URL
                    String targetUrlVid = media.getInfo();
                    if (targetUrlVid != null && !targetUrlVid.trim().isEmpty()) {
                        QRCodeMaker(targetUrlVid);
                        qrImage.setVisibility(View.VISIBLE);
                    } else {
                        qrImage.setVisibility(View.GONE);
                    }
                    if (DataHolder.getInstance().displayFlag == 1) {
                        displayText.setSelected(true);
                        displayText.setVisibility(View.VISIBLE);
                    }
                    setupExoPlayer(media.getUrl(), null, null);
                    handler.postDelayed(this, durationMs);
                    saveAndSendImpression(media, durationMs, context);

                } else if (media.getType().equals("weather")) {
                    android.util.Log.d("AdvertWatching", "   ️  Showing WEATHER");
                    updateDebugText("Item " + (currentIndex + 1) + "/" + mediaList.size() + " | WEATHER | 10s");
                    waitingLogo.setVisibility(View.GONE);
                    logoImage.setVisibility(View.GONE);
                    qrImage.setVisibility(View.GONE);
                    displayText.setVisibility(View.GONE);
                    slideTransition(weatherLayout, currentVisible);
                    handler.postDelayed(this, durationMs);

                } else if (media.getType().equals("news")) {
                    android.util.Log.d("AdvertWatching", "    Showing NEWS");
                    updateDebugText("NEWS Slide " + (newsIndex + 1) + " | 10s");
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
                                getBackupNews = new ArrayList<>(rss);
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
                            getNews = new ArrayList<>(getBackupNews);
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
                        if (newsSource != null) {
                            if (src != null && !src.isEmpty()) {
                                newsSource.setText(src);
                                newsSource.setVisibility(View.VISIBLE);
                            } else {
                                newsSource.setVisibility(View.GONE);
                            }
                        }
                        newsIndex++;
                    }

                    newsHeader.setVisibility(View.VISIBLE);
                    newsDesc.setVisibility(View.VISIBLE);
                    newsImg.setVisibility(View.VISIBLE);
                    
                    //  Apply Ken Burns zoom animation to hero image
                    if (newsImg != null) {
                        newsImg.clearAnimation(); // Clear any previous animation
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

                } else if (media.getType().equals("LIVE_STREAM")) {
                    android.util.Log.d("AdvertWatching", "   LIVE_STREAM: " + media.getUrl());
                    updateDebugText("Item " + (currentIndex + 1) + "/" + mediaList.size() + " | LIVE_STREAM | " + (durationMs / 1000) + "s");
                    waitingLogo.setVisibility(View.GONE);
                    adPlayerView.setVisibility(View.INVISIBLE);
                    logoImage.setVisibility(View.VISIBLE);
                    qrImage.setVisibility(View.GONE);
                    displayText.setVisibility(View.GONE);
                    String liveUrl = media.getUrl();
                    if (liveUrl != null && !liveUrl.isEmpty()) {
                        setupExoPlayer(liveUrl, null, null);
                    } else {
                        android.util.Log.w("AdvertWatching", "⚠️ LIVE_STREAM has no URL, advancing rotation");
                    }
                    // Duration drives rotation; STATE_READY won't reschedule for live (C.TIME_UNSET)
                    handler.postDelayed(this, durationMs);
                    saveAndSendImpression(media, durationMs, context);

                } else if (media.getType().equals("WEB_CONTENT")) {
                    android.util.Log.d("AdvertWatching", "   WEB_CONTENT: " + media.getUrl());
                    updateDebugText("Item " + (currentIndex + 1) + "/" + mediaList.size() + " | WEB_CONTENT | " + (durationMs / 1000) + "s");
                    waitingLogo.setVisibility(View.GONE);
                    logoImage.setVisibility(View.GONE);
                    qrImage.setVisibility(View.GONE);
                    displayText.setVisibility(View.GONE);
                    if (webContentView != null) {
                        webContentView.loadUrlSafe(media.getUrl());
                        slideTransition(webContentView, currentVisible);
                    }
                    handler.postDelayed(this, durationMs);
                    saveAndSendImpression(media, durationMs, context);

                } else if (media.getType().equals("SOCIAL_FEED")) {
                    android.util.Log.d("AdvertWatching", "   SOCIAL_FEED: platform=" + media.getSocialPlatform() + " hashtag=" + media.getSocialHashtag());
                    updateDebugText("Item " + (currentIndex + 1) + "/" + mediaList.size() + " | SOCIAL_FEED | " + (durationMs / 1000) + "s");
                    waitingLogo.setVisibility(View.GONE);
                    logoImage.setVisibility(View.GONE);
                    qrImage.setVisibility(View.GONE);
                    displayText.setVisibility(View.GONE);
                    if (socialFeedLayout != null) {
                        String platform = media.getSocialPlatform();
                        String hashtag = media.getSocialHashtag();
                        if (socialPlatformLabel != null) {
                            socialPlatformLabel.setText(platform != null ? platform : "Social");
                        }
                        if (socialHashtagLabel != null) {
                            socialHashtagLabel.setText(hashtag != null ? "#" + hashtag : "");
                        }
                        SocialMediaManager.SocialPlatform p =
                                "INSTAGRAM".equalsIgnoreCase(platform)
                                        ? SocialMediaManager.SocialPlatform.INSTAGRAM
                                        : SocialMediaManager.SocialPlatform.TWITTER;
                        socialMediaManager.fetchMixedFeed(
                                hashtag != null ? hashtag : "",
                                new SocialMediaManager.SocialPlatform[]{p},
                                8,
                                new SocialMediaManager.SocialFeedListener() {
                                    @Override
                                    public void onPostsLoaded(List<SocialMediaManager.SocialPost> posts) {
                                        if (!isFinishing() && !isDestroyed() && socialPostsRecyclerView != null) {
                                            socialPostsRecyclerView.setAdapter(new SocialPostAdapter(posts));
                                        }
                                    }
                                    @Override
                                    public void onError(String error) {
                                        android.util.Log.e("AdvertWatching", "Social feed error: " + error);
                                    }
                                }
                        );
                        slideTransition(socialFeedLayout, currentVisible);
                    }
                    handler.postDelayed(this, durationMs);
                    saveAndSendImpression(media, durationMs, context);

                } else {
                    android.util.Log.w("AdvertWatching", "⚠️ Unknown media type: " + media.getType() + ", advancing rotation");
                    handler.postDelayed(this, durationMs);
                }

                currentIndex = (currentIndex + 1) % mediaList.size();
            }
        }

        ;
        handler.removeCallbacks(mediaSwitcher);
        android.util.Log.e("AdvertWatching", " About to post mediaSwitcher to handler");
        handler.post(mediaSwitcher);
        android.util.Log.e("AdvertWatching", " mediaSwitcher posted to handler");

        handler.removeCallbacks(refreshRunnable);

        if (isInternetAvailable()) {
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

        exoPlayer = new ExoPlayer.Builder(AdvertWatching.this).build();
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
        impression.currency = media.getCurrency();
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
             /*   AdDatabase adDatabase = AdDatabase.getInstance(context);
                List<ImpressionEntity> impressions = adDatabase.impDao().getAllImpressions();*/
                //for (ImpressionEntity impression1 : impressions) {


                //}
            }
        }).start();
    }

    private void logAnimation() {
        rotate = new RotateAnimation(
                0f, 360f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );

        rotate.setDuration(1000); // سرعة اللفة
        rotate.setRepeatCount(Animation.INFINITE);
        rotate.setInterpolator(new LinearInterpolator());
    }

    private String getCurrentHourFormatted() {
        return new SimpleDateFormat("H", Locale.getDefault()).format(new Date());
    }

    private boolean isInternetAvailable(Context context) {
        return isInternetAvailable();
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

    public void setNoAdsLogoAnimation() {
        handlerLogo = new Handler();
        runnableLogo = new Runnable() {
            @Override
            public void run() {
                float x = noAdsLogo.getX() + dx;
                float y = noAdsLogo.getY() + dy;

                // حدود الشاشة
                int screenWidth = ((View) noAdsLogo.getParent()).getWidth();
                int screenHeight = ((View) noAdsLogo.getParent()).getHeight();

                // لو الوجو خبط في اليمين أو الشمال
                if (x <= 0 || x + noAdsLogo.getWidth() >= screenWidth) {
                    dx = -dx;
                }

                // لو الوجو خبط فوق أو تحت
                if (y <= 0 || y + noAdsLogo.getHeight() >= screenHeight) {
                    dy = -dy;
                }

                noAdsLogo.setX(x);
                noAdsLogo.setY(y);

                handlerLogo.postDelayed(this, 16); // ~60 FPS
            }
        };
        noAdsLogo.post(() -> handlerLogo.post(runnableLogo));
    }

    /**
     * Slide the new view in from the right while fading out the old view to the left.
     * Works at any screen size — offset is proportional to display density.
     */
    private void slideTransition(final View showView, final View hideView) {
        float offsetPx = getResources().getDisplayMetrics().density * 60; // 60dp in px

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

    /** Simple crossfade kept for video player internal transitions. */
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
     * Updates debug overlay with current playback status.
     * Shows: current media type, index, ads loaded, etc.
     * TODO: Remove this before production release
     */
    private void updateDebugText(String message) {
        String timestamp = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
        String fullMsg = timestamp + " | " + message;

        // Always log to logcat
        android.util.Log.e("AdvertWatching", " " + fullMsg);

        // Also try to update overlay if it exists
        if (debugOverlay != null) {
            runOnUiThread(() -> {
                try {
                    debugOverlay.setText(fullMsg);
                    android.util.Log.e("AdvertWatching", " Overlay updated: " + fullMsg);
                } catch (Exception e) {
                    android.util.Log.e("AdvertWatching", " ERROR updating overlay: " + e.getMessage());
                }
            });
        } else {
            android.util.Log.e("AdvertWatching", " WARNING: debugOverlay is NULL, can't update UI");
        }
    }
}
