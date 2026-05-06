# 🚀 LIVE DEBUG TESTING - STEP BY STEP

## Status: ✅ Logs Capturing in Background
**Log file**: `live_debug.log` (real-time capture)

---

## QUICK TEST STEPS

### 1️⃣ Launch App
- Open the Adjaba Player app on the device
- Should see SelectScreens activity

### 2️⃣ Select Configuration
- **Screen**: Select any available screen from dropdown
- **Orientation**: Choose "Landscape" or "Portrait"
- Leave other options as default

### 3️⃣ Trigger Ad Download
- Click the "Play" button
- Watch for the waiting logo animation

### 4️⃣ Monitor What Happens
- ✅ Loading bar appears → Downloads starting
- ✅ Loading bar progresses → Files downloading
- ✅ Logo animates out → All downloads complete
- ✅ Transitions to AdvertWatching → Playback starting
- ✅ Ads appear on screen OR weather+news rotate

---

## WHAT WE'RE DEBUGGING

### If Ads Download Successfully ✅
You'll see in logs:
```
🌐 Calling /media/{path} API for ad [ID]
✅ Got URL from API: https://eu2.contabostorage.com/...
💾 Saved ad [ID] locally
📊 Inserted ad to Room DB
📈 Progress: 1/2, 2/2...
🎉 ALL ADS DOWNLOADED!
🚀 Launching AdvertWatching
```

### If /media API Fails ❌
You'll see:
```
❌ API /media/{path} returned error code: 400/401/500
or
❌ Network error calling /media/{path} API
```
→ **Action**: Check API endpoint & auth token

### If S3 Download Fails ❌
You'll see:
```
❌ HTTP Error at S3: 403/404/500
```
→ **Action**: Check S3 bucket permissions & file existence

### If JSON Parse Fails ❌
You'll see (SHOULD BE FIXED NOW):
```
❌ Use JsonReader.setLenient(true)
```
→ **Status**: FIXED with lenient parser

---

## LOGS TO CAPTURE AFTER TEST

Run this command after finishing test:
```powershell
$adbPath = "C:\Users\User\AppData\Local\Android\Sdk\platform-tools\adb.exe"
& $adbPath -s R52MB18CEGR logcat -d SelectScreens:* AdvertWatching:* "*:E" > my_test_logs.txt
```

---

## SUCCESS INDICATORS

### 🟢 Full Success
- [ ] App launches
- [ ] Can select screen
- [ ] Loading bar appears & progresses
- [ ] Logs show "✅ Got URL from API"
- [ ] Logs show "💾 Saved ad locally"
- [ ] Logs show "🎉 ALL ADS DOWNLOADED!"
- [ ] Transitions to AdvertWatching
- [ ] Ads/weather/news visible on screen

### 🟡 Partial Success (Downloaded but Not Displaying)
- [ ] All download logs successful ✅
- [ ] But ads/weather/news not showing on screen ❌
- [ ] → Playback activity issue (not download)

### 🔴 Download Failure
- [ ] Loading bar appears
- [ ] But error logs show HTTP 4xx/5xx
- [ ] → API/backend issue

---

## NOW: PLEASE DO

1. Pull up the device
2. Open Adjaba Player app
3. Select a screen
4. Choose Landscape orientation  
5. Click Play button
6. Wait for download to complete (5-30 seconds typically)
7. Watch what happens next
8. Take a screenshot or tell me what you see

**I'm capturing all logs in background** - just need your action to trigger the app!

