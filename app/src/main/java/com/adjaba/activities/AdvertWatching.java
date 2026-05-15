package com.adjaba.activities;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.text.SpannableString;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import android.annotation.SuppressLint;
import android.content.Context;
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
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

import com.adjaba.R;
import com.adjaba.activities.viewmodel.APIImpression;
import com.adjaba.activities.viewmodel.DataHolder;
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
    ConstraintLayout weatherLayout;
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
    TextView tvTemp, tvLoc, tvStatus, timeNow, dateNow, wind, rain, humadity, progressText;
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
    // ─────────────────────────────────────────────────────────────────────────
    String mediaFormat = "";
    TextView displayText, newsHeader, newsDesc, newsTitle;
    TextView debugOverlay; // Debug overlay for playback status
    String orient;
    NewsHandler newsHandler;

    @SuppressLint({"MissingInflatedId", "UnsafeOptInUsageError"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_advert_watching);
        constLayout = findViewById(R.id.mainConstLayout);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        SharedPreferences prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        boolean isDataLoaded = prefs.getBoolean("data_loaded", false);
        getNews = new ArrayList<>();
        getBackupNews=new ArrayList<>();
        orient = DataHolder.getInstance().orient.toLowerCase();
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
        if ("landscape".equalsIgnoreCase(orient)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            prefs.edit().remove("data_loaded").apply();

        } else if ("portrait".equalsIgnoreCase(orient)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            prefs.edit().remove("data_loaded").apply();

        }
        displayText = findViewById(R.id.displayText);
        debugOverlay = findViewById(R.id.debugOverlay);
        context = this;
        advertHoursMap = new HashMap<>();
        rain = findViewById(R.id.rain);
        wind = findViewById(R.id.windW);
        humadity = findViewById(R.id.hamudity);
        progressBar = findViewById(R.id.loadBar);
        progressText = findViewById(R.id.progressText);
        weatherCurrent = 3;
        weatherLayout = findViewById(R.id.weatherLayout);
        weatherImg = findViewById(R.id.currentWeatherImg);
        tvLoc = findViewById(R.id.weatherLoc);
        adPlayerView = findViewById(R.id.adPlayerView);
        screenId = DataHolder.getInstance().screenID;
        location = DataHolder.getInstance().location;
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
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            percent = 12;
        }else{
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
            android.util.Log.i("AdvertWatching", "🎬 onCreate() - Initializing playback");
            android.util.Log.i("AdvertWatching", "   isDataLoaded: " + isDataLoaded);
            android.util.Log.i("AdvertWatching", "   DataHolder.allAds: " + (DataHolder.getInstance().allAds == null ? "NULL" : DataHolder.getInstance().allAds.size() + " ads"));

            newsHandler = new NewsHandler(0);
            newsHandler.load(DataHolder.getInstance().location, context, (rss, i) -> {
                getNews = new ArrayList<>(rss);
                getBackupNews = new ArrayList<>(rss);
                newsIndex = 0;
                android.util.Log.d("AdvertWatching", "   📰 News loaded: " + (rss == null ? "0" : rss.size()) + " articles");
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
                android.util.Log.e("AdvertWatching", "🔴 STARTING WEATHER+NEWS ROTATION (no ads)");
                startMediaRotation(infoSlides, context);
            } else {
                android.util.Log.i("AdvertWatching", "✨ Starting playback with " + DataHolder.getInstance().allAds.size() + " ads");
                updateDebugText("Playing " + DataHolder.getInstance().allAds.size() + " ads");
                getWeather(location, context);
                List<MediaModel> rotationList = insertWeatherEveryThreeAds(DataHolder.getInstance().allAds);
                android.util.Log.i("AdvertWatching", "   Total items in rotation: " + (rotationList == null ? "0" : rotationList.size()) + " (ads + weather + news)");
                updateDebugText("Rotation: " + (rotationList == null ? "0" : rotationList.size()) + " items (ads+weather+news)");
                android.util.Log.e("AdvertWatching", "🔴 STARTING AD+WEATHER+NEWS ROTATION with " + (rotationList == null ? 0 : rotationList.size()) + " items");
                startMediaRotation(rotationList, context);
            }


            prefs.edit().putBoolean("data_loaded", true).apply();
            startWeatherAutoRefresh();
            startNewsAutoRefresh();
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
                        contractId, targetHours, serverOrder, currency, maxBid
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
                // After each full ad cycle: weather slide then news slide
                newList.add(new MediaModel("", "", 0, "weather", "", 10000, "", "", "", "", ""));
                newList.add(new MediaModel("", "", 0, "news",    "", 10000, "", "", "", "", ""));
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
                    tvLoc.setText(DataHolder.getInstance().location);
                    tvTemp.setText(Math.round(response.body().current.temp_c) + "°C");
                    tvStatus.setText(response.body().current.condition.text);

                } else {
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
                String currentTime = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(now);
                SpannableString spannable = new SpannableString(currentTime);
                int colon = currentTime.indexOf(':');
                if (colon >= 0) {
                    spannable.setSpan(
                        new ForegroundColorSpan(0xFFE50914),
                        colon, colon + 1,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

                // 🎬 Apply smooth time digit fade animation
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

                    // 🎬 Apply smooth date update animation
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


    private void startMediaRotation(List<MediaModel> mediaList, Context context) {
        this.mediaList = mediaList;
        this.currentIndex = 0;

        android.util.Log.e("AdvertWatching", "🔴🔴🔴 startMediaRotation() CALLED - Total items: " + (mediaList == null ? "0" : mediaList.size()));

        if (mediaSwitcher != null) {
            handler.removeCallbacks(mediaSwitcher);
        }
        mediaSwitcher = new Runnable() {
            @Override
            public void run() {
                if (mediaList == null || mediaList.isEmpty()) {
                    android.util.Log.e("AdvertWatching", "🔴 Media list is empty or null");
                    return;
                }
                android.util.Log.e("AdvertWatching", "🔴 mediaSwitcher.run() - currentIndex=" + currentIndex + ", total=" + mediaList.size());
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

                releaseExoPlayer();
                adPlayerView.setVisibility(View.GONE);
                if (currentVisible == adPlayerView) currentVisible = null;
                if (currentVisible != adImageView)   adImageView.setVisibility(View.GONE);
                if (currentVisible != weatherLayout) weatherLayout.setVisibility(View.GONE);
                if (currentVisible != newsLayout)    newsLayout.setVisibility(View.GONE);

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

                if (media.getType().equals("IMAGE") || media.getType().equals("")) {
                    android.util.Log.d("AdvertWatching", "   🖼️  Displaying IMAGE");
                    updateDebugText("Item " + (currentIndex + 1) + "/" + mediaList.size() + " | IMAGE | " + (durationMs/1000) + "s");
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
                    android.util.Log.d("AdvertWatching", "   🎬 Playing VIDEO");
                    updateDebugText("Item " + (currentIndex + 1) + "/" + mediaList.size() + " | VIDEO | " + (durationMs/1000) + "s");
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
                    android.util.Log.d("AdvertWatching", "   🌤️  Showing WEATHER");
                    updateDebugText("Item " + (currentIndex + 1) + "/" + mediaList.size() + " | WEATHER | 10s");
                    waitingLogo.setVisibility(View.GONE);
                    logoImage.setVisibility(View.GONE);
                    qrImage.setVisibility(View.GONE);
                    displayText.setVisibility(View.GONE);
                    slideTransition(weatherLayout, currentVisible);
                    handler.postDelayed(this, durationMs);

                } else if (media.getType().equals("news")) {
                    android.util.Log.d("AdvertWatching", "   📰 Showing NEWS");
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
                        newsDesc.setText(getNews.get(newsIndex).getDescription());
                        newsIndex++;
                    }

                    newsHeader.setVisibility(View.VISIBLE);
                    newsDesc.setVisibility(View.VISIBLE);
                    newsImg.setVisibility(View.VISIBLE);
                    
                    // 🎬 Apply Ken Burns zoom animation to hero image
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
                }

                currentIndex = (currentIndex + 1) % mediaList.size();
            }
        }

        ;
        handler.removeCallbacks(mediaSwitcher);
        android.util.Log.e("AdvertWatching", "🔴 About to post mediaSwitcher to handler");
        handler.post(mediaSwitcher);
        android.util.Log.e("AdvertWatching", "🔴 mediaSwitcher posted to handler");

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
        android.util.Log.e("AdvertWatching", "🐛 " + fullMsg);

        // Also try to update overlay if it exists
        if (debugOverlay != null) {
            runOnUiThread(() -> {
                try {
                    debugOverlay.setText(fullMsg);
                    android.util.Log.e("AdvertWatching", "🐛 Overlay updated: " + fullMsg);
                } catch (Exception e) {
                    android.util.Log.e("AdvertWatching", "🐛 ERROR updating overlay: " + e.getMessage());
                }
            });
        } else {
            android.util.Log.e("AdvertWatching", "🐛 WARNING: debugOverlay is NULL, can't update UI");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(mediaSwitcher);
        releaseExoPlayer();
        if (executorService != null) {
            executorService.shutdownNow();
        }
        handler.removeCallbacks(refreshRunnable);
        if (weatherRefreshRunnable != null) weatherRefreshHandler.removeCallbacks(weatherRefreshRunnable);
        if (newsRefreshRunnable != null) newsRefreshHandler.removeCallbacks(newsRefreshRunnable);
        stopLiveClock();
        if (breatheAnimator != null) breatheAnimator.cancel();
    }
}
