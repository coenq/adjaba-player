# ⚠️ Java Version Issue - Java 11+ Required

## Problem
Your current Java: **1.8.0_481 (Java 8)**
Required: **Java 11 or newer**

**Error**: 
```
Dependency requires at least JVM runtime version 11. 
This build uses a Java 8 JVM.
```

---

## Solution: Install Java 11+ (JDK)

### Option 1: Oracle JDK 21 (Recommended) - 5 minutes

1. **Download**:
   - Go to: https://www.oracle.com/java/technologies/downloads/
   - Select: **JDK 21** (or JDK 17 LTS)
   - Choose: **Windows x64 Installer**

2. **Install**:
   - Run: `jdk-21_windows-x64_bin.exe`
   - Follow installer (default options OK)
   - Note installation path (usually: `C:\Program Files\Java\jdk-21`)

3. **Set Environment Variable**:
   - Press: `Win + X` → System
   - Click: Advanced system settings
   - Click: Environment Variables
   - Click: New (under System variables)
   - Variable name: `JAVA_HOME`
   - Variable value: `C:\Program Files\Java\jdk-21` (or your install path)
   - Click: OK → OK → OK

4. **Verify**:
   - Restart PowerShell
   - Run: `java -version`
   - Should show: `java version "21.x.x"` or `java version "17.x.x"`

5. **Build App**:
   ```bash
   cd C:\Users\somen\StudioProjects\adjaba-player
   ./gradlew clean assembleDebug
   ```

---

### Option 2: OpenJDK 21 (Free Alternative) - 5 minutes

1. **Download**: https://adoptopenjdk.net/ or https://jdk.java.net/
   - Select: JDK 21 or 17
   - Platform: Windows x64

2. **Install**: Same as Option 1 above

---

### Option 3: Using Chocolatey (If installed)

```bash
choco install openjdk21
```

Then restart PowerShell and verify: `java -version`

---

## Quick Steps to Get Running

1. **Install Java 11+** (5 minutes) - Download and run installer
2. **Set JAVA_HOME** (1 minute) - Environment variable
3. **Restart PowerShell** (1 minute)
4. **Build App** (2-3 minutes)
   ```bash
   cd C:\Users\somen\StudioProjects\adjaba-player
   ./gradlew clean assembleDebug
   ```
5. **Create TV Emulator** (2 minutes)
   ```bash
   $env:ANDROID_HOME="C:\Users\somen\AppData\Local\Android\Sdk"
   & "$env:ANDROID_HOME\tools\bin\avdmanager.exe" create avd `
     -n "TV_36" `
     -k "system-images;android-36;google_apis;x86_64" `
     -d "tv_1080p" `
     -f
   ```
6. **Start Emulator** (1 minute) - Wait 60 seconds
   ```bash
   & "$env:ANDROID_HOME\emulator\emulator.exe" @TV_36
   ```
7. **Install & Run** (1 minute)
   ```bash
   adb install app/build/outputs/apk/debug/app-debug.apk
   adb shell am start -n com.rnd/com.rnd.activities.LoginActivity
   ```

**Total: ~15 minutes and you'll have the app running on Android TV!** 🎉

---

## Why Java 8 Doesn't Work

- Android Gradle Plugin 8.10.1 requires Java 11+
- Java 8 is from 2014 (12 years old)
- Modern Android tools need newer Java features
- JRE (what you have) is runtime only
- JDK (what you need) includes compiler + tools

---

## After Installing Java 11+

Run this command to start everything:

```bash
# Set JAVA_HOME (one time per session, or add to environment)
$env:JAVA_HOME="C:\Program Files\Java\jdk-21"

# Build app
cd C:\Users\somen\StudioProjects\adjaba-player
./gradlew clean assembleDebug

# Wait 2-3 minutes for build...
# Then: "BUILD SUCCESSFUL" ✅
```

---

## Need Help?

After installing Java 11+:
1. Open: `QUICK_START_ANDROID_TV.md`
2. Follow 4 steps
3. App running on TV in 15 minutes!

---

**Next: Install Java 11+ and you're ready to go!** 🚀

