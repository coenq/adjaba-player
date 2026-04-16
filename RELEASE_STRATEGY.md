# Adjaba Player - Release Branch Strategy

## 📦 Release Branch Overview

You are currently on the **`release`** branch, which is based on the **master** branch at commit `7a8f360`.

```
master (7a8f360) ── release ← YOU ARE HERE
         │
         └─── staging (27d776a) - Advanced features (MQTT, media player, etc.)
```

---

## 🎯 Release Branch Goals

The `release` branch is designated for:

1. **Stable, tested builds** ready for production deployment
2. **Bug fixes and security patches** to master features
3. **Performance optimization** without major feature additions
4. **Version management** (versioning, changelogs, release notes)

---

## 🛠️ Current Release Branch Status

### **What's Included** (from Master):
✅ Login system with encrypted tokens  
✅ Screen selection and configuration  
✅ Face detection & demographics analytics  
✅ TensorFlow gender/age models  
✅ ML Kit sentiment detection  
✅ Room database persistence  
✅ Impression tracking via API  
✅ Retrofit HTTP client setup  

### **What's NOT Included** (waiting for staging):
❌ MQTT real-time ad triggering  
❌ Media playback (images/videos)  
❌ News ticker (RSS feeds)  
❌ Weather widget  
❌ CameraAnalyticsService (background mode)  
❌ Full AdvertWatchingActivity  

---

## 📋 Release Branch Activities

### **Activity 1: Test & Verify Current Features**

Before building, verify all master branch features work:

```bash
# Switch to release (you're already here)
git status
# Should show: On branch 'release'

# View recent commits
git log --oneline -5
# Should show: 7a8f360 (Merge PR #2), 16551ae (add ui features), etc.

# Check what's different from master
git diff master release
# Should show: Empty (or only version bumps)
```

### **Activity 2: Add Version/Release Information**

Update app metadata:

```gradle
// app/build.gradle
android {
    defaultConfig {
        versionCode 1        // Increment: 2, 3, 4...
        versionName "1.0"    // Use semantic versioning: 1.0.0, 1.0.1...
    }
}
```

### **Activity 3: Generate Signed APK**

Once testing passes:

```bash
# Create release keystore (one-time)
keytool -genkey -v -keystore release-key.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias release

# Configure signing in build.gradle
android {
    signingConfigs {
        release {
            storeFile file('release-key.jks')
            storePassword 'your_password'
            keyAlias 'release'
            keyPassword 'your_password'
        }
    }
    buildTypes {
        release {
            signingConfig signingConfigs.release
            minifyEnabled true
            proguardFiles getDefaultProguardFile('proguard-android.txt'), 'proguard-rules.pro'
        }
    }
}

# Build signed release APK
./gradlew assembleRelease

# Output: app/build/outputs/apk/release/app-release.apk (~8-12 MB)
```

### **Activity 4: Create Release Tag**

Mark the release point in Git:

```bash
git tag -a v1.0 -m "Release version 1.0 - Master branch features"
git push origin v1.0

# Future releases:
git tag -a v1.0.1 -m "Hotfix: Fix login validation"
git tag -a v1.1.0 -m "Minor update: UI improvements"
```

---

## 🚀 Deployment Checklist

Before publishing app-release.apk:

- [ ] **App signature**: Verified with `jarsigner -verify app-release.apk`
- [ ] **ProGuard obfuscation**: Enabled for code protection
- [ ] **Manifest permissions**: Reviewed and necessary
- [ ] **Dependencies**: All resolved without conflicts
- [ ] **Version name**: Incremented in build.gradle
- [ ] **Release notes**: Created in RELEASE_NOTES.md
- [ ] **Changelog**: Updated with features/fixes
- [ ] **Testing report**: All features verified on test devices
- [ ] **Device compatibility**: Tested on API 24+ devices

---

## 📝 Release Notes Template

Create `RELEASE_NOTES.md`:

```markdown
# Adjaba Player v1.0 - Release Notes

**Release Date**: April 15, 2026  
**Branch**: release  
**Based on**: master (commit 7a8f360)

## New Features
- User authentication with PIN/Screen ID
- Device-agnostic screen selection
- Real-time face detection and demographic analytics
- Gender classification (Male/Female)
- Age group estimation (20, 32, 40, 50, 50+)
- Sentiment analysis via ML Kit smile detection
- Impression tracking and storage

## Bug Fixes
- Fixed UI alignment issues from PR #2
- Improved permission handling for Camera access
- Enhanced error handling for API failures
- Fixed SharedPreferences encryption

## Technical Details
- **Min SDK**: Android 8.0 (API 24)
- **Target SDK**: Android 14 (API 34)
- **Architecture**: ARM 32-bit and 64-bit
- **Dependencies**: Retrofit 2, Room, ML Kit, TensorFlow Lite

## Installation
1. Download `app-release.apk`
2. Enable "Unknown sources" in device settings
3. Install APK
4. Grant permissions when prompted (Camera, Storage)
5. Login with Screen ID and PIN

## Known Limitations
- Playlist playback not available in this release
- MQTT real-time ad triggering pending in staging branch
- News and weather widgets require staging branch features

## Next Steps
- Merge staging branch for full media playback capabilities
- Implement MQTT-based dynamic ad switching
- Add news ticker and weather widget support
- Performance optimization for large-scale deployments

## Support
Contact: [support email]
Issues: [GitHub issues link]
```

---

## 🔄 Release vs Staging Decision Matrix

| Scenario | Use Master/Release | Use Staging |
|----------|-----------|---------|
| **Analytics-only deployment** | ✅ | ❌ |
| **Retail signage with ads** | ❌ | ✅ |
| **Testing face detection** | ✅ | ✅ |
| **Stable, proven build** | ✅ | ❌ |
| **Need playlist playback** | ❌ | ✅ |
| **Need MQTT integration** | ❌ | ✅ |
| **Need news/weather widgets** | ❌ | ✅ |
| **Production deployment (beta)** | ✅ | ⚠️ (test first) |

---

## 🔀 Branching Strategy

### **Current Branch Structure**

```
                      release
                         ↑
                         │
master (main stable) ────┘
         ↑
         │
       origin/master (GitHub)


                      staging (experimental)
                         ↑
                         │
     milestone1 ─────────┘
         ↑
         │
    origin/staging (GitHub)
```

### **Recommended Git Workflow**

1. **Create feature branch from release** (for hotfixes):
   ```bash
   git checkout release
   git checkout -b hotfix/login-validation
   # Make changes
   git commit -m "hotfix: Add input validation"
   git push origin hotfix/login-validation
   # Create Pull Request → merge back to release
   ```

2. **Backport critical fixes** from staging to release:
   ```bash
   git checkout release
   git cherry-pick [commit-hash-from-staging]
   ```

3. **Track major releases** with tags:
   ```bash
   git tag -a v1.0.0 -m "Initial release"
   git tag -a v1.0.1 -m "Security patch"
   git tag -a v1.1.0 -m "Minor feature addition"
   ```

---

## 🔐 Security Considerations for Release

### **Before Publishing**

1. **Review AndroidManifest.xml**:
   ```xml
   <!-- Ensure this is set -->
   android:usesCleartextTraffic="false"  <!-- Only HTTPS -->
   
   <!-- Remove debug flags -->
   android:debuggable="false"
   ```

2. **Check for hardcoded credentials**:
   ```bash
   grep -r "password\|api_key\|token" app/src/main/java/
   # Should return: only Config.java with URLs (not secrets)
   ```

3. **Enable ProGuard obfuscation**:
   ```gradle
   buildTypes {
       release {
           minifyEnabled true
           shrinkResources true
       }
   }
   ```

4. **Test secure storage**:
   - Verify EncryptedSharedPreferences is used for token storage
   - Confirm AES256-GCM encryption enabled

### **After Publishing**

- Monitor crash reports via Firebase/Crashlytics
- Track API errors for suspicious patterns
- Update security policies if vulnerabilities discovered
- Plan patch releases for critical issues

---

## 📊 Release Metrics

Track these metrics for each release:

```
Release v1.0
├─ Build Size: ~12 MB
├─ Installation Size: ~25 MB (with models)
├─ Supported Devices: Android 8.0+
├─ Test Coverage: [XX%]
├─ Performance (avg. startup): [X seconds]
├─ Crash Rate: [X%]
├─ User Rating: [X/5 stars]
└─ Known Issues: [X critical, Y non-critical]
```

---

## 🎯 Next Release Planning

### **v1.0.1 (Hotfix)**
- [ ] Fix login validation edge cases
- [ ] Improve permission request UX
- [ ] Add API timeout handling

### **v1.1.0 (Minor Update)**
- [ ] Add offline mode detection
- [ ] Improve camera performance
- [ ] Add language support (Arabic, English)

### **v2.0.0 (Major Merge with Staging)**
- [ ] Merge staging features (media playback)
- [ ] Add MQTT integration
- [ ] Include news and weather widgets
- [ ] Implement CameraAnalyticsService

---

## 📚 Release Documentation

### **Files to Maintain**

1. **RELEASE_NOTES.md** - What changed
2. **CHANGELOG.md** - Historical versions
3. **INSTALLATION.md** - How to install
4. **TROUBLESHOOTING.md** - Common issues
5. **API_DOCUMENTATION.md** - Endpoint specs

### **Example Changelog Entry**

```markdown
## [1.0.0] - 2026-04-15

### Added
- User authentication with encrypted token storage
- Face detection with demographic analysis
- Impression tracking database
- Permission management (Dexter)

### Fixed
- UI alignment issues from previous PR
- API error handling improvements

### Changed
- Updated targetSdkVersion to 34
- Improved SharedPreferences encryption

### Removed
- Legacy camera preview activity (TestCamera is backup)

### Security
- Enabled cleartext traffic prevention
- AES256-GCM encryption for tokens
```

---

## ✅ Release Readiness Checklist

Before building app-release.apk:

**Code Quality**
- [ ] No compilation errors
- [ ] No critical lint warnings
- [ ] Code reviewed for security
- [ ] Dependencies updated (no vulnerabilities)

**Testing**
- [ ] Unit tests pass
- [ ] Integration tests pass
- [ ] Manual testing on multiple devices
- [ ] Network error scenarios tested

**Documentation**
- [ ] README updated
- [ ] API documentation complete
- [ ] Installation instructions clear
- [ ] Release notes written

**Deployment**
- [ ] Version number incremented
- [ ] Git tag created
- [ ] Signed APK generated
- [ ] Upload to testing platform

**Monitoring**
- [ ] Crash reporting configured
- [ ] Analytics enabled
- [ ] Server-side logging ready
- [ ] Alert system configured

---

## 🚀 Quick Start for Release Build

```bash
# 1. Switch to release branch
git checkout release

# 2. Verify you're on the right branch
git status

# 3. Update version in build.gradle
nano app/build.gradle
# Change: versionCode 1 → versionCode 2
# Change: versionName "1.0" → versionName "1.0.1"

# 4. Commit version change
git add app/build.gradle
git commit -m "chore: bump version to 1.0.1"

# 5. Create release tag
git tag -a v1.0.1 -m "Release v1.0.1 - Hotfix"

# 6. Build signed APK (requires keytool setup first)
./gradlew clean
./gradlew assembleRelease

# 7. Find your APK
# Location: app/build/outputs/apk/release/app-release.apk

# 8. Verify signature
jarsigner -verify app/build/outputs/apk/release/app-release.apk

# 9. Push to GitHub
git push origin release
git push origin v1.0.1
```

---

## 📞 Support & Escalation

**For questions about**:
- **Release process**: Check this document
- **Building APK**: See "Quick Start for Release Build"
- **Features**: Refer to APP_FLOW_SIMULATION.md
- **Branch differences**: See BRANCH_COMPARISON.md
- **Code issues**: Review source code comments

---

**Last Updated**: April 15, 2026  
**Release Branch Version**: Based on master (7a8f360)  
**Status**: Ready for testing and deployment  
**Next Major Release**: v2.0.0 (with staging merge)

