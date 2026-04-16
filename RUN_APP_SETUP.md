# 🚀 Running Adjaba Player - Setup Guide

## Current System Status

✅ **Android SDK**: Found at `C:\Users\somen\AppData\Local\Android\Sdk`  
✅ **Android Emulator**: Available (`Medium_Phone_API_36.1`)  
❌ **Java/JDK**: NOT installed  

---

## ⚠️ BLOCKER: Java/JDK Required

To build and run the app, you **MUST** have Java Development Kit (JDK) installed.

### Option 1: Install Oracle JDK (Recommended)

1. Download from: https://www.oracle.com/java/technologies/downloads/
   - Select: **Windows x64 Installer** (latest LTS version)
   - Currently: JDK 21 LTS or JDK 17 LTS

2. Run the installer:
   ```
   jdk-21_windows-x64_bin.exe (or latest version)
   ```

3. Follow installation wizard:
   - Accept license
   - Choose installation directory (default is fine)
   - Note the installation path

4. Verify installation:
   ```bash
   java -version
   javac -version
   ```
   Should show: `java version "21.x.x"`

5. Set JAVA_HOME environment variable:
   - Press `Win + X` → System
   - Click "Advanced system settings"
   - Click "Environment Variables"
   - Click "New" (under System variables)
   - Variable name: `JAVA_HOME`
   - Variable value: `C:\Program Files\Java\jdk-21` (or your installation path)
   - Click OK

6. Restart PowerShell and verify:
   ```bash
   echo $env:JAVA_HOME
   ```

### Option 2: Install OpenJDK (Free Alternative)

1. Download from: https://adoptopenjdk.net/
   - Select: Temurin (recommended) or Eclipse Adoptium
   - Version: JDK 21 or 17
   - Platform: Windows x64

2. Run installer and follow same steps as Oracle JDK

### Option 3: Use Chocolatey (If Installed)

```bash
# Install OpenJDK via Chocolatey
choco install openjdk21

# Verify
java -version
```

---

## 📋 Pre-Flight Checklist

After installing Java, verify all prerequisites:

```bash
# 1. Check Java
java -version
# Should show: java version "21.x.x" (or 17+)

# 2. Check JAVA_HOME
echo $env:JAVA_HOME
# Should show: C:\Program Files\Java\jdk-21 (or similar)

# 3. Check Android SDK
echo $env:ANDROID_HOME
# OR check local.properties file for: sdk.dir=...

# 4. Check Gradle
./gradlew --version
# Should show: Gradle 8.x

# 5. List Android Emulators
$env:ANDROID_HOME="C:\Users\somen\AppData\Local\Android\Sdk"
& "$env:ANDROID_HOME\emulator\emulator.exe" -list-avds
# Should show: Medium_Phone_API_36.1
```

---

## 🏗️ Build Steps

Once Java is installed:

```bash
# 1. Navigate to project
cd C:\Users\somen\StudioProjects\adjaba-player

# 2. Clean previous builds
./gradlew clean

# 3. Build debug APK (for emulator/device testing)
./gradlew assembleDebug
# Output: app/build/outputs/apk/debug/app-debug.apk

# OR build release APK (for production)
./gradlew assembleRelease
# Output: app/build/outputs/apk/release/app-release.apk
```

---

## 📱 Run on Android Emulator

### Step 1: Start Emulator

```bash
# Set ANDROID_HOME
$env:ANDROID_HOME="C:\Users\somen\AppData\Local\Android\Sdk"

# Start the emulator (this will take 30-60 seconds)
& "$env:ANDROID_HOME\emulator\emulator.exe" @Medium_Phone_API_36.1

# Wait for emulator to fully boot (you'll see Android home screen)
```

### Step 2: Build and Run App

In a **NEW PowerShell window** (keep emulator running in first window):

```bash
# Navigate to project
cd C:\Users\somen\StudioProjects\adjaba-player

# Run on emulator (builds debug APK and installs)
./gradlew installDebug
./gradlew runDebug

# OR use adb directly
adb devices
# Should show: emulator-5554 device

adb install app/build/outputs/apk/debug/app-debug.apk
```

### Step 3: Start the App

Once installed, either:
- **Emulator**: Tap the app icon "Adjaba Player"
- **Command**: `adb shell am start -n com.rnd/com.rnd.activities.LoginActivity`

---

## 🔌 Run on Physical Device (USB)

### Prerequisites

1. Enable USB Debugging on your Android device:
   - Settings → About Phone
   - Tap "Build Number" 7 times (Developer options enabled)
   - Go to Settings → Developer Options
   - Enable "USB Debugging"

2. Connect device via USB cable

### Build and Install

```bash
cd C:\Users\somen\StudioProjects\adjaba-player

# Verify device is connected
adb devices
# Should show: [device-name] device

# Build and install
./gradlew installDebug

# Start app
adb shell am start -n com.rnd/com.rnd.activities.LoginActivity
```

---

## 🧪 Test the App Flow

### Login Screen
```
Screen ID: SCREEN_001
PIN: 123456
Remember Me: ✓ Checked
→ Click LOGIN
```

Expected: Navigate to SelectScreens activity

### Select Screen
```
Screen Mode: Portrait
Playlist: Default
Screen: Any available
→ Click PLAY
```

Expected: Launch TestCamera (face detection preview)

### Camera Preview
```
- Camera starts automatically
- Stand in front of camera
- Face detection bounding boxes appear
- Demographics counted (age/gender/sentiment)
→ Press BACK to exit
```

Expected: Impression sent to API, return to SelectScreens

---

## 🐛 Debugging

### View App Logs

```bash
# Real-time logs
adb logcat -s "adjaba-player"

# Or use Android Studio's Logcat viewer
# File → Open project → adjaba-player
# View → Tool Windows → Logcat
```

### Check Package Name

```bash
# List installed packages
adb shell pm list packages | grep rnd

# Launch app via package
adb shell am start -n com.rnd/.activities.LoginActivity
```

### Uninstall App

```bash
adb uninstall com.rnd
```

---

## ⚠️ Common Issues

### **Issue: "JAVA_HOME is not set"**
```bash
# Solution: Set JAVA_HOME environment variable
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"

# Verify
echo $env:JAVA_HOME
```

### **Issue: "Gradle wrapper not found"**
```bash
# Solution: Use full path or ensure gradlew.bat exists
dir gradlew.bat
./gradlew.bat clean
```

### **Issue: "Emulator won't start"**
```bash
# Solution: Reset emulator AVD
& "$env:ANDROID_HOME\tools\bin\avdmanager.exe" list avd
# Then delete and recreate AVD if needed
```

### **Issue: "App crashes on startup"**
```bash
# Check logs
adb logcat -s "com.rnd"

# Common causes:
# - Missing permissions (Camera, Storage)
# - API key not configured
# - Network connectivity
```

### **Issue: "adb device not found"**
```bash
# Solution: Reconnect and authorize
adb kill-server
adb start-server
adb devices

# If still not recognized, install drivers:
# Download: Android SDK Platform Tools
# Or: Use Google USB Driver
```

---

## 🎯 Quick Start (After Java Installed)

```bash
# Terminal 1: Start emulator
$env:ANDROID_HOME="C:\Users\somen\AppData\Local\Android\Sdk"
& "$env:ANDROID_HOME\emulator\emulator.exe" @Medium_Phone_API_36.1

# Wait 30 seconds for emulator to boot...

# Terminal 2: Build and run app
cd C:\Users\somen\StudioProjects\adjaba-player
./gradlew installDebug runDebug

# Wait for app to launch on emulator screen!
```

---

## 📊 Expected App Behavior

### Login Activity
- **Input fields**: Email (Screen ID), Password (PIN)
- **Remember Me checkbox**: Saves credentials
- **Terms & Conditions link**: Opens TermsActivity
- **Error handling**: Shows Toast for invalid input

### SelectScreens Activity
- **Spinners**: Screen selection, mode, interval
- **API call**: Fetches user's screens
- **Play button**: Launches TestCamera

### TestCamera Activity
- **Live preview**: Camera view with face detection
- **Bounding boxes**: Around detected faces
- **Statistics**: Age/Gender/Sentiment counts
- **Back button**: Sends impressions and exits

---

## 🚀 After App Runs Successfully

1. ✅ Verify all activities load correctly
2. ✅ Test login with credentials
3. ✅ Check camera face detection working
4. ✅ Review logs for errors
5. ✅ Test API connectivity
6. ✅ Check database persistence

---

## 📚 Resources

- Android Emulator Docs: https://developer.android.com/studio/run/emulator
- ADB Commands: https://developer.android.com/tools/adb
- Gradle Build System: https://developer.android.com/build
- Java JDK Download: https://www.oracle.com/java/technologies/downloads/

---

## ✅ Status After Following This Guide

- ✅ Java/JDK installed
- ✅ Gradle configured
- ✅ App built successfully
- ✅ App running on emulator/device
- ✅ Login flow tested
- ✅ Camera working
- ✅ API integrated
- ✅ Ready for production deployment

---

**Next Steps**:
1. Install Java/JDK
2. Follow Quick Start section above
3. App will launch on emulator!


