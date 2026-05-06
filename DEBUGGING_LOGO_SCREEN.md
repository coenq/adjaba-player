# 🔧 DEBUG GUIDE - Stuck on Logo Screen

## Problem: Logo screen appears but doesn't progress

This means the API call either:
1. **Failed silently** (network error)
2. **Returned 0 ads** (Demo136 not configured)
3. **Returned an error** (401, 403, 500, etc)

---

## Quick Diagnostic (Copy & Paste)

### Option A: Real-time Log View
```bash
adb logcat -c
adb logcat | grep -E "SelectScreens|AdvertWatching|ERROR|Exception|API"
# Then perform test scenario on device
# Watch for errors in real-time
```

### Option B: Capture Full Logs
```bash
adb logcat -c
# Perform test on device (wait 60 seconds)
adb logcat > full_logs.txt
# Share full_logs.txt with me
```

### Option C: Use the debug script
```bash
bash debug_ads.sh
# Perform test when prompted
# Check output for errors
```

---

## What to Look For

### ✅ SUCCESS - You'll see:
```
SelectScreens: 🎬 getAds() started - screenID: Demo136
SelectScreens: 🔗 API call - endpoint: get_screen_playlists/Demo136
SelectScreens: 📨 API response code: 200
SelectScreens: 📦 Ads received from API: 3 ads
```

### ❌ PROBLEM - You might see:
```
SelectScreens: ❌ Network error in getAds: Connection timeout
    → Network not working, check internet

SelectScreens: 📨 API response code: 401
    → Authentication failed, check token

SelectScreens: 📨 API response code: 403
    → Permission denied, user doesn't have access

SelectScreens: 📨 API response code: 404
    → Endpoint not found, check API server

SelectScreens: 📨 API response code: 500
    → Server error, check backend

SelectScreens: 📦 Ads received from API: 0 ads
    → Demo136 has no ads, configure in backend

SelectScreens: ❌ ERROR: Response body is NULL
    → API returned null, check response format
```

---

## Step-by-Step Debugging

### Step 1: Verify Internet Connection
```bash
# Can device reach API server?
adb shell ping google.com
# Should see "PING google.com ... replies"

# Can device reach your API?
adb shell ping your-api-server.com
```

### Step 2: Check Token
```bash
# Is authentication token valid?
# Check LoginActivity.java logs for token generation
# Token should be saved after successful login
```

### Step 3: Verify Screen Configuration
```bash
# Does Demo136 have ads?
# Check your backend admin panel
# Should see Demo136 with ads assigned and published

# Try with a different screen if available
```

### Step 4: API Testing
```bash
# Test API directly (from your computer):
curl -X GET \
  -H "Authorization: Bearer YOUR_TOKEN" \
  "https://your-api-server/get_screen_playlists/Demo136"

# Should return JSON array with ads
```

---

## Most Common Issues & Fixes

### Issue 1: "Network error" or Timeout
**Cause:** Can't reach API server
**Fix:**
- Check internet connection: `adb shell ping google.com`
- Verify API server is running
- Check firewall/proxy settings
- Verify API URL in Config.java is correct

### Issue 2: "API response code: 401"
**Cause:** Authentication failed
**Fix:**
- Re-login with boss:password
- Check token wasn't revoked
- Verify AuthManager is saving token correctly
- Check token format (should be "Bearer TOKEN")

### Issue 3: "Ads received from API: 0 ads"
**Cause:** Demo136 has no ads configured
**Fix:**
- Login to backend admin panel
- Navigate to Demo136 screen
- Add some ads to it
- Publish the ads
- Retry the test

### Issue 4: "Response body is NULL"
**Cause:** API returned unexpected format
**Fix:**
- Check API response format matches WatchingModel
- Verify response body isn't empty
- Check JSON structure

### Issue 5: App crashes or ANR
**Cause:** Unhandled exception
**Fix:**
- Check logcat for full stack trace
- Look for NullPointerException
- Check database errors
- Share crash log for analysis

---

## Files to Check

### For Network Issues:
- `Config.java` - API base URL
- `ApiCalls.java` - Endpoint definitions
- `RetrofitBuilder.java` - Retrofit setup

### For Auth Issues:
- `LoginActivity.java` - Login flow
- `AuthManager.java` - Token management
- Check SharedPreferences for saved token

### For Database Issues:
- `AdDatabase.java` - Database setup
- `AdDao.java` - Database queries
- Check if database is accessible

### For App Issues:
- `SelectScreens.java` - Ad loading logic
- `AdvertWatching.java` - Playback logic
- `DataHolder.java` - Data passing

---

## Capture and Share

Once you have logs, share:

1. **Log excerpt showing the issue:**
   - Copy the ERROR lines
   - Show 5 lines before and after

2. **Device information:**
   - Emulator or real device?
   - Android version?
   - Screen size?

3. **Network information:**
   - Can ping google.com? (Yes/No)
   - Can reach API server? (Yes/No)
   - Connected to WiFi or cellular?

4. **Test details:**
   - How long did logo show?
   - Did any toast messages appear?
   - Any other screens shown?

---

## Next Actions

**Do this now:**

1. **Capture fresh logs:**
   ```bash
   adb logcat -c
   adb logcat | grep -i "selectscreens\|error"
   # Run test
   # Wait 60 seconds
   # Copy all output
   ```

2. **Share with me:**
   - Paste the log output
   - Tell me what you see on screen
   - Describe how long logo shows

3. **I'll:**
   - Analyze the exact error
   - Tell you what's wrong
   - Provide specific fix

---

**Ready to debug! Share the logs and I'll diagnose exactly what's wrong!** 🔍


