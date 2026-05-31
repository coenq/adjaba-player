# ✅ ADJABA PLAYER EMULATOR TESTING CHECKLIST

**Version**: 1.0  
**Date**: May 16, 2026  
**Status**: Ready to Use  
**Print & Use**: Yes - This is a checklist!

---

## 📋 PRE-DEPLOYMENT CHECKLIST

### System Requirements
- [ ] Windows 10 or Windows 11
- [ ] Android Studio installed
- [ ] Java JDK 11+ installed
- [ ] 5GB free disk space
- [ ] Network connection (for SDK downloads)
- [ ] No Hyper-V conflicts (if on Windows)

### Setup Verification
- [ ] Found file: `simple_deploy.bat` in C:\project\adjaba-player\
- [ ] Found file: `MANUAL_TESTING_GUIDE.md`
- [ ] Found file: `QUICK_REFERENCE.txt`
- [ ] Android SDK available at: C:\Users\User\AppData\Local\Android\Sdk\

### Understanding
- [ ] Understand what emulator is (virtual Android device)
- [ ] Understand what will be tested (5 features)
- [ ] Know how to navigate in emulator (arrow keys)
- [ ] Know emulator rotation command (Ctrl+F11)

---

## 🚀 DEPLOYMENT CHECKLIST

### Start Deployment
- [ ] Open Command Prompt or PowerShell
- [ ] Navigate: `cd C:\project\adjaba-player`
- [ ] Verify location shows project directory path
- [ ] Run: `simple_deploy.bat`
- [ ] Watch for script to start

### Build Phase (2-3 minutes)
- [ ] Gradle starts downloading dependencies (normal, first time 1-2 min)
- [ ] Java compilation begins
- [ ] Resources are processed
- [ ] APK is assembled
- [ ] Look for: NO ERRORS in output

### Emulator Startup (1-5 minutes)
- [ ] Emulator window opens
- [ ] Android splash screen appears
- [ ] Android boot animation plays (bird-like animation)
- [ ] Wait for full boot (may take 3-5 min first time)

### Installation (1-2 minutes)
- [ ] APK transfers to device
- [ ] Package installation message appears
- [ ] Success confirmed in output

### Launch (1 minute)
- [ ] Adjaba Player UI appears
- [ ] Main interface is visible
- [ ] App is responsive to input

### Final Status
- [ ] See completion message
- [ ] No ERROR or FAILED messages
- [ ] Ready to proceed to testing

---

## 🎮 FEATURE TESTING CHECKLIST

### Setup for Testing
- [ ] Emulator is running and stable
- [ ] App is displayed and responsive
- [ ] Have keyboard ready for navigation
- [ ] Have `MANUAL_TESTING_GUIDE.md` open or printed
- [ ] Prepare to take notes

---

### TEST 1: Kolkata Local Time (IST)

#### Setup
- [ ] Navigate to location selection
- [ ] Enter location: "Kolkata, India" (or select if available)
- [ ] Navigate to clock display area
- [ ] Note current device time for comparison

#### Verification
- [ ] Clock is visible on screen
- [ ] Format is HH:MM AM/PM (readable time format)
- [ ] Time shows IST offset (~UTC+5:30)
- [ ] Check: Is time approximately 5.5 hours ahead of UTC?

#### Expected Result
- [ ] ✅ PASS: Shows local IST time, not device UTC
- [ ] ❌ FAIL: Still showing device time
- [ ] ⚠️ PARTIAL: Shows some time offset but not exact

#### Result: ☐ PASS ☐ FAIL ☐ PARTIAL

**Notes**: ___________________________________________________________

---

### TEST 2: Weather by Location

#### Setup
- [ ] Keep location as "Kolkata, India"
- [ ] Navigate to weather display
- [ ] Wait 3-5 seconds for data load

#### Verification
- [ ] Weather widget is visible
- [ ] Location label shows "Kolkata" (not device location)
- [ ] Temperature is displayed (numeric value)
- [ ] Weather condition shown (sunny, cloudy, rain, etc.)
- [ ] Check: Is temperature reasonable for Kolkata (25-32°C)?

#### Expected Result
- [ ] ✅ PASS: Kolkata weather displayed with correct data
- [ ] ❌ FAIL: Shows device location or no data
- [ ] ⚠️ PARTIAL: Shows Kolkata but with incomplete data

#### Result: ☐ PASS ☐ FAIL ☐ PARTIAL

**Notes**: ___________________________________________________________

---

### TEST 3: News Images Loading

#### Setup
- [ ] Keep location as "Kolkata, India"
-[ ] Navigate to news headlines section
- [ ] Wait 10 seconds for headlines to load

#### Verification
- [ ] News headlines are visible (at least 3-5)
- [ ] **Each headline has an image thumbnail next to it**
- [ ] No broken image placeholders (no ! or X icon)
- [ ] Images are properly sized and aligned
- [ ] Headline text is readable below/next to images

#### Expected Result
- [ ] ✅ PASS: 5+ headlines with images loaded
- [ ] ❌ FAIL: No images or broken placeholders
- [ ] ⚠️ PARTIAL: Some images loaded, some broken

#### Result: ☐ PASS ☐ FAIL ☐ PARTIAL

**Notes**: ___________________________________________________________

---

### TEST 4: Landscape Headline Position

#### Setup
- [ ] Keep Kolkata location and news visible
- [ ] Headline text should be visible on screen
- [ ] Press: **Ctrl+F11** to rotate to landscape
- [ ] Wait 2-3 seconds for layout adjustment

#### Verification
- [ ] Emulator rotates 90 degrees (landscape)
- [ ] Headline text remains visible
- [ ] **Headline is positioned ABOVE adjaba logo**
- [ ] No overlapping of headline text with logo
- [ ] Full headline text is readable
- [ ] Professional spacing appearance

#### Expected Result
- [ ] ✅ PASS: Headline clear of logo, 56dp spacing
- [ ] ❌ FAIL: Headline overlaps logo area
- [ ] ⚠️ PARTIAL: Headline moved but still slightly overlaps

#### Result: ☐ PASS ☐ FAIL ☐ PARTIAL

**Notes**: ___________________________________________________________

---

### TEST 5: QR Code Visibility

#### Setup - Ad Slide
- [ ] Navigate to or wait for advertisement slide
- [ ] Look at bottom-right area of screen

#### Ad Verification
- [ ] QR code is **VISIBLE** (you can see it)
- [ ] QR code is properly formatted (square pattern)
- [ ] Can be interacted with (looks clickable)

#### Result: ☐ QR VISIBLE ☐ QR HIDDEN ☐😕 UNCLEAR

#### Setup - News Slide
- [ ] Navigate to or wait for news headline slide
- [ ] Look at bottom-right area (where QR would be)

#### News Verification
- [ ] QR code is **HIDDEN** (not visible)
- [ ] No QR-shaped object in that area
- [ ] Only news content showing

#### Result: ☐ QR HIDDEN ☐ QR VISIBLE ☐😕 UNCLEAR

#### Setup - Weather Slide
- [ ] Navigate to or wait for weather display
- [ ] Look at bottom-right area

#### Weather Verification
- [ ] QR code is **HIDDEN** (not visible)
- [ ] No QR-shaped object in that area
- [ ] Only weather content showing

#### Result: ☐ QR HIDDEN ☐ QR VISIBLE ☐😕 UNCLEAR

#### Expected Result Overall
- [ ] ✅ PASS: Ads=visible, News=hidden, Weather=hidden
- [ ] ❌ FAIL: QR shows on news or weather
- [ ] ⚠️ PARTIAL: Some incorrect, not all follow rule

#### Result: ☐ PASS ☐ FAIL ☐ PARTIAL

**Notes**: ___________________________________________________________

---

## 📊 TEST SUMMARY

### Score Card

| Feature | Result | Notes |
|---------|--------|-------|
| 1. Kolkata IST Time | ☐ PASS ☐ FAIL ☐ PARTIAL | |
| 2. Weather Location | ☐ PASS ☐ FAIL ☐ PARTIAL | |
| 3. News Images | ☐ PASS ☐ FAIL ☐ PARTIAL | |
| 4. Landscape Layout | ☐ PASS ☐ FAIL ☐ PARTIAL | |
| 5. QR Visibility | ☐ PASS ☐ FAIL ☐ PARTIAL | |

### Overall Result
```
☐ ALL PASS - Ready for Production!
☐ MOSTLY PASS - Minor issues only
☐ MIXED - Some issues need fixing
☐ MOSTLY FAIL - Review code changes
☐ ALL FAIL - Critical issues
```

---

## 📝 DETAILED NOTES

### Feature 1 - Time Issues
```
Symptom: Time not showing IST
Check:   1. Is location definitely "Kolkata"?
         2. Check log: adb logcat | findstr "Timezone"
         3. Verify Java TimeZone class available
Solution: Check AdvertWatching.java line 703
```

### Feature 2 - Weather Issues
```
Symptom: Weather not showing Kolkata data
Check:   1. Is location set correctly?
         2. Is network working? (ping 8.8.8.8)
         3. Check API connectivity logs
Solution: Verify DataHolder.location is set
```

### Feature 3 - Image Issues
```
Symptom: No images or broken placeholders
Check:   1. Wait longer (up to 15 seconds)
         2. Check network connectivity
         3. Verify RSS feed is accessible
Solution: Check MANUAL_TESTING_GUIDE.md § News Images
```

### Feature 4 - Layout Issues
```
Symptom: Headline overlaps logo in landscape
Check:   1. Verify margin is 56dp in XML
         2. Try rotating back and forth
         3. Check if custom view overrides margin
Solution: See MANUAL_TESTING_GUIDE.md § Landscape
```

### Feature 5 - QR Issues
```
Symptom: QR showing on news/weather
Check:   1. Verify media type detection
         2. Check setVisibility calls
         3. Review QR visibility logic
Solution: See MANUAL_TESTING_GUIDE.md § QR Codes
```

---

## 🔴 FAILURE RESPONSE GUIDE

### If Test 1 Fails (IST Time)
```
□ Clear app cache:         adb shell pm clear com.adjaba.adplayer
□ Uninstall and reinstall: adb uninstall com.adjaba.adplayer
                           gradlew installDebug
□ Verify code:            Check AdvertWatching.java (line 703-741)
□ Check logs:             adb logcat | findstr "Timezone"
```

### If Test 2 Fails (Weather)
```
□ Verify network:     adb shell ping 8.8.8.8
□ Check location:     adb shell getprop ro.build.display.id
□ Review API:         Check weather API call in logcat
□ Restart app:        adb shell am force-stop com.adjaba.adplayer
```

### If Test 3 Fails (News Images)
```
□ Wait longer:     Give it 15 seconds to load
□ Check network:   adb shell ping 8.8.8.8
□ View logs:       adb logcat | findstr "Image\|News"
□ Clear cache:     adb shell pm clear com.adjaba.adplayer
```

### If Test 4 Fails (Layout)
```
□ Verify XML:      grep "main_header" app/src/main/res/layout-land/*.xml
□ Check margin:    Should be android:layout_marginBottom="56dp"
□ Rebuild app:     gradlew clean build
□ Reinstall:       gradlew installDebug
```

### If Test 5 Fails (QR)
```
□ Check visibility:   adb logcat | findstr "QR\|Visibility"
□ Verify media type:  adb logcat | findstr "Media type"
□ Review code:        Check AdvertWatching.java (QR setVisibility)
□ Test all slides:    Verify ads, news, and weather separately
```

---

## ✅ COMPLETION CHECKLIST

### When All Tests Pass ✅
- [ ] All 5 tests show PASS
- [ ] No ERROR messages in logs
- [ ] App is stable and responsive
- [ ] Features working as documented
- [ ] Ready to commit code

### Before Committing
- [ ] Take screenshots of each feature working
- [ ] Save detailed notes above
- [ ] Verify build is clean (zero errors)
- [ ] Check git status for changes
- [ ] Review code changes once more

### Commit Message Template
```
Feat: Add timezone support and verify location features

- Kolkata displays local IST time (UTC+5:30)
- Weather API uses playlist location (not device)
- News fetches from location-specific RSS feed
- Headline positioned 56dp from bottom in landscape
- QR codes hidden on news/weather (ads only)
- 19 major cities with timezone support
- All tests pass on emulator
```

### After Commit
- [ ] Push code to repository
- [ ] Mark version/release as tested
- [ ] Update release notes
- [ ] Notify team of completion
- [ ] Archive test results

---

## 📊 SESSION SUMMARY

**Session Date**: ___________________  
**Tester Name**: ___________________  
**Device/Emulator**: Android 13, Emulator  
**Build Version**: ____________________  
**Total Test Time**: ____________________  

**Overall Status**: ☐ PASS ☐ FAIL

**Issues Found**: _________________________________________________________

__________________________________________________________________

__________________________________________________________________

**Actions Needed**: __________________________________________________________

__________________________________________________________________

**Sign Off**: _____________________ Date: _____________

---

## 🚀 POST-TESTING NEXT STEPS

### If All Tests Pass ✅
- [ ] 1. Commit code changes to repository
- [ ] 2. Create release notes
- [ ] 3. Plan production deployment
- [ ] 4. Notify stakeholders
- [ ] 5. Archive test results

### If Some Tests Fail ⚠️
- [ ] 1. Identify root cause from logs
- [ ] 2. Review code changes for that feature
- [ ] 3. Apply fix to source code
- [ ] 4. Rebuild: `gradlew clean build`
- [ ] 5. Reinstall: `gradlew installDebug`
- [ ] 6. Re-test the problematic feature
- [ ] 7. Verify fix resolves issue
- [ ] 8. If pass: proceed to commit

### If Critical Failure ❌
- [ ] 1. Capture all logs to file
- [ ] 2. Take screenshots of errors
- [ ] 3. Document exact failure mode
- [ ] 4. Escalate to development team
- [ ] 5. Do not proceed to production
- [ ] 6. Wait for code review/fix

---

## 📞 QUICK REFERENCE

### Key Files Location
```
Deployment: C:\project\adjaba-player\simple_deploy.bat
Guide:      C:\project\adjaba-player\MANUAL_TESTING_GUIDE.md
Reference:  C:\project\adjaba-player\QUICK_REFERENCE.txt
```

### Android SDK
```
SDK Path:   C:\Users\User\AppData\Local\Android\Sdk
Emulator:   ....\emulator.exe
ADB:        ....\platform-tools\adb.exe
```

### Useful Commands
```
adb devices                    → List connected devices
adb logcat | findstr "Adjaba"  → View app logs
adb shell am force-stop ...    → Force stop app
Ctrl+F11                       → Rotate landscape
```

---

## 🎯 THIS CHECKLIST

- [ ] Printed this checklist
- [ ] Have pen/pencil ready
- [ ] Have this checklist next to testing device
- [ ] Mark boxes as you complete each test
- [ ] Save this completed checklist for records

---

**Checklist Version**: 1.0  
**Created**: May 16, 2026  
**Status**: Ready to Use  
**Format**: Print-Friendly ✅

**GOOD LUCK WITH YOUR TESTING!** 🎉


