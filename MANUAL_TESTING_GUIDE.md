# Comprehensive Manual Testing Guide - Adjaba Player

**Date**: May 16, 2026  
**Purpose**: Verify all 5 features after emulator deployment  
**Expected Time**: 10-15 minutes for thorough testing

---

## 📋 Test Environment Setup

### Prerequisites
- Emulator running (see deployment status)
- App installed and launched
- Emulator in foreground with focus

### Navigation Controls
- **Arrow Keys** = D-pad / Navigate menus
- **Enter** = Select / Confirm
- **Escape** = Back button
- **Ctrl+F11 or F1** = Rotate landscape/portrait
- **Ctrl+Home** = Home button

### Monitoring Logs
Open terminal in another window:
```batch
adb logcat | findstr "Adjaba\|Timezone\|Weather\|News"
```

---

## ✅ TEST 1: Kolkata Local Time (IST)

**Feature**: When screen location is "Kolkata, India", clock should display IST (UTC+5:30)

### What Changed
- File: `AdvertWatching.java` and `AdvertLandWatch.java`
- Added timezone mapping with 19 cities
- Kolkata → "Asia/Kolkata" → UTC+5:30

### Test Steps

1. **Select Location**
   - [ ] In app: Navigate to location selection
   - [ ] Enter or select: "Kolkata, India"
   - [ ] Note the device local time zone first

2. **Observe Clock Display**
   - [ ] Find clock display (usually bottom of screen)
   - [ ] Format should be: `HH:MM AM/PM`
   - [ ] Example: If device is in UTC+0 (London), Kolkata should be ~5.5 hours ahead

3. **Expected Results**
   - ✓ Clock shows IST (UTC+5:30)
   - ✓ Not showing device local time
   - ✓ Format is correct (HH:MM AM/PM)
   - ✓ Time is reasonable (not 12:00 or showing errors)

4. **Verification in Code**
   - Check logs for: `"🕐 Timezone for 'Kolkata': Asia/Kolkata"`
   - If no match: `"🕐 No timezone match for 'Kolkata', using device default"`

### Test Result: ☐ PASS / ☐ FAIL / ☐ PARTIAL

**Evidence**: 
- [ ] Time displayed: ________________
- [ ] Device time: ________________
- [ ] Difference: ________________
- [ ] Log output: ________________

---

## ✅ TEST 2: Weather by Location

**Feature**: Weather API should use screen location (Kolkata) not device location

### What Changed
- Already implemented (verified in existing code)
- Weather API uses `DataHolder.getInstance().location`
- Kolkata coordinates: 22.5726°N, 88.3639°E

### Test Steps

1. **Select Same Location**
   - [ ] Ensure still using "Kolkata, India"
   - [ ] Navigate to weather display screen
   - [ ] Wait 3-5 seconds for data to load

2. **Verify Weather Display**
   - [ ] Weather widget is visible
   - [ ] Location label shows: "Kolkata" or "Kolkata, India"
   - [ ] Temperature is displayed
   - [ ] Weather condition is shown (sunny, cloudy, etc.)

3. **Check Data Accuracy**
   - [ ] Temperature is reasonable for Kolkata (~27-32°C typically)
   - [ ] Not showing device location weather
   - [ ] Data updates after waiting

4. **Expected Results**
   - ✓ Weather shows Kolkata data
   - ✓ Location label correct
   - ✓ Temperature reasonable
   - ✓ API fetched without errors

### Verification Logs
Look for:
- `"Fetching weather for location: Kolkata"`
- `"Weather API response received"`
- `"Temperature: XX°C"` or similar

### Test Result: ☐ PASS / ☐ FAIL / ☐ PARTIAL

**Evidence**:
- [ ] Weather shown: YES / NO
- [ ] Location correct: YES / NO
- [ ] Temperature: ________________
- [ ] Condition: ________________

---

## ✅ TEST 3: News Images Loading

**Feature**: News headlines should display with images from Times of India RSS feed

### What Changed
- Already implemented (verified in existing code)
- News handler receives location from `DataHolder.getInstance().location`
- Utils.kt has India → "Times of India" RSS mapping

### Test Steps

1. **Navigate to News Screen**
   - [ ] Keep location as "Kolkata, India"
   - [ ] Navigate to news headlines section
   - [ ] Wait 5-10 seconds for headlines to load

2. **Verify Images Display**
   - [ ] Headlines are visible (text appears)
   - [ ] **Images are showing** (not broken image icons)
   - [ ] At least 3-5 headlines visible
   - [ ] Headlines are readable

3. **Check Image Quality**
   - [ ] Images are thumbnails (small, ~100x60px)
   - [ ] Images are properly cropped
   - [ ] No error messages in image area
   - [ ] Images load progressively

4. **Expected Results**
   - ✓ News headlines visible
   - ✓ Images load and display
   - ✓ From Times of India feed (Kolkata data)
   - ✓ No broken image placeholders

### Verification Logs
Look for:
- `"Loading news for location: Kolkata"`
- `"News API URL:...times of india..."`
- `"Headlines loaded: X items"`
- `"Image URL: https://..."`

### Test Result: ☐ PASS / ☐ FAIL / ☐ PARTIAL

**Evidence**:
- [ ] Headlines visible: YES / NO
- [ ] Images loading: YES / NO / PARTIAL
- [ ] Count: _____ headlines
- [ ] Issues: ________________

---

## ✅ TEST 4: Landscape Mode - Headline Position

**Feature**: In landscape, news headline should be positioned 56dp from bottom (above adjaba logo)

### What Changed
- File: `fragment_advert_watching.xml`
- Changed: `android:layout_marginBottom="16dp"` → `56dp`
- Result: Headline moved 40dp higher, clearing logo

### Test Steps

1. **Prepare Screen**
   - [ ] Keep Kolkata location selected
   - [ ] Navigate to news display with headlines
   - [ ] Make sure headline text is visible

2. **Rotate to Landscape**
   - [ ] Press **Ctrl+F11** or **F1** on keyboard
   - [ ] OR press **Ctrl+Left Arrow** (some versions)
   - [ ] Emulator should rotate 90° (landscape)
   - [ ] Wait 2-3 seconds for layout adjustment

3. **Verify Headline Position**
   - [ ] Headline text is **ABOVE** adjaba logo
   - [ ] No overlapping text and logo
   - [ ] Headline is fully readable
   - [ ] Bottom margin is visible (white space)
   - [ ] Logo is at bottom of screen

4. **Expected Results**
   - ✓ Headline positioned higher than before
   - ✓ Clear separation from logo (56dp / ~18mm)
   - ✓ Text fully visible and readable
   - ✓ Professional appearance

### Layout Verification
Expected margin: 56dp (approximately 18mm or ~56 pixels at 160dpi)

### Test Result: ☐ PASS / ☐ FAIL / ☐ PARTIAL

**Evidence**:
- [ ] Headline visible in landscape: YES / NO
- [ ] Above logo: YES / NO
- [ ] No overlap: YES / NO
- [ ] Fully readable: YES / NO
- [ ] Visual notes: ________________

---

## ✅ TEST 5: QR Code Visibility

**Feature**: QR codes should ONLY appear on ads, never on news or weather

### What Changed
- Code already implements this correctly (verified)
- QR visibility set to `View.GONE` for non-ad media types
- Pattern: Ads = VISIBLE, News = HIDDEN, Weather = HIDDEN

### Test Steps

1. **Test on Advertisement Slide**
   - [ ] Navigate to or wait for ad display
   - [ ] Look for QR code (usually bottom-right area)
   - [ ] QR code should be **VISIBLE** and clickable
   - [ ] QR should point to ad link/website
   - [ ] Note QR presence

2. **Test on News Slide**
   - [ ] Navigate to or wait for news display
   - [ ] Look for QR code area
   - [ ] QR code should be **HIDDEN** (not visible)
   - [ ] No QR-shaped object visible
   - [ ] Only headline and images showing

3. **Test on Weather Slide**
   - [ ] Navigate to or wait for weather display
   - [ ] Look for QR code area
   - [ ] QR code should be **HIDDEN** (not visible)
   - [ ] Only weather info showing
   - [ ] No QR-shaped object visible

4. **Expected Results**
   - ✓ Ads: QR code VISIBLE
   - ✓ News: QR code HIDDEN
   - ✓ Weather: QR code HIDDEN
   - ✓ Consistent across all slides

### Verification Logs
Look for:
- On ads: `"QR code visibility: VISIBLE"`
- On news: `"QR code visibility: GONE"` or `"QR hidden for news"`
- On weather: `"QR code visibility: GONE"` or `"QR hidden for weather"`

### Test Result: ☐ PASS / ☐ FAIL / ☐ PARTIAL

**Evidence**:
- [ ] Ad QR visible: YES / NO
- [ ] News QR hidden: YES / NO
- [ ] Weather QR hidden: YES / NO

---

## 📊 Summary Test Matrix

| Feature | Status | Pass | Fail | Notes |
|---------|--------|------|------|-------|
| 1. Kolkata IST Time | ☐ | ☐ | ☐ | |
| 2. Weather Location | ☐ | ☐ | ☐ | |
| 3. News Images | ☐ | ☐ | ☐ | |
| 4. Landscape Headline | ☐ | ☐ | ☐ | |
| 5. QR Code Visibility | ☐ | ☐ | ☐ | |
| **OVERALL** | | | | |

---

## 🔴 If Issues Occur

### Issue: Time not showing IST
```
Troubleshooting:
1. Check location is actually "Kolkata, India"
2. View logs: adb logcat | findstr "Timezone"
3. Verify TimeZone class imported in code
4. Check if timezone string matches map key
```

### Issue: Weather not showing Kolkata data
```
Troubleshooting:
1. Check API connectivity: adb shell ping 8.8.8.8
2. View network logs: adb logcat | findstr "Weather"
3. Verify DataHolder.location is set
4. Check API key is valid (if applicable)
```

### Issue: News images not loading
```
Troubleshooting:
1. Check image URLs in logs
2. Verify network connectivity
3. Check RSS feed is accessible
4. View image load errors: adb logcat | findstr "Image\|News"
```

### Issue: Headline overlapping in landscape
```
Troubleshooting:
1. Verify margin is 56dp in XML: grep -r "main_header" app/src/main/res/
2. Rotate back to portrait, then landscape again
3. Check if custom view is overriding margin
4. View layout params in logs
```

### Issue: QR codes not hiding
```
Troubleshooting:
1. Check media type detection
2. View QR visibility in logs: adb logcat | findstr "QR\|Visibility"
3. Verify View.GONE is being called
4. Check if custom view is showing QR anyway
```

---

## 📝 Testing Notes Template

### Test Session Info
- **Date**: _______________
- **Time**: _______________
- **Tester**: _______________
- **Device/Emulator**: _______________
- **App Version**: _______________
- **Location Used**: _______________

### Overall Status
- [ ] All tests passed ✓
- [ ] Some tests failed
- [ ] Critical issues found

### Issues Found
```
1. ____________________________________________________
2. ____________________________________________________
3. ____________________________________________________
```

### Screenshots
(Attach screenshots showing):
- [ ] IST time display
- [ ] Weather for Kolkata
- [ ] News with images
- [ ] Landscape headline position
- [ ] QR visibility on different slides

### Recomm actions
```
1. ____________________________________________________
2. ____________________________________________________
3. ____________________________________________________
```

---

## 📞 Debugging Commands

```bash
# View all logs
adb logcat

# View only Adjaba logs
adb logcat | findstr "Adjaba"

# View specific feature logs
adb logcat | findstr "Timezone"     # Time feature
adb logcat | findstr "Weather"      # Weather feature
adb logcat | findstr "News"         # News feature
adb logcat | findstr "QR"           # QR code feature

# View app crashes
adb logcat | findstr "Exception\|Error\|Crash"

# Clear logs
adb logcat -c

# Save logs to file
adb logcat > app_logs.txt

# View device system info
adb shell getprop

# Get device timezone
adb shell getprop persist.sys.timezone

# Monitor app performance
adb shell dumpsys meminfo com.adjaba.adplayer

# Force stop and restart app
adb shell am force-stop com.adjaba.adplayer
adb shell am start -n com.adjaba.adplayer/.activities.SelectScreens
```

---

## ✨ Next Steps After Testing

### If All Tests Pass ✓
1. Document results in this file
2. Take screenshots for verification
3. Commit code changes with message:
   ```
   "Feat: Add timezone support, fix landscape headline,
   verify location-based features
   
   - Kolkata now displays IST (UTC+5:30)
   - Location-based weather and news fetching
   - Headline positioned 56dp from bottom in landscape
   - QR codes hidden on news/weather (ads only)
   - 19 major cities with timezone support"
   ```
4. Deploy to production

### If Issues Found ✗
1. Document exact error in this file
2. Capture logs: `adb logcat > error_logs.txt`
3. Take screenshots showing issue
4. Identify which feature has issue
5. Review code changes for that feature
6. Make targeted fix and rebuild
7. Re-test just that feature

---

**Guide Version**: 1.0  
**Last Updated**: May 16, 2026  
**Status**: ✅ Ready for testing

