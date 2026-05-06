# Comprehensive Testing Workflow

## A: FULL PLAY BUTTON WORKFLOW TEST
### Step 1: Select Screen ID
```
Click spinnerID dropdown
Select first available screen
```

### Step 2: Select Orientation  
```
Click spinner1 dropdown
Select "Landscape"
```

### Step 3: Click PLAY Button
```
Click loginbtn (PLAY button)
```

### Step 4: Monitor Results
```
Expected:
- Log: "getAds() called"
- Log: "API Response received" 
- Log: "Starting media download"
- Activity transition to AdvertLandWatch or AdvertPressPlay
- Media starts playing
```

---

## C: SPECIFIC FEATURES TEST

### Feature 1: Ad Playback
- Monitor: AdvertLandWatch activity
- Check: Video/image display
- Check: Media rotation timing
- Check: Audio playback (if applicable)

### Feature 2: Media Rotation
- Check: Images/videos switching at correct intervals
- Check: Correct duration display
- Check: Smooth transitions

### Feature 3: Weather Display
- Check: Weather widget rendering
- Check: Temperature display
- Check: Weather icon loading
- Check: Auto-refresh every 15 minutes

### Feature 4: News Ticker
- Check: News items displaying
- Check: News rotation
- Check: Image loading
- Check: Auto-refresh every 15 minutes

### Feature 5: QR Code Generation  
- Check: QR code rendering
- Check: Correct data encoding
- Check: Visibility toggle

### Feature 6: Target Hours Filtering
- Check: Ads filtered by current hour
- Check: Business rules enforcement
- Check: Correct ad selection

### Feature 7: Display Text
- Check: Text rendering if enabled
- Check: Text positioning
- Check: Font sizing

---

## Expected Logs During Each Phase

### Phase 1: App Startup
```
ActivityThread: handleBindApplication()
ProfileInstaller: Installing profile
SelectScreens: onCreate()
getIDs() starting
API call to fetch screens
Spinners populated
```

### Phase 2: Play Button Click
```
Play button clicked
DataHolder values set
getAds() called
API call for ads
Response received with ad list
Media download starting
```

### Phase 3: Playback Startup
```
ExoPlayer created
Media items queued
Playback started
First media displaying
```

### Phase 4: Feature Tests
```
Media rotation: Next media loaded
Weather: API refresh scheduled
News: RSS feed loading
QR: Bitmap generated
```

---

## Error Scenarios to Monitor

1. **API Errors**
   - No internet connection
   - Invalid credentials
   - Server errors (4xx, 5xx)
   
2. **Media Errors**
   - File not found
   - Invalid format
   - Download timeout
   
3. **Runtime Errors**
   - NullPointerException on DataHolder
   - Missing layout resources
   - View initialization failures

4. **Permission Errors**
   - Network access denied
   - Storage access denied
   - Camera access denied (for QR)

---

## Data Points to Verify

### UI State
- [ ] Screen ID spinner populated
- [ ] Orientation spinner set to default
- [ ] Checkboxes initialized
- [ ] Play button enabled

### After Play Click
- [ ] Dialog/loader shown
- [ ] Screen dims/freezes
- [ ] Activity transitions
- [ ] New activity displays

### During Playback
- [ ] Media displays
- [ ] Audio plays
- [ ] Controls responsive
- [ ] Rotation works

### After First Media Completes
- [ ] Next media loads  
- [ ] Weather displays
- [ ] News displays
- [ ] No interruptions

---

## Termination Conditions

### Success
- Full workflow completes without crashes
- All specific features work as expected
- No error logs in logcat

### Failure
- App crashes at any point
- API returns error
- Media fails to load
- Features display incorrectly

---

## Log Capture Strategy

1. Clear logcat: `adb logcat -c`
2. Start test sequence
3. Capture full logs: `adb logcat -d > logs.txt`
4. Analyze for errors and API responses


