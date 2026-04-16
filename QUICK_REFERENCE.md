# Adjaba Player - Quick Reference Guide

## 🎯 You Are Here

**Current Branch**: `release` (based on master @ 7a8f360)  
**Current Action**: Checked out from master ✅  
**Documentation**: Complete ✅

---

## 📚 Documentation Files Created

I've created 4 comprehensive guides for you:

### 1. **APP_FLOW_SIMULATION.md** 📱
**What**: Complete user journey through the app
- Login flow with credential validation
- Screen selection and configuration
- Face detection and demographic analytics
- Camera preview with statistics
- Impression tracking and API sync
- Database schema
- API endpoints

**Best for**: Understanding how the app works from user perspective

---

### 2. **BRANCH_COMPARISON.md** 🌿
**What**: Master vs Staging feature comparison
- Side-by-side feature matrix
- Staging branch advanced features:
  - MQTT real-time ad switching
  - Full media player (images + videos)
  - News ticker (BBC RSS)
  - Weather widget
  - CameraAnalyticsService
- Detailed code examples for staging features
- Testing checklist

**Best for**: Deciding which branch to use and understanding staging capabilities

---

### 3. **RELEASE_STRATEGY.md** 📦
**What**: Release branch management guide
- Version management and tagging
- APK build & signing process
- Deployment checklist
- Security considerations
- Release notes template
- Quick start build commands

**Best for**: Building and deploying the app

---

### 4. **APP_FLOW_SIMULATION.md** (this guide)
**What**: Quick reference for all key information
- Current status
- Quick commands
- FAQ
- Troubleshooting

**Best for**: Quick lookups and common tasks

---

## 🚀 Quick Commands

### **Current Status**
```bash
cd C:\Users\somen\StudioProjects\adjaba-player
git status
# Should show: On branch 'release'
```

### **View Branch Tree**
```bash
git log --oneline --all --graph -10
```

### **See What's Different from Master**
```bash
git diff master release
# Should be empty (release = master currently)
```

### **Update to Latest Master (if needed)**
```bash
git checkout master
git pull origin master
git checkout release
git merge master
```

### **View Documentation**
```bash
# Open any of the created files:
# APP_FLOW_SIMULATION.md
# BRANCH_COMPARISON.md
# RELEASE_STRATEGY.md
```

---

## 🌿 Branch Overview

```
Remote Origin (GitHub)
├── main/master (deprecated?)
├── feature/client-updates
├── milestone1 (has new features)
├── staging (has FULL features - media player, MQTT)
└── ui-improves

Local Branches
├── master (current stable, 7a8f360)
├── milestone1 (intermediate features)
├── staging (advanced features, 27d776a)
└── release ← YOU ARE HERE
    └── Same as master currently
```

---

## 📋 Master Branch Features

### **Available Now** ✅
- **Authentication**: JWT token-based login
- **Screen Management**: Select and configure screens
- **Face Detection**: Real-time ML Kit + TensorFlow
- **Demographics**: Age/Gender/Sentiment tracking
- **Analytics**: Camera preview with statistics
- **Database**: Room ORM for local persistence
- **API Sync**: Retrofit 2 HTTP client
- **Impression Tracking**: Send analytics to server

### **Missing** ❌
- **Media Playback**: No image/video display
- **MQTT Integration**: No real-time ad switching
- **News/Weather**: No widget support
- **Background Camera**: No ForegroundService

---

## 📈 Staging Branch Features (Extra)

Beyond master:
- ✅ **Full Media Player** (images, videos)
- ✅ **MQTT Real-Time Triggers** (demographic-based ad selection)
- ✅ **News Ticker** (BBC RSS feed, 15-min refresh)
- ✅ **Weather Widget** (weatherapi.com, 15-min refresh)
- ✅ **Background Camera Service** (runs silently)
- ✅ **Playlist Management** (hourly reload)
- ✅ **Dynamic Ad Scoring** (gender/age/sentiment matching)

---

## 🎯 What Should You Do Next?

### **Option A: Build Release APK (Master)**
1. If Java/JDK installed:
   ```bash
   ./gradlew clean
   ./gradlew assembleRelease
   # Output: app/build/outputs/apk/release/app-release.apk
   ```
2. Test on Android device
3. Create release notes
4. Tag version: `git tag -a v1.0 -m "Initial release"`

### **Option B: Merge Staging into Release**
1. If you want full features:
   ```bash
   git merge staging
   # Resolves conflicts if any
   ```
2. This adds media player, MQTT, news, weather

### **Option C: Just Review Code**
1. Read the generated documentation
2. Explore the source files
3. Plan improvements

---

## ❓ FAQ

**Q: Why is master behind staging?**
A: Master is the stable release branch. Staging has experimental features (media player, MQTT) that need more testing.

**Q: Should I merge staging into release?**
A: Only if:
- Features are fully tested
- All dependencies resolved
- No breaking changes
- Security review passed

**Q: Can I build without Java?**
A: No. Gradle requires JDK. Install Java first.

**Q: What's the difference between release and staging?**
A: Release = stable master features only  
Staging = experimental advanced features (media, MQTT, news, weather)

**Q: How do I deploy to users?**
A: Build signed APK on release branch, distribute via Play Store or direct download

**Q: Can both branches coexist in one installation?**
A: No. Choose one version to install on device.

**Q: What if master gets new features?**
A: Update release branch: `git merge master`

---

## 🐛 Troubleshooting

### **Issue: `git checkout release` fails**
```bash
# Solution: Branch already exists locally
git branch -a  # List all branches
git checkout release  # This should work
```

### **Issue: Permission denied on app folder**
```bash
# Solution: Run terminal as Administrator
# Right-click PowerShell → Run as Administrator
```

### **Issue: Git command not found**
```bash
# Solution: Install Git from https://git-scm.com/download/win
# Restart PowerShell after installation
```

### **Issue: Java/JDK not found**
```bash
# Solution: Install JDK
# Download from https://www.oracle.com/java/technologies/downloads/
# OR use OpenJDK from https://adoptopenjdk.net/
# Set JAVA_HOME environment variable
```

---

## 📊 Project Statistics

| Metric | Value |
|--------|-------|
| **Total Java Files** | 30+ |
| **Total Activities** | 5 (Login, SelectScreens, TestCamera, TermsActivity, ReportDashboard) |
| **Database Entities** | 2 (ReportEntity, ImpressionEntity) |
| **API Endpoints** | 3 (login, getScreens, sendImpressions) |
| **ML Models** | 3 (gender, age, ML Kit face detection) |
| **Target SDK** | Android 14 (API 34) |
| **Min SDK** | Android 8.0 (API 24) |
| **Main Language** | Java |
| **Build System** | Gradle |
| **Package Name** | com.rnd |

---

## 🔒 Security Checklist

- ✅ **Token Storage**: AES256-GCM encryption enabled
- ✅ **HTTP**: HTTPS only (no cleartext traffic)
- ✅ **Permissions**: Camera, Storage (runtime permission checks)
- ✅ **Validation**: Input validation on login
- ✅ **API Auth**: Bearer token in Authorization header
- ✅ **Database**: Room with encryption support
- ⚠️ **Code Obfuscation**: Needs ProGuard for release build

---

## 📞 Common Tasks

### **Task: Add new feature to release**
```bash
git checkout release
git checkout -b feature/new-feature
# Make changes
git commit -m "feat: add new feature"
git push origin feature/new-feature
# Create Pull Request
```

### **Task: Hotfix critical bug**
```bash
git checkout release
git checkout -b hotfix/bug-fix
# Fix the bug
git commit -m "fix: critical bug"
# Create Pull Request → merge to release
```

### **Task: Update dependencies**
```bash
# Edit app/build.gradle
# Update version numbers
git add app/build.gradle
git commit -m "chore: update dependencies"
git push origin release
```

### **Task: Check API status**
```bash
# Test API endpoints:
# Login: curl -X POST https://api.adjaba.in/v2/authenticate_user
# Screens: curl -X GET https://api.adjaba.in/get_screen_by_user \
#           -H "Authorization: Bearer TOKEN"
```

---

## 📈 Metrics to Track

Monitor these for app health:

```
Performance:
- App startup time (target: < 3 seconds)
- Face detection FPS (target: 30 FPS)
- API response time (target: < 2 seconds)

Stability:
- Crash rate (target: < 0.1%)
- ANR rate (target: < 0.05%)
- Memory leaks (target: 0)

User Experience:
- Camera permission grant rate (target: > 95%)
- Login success rate (target: > 99%)
- Session duration (target: > 30 minutes)

Analytics:
- Demographics accuracy (target: > 85%)
- Face detection sensitivity (target: > 90%)
- API uptime (target: > 99.9%)
```

---

## 🎓 Learning Resources

### **For Understanding the Code**
1. Read: APP_FLOW_SIMULATION.md (complete flow)
2. Review: `app/src/main/java/com/rnd/activities/LoginActivity.java`
3. Study: `app/src/main/java/com/rnd/face_detection/FaceRecognitionProcessor.java`
4. Explore: Room database entities in `app/src/main/java/com/rnd/room/`

### **For Building Features**
1. Retrofit: https://square.github.io/retrofit/
2. Room: https://developer.android.com/training/data-storage/room
3. ML Kit: https://developers.google.com/ml-kit
4. TensorFlow Lite: https://www.tensorflow.org/lite

### **For Deployment**
1. Read: RELEASE_STRATEGY.md
2. Follow: Google Play Store publishing guide
3. Review: Security best practices for Android

---

## ✅ Current Status Summary

| Item | Status | Details |
|------|--------|---------|
| **Current Branch** | ✅ release | Checked out from master |
| **Documentation** | ✅ Complete | 4 guides created |
| **Code State** | ✅ Stable | Master branch features intact |
| **Build Ready** | ⚠️ Pending | Requires Java/JDK |
| **Testing** | ⚠️ Pending | Needs device/emulator |
| **Deployment** | ⚠️ Pending | After successful build |
| **MQTT** | ❌ Not Available | In staging branch |
| **Media Player** | ❌ Not Available | In staging branch |
| **News/Weather** | ❌ Not Available | In staging branch |

---

## 🎯 Recommended Next Steps

1. **Immediate** (Now):
   - ✅ Review documentation files
   - ✅ Understand the app flow
   - ✅ Plan your next action

2. **Short-term** (Today):
   - ⏳ Install Java/JDK (if not installed)
   - ⏳ Build release APK
   - ⏳ Test on device

3. **Medium-term** (This week):
   - ⏳ Review staging branch features
   - ⏳ Decide: master-only or merge staging?
   - ⏳ Plan version 1.0 release

4. **Long-term** (This month):
   - ⏳ Deploy to production
   - ⏳ Monitor app performance
   - ⏳ Plan version 2.0 (with staging merge)

---

## 📝 Notes

- All documentation is in the project root folder
- Hyperlinks in docs reference code locations
- Code examples are from actual source files
- Flows are based on commit history analysis

---

**Generated**: April 15, 2026  
**For Project**: Adjaba Player - Digital Signage  
**Current Branch**: release (based on master)  
**Status**: Ready for development, testing, and deployment

**Next Action**: Choose your path → Build Release, Merge Staging, or Continue Development

