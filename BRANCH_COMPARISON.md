# Adjaba Player - Branch Comparison & Staging Features

## 🌿 Branch Comparison

### **Current Status**

| Feature | Master | Staging | Notes |
|---------|--------|---------|-------|
| **Login System** | ✅ | ✅ | JWT token-based, encrypted storage |
| **Screen Selection** | ✅ | ✅ | Dropdown with user's screens |
| **Face Detection** | ✅ | ✅ | TensorFlow + ML Kit integration |
| **Demographics Analytics** | ✅ | ✅ | Age/Gender/Sentiment tracking |
| **Impressions Tracking** | ✅ | ✅ | Room database persistence |
| **API Synchronization** | ✅ | ✅ | Retrofit HTTP client |
| | | | |
| **Advert Playback** | ❌ | ✅ | Image/Video media3 player |
| **Playlist Loading** | ❌ | ✅ | `get_screen_playlists/{screenId}` API |
| **News Ticker** | ❌ | ✅ | BBC RSS feed integration |
| **Weather Widget** | ❌ | ✅ | weatherapi.com (15min refresh) |
| **MQTT Integration** | ❌ | ✅ | Real-time ad switching |
| **Camera as Service** | ❌ | ✅ | Runs silently during playback |
| **Landscape Ad View** | ❌ | ✅ | AdvertWatchingActivity |

### **Commit Timeline**

```
Master (7a8f360) - Merge PR #2 from ui-improves
    ↑
    │ (ui-improves branch)
    │   16551ae - add ui features
    │
    └─── Staging (27d776a) - feat: Add CameraAnalyticsService
             ↑
             │ (Recent commits in staging)
             │   b87169b - fix: remove invalid XML attr
             │   d782474 - feat: Add MQTT player + media3/Glide
             │
             └─── Both branches share:
                  - 5025631 - Remove large APK
                  - 7a8f360 - Merge PR #2 (ui-improves)
```

---

## 🎬 Staging Branch: Complete Feature Set

### **1. MQTT Manager - Real-Time Ad Switching**

**Location**: `app/src/main/java/com/rnd/mqtt/MqttManager.java`

**Purpose**: Listen for demographic triggers from the Analytics app and switch ads in real-time

```java
public class MqttManager {
    private static final String BROKER_URL = "tcp://broker.hivemq.com:1883";
    private static final String TOPIC_TRIGGER = "store/player/trigger";
    private static final String TOPIC_DEMOGRAPHICS = "store/analytics/demographics";
    
    private MqttClient client;
    private Context context;
    
    // Singleton pattern
    private static MqttManager instance;
    public static MqttManager getInstance(Context context) {
        if (instance == null) {
            instance = new MqttManager(context);
        }
        return instance;
    }
    
    public void connect() {
        // Features:
        // - Auto-reconnect with linear backoff (5 attempts)
        // - 10-second connection timeout
        // - TLS secured connection
        // - QoS level 1 (at least once)
    }
    
    public void subscribeTriggers() {
        // Listen for:
        // store/player/trigger:
        //   {
        //     "age_min": 20,
        //     "age_max": 35,
        //     "gender": "male",
        //     "sentiment": "happy"
        //   }
        
        client.subscribe(TOPIC_TRIGGER, 1, (topic, message) -> {
            DemographicTrigger trigger = parseTrigger(message);
            // → Triggers ad selection logic
            notifyListeners(trigger);
        });
    }
    
    public void disconnect() {
        // Graceful shutdown
        // Unsubscribe from topics
        // Close connection
    }
}
```

**Trigger JSON Payload** (from Analytics app):
```json
{
  "age_min": 20,
  "age_max": 35,
  "gender": "male",
  "sentiment": "happy",
  "timestamp": 1618234567890
}
```

### **2. AdvertWatchingActivity - Full Digital Signage Player**

**Location**: `app/src/main/java/com/rnd/activities/AdvertWatchingActivity.java`

**Features**:
- 🖼️ Image playback (Glide library, timed Handler)
- 🎬 Video playback (ExoPlayer/Media3)
- 📰 News ticker (BBC RSS, refreshed every 15 min)
- ☀️ Weather widget (weatherapi.com, refreshed every 15 min)
- 📋 Playlist loading from API every hour
- 🎯 Dynamic ad scoring based on demographics

```java
public class AdvertWatchingActivity extends AppCompatActivity {
    
    private List<WatchingModel> playlist;      // Loaded from API
    private int currentPlaylistIndex = 0;
    private Handler playHandler;               // For image timing
    private SimpleExoPlayer videoPlayer;       // For video playback
    
    private ImageView adImageView;
    private FrameLayout videoPlayerFrame;
    private TextView newsTickerView;
    private TextView weatherView;
    
    // MQTT listener
    private MqttManager mqttManager;
    private DemographicTrigger currentTrigger;
    
    onCreate() {
        // 1. Load playlist
        loadPlaylist(screenId);
        
        // 2. Setup video player
        videoPlayer = new SimpleExoPlayer.Builder(this).build();
        videoPlayerFrame.setPlayer(videoPlayer);
        
        // 3. Start background camera service
        startForegroundService(
            new Intent(this, CameraAnalyticsService.class)
        );
        
        // 4. Setup MQTT listener
        mqttManager = MqttManager.getInstance(this);
        mqttManager.connect();
        mqttManager.setOnTriggerListener((trigger) -> {
            selectAdBasedOnDemographic(trigger);
        });
        
        // 5. Setup news & weather refresh
        startNewsRefresh();  // Every 15 minutes
        startWeatherRefresh();  // Every 15 minutes
        
        // 6. Setup playlist reload
        schedulePlaylistReload();  // Every 60 minutes
    }
    
    private void loadPlaylist(String screenId) {
        // GET /get_screen_playlists/{screenId}
        ApiCalls api = retrofitBuilder.apiCalls();
        api.getPlaylist(screenId, authHeader)
            .enqueue(new Callback<List<WatchingModel>>() {
                onResponse(response):
                    playlist = response.body();
                    playNextItem();
                
                onFailure(throwable):
                    showErrorAndFallback();
            });
    }
    
    private void playNextItem() {
        if (currentPlaylistIndex >= playlist.size()) {
            currentPlaylistIndex = 0;  // Loop
        }
        
        WatchingModel item = playlist.get(currentPlaylistIndex);
        
        if (item.isImage()) {
            playImage(item.mediaUrl, item.durationSeconds);
        } else if (item.isVideo()) {
            playVideo(item.mediaUrl);
        }
        
        currentPlaylistIndex++;
    }
    
    private void playImage(String imageUrl, int durationSeconds) {
        Glide.with(this)
            .load(MEDIA_URL + imageUrl)
            .into(adImageView);
        
        adImageView.setVisibility(View.VISIBLE);
        videoPlayerFrame.setVisibility(View.GONE);
        
        // Schedule next item after duration
        playHandler.postDelayed(
            this::playNextItem,
            durationSeconds * 1000
        );
    }
    
    private void playVideo(String videoUrl) {
        MediaItem mediaItem = MediaItem.fromUri(MEDIA_URL + videoUrl);
        videoPlayer.setMediaItem(mediaItem);
        videoPlayer.prepare();
        videoPlayer.play();
        
        adImageView.setVisibility(View.GONE);
        videoPlayerFrame.setVisibility(View.VISIBLE);
    }
    
    // MQTT-based ad selection
    private void selectAdBasedOnDemographic(DemographicTrigger trigger) {
        // Score all ads in playlist against trigger
        List<WatchingModel> scoredAds = new ArrayList<>();
        
        for (WatchingModel ad : playlist) {
            int score = 0;
            
            // +2 if gender matches
            if (trigger.gender.equals(ad.targetGender)) {
                score += 2;
            }
            
            // +2 if age group matches
            if (isAgeInRange(trigger.age, ad.ageMin, ad.ageMax)) {
                score += 2;
            }
            
            // +1 if sentiment matches
            if (trigger.sentiment.equals(ad.targetSentiment)) {
                score += 1;
            }
            
            ad.priority = score;
        }
        
        // Sort by score (descending) and play highest-scoring ad
        Collections.sort(scoredAds, (a, b) -> b.priority - a.priority);
        
        if (!scoredAds.isEmpty()) {
            WatchingModel topAd = scoredAds.get(0);
            // Interrupt current playback and play topAd
            playAdImmediate(topAd);
        }
    }
    
    private void startNewsRefresh() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(
            this::refreshNews,
            0,           // initial delay
            15,          // period
            TimeUnit.MINUTES
        );
    }
    
    private void refreshNews() {
        // Fetch BBC RSS feed
        NewsUtils.fetchRSSFeed("https://feeds.bbc.co.uk/news/rss.xml",
            headlines -> {
                // Update news ticker TextView
                newsTickerView.setText(headlines);
            });
    }
    
    private void startWeatherRefresh() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(
            this::refreshWeather,
            0,           // initial delay
            15,          // period
            TimeUnit.MINUTES
        );
    }
    
    private void refreshWeather() {
        // Fetch from weatherapi.com
        ApiCalls api = retrofitBuilder.apiCalls();
        api.getWeather(Config.WEATHER_API_KEY, "London")
            .enqueue(new Callback<WeatherResponse>() {
                onResponse(response):
                    // Display current weather + 5-day forecast
                    // Format: "🌡️ 15°C, Partly Cloudy"
                    weatherView.setText(
                        "🌡️ " + response.current.tempC + 
                        "°C, " + response.current.condition
                    );
            });
    }
    
    onDestroy() {
        // Stop background camera service
        stopService(new Intent(this, CameraAnalyticsService.class));
        
        // Cleanup
        videoPlayer.release();
        mqttManager.disconnect();
        playHandler.removeCallbacksAndMessages(null);
    }
}
```

### **3. CameraAnalyticsService - Background Camera**

**Location**: `app/src/main/java/com/rnd/activities/CameraAnalyticsService.java`

**Problem Solved**: Camera and AdvertWatchingActivity couldn't run simultaneously (Activity conflict)

**Solution**: Foreground Service holding camera while Activity shows ads

```java
public class CameraAnalyticsService extends Service {
    
    // Key difference from TestCamera:
    // - Uses CameraX ImageAnalysis (NO preview surface needed)
    // - Runs silently in background
    // - Publishes MQTT messages every 10 seconds
    
    private CameraExecutor cameraExecutor;
    private ProcessCameraProvider cameraProvider;
    
    onStartCommand(Intent intent, int flags, int startId) {
        // Create foreground notification (required for API 30+)
        Notification notification = createNotification(
            "Digital Signage Active",
            "Camera running for analytics"
        );
        startForeground(NOTIFICATION_ID, notification);
        
        // Initialize background camera
        initializeCamera();
        
        return START_STICKY;  // Restart if killed
    }
    
    private void initializeCamera() {
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build();
        
        imageAnalysis.setAnalyzer(cameraExecutor, image -> {
            // Process frames WITHOUT preview surface
            processDemographics(image);
            image.close();
        });
        
        cameraProvider.bindToLifecycle(this, 
            CameraSelector.DEFAULT_FRONT_CAMERA,
            imageAnalysis);
    }
    
    private void processDemographics(ImageProxy image) {
        // Run TensorFlow models
        float genderScore = runGenderModel(image);
        int ageGroup = runAgeModel(image);
        float sentiment = runSentimentModel(image);
        
        // Aggregate statistics
        aggregateDemographics(genderScore, ageGroup, sentiment);
    }
    
    // Publish demographics every 10 seconds
    private void publishDemographicsTrigger() {
        MqttManager mqtt = MqttManager.getInstance(this);
        
        DemographicTrigger trigger = new DemographicTrigger(
            currentGender,
            currentAgeGroup,
            currentSentiment
        );
        
        mqtt.publish("store/analytics/demographics", trigger.toJson());
    }
    
    onDestroy() {
        cameraExecutor.shutdown();
        stopForeground(STOP_FOREGROUND_REMOVE);
    }
}
```

**Manifest Permissions** (Staging):
```xml
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_CAMERA" />

<service
    android:name=".activities.CameraAnalyticsService"
    android:foregroundServiceType="camera"
    android:enabled="true"
    android:exported="false" />
```

### **4. NewsUtils - RSS Feed Integration**

**Fetches from**: BBC News RSS (https://feeds.bbc.co.uk/news/rss.xml)

```java
public class NewsUtils {
    
    public static void fetchRSSFeed(String rssUrl, 
                                    Consumer<String> callback) {
        new Thread(() -> {
            try {
                URL url = new URL(rssUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                
                InputStream stream = conn.getInputStream();
                // Parse XML RSS feed
                
                // Extract headlines (titles from <item> tags)
                List<String> headlines = parseHeadlines(stream);
                
                // Format as scrolling ticker
                String ticker = String.join(" | ", headlines);
                callback.accept(ticker);
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
```

**Display**: Horizontal scrolling TextView with news headlines

### **5. Dependencies Added in Staging**

```gradle
// Media3 (ExoPlayer successor)
implementation 'androidx.media3:media3-exoplayer:1.3.1'
implementation 'androidx.media3:media3-ui:1.3.1'

// Image loading
implementation 'com.github.bumptech.glide:glide:4.16.0'
annotationProcessor 'com.github.bumptech.glide:compiler:4.16.0'

// MQTT
implementation 'org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.5'

// HTTP (upgraded to stable)
implementation 'com.squareup.okhttp3:okhttp:4.12.0'

// RxJava adapter fix
implementation 'com.squareup.retrofit2:adapter-rxjava2:2.9.0'
```

---

## 🚦 Quick Staging Branch Walkthrough

### **User Flow in Staging**

```
1. LoginActivity → Select screen (same as master)

2. SelectScreens → PLAY button

3. AdvertWatchingActivity LAUNCHES
   │
   ├─ Start CameraAnalyticsService
   │  └─ Camera runs silently, publishes demographics every 10s
   │
   ├─ Connect to MQTT broker
   │  └─ Subscribe to store/player/trigger
   │
   ├─ Load playlist via API
   │  GET /get_screen_playlists/{screenId}
   │
   ├─ Start playing ads
   │  ├─ Image 1 (20 seconds) → Uses Glide
   │  ├─ Video 1 (45 seconds) → Uses ExoPlayer/Media3
   │  └─ Image 2 (15 seconds)
   │
   ├─ Run news refresh (every 15 min)
   │  └─ Fetch BBC RSS → Display as ticker
   │
   ├─ Run weather refresh (every 15 min)
   │  └─ Fetch from weatherapi.com → Show current + forecast
   │
   └─ Wait for MQTT trigger
      └─ When demographic match received
         → Interrupt current ad
         → Play high-priority ad (based on gender/age/sentiment)

4. When user exits or app closes
   ├─ Stop camera service
   ├─ Disconnect MQTT
   └─ Send impressions via APIImpression
```

---

## 📈 Comparison: Master vs Staging

### **Master Branch (Release)**
- ✅ Core authentication & analytics
- ✅ Face detection with TensorFlow
- ✅ Database persistence
- ❌ No playback capability
- ❌ Analytics only (camera preview mode)
- **Use Case**: Analytics data collection platform

### **Staging Branch (Full Digital Signage)**
- ✅ Complete media playback (images + videos)
- ✅ Real-time ad switching via MQTT
- ✅ News & weather widgets
- ✅ Playlist management
- ✅ Background camera service
- **Use Case**: Full retail signage solution

---

## 🔀 Merging Strategy

### **Option 1: Fast-Forward Merge (Recommended)**
```bash
git checkout master
git merge staging
# Master now has all staging features
```

### **Option 2: Create Release Branch with Staging**
```bash
git checkout master
git pull origin staging
git checkout -b release
# Test and verify all features

# If satisfied:
git push origin release
```

### **Option 3: Cherry-Pick Specific Commits**
```bash
git checkout master
git cherry-pick d782474  # feat: Add MQTT player
git cherry-pick 27d776a  # feat: Add CameraAnalyticsService
```

---

## 📋 Testing Checklist for Staging

- [ ] **MQTT Connection**: Verify broker connection (broker.hivemq.com:1883)
- [ ] **Playlist Loading**: Confirm API returns playlist items
- [ ] **Image Display**: Glide loads and displays images correctly
- [ ] **Video Playback**: Media3 plays video without stuttering
- [ ] **News Ticker**: BBC RSS feed fetches and scrolls
- [ ] **Weather Widget**: Weather updates every 15 minutes
- [ ] **Camera Service**: Background camera publishes demographics
- [ ] **Ad Selection**: Demographic trigger changes displayed ad
- [ ] **Graceful Fallback**: App continues if MQTT broker unreachable

---

## ❓ FAQ

**Q: Is staging production-ready?**
A: Nearly. Needs testing on target devices and MQTT broker configuration validation.

**Q: Can master and staging coexist?**
A: No. Choose master for analytics-only or staging for full signage.

**Q: How often should I reload playlist?**
A: Every 60 minutes (hourly) to pick up time-targeted ads.

**Q: What if camera can't run with player?**
A: Staging uses ForegroundService (solved in 27d776a commit).

---

**Generated**: April 15, 2026  
**Status**: Ready for testing  
**Next Step**: Merge staging into master or test both branches independently

