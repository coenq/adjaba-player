# 🚀 INSTALL APP ON TV EMULATOR - STEP BY STEP

## Status: Ready to Install!

Your Android TV emulator (TV_36) is booting up. Once it's ready, follow these steps to install and run the app.

---

## ⚡ FAST INSTALLATION (Copy & Paste)

### Step 1: Set Environment (Do Once)
```powershell
$env:JAVA_HOME="C:\Program Files\Java\jdk-26"
$env:ANDROID_HOME="C:\Users\somen\AppData\Local\Android\Sdk"
$env:PATH = "$env:JAVA_HOME\bin;" + "$env:ANDROID_HOME\platform-tools;" + $env:PATH
cd "C:\Users\somen\StudioProjects\adjaba-player"
```

### Step 2: Check Device Ready
```powershell
Write-Host "Checking device status..."
& adb devices
# Wait until you see: emulator-5554            device
```

### Step 3: Install APK
```powershell
Write-Host "Installing app (this takes ~30-45 seconds)..."
& adb install -r app/build/outputs/apk/debug/app-debug.apk
# Wait for: Success
```

### Step 4: Launch App
```powershell
Write-Host "Launching app on TV emulator..."
& adb shell am start -n com.rnd/com.rnd.activities.LoginActivity
# App should appear on TV screen within 3 seconds!
```

### Step 5: View App
```
📺 Look at the Android TV emulator window
✅ You should see the LoginActivity
✅ App is fullscreen in landscape mode
✅ Ready to test!
```

---

## 📋 Complete Script (Copy All & Run)

If you prefer one script:

```powershell
# ===== SETUP =====
$env:JAVA_HOME="C:\Program Files\Java\jdk-26"
$env:ANDROID_HOME="C:\Users\somen\AppData\Local\Android\Sdk"
$env:PATH = "$env:JAVA_HOME\bin;" + "$env:ANDROID_HOME\platform-tools;" + $env:PATH
cd "C:\Users\somen\StudioProjects\adjaba-player"

# ===== CHECK DEVICE =====
Write-Host "================================"
Write-Host "Checking for connected devices..."
Write-Host "================================"
& adb devices

# ===== WAIT FOR DEVICE =====
$deviceReady = $false
$attempts = 0
while (-not $deviceReady -and $attempts -lt 30) {
    Start-Sleep -Seconds 2
    $devices = & adb devices
    if ($devices -like "*device*" -and $devices -notlike "*offline*") {
        $deviceReady = $true
        Write-Host "✅ Device is ready!"
    }
    $attempts++
}

if (-not $deviceReady) {
    Write-Host "❌ Device not found. Check emulator window."
    exit
}

# ===== INSTALL APP =====
Write-Host ""
Write-Host "================================"
Write-Host "Installing app..."
Write-Host "================================"
& adb install -r app/build/outputs/apk/debug/app-debug.apk

# ===== LAUNCH APP =====
Write-Host ""
Write-Host "================================"
Write-Host "Launching app on TV emulator..."
Write-Host "================================"
Start-Sleep -Seconds 2
& adb shell am start -n com.rnd/com.rnd.activities.LoginActivity

Write-Host ""
Write-Host "✅ ============================================"
Write-Host "✅ App is running on Android TV emulator!"
Write-Host "✅ ============================================"
Write-Host ""
Write-Host "Next steps:"
Write-Host "1. Look at Android TV emulator window"
Write-Host "2. Use arrow keys to navigate"
Write-Host "3. Enter login: SCREEN_001 / 123456"
Write-Host "4. Test features (camera, analytics)"
Write-Host ""
```

---

## 🎯 Testing the App

Once app is running on TV:

### Login Test
```
1. Use arrow keys (↑↓←→) to navigate
2. Enter Screen ID: SCREEN_001
3. Enter PIN: 123456
4. Press Enter to LOGIN
5. You should see SelectScreens activity
```

### Navigation Test
```
↑ = Move up
↓ = Move down
← = Move left
→ = Move right
Enter = Select/Click
Escape = Go back
```

### Expected Flow
```
LoginActivity 
   ↓ (login)
SelectScreens 
   ↓ (click PLAY)
TestCamera (face detection)
   ↓ (capture demographics)
API sends impressions
```

---

## ⏱️ Timeline

| Step | Time | Description |
|------|------|-------------|
| Device check | 30 sec | Wait for emulator ready |
| Install APK | 30-45 sec | Copy app to emulator |
| Launch app | <3 sec | App appears |
| **Total** | **~2 min** | App running! |

---

## ✅ Success Indicators

When everything works:

✅ Device shows in `adb devices`
✅ Install command shows "Success"
✅ App appears on TV emulator window
✅ Screen is fullscreen landscape
✅ Can see LoginActivity
✅ Can navigate with arrow keys
✅ Can enter text in fields
✅ Can click LOGIN button

---

## 🐛 Troubleshooting

### Device Not Showing in `adb devices`
```
Wait 30-60 more seconds and try again
Emulator takes time to boot on first launch
```

### Install Fails
```
Try: adb uninstall com.rnd
Then: adb install app-debug.apk
```

### App Won't Launch
```
Check: adb logcat | grep "com.rnd"
Look for error messages
```

### Emulator Window Doesn't Appear
```
Check: Task Manager → Look for "emulator.exe"
If not there, restart with:
  & $env:ANDROID_HOME\emulator\emulator.exe @TV_36
```

---

## 📞 Support

For issues, check these files:
- **ANDROID_TV_RUNNING.md** - Installation guide
- **ANDROID_TV_SETUP.md** - Detailed TV setup
- **BUILD_SUCCESS.md** - Build information
- **QUICK_START_ANDROID_TV.md** - Quick reference

---

## 🎊 Ready!

**The app is built and waiting to be installed.**

**Run the script above and enjoy your Adjaba Player on Android TV! 📺✨**

---

**Time until app running: ~2-3 minutes**

**Status: Ready for installation**

🚀 **GO FOR IT!**

