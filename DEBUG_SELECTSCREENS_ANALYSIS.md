# 📊 Detailed Debug Report - SelectScreens Activity

## 🎯 CURRENT STATE

**App Running**: ✅ YES
**Activity Visible**: SelectScreens
**Process**: 4668
**Status**: Stable, no crashes

---

## 🔍 SelectScreens Activity Analysis

### Purpose
The SelectScreens activity is a **configuration screen** that allows users to:

1. **Select Screen ID** - Choose which display/screen to show ads on
2. **Select Orientation** - Choose: Landscape, Portrait, or Forced Portrait
3. **Set Refresh Time** - Choose how often to refresh ad content (1, 5, 30, 60, 100 minutes)
4. **Enable Display Text** - Toggle to show/hide text on ads
5. **Enable Business Rules** - Toggle to apply target hours rules for ads

### UI Components Initialized
```java
- spinnerID        (Screen selection dropdown)
- spinner1         (Orientation dropdown)
- spinner2         (Refresh time dropdown)
- play button      (Start ad playback)
- logOut button    (Logout)
- rememberMe       (Remember settings checkbox)
- displayText      (Display text checkbox)
- businessRules    (Business rules checkbox)
```

---

## 📋 STARTUP FLOW

1. **onCreate() Called** ✅
   - Inflation of layout `/activity_select_screen`
   - SharedPreferences initialized
   - RecyclerView/Spinners setup

2. **getIDs() Called** ✅
   - Fetches screen IDs from backend
   - Populates spinnerID options
   - Creates screenPlayerMap, screenDeviceMap, screenLocationMap

3. **UI Ready** ✅
   - All spinners and buttons visible
   - Ready for user interaction

---

## 🎬 PLAY BUTTON FLOW (When User Clicks "Play")

```
1. Validates selections:
   - screen_id != "Select Screen"
   - orient != "Orientation"

2. Stores configuration in DataHolder singleton:
   - DataHolder.screenID = selected_screen_id
   - DataHolder.orient = selected_orientation
   - DataHolder.time = refresh_interval
   - DataHolder.location = screen_location
   - DataHolder.displayFlag = 1/0
   - DataHolder.targetHoursFlag = 1/0

3. Calls getAds(0) to:
   - Fetch ads from REST API
   - Download media files
   - Start AdvertLandWatch or AdvertPressPlay activity
```

---

## 🔗 EXPECTED API CALLS

### From SelectScreens:

1. **getIDs()** - Fetch available screens
   ```
   GET /api/screens  (or similar)
   Response: List of screen configurations
   ```

2. **getAds()** - Fetch ads for selected screen
   ```
   GET /api/screens/{screenId}/ads
   Authorization: Bearer {token}
   Response: List<WatchingModel> with ad details
   ```

3. **getUrl()** - Get presigned URL for media
   ```
   GET /api/media/{mediaId}/url
   Authorization: Bearer {token}
   Response: { url: "https://..." }
   Downloads media and stores locally
   ```

---

## 🎥 EXPECTED NEXT ACTIVITY

After clicking Play button and ads load, app should navigate to:
- **AdvertLandWatch** (for landscape orientation)
- **AdvertPressPlay** (for portrait orientation)

These activities handle:
- Media playback (images/videos)
- Ad rotation
- Weather display
- News ticker
- QR code generation

---

## ⚙️ WHAT WE NEED TO DEBUG NEXT

### Option 1: Test the Play Button Flow
```bash
Steps:
1. Select a screen from the dropdown
2. Select an orientation
3. Click Play button
4. Observe:
   - Does getAds() call succeed?
   - Are media files downloaded?
   - Does next activity launch?
   - Any errors in transition?
```

### Option 2: Test getIDs() Data Loading
```bash
Check:
1. Are screen options populated in spinner?
2. Are screen details (player, device, location) loaded?
3. Any API errors during initial load?
```

### Option 3: Test AdvertLandWatch Navigation
```bash
After clicking Play:
1. Does app transition to ad playback screen?
2. Are ads displayed?
3. Is media rotating?
4. Any specific crashes?
```

---

## 📝 NEXT DEBUG CHECKPOINT

**Current**: SelectScreens UI is working
**Question**: What happens when the user clicks Play?

### What to Look For:

1. **Check logcat for API calls:**
   ```bash
   adb logcat -s "com.adjaba,retrofit,okhttp" -v threadtime
   ```

2. **Look for these patterns:**
   ```
   - "getAds() called"
   - "API response received"
   - "Media download started"
   - "Navigating to AdvertLandWatch"
   - Any "ERROR" or "Exception"
   ```

3. **Test Step-by-Step:**
   - Tap spinner to select screen
   - Tap second spinner to select orientation
   - Tap Play button
   - Observe app behavior
   - Report what happens

---

## 📞 INSTRUCTIONS FOR NEXT PHASE

Please try one of these:

**A) Simulate User Interaction:**
1. Click on the screen dropdown
2. Select the first available screen
3. Select "Landscape" orientation
4. Click "Play" button
5. Tell me what happens next

**B) Check the Dropdown Content:**
1. What screen options are available in the dropdown?
2. Are there any error messages or empty options?

**C) Check API Response:**
1. Are ads loading after clicking Play?
2. Any toast/error messages?
3. Does the app transition to playback screen?

---

## 🛠️ CURRENT DEBUG STATUS

| Component | Status | Notes |
|-----------|--------|-------|
| App Launch | ✅ OK | No crashes |
| SelectScreens UI | ✅ OK | All controls visible |
| Layout Inflation | ✅ OK | Working properly |
| Spinner Setup | ✅ OK | Ready for selection |
| getIDs() | ? | Need to verify screen list is populating |
| getAds() | ? | Need to test Play button click |
| Navigation | ? | Need to verify transition to ad playback |
| Playback Engine | ? | Not yet tested |

---

## 📂 FILES TO EXAMINE

- `LoginActivity.java` - Entry point
- `SelectScreens.java` - Current screen (line 143: `getIDs()` method)
- `AdvertLandWatch.java` - Ad playback screen
- `DataHolder.java` - Singleton data storage
- `RetrofitBuilder.java` - API client

---

## 🎯 READY FOR NEXT STEP

I've confirmed the app launches successfully and SelectScreens is displayed.

**What would you like to debug next?**


