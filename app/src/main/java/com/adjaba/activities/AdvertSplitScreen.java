package com.adjaba.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;

import com.adjaba.R;
import com.adjaba.activities.viewmodel.APIImpression;
import com.adjaba.activities.viewmodel.DataHolder;
import com.adjaba.content.SecureSignageWebView;
import com.adjaba.models.newmodels.MediaModel;
import com.adjaba.models.newmodels.Current;
import com.adjaba.models.newmodels.WeatherModel;
import com.adjaba.news.NewsHandler;
import com.adjaba.news.RssItem;
import com.adjaba.news.Utils;
import com.adjaba.room.AdDatabase;
import com.adjaba.room.AdEntity;
import com.adjaba.room.ImpressionEntity;
import com.adjaba.utilities.Config;
import com.adjaba.utilities.PlaylistSyncManager;
import com.adjaba.utilities.RetrofitBuilder;
import com.bumptech.glide.Glide;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.PlayerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.Executors;

import kotlin.Unit;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Split-screen orientation:
 *   Left 70%  — rotating ads (IMAGE / VIDEO / WEB_CONTENT / LIVE_STREAM / weather)
 *   Right 30% — social feed items (rotated if multiple) or news headlines as fallback
 *
 * Two independent Handlers manage the two zones so right-zone timing is never
 * disrupted by left-zone ad durations and vice-versa.
 */
public class AdvertSplitScreen extends AppCompatActivity {

    // ── Left zone ──────────────────────────────────────────────────
    private List<MediaModel> leftList  = new ArrayList<>();
    private int              leftIndex = 0;
    private Handler          leftHandler;
    private Runnable         leftSwitcher;

    // ── Right zone ─────────────────────────────────────────────────
    private List<MediaModel> socialFeedList = new ArrayList<>();
    private int              rightIndex     = 0;
    private List<RssItem>    rightNewsList  = new ArrayList<>();
    private int              newsIndex      = 0;
    private Handler          rightHandler;
    private Runnable         rightSwitcher;
    private NewsHandler      newsHandler;

    // ── ExoPlayer (left zone only) ─────────────────────────────────
    private ExoPlayer       exoPlayer;
    private Player.Listener playerListener;

    // ── Common ────────────────────────────────────────────────────
    private String          location;
    private Context         context;
    private RetrofitBuilder retrofitBuilder;

    // ── Auto-refresh intervals ─────────────────────────────────────
    private static final long WEATHER_REFRESH_MS = 15 * 60 * 1000L;
    private static final long NEWS_REFRESH_MS    = 15 * 60 * 1000L;
    private final Handler  weatherRefreshHandler = new Handler(Looper.getMainLooper());
    private final Handler  newsRefreshHandler    = new Handler(Looper.getMainLooper());
    private Runnable       weatherRefreshRunnable, newsRefreshRunnable;

    // ── Playlist sync ──────────────────────────────────────────────
    private Observer<PlaylistSyncManager.PlaylistUpdate> playlistObserver;

    // ── Live clock ─────────────────────────────────────────────────
    private final Handler  timeHandler  = new Handler(Looper.getMainLooper());
    private Runnable       timeRunnable;

    // ── Views – left zone ──────────────────────────────────────────
    private ImageView     adImageView, logoImage, waitingLogo;
    private PlayerView    adPlayerView;
    private SecureSignageWebView webContentView;
    private ViewGroup     weatherLayout;
    private ImageView     weatherImg;
    private TextView      tvTemp, tvLoc, tvStatus, timeNow, dateNow;
    private TextView      wind, humadity, rain, pressure, displayText;

    // ── Views – right zone ─────────────────────────────────────────
    private FrameLayout  socialFeedLayout;
    private TextView     socialPlatformLabel, socialHashtagLabel;
    private SecureSignageWebView socialFeedWebView;
    private LinearLayout newsRightLayout;
    private TextView     newsRightHeadline, newsRightDesc, newsRightSource;

    // ══════════════════════════════════════════════════════════════
    // Lifecycle
    // ══════════════════════════════════════════════════════════════

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_advert_split_screen);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        context         = this;
        retrofitBuilder = new RetrofitBuilder();
        leftHandler     = new Handler(Looper.getMainLooper());
        rightHandler    = new Handler(Looper.getMainLooper());

        bindViews();

        location = DataHolder.getInstance().location;
        if (tvLoc != null && location != null) tvLoc.setText(location);

        // Double-tap logo to exit
        logoImage.setOnClickListener(new View.OnClickListener() {
            long lastClick = 0;
            @Override public void onClick(View v) {
                long now = System.currentTimeMillis();
                if (now - lastClick < 350) finish();
                lastClick = now;
            }
        });

        startLiveClock();
        loadNews();
        loadAndStartPlayback();
        startWeatherAutoRefresh();
        startNewsAutoRefresh();
        registerPlaylistSyncReceiver();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (leftHandler   != null && leftSwitcher   != null) leftHandler.removeCallbacks(leftSwitcher);
        if (rightHandler  != null && rightSwitcher  != null) rightHandler.removeCallbacks(rightSwitcher);
        if (timeHandler   != null && timeRunnable   != null) timeHandler.removeCallbacks(timeRunnable);
        if (weatherRefreshHandler != null && weatherRefreshRunnable != null)
            weatherRefreshHandler.removeCallbacks(weatherRefreshRunnable);
        if (newsRefreshHandler != null && newsRefreshRunnable != null)
            newsRefreshHandler.removeCallbacks(newsRefreshRunnable);
        releaseExoPlayer();
        if (webContentView   != null) webContentView.cleanup();
        if (socialFeedWebView != null) socialFeedWebView.cleanup();
    }

    // ══════════════════════════════════════════════════════════════
    // View binding
    // ══════════════════════════════════════════════════════════════

    private void bindViews() {
        waitingLogo             = findViewById(R.id.waitingLogo);
        adImageView             = findViewById(R.id.adImageView);
        adPlayerView            = findViewById(R.id.adPlayerView);
        webContentView          = findViewById(R.id.webContentView);
        weatherLayout           = findViewById(R.id.weatherLayout);
        weatherImg              = findViewById(R.id.currentWeatherImg);
        tvTemp                  = findViewById(R.id.weatherTemp);
        tvLoc                   = findViewById(R.id.weatherLoc);
        tvStatus                = findViewById(R.id.currentStatus);
        timeNow                 = findViewById(R.id.timeNow);
        dateNow                 = findViewById(R.id.dateNow);
        wind                    = findViewById(R.id.windW);
        humadity                = findViewById(R.id.hamudity);
        rain                    = findViewById(R.id.rain);
        pressure                = findViewById(R.id.pressure);
        logoImage               = findViewById(R.id.logoImage);
        displayText             = findViewById(R.id.displayText);
        socialFeedLayout        = findViewById(R.id.socialFeedLayout);
        socialPlatformLabel     = findViewById(R.id.socialPlatformLabel);
        socialHashtagLabel      = findViewById(R.id.socialHashtagLabel);
        socialFeedWebView       = findViewById(R.id.socialFeedWebView);
        newsRightLayout         = findViewById(R.id.newsRightLayout);
        newsRightHeadline       = findViewById(R.id.newsRightHeadline);
        newsRightDesc           = findViewById(R.id.newsRightDesc);
        newsRightSource         = findViewById(R.id.newsRightSource);
    }

    // ══════════════════════════════════════════════════════════════
    // Playback startup
    // ══════════════════════════════════════════════════════════════

    private void loadAndStartPlayback() {
        List<MediaModel> allAds = DataHolder.getInstance().allAds;

        leftList.clear();
        socialFeedList.clear();

        if (allAds != null) {
            for (MediaModel m : allAds) {
                if ("SOCIAL_FEED".equals(m.getType())) {
                    socialFeedList.add(m);
                } else {
                    leftList.add(m);
                }
            }
        }

        List<MediaModel> rotation = buildLeftRotation(leftList);
        if (rotation.isEmpty()) {
            // Nothing to play: show weather-only slide until playlist syncs
            rotation = buildWeatherOnlyList();
        }

        getWeather(location);
        startLeftRotation(rotation);
        startRightRotation();
    }

    /** Appends a weather slide at the end of each complete ad cycle for the left zone. */
    private List<MediaModel> buildLeftRotation(List<MediaModel> ads) {
        List<MediaModel> result = new ArrayList<>();
        if (ads != null) result.addAll(ads);
        if (DataHolder.getInstance().weatherFlag == 1) {
            result.add(new MediaModel("", "", 0, "weather", "", 8000, "", "", "", "", ""));
        }
        return result;
    }

    private List<MediaModel> buildWeatherOnlyList() {
        List<MediaModel> list = new ArrayList<>();
        list.add(new MediaModel("", "", 0, "weather", "", 10000, "", "", "", "", ""));
        return list;
    }

    // ══════════════════════════════════════════════════════════════
    // Left zone rotation
    // ══════════════════════════════════════════════════════════════

    private void startLeftRotation(List<MediaModel> list) {
        leftList  = list;
        leftIndex = 0;
        if (leftSwitcher != null) leftHandler.removeCallbacks(leftSwitcher);

        leftSwitcher = new Runnable() {
            @Override
            public void run() {
                if (leftList == null || leftList.isEmpty()) return;
                if (leftIndex >= leftList.size()) leftIndex = 0;

                MediaModel media = leftList.get(leftIndex);
                String     type  = media.getType() != null ? media.getType() : "";
                long       durMs = media.getDurationInMillis() > 0 ? media.getDurationInMillis() : 6000L;
                int        hour  = Integer.parseInt(currentHour());

                // Skip items outside their target hours (weather always shown)
                if (DataHolder.getInstance().targetHoursFlag == 1 && !type.equals("weather")) {
                    List<Integer> targets = stringToList(media.getTargetHours());
                    if (!targets.isEmpty() && !targets.contains(hour)) {
                        leftIndex = (leftIndex + 1) % leftList.size();
                        leftHandler.post(this);
                        return;
                    }
                }

                View prevVisible = currentVisibleLeftView();
                hideAllLeft(prevVisible);

                switch (type) {
                    case "IMAGE":
                    case "": {
                        Glide.with(getApplicationContext()).load(media.getUrl()).into(adImageView);
                        showDisplayText(media.getDisplayText());
                        logoImage.setVisibility(View.VISIBLE);
                        crossFadeIn(adImageView, prevVisible);
                        leftHandler.postDelayed(this, durMs);
                        saveImpression(media, durMs);
                        break;
                    }
                    case "VIDEO": {
                        String url = media.getUrl();
                        showDisplayText(media.getDisplayText());
                        logoImage.setVisibility(View.VISIBLE);
                        if (url != null && !url.isEmpty()) {
                            setupExoPlayer(url, durMs);
                        } else {
                            leftHandler.postDelayed(this, durMs);
                        }
                        saveImpression(media, durMs);
                        break;
                    }
                    case "LIVE_STREAM": {
                        String url = media.getUrl();
                        logoImage.setVisibility(View.VISIBLE);
                        if (url != null && !url.isEmpty()) {
                            setupExoPlayer(url, durMs);
                        } else {
                            android.util.Log.w("AdvertSplitScreen", "⚠️ LIVE_STREAM has no URL, skipping");
                            leftIndex = (leftIndex + 1) % leftList.size();
                            leftHandler.post(this);
                            return;
                        }
                        saveImpression(media, durMs);
                        break;
                    }
                    case "WEB_CONTENT": {
                        String url = media.getUrl();
                        if (webContentView != null && url != null && !url.isEmpty()) {
                            webContentView.loadUrlSafe(url);
                            crossFadeIn(webContentView, prevVisible);
                        }
                        leftHandler.postDelayed(this, durMs);
                        saveImpression(media, durMs);
                        break;
                    }
                    case "weather": {
                        displayText.setVisibility(View.GONE);
                        logoImage.setVisibility(View.GONE);
                        crossFadeIn(weatherLayout, prevVisible);
                        leftHandler.postDelayed(this, durMs);
                        break;
                    }
                    default: {
                        android.util.Log.w("AdvertSplitScreen", "⚠️ Unrecognised left-zone type: " + type);
                        leftIndex = (leftIndex + 1) % leftList.size();
                        leftHandler.postDelayed(this, durMs);
                        return;
                    }
                }
                leftIndex = (leftIndex + 1) % leftList.size();
            }
        };

        waitingLogo.setVisibility(View.GONE);
        leftHandler.post(leftSwitcher);
    }

    private void hideAllLeft(View keep) {
        releaseExoPlayer();
        adPlayerView.setVisibility(View.GONE);
        if (keep != adImageView)   adImageView.setVisibility(View.GONE);
        if (keep != weatherLayout && weatherLayout != null) weatherLayout.setVisibility(View.GONE);
        if (keep != webContentView && webContentView != null) webContentView.setVisibility(View.GONE);
        logoImage.setVisibility(View.GONE);
        displayText.setVisibility(View.GONE);
    }

    private View currentVisibleLeftView() {
        if (adImageView.getVisibility()   == View.VISIBLE) return adImageView;
        if (adPlayerView.getVisibility()  == View.VISIBLE) return adPlayerView;
        if (weatherLayout  != null && weatherLayout.getVisibility()  == View.VISIBLE) return weatherLayout;
        if (webContentView != null && webContentView.getVisibility() == View.VISIBLE) return webContentView;
        return null;
    }

    private void showDisplayText(String text) {
        if (DataHolder.getInstance().displayFlag == 1 && text != null && !text.isEmpty()) {
            displayText.setText(text);
            displayText.setSelected(true);
            displayText.setVisibility(View.VISIBLE);
        }
    }

    // ══════════════════════════════════════════════════════════════
    // Right zone rotation
    // ══════════════════════════════════════════════════════════════

    private void startRightRotation() {
        if (rightSwitcher != null) rightHandler.removeCallbacks(rightSwitcher);

        rightSwitcher = new Runnable() {
            @Override
            public void run() {
                if (!socialFeedList.isEmpty()) {
                    if (rightIndex >= socialFeedList.size()) rightIndex = 0;
                    MediaModel feed = socialFeedList.get(rightIndex);
                    showSocialSlot(feed);
                    long dur = feed.getDurationInMillis() > 0 ? feed.getDurationInMillis() : 30_000L;
                    rightIndex = (rightIndex + 1) % socialFeedList.size();
                    rightHandler.postDelayed(this, dur);
                } else {
                    showNextNews();
                    rightHandler.postDelayed(this, 15_000L);
                }
            }
        };
        rightHandler.post(rightSwitcher);
    }

    /** Social feed is Twitter/X only — rendered as the official public hashtag-search
     *  timeline widget (no API key required), loaded directly in a WebView. */
    private void showSocialSlot(MediaModel feed) {
        String hashtag = feed.getSocialHashtag();

        socialFeedLayout.setVisibility(View.VISIBLE);
        newsRightLayout.setVisibility(View.GONE);

        if (socialPlatformLabel != null) socialPlatformLabel.setText("TWITTER");
        if (socialHashtagLabel != null)
            socialHashtagLabel.setText(hashtag != null ? "#" + hashtag : "");

        if (socialFeedWebView != null) {
            socialFeedWebView.loadHtmlContent(buildTwitterTimelineHtml(hashtag), "https://twitter.com/");
        }
    }

    private String buildTwitterTimelineHtml(String hashtag) {
        String tag = hashtag != null ? hashtag : "";
        String query;
        try {
            query = java.net.URLEncoder.encode("#" + tag, "UTF-8");
        } catch (java.io.UnsupportedEncodingException e) {
            query = "%23" + tag;
        }
        return "<!DOCTYPE html><html><head><meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">"
                + "<style>body{margin:0;background:#111111;}</style></head><body>"
                + "<a class=\"twitter-timeline\" data-theme=\"dark\" data-chrome=\"nofooter noheader transparent\" "
                + "data-tweet-limit=\"8\" href=\"https://twitter.com/search?q=" + query + "&src=typed_query\">"
                + "Tweets about #" + tag + "</a>"
                + "<script async src=\"https://platform.twitter.com/widgets.js\" charset=\"utf-8\"></script>"
                + "</body></html>";
    }

    private void showNextNews() {
        socialFeedLayout.setVisibility(View.GONE);
        newsRightLayout.setVisibility(View.VISIBLE);

        if (rightNewsList == null || rightNewsList.isEmpty()) return;
        if (newsIndex >= rightNewsList.size()) newsIndex = 0;

        RssItem item = rightNewsList.get(newsIndex);

        if (newsRightHeadline != null) {
            newsRightHeadline.setText(item.getTitle() != null ? item.getTitle() : "");
        }
        if (newsRightDesc != null) {
            String desc = item.getDescription();
            newsRightDesc.setText((desc != null && !desc.trim().isEmpty()) ? desc
                    : "Breaking news from " + DataHolder.getInstance().location);
        }
        if (newsRightSource != null) {
            String src = item.getSource();
            if (src != null && !src.isEmpty()) {
                newsRightSource.setText(src);
                newsRightSource.setVisibility(View.VISIBLE);
            } else {
                newsRightSource.setVisibility(View.GONE);
            }
        }
        newsIndex++;
    }

    // ══════════════════════════════════════════════════════════════
    // ExoPlayer
    // ══════════════════════════════════════════════════════════════

    private void setupExoPlayer(String url, long fallbackDurMs) {
        releaseExoPlayer();
        exoPlayer = new ExoPlayer.Builder(this).build();
        adPlayerView.setPlayer(exoPlayer);
        adPlayerView.setUseController(false);
        adPlayerView.setVisibility(View.VISIBLE);
        adPlayerView.setAlpha(0f);
        exoPlayer.setPlayWhenReady(false);
        exoPlayer.setMediaItem(MediaItem.fromUri(Uri.parse(url)));
        exoPlayer.prepare();

        playerListener = new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int state) {
                if (state == Player.STATE_READY) {
                    exoPlayer.setPlayWhenReady(true);
                    adPlayerView.animate().alpha(1f).setDuration(300).start();
                    // For finite content use the actual duration; live streams keep fallback timer
                    long dur = exoPlayer.getDuration();
                    if (dur != C.TIME_UNSET && dur > 0) {
                        leftHandler.removeCallbacks(leftSwitcher);
                        leftHandler.postDelayed(leftSwitcher, dur);
                    }
                } else if (state == Player.STATE_ENDED) {
                    leftHandler.removeCallbacks(leftSwitcher);
                    leftHandler.postDelayed(leftSwitcher, 500);
                }
            }
            @Override
            public void onPlayerError(PlaybackException error) {
                android.util.Log.e("AdvertSplitScreen", "ExoPlayer error: " + error.getMessage());
                leftHandler.removeCallbacks(leftSwitcher);
                leftHandler.post(leftSwitcher);
            }
        };
        exoPlayer.addListener(playerListener);
    }

    private void releaseExoPlayer() {
        if (exoPlayer != null) {
            if (playerListener != null) exoPlayer.removeListener(playerListener);
            exoPlayer.release();
            exoPlayer = null;
        }
    }

    // ══════════════════════════════════════════════════════════════
    // Weather
    // ══════════════════════════════════════════════════════════════

    private void getWeather(String loc) {
        if (loc == null || loc.isEmpty()) return;
        retrofitBuilder.apiCalls2().getWeather(Config.weatherKey, loc).enqueue(new Callback<WeatherModel>() {
            @Override
            public void onResponse(Call<WeatherModel> call, Response<WeatherModel> response) {
                if (isFinishing() || isDestroyed()) return;
                if (!response.isSuccessful() || response.body() == null
                        || response.body().current == null
                        || response.body().current.condition == null) return;

                Current cur = response.body().current;
                String iconUrl = "https:" + cur.condition.icon.replace("/64x64/", "/128x128/");
                Glide.with(AdvertSplitScreen.this).load(iconUrl).into(weatherImg);
                tvTemp.setText(Math.round(cur.temp_c) + "°C");
                tvStatus.setText(cur.condition.text);
                tvLoc.setText(DataHolder.getInstance().location);
                humadity.setText(cur.humidity + "%");
                wind.setText(Math.round(cur.wind_kph) + "");
                rain.setText(Math.round(cur.feelslike_c) + "°");
                if (pressure != null) pressure.setText(Math.round(cur.pressure_mb) + "");
            }
            @Override
            public void onFailure(Call<WeatherModel> call, Throwable t) {
                android.util.Log.e("AdvertSplitScreen", "Weather error: " + (t != null ? t.getMessage() : ""));
                if (!isFinishing() && !isDestroyed()) {
                    if (tvStatus != null) tvStatus.setText("Weather unavailable");
                    if (tvLoc != null && location != null) tvLoc.setText(location);
                }
            }
        });
    }

    private void startWeatherAutoRefresh() {
        weatherRefreshRunnable = new Runnable() {
            @Override public void run() {
                if (!isFinishing() && !isDestroyed()) getWeather(location);
                weatherRefreshHandler.postDelayed(this, WEATHER_REFRESH_MS);
            }
        };
        weatherRefreshHandler.postDelayed(weatherRefreshRunnable, WEATHER_REFRESH_MS);
    }

    /**
     * DataHolder.location can be null — e.g. a screen configured remotely from a phone during
     * TV login whose location lookup hasn't completed/failed (see SelectScreens.runAutoPlayIfRequested).
     * NewsHandler.load's cityName parameter is a Kotlin non-null String; passing null crashes
     * the whole app on launch. Never call newsHandler.load() with the raw DataHolder value.
     */
    private String safeLocation() {
        String loc = DataHolder.getInstance().location;
        return loc != null ? loc : "";
    }

    // ══════════════════════════════════════════════════════════════
    // News
    // ══════════════════════════════════════════════════════════════

    private void loadNews() {
        newsHandler = new NewsHandler(0);
        newsHandler.load(safeLocation(), context, (rss, i) -> {
            rightNewsList = new ArrayList<>(rss);
            newsIndex = 0;
            return Unit.INSTANCE;
        }, bar -> Unit.INSTANCE);
    }

    private void startNewsAutoRefresh() {
        newsRefreshRunnable = new Runnable() {
            @Override public void run() {
                if (!isFinishing() && !isDestroyed()) {
                    Utils.INSTANCE.getNewsList().clear();
                    loadNews();
                }
                newsRefreshHandler.postDelayed(this, NEWS_REFRESH_MS);
            }
        };
        newsRefreshHandler.postDelayed(newsRefreshRunnable, NEWS_REFRESH_MS);
    }

    // ══════════════════════════════════════════════════════════════
    // Live clock
    // ══════════════════════════════════════════════════════════════

    private void startLiveClock() {
        timeRunnable = new Runnable() {
            @Override public void run() {
                if (isFinishing() || isDestroyed()) return;
                Date now = new Date();
                if (timeNow != null)
                    timeNow.setText(new SimpleDateFormat("HH:mm", Locale.getDefault()).format(now));
                if (dateNow != null)
                    dateNow.setText(new SimpleDateFormat("EEE, d MMM", Locale.getDefault())
                            .format(now).toUpperCase(Locale.getDefault()));
                timeHandler.postDelayed(this, 1000);
            }
        };
        timeHandler.post(timeRunnable);
    }

    // ══════════════════════════════════════════════════════════════
    // Playlist sync
    // ══════════════════════════════════════════════════════════════

    private void registerPlaylistSyncReceiver() {
        String screenId = DataHolder.getInstance().screenID;
        playlistObserver = update -> {
            if (update != null && screenId != null && screenId.equals(update.screenId)) {
                reloadFromDatabase();
            }
        };
        PlaylistSyncManager.getInstance().getPlaylistUpdateLiveData().observe(this, playlistObserver);
    }

    private void reloadFromDatabase() {
        String screenId = DataHolder.getInstance().screenID;
        Executors.newSingleThreadExecutor().execute(() -> {
            AdDatabase db = AdDatabase.getInstance(context);
            List<AdEntity> entities = db.adDao().getAllAds(screenId);
            if (entities == null || entities.isEmpty()) return;

            List<MediaModel> newLeft   = new ArrayList<>();
            List<MediaModel> newSocial = new ArrayList<>();

            for (AdEntity ad : entities) {
                boolean streaming = "LIVE_STREAM".equals(ad.format)
                        || "WEB_CONTENT".equals(ad.format)
                        || "SOCIAL_FEED".equals(ad.format);
                if ((ad.localPath != null && !ad.localPath.isEmpty()) || streaming) {
                    MediaModel m = new MediaModel(
                            ad.contractId, ad.currency, ad.maxBid, ad.format,
                            ad.localPath != null ? ad.localPath : "",
                            ad.duration, ad.textBottom, ad.textTop, "", ad.targetHours, ad.advertId,
                            ad.targetGender, ad.targetAgeGroup, ad.targetTags, ad.targetEmotion);
                    if (ad.streamType     != null) m.setStreamType(ad.streamType);
                    if (ad.socialPlatform != null) m.setSocialPlatform(ad.socialPlatform);
                    if (ad.socialHashtag  != null) m.setSocialHashtag(ad.socialHashtag);

                    if ("SOCIAL_FEED".equals(ad.format)) newSocial.add(m);
                    else                                 newLeft.add(m);
                }
            }

            List<MediaModel> newRotation = buildLeftRotation(newLeft);
            if (newRotation.isEmpty()) newRotation = buildWeatherOnlyList();
            final List<MediaModel> rotation = newRotation;

            new Handler(Looper.getMainLooper()).post(() -> {
                boolean socialChanged = newSocial.size() != socialFeedList.size();

                boolean wasLeftEmpty = leftList.isEmpty();
                leftList.clear();
                leftList.addAll(rotation);
                if (leftIndex >= leftList.size()) leftIndex = 0;
                if (!leftList.isEmpty() && wasLeftEmpty) {
                    leftHandler.removeCallbacks(leftSwitcher);
                    leftHandler.post(leftSwitcher);
                }

                socialFeedList.clear();
                socialFeedList.addAll(newSocial);
                if (rightIndex >= socialFeedList.size()) rightIndex = 0;

                if (socialChanged) {
                    // Restart right zone so it picks up the new mode (social vs news)
                    rightHandler.removeCallbacks(rightSwitcher);
                    rightIndex = 0;
                    rightHandler.post(rightSwitcher);
                }

                Toast.makeText(context, "Playlist updated", Toast.LENGTH_SHORT).show();
            });
        });
    }

    // ══════════════════════════════════════════════════════════════
    // Helpers
    // ══════════════════════════════════════════════════════════════

    private void crossFadeIn(View incoming, View outgoing) {
        if (incoming == null) return;
        incoming.setAlpha(0f);
        incoming.setVisibility(View.VISIBLE);
        incoming.animate().alpha(1f).setDuration(350).start();
        if (outgoing != null && outgoing != incoming) {
            final View out = outgoing;
            out.animate().alpha(0f).setDuration(350).withEndAction(() -> {
                out.setVisibility(View.GONE);
                out.setAlpha(1f);
            }).start();
        }
    }

    private String currentHour() {
        return new SimpleDateFormat("H", Locale.getDefault()).format(new Date());
    }

    private List<Integer> stringToList(String str) {
        List<Integer> list = new ArrayList<>();
        if (str == null || str.isEmpty()) return list;
        for (String s : str.split("[/,]")) {
            try { list.add(Integer.parseInt(s.trim())); } catch (NumberFormatException ignored) {}
        }
        return list;
    }

    private void saveImpression(MediaModel media, long durationMs) {
        ImpressionEntity imp     = new ImpressionEntity();
        imp.impressionId         = UUID.randomUUID().toString();
        imp.advertId             = media.getAdvertId();
        imp.amountSettled        = false;
        imp.contractId           = media.getContractId();
        imp.currency             = media.getCurrency();
        imp.dayHour              = Integer.parseInt(currentHour());
        imp.playSec              = (int) (durationMs / 1000);
        imp.format               = media.getType();
        imp.locationType         = DataHolder.getInstance().locationTypes;
        imp.maxBid               = media.getMaxBid();
        imp.orientation          = "Split Screen";
        imp.playTimeStamp        = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                Locale.getDefault()).format(new Date());
        imp.screenDevice         = DataHolder.getInstance().screenDevice;
        imp.screenPlayer         = DataHolder.getInstance().screenPlayer;
        imp.screenId             = DataHolder.getInstance().screenID;
        imp.tags                 = DataHolder.getInstance().tags;

        AdDatabase db = AdDatabase.getInstance(context);
        new Thread(() -> {
            db.impDao().insertImpression(imp);
            if (isInternetAvailable()) APIImpression.sendImpression(context, imp);
        }).start();
    }

    private boolean isInternetAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        NetworkCapabilities caps = cm.getNetworkCapabilities(cm.getActiveNetwork());
        return caps != null && (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                || caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                || caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
    }
}
