package com.adjaba.activities;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
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

import com.adjaba.mqtt.MqttPlayerManager;
import com.adjaba.mqtt.ResponseModel;
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
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
    LinearLayout weatherLayout;
    ImageView waitingLogo, newsImg;
    int weatherCurrent;
    Runnable runnableLogo;
    private int currentIndex = 0;
    private ExoPlayer exoPlayer;
    float dx = 6f; // سرعة الاتجاه الأفقي
    float dy = 6f; // سرعة الاتجاه الرأسي
    private MqttPlayerManager mqttManager;

    private Handler handler1 = new Handler(Looper.getMainLooper());
    private Player.Listener playerListener = null;
    private Handler handler = new Handler();
    ImageView logoImage;
    Map<String, List<Integer>> advertHoursMap; // المفتاح advertId، والقيمة الساعات اللي يتعرض فيها الإعلان
    RotateAnimation rotate;
    ResponseModel responseModel;
    ShimmerFrameLayout shimmer;
    private RetrofitBuilder retrofitBuilder = new RetrofitBuilder();
    private ImageView adImageView, noAdsLogo;
    private ImageView weatherImg;
    TextView tvTemp, tvLoc, tvStatus, timeNow, wind, rain, humadity, progressText;
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
    MediaModel updatedMedia;
    private Runnable timeRunnable;
    int qrImageDimension;
    ImageView qrImage;
    List<RssItem> getNews, getBackupNews;
    private Runnable refreshRunnable;
    String mediaFormat = "";
    TextView displayText, newsHeader, newsDesc, newsTitle;
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
        getBackupNews = new ArrayList<>();
        orient = DataHolder.getInstance().orient.toLowerCase();
        qrImage = findViewById(R.id.qrCodeImage);
        logoImage = findViewById(R.id.logoImage);
        adImageView = findViewById(R.id.adImageView);
        newsHeader = findViewById(R.id.main_header);
        tvStatus = findViewById(R.id.currentStatus);
        newsTitle = findViewById(R.id.newsTitle);
        newsImg = findViewById(R.id.news_img);
        timeNow = findViewById(R.id.timeNow);
        shimmer = findViewById(R.id.shimmer);
        waitingLogo = findViewById(R.id.waitingLogo);
        tvTemp = findViewById(R.id.weatherTemp);
        newsDesc = findViewById(R.id.news_details);
        if (getResources().getConfiguration().smallestScreenWidthDp < 600) {
            newsHeader.setTextSize(20);
            newsDesc.setTextSize(18);
            newsTitle.setTextSize(30);
            timeNow.setTextSize(50);
            tvTemp.setTextSize(25);
            tvStatus.setTextSize(15);
        }
        if ("landscape".equalsIgnoreCase(orient)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            prefs.edit().remove("data_loaded").apply();

        } else if ("portrait".equalsIgnoreCase(orient)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            prefs.edit().remove("data_loaded").apply();

        }
        displayText = findViewById(R.id.displayText);
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
        mqttManager = new MqttPlayerManager(this);

        //mqttManager.connect(screenId);
        Log.d("sayed_MQTT", "json");

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

        boolean isTV = getPackageManager().hasSystemFeature("android.software.leanback");
        int percent = 0;
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            percent = 12;
        } else {
            percent = isTV ? 12 : 20;
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

                Executors.newSingleThreadExecutor().execute(() -> {
                    AdDatabase db = AdDatabase.getInstance(getApplicationContext());
                    db.adDao().deleteAllAds(); // مسح الإعلانات القديمة

                    // بعد ما تخلص المسح وتأكدت، نرجع للـ UI thread لتحديث الداتا
                    handler.post(() -> {
                        getAds(0);
                    });
                });

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
            newsHandler = new NewsHandler(newsIndex);
            newsHandler.load(DataHolder.getInstance().location, context, (rss, i) -> {
                if (i != 1) {
                    newsIndex = 1;
                }
                getNews = rss;
                getBackupNews = rss;
                return Unit.INSTANCE;
            }, bar -> {
                if (bar == 1) shimmer.stopShimmer();
                shimmer.setVisibility(View.GONE);
                return Unit.INSTANCE;
            });
            if (DataHolder.getInstance().isData == 5) {
                Log.d("sayed-99", "next");
                weatherLayout.setVisibility(View.VISIBLE);
                getWeather(location, context);
            } else {
                Log.d("sayed-99", "next11");
                getWeather(location, context);
                startMediaRotation(insertWeatherEveryThreeAds(DataHolder.getInstance().allAds), context);

            }


            prefs.edit().putBoolean("data_loaded", true).apply();
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
                        getUrl(response.body().get(i).contractId, response.body().get(i).adContractData.targetGender, response.body().get(i).adContractData.targetAgeGroup, response.body().get(i).currency, response.body().get(i).maxBid, i, listToString(response.body().get(i).adContractData.targetHours),
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

    private void getUrl(String contractId, List<String> genderGroup, List<String> ageGroup, String currency, int maxBid, int serverOrder, String targetHours, String txtTop, String txtRight, String txtLeft, String info, String advertId, String screenId, String path, String type, int[] loadedCount, int totalCount, int duration, Context context, int flag, Runnable onComplete) {
        if (path == null || path.isEmpty()) {
            return;
        }
        retrofitBuilder.apiCalls().getUrl("Bearer " + AuthManager.getToken(this), path).enqueue(new Callback<VideoImageModel>() {
            @Override
            public void onResponse(Call<VideoImageModel> call, Response<VideoImageModel> response) {
                try {

                    if (response.isSuccessful() && response.body() != null) {
                        String resolvedUrl = response.body().url;


                        String extension;
                        try {
                            int start = resolvedUrl.lastIndexOf('.') + 1;
                            int end = resolvedUrl.contains("?") ? resolvedUrl.indexOf("?") : resolvedUrl.length();
                            extension = resolvedUrl.substring(start, end);
                        } catch (Exception e) {
                            extension = "mp4"; // fallback
                        }

                        String fileName = UUID.randomUUID().toString() + "." + extension;
                        List<MediaModel> newMediaM = new ArrayList<>();
                        // ✅ 2. تحميل الملف وحفظه في Room
                        Executors.newSingleThreadExecutor().execute(() -> {
                            String localPath = downloadFileToInternalStorage(context, resolvedUrl, fileName);
                            if (localPath != null) {
                                if (Objects.equals(path, "") || isImage(path)) {
                                    mediaFormat = "Image";
                                } else if (isVideo(path)) {
                                    mediaFormat = "Video";
                                }
                                // يمكنك تعديل القيم حسب ما هو متوفر
                                AdEntity ad = new AdEntity(
                                        advertId, genderGroup, ageGroup,
                                        mediaFormat.toUpperCase(), // format
                                        localPath,
                                        txtTop, // textTop
                                        info, // textBottom
                                        txtLeft, // textLeft
                                        txtRight, // textRight
                                        duration * 1000,
                                        "Landscape", // orientation مؤقت
                                        screenId,  // مؤقت
                                        contractId, targetHours, serverOrder, currency, maxBid
                                );

                                AdDatabase db = AdDatabase.getInstance(context);
                                db.adDao().insertAd(ad);

                            }
                            if (onComplete != null) {
                                new Handler(Looper.getMainLooper()).post(onComplete); // نادِ الكولباك لكل إعلان فوراً بعد انتهاء تحميله
                            }
                        });
                    } else {
                    }

                } catch (RuntimeException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onFailure(Call<VideoImageModel> call, Throwable t) {
                if (onComplete != null) {
                    new Handler(Looper.getMainLooper()).post(onComplete);
                }
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
        int count = 0;
        for (MediaModel media : originalList) {
            newList.add(media);
            count++;
            if (originalList.size() > 1) {
                if (count == originalList.size()) {
                    MediaModel weather = new MediaModel("", "", 0, "weather", "", 10000, "", "", "", "", "");
                    newList.add(weather);
                    count = 0;
                }
            } else {
                MediaModel weather = new MediaModel("", "", 0, "weather", "", 10000, "", "", "", "", "");
                newList.add(weather);
            }
        }
        progressBar.setVisibility(View.GONE);
        return newList;
    }

    void getWeather(String loc, Context context) {

        retrofitBuilder.apiCalls2().getWeather(Config.weatherKey, loc).enqueue(new Callback<WeatherModel>() {
            @Override
            public void onResponse(Call<WeatherModel> call, Response<WeatherModel> response) {
                if (response.isSuccessful()) {
                    if (!isFinishing() && !isDestroyed()) {
                        Glide.with(AdvertWatching.this).load("https:" + response.body().current.condition.icon).into(weatherImg);

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
                    humadity.setText(response.body().current.humidity + "");
                    wind.setText(response.body().current.wind_kph + "");
                    tvLoc.setText(DataHolder.getInstance().location);
                    tvTemp.setText(Math.round(response.body().current.temp_c) + "°C");
                    tvStatus.setText(response.body().current.condition.text);

                } else {
                }
                //progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<WeatherModel> call, Throwable t) {

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
                String currentTime = new SimpleDateFormat("hh:mm a", Locale.getDefault())
                        .format(new Date());
                timeTextView.setText(currentTime);

                // إعادة التحديث كل ثانية
                timeHandler.postDelayed(this, 1000);
            }
        };

        // بدء الساعة
        timeHandler.post(timeRunnable);
    }

    private void stopLiveClock() {
        timeHandler.removeCallbacks(timeRunnable);
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
                MediaModel nn = updateAppropriateAds();
                if (nn != null) {
                    Log.d("sayed-99", nn.getAdvertId());

                }

                if (mediaList == null || mediaList.isEmpty()) return;
                int currentHour = Integer.parseInt(getCurrentHourFormatted());
                adImageView.setVisibility(View.GONE);
                adPlayerView.setVisibility(View.GONE);
                weatherLayout.setVisibility(View.GONE);
                releaseExoPlayer();
                Animation inAnim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.enter_from_right);
                Animation outAnim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.exit_to_left);
                //int currentHour = Integer.parseInt(getCurrentHourFormatted());
                MediaModel media;
                if (nn != null) {
                    updatedMedia=null;
                    media = nn;

                } else {
                    media = mediaList.get(currentIndex);
                }

                if (DataHolder.getInstance().targetHoursFlag == 1) {
                    if (!stringToList(media.getTargetHours()).contains(currentHour) && !media.getType().equals("weather")) {

                        currentIndex = (currentIndex + 1) % mediaList.size();
                        handler.post(this);
                    }
                }
                long durationMs = media.getDurationInMillis();


                if (media.getType().equals("IMAGE") || media.getType().equals("")) {
                    waitingLogo.setVisibility(View.GONE);
                    Glide.with(getApplicationContext()).load(media.getUrl()).into(adImageView);
                    QRCodeMaker(media.getInfo());
                    displayText.setText(media.getDisplayText());
                    adPlayerView.setVisibility(View.GONE);
                    weatherLayout.setVisibility(View.GONE);
                    qrImage.setVisibility(View.VISIBLE);
                    logoImage.setVisibility(View.VISIBLE);
                    adImageView.setVisibility(View.VISIBLE);

                    if (DataHolder.getInstance().displayFlag == 1) {
                        displayText.setSelected(true);
                        displayText.setVisibility(View.VISIBLE);
                    }
                    crossfade(adImageView, adPlayerView, 1000);
                    handler.postDelayed(this, durationMs);
                    saveAndSendImpression(media, durationMs, context);
                } else if (media.getType().equals("VIDEO")) {
                    waitingLogo.setVisibility(View.GONE);
                    weatherLayout.setVisibility(View.GONE);
                    displayText.setText(media.getDisplayText());
                    adPlayerView.setVisibility(View.INVISIBLE);
                    logoImage.setVisibility(View.VISIBLE);
                    QRCodeMaker(media.getInfo());
                    qrImage.setVisibility(View.VISIBLE);
                    if (DataHolder.getInstance().displayFlag == 1) {
                        displayText.setSelected(true);
                        displayText.setVisibility(View.VISIBLE);
                    }
                    setupExoPlayer(media.getUrl(), inAnim, outAnim);
                    crossfade(adPlayerView, adImageView, 1000);
                    handler.postDelayed(this, durationMs);
                    saveAndSendImpression(media, durationMs, context);
                } else if (media.getType().equals("weather")) {
                    findViewById(R.id.newsFrame).setVisibility(View.VISIBLE);
                    waitingLogo.setVisibility(View.GONE);
                    if (getNews.size() - 1 == newsIndex) {
                        Utils.INSTANCE.getNewsList().clear();
                        getNews.clear();
                    }
                    if (Utils.INSTANCE.getNewsList().size() == 0) {
                        shimmer.startShimmer();
                        shimmer.setVisibility(View.VISIBLE);
                        newsHandler = new NewsHandler(newsIndex);
                        try {
                            newsHandler.load(DataHolder.getInstance().location, context, (rss, i) -> {
                                if (i != 1) {
                                    newsIndex = 1;
                                }
                                getNews = rss;
                                shimmer.stopShimmer();
                                shimmer.setVisibility(View.GONE);
                                return Unit.INSTANCE;
                            }, bar -> {
                                if (bar == 1) shimmer.stopShimmer();
                                shimmer.setVisibility(View.GONE);
                                //progressBar.setVisibility(View.GONE);
                                return Unit.INSTANCE;
                            });
                        } catch (Exception e) {
                            getNews = getBackupNews;
                        }
                    }
                    if (Utils.INSTANCE.getNewsList().size() > 0) {

                        if (getNews.get(newsIndex).getThumbnailUrl().endsWith(".gif")) {
                            Glide.with(context)
                                    .asGif()
                                    .load(getNews.get(newsIndex).getThumbnailUrl())
                                    .into(newsImg);

                        } else {

                            Glide.with(getApplicationContext()).load(getNews.get(newsIndex).getThumbnailUrl()).into(newsImg);
                        }
                        newsHeader.setText(getNews.get(newsIndex).getTitle());
                        newsDesc.setText(getNews.get(newsIndex).getDescription());
                        newsIndex++;
                    } else {
                        findViewById(R.id.newsFrame).setVisibility(View.GONE);
                    }
                    newsHeader.setVisibility(View.VISIBLE);
                    newsDesc.setVisibility(View.VISIBLE);
                    newsImg.setVisibility(View.VISIBLE);
                    adImageView.setVisibility(View.GONE);
                    adPlayerView.setVisibility(View.GONE);
                    logoImage.setVisibility(View.GONE);
                    qrImage.setVisibility(View.GONE);
                    displayText.setVisibility(View.GONE);
                    weatherLayout.setVisibility(View.VISIBLE);
                    crossfade(weatherLayout, adImageView, 1000);


                    // الاستمرار في الدوران بعد المدة
                    handler.postDelayed(this, durationMs);
                }

                currentIndex = (currentIndex + 1) % mediaList.size();
            }
        };


        handler.removeCallbacks(mediaSwitcher);
        handler.post(mediaSwitcher);

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
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR));
        }
        return false;
    }

    private MediaModel updateAppropriateAds() {

        mqttManager.setMessageListener(new MqttPlayerManager.MessageListener() {
            @Override
            public void onMessageReceived(String topic, String message) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String fixedMessage = message;
                            if (fixedMessage.startsWith("\"")) {
                                fixedMessage = fixedMessage.substring(1, fixedMessage.length() - 1);
                                fixedMessage = fixedMessage.replace("\\\"", "\"");
                            }
                            JSONObject jsonObject = new JSONObject(fixedMessage);

                            responseModel = new ResponseModel(
                                    jsonObject.getString("storeId"),
                                    jsonObject.getString("timestamp"),
                                    jsonObject.getString("customerCount"),
                                    jsonObject.getString("ageRange"),
                                    jsonObject.getString("gender")
                            );
                            Log.d("fgh", responseModel.getGender());
                            AdDatabase adDatabase = AdDatabase.getInstance(context);
                            AdEntity ad = adDatabase.adDao().getAdByGender(responseModel.getGender());
                            if (ad == null) return;
                            updatedMedia = new MediaModel(ad.contractId, ad.currency
                                    , ad.maxBid, ad.format,
                                    ad.localPath, ad.duration, ad.textBottom
                                    , ad.textTop, "", ad.targetHours, ad.advertId);
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                            }
                        });

                        Log.d("sayed_MQTT", "Topic: " + topic + ", Message: " + message);


                    }
                }).start();
            }
        });
        mqttManager.setConnectionListener(new MqttPlayerManager.ConnectionListener() {
            @Override
            public void onConnectionStatus(boolean isConnected) {
                Log.d("sayed_MQTT", "MQTT Connected: " + isConnected);
            }
        });
        if (!mqttManager.isConnected()) {
            String clientId = "android-client-" + System.currentTimeMillis();
            mqttManager.connect(clientId);
        }
        return updatedMedia;
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


        impression.impressionId = DataHolder.getInstance().screenID + media.getAdvertId() + String.format("%03d", new Random().nextInt(1000));

        impression.advertId = media.getAdvertId();
        impression.amountSettled = false;
        impression.contractId = media.getContractId(); // ممكن تغيرها لو عندك بيانات ديناميكية
        impression.currency = "USD";      // ممكن تغيرها لو عندك بيانات من JSON
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

            if (isInternetAvailable(context)) {

                List<ImpressionEntity> impressions = db.impDao().getAllImpressions();
                for (ImpressionEntity impression1 : impressions) {
                    APIImpression.sendImpression(context, impression1);
                    db.impDao().deleteAdById(impression1.impressionId);

                }
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

    // دالة تحقق اتصال الانترنت (مثال بسيط)
    private boolean isInternetAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
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
                return;
            }
        };
        noAdsLogo.post(() -> handlerLogo.post(runnableLogo));
    }

    private void crossfade(final View showView, final View hideView, long duration) {
        // hideView يختفي تدريجيًا
        hideView.animate()
                .alpha(0f)
                .setDuration(duration)
                .withEndAction(() -> hideView.setVisibility(View.GONE))
                .start();

        // showView يظهر تدريجيًا
        showView.setAlpha(0f);
        showView.setVisibility(View.VISIBLE);
        showView.animate()
                .alpha(1f)
                .setDuration(duration)
                .start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mqttManager != null) {
            mqttManager.disconnect();
        }
        handler.removeCallbacks(mediaSwitcher);
        releaseExoPlayer();
        if (executorService != null) {
            executorService.shutdownNow();
        }
        handler.removeCallbacks(refreshRunnable);
        stopLiveClock();

    }

}
