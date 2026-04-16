# Running Adjaba Player on Android TV Emulator

## 📦 APK Ready
✅ **Debug APK**: `app/build/outputs/apk/debug/app-debug.apk` (130MB)

## 🚀 Steps to Run on TV Emulator

### Option 1: Using Android Studio (Recommended)

1. **Open Project in Android Studio**
   - File → Open → Select adjaba-player directory

2. **Create/Select Android TV Emulator**
   - Tools → Device Manager → Create Virtual Device
   - Choose "Android TV" as category
   - Select Android 13 or later (API 33+)
   - Name: "AndroidTV_Emulator" (or your preference)
   - Finish & Start the emulator

3. **Run the App**
   - Click Run (▶) or press Shift+F10
   - Select the Android TV emulator
   - App will install and launch automatically

### Option 2: Using ADB (Command Line)

1. **Start Android TV Emulator**
   ```bash
   # Find emulator path (usually in Android SDK)
   # Windows example:
   "C:\Android\sdk\emulator\emulator.exe" -avd AndroidTV_Emulator
   ```

2. **Install APK**
   ```bash
   adb install "C:\Users\somen\StudioProjects\adjaba-player\app\build\outputs\apk\debug\app-debug.apk"
   ```

3. **Launch App**
   ```bash
   adb shell am start -n com.rnd/.activities.LoginActivity
   ```

### Option 3: Using Command Line Build & Run

```bash
cd C:\Users\somen\StudioProjects\adjaba-player
./gradlew installDebug
```

## 📺 TV Emulator Specs (Recommended)

| Setting | Value |
|---------|-------|
| Device | Android TV (1080p) or (4K) |
| API Level | 33+ (Android 13+) |
| RAM | 2GB+ |
| Storage | 100MB+ free |
| Architecture | x86_64 |

## ⚙️ Configuration Before Running

### Login Credentials
The app requires authentication. You'll see:
- **Screen ID**: Login prompt
- **Authentication**: Enter credentials for your setup
- **Screen Selection**: Choose orientation & direction

### Network
- Ensure emulator has internet access
- API endpoints:
  - Main: `https://api.adjaba.in`
  - Reports: `https://api.buyir.uk/`
  - Weather: `https://api.weatherapi.com`

## 📊 What the App Does

### Flow:
1. **LoginActivity** → Initial login screen
2. **Login** → Authentication
3. **SelectScreens** → Choose orientation & screen settings
4. **ReportDashboardActivity** → Ad/News/Weather player (Landscape)

### Features:
✅ Ad playback
✅ News display
✅ Weather updates
✅ Screen management
✅ Impression tracking

## ✅ Verification Checklist

- [ ] Emulator starts successfully
- [ ] APK installs without errors
- [ ] App launches to LoginActivity
- [ ] UI is readable and responsive
- [ ] Navigation works (use arrow keys on keyboard for D-pad)
- [ ] ReportDashboardActivity displays content

## 🎮 TV Remote Simulation in Emulator

| Key | Action |
|-----|--------|
| Arrow Keys | Navigate |
| Enter | Select/Click |
| Esc | Back |
| F1 | Menu |

## 🔧 Troubleshooting

**APK not installing?**
```bash
adb uninstall com.rnd
# Then reinstall
```

**App crashes on launch?**
- Check logcat: `adb logcat | grep com.rnd`
- Ensure proper network connectivity in emulator

**No internet in emulator?**
- Check DNS settings: Settings → Network → DNS
- Try: 8.8.8.8 or 1.1.1.1

**Build fails?**
```bash
./gradlew clean assembleDebug
```

## 📱 Device Info
- **Package**: com.rnd
- **App Name**: RetailAI
- **Min SDK**: 24
- **Target SDK**: 34
- **Build Type**: Debug

---

**Ready to test!** Start the TV emulator and run the app. 🎬
