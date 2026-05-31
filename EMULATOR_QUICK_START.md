# 🎮 Lightweight Android Emulator Setup for Adjaba Player

## Summary

I've created a complete **lightweight Android emulator setup** for testing your Adjaba Player app inside your IDE/development environment. This allows you to test all the recent fixes (timezone, location-based features, headline positioning, QR code visibility) without needing a physical device.

---

## 📦 What Was Created

### 1. **deploy_to_emulator.bat** ⭐ (Recommended - One-Click Deployment)
**File**: `C:\project\adjaba-player\deploy_to_emulator.bat`

Automated script that does everything in one go:
- ✓ Checks prerequisites
- ✓ Creates lightweight AVD  
- ✓ Cleans and builds app
- ✓ Starts emulator
- ✓ Waits for emulator boot
- ✓ Installs app
- ✓ Launches app

**Usage**:
```batch
cd C:\project\adjaba-player
.\deploy_to_emulator.bat
```

**Time**: ~7-10 minutes first run, ~3-5 minutes on subsequent runs

---

### 2. **setup_emulator.bat** (Manual Setup - Step 1)
**File**: `C:\project\adjaba-player\setup_emulator.bat`

Creates a lightweight Android Virtual Device with specs:
- **Name**: `Adjaba_TV_Test`
- **API Level**: Android 13 (or 32 fallback)
- **Architecture**: x86_64 (fastest for desktop)
- **Device Profile**: TV 1080p
- **RAM**: 2GB
- **GPU**: Enabled with host acceleration
- **Optimization**: Disabled audio, camera for minimal footprint

**Usage**:
```batch
.\setup_emulator.bat
```

---

### 3. **run_emulator.bat** (Manual Setup - Step 2)
**File**: `C:\project\adjaba-player\run_emulator.bat`

Starts the emulator with optimizations:
- Hardware acceleration enabled
- Network fast mode
- KVM enabled (if available)
- Proper memory allocation

**Usage**:
```batch
.\run_emulator.bat
```

---

### 4. **setup_emulator.ps1** (Alternative PowerShell Setup)
**File**: `C:\project\adjaba-player\setup_emulator.ps1`

PowerShell version of setup script (better output formatting on Windows):

**Usage**:
```powershell
powershell -ExecutionPolicy Bypass -File setup_emulator.ps1
```

---

### 5. **EMULATOR_SETUP_GUIDE.md** (Complete Documentation)
**File**: `C:\project\adjaba-player\EMULATOR_SETUP_GUIDE.md`

Comprehensive guide including:
- Quick start instructions
- Troubleshooting guide
- Testing checklist for all 5 features
- Advanced configuration options
- Performance tuning tips
- Command reference

---

## 🚀 Quick Start (Easiest)

### Option A: One-Click (Fully Automated)

```batch
cd C:\project\adjaba-player
.\deploy_to_emulator.bat
```

Sit back and wait ~10 minutes. The app will:
1. Set up emulator
2. Build the app
3. Start emulator
4. Install and launch app
5. Display success message

---

### Option B: Manual Steps (If you prefer control)

**Step 1**: Create AVD
```batch
.\setup_emulator.bat
```

**Step 2**: Start emulator (in separate terminal)
```batch
.\run_emulator.bat
```

**Step 3**: While emulator boots, build app (in another terminal)
```batch
gradlew clean build
```

**Step 4**: Install app
```batch
gradlew installDebug
```

**Step 5**: Launch app
```batch
adb shell am start -n com.adjaba.adplayer/.activities.SelectScreens
```

---

## ✅ Testing the 5 Fixes

Once the app is running on the emulator, test these scenarios:

### 1. **Kolkata Local Time (IST)** ✓
```
Expected: Should show IST (UTC+5:30) instead of device time
- Open app → Select "Kolkata, India"
- Check clock display (bottom of screen)
- Should show local time ~5.5 hours ahead of UTC
```

### 2. **Weather by Location** ✓
```
Expected: Weather displays for Kolkata location
- Select Kolkata in UI
- Verify weather widget shows Kolkata weather
- Check temperature and conditions display
```

### 3. **News Images Loading** ✓
```
Expected: News images fetch from Times of India RSS feed
- Select Kolkata location
- Wait for news headlines
- Verify images load (not broken image icon)
```

### 4. **Landscape Headline Position** ✓
```
Expected: Headline text positioned ABOVE adjaba logo (no overlap)
- Select any location with news
- Rotate to landscape (Ctrl+F11 on Windows)
- Verify headline is 56dp from bottom
- Verify no overlapping with logo
```

### 5. **QR Code Visibility** ✓
```
Expected: QR codes only on ads, hidden on news/weather
- Ads: QR code visible ✓
- News: QR code HIDDEN ✓
- Weather: QR code HIDDEN ✓
```

---

## 📋 Prerequisites

Before running the scripts, ensure you have:

### ✓ Required
- [ ] Android Studio installed
- [ ] Android SDK (API 32 or 33+)
- [ ] Java JDK 11+
- [ ] 5GB free disk space
- [ ] CPU with virtualization support

### ✓ Optional (for better performance)
- [ ] Virtualization enabled in BIOS
- [ ] 16GB RAM or higher
- [ ] SSD (not mechanical drive)

### ✓ To Install Missing Components

In **Android Studio**:
1. Tools → SDK Manager
2. Click "SDK Tools" tab
3. Check:
   - [ ] Android Emulator
   - [ ] Android SDK Platform (API 32 or 33)
   - [ ] System Images (x86_64 for fast emulation)
4. Click OK

---

## 🔧 Troubleshooting

### "System image not found"
```batch
REM Manually create with different API level
call "C:\Users\User\AppData\Local\Android\Sdk\tools\bin\avdmanager.bat" create avd ^
    -n Adjaba_TV_Test ^
    -k "system-images;android-32;default;x86_64" ^
    -d "tv_1080p" --force
```

### "Emulator won't start" or "KVM not available"
- Run as Administrator
- Disable Hyper-V if using WSL2
- Or use compatibility mode (slower but works)

### "App won't install"
```batch
REM Clear previous installation
adb uninstall com.adjaba.adplayer

REM Then reinstall
gradlew installDebug
```

### "App crashes on startup"
```batch
REM Check logs for errors
adb logcat | findstr "Adjaba\|Error\|Exception"
```

### "Emulator is slow"
- Reduce RAM in run_emulator.bat
- Disable GPU acceleration
- Use snapshot mode instead of cold start

See **EMULATOR_SETUP_GUIDE.md** for detailed troubleshooting with solutions.

---

## 📊 Performance Expectations

| Task | Time | Notes |
|------|------|-------|
| First boot | 3-5 min | Initializes filesystem |
| Subsequent boots | 1-2 min | Uses cache |
| App build | 2-3 min | Incremental faster |
| App install | 30-60s | Via ADB |
| App launch | 5-10s | On-device startup |
| **Total first run** | **~10 min** | Fully automated |

---

## 📁 File Summary

| File | Purpose | Run As |
|------|---------|--------|
| `deploy_to_emulator.bat` | Full automated deployment | `.\deploy_to_emulator.bat` |
| `setup_emulator.bat` | Manual AVD creation | `.\setup_emulator.bat` |
| `run_emulator.bat` | Start emulator | `.\run_emulator.bat` |
| `setup_emulator.ps1` | PowerShell setup (better UI) | `powershell -ExecutionPolicy Bypass -File setup_emulator.ps1` |
| `EMULATOR_SETUP_GUIDE.md` | Complete documentation | Read in IDE |

---

## 🎯 Next Steps

### Immediate (Right Now)
1. **Run the setup**:
   ```batch
   cd C:\project\adjaba-player
   deploy_to_emulator.bat
   ```

2. **Wait for completion** (~10 minutes)

3. **App will launch automatically** in emulator

### When App is Running
1. Navigate through screens using arrow keys/D-pad
2. Select different locations (use keyboard)
3. Test the 5 scenarios from **Testing Checklist** above
4. Check logs if anything looks wrong:
   ```batch
   adb logcat | findstr "Adjaba"
   ```

### Verification
- All 5 features work? → Ready for production ✓
- Any issues? → Check logcat output and troubleshooting guide

---

## 💡 Tips & Tricks

### Stop Emulator Cleanly
```
adb emu kill
REM Or just close the emulator window
```

### View App Logs in Real-Time
```batch
adb logcat -s "Adjaba" --follow
```

### Take Screenshot
```batch
adb shell screencap -p /sdcard/screenshot.png
adb pull /sdcard/screenshot.png
```

### Rotate Emulator
- Windows: `Ctrl+F11` or `F1`
- Mac: `Cmd+Left/Right Arrow`

### Fast Restart
```batch
adb shell am force-stop com.adjaba.adplayer
adb shell am start -n com.adjaba.adplayer/.activities.SelectScreens
```

---

## 🔐 Security Notes

The scripts store no sensitive data. The emulator:
- Is isolated from production
- Doesn't access real credentials
- Uses test API only
- Can be deleted anytime: `emulator -list-avds` then delete from Android Studio

---

## 🎓 Understanding What's Happening

### AVD (Android Virtual Device)
- Virtual Android phone/tablet/TV
- Runs Android OS in an emulator
- Can test your app without physical device

### x86_64 Architecture  
- Matches most desktop CPUs
- Much faster than ARM emulation
- Smaller download (~1.5GB system image)

### Hardware Acceleration
- GPU pass-through enabled
- Makes rendering faster
- Requires decent graphics capability

### TV 1080p Profile
- Emulates TV interface (not phone)
- 1920x1080 resolution
- D-pad navigation (keyboard arrows)

---

## 📞 Support

If you encounter issues:

1. Check **Troubleshooting** section above
2. Read **EMULATOR_SETUP_GUIDE.md** for detailed help
3. Look at logcat output:
   ```batch
   adb logcat | findstr "Error\|Exception"
   ```
4. Try manual steps instead of automated script

---

## ✨ Summary

You now have **3 ways to test the app**:

1. **Physical Device** (most realistic) ← Previous method
2. **Lightweight Emulator** (fast, local) ← NEW - Recommended for quick testing
3. **Android Studio Device Manager** (built-in, manual) ← Alternative

The emulator scripts are ready to use. Just run `deploy_to_emulator.bat` and let the automation do the work!

---

**Created**: May 16, 2026  
**Status**: ✅ Ready to use  
**Compatibility**: Windows 10/11, Android Studio 2021.3+  
**Default AVD**: Adjaba_TV_Test (Android 13, x86_64, TV 1080p)


