# 🧪 TEST SCENARIO EXECUTION GUIDE

**Date:** May 5, 2026  
**Objective:** Debug and validate all fixes are working correctly  
**Build Status:** ⏳ Building...

---

## Pre-Test Checklist

Before running test scenarios:

- [ ] APK successfully built
- [ ] APK installed on device
- [ ] Device connected via ADB
- [ ] Logcat monitoring ready
- [ ] SelectScreens activity accessible
- [ ] Play button clickable
- [ ] Device has internet connection

---

## Test Scenario 1: Happy Path - Ads Download Successfully ✅

**Objective:** Verify normal operation isn't broken by fixes

**Setup:**
- Ads are configured in backend
- Ad files exist and are accessible
- Weather API is accessible (optional for this test)

**Steps:**
1. Launch SelectScreens activity
2. Click Play button
3. Observe loading/downloading phase (10-15 seconds)
4. Wait for transition to AdvertWatching
5. Observe ad carousel rotation

**Expected Behavior:**
- [ ] SelectScreens shows loading animation
- [ ] Progress bar increments as ads download
- [ ] After all ads processed → fade animation
- [ ] AdvertWatching launches
- [ ] Ads display and rotate
- [ ] Weather slide appears (if configured)
- [ ] News slide appears (if configured)
- [ ] Rotation continues for several minutes

**Failure Indicators:**
- [ ] Stuck on SelectScreens (blue loading screen)
- [ ] Progress bar doesn't move
- [ ] No ads appear (black screen)
- [ ] Crash/ANR errors
- [ ] Timeout > 30 seconds

**Logcat Expected Output:**
```
SelectScreens: 📥 Ad 1/2 - ID: ad165784
SelectScreens: 🌐 Calling /media/{path} API...
SelectScreens: ✅ Downloaded media (size: 5.2 MB)
SelectScreens: 📥 Ad 2/2 - ID: ad165785
SelectScreens: 🌐 Calling /media/{path} API...
SelectScreens: ✅ Downloaded media (size: 3.1 MB)
SelectScreens: ✅ ALL ADS PROCESSED (loaded: 2/2)
SelectScreens: 🚀 Launching AdvertWatching with 2 ads
AdvertWatching: ⚠️ Loading 2 ads for playback
AdvertWatching: 📺 Starting media carousel
```

**Pass Criteria:** ✅ If app launches AdvertWatching within 30 seconds

---

## Test Scenario 2: All Ads Fail (404 Errors) - PRIMARY FIX TEST ⭐

**Objective:** Validate that app launches even when ALL ads fail to download

**Setup:**
- Disable backend ad serving OR
- Configure ads with invalid/fake file paths OR
- Use network filter to block /media/* endpoints

**Steps:**
1. Launch SelectScreens
2. Click Play button
3. Observe for 20 seconds
4. Watch response
5. Check final screen

**Expected Behavior (THIS IS THE FIX):**
- [ ] SelectScreens shows loading animation
- [ ] Logs show API errors for each ad
- [ ] Progress bar increments to 100% anyway
- [ ] After ~15-20 seconds → fade animation
- [ ] AdvertWatching LAUNCHES (doesn't hang!)
- [ ] Weather section is visible
- [ ] News section is visible
- [ ] Rotation alternates: Weather → News → Weather → News...
- [ ] UI is responsive, not frozen

**Failure Indicators (The Bug We're Fixing):**
- [ ] ❌ Stuck on SelectScreens indefinitely
- [ ] ❌ Blue loading screen never goes away
- [ ] ❌ Progress bar stuck at 0% or incomplete
- [ ] ❌ Logcat shows repeated download attempts but no progression
- [ ] ❌ Timeout > 30 seconds
- [ ] ❌ App becomes unresponsive (ANR)

**Logcat Expected Output (shows fixes working):**
```
SelectScreens: 📥 Ad 1/2 - ID: ad165784
SelectScreens: 🌐 Calling /media/{path} API...
SelectScreens: ❌ API /media/{path} returned error code: 404
SelectScreens: 🔄 Incrementing counter: 1/2 processed
SelectScreens: 📥 Ad 2/2 - ID: ad165785
SelectScreens: 🌐 Calling /media/{path} API...
SelectScreens: ❌ API /media/{path} returned error code: 404
SelectScreens: 🔄 Incrementing counter: 2/2 processed
SelectScreens: ✅ ALL ADS PROCESSED (loaded: 2/2)  ← KEY LINE (Fix #2)
SelectScreens: 🚀 Launching AdvertWatching with 0 ads
AdvertWatching: ⚠️ NO ADS AVAILABLE - Showing weather and news only
AdvertWatching: 🔴 STARTING WEATHER+NEWS ROTATION (no ads)
AdvertWatching: 🔴 mediaSwitcher.run() - currentIndex=0, total=2
AdvertWatching: 🌤️  Showing WEATHER
AdvertWatching: 📰 Showing NEWS
```

**Pass Criteria:** ✅ If AdvertWatching launches within 30 seconds WITH 0 ads

**Critical Success Indicator:**
- Line in logcat: `✅ ALL ADS PROCESSED (loaded: 2/2)`
- App doesn't hang
- Weather + News rotate

---

## Test Scenario 3: Weather API Fails - SECONDARY FIX TEST ⭐

**Objective:** Validate that weather gracefully degrades (shows "unavailable") instead of blank

**Setup:**
- Have some ads configured (or run Test 2 first)
- Disable weather API endpoint OR
- Block weather API domain in network filter OR
- Use wrong API key in config

**Steps:**
1. Launch SelectScreens
2. Click Play
3. Wait for AdvertWatching to launch
4. Observe weather section for 10 seconds
5. Take screenshot of weather display

**Expected Behavior (THIS IS THE FIX):**
- [ ] SelectScreens loads normally
- [ ] Ads download (or fail gracefully if no ads)
- [ ] AdvertWatching launches
- [ ] Weather section shows clearly visible text:
  - Temperature: "N/A"
  - Status: "Weather unavailable"
  - Location: [Location name] or "Unknown"
  - Humidity: "--"
  - Wind: "--"
  - Rain/Feels-like: "--"
- [ ] Not blank/black/invisible
- [ ] Not showing stale/cached data
- [ ] Easy to read, clear fallback

**Failure Indicators (The Bug We're Fixing):**
- [ ] ❌ Weather section is completely blank
- [ ] ❌ Weather section is black/dark with no text
- [ ] ❌ Weather fields are empty strings ""
- [ ] ❌ Weather shows old cached data
- [ ] ❌ App crashes when fetching weather
- [ ] ❌ Weather layout disappears entirely

**Logcat Expected Output (shows fix working):**
```
AdvertWatching: 🌐 Calling weather API...
AdvertWatching: ❌ Weather API failed: Unexpected end of stream  ← Error
AdvertWatching: 🌤️  Fallback weather values set                ← Fix applied
AdvertWatching: ✅ Set fallback weather values                  ← Fix confirmed
[UI shows:]
tvTemp.setText("N/A")
tvStatus.setText("Weather unavailable")
humadity.setText("--")
```

**Pass Criteria:** ✅ If weather shows readable fallback values (not blank)

**Visual Test:**
- Take screenshot
- Verify text is visible
- Verify values are reasonable placeholders
- Compare before/after if possible

---

## Test Scenario 4: Network Error (ConnectionException)

**Objective:** Validate error handling for network-level failures

**Setup:**
- Put device in airplane mode OR
- Disable WiFi/Mobile data
- Have SelectScreens already loaded

**Steps:**
1. Launch SelectScreens
2. Click Play button
3. Observe for 20 seconds
4. Check final result

**Expected Behavior:**
- [ ] Logs show network error messages
- [ ] Progress counter still increments
- [ ] App eventually launches (doesn't hang forever)
- [ ] Falls back to weather+news (or whatever ads succeeded)

**Logcat Expected Output:**
```
SelectScreens: ❌ Network error calling /media/{path} API
SelectScreens: ✅ ALL ADS PROCESSED (loaded: 2/2)
SelectScreens: 🚀 Launching AdvertWatching with 0 ads
```

**Pass Criteria:** ✅ If app launches despite network error

---

## Test Scenario 5: Mixed Success/Failure

**Objective:** Validate partial success scenario

**Setup:**
- Configure 3 ads
- Make 1st and 3rd fail, let 2nd succeed (manually block URLs)

**Steps:**
1. Launch SelectScreens
2. Click Play
3. Observe which ads appear
4. Check rotation

**Expected Behavior:**
- [ ] 1 successful ad appears
- [ ] 2 failed ads don't appear
- [ ] Weather + News appear (if configured)
- [ ] Rotation includes: Ad → Weather → News → Ad → ...

**Logcat Expected Output:**
```
SelectScreens: ❌ API /media/ad1.mp4 returned error: 404
SelectScreens: ✅ Downloaded media: ad2.mp4
SelectScreens: ❌ Network error: ad3.mp4
SelectScreens: ✅ ALL ADS PROCESSED (loaded: 3/3)
SelectScreens: 🚀 Launching AdvertWatching with 1 ads
```

**Pass Criteria:** ✅ If app launches with 1 successful ad

---

## Logcat Monitoring Command

```powershell
# Monitor SelectScreens, AdvertWatching, and AdvertLandWatch logs in real-time
adb logcat SelectScreens:V AdvertWatching:V AdvertLandWatch:V *:S

# Save to file for later analysis
adb logcat -c
adb logcat > debug_test_logs.txt

# After test, filter for key markers
Select-String -Path "debug_test_logs.txt" -Pattern "✅|❌|🚀|API|PROCESSED|Weather"
```

---

## Screenshot Capture Commands

```powershell
# Before starting test
adb shell screencap -p /sdcard/before_test.png
adb pull /sdcard/before_test.png before_test.png

# After test completes
adb shell screencap -p /sdcard/after_test.png
adb pull /sdcard/after_test.png after_test.png

# Compare results
Write-Host "Before:" (Get-Item before_test.png).Length
Write-Host "After:" (Get-Item after_test.png).Length
```

---

## Test Matrix

| Scenario | Priority | Status | Duration | Pass Criteria |
|----------|----------|--------|----------|--------------|
| **1: Happy Path** | 🔴 Required | ⏳ Pending | 15-20s | App launches with ads |
| **2: All Ads Fail** | 🔴 CRITICAL | ⏳ Pending | 20-25s | App launches, no hang |
| **3: Weather Fails** | 🟡 Important | ⏳ Pending | 10-15s | Shows fallback values |
| **4: Network Error** | 🟡 Important | ⏳ Pending | 20-30s | Doesn't hang forever |
| **5: Mixed Failures** | 🟢 Nice-to-have | ⏳ Pending | 15-20s | Shows available content |

---

## Success Criteria Summary

**Minimum (Must Pass):**
- [ ] Scenario 1: Happy path works (doesn't break existing functionality)
- [ ] Scenario 2: All ads fail → app launches (key fix: no more hanging)

**Should Pass:**
- [ ] Scenario 3: Weather fails → shows fallback (key fix: not blank)
- [ ] Scenario 4: Network error → doesn't hang

**Nice-to-Have:**
- [ ] Scenario 5: Partial success works gracefully

---

## Quick Test Command (run all scenarios)

```powershell
# 1. Build APK
.\gradlew.bat assembleDebug

# 2. Install on device
$adb = "C:\Users\User\AppData\Local\Android\Sdk\platform-tools\adb.exe"
& $adb -s R52MB18CEGR install app/build/outputs/apk/debug/app-debug.apk

# 3. Start logging
& $adb -s R52MB18CEGR logcat -c
& $adb -s R52MB18CEGR logcat > test_logs.txt &

# 4. Run tests manually (via UI)
# - Launch app
# - Click Play
# - Observe for 20 seconds
# - Repeat for different scenarios

# 5. Stop logging and analyze
# (kill logcat process)
Select-String -Path "test_logs.txt" -Pattern "✅|ALL ADS PROCESSED|Weather unavailable"
```

---

## Notes

- Each test scenario should take 15-30 seconds
- Total testing time: ~2 hours (if doing all scenarios multiple times)
- Take screenshots/videos for documentation
- Capture full logcat output for analysis
- Note any crashes or unexpected behavior

---

## Status

🔨 Build Status: ⏳ IN PROGRESS  
📱 Device Testing: ⏳ PENDING  
✅ Test Scenarios: Ready to Execute


