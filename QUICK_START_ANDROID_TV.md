# 📺 Quick Start: Run Adjaba Player on Android TV Emulator

## 🚀 FASTEST WAY (4 Steps)

### Step 1️⃣: Install Java/JDK (5 minutes)

**Download**: https://www.oracle.com/java/technologies/downloads/

**Choose**: Windows x64 Installer (JDK 21)

**Install**:
- Run `jdk-21_windows-x64_bin.exe`
- Follow wizard (default options fine)

**Verify** (in PowerShell):
```bash
java -version
# Should show: java version "21.x.x"
```

**Set JAVA_HOME** (Windows):
- Win + X → System
- Advanced system settings
- Environment Variables
- New variable:
  - Name: `JAVA_HOME`
  - Value: `C:\Program Files\Java\jdk-21`
- Click OK
- Restart PowerShell

---

### Step 2️⃣: Create Android TV Emulator (2 minutes)

**Copy and paste this in PowerShell:**

```bash
$env:ANDROID_HOME="C:\Users\somen\AppData\Local\Android\Sdk"

# Create TV emulator (1080p)
& "$env:ANDROID_HOME\tools\bin\avdmanager.exe" create avd `
  -n "TV_36" `
  -k "system-images;android-36;google_apis;x86_64" `
  -d "tv_1080p" `
  -f

# Should end with: "Created AVD 'TV_36'"
```

---

### Step 3️⃣: Build the App (2 minutes)

```bash
cd C:\Users\somen\StudioProjects\adjaba-player
./gradlew clean assembleDebug

# Wait for "BUILD SUCCESSFUL"
# Output: app/build/outputs/apk/debug/app-debug.apk
```

---

### Step 4️⃣: Run on TV Emulator

**Terminal 1** (Start emulator):
```bash
$env:ANDROID_HOME="C:\Users\somen\AppData\Local\Android\Sdk"
& "$env:ANDROID_HOME\emulator\emulator.exe" @TV_36 -netdelay none -netspeed full

# Wait 60 seconds for Android TV home screen to appear
```

**Terminal 2** (Install and run app):
```bash
cd C:\Users\somen\StudioProjects\adjaba-player
adb devices
# Should show: emulator-5554 device

adb install app/build/outputs/apk/debug/app-debug.apk
# Should show: Success

adb shell am start -n com.rnd/com.rnd.activities.LoginActivity
# App launches on TV screen!
```

---

## 🎮 Navigate on TV

Use keyboard arrow keys:
```
↑ ↓ → ←   Navigate (D-Pad)
Enter     Select/Click
Escape    Back/Exit
```

---

## 📺 On TV Screen You'll See

### Login Screen
```
ADJABA PLAYER 📺

Screen ID:  SCREEN_001
PIN:        123456
✓ Remember Me

[LOGIN]
```

**Navigate with arrow keys** → Select with Enter

### Camera Preview
```
LIVE PREVIEW

Face detected: Person
├─ Age: 28
├─ Gender: Male
└─ Smile: 85%

[BACK]
```

---

## ✅ Success Checklist

After Step 4, verify:

- [ ] Emulator boots (Android TV home visible)
- [ ] LoginActivity appears on TV screen
- [ ] UI fills entire TV area
- [ ] Can navigate with arrow keys
- [ ] Can login and see SelectScreens
- [ ] Can launch camera
- [ ] Face detection works
- [ ] No crashes

**All checked? ✅ Your app is running on Android TV!**

---

## 🐛 If Something Fails

| Problem | Solution |
|---------|----------|
| "Java not found" | Install Java first, set JAVA_HOME |
| "Emulator fails to start" | `adb kill-server` then retry |
| "adb not found" | Set ANDROID_HOME and restart PowerShell |
| "App won't install" | `adb uninstall com.rnd` then reinstall |
| "Build fails" | Install Java and set JAVA_HOME |

---

## 📊 Expected Times

| Task | Time |
|------|------|
| Install Java | 5 min |
| Create TV emulator | 2 min |
| Build app | 2-3 min |
| Emulator boot | 60 sec |
| Install app | 30 sec |
| Total | ~15 min |

---

## 🎯 That's It!

You now have Adjaba Player running on Android TV emulator.

**Next**: Test login, camera, face detection, and analytics on TV screen!

---

For more details, see: `ANDROID_TV_SETUP.md`

