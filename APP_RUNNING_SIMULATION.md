# 🎬 Adjaba Player - App Running Simulation

## 📱 Visual Walkthrough

This document simulates what you'll see when you run the app on the Android emulator.

---

## 🔐 SCREEN 1: Login Activity

### Layout
```
┌─────────────────────────────────┐
│                                 │
│    ADJABA PLAYER 🎥            │
│                                 │
├─────────────────────────────────┤
│                                 │
│  Screen ID:                     │
│  ┌─────────────────────────┐    │
│  │ SCREEN_001            │    │
│  └─────────────────────────┘    │
│                                 │
│  PIN/Password:                  │
│  ┌─────────────────────────┐    │
│  │ ••••••                │    │
│  └─────────────────────────┘    │
│                                 │
│  ☑ Remember Me                  │
│                                 │
│  ┌─────────────────────────┐    │
│  │     LOGIN              │    │
│  └─────────────────────────┘    │
│                                 │
│  Terms & Conditions             │
│                                 │
└─────────────────────────────────┘
```

### User Actions
```
1. Enter Screen ID: "SCREEN_001"
2. Enter PIN: "123456"
3. Check "Remember Me"
4. Tap "LOGIN"
```

### Behind the Scenes (Logs)
```
04-15 10:30:45.123  D/adjaba: Validating input...
04-15 10:30:45.234  D/adjaba: Input valid. Attempting login...
04-15 10:30:45.567  I/adjaba: API Call: POST /v2/authenticate_user
04-15 10:30:46.234  D/adjaba: Login Response: Success
04-15 10:30:46.345  D/adjaba: Saving token to EncryptedSharedPreferences
04-15 10:30:46.456  D/adjaba: Navigating to SelectScreens...
```

### Network Request
```
POST https://api.adjaba.in/v2/authenticate_user
Content-Type: application/json

{
  "userId": "SCREEN_001",
  "password": "123456"
}

Response:
{
  "success": true,
  "loginToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "screenId": "SCREEN_001"
}
```

---

## 📺 SCREEN 2: Select Screens Activity

### Layout
```
┌─────────────────────────────────┐
│ ← ADJABA PLAYER                 │
├─────────────────────────────────┤
│                                 │
│ Select Screen Configuration     │
│                                 │
│ Screen Mode:                    │
│ ┌─────────────────────────────┐ │
│ │ Portrait ▼                  │ │
│ └─────────────────────────────┘ │
│ Options: Portrait               │
│          Landscape              │
│          Force Portrait         │
│                                 │
│ Display Options:                │
│ ☑ Show Display Text             │
│ ☐ Business Rules                │
│                                 │
│ Select Screen:                  │
│ ┌─────────────────────────────┐ │
│ │ Entrance Hall - Main Floor ▼│ │
│ └─────────────────────────────┘ │
│ Options: Entrance Hall          │
│          Main Aisle             │
│          Checkout Counter       │
│          Back Store             │
│                                 │
│ Refresh Interval (minutes):     │
│ ┌─────────────────────────────┐ │
│ │ 15                          │ │
│ └─────────────────────────────┘ │
│                                 │
│         [  PLAY  ]              │
│                                 │
│ Last Login: SCREEN_001          │
│ Location: Retail Store #5       │
│                                 │
└─────────────────────────────────┘
```

### API Call (Background)
```
GET https://api.adjaba.in/get_screen_by_user
Headers:
  Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...

Response:
[
  {
    "screenId": "SCREEN_001",
    "screenName": "Entrance Hall - Main Floor",
    "screenDevice": "Samsung 55 inch",
    "screenPlayer": "Player_A",
    "screenLocation": "Main Floor",
    "tags": ["entrance", "prime_time"]
  },
  {
    "screenId": "SCREEN_002",
    "screenName": "Main Aisle",
    "screenDevice": "LG 43 inch",
    "screenPlayer": "Player_B",
    "screenLocation": "Sales Floor"
  },
  ...
]
```

### User Actions
```
1. Spinner 1: Select "Portrait"
2. Spinner 2: Select "Entrance Hall - Main Floor"
3. Interval: Keep as "15" minutes
4. Check "Show Display Text"
5. Tap "PLAY"
```

### Database State (Room)
```
Table: ReportEntity
┌─────────────────────────────────┐
│ id: 1                           │
│ screenId: SCREEN_001            │
│ playSec: 0 (just started)       │
│ male20: 0                       │
│ male32: 0                       │
│ female20: 0                     │
│ female32: 0                     │
│ viewCount: 0                    │
│ timestamp: 2026-04-15T10:31:00 │
└─────────────────────────────────┘
```

---

## 📷 SCREEN 3: TestCamera Activity (Face Detection)

### Layout
```
┌─────────────────────────────────┐
│                                 │
│    LIVE CAMERA PREVIEW          │
│  ┌─────────────────────────┐    │
│  │                         │    │
│  │   [Person's Face]       │    │
│  │   ┌─────────────┐       │    │
│  │   │ Smile: 85%  │       │    │
│  │   │ Age: 28     │       │    │
│  │   │ Gender: M   │       │    │
│  │   └─────────────┘       │    │
│  │                         │    │
│  │   [Second Person]       │    │
│  │   ┌─────────────┐       │    │
│  │   │ Smile: 45%  │       │    │
│  │   │ Age: 35     │       │    │
│  │   │ Gender: F   │       │    │
│  │   └─────────────┘       │    │
│  │                         │    │
│  └─────────────────────────┘    │
│                                 │
│ Detected: 2 faces               │
│ Playing: 00:45 seconds          │
│                                 │
│        [  BACK  ]               │
│                                 │
└─────────────────────────────────┘
```

### Real-Time Statistics Update (Every Second)
```
Time: 0s
──────────────────────────────
Total Faces Detected: 0
Duration: 0 sec
Male (20-31): 0
Male (32-39): 0
Female (20-31): 0
Female (32-39): 0

Time: 5s
──────────────────────────────
Total Faces Detected: 1
Duration: 5 sec
Male (20-31): 1  ← Person detected
Male (32-39): 0
Female (20-31): 0
Female (32-39): 0
Sentiment (Smile): 85%

Time: 15s
──────────────────────────────
Total Faces Detected: 2
Duration: 15 sec
Male (20-31): 1
Male (32-39): 0
Female (20-31): 0
Female (32-39): 1  ← Another person
Sentiment: Mixed (85%, 45%)

Time: 45s - USER PRESSES BACK
──────────────────────────────
Total Faces Detected: 2
Duration: 45 sec
Male (20-31): 1
Male (32-39): 0
Female (20-31): 0
Female (32-39): 1
→ Preparing to send to API...
```

### Processing Pipeline (Behind Scenes)
```
Frame captured
   ↓
Detect faces with ML Kit
   ↓
For each face:
   ├─ Extract region (300×300 pixels)
   ├─ Run gender_model.pb (TensorFlow)
   │  └─ Output: male (0.87) → MALE
   ├─ Run age_model.pb (TensorFlow)
   │  └─ Output: age_group (28 years) → MALE_20
   └─ Get smile confidence (ML Kit)
      └─ Output: 0.85 (85% confident smiling)
   ↓
Update ReportEntity in Room database
   ├─ male20++
   ├─ viewCount++
   └─ playSec = elapsed_time
   ↓
Display bounding box on preview
```

### Console Logs
```
04-15 10:31:15.123  D/TensorFlow: Loaded gender_model.pb
04-15 10:31:15.234  D/TensorFlow: Loaded age_model.pb
04-15 10:31:15.345  I/Camera: Starting camera preview...
04-15 10:31:16.123  D/FaceDetection: 1 face detected at (150, 100)
04-15 10:31:16.234  D/FaceDetection: Gender: Male (0.87)
04-15 10:31:16.345  D/FaceDetection: Age: 28 years → Group: 20-31
04-15 10:31:16.456  D/FaceDetection: Smile: 0.85 (85%)
04-15 10:31:16.567  D/Database: Inserted ReportEntity (male20++)
04-15 10:31:26.123  D/FaceDetection: 2 faces detected
04-15 10:31:26.234  D/FaceDetection: Face 1: Male, 28, Smile 85%
04-15 10:31:26.345  D/FaceDetection: Face 2: Female, 35, Smile 45%
04-15 10:31:26.456  D/Database: Updated ReportEntity
04-15 10:31:45.123  I/App: Back pressed. Stopping camera...
04-15 10:31:45.234  I/API: Preparing impression data...
04-15 10:31:45.345  I/API: POST /create_screenview
04-15 10:31:46.123  I/API: Response: 200 OK
04-15 10:31:46.234  D/Database: Inserted ImpressionEntity
04-15 10:31:46.345  I/App: Returning to SelectScreens...
```

### Network Request (On App Exit)
```
POST https://api.adjaba.in/create_screenview
Headers:
  Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
  Content-Type: application/x-www-form-urlencoded

Body:
screenId=SCREEN_001
screenViewId=3fa85f64-5717-4562-b3fc-2c963f66afa6
amountSettled=true
currency=USD
dayHour=1618472305000
playSec=45.0
female20=0
female32=1
female40=0
female50=0
female50plus=0
male20=1
male32=0
male40=0
male50=0
male50plus=0
format=portrait
impressionCost=2.50
locationType=retail
objectDetected=person,face
orientation=portrait
playTimeStamp=2026-04-15T10:31:45Z
screenDevice=Samsung 55 inch
screenPlayer=Player_A
tags=entrance,prime_time
textDetected=empty
viewCount=2

Response:
HTTP/1.1 200 OK
{
  "success": true,
  "message": "Impression recorded"
}
```

---

## 💾 Database State After Run

### ReportEntity
```
┌────────────────────────────────────────┐
│ id: 1                                  │
│ screenId: SCREEN_001                   │
│ male20: 1                              │
│ male32: 0                              │
│ male40: 0                              │
│ male50: 0                              │
│ male50plus: 0                          │
│ female20: 0                            │
│ female32: 1                            │
│ female40: 0                            │
│ female50: 0                            │
│ female50plus: 0                        │
│ viewCount: 2                           │
│ playSec: 45.0                          │
│ smileCount: 1                          │
│ timestamp: 2026-04-15T10:31:45        │
└────────────────────────────────────────┘
```

### ImpressionEntity
```
┌────────────────────────────────────────┐
│ id: 1                                  │
│ screenId: SCREEN_001                   │
│ screenViewId: 3fa85f64...              │
│ male20: 1                              │
│ female32: 1                            │
│ playSec: 45.0                          │
│ viewCount: 2                           │
│ impressionCost: 2.50                   │
│ timestamp: 2026-04-15T10:31:45        │
│ synced: true                           │
└────────────────────────────────────────┘
```

---

## 🔄 Complete User Flow Diagram

```
START
  ↓
[LoginActivity]
  ├─ User enters credentials
  ├─ API validates login
  ├─ Token saved (encrypted)
  └─ Navigate to SelectScreens
      ↓
[SelectScreens]
  ├─ Fetch user's screens from API
  ├─ Populate spinners
  ├─ User selects screen & mode
  └─ Click PLAY
      ↓
[TestCamera]
  ├─ Start camera preview
  ├─ Detect faces every frame
  │  ├─ Run gender TF model
  │  ├─ Run age TF model
  │  └─ Get sentiment (smile)
  ├─ Update statistics in real-time
  ├─ Display face bounding boxes
  ├─ Run for 45 seconds
  └─ User presses BACK
      ↓
[API Impression Upload]
  ├─ Collect all statistics
  ├─ POST to /create_screenview
  ├─ Server responds: 200 OK
  ├─ Save to local database
  └─ Return to SelectScreens
      ↓
[SelectScreens]
  ├─ Ready for next session
  └─ Repeat cycle...
      ↓
END (or restart from step 2)
```

---

## 🎯 Test Scenarios

### Scenario 1: Normal Face Detection
```
Step 1: Launch app
Step 2: Login with SCREEN_001 / 123456
Step 3: Select "Portrait" mode and "Entrance Hall"
Step 4: Click PLAY
Step 5: Stand in front of camera (5 seconds)
Step 6: Another person walks by (10 seconds)
Step 7: First person leaves, second stays (15 seconds)
Step 8: Both people leave (20 seconds total no detection)
Step 9: Press BACK

Expected Results:
- Face bounding boxes appear/disappear correctly
- Statistics update: 2 faces detected total
- Impression sent with correct counts
- App returns to SelectScreens
```

### Scenario 2: Poor Lighting
```
Step 1-4: Same as Scenario 1
Step 5: Face detection shows fewer detections
Step 6: Try improving lighting
Step 7: More detections appear

Expected Results:
- TensorFlow models work best with good lighting
- Low-light scenarios may miss detections
- No crashes or errors in logs
```

### Scenario 3: No Internet
```
Step 1-4: Same as Scenario 1
Step 5-9: Run camera normally
Step 10: When pressing BACK, no internet available
Step 11: API call times out

Expected Results:
- Impression saved to local database
- Log shows: "Failed to send impressions"
- App still returns to SelectScreens
- Next session will retry upload
```

---

## 🔍 Debugging Tips

### Enable Verbose Logging
```
adb shell setprop log.tag.adjaba VERBOSE
adb logcat -s "*adjaba*"
```

### Monitor Database
```
# Pull database file from emulator
adb pull /data/data/com.rnd/databases/

# View with SQLite browser
sqlite3 report_database.db "SELECT * FROM report_entity;"
```

### Check Memory Usage
```
adb shell dumpsys meminfo com.rnd
```

### Monitor Camera Frames
```
adb logcat -s "CameraX" "FaceDetection"
```

---

## ✅ Expected Behavior Checklist

After running the app, verify:

- [ ] Login screen appears and loads correctly
- [ ] Credentials validation works (shows error on invalid input)
- [ ] SelectScreens loads spinners with API data
- [ ] Camera preview starts after clicking PLAY
- [ ] Face detection bounding boxes appear
- [ ] Age/Gender/Sentiment detected for multiple faces
- [ ] Statistics update in real-time
- [ ] No crashes or ANR (Application Not Responding)
- [ ] API call succeeds when exiting camera
- [ ] Return to SelectScreens works
- [ ] Can restart the flow (login, select, play again)

---

**This simulation represents the complete running app experience. Once you have Java installed and run the app on the emulator, you'll see exactly this behavior!**

