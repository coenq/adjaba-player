# 🎯 MANUAL DEBUGGING GUIDE - Ad Download Testing

## Phase 1: App Launch & Navigation

### Step 1: Open Adjaba Player App
- Tap the app icon on your TV/device home screen
- OR navigate to: Settings → Apps → Adjaba Player → Open

### Step 2: Login (if required)
- If you see a Login screen, use any stored credentials
- Click "Login" to proceed

### Step 3: Navigate to SelectScreens  
- You should now see the screen selection interface
- Spinners for: Screen, Orientation, Time Refresh

---

## Phase 2: Prepare Test Configuration

### Select Screen
1. Tap the **"Select Screen"** dropdown
2. Choose any available screen (e.g., `l169307`)
3. Note the screen ID for later

### Select Orientation
1. Tap the **"Orientation"** dropdown  
2. Choose **"Landscape"** (recommended for testing)
3. Other options: Portrait, Forced Portrait

### Leave Other Options Default
- Time Refresh: Keep as-is
- Check boxes: Leave unchecked for now

---

## Phase 3: Trigger Ad Download

### Click "Play" Button
1. Once Screen and Orientation are selected
2. Click the large **"Play"** button
3. Watch for the **waiting logo animation** to appear

### What You'll See:
- ✅ Logo scales up with fade-in animation (1-2 seconds)
- ✅ Logo bounces up and down continuously
- ✅ **Loading bar appears below** (if initialized)
- ✅ Loading bar shows progress 0/X to X/X

---

## Phase 4: Download Progress

### Optimal Scenario (✅ Ads Download Successfully):
```
Timeline:
  T+0s    Logo animates in
  T+0.5s  Loading bar appears
  T+1-30s Loading bar fills up (depends on file sizes)
  T+30s   Logo animates out
  T+31s   AdvertWatching launches
```

### Warning Signs (⚠️ Download May Be Failing):
```
1. Loading bar stuck at 0 for >5 seconds
2. Logo animation stops abruptly  
3. Returns to SelectScreens screen
4. See Toast error message
```

---

## Phase 5: Capture Logs While Testing

### Open Terminal (Keep Running During Test):
```powershell
$adbPath = "C:\Users\User\AppData\Local\Android\Sdk\platform-tools\adb.exe"

# CLEAR LOGS FIRST
& $adbPath -s R52MB18CEGR logcat -c

# Then perform test (click Play button)
# Wait 30 seconds...

# CAPTURE LOGS
& $adbPath -s R52MB18CEGR logcat -d SelectScreens > test_logs_selectscreens.txt
& $adbPath -s R52MB18CEGR logcat -d AdvertWatching > test_logs_advertwatching.txt
& $adbPath -s R52MB18CEGR logcat -d > test_logs_full.txt
```

---

## Expected Log Patterns

### ✅ Success Pattern:
```
I/SelectScreens: 🎬 getAds() started - screenID: l169307
I/SelectScreens: 🔗 API call - endpoint: get_screen_playlists/l169307
I/SelectScreens: 📨 API response code: 200
I/SelectScreens: 📦 Ads received from API: 2 ads
I/SelectScreens: ✨ Starting to download 2 ads
D/SelectScreens: 🌐 Calling /media/{path} API for ad ad165784
D/SelectScreens: ✅ Got URL from API: https://eu2.contabostorage.com/...
D/SelectScreens: 💾 Saved ad ad165784 locally
D/SelectScreens: 📊 Inserted ad to Room DB
I/SelectScreens: 📈 Progress: 1/2 ads loaded
I/SelectScreens: 📈 Progress: 2/2 ads loaded
I/SelectScreens: 🎉 ALL ADS DOWNLOADED!
I/SelectScreens: 🚀 Launching AdvertWatching with 2 ads
```

### ⚠️ API Call Fails:
```
E/SelectScreens: ❌ API /media/{path} returned error code: 400
E/SelectScreens: Error body: [error details here]
```

### ❌ Network Error:
```
E/SelectScreens: ❌ Network error calling /media/{path} API
E/SelectScreens: Error: [Exception type] - [message]
```

### ❌ S3 Download Fails:
```
E/SelectScreens: ❌ HTTP Error 403/404
E/SelectScreens: Error response body: [error]
```

---

## Post-Download Success

### If Ads Downloaded OK:
Should see `AdvertWatching` activity with:
- ✅ Logo hidden or visible depending on orientation
- ✅ First ad displayed (image or video)
- ✅ Bottom text showing ad info
- ✅ Weather/News slides may appear between ads

### If Download OK but No Display:
- Check if ads are actually loading in AdvertWatching
- See "Playback Issues" section below

---

## Troubleshooting

### Problem: Logo Stays but No Loading Bar
**Cause**: Download loop not started  
**Action**: 
1. Check SelectScreens logs for "Starting to download X ads"
2. If missing, check getAds() completed successfully
3. Look for "Ads received from API: 0 ads" → No ads to download

### Problem: Loading Bar Stuck
**Cause**: Download hanging on first file  
**Action**:
1. Check `/media/{path}` API response time (should be <1s)
2. Check S3 file size (should download at <1MB/sec)
3. Device internet might be slow/disconnected

### Problem: HTTP 404 on /media Endpoint
**Cause**: API endpoint not found or path format wrong  
**Action**:
1. Verify API server has `/media/{path}` endpoint
2. Check if correct token is in Authorization header
3. Check path format matches what API expects

### Problem: HTTP 403 on S3 Download
**Cause**: Presigned URL expired or no permission  
**Action**:
1. Check S3 bucket policy
2. Verify presigned URL not expired
3. Check CORS settings if cross-origin

---

## Capture & Share Logs

### After test completes, run:
```powershell
$adbPath = "C:\Users\User\AppData\Local\Android\Sdk\platform-tools\adb.exe"

# Save all logs to file
& $adbPath -s R52MB18CEGR logcat -d > full_test_$(Get-Date -Format "yyyyMMdd_HHmmss").txt

# Filter to just errors and our app
& $adbPath -s R52MB18CEGR logcat -d SelectScreens AdvertWatching "*:E" > error_test_summary.txt
```

---

## Summary Checklist

- [ ] App launches without crash
- [ ] Can select screen & orientation  
- [ ] Play button visible & clickable
- [ ] Logo animation appears
- [ ] Loading bar appears & progresses
- [ ] Logs show API calls succeeded
- [ ] Logs show "/media/{path}" returning success
- [ ] Logs show files saving locally
- [ ] Logo animates out
- [ ] AdvertWatching activity launches
- [ ] Ads/content visible on screen

---

## NEXT: Please Do

1. **Clear device logs**: `adb logcat -c`
2. **Open app** and navigate to SelectScreens
3. **Select screen** (any available) and **Landscape**
4. **Click Play** and **wait 30-60 seconds**
5. **Observe**: Loading bar, logo animation, transition to AdvertWatching
6. **Send screenshot** of the result
7. **Run capture command** above to get logs file

**Then reply with:**
- What you see on screen
- Screenshot if possible
- Output/error log if download failed

