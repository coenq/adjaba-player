# Ads Not Playing - Diagnostic & Fix Guide

## Summary of Changes Made

I've added **comprehensive logging** to trace the ads loading flow. This will help identify where exactly the ads are disappearing.

### Files Modified:
1. **SelectScreens.java** - Added detailed logging for:
   - API call initiation
   - API response receipt and validation
   - Each ad download progress
   - Database save operations
   - DataHolder population
   - Activity navigation

2. **AdvertWatching.java** - Added detailed logging for:
   - onCreate initialization
   - DataHolder state inspection
   - Media list population
   - Playback rotation

---

## How to Use the Diagnostic Logs

### Step 1: Clear Previous Logs
```bash
adb logcat -c
```

### Step 2: Run Your Test Scenario
1. Authenticate as `boss:password`
2. Select orientation: **Landscape**
3. Select screen: **Demo136**
4. Click **PLAY**

### Step 3: Monitor Logs in Real-Time
```bash
adb logcat | grep -E "SelectScreens|AdvertWatching"
```

### Step 4: Save Logs to File for Analysis
```bash
adb logcat > ads_debug.txt
# Let it run while you execute the test, then Ctrl+C
```

---

## Expected Log Output (If Working Correctly)

```
I/SelectScreens: 🎬 getAds() started - screenID: Demo136
I/SelectScreens: ✅ Database cleared - All old ads deleted
I/SelectScreens: 🔗 API call - endpoint: get_screen_playlists/Demo136
I/SelectScreens: 📨 API response code: 200
I/SelectScreens: 📦 Ads received from API: 3 ads
I/SelectScreens: ✨ Starting to download 3 ads
I/SelectScreens:   📥 Ad 1/3 - ID: ad_001
D/SelectScreens:   🌐 Downloading ad ad_001 - URL: https://...
D/SelectScreens:   ✅ Got resolved URL for ad ad_001
D/SelectScreens:   💾 Saved ad ad_001 locally: 8f9d2e4...mp4
D/SelectScreens:   📊 Inserted ad ad_001 to Room DB
I/SelectScreens: 📈 Progress: 1/3 ads loaded
I/SelectScreens:   📥 Ad 2/3 - ID: ad_002
D/SelectScreens:   🌐 Downloading ad ad_002 - URL: https://...
D/SelectScreens:   ✅ Got resolved URL for ad ad_002
D/SelectScreens:   💾 Saved ad ad_002 locally: 9g8e3f5...jpg
D/SelectScreens:   📊 Inserted ad ad_002 to Room DB
I/SelectScreens: 📈 Progress: 2/3 ads loaded
I/SelectScreens:   📥 Ad 3/3 - ID: ad_003
D/SelectScreens:   🌐 Downloading ad ad_003 - URL: https://...
D/SelectScreens:   ✅ Got resolved URL for ad ad_003
D/SelectScreens:   💾 Saved ad ad_003 locally: 1h0f4g6...mp4
D/SelectScreens:   📊 Inserted ad ad_003 to Room DB
I/SelectScreens: 📈 Progress: 3/3 ads loaded
I/SelectScreens: 🎉 ALL ADS DOWNLOADED! Preparing to launch AdvertWatching...
I/SelectScreens:    Retrieved 3 ads from DB for screenId: Demo136
I/SelectScreens:    Created MediaModel list with 3 items
I/SelectScreens:    ✅ Updated DataHolder.allAds with 3 MediaModels
I/SelectScreens:    isData flag = 0
I/SelectScreens: 🚀 Launching AdvertWatching with 3 ads

I/AdvertWatching: 🎬 onCreate() - Initializing playback
I/AdvertWatching:    isDataLoaded: false
I/AdvertWatching:    DataHolder.isData: 0
I/AdvertWatching:    DataHolder.allAds: 3 ads
I/AdvertWatching:    📰 News loaded: 10 articles
I/AdvertWatching: ✨ Starting playback with 3 ads
I/AdvertWatching:    Total items in rotation: 5 (ads + weather + news)
I/AdvertWatching: 🔄 startMediaRotation() - Total items: 5
I/AdvertWatching: ▶️  Playing item 1/5 (Hour: 14)
I/AdvertWatching:    Type: VIDEO, Duration: 30s
I/AdvertWatching:    Ad ID: ad_001
I/AdvertWatching:    🎬 Playing VIDEO
```

---

## Troubleshooting by Log Pattern

### ❌ Problem: "API response code: 400/401/403"
**Cause:** Authentication or permission issue
```
I/SelectScreens: 📨 API response code: 401
I/SelectScreens: ❌ API error - response code: 401
```
**Solution:**
- Verify token is still valid
- Check if user:boss still has access to Demo136
- Check authentication flow in LoginActivity

---

### ❌ Problem: "API response: 200 but Ads received from API: 0 ads"
**Cause:** API endpoint returns empty list for this screen
```
I/SelectScreens: 📨 API response code: 200
I/SelectScreens: 📦 Ads received from API: 0 ads
I/SelectScreens: ⚠️ No ads returned from API for screenID: Demo136
```
**Solution:**
- Verify Demo136 has ads assigned in backend
- Check if ads are published/active
- Compare with another screen that has ads
- Check backend logs for getScreenPlaylist endpoint

---

### ❌ Problem: "Downloaded but not found in DB"
**Cause:** Downloaded ads not being saved to Room database correctly
```
I/SelectScreens: 📈 Progress: 3/3 ads loaded
I/SelectScreens: 🎉 ALL ADS DOWNLOADED!
I/SelectScreens:    Retrieved 0 ads from DB for screenId: Demo136
```
**Solution:**
- Check Room database schema is correct
- Verify AdEntity is being created properly
- Check if screenId in AdEntity matches query screenId
- Check database read permissions

---

### ❌ Problem: "Ads loaded but AdvertWatching shows no ads"
**Cause:** DataHolder not being passed correctly
```
I/SelectScreens: ✅ Updated DataHolder.allAds with 3 MediaModels
I/AdvertWatching:    DataHolder.allAds: null  // ← PROBLEM!
```
**Solution:**
- Verify DataHolder is singleton (should be)
- Check if AdvertWatching is being created in new process
- Verify Intent flags don't cause app restart

---

### ❌ Problem: "Ads in memory but rotation not starting"
**Cause:** MediaList empty in startMediaRotation
```
I/AdvertWatching: ✨ Starting playback with 3 ads
I/AdvertWatching: 🔄 startMediaRotation() - Total items: 0  // ← PROBLEM!
```
**Solution:**
- Check `insertWeatherEveryThreeAds()` logic
- Verify MediaModel list is being populated correctly
- Check if news loading is interfering

---

## Next Steps to Debug

### 1. Run the logging version
- Rebuild: `./gradlew :app:assembleDebug`
- Install: `adb install -r app/build/outputs/apk/debug/app-debug.apk`
- Run test scenario
- Capture logs

### 2. Share logs with me
- Extract logs: `adb logcat > logs_output.txt`
- Look for ERROR or WARNING lines
- Send key log snippets

### 3. Verify each step works:

**Test 1: Login & Navigate to SelectScreens**
```
Expected: Screen dropdown populated with Demo136
Log: Should see screenOptions populated
```

**Test 2: Select Demo136 and Click PLAY**
```
Expected: Waiting logo appears, progress visible
Log: Should see getAds() called with screenId: Demo136
```

**Test 3: Wait for ads to load (should take 10-30 seconds)**
```
Expected: Each ad download shows progress
Log: Should see multiple "Progress: X/Y" messages
```

**Test 4: AdvertWatching launches**
```
Expected: Ads start playing immediately
Log: Should see "AdvertWatching onCreate" logs
```

---

## Critical Code Paths to Check

### Path 1: SelectScreens → API → getAds()
```
SelectScreens.onClick(PLAY)
  └─ setWaitingLogo() [shows animation]
  └─ getAds(0) [called on main thread executor]
       └─ Background thread deletes old ads
       └─ Calls API.getAdsByScreen(screenId)
            ├─ onResponse: Loop through each ad
            │   └─ Call getUrl() for each ad [in executor thread]
            └─ onFailure: Log error
```

### Path 2: Download & Store
```
getUrl(adId, path, ...)
  └─ API.getUrl(path) [get direct download link]
       ├─ downloadFileToInternalStorage() [download file]
       ├─ Create AdEntity with local path
       ├─ Room.insertAd() [save to database]
       └─ Check if all ads loaded
            └─ When count == total: Query DB & create MediaModels
                 └─ DataHolder.allAds = mediaModels
                 └─ Navigate to AdvertWatching
```

### Path 3: AdvertWatching Playback
```
AdvertWatching.onCreate()
  └─ Load news & weather
  └─ Check: isData == 5 OR allAds == null OR allAds.isEmpty()
       ├─ TRUE: Show weather & news only
       └─ FALSE: Start media rotation with ads
```

---

## Code Quality Improvements Needed

### Priority 1 (CRITICAL) - These will be addressed:
- [x] Add comprehensive logging
- [ ] Fix DataHolder initialization issue (if it exists)
- [ ] Prevent duplicate getAds() calls in AdvertWatching
- [ ] Add error toast notifications

### Priority 2 (HIGH) - These should be addressed:
- [ ] Add proper error handling for network failures
- [ ] Add timeout management for long-running operations
- [ ] Validate that screenId format matches between API and DB query
- [ ] Add retry logic for failed downloads

### Priority 3 (MEDIUM) - Nice to have:
- [ ] Add progress bar updates during download
- [ ] Implement selective refresh instead of full reload
- [ ] Add bandwidth-aware quality selection
- [ ] Cache ads more efficiently

---

## Quick Reference: Log Markers

| Marker | Meaning | Status |
|--------|---------|--------|
| 🎬 | Starting major operation | INFO |
| 🔗 | Making API call | INFO |
| 📨 | API response received | INFO |
| 📦 | Data received from server | INFO |
| 🌐 | Downloading file | DEBUG |
| 💾 | Saving to local storage | DEBUG |
| 📊 | Progress update | INFO |
| 🎉 | Task completed successfully | INFO |
| ⚠️  | Warning - issue but continuing | WARN |
| ❌ | Error encountered | ERROR |
| ✅ | Verification passed | DEBUG |
| ✨ | Starting major step | INFO |
| 🚀 | Launching activity | INFO |
| 🔄 | Starting rotation/loop | INFO |
| ▶️  | Playing item | DEBUG |
| 📰 | News/info message | DEBUG |

---

## How to Read the Analysis

After collecting logs, look for:

1. **Is API being called?**
   - Look for: `🔗 API call - endpoint:`
   - If missing: Play button click not working

2. **Is API returning ads?**
   - Look for: `📨 API response code: 200` followed by `📦 Ads received`
   - If shows `0 ads`: Backend issue or wrong screenId

3. **Are ads being downloaded?**
   - Look for: `🌐 Downloading ad` messages
   - Check count matches expected number

4. **Are ads being saved to DB?**
   - Look for: `💾 Saved ad` and `📊 Inserted ad`
   - Count should match API response

5. **Is AdvertWatching getting the ads?**
   - Look for: `DataHolder.allAds: X ads`
   - Should match the count from SelectScreens

6. **Is playback starting?**
   - Look for: `▶️  Playing item`
   - Should see alternating ads, weather, and news

---

## Build & Run Commands

```bash
# Build debug APK
cd /c/project/adjaba-player
./gradlew :app:assembleDebug

# Install on emulator/device
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Clear logs
adb logcat -c

# Run app manually or via adb
adb shell am start -n com.adjaba/.activities.LoginActivity

# View logs
adb logcat | grep -E "SelectScreens|AdvertWatching|adjaba"

# Export logs
adb logcat > debug_output.txt
```

---

## Next: What I'm Ready to Fix

Once you provide logs showing where it fails, I can implement targeted fixes for:

1. **If API is not returning ads:** Debug API contract or add retry logic
2. **If downloads are failing:** Fix network handling or resume logic
3. **If DB saving is failing:** Fix Room schema or query logic
4. **If playback not starting:** Fix DataHolder passing or state management
5. **If duplicate calls issue:** Prevent getAds() re-calling in AdvertWatching

Let me know when you've captured the logs! 🚀


