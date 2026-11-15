package com.adjaba.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
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
import com.adjaba.models.newmodels.Current;
import com.adjaba.models.newmodels.MediaModel;
import com.adjaba.models.newmodels.WeatherModel;
import com.adjaba.models.zones.ZoneLayout;
import com.adjaba.news.NewsHandler;
import com.adjaba.news.RssItem;
import com.adjaba.room.AdDatabase;
import com.adjaba.room.AdEntity;
import com.adjaba.room.ImpressionEntity;
import com.adjaba.utilities.Config;
import com.adjaba.utilities.PlaylistSyncManager;
import com.adjaba.utilities.RetrofitBuilder;
import com.bumptech.glide.Glide;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.PlayerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;

import kotlin.Unit;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Dynamic Canvas: renders an arbitrary N-zone layout (ZoneLayout, 1-4 zones, percentage-based)
 * instead of a single fixed split like AdvertSplitScreen's hard-coded 70/30. The layout preset
 * (ZoneLayout.LayoutType) and each ad's zone assignment both come from the synced playlist —
 * see MediaModel.getZoneLayoutType()/getZoneId() and AdContractData's matching fields.
 *
 * Every zone runs its own independent Handler/Runnable loop (same pattern AdvertSplitScreen uses
 * for its two zones), so one zone's rotation timing never disrupts another's.
 *
 * Known scope limits in this first increment (documented, not silent):
 *  - Per-ad display-text/QR overlays are not rendered per zone (global-only in the single-zone
 *    activities today).
 *  - WEB_VIEW/DATA_WIDGET zones render ZoneLayout.Zone#contentSource, but nothing in the synced
 *    playlist sets it yet — the CMS/backend needs a field for that before it's end-to-end live.
 *  - QR_CODE and CUSTOM (raw backend-supplied zone arrays) are modeled but not wired to a parser.
 *  - A SOCIAL_FEED or AD_ROTATION zone that starts empty and only gains content via a later
 *    background sync will pick it up (polls every 5s) — but a brand-new zone type appearing only
 *    after sync (not present when the activity was built) will not retroactively appear.
 *  - A NEWS_TICKER zone fetches its headlines once at zone start and rotates through them every
 *    15s; unlike AdvertSplitScreen it does not periodically re-fetch from the network.
 */
public class AdvertDynamicCanvas extends AppCompatActivity {

    private static final String TAG = "AdvertDynamicCanvas";
    private static final long WEATHER_REFRESH_MS = 15 * 60 * 1000L;
    private static final long NEWS_HEADLINE_ROTATE_MS = 15_000L;
    private static final long EMPTY_ZONE_POLL_MS = 5_000L;

    private Context context;
    private RetrofitBuilder retrofitBuilder;
    private FrameLayout zoneCanvasRoot;
    private ImageView waitingLogo, logoImage;
    private ZoneLayout zoneLayout;
    private final List<ZoneRuntime> zoneRuntimes = new ArrayList<>();
    private Observer<PlaylistSyncManager.PlaylistUpdate> playlistObserver;

    /** Per-zone runtime state: its own views, Handler loop, and content buffer. */
    private static class ZoneRuntime {
        final ZoneLayout.Zone zone;
        final FrameLayout container;
        List<MediaModel> items = new ArrayList<>();
        int index = 0;
        final Handler handler = new Handler(Looper.getMainLooper());
        Runnable switcher;

        ImageView imageView, placeholderView;
        PlayerView playerView;
        SecureSignageWebView webView;
        ExoPlayer exoPlayer;
        Player.Listener playerListener;

        ImageView weatherIcon;
        TextView weatherTemp, weatherStatus;

        TextView newsHeadline, newsSource;
        List<RssItem> newsItems = new ArrayList<>();
        int newsIndex = 0;

        ZoneRuntime(ZoneLayout.Zone zone, FrameLayout container) {
            this.zone = zone;
            this.container = container;
        }
    }

    // ══════════════════════════════════════════════════════════════
    // Lifecycle
    // ══════════════════════════════════════════════════════════════

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_advert_dynamic_canvas);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        context = this;
        retrofitBuilder = new RetrofitBuilder();

        waitingLogo = findViewById(R.id.waitingLogo);
        zoneCanvasRoot = findViewById(R.id.zoneCanvasRoot);
        logoImage = findViewById(R.id.logoImage);
        logoImage.setAlpha(0.55f);
        logoImage.setVisibility(View.VISIBLE);
        setupExitGesture();

        zoneLayout = resolveZoneLayout();
        Log.i(TAG, "Resolved layout: " + zoneLayout.getLayoutType() + " (" + zoneLayout.getZoneCount() + " zones)");

        // Wait for the first layout pass so zoneCanvasRoot has a real measured size.
        zoneCanvasRoot.post(this::buildZones);
        registerPlaylistSyncReceiver();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        for (ZoneRuntime rt : zoneRuntimes) {
            if (rt.switcher != null) rt.handler.removeCallbacks(rt.switcher);
            releaseZonePlayer(rt);
            if (rt.webView != null) rt.webView.cleanup();
        }
    }

    private void setupExitGesture() {
        logoImage.setOnClickListener(new View.OnClickListener() {
            long lastClick = 0;
            @Override public void onClick(View v) {
                long now = System.currentTimeMillis();
                if (now - lastClick < 350) finish();
                lastClick = now;
            }
        });
    }

    // ══════════════════════════════════════════════════════════════
    // Layout resolution + zone building
    // ══════════════════════════════════════════════════════════════

    /** Takes the first non-null zoneLayoutType found among the synced ads; defaults to a single
     *  full-screen zone (today's existing single-zone behavior) when nothing overrides it. */
    private ZoneLayout resolveZoneLayout() {
        List<MediaModel> ads = DataHolder.getInstance().allAds;
        String typeName = null;
        if (ads != null) {
            for (MediaModel m : ads) {
                if (m.getZoneLayoutType() != null && !m.getZoneLayoutType().trim().isEmpty()) {
                    typeName = m.getZoneLayoutType().trim();
                    break;
                }
            }
        }
        ZoneLayout.LayoutType type = ZoneLayout.LayoutType.SINGLE_FULL_SCREEN;
        if (typeName != null) {
            try {
                type = ZoneLayout.LayoutType.valueOf(typeName.toUpperCase(Locale.US));
            } catch (IllegalArgumentException e) {
                Log.w(TAG, "Unknown zoneLayoutType '" + typeName + "' — defaulting to SINGLE_FULL_SCREEN");
            }
        }
        return new ZoneLayout(type);
    }

    private void buildZones() {
        zoneCanvasRoot.removeAllViews();
        zoneRuntimes.clear();

        int rootW = zoneCanvasRoot.getWidth();
        int rootH = zoneCanvasRoot.getHeight();
        if (rootW == 0 || rootH == 0 || zoneLayout.getZones() == null) {
            Log.w(TAG, "Canvas not measured yet or layout has no zones — retrying");
            zoneCanvasRoot.post(this::buildZones);
            return;
        }

        Map<Integer, List<MediaModel>> byZone = partitionByZone(DataHolder.getInstance().allAds);

        for (ZoneLayout.Zone zone : zoneLayout.getZones()) {
            FrameLayout container = new FrameLayout(this);
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                    Math.round(rootW * zone.widthPercent / 100f),
                    Math.round(rootH * zone.heightPercent / 100f));
            lp.leftMargin = Math.round(rootW * zone.xPercent / 100f);
            lp.topMargin = Math.round(rootH * zone.yPercent / 100f);
            container.setLayoutParams(lp);
            container.setBackgroundColor(Color.parseColor("#0A0A0A"));
            zoneCanvasRoot.addView(container);

            ZoneRuntime rt = new ZoneRuntime(zone, container);
            List<MediaModel> items = byZone.get(zone.zoneId);
            if (items != null) rt.items = items;
            zoneRuntimes.add(rt);
            startZone(rt);
        }

        waitingLogo.setVisibility(View.GONE);
    }

    /** Routes each ad to the zone whose id matches MediaModel.getZoneId(); zoneId 0 (unset,
     *  i.e. every ad synced before this feature existed) and any zoneId with no matching zone
     *  in the current layout both fall back to zone 1 — the main zone in every preset above. */
    private Map<Integer, List<MediaModel>> partitionByZone(List<MediaModel> ads) {
        Map<Integer, List<MediaModel>> result = new HashMap<>();
        Set<Integer> validZoneIds = new HashSet<>();
        if (zoneLayout.getZones() != null) {
            for (ZoneLayout.Zone z : zoneLayout.getZones()) {
                result.put(z.zoneId, new ArrayList<>());
                validZoneIds.add(z.zoneId);
            }
        }
        if (ads != null) {
            for (MediaModel m : ads) {
                int zid = m.getZoneId();
                int target = (zid != 0 && validZoneIds.contains(zid)) ? zid : 1;
                List<MediaModel> bucket = result.get(target);
                if (bucket != null) bucket.add(m);
            }
        }
        return result;
    }

    private void startZone(ZoneRuntime rt) {
        switch (rt.zone.contentType) {
            case AD_ROTATION:   startAdRotationZone(rt); break;
            case WEATHER:       startWeatherZone(rt); break;
            case NEWS_TICKER:   startNewsZone(rt); break;
            case SOCIAL_FEED:   startSocialZone(rt); break;
            case CLOCK:         startClockZone(rt); break;
            case WEB_VIEW:
            case DATA_WIDGET:   startWebZone(rt); break;
            default:
                Log.w(TAG, "Zone " + rt.zone.zoneId + ": content type " + rt.zone.contentType + " not implemented yet");
        }
    }

    // ══════════════════════════════════════════════════════════════
    // AD_ROTATION zone — same per-item dispatch as the single-zone activities
    // ══════════════════════════════════════════════════════════════

    private void startAdRotationZone(ZoneRuntime rt) {
        rt.imageView = new ImageView(this);
        rt.imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        rt.container.addView(rt.imageView, matchParent());

        rt.playerView = new PlayerView(this);
        rt.playerView.setUseController(false);
        rt.playerView.setVisibility(View.GONE);
        rt.container.addView(rt.playerView, matchParent());

        rt.webView = new SecureSignageWebView(this);
        rt.webView.setVisibility(View.GONE);
        rt.container.addView(rt.webView, matchParent());

        rt.placeholderView = new ImageView(this);
        rt.placeholderView.setImageResource(R.drawable.adjaba_logo_white);
        rt.placeholderView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        rt.placeholderView.setAlpha(0.2f);
        rt.placeholderView.setPadding(dp(16), dp(16), dp(16), dp(16));
        rt.container.addView(rt.placeholderView, matchParent());

        rt.switcher = new Runnable() {
            @Override public void run() {
                if (rt.items.isEmpty()) {
                    rt.placeholderView.setVisibility(View.VISIBLE);
                    rt.handler.postDelayed(this, EMPTY_ZONE_POLL_MS);
                    return;
                }
                rt.placeholderView.setVisibility(View.GONE);
                if (rt.index >= rt.items.size()) rt.index = 0;
                MediaModel media = rt.items.get(rt.index);
                String type = media.getType() != null ? media.getType() : "";
                long durMs = media.getDurationInMillis() > 0 ? media.getDurationInMillis() : 6000L;

                hideAdViews(rt);
                switch (type) {
                    case "IMAGE":
                    case "":
                        Glide.with(getApplicationContext()).load(media.getUrl()).into(rt.imageView);
                        rt.imageView.setVisibility(View.VISIBLE);
                        rt.handler.postDelayed(this, durMs);
                        saveImpression(media, durMs);
                        break;
                    case "VIDEO":
                    case "LIVE_STREAM": {
                        String url = media.getUrl();
                        if (url != null && !url.isEmpty()) {
                            setupZoneExoPlayer(rt, url);
                        } else {
                            rt.handler.postDelayed(this, durMs);
                        }
                        saveImpression(media, durMs);
                        break;
                    }
                    case "WEB_CONTENT": {
                        String url = media.getUrl();
                        if (url != null && !url.isEmpty()) {
                            rt.webView.loadUrlSafe(url);
                            rt.webView.setVisibility(View.VISIBLE);
                        }
                        rt.handler.postDelayed(this, durMs);
                        saveImpression(media, durMs);
                        break;
                    }
                    default:
                        Log.w(TAG, "Zone " + rt.zone.zoneId + ": unrecognised type '" + type + "'");
                        rt.handler.postDelayed(this, durMs);
                        return;
                }
                rt.index = (rt.index + 1) % rt.items.size();
            }
        };
        rt.handler.post(rt.switcher);
    }

    private void hideAdViews(ZoneRuntime rt) {
        releaseZonePlayer(rt);
        if (rt.imageView != null) rt.imageView.setVisibility(View.GONE);
        if (rt.playerView != null) rt.playerView.setVisibility(View.GONE);
        if (rt.webView != null) rt.webView.setVisibility(View.GONE);
    }

    private void setupZoneExoPlayer(ZoneRuntime rt, String url) {
        releaseZonePlayer(rt);
        rt.exoPlayer = new ExoPlayer.Builder(this).build();
        rt.playerView.setPlayer(rt.exoPlayer);
        rt.playerView.setVisibility(View.VISIBLE);
        rt.exoPlayer.setMediaItem(MediaItem.fromUri(Uri.parse(url)));
        rt.exoPlayer.setPlayWhenReady(true);
        rt.exoPlayer.prepare();

        rt.playerListener = new Player.Listener() {
            @Override public void onPlaybackStateChanged(int state) {
                if (state == Player.STATE_ENDED) {
                    rt.handler.removeCallbacks(rt.switcher);
                    rt.handler.postDelayed(rt.switcher, 300);
                }
            }
            @Override public void onPlayerError(PlaybackException error) {
                Log.e(TAG, "Zone " + rt.zone.zoneId + " ExoPlayer error: " + error.getMessage());
                rt.handler.removeCallbacks(rt.switcher);
                rt.handler.post(rt.switcher);
            }
        };
        rt.exoPlayer.addListener(rt.playerListener);
    }

    private void releaseZonePlayer(ZoneRuntime rt) {
        if (rt.exoPlayer != null) {
            if (rt.playerListener != null) rt.exoPlayer.removeListener(rt.playerListener);
            rt.exoPlayer.release();
            rt.exoPlayer = null;
        }
    }

    // ══════════════════════════════════════════════════════════════
    // WEATHER zone
    // ══════════════════════════════════════════════════════════════

    private void startWeatherZone(ZoneRuntime rt) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);

        rt.weatherIcon = new ImageView(this);
        layout.addView(rt.weatherIcon, new LinearLayout.LayoutParams(dp(48), dp(48)));

        rt.weatherTemp = new TextView(this);
        rt.weatherTemp.setTextColor(Color.WHITE);
        rt.weatherTemp.setTextSize(22);
        rt.weatherTemp.setTypeface(null, Typeface.BOLD);
        rt.weatherTemp.setGravity(Gravity.CENTER);
        layout.addView(rt.weatherTemp);

        rt.weatherStatus = new TextView(this);
        rt.weatherStatus.setTextColor(Color.parseColor("#B3B3B3"));
        rt.weatherStatus.setTextSize(11);
        rt.weatherStatus.setGravity(Gravity.CENTER);
        layout.addView(rt.weatherStatus);

        rt.container.addView(layout, matchParent());

        fetchWeatherInto(rt);
        rt.switcher = () -> {
            fetchWeatherInto(rt);
            rt.handler.postDelayed(rt.switcher, WEATHER_REFRESH_MS);
        };
        rt.handler.postDelayed(rt.switcher, WEATHER_REFRESH_MS);
    }

    private void fetchWeatherInto(ZoneRuntime rt) {
        String loc = DataHolder.getInstance().location;
        if (loc == null || loc.isEmpty()) return;
        retrofitBuilder.apiCalls2().getWeather(Config.weatherKey, loc).enqueue(new Callback<WeatherModel>() {
            @Override public void onResponse(Call<WeatherModel> call, Response<WeatherModel> response) {
                if (isFinishing() || isDestroyed()) return;
                if (!response.isSuccessful() || response.body() == null
                        || response.body().current == null || response.body().current.condition == null) return;
                Current cur = response.body().current;
                String iconUrl = "https:" + cur.condition.icon.replace("/64x64/", "/128x128/");
                Glide.with(AdvertDynamicCanvas.this).load(iconUrl).into(rt.weatherIcon);
                rt.weatherTemp.setText(Math.round(cur.temp_c) + "°C");
                rt.weatherStatus.setText(cur.condition.text);
            }
            @Override public void onFailure(Call<WeatherModel> call, Throwable t) {
                Log.e(TAG, "Zone " + rt.zone.zoneId + " weather error: " + (t != null ? t.getMessage() : ""));
            }
        });
    }

    // ══════════════════════════════════════════════════════════════
    // NEWS_TICKER zone
    // ══════════════════════════════════════════════════════════════

    private void startNewsZone(ZoneRuntime rt) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(dp(16), dp(16), dp(16), dp(16));

        rt.newsHeadline = new TextView(this);
        rt.newsHeadline.setTextColor(Color.WHITE);
        rt.newsHeadline.setTextSize(14);
        rt.newsHeadline.setTypeface(null, Typeface.BOLD);
        rt.newsHeadline.setMaxLines(4);
        layout.addView(rt.newsHeadline);

        rt.newsSource = new TextView(this);
        rt.newsSource.setTextColor(Color.parseColor("#B3B3B3"));
        rt.newsSource.setTextSize(10);
        rt.newsSource.setPadding(0, dp(8), 0, 0);
        layout.addView(rt.newsSource);

        rt.container.addView(layout, matchParent());

        String loc = DataHolder.getInstance().location != null ? DataHolder.getInstance().location : "";
        new NewsHandler(0).load(loc, context, (rss, i) -> {
            rt.newsItems = new ArrayList<>(rss);
            return Unit.INSTANCE;
        }, bar -> Unit.INSTANCE);

        rt.switcher = new Runnable() {
            @Override public void run() {
                if (!rt.newsItems.isEmpty()) {
                    if (rt.newsIndex >= rt.newsItems.size()) rt.newsIndex = 0;
                    RssItem item = rt.newsItems.get(rt.newsIndex);
                    rt.newsHeadline.setText(item.getTitle() != null ? item.getTitle() : "");
                    rt.newsSource.setText(item.getSource() != null ? item.getSource() : "");
                    rt.newsIndex++;
                }
                rt.handler.postDelayed(this, NEWS_HEADLINE_ROTATE_MS);
            }
        };
        rt.handler.post(rt.switcher);
    }

    // ══════════════════════════════════════════════════════════════
    // SOCIAL_FEED zone — same Twitter hashtag-timeline embed as AdvertSplitScreen
    // ══════════════════════════════════════════════════════════════

    private void startSocialZone(ZoneRuntime rt) {
        SecureSignageWebView webView = new SecureSignageWebView(this);
        rt.webView = webView;
        rt.container.addView(webView, matchParent());
        rt.items = filterSocialFeeds(rt.items);

        rt.switcher = new Runnable() {
            boolean placeholderShown = false;
            @Override public void run() {
                if (rt.items.isEmpty()) {
                    if (!placeholderShown) {
                        webView.loadHtmlContent(placeholderHtml("No social feed assigned to this zone"), "https://adjaba.in/");
                        placeholderShown = true;
                    }
                    rt.handler.postDelayed(this, EMPTY_ZONE_POLL_MS);
                    return;
                }
                placeholderShown = false;
                if (rt.index >= rt.items.size()) rt.index = 0;
                MediaModel feed = rt.items.get(rt.index);
                webView.loadHtmlContent(buildTwitterTimelineHtml(feed.getSocialHashtag()), "https://twitter.com/");
                long dur = feed.getDurationInMillis() > 0 ? feed.getDurationInMillis() : 30_000L;
                rt.index = (rt.index + 1) % rt.items.size();
                rt.handler.postDelayed(this, dur);
            }
        };
        rt.handler.post(rt.switcher);
    }

    private List<MediaModel> filterSocialFeeds(List<MediaModel> items) {
        List<MediaModel> out = new ArrayList<>();
        for (MediaModel m : items) if (m.isSocialFeed()) out.add(m);
        return out;
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

    // ══════════════════════════════════════════════════════════════
    // CLOCK zone
    // ══════════════════════════════════════════════════════════════

    private void startClockZone(ZoneRuntime rt) {
        TextView tv = new TextView(this);
        tv.setTextColor(Color.WHITE);
        tv.setTextSize(32);
        tv.setTypeface(null, Typeface.BOLD);
        tv.setGravity(Gravity.CENTER);
        rt.container.addView(tv, matchParent());

        rt.switcher = () -> {
            if (isFinishing() || isDestroyed()) return;
            tv.setText(new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date()));
            rt.handler.postDelayed(rt.switcher, 1000);
        };
        rt.handler.post(rt.switcher);
    }

    // ══════════════════════════════════════════════════════════════
    // WEB_VIEW / DATA_WIDGET zone — renders Zone#contentSource if the CMS set one
    // ══════════════════════════════════════════════════════════════

    private void startWebZone(ZoneRuntime rt) {
        SecureSignageWebView webView = new SecureSignageWebView(this);
        rt.webView = webView;
        rt.container.addView(webView, matchParent());

        if (rt.zone.contentSource != null && !rt.zone.contentSource.isEmpty()) {
            webView.loadUrlSafe(rt.zone.contentSource);
        } else {
            webView.loadHtmlContent(placeholderHtml("No widget URL configured for this zone"), "https://adjaba.in/");
        }
    }

    private String placeholderHtml(String message) {
        return "<!DOCTYPE html><html><head><meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">"
                + "<style>body{margin:0;background:#0A0A0A;color:#666;font-family:sans-serif;"
                + "display:flex;align-items:center;justify-content:center;height:100vh;"
                + "text-align:center;padding:16px;font-size:13px;}</style></head>"
                + "<body>" + message + "</body></html>";
    }

    // ══════════════════════════════════════════════════════════════
    // Playlist sync — mirrors AdvertSplitScreen.reloadFromDatabase, generalized to N zones
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

            List<MediaModel> newAds = new ArrayList<>();
            for (AdEntity ad : entities) {
                boolean streaming = "LIVE_STREAM".equals(ad.format) || "WEB_CONTENT".equals(ad.format)
                        || "SOCIAL_FEED".equals(ad.format);
                if ((ad.localPath != null && !ad.localPath.isEmpty()) || streaming) {
                    MediaModel m = new MediaModel(ad.contractId, ad.currency, ad.maxBid, ad.format,
                            ad.localPath != null ? ad.localPath : "", ad.duration, ad.textBottom, ad.textTop,
                            "", ad.targetHours, ad.advertId, ad.targetGender, ad.targetAgeGroup,
                            ad.targetTags, ad.targetEmotion);
                    m.setZoneId(ad.zoneId);
                    if (ad.streamType != null) m.setStreamType(ad.streamType);
                    if (ad.socialPlatform != null) m.setSocialPlatform(ad.socialPlatform);
                    if (ad.socialHashtag != null) m.setSocialHashtag(ad.socialHashtag);
                    newAds.add(m);
                }
            }

            Map<Integer, List<MediaModel>> byZone = partitionByZone(newAds);

            new Handler(Looper.getMainLooper()).post(() -> {
                for (ZoneRuntime rt : zoneRuntimes) {
                    if (rt.zone.contentType != ZoneLayout.ZoneContentType.AD_ROTATION
                            && rt.zone.contentType != ZoneLayout.ZoneContentType.SOCIAL_FEED) {
                        continue;
                    }
                    List<MediaModel> updated = byZone.get(rt.zone.zoneId);
                    if (updated == null) updated = new ArrayList<>();
                    if (rt.zone.contentType == ZoneLayout.ZoneContentType.SOCIAL_FEED) {
                        updated = filterSocialFeeds(updated);
                    }
                    rt.items = updated;
                    if (rt.index >= rt.items.size()) rt.index = 0;
                }
                Toast.makeText(context, "Playlist updated", Toast.LENGTH_SHORT).show();
            });
        });
    }

    // ══════════════════════════════════════════════════════════════
    // Helpers
    // ══════════════════════════════════════════════════════════════

    private FrameLayout.LayoutParams matchParent() {
        return new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private String currentHour() {
        return new SimpleDateFormat("H", Locale.getDefault()).format(new Date());
    }

    private void saveImpression(MediaModel media, long durationMs) {
        ImpressionEntity imp = new ImpressionEntity();
        imp.impressionId = UUID.randomUUID().toString();
        imp.advertId = media.getAdvertId();
        imp.amountSettled = false;
        imp.contractId = media.getContractId();
        imp.currency = media.getCurrency();
        imp.dayHour = Integer.parseInt(currentHour());
        imp.playSec = (int) (durationMs / 1000);
        imp.format = media.getType();
        imp.locationType = DataHolder.getInstance().locationTypes;
        imp.maxBid = media.getMaxBid();
        imp.orientation = "Dynamic Canvas";
        imp.playTimeStamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).format(new Date());
        imp.screenDevice = DataHolder.getInstance().screenDevice;
        imp.screenPlayer = DataHolder.getInstance().screenPlayer;
        imp.screenId = DataHolder.getInstance().screenID;
        imp.tags = DataHolder.getInstance().tags;

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
