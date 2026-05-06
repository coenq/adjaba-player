# 📱 Adjaba Player - Android TV Digital Signage App Architecture

**Author:** Android UI/UX Architect Analysis  
**Date:** May 4, 2026  
**Purpose:** Comprehensive app structure, screens, flows, orientation handling, and issues documentation

---

## 🏗️ Application Overview

**Type:** Android TV / Fire TV Digital Signage Player  
**Architecture:** Activity-based with REST API backend  
**Core Purpose:** Display rotating advertisements, weather, and news on commercial TV displays  
**Key Technologies:**
- ExoPlayer (video playback)
- Room Database (local ad caching)
- Retrofit (REST API)
- Glide (image loading)
- RSS feed parser (news)
- Weather API integration

---

## 📐 Screen Flow Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                     APP LAUNCH                                   │
│                          │                                       │
│                          ▼                                       │
│              ┌──────────────────────┐                           │
│              │  LoginActivity.java  │                           │
│              │  (activity_login.xml)│                           │
│              └──────────┬───────────┘                           │
│                         │ [credentials validated]                │
│                         ▼                                       │
│              ┌──────────────────────────┐                       │
│              │   SelectScreens.java      │                       │
│              │ (activity_select_screen)  │                       │
│              └──────────┬────────────────┘                       │
│                         │ [PLAY button]                          │
│                         ├──────────────────┬────────────────┐   │
│                         │ Orientation?     │                │   │
│                         ▼                  ▼                ▼   │
│              ┌──────────────────┐  ┌────────────────┐  ┌──────┐│
│              │ AdvertWatching   │  │ AdvertWatching │  │AdvertLand│
│              │ (Landscape)      │  │ (Portrait)     │  │Watch  ││
│              │fragment_advert_  │  │fragment_advert_│  │(Forced ││
│              │watching.xml      │  │watching.xml    │  │Portrait)││
│              └──────────────────┘  └────────────────┘  └───────┘│
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

## 🔐 **SCREEN 1: Login Activity**

### Purpose
User authentication and credential management

### Files
- **Java:** `LoginActivity.java`
- **Layout:** `activity_login.xml`

### UI Components
```
┌──────────────────────────────────┐
│         App Logo                 │
│                                  │
│  ┌────────────────────────┐     │
│  │ Username/Email         │     │
│  └────────────────────────┘     │
│                                  │
│  ┌────────────────────────┐     │
│  │ Password               │     │
│  └────────────────────────┘     │
│                                  │
│  ☑ Remember Me                   │
│                                  │
│  ┌────────────────────────┐     │
│  │      LOGIN             │     │
│  └────────────────────────┘     │
│                                  │
│  Terms & Conditions             │
└──────────────────────────────────┘
```

### Key Features
- **Encrypted storage** via `EncryptedSharedPreferences` for token storage
- **Remember Me** checkbox to persist credentials
- **Input validation:**
  - Username: min 2 characters
  - Password: min 6 characters
- **Network check** before API call
- **Database cleanup** on launch (deletes old ads)

### API Integration
```java
POST /v2/authenticate_user
Request: { userId: string, password: string }
Response: { loginToken: string, userid: string, email: string }
```

### State Management
```java
// Saves token securely
securePrefs.edit().putString("token", data.loginToken).apply();

// Navigate to screen selection
startActivity(new Intent(this, SelectScreens.class));
```

### Issues Identified ⚠️
1. **No fingerprint/biometric auth** for TV-based kiosk mode
2. **Token expiry not handled** - may fail silently after expiration
3. **No offline mode** - requires internet for first login
4. **Error messages generic** - doesn't differentiate between invalid credentials vs network issues

---

## 🖥️ **SCREEN 2: Select Screen Configuration**

### Purpose
Configure playback parameters: screen ID, orientation, refresh interval, and business rules

### Files
- **Java:** `SelectScreens.java`
- **Layout:** `activity_select_screen.xml` (portrait)
- **Layout-Land:** `layout-land/activity_select_screen.xml` (landscape)

### UI Components
```
┌─────────────────────────────────────────────┐
│  [App Logo]  Screen Configuration    [📊]  │
│─────────────────────────────────────────────│
│                                             │
│  Screen Orientation:                        │
│  ┌─────────────────────────────────┐       │
│  │ ▼ Landscape / Portrait /        │       │
│  │   Forced Portrait                │       │
│  └─────────────────────────────────┘       │
│                                             │
│  Screen ID:                                 │
│  ┌─────────────────────────────────┐       │
│  │ ▼ Demo136 / Screen001 / ...     │       │
│  └─────────────────────────────────┘       │
│                                             │
│  Refresh Interval:                          │
│  ┌─────────────────────────────────┐       │
│  │ ▼ Never / 5min / 30min / 1hr    │       │
│  └─────────────────────────────────┘       │
│                                             │
│  ☑ Remember Selection                      │
│  ☑ Display Ad Text                         │
│  ☑ Apply Business Rules (Target Hours)     │
│                                             │
│  ┌─────────────────────────────────┐       │
│  │           PLAY                   │       │
│  └─────────────────────────────────┘       │
│                                             │
│                          [LOGOUT]           │
└─────────────────────────────────────────────┘
```

### Orientation Handling Logic 🔄

#### **3 Orientation Modes Explained:**

| Mode | Hardware | Physical Mount | Display Behavior |
|------|----------|----------------|-----------------|
| **Landscape** | Landscape TV | Horizontal mount | Normal landscape display |
| **Portrait** | Portrait display | Vertical mount | Normal portrait display |
| **Forced Portrait** | Landscape TV | **Rotated 90° clockwise** | Software rotates content -90° to appear upright |

#### **Critical Orientation Flow:**
```java
// Line 131 - Available orientations
String[] orientationOptions = {"Orientation", "Landscape", "Portrait", "Forced Portrait"};

// Line 383-387 - Activity selection based on orientation
if (orient.toLowerCase().equalsIgnoreCase("forced portrait")) {
    startActivity(new Intent(context, AdvertLandWatch.class));
} else {
    startActivity(new Intent(context, AdvertWatching.class));
}
```

### Ad Download Process 📥

```
PLAY Button Clicked
    │
    ├─> Validate: screenId != "Select Screen" && orient != "Orientation"
    │
    ├─> Store config in DataHolder singleton
    │   ├─ screenID
    │   ├─ orientation
    │   ├─ location
    │   ├─ displayFlag (show ad text?)
    │   └─ targetHoursFlag (business rules?)
    │
    ├─> Show animated waiting logo
    │
    ├─> Call getAds(0)
        │
        ├─> DELETE all ads from Room DB (RxJava)
        │
        ├─> API: GET /get_screen_playlists/{screenId}
        │   │
        │   ├─> Response: List<WatchingModel> (ad metadata)
        │   │
        │   ├─> For each ad:
        │   │   ├─> getUrl(videoUrl) → resolves S3 URL
        │   │   ├─> Download file to internal storage
        │   │   ├─> Save to Room DB as AdEntity
        │   │   └─> Track progress
        │   │
        │   └─> When all ads downloaded:
        │       ├─> Read AdEntity list from DB
        │       ├─> Convert to MediaModel list
        │       ├─> Store in DataHolder.allAds
        │       └─> Launch AdvertWatching/AdvertLandWatch
        │
        └─> On API error or no ads:
            └─> Launch player anyway (weather + news only)
```

### Key State Management
```java
DataHolder.getInstance().screenID = screen_id;
DataHolder.getInstance().orient = orient;
DataHolder.getInstance().allAds = mediaModels; // Downloaded ads
DataHolder.getInstance().displayFlag = displayText.isChecked() ? 1 : 0;
DataHolder.getInstance().targetHoursFlag = businessRules.isChecked() ? 1 : 0;
```

### Issues Identified ⚠️

#### **Critical Issue #1: Race Condition in Ad Loading**
```java
// Lines 547-557 - Query by screenId MIGHT NOT MATCH
List<AdEntity> ads = db.adDao().getAllAds(screenId);
// screenId comes from API response (adList.get(i).screenId)
// But stored screenId might be from user selection
// → Potential mismatch causes empty list
```

#### **Critical Issue #2: Database Cleared But Not Repopulated**
```java
// Line 309-325 - Database cleared on background thread
adDatabase.adDao().deleteAllAds()
    .subscribeOn(Schedulers.io())
    .observeOn(AndroidSchedulers.mainThread())
    .subscribe(...);

// But getUrl() download happens in separate executor
// → Timing issue: Activity might launch before downloads complete
```

#### **Critical Issue #3: Progress Tracking Not Visible**
```java
// Lines 541-543 - Progress logged but not shown to user
int loaded = loadedCount[0];
int percent = (loaded * 100) / totalCount;
// No UI update → User sees waiting logo indefinitely
```

#### **Other Issues:**
4. **No timeout handling** - downloads can hang forever
5. **No retry mechanism** - single network failure = no ads
6. **Waiting logo animation loops** - no indication of actual progress
7. **Double-click to exit waiting** - UX is not discoverable

---

## 🎬 **SCREEN 3: Ad Playback View**

### Purpose
Plays rotating ads, weather slides, and news slides in an endless loop

### Files (Landscape/Portrait)
- **Java:** `AdvertWatching.java`
- **Layout:** `fragment_advert_watching.xml`

### Files (Forced Portrait)
- **Java:** `AdvertLandWatch.java`
- **Layout:** `activity_advert_land_watch.xml`

### UI Architecture (Multi-View Carousel)
```
┌──────────────────────────────────────────┐
│  [ConstraintLayout - Full Screen]        │
│                                          │
│  View Stack (only 1 visible at a time): │
│  ┌─────────────────────────────────┐    │
│  │ 1. waitingLogo (splash)         │    │
│  ├─────────────────────────────────┤    │
│  │ 2. adImageView (image ads)      │    │
│  ├─────────────────────────────────┤    │
│  │ 3. adPlayerView (video ads)     │    │
│  ├─────────────────────────────────┤    │
│  │ 4. weatherLayout (weather slide)│    │
│  ├─────────────────────────────────┤    │
│  │ 5. newsLayout (news slide)      │    │
│  └─────────────────────────────────┘    │
│                                          │
│  Overlays (visible during ads):          │
│  ├─ logoImage (brand logo)               │
│  ├─ qrImage (QR code for ad details)     │
│  └─ displayText (scrolling text)         │
└──────────────────────────────────────────┘
```

### Rotation Algorithm 🔄

```java
// insertWeatherEveryThreeAds() - Lines 528-544 (AdvertWatching)
// Rotation pattern:
[Ad1] → [Ad2] → [Ad3] → [Weather] → [News] → [loop]

Example with 3 ads:
Index  Type       Duration
  0    VIDEO      15s
  1    IMAGE      10s
  2    IMAGE      8s
  3    WEATHER    10s
  4    NEWS       10s
  5    VIDEO      15s (wraps to 0)
  ...endless loop...
```

### Media Switching Flow
```
Handler + Runnable Pattern:
    │
    ├─> mediaSwitcher.run() executes:
        │
        ├─> Get current media: mediaList[currentIndex]
        │
        ├─> Check Business Rules:
        │   └─> If targetHours enabled && current hour not in targetHours
        │       └─> Skip this ad, increment index
        │
        ├─> Hide all views
        ├─> Release ExoPlayer if playing
        │
        ├─> Switch based on media type:
        │   │
        │   ├─ "IMAGE":
        │   │   ├─ Load with Glide → adImageView
        │   │   ├─ Generate QR code
        │   │   ├─ Show logo, displayText
        │   │   ├─ slideTransition() animation
        │   │   ├─ Schedule next switch: handler.postDelayed(mediaSwitcher, duration)
        │   │   └─ saveAndSendImpression()
        │   │
        │   ├─ "VIDEO":
        │   │   ├─ Setup ExoPlayer
        │   │   ├─ Load MediaItem
        │   │   ├─ Wait for STATE_READY
        │   │   ├─ Fade in video, fade out image
        │   │   ├─ Schedule switch on STATE_ENDED
        │   │   └─ saveAndSendImpression()
        │   │
        │   ├─ "weather":
        │   │   ├─ Hide logo/QR/text
        │   │   ├─ Show weatherLayout
        │   │   ├─ slideTransition() animation
        │   │   └─ Duration: 10s
        │   │
        │   └─ "news":
        │       ├─ Check if newsIndex >= getNews.size()
        │       │   └─> Reload RSS feed
        │       ├─ Load image with Glide
        │       ├─ Set headline & description
        │       ├─ Show newsLayout
        │       ├─ slideTransition() animation
        │       └─ Duration: 10s
        │
        └─> currentIndex = (currentIndex + 1) % mediaList.size()
```

### Business Rules (Target Hours) 🕐

```java
// Lines 661-668 - Hour-based filtering
if (DataHolder.getInstance().targetHoursFlag == 1) {
    String type = media.getType();
    if (!stringToList(media.getTargetHours()).contains(currentHour)
            && !type.equals("weather") && !type.equals("news")) {
        currentIndex = (currentIndex + 1) % mediaList.size();
        handler.post(this); // Skip this ad
        return;
    }
}
```

**Example:**
- Ad has `targetHours: [9, 10, 11, 14, 15, 16, 17, 18]`
- Current time: 13:00
- **Result:** Ad is skipped, next media item shown immediately

### Impression Tracking 📊

```java
// saveAndSendImpression() - Lines 855-900
ImpressionEntity impression = new ImpressionEntity();
impression.impressionId = screenID + advertId + random3Digits;
impression.contractId = contractId;
impression.dayHour = currentHour;
impression.playSec = duration / 1000;
impression.playTimeStamp = ISO8601_timestamp;
impression.screenId = screenID;

// Save to Room DB
db.impDao().insertImpression(impression);

// Send to API if online
if (isInternetAvailable()) {
    APIImpression.sendImpression(context, impression);
}
```

### Auto-Refresh Mechanisms 🔄

#### **1. Weather Auto-Refresh (Every 15 minutes)**
```java
// Lines 209-220 - Background update
weatherRefreshHandler.postDelayed(() -> {
    getWeather(location, context); // Silent update
}, 15 * 60 * 1000L);
```

#### **2. News Auto-Refresh (Every 15 minutes)**
```java
// Lines 222-236
newsRefreshHandler.postDelayed(() -> {
    newsHandler.load(...); // Reload RSS
    Utils.INSTANCE.getNewsList().clear();
    Utils.INSTANCE.getNewsList().addAll(rss);
}, 15 * 60 * 1000L);
```

#### **3. Ad Playlist Refresh (Configurable: 5min - 1hr)**
```java
// Lines 768-774 - PROBLEM: Deletes and re-downloads ALL ads
handler.postDelayed(() -> {
    AdDatabase db = AdDatabase.getInstance(context);
    db.adDao().deleteAllAds(); // ← Clears current ads!
    handler.post(() -> {
        getAds(0); // ← Re-download from API
    });
}, newTime * 60 * 1000);
```

### Issues Identified ⚠️

#### **Critical Issue #1: Ads Refresh Deletes Playing Ads** 🔴
```java
// Problem: While ads are playing, the refresh deletes them!
// Lines 768-774 in startMediaRotation()
handler.postDelayed(refreshRunnable, (long) newTime * 60 * 1000);

// This calls:
db.adDao().deleteAllAds(); // ← Kills currently playing ads
getAds(0); // ← Downloads new ads, but playback broken
```
**Impact:**
- Playing ads suddenly stop
- Database emptied mid-playback
- User sees only weather/news until download completes

**Fix:** Move refresh logic outside rotation, or only refresh weather/news.

#### **Critical Issue #2: ExoPlayer State Not Fully Managed**
```java
// setupExoPlayer() - Lines 792-856
// Problem: No handling for:
// - Connection errors (buffering timeout)
// - Corrupted video files
// - Audio-only files (black screen)
```

#### **Critical Issue #3: Weather/News Slide Timing Issues**
```java
// Both weather and news hardcoded to 10 seconds
// But:
// - Weather data loads async (might not be ready)
// - News RSS might fail to load
// → Blank screen shown for 10s if data missing
```

#### **Issue #4: Memory Leaks**
```java
// onDestroy() cleanup incomplete:
// - Handler callbacks removed
// - ExoPlayer released
// BUT:
// - Retrofit calls still pending
// - ExecutorService might not shut down properly
// - Glide requests not cancelled
```

#### **Issue #5: No Error Recovery**
- Video playback error → skips to next item (no retry)
- Image load failure → blank screen for full duration
- Weather API failure → old data shown indefinitely
- News RSS failure → empty news slide

---

## 🌦️ **SCREEN 4: Weather Slide**

### Purpose
Display current weather conditions, time, and forecast

### Layout Structure (Landscape/Portrait)
```
┌─────────────────────────────────────┐
│  📍 London                          │
│                                     │
│            15:05                    │
│                                     │
│            ☁️                       │
│            24°C                     │
│        PARTLY CLOUDY                │
│                                     │
│  ──────────────────────────────────│
│                                     │
│   💨 Wind    💧 Humidity  🌡️ Feels │
│    32 km/h      60%        22°     │
└─────────────────────────────────────┘
```

### Layout Structure (Forced Portrait)
```
TV physically rotated 90° clockwise → appears as:

┌────────────────────┬────────────────────┐
│                    │                    │
│    📍 London       │      ☁️           │
│                    │                    │
│      15:05         │      24°C         │
│    (rotated        │                    │
│     -90°)          │  PARTLY CLOUDY    │
│                    │                    │
│                    │  ──────────────── │
│                    │                    │
│                    │  💨  💧  🌡️      │
│                    │  32  60% 22°      │
│                    │  (rotated -90°)   │
└────────────────────┴────────────────────┘
```

### Data Source
```java
// getWeather() - Lines 545-595
API: GET https://api.weatherapi.com/v1/forecast.json
Params:
  - key: Config.weatherKey
  - q: location (e.g., "London")
  - days: 3

Response fields used:
  - current.condition.icon → weatherImg
  - current.temp_c → tvTemp
  - current.condition.text → tvStatus
  - current.humidity → humadity
  - current.wind_kph → wind
  - current.feelslike_c → rain (mislabeled!)
```

### Issues Identified ⚠️

#### **Issue #1: Inconsistent Layout Between Orientations**
**Landscape/Portrait:** Uses ConstraintLayout with guideline at 82%
**Forced Portrait:** Uses LinearLayout with two halves

→ **Result:** Weather data positioned differently, font sizes vary

#### **Issue #2: Field Mislabeling**
```java
rain.setText(Math.round(response.body().current.feelslike_c) + "°");
// Variable named "rain" but shows "feels like" temp!
```

#### **Issue #3: Network Failure Handling**
```java
@Override
public void onFailure(Call<WeatherModel> call, Throwable t) {
    // Empty! No fallback data, no error message
}
```
→ **Result:** Old/stale weather data shown indefinitely

#### **Issue #4: Location String Not Validated**
- User enters location: "Londonn" (typo)
- API returns 400 error
- UI still shows "Londonn" with no data
- No user feedback

#### **Issue #5: Time Display Not Localized**
```java
SimpleDateFormat("hh:mm a", Locale.getDefault())
// Uses 12-hour format always
// No respect for user's 24-hour preference
```

---

## 📰 **SCREEN 5: News Slide**

### Purpose
Display rotating RSS news headlines with images

### Layout Structure (Landscape/Portrait)
```
┌─────────────────────────────────────────┐
│                                         │
│     [Full-bleed news image]             │
│           ┌──────┐                      │
│           │ NEWS │ badge                │
│           └──────┘                      │
│                                         │
│  ═══════════════════════════════════   │
│                                         │
│  Breaking news headline goes here       │
│  and wraps to multiple lines...         │
│  ─────────────────────────────────      │
│  News description with more details     │
│  about the story, truncated after       │
│  5 lines maximum.                       │
└─────────────────────────────────────────┘
```

### Layout Structure (Forced Portrait)
```
┌──────────────────┬──────────────────────┐
│                  │                      │
│  [News Image]    │  ─── (accent bar)   │
│                  │                      │
│  ┌──────┐        │  Breaking headline  │
│  │ NEWS │        │  wraps multiple     │
│  └──────┘        │  lines here...      │
│                  │                      │
│  (gradient       │  ───────────────   │
│   overlay)       │                      │
│                  │  Description text   │
│  (rotated -90°)  │  truncated after    │
│                  │  5 lines max        │
│                  │                      │
│                  │  (rotated -90°)     │
└──────────────────┴──────────────────────┘
```

### Data Source
```java
// NewsHandler.load() - Kotlin class
RSS Feed URL: Determined by location
- Default: BBC News RSS
- Location-specific feeds possible

newsHandler.load(location, context, callback);

Parsed fields:
  - title → newsHeader
  - description → newsDesc
  - thumbnailUrl → newsImg
```

### News Rotation Logic
```java
// Lines 738-784 - In startMediaRotation()
if (newsIndex >= getNews.size()) {
    Utils.INSTANCE.getNewsList().clear();
    getNews.clear();
    newsIndex = 0;
}

if (Utils.INSTANCE.getNewsList().size() == 0) {
    // Show shimmer loading
    shimmer.startShimmer();
    // Re-fetch RSS
    newsHandler.load(...);
}

// Display current news item
newsHeader.setText(getNews.get(newsIndex).getTitle());
newsDesc.setText(getNews.get(newsIndex).getDescription());
Glide.load(getNews.get(newsIndex).getThumbnailUrl()).into(newsImg);

newsIndex++; // Move to next item
```

### Issues Identified ⚠️

#### **Issue #1: Shimmer Shown DURING Playback**
```java
// Lines 746-750
if (Utils.INSTANCE.getNewsList().size() == 0) {
    shimmer.startShimmer();
    shimmer.setVisibility(View.VISIBLE);
    // NewsHandler loads async, but shimmer blocks entire screen!
}
```
→ **Result:** User sees loading animation interrupting ad playback

#### **Issue #2: No Timeout on RSS Fetch**
- News fetch hangs → shimmer shows forever
- No fallback to cached news
- No skip to next slide

#### **Issue #3: GIF Support Inconsistent**
```java
if (getNews.get(newsIndex).getThumbnailUrl().endsWith(".gif")) {
    Glide.with(context).asGif().load(...);
} else {
    Glide.with(context).load(...);
}
```
→ Only checks file extension, not MIME type
→ Some GIFs might not be detected

#### **Issue #4: Text Truncation Not Consistent**
- Headline: `maxLines="3"` (landscape) vs varies in forced portrait
- Description: `maxLines="5"` (landscape) vs different in forced portrait
- No ellipsis indicator when truncated

#### **Issue #5: News Index Out of Bounds Risk**
```java
if (newsIndex < getNews.size()) {
    newsHeader.setText(getNews.get(newsIndex).getTitle());
    // ...
    newsIndex++;
}
```
→ Race condition: List might be cleared by refresh while accessing

---

## 🔄 Orientation Handling Deep Dive

### The Forced Portrait Complexity

**Hardware Reality:**
- TV is landscape (1920x1080)
- Physically mounted vertically (rotated 90° clockwise)
- Android still sees landscape resolution

**Software Solution:**
```xml
<!-- activity_advert_land_watch.xml -->
<!-- All views rotated -90° in XML -->
<ImageView android:rotation="-90" /> <!-- Waiting logo -->
<ImageView android:rotation="180" /> <!-- Ad image (flipped) -->
<PlayerView android:rotation="180" /> <!-- Video -->
<LinearLayout android:rotation="-90"> <!-- Weather layout -->
  <!-- Content inside also rotated to appear upright -->
</LinearLayout>
```

**Why the complexity?**
1. **Ad content prepared for landscape** viewing
2. **TV mounted vertically** for portrait display
3. **Need to rotate content** to match physical orientation
4. **Different rotation angles** for different content types

### Rotation Matrix

| Content Type | Landscape | Portrait | Forced Portrait |
|-------------|-----------|----------|-----------------|
| **Waiting Logo** | 0° | 0° | -90° |
| **Ad Image** | 0° | 0° | 180° (flipped) |
| **Ad Video** | 0° | 0° | 180° (flipped) |
| **Weather Layout** | 0° | 0° | -90° (each half) |
| **News Layout** | 0° | 0° | -90° (each half) |
| **QR Code** | 0° | 0° | Varies by TV |
| **Brand Logo** | 0° | 0° | -90° |
| **Display Text** | 0° | 0° | -90° |

### Layout File Differences

**AdvertWatching (Landscape/Portrait):**
- Single `ConstraintLayout` root
- Weather: `ConstraintLayout` with guideline-based positioning
- News: `FrameLayout` wrapping `ConstraintLayout`

**AdvertLandWatch (Forced Portrait):**
- Single `ConstraintLayout` root
- Weather: `LinearLayout` with two halves (each rotated -90°)
- News: `LinearLayout` with two halves (each rotated -90°)

**Why different layouts?**
- Forced portrait needs to split screen into two rotated regions
- Each half appears as top/bottom on vertically-mounted TV
- Constraint-based layout doesn't work well with rotation

### Issues Identified ⚠️

#### **Issue #1: Inconsistent Ad Rotation**
```java
// Ad image rotated 180° but video also 180°
// Problem: Ads uploaded for landscape might appear upside-down
```
**Why 180° instead of -90°?**
→ Likely ads are pre-rotated by user during upload
→ But this is not documented anywhere!

#### **Issue #2: QR Code Size Calculation**
```java
// qrCodeImageDimension() - Lines 875-882
int width = point.x;
int height = point.y;
int smallerDimension = width < height ? width : height;
return smallerDimension = smallerDimension * 3 / 4;
```
→ Uses device dimensions, not taking rotation into account
→ QR code might be too small or cut off

#### **Issue #3: No Rotation for All Elements**
Brand logo, QR code, displayText all rotated -90°
But: Ad content rotated 180°
→ Visual inconsistency

#### **Issue #4: Different TextView IDs**
```xml
<!-- Landscape/Portrait uses: -->
android:id="@+id/main_header"
android:id="@+id/news_details"

<!-- Forced Portrait uses: -->
android:id="@+id/main_headerF"
android:id="@+id/news_detailsF"
```
→ Java code must know which layout is active
→ Code duplication in AdvertLandWatch.java vs AdvertWatching.java

---

## 🗄️ Data Architecture

### Singleton: DataHolder
```java
public class DataHolder {
    private static DataHolder instance;
    
    public String time;              // Refresh interval
    public String orient;            // "Landscape"/"Portrait"/"Forced Portrait"
    public String screenID;          // Selected screen ID
    public String location;          // City for weather/news
    public List<MediaModel> allAds;  // Downloaded ads
    public int displayFlag;          // Show ad text overlay?
    public int targetHoursFlag;      // Apply business rules?
    public String locationTypes;     // Screen location type
    public String screenPlayer;      // Player type
    public String screenDevice;      // Device type
    public List<String> tags;        // Screen tags
    public List<String> advertIds;   // Ad IDs in playlist
    public int isData;               // ⚠️ PROBLEMATIC FLAG
}
```

### Room Database

#### **AdEntity** (Local ad cache)
```java
@Entity(tableName = "ads")
public class AdEntity {
    @PrimaryKey String advertId;
    String format;              // "IMAGE" / "VIDEO"
    String localPath;           // /data/data/.../uuid.mp4
    String textTop;             // Overlay text
    String textBottom;
    String textLeft;
    String textRight;
    int duration;               // Milliseconds
    String orientation;         // "Landscape" (not used)
    String screenId;            // Which screen this ad belongs to
    String contractId;
    String targetHours;         // "9/10/11/14/15/16"
    int serverOrder;            // Ad sequence order
    String currency;
    int maxBid;
}
```

#### **ImpressionEntity** (Analytics tracking)
```java
@Entity(tableName = "impressions")
public class ImpressionEntity {
    @PrimaryKey String impressionId; // screenID+advertID+random
    String advertId;
    String contractId;
    String playTimeStamp;       // ISO 8601
    int dayHour;                // 0-23
    int playSec;                // Duration played
    String format;              // "IMAGE"/"VIDEO"
    String screenId;
    String orientation;         // Actual orientation played
    String locationType;
    String screenDevice;
    String screenPlayer;
    String tags;
    String currency;
    int maxBid;
    boolean amountSettled;      // Analytics processed?
}
```

### API Models

#### **WatchingModel** (API response for ad list)
```json
{
  "contractId": "contract123",
  "currency": "USD",
  "maxBid": 10,
  "duration": 15,
  "screenId": "Demo136",
  "adContractData": {
    "advertId": "ad456",
    "format": "video",
    "videoUrl": "media/ads/video.mp4",
    "targetHours": [9, 10, 11, 14, 15, 16],
    "textTop": "Ad title",
    "textBottom": "www.example.com",
    "textLeft": "",
    "textRight": ""
  }
}
```

#### **MediaModel** (In-memory playback model)
```java
public class MediaModel {
    String contractId;
    String currency;
    int maxBid;
    String type;           // "IMAGE"/"VIDEO"/"weather"/"news"
    String url;            // Local file path or "weather"/"news"
    int durationInMillis;
    String displayText;    // Combined bottom/top text
    String info;           // textBottom (for QR)
    String targetHours;    // "9/10/11/14"
    String advertId;
}
```

---

## 🐛 **CRITICAL ISSUES SUMMARY**

### 🔴 **SEVERITY: CRITICAL - App Breaking**

#### **1. DataHolder.isData Flag Prevents Ad Playback**
**Location:** Multiple files  
**Code:**
```java
// SelectScreens.java - Lines 360, 440
DataHolder.getInstance().isData = 5; // Set when NO ads

// AdvertWatching.java - Line 308
if (DataHolder.getInstance().isData == 5 || 
    DataHolder.getInstance().allAds == null || 
    DataHolder.getInstance().allAds.isEmpty()) {
    // Show ONLY weather/news, NO ADS!
}
```
**Problem:** Flag set to 5 when no ads exist, but also checked to determine if ads should play → Logic error causes ads to never play!

**Fix:** Remove `isData` flag completely, only check `allAds`:
```java
if (DataHolder.getInstance().allAds == null || 
    DataHolder.getInstance().allAds.isEmpty()) {
    // Show weather/news only
}
```

---

#### **2. Ad Refresh Deletes Currently Playing Ads**
**Location:** `AdvertWatching.java` lines 768-774, `AdvertLandWatch.java` similar  
**Code:**
```java
handler.postDelayed(() -> {
    db.adDao().deleteAllAds(); // ← Kills playing ads!
    handler.post(() -> getAds(0)); // Re-download
}, newTime * 60 * 1000);
```
**Problem:** While ads are playing, database is wiped and re-populated → Playback disrupted!

**Fix Options:**
1. **Remove auto-refresh** during playback
2. **Download to temp table** first, then swap
3. **Only refresh weather/news**, not ads

---

#### **3. Race Condition: Activity Launches Before Ads Downloaded**
**Location:** `SelectScreens.java` lines 560-587  
**Flow:**
```
Delete DB → API call → Download files (async) → Save to DB → Launch Activity
                                    ↓
                            (Activity might launch here!)
```
**Problem:** `loadedCount` increments, but file downloads are async → Activity launches with empty `allAds`

**Fix:**
```java
// Use CountDownLatch or blocking queue
final CountDownLatch latch = new CountDownLatch(totalCount);
for (each ad) {
    download(ad, () -> latch.countDown());
}
latch.await(); // Block until all done
launchActivity();
```

---

### 🟠 **SEVERITY: HIGH - UX Breaking**

#### **4. No Download Progress Shown to User**
**Location:** `SelectScreens.java` waiting logo  
**Problem:** User sees animated logo indefinitely, no indication if:
- Downloads are progressing
- Downloads failed
- How many ads left

**Fix:** Add ProgressBar with percentage:
```java
progressText.setText("Loading ads: " + loaded + "/" + total);
progressBar.setProgress((loaded * 100) / total);
```

---

#### **5. Weather/News RSS Fetches Block UI**
**Location:** `AdvertWatching.java` lines 746-772  
**Problem:** Shimmer effect shown over entire screen during RSS reload → Interrupts playback

**Fix:** Load in background, keep old data visible:
```java
// Don't show shimmer if data already exists
if (Utils.INSTANCE.getNewsList().size() == 0) {
    showShimmer();
}
newsHandler.load(..., newData -> {
    Utils.INSTANCE.getNewsList().addAll(newData);
    hideShimmer();
});
```

---

#### **6. No Error Recovery for Failed Media**
**Problem:**
- Video fails to load → black screen for full duration
- Image 404 → blank screen for full duration
- No retry mechanism

**Fix:** Add timeout + fallback:
```java
handler.postDelayed(() -> {
    if (exoPlayer.isPlaying() == false) {
        // Video failed to start
        skipToNext();
    }
}, 5000); // 5s timeout
```

---

### 🟡 **SEVERITY: MEDIUM - Quality Issues**

#### **7. Inconsistent Layout Between Orientations**
**Problem:** Weather/news look different in:
- Landscape (ConstraintLayout)
- Portrait (ConstraintLayout)
- Forced Portrait (LinearLayout with rotation)

**Impact:** Branding inconsistency, user confusion

**Fix:** Standardize on single layout approach, use dimensions for orientation-specific sizing

---

#### **8. Field Mislabeling in Code**
```java
rain.setText(feelslike_c); // "rain" shows feels-like temp
hamudity.setText(humidity); // Typo: "hamudity" instead of "humidity"
```
**Impact:** Maintainability nightmare, future confusion

**Fix:** Rename variables to match purpose

---

#### **9. QR Code Size Calculation Broken**
```java
qrImageDimension = qrCodeImageDimension();
// Uses `12%` or `30%` of screen width
// But doesn't account for rotation!
```
**Problem:** In forced portrait, QR code might be cut off or too small

**Fix:** Calculate based on **post-rotation** dimensions

---

#### **10. Memory Leaks**
**Problem:** 
- Retrofit calls not cancelled in `onDestroy()`
- ExecutorService might not terminate
- Handler callbacks might leak activity reference

**Fix:**
```java
@Override
protected void onDestroy() {
    super.onDestroy();
    handler.removeCallbacksAndMessages(null);
    if (call != null && !call.isCanceled()) {
        call.cancel();
    }
    executorService.shutdownNow();
}
```

---

## 📝 **Recommendations**

### **Immediate Actions (This Week)**

1. **Fix Critical Issue #1** - Remove `isData` flag logic
2. **Fix Critical Issue #2** - Disable ad refresh during playback OR implement safe swap
3. **Fix Critical Issue #3** - Add download completion check before launching player
4. **Add progress UI** - Show download percentage to user

### **Short Term (Next Sprint)**

5. **Add error recovery** - Retry failed downloads, skip broken media
6. **Improve news loading** - Background refresh, don't block UI
7. **Standardize layouts** - Choose one approach for all orientations
8. **Add analytics dashboard** - Show impressions sent/pending

### **Long Term (Future Releases)**

9. **Offline mode** - Cache weather/news for X hours
10. **Dynamic orientation detection** - Auto-detect physical mounting
11. **A/B testing support** - Rotate different ad variations
12. **Health monitoring** - Self-report errors to backend

---

## 📐 Architecture Diagram

```
┌───────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                      │
├───────────────────────────────────────────────────────────┤
│  LoginActivity          SelectScreens                     │
│  AdvertWatching         AdvertLandWatch                    │
│  (UI + Lifecycle)       (UI + Lifecycle)                   │
└─────────────┬─────────────────────────┬───────────────────┘
              │                         │
              ▼                         ▼
┌───────────────────────────────────────────────────────────┐
│                     DATA LAYER                            │
├───────────────────────────────────────────────────────────┤
│  DataHolder (Singleton)                                    │
│  ├─ Screen configuration                                   │
│  ├─ Playlist (allAds)                                      │
│  └─ Business rules                                         │
│                                                            │
│  Room Database (SQLite)                                    │
│  ├─ AdEntity (local ad cache)                              │
│  ├─ ImpressionEntity (analytics queue)                     │
│  └─ InfoEntity (logs)                                      │
└─────────────┬─────────────────────────┬───────────────────┘
              │                         │
              ▼                         ▼
┌───────────────────────────────────────────────────────────┐
│                   NETWORK LAYER                           │
├───────────────────────────────────────────────────────────┤
│  RetrofitBuilder (REST API)                                │
│  ├─ POST /authenticate_user (login)                        │
│  ├─ GET /get_screens (screen list)                         │
│  ├─ GET /get_screen_playlists/{id} (ad list)              │
│  ├─ GET /get_url (S3 signed URL)                           │
│  └─ POST /post_impression (analytics)                      │
│                                                            │
│  OkHttpClient (File downloads)                             │
│  ├─ Download ads to internal storage                       │
│  └─ Cache strategy: CacheControl.FORCE_NETWORK             │
│                                                            │
│  Weather API                                               │
│  └─ GET https://api.weatherapi.com/v1/forecast.json       │
│                                                            │
│  NewsHandler (RSS Parser)                                  │
│  └─ Parse location-based RSS feeds                         │
└───────────────────────────────────────────────────────────┘
              │
              ▼
┌───────────────────────────────────────────────────────────┐
│                   MEDIA LAYER                             │
├───────────────────────────────────────────────────────────┤
│  ExoPlayer (Video playback)                                │
│  ├─ MediaItem from local file URI                          │
│  ├─ Listener for playback state                            │
│  └─ Resize mode: FILL / FIT                                │
│                                                            │
│  Glide (Image loading)                                     │
│  ├─ Load from local file / URL                             │
│  ├─ GIF support                                            │
│  └─ Placeholder/error handling                             │
└───────────────────────────────────────────────────────────┘
```

---

## 🎯 **Conclusion**

This Android TV digital signage app has a **solid architectural foundation** but suffers from **critical logic errors** in state management and **timing issues** in async operations. The **forced portrait orientation handling** is innovative but adds complexity that leads to inconsistent layouts.

**Top Priority Fixes:**
1. ✅ Remove `DataHolder.isData` flag bug
2. ✅ Fix ad refresh timing to not interrupt playback
3. ✅ Add proper download completion checks
4. ✅ Show progress UI to users

**System Strengths:**
- Clean separation of activities for different orientations
- Room DB for offline caching
- Comprehensive impression tracking
- Auto-refresh mechanisms for weather/news

**System Weaknesses:**
- Race conditions in async flows
- Limited error recovery
- Inconsistent UI across orientations
- Memory leak risks

---

**End of Code Map**  
*Last Updated: May 4, 2026*

