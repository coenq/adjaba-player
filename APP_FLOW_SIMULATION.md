# Adjaba Player - Digital Signage App Flow & Simulation Guide

## 📱 Project Overview

**Adjaba Player** is a Digital Signage application for Android that displays advertisements, news, and weather on retail screens. The app is built on the master branch with a modern architecture using:

- **Authentication**: JWT Token-based login (EncryptedSharedPreferences)
- **API**: Retrofit 2 for REST calls to `https://api.adjaba.in`
- **Database**: Room ORM for local persistence (AdDatabase)
- **UI Framework**: AndroidX, Material Design
- **Target**: Android 8.0+ (minSdkVersion 24, targetSdkVersion 34)

---

## 🔄 Complete Application Flow

### **PHASE 1: LAUNCH & AUTHENTICATION**

```
┌─────────────────────────────────────────────────────────────┐
│ App Launch                                                   │
│ (MainActivity → LoginActivity)                               │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│ LoginActivity.java                                           │
│ ┌──────────────────────────────────────────────────────────┐│
│ │ 1. Load saved credentials from SharedPreferences         ││
│ │ 2. Display Login Form:                                   ││
│ │    - Screen ID (etEmail field)                           ││
│ │    - PIN/Password (etPassword field)                     ││
│ │    - Remember Me checkbox                                ││
│ │    - Terms & Conditions link                             ││
│ └──────────────────────────────────────────────────────────┘│
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
         ┌───────────────────────┐
         │  Validate Credentials │
         │  isValidInput()       │
         └───────────┬───────────┘
                     │
        ┌────────────┴────────────┐
        │                         │
   FAIL │                         │ PASS
        │                         │
        ▼                         ▼
    Show Error              Call API
    Toast                   Login Request
                           (POST /v2/authenticate_user)
                                 │
                                 ▼
                          ┌──────────────────┐
                          │ API Response OK? │
                          └────┬─────────┬───┘
                               │         │
                            YES│         │NO
                               ▼         ▼
                         Save Token    Show Error
                         (Encrypted)   (Connection Error)
                              │
                              ▼
                    ┌──────────────────────┐
                    │  Save Credentials    │
                    │  (if Remember Me)    │
                    └─────────┬────────────┘
                              │
                              ▼
                    ┌──────────────────────┐
                    │ Navigate to          │
                    │ SelectScreens.java   │
                    └──────────────────────┘
```

**Code Flow - LoginActivity.java (Lines 107-161)**:
```java
// User clicks LOGIN button
btnLogin.setOnClickListener(new View.OnClickListener() {
    public void onClick(View view) {
        attemptLogin(editor);  // Validates input
    }
});

// Validation
private boolean isValidInput(String userId, String password) {
    // Check: userId not empty, length >= 2
    // Check: password not empty, length >= 6
    return true; // or false with error
}

// API Call
startLogin(userId, password, apiCalls, editor) {
    LoginRequest request = new LoginRequest(userId, password);
    apiCalls.login(request).enqueue(new Callback<LoginResponse>() {
        onResponse(): 
            → Save token to EncryptedSharedPreferences
            → Save credentials if "Remember Me" checked
            → Navigate to SelectScreens
        
        onFailure(): 
            → Show "Connection Error!" Toast
            → Reset button visibility
    });
}
```

**Key Features**:
- 🔐 **Secure Storage**: Uses AES256-GCM encryption for token storage
- 💾 **Remember Me**: Optional credential persistence
- 🌐 **Offline Detection**: `isInternetAvailable()` checks before login
- ✅ **Input Validation**: Minimum length checks

---

### **PHASE 2: SCREEN SELECTION**

```
┌─────────────────────────────────────────────────────────────┐
│ SelectScreens.java                                          │
│ ┌──────────────────────────────────────────────────────────┐│
│ │ 1. Fetch User's Screens from API                         ││
│ │    GET /get_screen_by_user (with Auth token)             ││
│ │                                                          ││
│ │ 2. Parse Response → List<Root>                           ││
│ │    Each Root contains:                                   ││
│ │    - screenId, screenName                                ││
│ │    - screenDevice, screenPlayer, screenLocation         ││
│ │    - tags, deviceTags                                    ││
│ └──────────────────────────────────────────────────────────┘│
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
        ┌─────────────────────────────────┐
        │  Populate Spinners              │
        │  spinner1: Screen Mode          │
        │  spinner2: Playlist/Content     │
        │  spinnerID: Screen Selection    │
        └──────────────┬──────────────────┘
                       │
                       ▼
        ┌──────────────────────────────────┐
        │ User Selects Screen Config       │
        │ - Portrait/Landscape/Forced      │
        │ - Interval settings              │
        │ - Display text option            │
        └──────────────┬───────────────────┘
                       │
                       ▼
        ┌──────────────────────────────────┐
        │ User Clicks "PLAY" Button        │
        │ TestCamera.java launches         │
        │ (Camera analytics + preview)     │
        └──────────────────────────────────┘
```

**SelectScreens Implementation** (Lines 1-100+):
```java
public class SelectScreens extends AppCompatActivity {
    // UI Components
    Spinner spinner1;      // Screen Mode (Portrait/Landscape)
    Spinner spinner2;      // Content/Playlist
    Spinner spinnerID;     // Screen Selection
    Button playButton;     // Play button
    
    // Data Collections
    Map<String, String> screenPlayerMap;    // screenId → player name
    Map<String, String> screenLocationMap;  // screenId → location
    Map<String, List<String>> screenTags;   // screenId → tags
    
    onCreate() {
        // Request permissions (Camera, Storage, etc.)
        requestPermissions();
        
        // Fetch screens from API
        ApiCalls api = retrofitBuilder.apiCalls();
        api.getScreenResponse(authHeader)
            .enqueue(new Callback<List<Root>>() {
                onResponse():
                    → Parse Root objects
                    → Populate screenPlayerMap, screenLocationMap
                    → Set spinner adapters
                
                onFailure():
                    → Show error Toast
            });
    }
    
    // Permission handling
    private void requestPermissions() {
        Dexter.withContext(this)
            .withPermissions(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            .withListener(new MultiplePermissionsListener() {
                onPermissionsChecked():
                    if (report.areAllPermissionsGranted())
                        → Enable play functionality
                    else
                        → Show error
            })
            .check();
    }
}
```

---

### **PHASE 3: CAMERA & FACE DETECTION**

```
┌─────────────────────────────────────────────────────────────┐
│ TestCamera.java                                             │
│ (Launched from SelectScreens "PLAY" button)                 │
│ ┌──────────────────────────────────────────────────────────┐│
│ │ 1. Initialize CameraX TextureView                        ││
│ │ 2. Load ML Models:                                       ││
│ │    - gender_model.pb (TensorFlow)                        ││
│ │    - rude_carnie_age_model.pb (TensorFlow)               ││
│ │    - ML Kit Face Detection                               ││
│ │                                                          ││
│ │ 3. Start Camera Preview                                  ││
│ │ 4. Process each frame:                                   ││
│ │    → Detect faces with ML Kit                            ││
│ │    → Extract gender (TF model)                           ││
│ │    → Extract age (TF model)                              ││
│ │    → Get smile confidence (ML Kit)                       ││
│ │                                                          ││
│ │ 5. Aggregate Statistics:                                 ││
│ │    - male20, male32, male40, male50, male50plus          ││
│ │    - female20, female32, female40, female50, female50+   ││
│ │    - viewCount, playSec                                  ││
│ └──────────────────────────────────────────────────────────┘│
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
        ┌──────────────────────────────────┐
        │ FaceRecognitionProcessor         │
        │ (Processes each frame)           │
        │ processImage() → ReportEntity    │
        └──────────────────────────────────┘
                     │
                     ▼
        ┌──────────────────────────────────┐
        │ Database Storage (Room)          │
        │ ReportEntity → ReportDatabase    │
        │ ImpressionEntity → ImpressionDB  │
        └──────────────────────────────────┘
```

**TestCamera.java Components**:

```java
public class TestCamera extends AppCompatActivity {
    private CameraExecutor cameraExecutor;
    private PreviewView viewFinder;
    
    // ML Models
    private Interpreter tfLiteGenderInterpreter;    // gender_model.pb
    private Interpreter tfLiteAgeInterpreter;       // rude_carnie_age_model.pb
    private FaceDetectorOptions detectorOptions;    // ML Kit
    
    // Statistics tracking
    private ReportEntity reportEntity;
    
    onCreate() {
        // Initialize camera
        startCamera();
        
        // Load TensorFlow models
        loadGenderModel();
        loadAgeModel();
        
        // Setup ML Kit face detection
        FaceDetectorOptions options = new FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_NONE)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .build();
        
        FaceDetector detector = FaceDetection.getClient(options);
    }
    
    private void startCamera() {
        ProcessCameraProvider cameraProvider = 
            ProcessCameraProvider.getInstance(this).get();
        
        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(viewFinder.getSurfaceProvider());
        
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build();
        
        imageAnalysis.setAnalyzer(cameraExecutor, image -> {
            processFrame(image);
        });
        
        cameraProvider.bindToLifecycle(this, cameraSelector, 
            preview, imageAnalysis);
    }
    
    private void processFrame(ImageProxy image) {
        // Detect faces
        List<Face> faces = faceDetector.process(image);
        
        for (Face face : faces) {
            // Extract face region
            // Run gender model
            float genderScore = runGenderModel(faceBitmap);
            String gender = (genderScore > 0.5) ? "male" : "female";
            
            // Run age model
            float[] ageScores = runAgeModel(faceBitmap);
            int ageGroup = getAgeGroup(ageScores);
            
            // Get smile score
            float smileScore = face.getSmilingProbability();
            
            // Update reportEntity
            updateStatistics(gender, ageGroup, smileScore);
        }
        image.close();
    }
    
    private void updateStatistics(String gender, int age, float smile) {
        if ("male".equals(gender)) {
            if (age < 32) reportEntity.male20++;
            else if (age < 40) reportEntity.male32++;
            // ... etc
        } else {
            if (age < 32) reportEntity.female20++;
            // ... etc
        }
        reportEntity.viewCount++;
    }
}
```

**Face Detection Processor**:
```java
public class FaceRecognitionProcessor extends VisionProcessorBase<List<Face>> {
    
    @Override
    protected void onSuccess(List<Face> faces, 
                             FrameMetadata frameMetadata, 
                             GraphicOverlay graphicOverlay) {
        for (Face face : faces) {
            // Extract face bounds
            RectF faceBounds = new RectF(face.getBoundingBox());
            
            // Process with TF models
            float gender = model.runGenderModel(cropFace(faceBounds));
            int age = model.runAgeModel(cropFace(faceBounds));
            float smile = face.getSmilingProbability();
            
            // Draw bounding box (for visualization)
            FaceGraphic graphic = new FaceGraphic(graphicOverlay, face);
            graphicOverlay.add(graphic);
        }
    }
}
```

---

### **PHASE 4: ADVERT DISPLAY & TRACKING** ⚠️ (NOT IN MASTER)

> **Note**: The staging branch has `AdvertWatchingActivity.java` which provides:
> - Playlist loading from API
> - Image/Video playback
> - Weather & News widgets (refreshed every 15 min)
> - MQTT integration for dynamic ad switching
> 
> **Master branch** currently has **TestCamera only** (no playlist playback)

**Placeholder for Master Branch**:
```java
// Master branch continues with:
// 1. Analytics aggregation
// 2. Database persistence
// 3. Impression tracking via APIImpression
```

---

### **PHASE 5: ANALYTICS & IMPRESSION TRACKING**

```
┌─────────────────────────────────────────────────────────────┐
│ APIImpression.java                                          │
│ ┌──────────────────────────────────────────────────────────┐│
│ │ When TestCamera closes or screen is about to exit:       ││
│ │                                                          ││
│ │ 1. Retrieve ReportEntity from ReportDatabase            ││
│ │ 2. Create ImpressionEntity:                             ││
│ │    - screenId, screenViewId                              ││
│ │    - playSec (duration in seconds)                       ││
│ │    - gender/age demographic counts                       ││
│ │    - viewCount (total faces detected)                    ││
│ │    - impressionCost (calculated)                         ││
│ │                                                          ││
│ │ 3. POST to API:                                          ││
│ │    POST /create_screenview                               ││
│ │    Body: All collected analytics                         ││
│ │                                                          ││
│ │ 4. Save to local Room database                           ││
│ │    (in case network fails)                               ││
│ └──────────────────────────────────────────────────────────┘│
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
        ┌──────────────────────────────────┐
        │ ImpressionEntity Stored          │
        │ in ImpressionDatabase            │
        │                                  │
        │ Fields:                          │
        │ - screenId                       │
        │ - screenViewId (UUID)            │
        │ - male20, male32... counts       │
        │ - female20, female32... counts   │
        │ - playSec                        │
        │ - viewCount                      │
        │ - timestamp                      │
        └──────────────────────────────────┘
```

**APIImpression Implementation**:
```java
public class APIImpression {
    public static void sendImpressionData(
            ReportEntity report,
            String screenId,
            Context context) {
        
        // Create impression object
        ImpressionEntity impression = new ImpressionEntity(
            screenId,
            UUID.randomUUID().toString(),
            report.male20, report.male32, report.male40, report.male50,
            report.female20, report.female32, report.female40, report.female50,
            report.playSec,
            report.viewCount,
            calculateCost(report)
        );
        
        // Save to local DB
        ImpressionDao dao = ImpressionDatabase.getInstance(context).dao();
        dao.insert(impression);
        
        // Send to API
        ApiCalls api = retrofitBuilder.apiCalls();
        api.sendCameraData(
            "Bearer " + AuthManager.getToken(context),
            screenId,
            impression.screenViewId,
            true,  // amountSettled
            "USD",
            System.currentTimeMillis(),
            impression.playSec,
            impression.female20, impression.female32, ...
        ).enqueue(new Callback<Void>() {
            onResponse(): Log.d("Success", "Impression sent");
            onFailure(): Log.e("Error", "Failed to send");
        });
    }
}
```

---

## 📊 Database Schema

### **Room Database Entities**

```
┌──────────────────────────────────┐
│ ReportEntity                     │
├──────────────────────────────────┤
│ @PrimaryKey                      │
│ - id (int, autoincrement)        │
│                                  │
│ Demographics:                    │
│ - male20, male32, male40...      │
│ - female20, female32, female40..│
│                                  │
│ Engagement:                      │
│ - viewCount (total faces)        │
│ - playSec (duration)             │
│ - smileCount                     │
└──────────────────────────────────┘

┌──────────────────────────────────┐
│ ImpressionEntity                 │
├──────────────────────────────────┤
│ @PrimaryKey                      │
│ - id (int, autoincrement)        │
│                                  │
│ Identifiers:                     │
│ - screenId                       │
│ - screenViewId (UUID)            │
│ - impressionCost                 │
│                                  │
│ Metrics:                         │
│ - male20, male32... counts       │
│ - female20, female32... counts   │
│ - playSec                        │
│ - viewCount                      │
│ - timestamp                      │
└──────────────────────────────────┘
```

---

## 🔌 API Endpoints (Master Branch)

| Endpoint | Method | Auth | Purpose |
|----------|--------|------|---------|
| `/v2/authenticate_user` | POST | ❌ | Login (Screen ID + PIN) |
| `/get_screen_by_user` | GET | ✅ | Fetch user's screens |
| `/create_screenview` | POST | ✅ | Send analytics/impressions |

**API Base URL**: `https://api.adjaba.in`

**Example Login Request**:
```json
{
  "userId": "SCREEN_001",
  "password": "1234"
}
```

**Example Login Response**:
```json
{
  "loginToken": "eyJhbGciOiJIUzI1NiIs...",
  "success": true,
  "screenId": "SCREEN_001"
}
```

---

## 🎯 User Interaction Simulation

### **Scenario 1: First-Time Login**

```
1. User opens app → LoginActivity appears
2. User enters:
   - Screen ID: "RETAIL_STORE_01"
   - PIN: "123456"
   - ✓ Checks "Remember Me"
3. Clicks LOGIN
4. App validates input (length checks)
5. API call: POST /v2/authenticate_user
   - Success: Save token to encrypted storage
   - Failure: Show "Connection Error!"
6. Navigate to SelectScreens
```

### **Scenario 2: Screen Selection & Play**

```
1. SelectScreens loads → Fetches screens via GET /get_screen_by_user
2. User sees dropdown with screens:
   - Entrance Hall (Portrait)
   - Main Aisle (Landscape)
   - Checkout (Forced Portrait)
3. User selects:
   - Screen: "Main Aisle"
   - Mode: "Landscape"
   - Interval: "15 seconds"
4. Clicks "PLAY" → TestCamera launches
```

### **Scenario 3: Camera Running (Statistics)**

```
Time: 0:00s - Camera starts, person enters frame
        → Age: 28, Gender: Male, Smile: 80%
        → male20++, viewCount++

Time: 10:00s - Person still in view
        → Age: 28, Gender: Male
        → male20++, viewCount++

Time: 20:00s - Another person (Female, Age 35)
        → Age: 35, Gender: Female, Smile: 60%
        → female32++, viewCount++

Time: 45:00s - User clicks BACK
        → TestCamera closes
        → ReportEntity: male20=2, female32=1, viewCount=3, playSec=45
        → APIImpression sends POST /create_screenview
        → ImpressionEntity stored in database
```

---

## 🛠️ Technical Architecture

### **Dependencies (Master)**:
```gradle
// AndroidX
androidx.appcompat:appcompat:1.6.1
androidx.constraintlayout:constraintlayout:2.1.4
com.google.android.material:material:1.11.0

// Network
com.squareup.retrofit2:retrofit:2.9.0
com.squareup.retrofit2:converter-gson:2.9.0

// ML Kit & TensorFlow
com.google.mlkit:face-detection
org.tensorflow:tensorflow-lite

// Permissions
com.karumi:dexter:6.2.2

// Room Database
androidx.room:room-runtime:2.5.1
androidx.room:room-compiler:2.5.1

// Reactive
io.reactivex.rxjava2:rxjava:2.2.19
io.reactivex.rxjava2:rxandroid:2.1.1

// Security
androidx.security:security-crypto:1.1.0-alpha06
```

### **Module Structure**:
```
app/src/main/java/com/rnd/
├── activities/
│   ├── LoginActivity.java      ← Login screen
│   ├── SelectScreens.java      ← Screen selection
│   ├── TestCamera.java         ← Camera + analytics
│   └── TermsActivity.java
├── camera/
│   ├── CameraX utilities
│   └── Frame processing
├── face_detection/
│   ├── FaceRecognitionProcessor.java
│   └── ML Kit integration
├── newmodels/
│   ├── LoginRequest/Response
│   ├── Root (Screen data)
│   └── Data POJOs
├── others/
│   └── APIImpression.java      ← Analytics sender
├── room/
│   ├── ReportDatabase.java
│   ├── ImpressionDatabase.java
│   ├── ReportEntity.java
│   └── ImpressionEntity.java
├── report/
│   └── ReportDashboardActivity.java
└── utilities/
    ├── ApiCalls.java           ← Retrofit interface
    ├── Config.java             ← Constants
    ├── AuthManager.java        ← Token management
    ├── RetrofitBuilder.java    ← HTTP client
    └── MainApplication.java
```

---

## 🚀 Build & Deployment

### **Build Configuration**:
```gradle
android {
    compileSdkVersion 34
    defaultConfig {
        applicationId "com.rnd"
        minSdkVersion 24
        targetSdkVersion 34
        versionCode 1
        versionName "1.0"
    }
    buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android.txt'), 'proguard-rules.pro'
        }
    }
}
```

### **Build Command** (requires JDK):
```bash
./gradlew clean
./gradlew assembleRelease
# Output: app/build/outputs/apk/release/app-release.apk
```

### **Device Requirements**:
- **OS**: Android 8.0+ (API 24+)
- **RAM**: 2GB minimum
- **Camera**: Required for face detection
- **Storage**: ~100MB for app + models

---

## ⚠️ Known Limitations (Master Branch)

1. **No Playlist Playback**: Master doesn't have `AdvertWatchingActivity`
   - Staging has full media player (images, videos, news, weather)
   - Current master only does camera/analytics

2. **No MQTT Integration**: Real-time ad switching not available
   - Staging has MqttManager for dynamic triggers
   - Master lacks demographic-based ad selection

3. **No News/Weather Widgets**: 
   - Staging fetches BBC RSS feeds (every 15 min)
   - Staging gets weather from weatherapi.com
   - Master has no weather/news display

4. **Camera Permission Must Be Granted**:
   - Uses Dexter library for runtime permissions
   - App won't function without CAMERA permission

---

## 📋 Checklist for Testing Simulation

- [ ] **Login Test**: Email="screen1", Password="1234" (remember me checked)
- [ ] **Screen Fetch**: Verify API returns user's assigned screens
- [ ] **Camera Permission**: Allow when prompted
- [ ] **Face Detection**: Stand in front of camera, verify faces detected
- [ ] **Statistics**: Check logcat for demographic counts
- [ ] **Database**: Verify ReportEntity & ImpressionEntity stored
- [ ] **API Upload**: Confirm POST /create_screenview called on exit
- [ ] **Error Handling**: Test without internet, verify fallback behavior

---

## 🔮 Migration Path to Staging

To enable full playback features, merge staging into master:
```bash
git checkout master
git merge staging

# New features available:
# - AdvertWatchingActivity (full media player)
# - MqttManager (real-time ad triggers)
# - NewsUtils (RSS feed parsing)
# - CameraAnalyticsService (background camera)
# - Weather widget (weatherapi.com integration)
```

---

**Last Updated**: April 15, 2026  
**App Version**: 1.0  
**Branch**: master (Release ready)

