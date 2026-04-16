# 📺 Adjaba Player - Android TV Simulator Setup

## 🎯 Goal
Run Adjaba Player on Android TV emulator (optimized for TV screens - 10+ inches)

---

## 📋 Current Status

| Item | Status | Details |
|------|--------|---------|
| Android SDK | ✅ Installed | C:\Users\somen\AppData\Local\Android\Sdk |
| TV Emulator | ❌ Not Created | Will create: TV_36 (Android 14) |
| Java/JDK | ❌ NOT installed | **REQUIRED - install first** |
| Source Code | ✅ Ready | Master branch with camera features |

---

## ⚠️ FIRST: Install Java/JDK

**Without Java, you CANNOT build the app.**

### Quick Java Installation (5 minutes)

**Option 1: Oracle JDK (Recommended)**
```
1. Download: https://www.oracle.com/java/technologies/downloads/
   Select: Windows x64 Installer (JDK 21)
   
2. Run installer: jdk-21_windows-x64_bin.exe

3. Set JAVA_HOME:
   - Win + X → System
   - Advanced system settings
   - Environment Variables
   - New: JAVA_HOME = C:\Program Files\Java\jdk-21

4. Restart PowerShell
5. Verify: java -version
```

**Option 2: Chocolatey**
```bash
choco install openjdk21
java -version
```

---

## 🖥️ Create Android TV Emulator

### Option A: Manual Creation (Using AVD Manager)

```bash
# Set environment
$env:ANDROID_HOME="C:\Users\somen\AppData\Local\Android\Sdk"

# Create TV emulator (Android 14 / API 34)
& "$env:ANDROID_HOME\tools\bin\avdmanager.exe" create avd `
  -n "TV_36" `
  -k "system-images;android-36;google_apis;x86_64" `
  -d "tv_1080p" `
  -f

# OR for older API:
& "$env:ANDROID_HOME\tools\bin\avdmanager.exe" create avd `
  -n "TV_34" `
  -k "system-images;android-34;google_apis;x86_64" `
  -d "tv_1080p" `
  -f
```

### Option B: Using Android Studio UI (If Installed)
1. Android Studio → Device Manager → Create Device
2. Select "Android TV" device type
3. Choose API level (34 or 36)
4. Name it "TV_Adjaba"
5. Finish

### Device Options
```
tv_1080p          - 1080p TV (24-inch)
tv_720p           - 720p TV (16-inch)
wear_round        - Wear OS (round)
Android TV (1080p) - Generic TV device
```

---

## 🏗️ Build App (Requires Java First)

```bash
# Navigate to project
cd C:\Users\somen\StudioProjects\adjaba-player

# Clean and build
./gradlew clean assembleDebug

# Output:
# ✅ app/build/outputs/apk/debug/app-debug.apk

# Expected time: 2-3 minutes
```

---

## 📺 Run on Android TV Emulator

### Step 1: Start the TV Emulator

```bash
$env:ANDROID_HOME="C:\Users\somen\AppData\Local\Android\Sdk"

# Start TV emulator (replace TV_36 with your emulator name)
& "$env:ANDROID_HOME\emulator\emulator.exe" @TV_36 -netdelay none -netspeed full

# Wait 60 seconds for emulator to fully boot
# You'll see Android TV home screen
```

### Step 2: Install and Run App

In a **NEW PowerShell window**:

```bash
cd C:\Users\somen\StudioProjects\adjaba-player

# List connected devices
adb devices
# Should show: emulator-5554 device

# Install app
adb install app/build/outputs/apk/debug/app-debug.apk

# Launch app
adb shell am start -n com.rnd/com.rnd.activities.LoginActivity

# App launches on TV screen!
```

---

## 🎮 Navigate on Android TV

### Using Keyboard (Emulator)
```
Arrow Keys → Navigate (D-Pad)
Enter      → Select/Click
Escape     → Back
Q          → Quit emulator
```

### Using Emulator Controller
```
Right-click in emulator window
→ Extended Controls
→ Virtual machine
→ Enable D-pad or arrow keys
```

---

## 📺 Android TV UI Optimization

The Adjaba Player will adapt to TV screen automatically:

### What Changes on TV
```
✅ Larger touch targets (buttons bigger)
✅ Better focus navigation (D-pad friendly)
✅ Full-screen optimized
✅ No phone status bar
✅ TV-safe area respected
✅ Remote control navigation
```

### LoginActivity on TV
```
┌──────────────────────────────────────────┐
│                                          │
│      ADJABA PLAYER 📺                   │
│      Digital Signage System             │
│                                          │
├──────────────────────────────────────────┤
│                                          │
│                                          │
│  Screen ID:                              │
│  ┌────────────────────────────────────┐  │
│  │ SCREEN_001                         │  │
│  └────────────────────────────────────┘  │
│                                          │
│  PIN/Password:                           │
│  ┌────────────────────────────────────┐  │
│  │ •••••••                            │  │
│  └────────────────────────────────────┘  │
│                                          │
│  ☑ Remember Me                           │
│                                          │
│  ┌────────────────────────────────────┐  │
│  │        [  LOGIN  ]                 │  │
│  └────────────────────────────────────┘  │
│                                          │
│  Terms & Conditions                      │
│                                          │
└──────────────────────────────────────────┘
```

---

## ✅ Quick Start (After Java Installed)

### Terminal 1: Create and Start TV Emulator
```bash
$env:ANDROID_HOME="C:\Users\somen\AppData\Local\Android\Sdk"

# Create TV emulator (first time only)
& "$env:ANDROID_HOME\tools\bin\avdmanager.exe" create avd `
  -n "TV_36" -k "system-images;android-36;google_apis;x86_64" `
  -d "tv_1080p" -f

# Start TV emulator (takes 30-60 seconds to boot)
& "$env:ANDROID_HOME\emulator\emulator.exe" @TV_36 -netdelay none -netspeed full
```

### Terminal 2: Build and Run App
```bash
cd C:\Users\somen\StudioProjects\adjaba-player

# Build
./gradlew clean assembleDebug

# Wait for emulator to boot (60 seconds)...

# Install and run
adb install app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.rnd/com.rnd.activities.LoginActivity

# App appears on TV screen!
```

---

## 🎯 Test on Android TV

### Test Case 1: Login Flow
```
1. Emulator shows LoginActivity
2. Use arrow keys to navigate to Screen ID field
3. Type: SCREEN_001
4. Press Tab or down arrow
5. Type: 123456 (PIN)
6. Arrow down to LOGIN button
7. Press Enter
8. Should navigate to SelectScreens
```

### Test Case 2: Screen Selection
```
1. SelectScreens appears
2. Navigate spinners with arrow keys
3. Select screen using Enter
4. Press down to PLAY button
5. Press Enter
6. Camera preview launches
```

### Test Case 3: Camera on TV
```
1. Camera preview shows on TV screen
2. Face detection works (same as phone)
3. Statistics update in real-time
4. Press ESC or Back to exit
5. Impressions sent to API
```

---

## 📊 TV Emulator Specs

### Android TV (1080p)
```
Resolution: 1920 x 1080
DPI: 160 dpi (24-inch TV)
RAM: 2 GB (configurable)
Storage: 200 MB (configurable)
Device: Generic TV
API: 34 or 36
Architecture: x86_64
```

### Android TV (720p)
```
Resolution: 1280 x 720
DPI: 160 dpi (16-inch TV)
RAM: 1 GB (configurable)
Storage: 200 MB (configurable)
Device: Generic TV
API: 34 or 36
Architecture: x86_64
```

---

## 🔌 Hardware Acceleration

### Enable for Better Performance

```bash
# Edit AVD config.ini
cd %USERPROFILE%\.android\avd\TV_36.avd

# Open config.ini and add/modify:
hw.gpu.enabled=yes
hw.gpu.mode=auto

# OR use command line (first time):
& "$env:ANDROID_HOME\tools\bin\avdmanager.exe" create avd `
  -n "TV_36" -k "system-images;android-36;google_apis;x86_64" `
  -d "tv_1080p" -f -c 1000M
```

---

## 🐛 Troubleshooting

### Issue: "Emulator failed to start"
```bash
# Solution 1: Kill existing processes
adb kill-server
adb start-server

# Solution 2: Delete and recreate AVD
adb uninstall com.rnd
# Delete: C:\Users\somen\.android\avd\TV_36.avd
# Recreate using avdmanager
```

### Issue: "App doesn't fill entire TV screen"
```
The app respects Android's TV-safe area (8% margin).
This is normal and required for TV apps.
No code changes needed.
```

### Issue: "Navigation doesn't work with arrow keys"
```bash
# Make sure emulator window is in focus
# Try: Right-click in emulator → Extended Controls
# Check: Virtual machine tab for D-pad settings
```

### Issue: "Java not found during build"
```bash
# Must install Java/JDK first!
# Download: https://www.oracle.com/java/technologies/downloads/
# Set JAVA_HOME environment variable
# Restart PowerShell
# Test: java -version
```

---

## 📋 Pre-Flight Checklist

Before running on TV emulator:

- [ ] Java/JDK installed and JAVA_HOME set
- [ ] Android SDK available at C:\Users\somen\AppData\Local\Android\Sdk
- [ ] TV Emulator created (TV_36 or TV_34)
- [ ] App built successfully: app/build/outputs/apk/debug/app-debug.apk
- [ ] Emulator boots without errors
- [ ] adb devices shows: emulator-5554 device
- [ ] App installs: adb install returns Success
- [ ] LoginActivity launches
- [ ] Can navigate with arrow keys

---

## 🎬 Expected Behavior on TV

### Visual Differences from Phone
```
Phone (5-6 inches)          TV (24+ inches)
─────────────────────────────────────────
Small buttons          →     Large buttons
Compact layout         →     Spacious layout
Phone UI paradigm      →     TV remote paradigm
Touch optimized        →     D-pad optimized
Portrait primary       →     Landscape primary
Status bar present     →     No status bar
```

### Functionality (Same as Phone)
```
✅ Login system (same)
✅ Screen selection (same)
✅ Face detection (same)
✅ Camera preview (same)
✅ Analytics (same)
✅ Database (same)
✅ API calls (same)
```

---

## 📱 Multi-Device Testing

Run on BOTH phone and TV simultaneously:

```bash
# Terminal 1: Phone emulator
& "$env:ANDROID_HOME\emulator\emulator.exe" @Medium_Phone_API_36.1

# Terminal 2: TV emulator
& "$env:ANDROID_HOME\emulator\emulator.exe" @TV_36

# Terminal 3: List devices
adb devices
# Shows: emulator-5554 and emulator-5556

# Install on both
adb -s emulator-5554 install app-debug.apk
adb -s emulator-5556 install app-debug.apk

# Run on both
adb -s emulator-5554 shell am start -n com.rnd/.activities.LoginActivity
adb -s emulator-5556 shell am start -n com.rnd/.activities.LoginActivity

# Now see app on phone AND TV side-by-side!
```

---

## 🚀 Full Android TV Deployment

After testing on emulator:

### Build Release APK
```bash
./gradlew assembleRelease

# Output: app/build/outputs/apk/release/app-release.apk
```

### Deploy to Google TV Store
```
1. Sign APK with release key
2. Create Google Play Console account
3. Upload signed APK
4. Fill app metadata (title, description, screenshots)
5. Submit for review
6. Wait for approval (~24 hours)
7. Live on Google TV!
```

### Deploy to Fire TV (Amazon)
```
1. Similar process on Amazon Appstore
2. More targeted for Android TV/Fire TV
3. Faster approval (~2-4 hours)
```

---

## 📊 Emulator vs Real TV

| Aspect | Emulator | Real TV |
|--------|----------|---------|
| **Cost** | Free | $200-500+ |
| **Speed** | Slower | Faster |
| **Testing** | Good | Better |
| **Real camera** | Simulated | Real |
| **Network** | Simulated | Real |
| **Performance** | Varies | Consistent |
| **Development** | Great | Essential |
| **Production** | Not suitable | Required |

---

## ✅ Success Criteria

After following this guide, you should see:

- [ ] TV Emulator boots successfully
- [ ] LoginActivity appears on TV screen
- [ ] UI fills entire TV area (with safe margins)
- [ ] Can navigate with arrow keys
- [ ] Can login and reach SelectScreens
- [ ] Can launch camera and detect faces
- [ ] No crashes or errors
- [ ] App returns to SelectScreens after camera
- [ ] Ready to merge staging for full TV digital signage

---

## 📚 Next Steps

1. **Install Java** (required) - 5 minutes
2. **Create TV Emulator** - 2 minutes
3. **Build App** - 2-3 minutes
4. **Run on TV** - 1 minute
5. **Test flow** - 5 minutes

**Total: ~15 minutes to have app running on Android TV emulator!**

---

## 🎯 Advanced: Staging Features on TV

After mastering master branch on TV, merge staging for:

```
✅ Full media playback optimized for TV
✅ MQTT real-time ad triggering
✅ News ticker (larger fonts for TV)
✅ Weather widget (TV-optimized layout)
✅ Background camera service
✅ Landscape-only mode (better for TV)
```

See: `BRANCH_COMPARISON.md` for staging features.

---

**Status: Ready to run on Android TV! Just need Java installed first.**

**Estimated Total Time: 20 minutes (including Java installation)**

