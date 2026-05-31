# Android Emulator Setup for Adjaba Player

## Quick Start (Recommended)

### Option 1: Automated One-Click Deployment ⭐

Run this single command to automatically set up emulator and deploy the app:

```batch
C:\project\adjaba-player\deploy_to_emulator.bat
```

This will:
1. ✓ Set up lightweight Android Virtual Device
2. ✓ Clean build the app
3. ✓ Start the emulator
4. ✓ Wait for emulator to boot
5. ✓ Install app on emulator
6. ✓ Launch the app

**Expected time**: 5-10 minutes on first run

---

### Option 2: Manual Step-by-Step

#### Step 1: Create Lightweight AVD

```batch
setup_emulator.bat
```

This creates an AVD named `Adjaba_TV_Test` with:
- Android 13 or 32
- TV 1080p profile
- x86_64 architecture (fastest)
- 2GB RAM
- Minimal disk footprint

#### Step 2: Start the Emulator

```batch
run_emulator.bat
```

Or from Android Studio:
- Tools → Device Manager → Launch `Adjaba_TV_Test`

#### Step 3: Build the App

```batch
CD C:\project\adjaba-player
gradlew.bat clean build
```

#### Step 4: Install on Emulator

```batch
gradlew.bat installDebug
```

#### Step 5: Launch the App

```batch
adb shell am start -n com.adjaba.adplayer/.activities.SelectScreens
```

---

## Detailed Instructions

### Prerequisites

1. **Android Studio installed** (with SDK tools)
2. **Java JDK 11+** installed
3. **CPU VMX/SVM support** enabled in BIOS (for -qemu -enable-kvm)
4. **5GB free disk space** for emulator image

### If Emulator System Images Not Installed

In Android Studio:
1. Go to **Tools → SDK Manager**
2. Click **SDK Tools** tab
3. Check **Android Emulator**
4. Check **System Images** (Android 13 or higher)
5. Click **OK** and let it install

### System Image Not Found Error?

If you get "System image package ... could not be found", manually create AVD:

```batch
C:\Users\User\AppData\Local\Android\Sdk\tools\bin\avdmanager.bat create avd ^
    -n Adjaba_TV_Test ^
    -k "system-images;android-32;default;x86_64" ^
    -d "tv_1080p" ^
    --force
```

---

## Testing Checklist

Once app runs on emulator:

### 1. Location-Based Time ✓
- [ ] Open app → Select Screens
- [ ] Choose or input "Kolkata, India"
- [ ] Verify clock shows IST (UTC+5:30)
- [ ] Compare with device time (should differ if device not in IST)

### 2. Weather Display ✓
- [ ] Select Kolkata or other supported location
- [ ] Verify weather shows location name
- [ ] Verify weather data displays (temperature, conditions)
- [ ] Verify weather uses correct location API

### 3. News Images ✓
- [ ] Select Kolkata location
- [ ] Wait for news headlines to load
- [ ] Verify images display (should show Times of India thumbnails)
- [ ] Check no broken image icons

### 4. Landscape Mode - Headline Position ✓
- [ ] Select any location with news
- [ ] Rotate emulator to landscape (Ctrl+F11 or F1)
- [ ] Verify headline text positioned ABOVE adjaba logo
- [ ] Verify no overlap with logo

### 5. QR Code Visibility ✓
- [ ] On Ad slides: QR code visible ✓
- [ ] On News slides: QR code HIDDEN
- [ ] On Weather slides: QR code HIDDEN

---

## Troubleshooting

### Emulator Won't Start

**Problem**: "KVM not available" or "Cannot boot"

**Solution**:
1. Check virtualization enabled in BIOS
2. Run as Administrator if on Windows
3. Disable Hyper-V if using WSL:
   ```batch
   dism.exe /Online /Disable-Feature:Microsoft-Hyper-V
   ```
4. Or use `-qemu -no-kvm` flag for slower but compatible boot

### App Won't Install

**Problem**: "Package conflicts" or "Incompatible"

**Solution**:
```batch
adb shell pm uninstall com.adjaba.adplayer
gradlew.bat installDebug
```

### App Crashes on Launch

**Solution**: Check logcat
```batch
adb shell logcat | findstr "Adjaba\|Error\|Exception"
```

### Emulator Too Slow

**Reduce emulator resource usage**:
- Disable GPU: Remove `-gpu on -gpu mode=host` from run_emulator.bat
- Reduce RAM: Change `hw.ramSize=1024` in AVD config
- Use snapshot: Change `-no-snapshot-load` to `-snapshot default`

### Device Not Found in ADB

**Check connection**:
```batch
adb devices
```

Should show emulator like: `emulator-5554    device`

If not listed:
```batch
adb kill-server
adb start-server
adb devices
```

---

## File Descriptions

| File | Purpose |
|------|---------|
| `setup_emulator.bat` | Creates lightweight Android Virtual Device |
| `run_emulator.bat` | Starts the emulator with optimization flags |
| `deploy_to_emulator.bat` | **Automated full deployment in one command** |
| `EMULATOR_SETUP_GUIDE.md` | This file |

---

## Advanced Configuration

### Custom AVD Configuration

Edit: `C:\Users\User\.android\avd\Adjaba_TV_Test.avd\config.ini`

```ini
# Performance tuning
hw.gpu.enabled=yes
hw.gpu.mode=host
hw.ramSize=2048
hw.videoCache=64
hw.initialOrientation=landscape
hw.keyboard=yes
hw.dPad=yes

# Disable hardware that app doesn't need
hw.camera.back=none
hw.camera.front=none
hw.audioInput=no
hw.audioOutput=no
```

### Rotate Emulator to Landscape

**Method 1**: Keyboard shortcut
- **Mac**: Cmd + Left/Right Arrow
- **Windows**: Ctrl + F11 or F1
- **Linux**: Ctrl + F11

**Method 2**: Extended controls
- Right-click emulator → Rotate

---

## Performance Notes

**First Boot**: 3-5 minutes (initializes filesystem)  
**Subsequent Boots**: 1-2 minutes (cached)  
**App Installation**: 30-60 seconds  
**App Launch**: 5-10 seconds

**System Requirements**:
- CPU: Modern processor (Intel/AMD) with virtualization
- RAM: 8GB minimum, 16GB recommended
- Disk: 5GB free for emulator image + 2GB for build artifacts
- Network: Required for downloading system images

---

## When to Use Physical Device Instead

Use emulator for:
- ✓ Quick feature testing
- ✓ UI/layout verification
- ✓ Regression testing
- ✓ Logcat debugging

Use physical device for:
- ✓ Performance profiling
- ✓ Hardware-specific features
- ✓ Final acceptance testing
- ✓ Production validation

---

## Quick Commands Reference

```batch
REM List all devices
adb devices

REM Install app
gradlew installDebug

REM Uninstall app
adb uninstall com.adjaba.adplayer

REM Watch logs
adb logcat | findstr "Adjaba"

REM Take screenshot
adb shell screencap /sdcard/screenshot.png
adb pull /sdcard/screenshot.png

REM Open app directly
adb shell am start -n com.adjaba.adplayer/.activities.SelectScreens

REM Kill app
adb shell am force-stop com.adjaba.adplayer

REM Check app version
adb shell dumpsys package com.adjaba.adplayer | findstr version
```

---

## Support

If you encounter issues:

1. Check **Troubleshooting** section above
2. Run with error redirection:
   ```batch
   setup_emulator.bat > setup_log.txt 2>&1
   ```
3. Share the setup_log.txt file with logs

---

**Last Updated**: May 16, 2026  
**Compatible With**: Adjaba Player 1.0+  
**Tested On**: Windows 10/11, Android Studio 2021.3+

