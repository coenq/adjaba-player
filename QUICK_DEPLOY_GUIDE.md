# ⚡ QUICK DEPLOYMENT GUIDE

## Current Status
✅ **Build**: SUCCESSFUL (3m 45s)  
⚠️ **Device**: OFFLINE (R52MB18CEGR)  
📦 **APK Ready**: `app/build/outputs/apk/debug/app-debug.apk`

---

## 🔌 STEP 1: Reconnect Device

### Physical Connection
```
1. Plug USB cable to SM-T510 tablet
2. On tablet: Tap "Allow" on USB debugging prompt
3. Set USB mode to "File Transfer" or "MTP"
4. Wait 5 seconds
```

### Verify Connection
```powershell
& "$env:LocalAppData\Android\Sdk\platform-tools\adb.exe" devices
```

**Expected Output**:
```
List of devices attached
R52MB18CEGR     device
```

If still offline:
```powershell
# Restart ADB
& "$env:LocalAppData\Android\Sdk\platform-tools\adb.exe" kill-server
Start-Sleep 2
& "$env:LocalAppData\Android\Sdk\platform-tools\adb.exe" start-server

# Wait 3 seconds, then check again
Start-Sleep 3
& "$env:LocalAppData\Android\Sdk\platform-tools\adb.exe" devices
```

---

## 📲 STEP 2: Deploy App

```powershell
cd C:\project\adjaba-player
./gradlew installDebug
```

**Expected Output**:
```
> Task :app:installDebug
Installing APK 'app-debug.apk' on 'SM-T510'...
Installed on 1 device.

BUILD SUCCESSFUL in ~30s
```

---

## 🎮 STEP 3: Launch App

### Option A: Manual (Easiest)
- Tap app icon on home screen or app drawer
- App should open to SelectScreens

### Option B: Command Line
```powershell
& "$env:LocalAppData\Android\Sdk\platform-tools\adb.exe" shell am start -n com.adjaba/.activities.SelectScreens
```

---

## ✅ VERIFICATION CHECKLIST

### SelectScreens Screen
- [ ] Light background in "Orientation" dropdown
- [ ] Light background in "Screen ID" dropdown
- [ ] Light background in "Data Refresh Interval" dropdown
- [ ] Options include: "Landscape", "Portrait", **"TV Portrait"**

### Choose Settings & Play
```
Orientation: Portrait
Screen ID: [any valid ID]
Data Refresh Interval: [any interval]
Press: Play/Select Button
```

### Portrait Weather Slide
- [ ] Location at TOP with red bar
- [ ] Time below location
- [ ] Date below time
- [ ] Temperature below date
- [ ] Condition text below temperature
- [ ] Thin divider line
- [ ] Metrics in 2x2 grid:
  - [ ] Upper Left: Wind icon + "32" + "km/h"
  - [ ] Upper Right: Humidity icon + "60%" + "humidity"
  - [ ] Lower Left: Thermostat icon + "22°" + "feels like"
  - [ ] Lower Right: Pressure icon + "1013" + "hPa"
- [ ] No overlapping text
- [ ] All text readable

### Portrait News Slide
- [ ] Hero image at top
- [ ] Headline centered below image
- [ ] Description text below headline
- [ ] Red accent line present

### Navigation
- [ ] D-pad UP works (scroll/focus)
- [ ] D-pad DOWN works (scroll/focus)
- [ ] D-pad LEFT works (navigate)
- [ ] D-pad RIGHT works (navigate)
- [ ] Select/OK button works
- [ ] No app crashes

---

## 📸 SCREENSHOTS TO CAPTURE

Take these for documentation:
1. SelectScreens with light dropdowns
2. Portrait weather slide (full view)
3. Portrait news slide (full view)
4. TV Portrait mode (for comparison)

---

## 🔧 TROUBLESHOOTING

### Issue: Device Still Offline
```powershell
# Check USB device is recognized
& "$env:LocalAppData\Android\Sdk\platform-tools\adb.exe" devices -l

# If showing "offline" repeatedly:
# - Restart tablet (long-press power button)
# - Try different USB cable
# - Try different USB port
# - Uninstall and reinstall USB drivers
# - Restart Windows
```

### Issue: Installation Fails
```powershell
# Clear old installation
& "$env:LocalAppData\Android\Sdk\platform-tools\adb.exe" uninstall com.adjaba

# Retry install
./gradlew installDebug
```

### Issue: App Crashes on Launch
```powershell
# Check logcat for errors
& "$env:LocalAppData\Android\Sdk\platform-tools\adb.exe" logcat -c
& "$env:LocalAppData\Android\Sdk\platform-tools\adb.exe" logcat *:V

# Look for lines starting with "E/" (errors)
```

---

## 📝 BUILD DETAILS

- **Build Time**: 3m 45s ✅
- **Errors**: 0 ✅
- **Warnings**: 2 (deprecated API - not critical)
- **Package**: com.adjaba.app ✅
- **API Level**: 11+ ✅

---

## 📞 QUICK REFERENCE

| Asset | Location |
|-------|----------|
| **APK** | `app/build/outputs/apk/debug/app-debug.apk` |
| **Logs** | `./gradlew installDebug 2>&1 \| tee install.log` |
| **Code** | `app/src/main/` |
| **Layout** | `app/src/main/res/layout/` |
| **Gradle** | `./gradlew` |
| **ADB** | `$env:LocalAppData\Android\Sdk\platform-tools\adb.exe` |

---

## 🎯 WHAT'S NEW

From Session 2:
- ✅ Portrait weather layout restructured to true vertical stack
- ✅ Build errors fixed (orphaned constraint IDs resolved)
- ✅ APK ready for deployment
- ✅ All previous changes verified (naming, spinners)

From Session 1:
- ✅ SelectScreens spinners: Light theme backgrounds
- ✅ "Forced Portrait" → "TV Portrait" renaming
- ✅ Orientation routing verified

---

**Status**: READY TO DEPLOY  
**Next**: Reconnect device and run `./gradlew installDebug`

---

**Build Date**: May 15, 2026  
**Session**: 2  
**Estimated Deploy Time**: 2-3 minutes

