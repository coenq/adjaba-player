# 📺 Android TV - Quick Reference Card

## 🎯 MISSION: Run Adjaba Player on Android TV

---

## ✅ STATUS
- Android SDK: ✅
- Emulator ready: ✅
- Source code: ✅
- Documentation: ✅ (12 guides)
- Java/JDK: ❌ INSTALL NOW

---

## 🚀 4 COMMANDS (Copy & Paste)

### Command 1: Install Java
```
Download: https://www.oracle.com/java/technologies/downloads/
Run: jdk-21_windows-x64_bin.exe
Set: JAVA_HOME = C:\Program Files\Java\jdk-21
Verify: java -version
```

### Command 2: Create TV Emulator (1 line)
```bash
$env:ANDROID_HOME="C:\Users\somen\AppData\Local\Android\Sdk"; & "$env:ANDROID_HOME\tools\bin\avdmanager.exe" create avd -n "TV_36" -k "system-images;android-36;google_apis;x86_64" -d "tv_1080p" -f
```

### Command 3: Build App
```bash
cd C:\Users\somen\StudioProjects\adjaba-player
./gradlew clean assembleDebug
```

### Command 4: Run on TV
```bash
# Terminal 1: Start emulator (wait 60s)
$env:ANDROID_HOME="C:\Users\somen\AppData\Local\Android\Sdk"; & "$env:ANDROID_HOME\emulator\emulator.exe" @TV_36

# Terminal 2 (after emulator boots):
cd C:\Users\somen\StudioProjects\adjaba-player
adb install app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.rnd/com.rnd.activities.LoginActivity
```

---

## ⏱️ TIMELINE
| Step | Time |
|------|------|
| Install Java | 5 min |
| Create emulator | 2 min |
| Build app | 2-3 min |
| Emulator boot | 1 min |
| Install & run | 1 min |
| **TOTAL** | **~15 min** |

---

## 📱 ON TV SCREEN

**What you'll see**:
- Large LoginActivity (1080p, 24-inch sized)
- Buttons 2x larger than phone
- Text 2x larger and readable
- Full-screen, no status bar
- Landscape-friendly layout

**How to navigate**:
```
Arrow keys: Move
Enter:      Select
Escape:     Back
```

---

## 🎯 TEST FLOW

1. **Login**: SCREEN_001 / 123456
2. **Select Screen**: Portrait mode
3. **Play**: Launch camera
4. **Face Detection**: Stand in front
5. **Back**: Exit and send impressions

---

## 🔍 IF SOMETHING FAILS

| Problem | Fix |
|---------|-----|
| "Java not found" | Install JDK, set JAVA_HOME, restart |
| "Emulator won't start" | `adb kill-server` then try again |
| "Build fails" | Make sure Java installed and JAVA_HOME set |
| "adb not found" | Set ANDROID_HOME, restart PowerShell |
| "App crashes" | Check logs: `adb logcat -s "com.rnd"` |

---

## 📖 BEST GUIDES

1. **QUICK_START_ANDROID_TV.md** ← FASTEST (5 min read)
2. **ANDROID_TV_SETUP.md** ← DETAILED (20 min read)
3. **PHONE_VS_TV_GUIDE.md** ← COMPARISON (15 min read)

---

## ✨ KEY FEATURES ON TV

✅ Face detection (ML Kit)
✅ Gender classification (TensorFlow)
✅ Age estimation (TensorFlow)
✅ Smile detection (sentiment)
✅ Real-time statistics
✅ Database persistence
✅ API integration
✅ Full-screen display
✅ D-pad navigation

---

## 🎊 WHEN YOU SUCCEED

You'll see:
✅ Emulator boots with Android TV home
✅ LoginActivity fills TV screen
✅ UI is large and readable
✅ Can login successfully
✅ Camera launches with face detection
✅ App runs smoothly on TV emulator

**THAT'S IT! APP IS RUNNING ON TV! 🎉**

---

## 📍 FILE LOCATIONS

**Start reading**: `QUICK_START_ANDROID_TV.md`
**Project folder**: `C:\Users\somen\StudioProjects\adjaba-player\`

---

**NEXT STEP: Open QUICK_START_ANDROID_TV.md and follow 4 steps!**

**YOU GOT THIS! 📺🚀**

