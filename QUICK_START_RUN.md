# 🚀 Quick Start: Run Adjaba Player on Emulator

## ⚡ TL;DR (Too Long; Didn't Read)

### What You Need
- ✅ Android SDK (you have this)
- ✅ Android Emulator (you have: `Medium_Phone_API_36.1`)
- ❌ **Java/JDK (you need to install this)**

### Install Java (5 minutes)

**Option A: Oracle JDK (Recommended)**
1. Download: https://www.oracle.com/java/technologies/downloads/ (JDK 21 or 17)
2. Run installer: `jdk-21_windows-x64_bin.exe`
3. Set environment variable:
   - Win + X → System
   - Advanced system settings
   - Environment Variables
   - New → Name: `JAVA_HOME`, Value: `C:\Program Files\Java\jdk-21`
4. Restart PowerShell
5. Test: `java -version`

**Option B: Chocolatey (1 command)**
```bash
choco install openjdk21
```

---

## 🏗️ Build App (After Java Installed)

```bash
cd C:\Users\somen\StudioProjects\adjaba-player
./gradlew clean assembleDebug
# Wait 2-3 minutes...
# Output: app/build/outputs/apk/debug/app-debug.apk ✅
```

---

## 📱 Run on Emulator

### Terminal 1: Start Emulator
```bash
$env:ANDROID_HOME="C:\Users\somen\AppData\Local\Android\Sdk"
& "$env:ANDROID_HOME\emulator\emulator.exe" @Medium_Phone_API_36.1
# Wait 30 seconds for emulator to boot (you'll see Android home screen)
```

### Terminal 2: Install & Run App
```bash
cd C:\Users\somen\StudioProjects\adjaba-player
./gradlew installDebug
./gradlew runDebug

# OR manually:
adb install app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.rnd/com.rnd.activities.LoginActivity
```

---

## 🎮 Test the App

### Login
```
Screen ID: SCREEN_001
PIN: 123456
✓ Remember Me
→ Click LOGIN
```

### Select Screen
```
Mode: Portrait
Screen: Any (auto-filled)
→ Click PLAY
```

### Camera Test
```
Stand in front of camera
You should see:
- Face detection bounding boxes
- Age/Gender/Sentiment labels
- Statistics updating in real-time
- Smile percentage

→ Press BACK to exit
```

---

## 📊 What Happens Behind the Scenes

```
1. Login → API validation → Token encrypted and saved
2. SelectScreens → Fetch user's screens from API → Display in dropdown
3. Play → Launch camera → Detect faces → Run TensorFlow models
4. Camera running → 45 seconds → Collect statistics
5. Back pressed → Send impressions to API → Save to database
6. Return to SelectScreens → Ready to play again
```

---

## 🐛 If Something Goes Wrong

| Error | Solution |
|-------|----------|
| `JAVA_HOME not set` | Install JDK and set environment variable |
| `gradle command not found` | Use `./gradlew.bat` instead of `./gradlew` |
| `Emulator won't start` | Kill all adb processes: `adb kill-server` |
| `App crashes on startup` | Check logs: `adb logcat -s "com.rnd"` |
| `Camera permission denied` | Allow when prompted on emulator |

---

## ✅ Verification Checklist

After following steps above:

- [ ] Java version shows 21 or 17
- [ ] Gradle build succeeds (ends with "BUILD SUCCESSFUL")
- [ ] APK created: `app/build/outputs/apk/debug/app-debug.apk`
- [ ] Emulator boots successfully
- [ ] App installs: `adb install` returns "Success"
- [ ] LoginActivity opens
- [ ] Can login and navigate to SelectScreens
- [ ] Can start camera and see face detection
- [ ] No crashes in logcat

**If all checked ✅ → App is running successfully!**

---

## 📚 Learn More

- **Full Setup Guide**: `RUN_APP_SETUP.md`
- **Visual Simulation**: `APP_RUNNING_SIMULATION.md`
- **Complete Flow**: `APP_FLOW_SIMULATION.md`
- **Master vs Staging**: `BRANCH_COMPARISON.md`

---

## 🎯 Next After App Runs

1. ✅ Test login (credentials: SCREEN_001 / 123456)
2. ✅ Test screen selection
3. ✅ Test camera/face detection
4. ✅ Verify impressions sent to API
5. ✅ Check logs for any errors
6. ✅ Review database contents
7. ✅ Plan improvements or merge staging features

---

**That's it! You're ready to run the app. Follow the steps above and the Adjaba Player will be live on your emulator! 🎉**

