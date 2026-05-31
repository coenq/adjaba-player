# 🎯 ADJABA PLAYER - EMULATOR DEPLOYMENT & TESTING GUIDE

**Date**: May 16, 2026  
**Status**: Deployment in Progress  
**Target**: Complete 5-feature verification on Android Emulator

---

## 📍 CURRENT DEPLOYMENT STATUS

### Scripts Running
- ✓ `simple_deploy.bat` - Main deployment script (running)
- ✓ Build verification in progress
- ✓ App installation to emulator in progress

### Expected Timeline
- **Build**: 2-3 minutes
- **Emulator startup**: 1-5 minutes (first time 5 min, subsequent 1-2 min)
- **Installation**: 1 minute
- **Verification**: 1 minute
- **Total**: 5-12 minutes

---

## 🎮 WHAT YOU'LL TEST (5 Features)

### Feature 1: Kolkata Local Time (IST)
```
Location: Kolkata, India
Expected: Clock displays IST (UTC+5:30)
Result: [PENDING - Manual Test]

WHY THIS MATTERS:
- Digital signage in different locations should show local time
- Kolkata is UTC+5:30, different from device time
- Makes schedule/timing relevant to locale

CODE CHANGES:
- File: AdvertWatching.java (line 703-741)
- File: AdvertLandWatch.java (identical)
- Method: getTimeZoneForLocation()
- Maps: "kolkata" → "Asia/Kolkata"
```

### Feature 2: Location-Based Weather
```
Location: Kolkata, India
Expected: Weather API fetches Kolkata data, not device location
Result: [PENDING - Manual Test]

WHY THIS MATTERS:
- Each display location should show its local weather
- Weather for location context (relevant to viewers)
- Not tied to device physical location

VERIFICATION:
- ✓ Already implemented (verified in code)
- Uses: DataHolder.getInstance().location
- API: OpenWeatherMap (Kolkata coordinates)
```

### Feature 3: News Images Loading
```
Location: Kolkata, India
Expected: News headlines with images from Times of India RSS
Result: [PENDING - Manual Test]

WHY THIS MATTERS:
- News content location-specific
- Images should fetch without errors
- Professional appearance requires images

VERIFICATION:
- ✓ Already implemented (verified in code)
- RSS Source: Times of India (India → TOI mapping)
- Location: DataHolder.getInstance().location
```

### Feature 4: Landscape Headline Position
```
Mode: Landscape rotation (Ctrl+F11)
Expected: News headline 56dp from bottom (above adjaba logo)
Result: [PENDING - Manual Test]

WHY THIS MATTERS:
- Landscape viewing is common on TV
- Logo placement important for brand
- Headline must not overlap logo
- Professional layout

CODE CHANGES:
- File: fragment_advert_watching.xml (line 468)
- Changed: marginBottom from 16dp → 56dp
- Result: Headline moves 40dp higher
- Clears logo area (56dp ≈ 18mm)
```

### Feature 5: QR Code Visibility
```
Rules: Ads = VISIBLE, News = HIDDEN, Weather = HIDDEN
Expected: QR codes only on ads, never on news/weather
Result: [PENDING - Manual Test]

WHY THIS MATTERS:
- QR links should only point to ads
- News should not have clickable QR
- Weather should not have clickable QR
- Clear user expectations

VERIFICATION:
- ✓ Already implemented (verified in code)
- Method: setVisibility(View.GONE) for non-ads
- Pattern: Consistent across both activities
```

---

## 🚀 HOW TO TEST

### Step 1: Check Emulator Status
While script is running, watch for:
- ✓ Build "BUILD SUCCESSFUL"
- ✓ Emulator startup message
- ✓ "App installed" confirmation
- ✓ "Launching app" message

### Step 2: When App Launches
You'll see the Adjaba Player interface:
- Navigation using arrow keys
- Select using Enter key
- Back using Escape
- Landscape via Ctrl+F11

### Step 3: Run Each Test
Follow: `MANUAL_TESTING_GUIDE.md` for detailed steps

### Step 4: Document Results
- [ ] All features work? → Production ready
- [ ] Issues found? → Capture logs and fix

---

## 📊 TESTING CHECKLIST

```
BEFORE TESTING:
☐ Emulator is running
☐ App is launched (shows UI)
☐ You can navigate with arrow keys
☐ Logs are readable (if needed)

TEST 1 - KOLKATA TIME
☐ Select location: "Kolkata, India"
☐ Clock shows IST (UTC+5:30)
☐ Time format is correct (HH:MM AM/PM)
☐ Not showing device local time

TEST 2 - WEATHER
☐ Weather widget visible
☐ Location: "Kolkata" or "Kolkata, India"
☐ Temperature displayed
☐ Condition shown (sunny, cloudy, etc.)

TEST 3 - NEWS IMAGES
☐ News headlines visible
☐ Images are showing (not broken icons)
☐ At least 3-5 headlines visible
☐ Images load within 10 seconds

TEST 4 - LANDSCAPE
☐ Press Ctrl+F11 to rotate landscape
☐ Headline text still visible
☐ Headline ABOVE adjaba logo
☐ No text overlapping logo
☐ Professional appearance

TEST 5 - QR CODES
☐ On ads: QR code VISIBLE
☐ On news: QR code HIDDEN
☐ On weather: QR code HIDDEN
☐ Consistent behavior

FINAL ASSESSMENT:
☐ ALL TESTS PASS → Ready for Production
☐ SOME TESTS FAIL → Needs investigation
☐ CRITICAL ISSUES → Review code changes
```

---

## 📁 KEY FILES

### Deployment Files
- `simple_deploy.bat` - Main deployment script
- `auto_deploy_and_test.bat` - Alternative with logging
- `deploy_and_test.ps1` - PowerShell version
- `setup_emulator.bat` - AVD creation only
- `run_emulator.bat` - Emulator startup only

### Testing Files
- `MANUAL_TESTING_GUIDE.md` - Detailed test procedures
- `QUICK_REFERENCE.txt` - Commands and navigation
- `EMULATOR_SETUP_GUIDE.md` - Complete setup documentation

### Updated App Files (Containing Fixes)
- `AdvertWatching.java` - Added timezone support + helper method
- `AdvertLandWatch.java` - Identical timezone support
- `utils.kt` - Added cityTimeZones map (19 cities)
- `fragment_advert_watching.xml` - Headline margin 56dp

---

## 🔧 EMULATOR NAVIGATION

| Key | Action |
|-----|--------|
| ↑↓←→ | Navigate / D-pad |
| Enter | Select / Confirm |
| Escape | Back Button |
| Ctrl+F11 or F1 | Rotate Landscape ↔ Portrait |
| Ctrl+Home | Home Button |
| Ctrl+F5 | Menu |

---

## 💾 LOG MONITORING

### Open in Separate Terminal
```cmd
adb logcat | findstr "Adjaba\|Timezone\|Weather\|News"
```

### What to Look For
- **Timezone**: `"🕐 Timezone for 'Kolkata': Asia/Kolkata"`
- **Weather**: `"Fetching weather for location: Kolkata"`
- **News**: `"Loading news for location: Kolkata"`
- **QR**: `"QR code visibility: GONE"` (on news/weather)
- **Errors**: `"Error\|Exception\|CRASH"`

---

## ⏱️ TIMING REFERENCE

### Build Phase
```
gradle clean      : ~30s
gradle build      : ~2-3 min (first time)
gradle build      : ~1-2 min (incremental)
```

### Emulator Phase
```
AVD creation      : 2-3 min (one time only)
Emulator startup  : 5 min (first time cold boot)
Emulator startup  : 1-2 min (subsequent boots)
```

### Installation Phase
```
APK build         : 1-2 min
ADB install       : 30-60 sec
App launch        : 5-10 sec
```

### Total Deployment Time
```
First run  : 10-15 minutes
Subsequent : 5-8 minutes
```

---

## ✨ EXPECTED RESULTS

### When Deployment Succeeds
```
✓ Emulator window appears
✓ Android boot animation plays
✓ App interface is visible
✓ Can navigate with arrow keys
✓ Location selection screen loads
✓ No crashes in logcat
```

### When Tests Pass
```
✓ Kolkata time shows IST (UTC+5:30) offset
✓ Weather shows Kolkata data, not device location
✓ News headlines load with images
✓ Landscape headline positioned above logo
✓ QR codes: ads YES, news NO, weather NO
```

---

## 🔴 COMMON ISSUES & RESOLUTIONS

### Build Fails
```
CAUSE: Android SDK issue, build cache, or network
FIX:   1. Delete build folder: rm -r C:\project\adjaba-player\build
       2. Re-run: gradlew clean build
       3. Check internet connectivity
```

### Emulator Won't Start
```
CAUSE: AVD not created, KVM disabled, or resources
FIX:   1. Verify AVD exists: adb devices
       2. Enable virtualization in BIOS
       3. Reduce RAM: edit run_emulator.bat
       4. Try compatibility mode
```

### App Crashes
```
CAUSE: Runtime error or incompatible API
FIX:   1. View crash log: adb logcat | findstr "Exception"
       2. Check logcat for specific error
       3. Ensure API 32+ device in emulator
       4. Verify app built for this architecture
```

### Features Not Working
```
CAUSE: Logic issue or incomplete implementation
FIX:   1. Review code changes in files above
       2. Check logcat for feature-specific logs
       3. Verify DataHolder has location set
       4. Test with known working location first
```

---

## 📞 DEBUGGING COMMANDS

### Quick Diagnostics
```cmd
REM Check Android SDK
echo %ANDROID_SDK_ROOT%

REM List virtual devices
adb devices

REM List AVDs
emulator -list-avds

REM Check app is installed
adb shell pm list packages | findstr adjaba

REM Get app version
adb shell dumpsys package com.adjaba.adplayer | findstr version

REM Clear app data
adb shell pm clear com.adjaba.adplayer

REM Uninstall app
adb uninstall com.adjaba.adplayer
```

### Advanced Debugging
```cmd
REM View full device logs
adb logcat > device_logs.txt

REM Get device info
adb shell getprop

REM Get timezone
adb shell getprop persist.sys.timezone

REM Monitor memory
adb shell dumpsys meminfo com.adjaba.adplayer

REM Monitor network
adb shell logcat | findstr "http"

REM Monitor storage
adb shell df /data
```

---

## 📊 SUCCESS CRITERIA

### Build Phase
- ✓ `BUILD SUCCESSFUL` message appears
- ✓ No compilation errors (warnings OK)
- ✓ APK generated (~35MB)

### Installation Phase
- ✓ `installDebug SUCCESSFUL` message
- ✓ App package shows in device
- ✓ No ADB errors

### Functional Phase
- ✓ App launches without crash
- ✓ Can navigate UI with arrow keys
- ✓ Features respond to input

### Testing Phase
- ✓ All 5 features pass verification
- ✓ No unexpected crashes
- ✓ Logs show expected messages

### Overall
- ✓ **PASS**: All steps complete, all features work
- ✓ **READY**: Move to production deployment

---

## 📝 TESTING SESSION TEMPLATE

```
Date: _______________
Tester: _______________
Device: Emulator - Adjaba_TV_Test
Build: _______________

DEPLOYMENT RESULTS
Build:        ☐ PASS   ☐ FAIL
Installation: ☐ PASS   ☐ FAIL
Launch:       ☐ PASS   ☐ FAIL
Overall:      ☐ PASS   ☐ FAIL

FEATURE TEST RESULTS
1. IST Time:        ☐ PASS   ☐ FAIL   Notes: _____________
2. Weather:         ☐ PASS   ☐ FAIL   Notes: _____________
3. News Images:     ☐ PASS   ☐ FAIL   Notes: _____________
4. Landscape:       ☐ PASS   ☐ FAIL   Notes: _____________
5. QR Codes:        ☐ PASS   ☐ FAIL   Notes: _____________

FINAL STATUS: ☐ READY FOR PRODUCTION   ☐ NEEDS FIXES
```

---

## 🎓 BACKGROUND INFO

### Why Emulator?
- Fast testing without physical device
- Controlled environment
- Repeatable test conditions
- Great for development iteration

### Why These 5 Features?
1. **Timezone**: Location-aware display
2. **Weather**: Remote API integration
3. **News**: Content fetching
4. **Layout**: UI/UX correctness
5. **QR**: Business logic (ads only)

Together they verify: network, location, UI, logic, and display correctness

### What Changed Between Versions?
- Previous: All affected locations showed device time/location
- Now: Each location shows its own time/data via playlist API
- Improvement: Better UX for distributed signage

---

## 24/7 Testing Approach

### Continuous Integration Ready
```
1. Emulator auto-boots from script
2. App auto-installs
3. Test scenarios auto-run (via ADB)
4. Results logged automatically
5. Reports generated

Ideal for CI/CD pipeline (Jenkins, GitLab CI, GitHub Actions)
```

---

## 📞 NEXT STEPS

1. **Deployment Complete?**
   - [ ] Watch script finish
   - [ ] Check log file for status
   - [ ] Verify "SUCCESSFUL" messages

2. **App Launching?**
   - [ ] See Adjaba Player UI
   - [ ] Can navigate with keyboard
   - [ ] No crashes

3. **Run Manual Tests**
   - [ ] Follow MANUAL_TESTING_GUIDE.md
   - [ ] Test each of 5 features
   - [ ] Document results

4. **All Tests Pass?**
   - [ ] YES → Commit and deploy to production
   - [ ] NO → Review logs, identify issue, fix code

---

**Status**: Deployment in progress  
**Last Updated**: May 16, 2026  
**Next Check**: Review log files after script completes  

🚀 **WATCH FOR COMPLETION MESSAGE IN TERMINAL**


