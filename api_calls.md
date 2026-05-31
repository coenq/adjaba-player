# 📡 Adjaba Player — API Calls Reference

> **Base URL:** `https://api.adjaba.in`  
> **Weather Base URL:** `https://api.weatherapi.com`  
> **Source of truth:** `ApiCalls.java` interface + `Config.java` + `APIImpression.java` + `ImpressionRetryWorker.java`

---

## 🔐 Authentication

### 1. Login (Email / Password)

| Field         | Value                       |
|---------------|-----------------------------|
| **Method**    | `POST`                      |
| **Endpoint**  | `/v2/authenticate_user`     |
| **Auth**      | None                        |
| **Called by** | `LoginActivity.startLogin()` |

**Request Body**
```json
{
  "userId": "user@example.com",
  "password": "yourPassword"
}
```

**Response** (`LoginResponse`)
```json
{
  "message": "Login successful",
  "userid": "user@example.com",
  "email": "user@example.com",
  "loginToken": "eyJhbGciOiJIUzI1NiIs..."
}
```

**On success:** Token saved via `AuthManager.saveToken()` + `AuthManager.saveTokenTimestamp()`.  
**On failure:** Error toast shown; login button re-enabled.

---

### 2. TV Code Auth — Start

| Field         | Value                           |
|---------------|---------------------------------|
| **Method**    | `POST`                          |
| **Endpoint**  | `/tv/start-auth`                |
| **Auth**      | None                            |
| **Called by** | `TvLoginActivity`               |

**Request Body:** *(none)*

**Response** (`TvStartAuthResponse`)
```json
{
  "device_code": "DEVICE_CODE_STRING",
  "user_code": "ABC-123",
  "qr_url": "https://adjaba.in/auth?code=ABC-123",
  "expires_in": 600,
  "poll_interval": 5
}
```

**Flow:** The device displays the `user_code` and/or QR code to the user; the app then polls `/tv/poll-auth/{deviceCode}` at the returned `poll_interval` (seconds) until a token is issued or the code expires.

---

### 3. TV Code Auth — Poll

| Field         | Value                                    |
|---------------|------------------------------------------|
| **Method**    | `GET`                                    |
| **Endpoint**  | `/tv/poll-auth/{deviceCode}`             |
| **Auth**      | None                                     |
| **Called by** | `TvLoginActivity` (polling loop)         |

**Path Parameter**

| Param        | Type   | Description                          |
|--------------|--------|--------------------------------------|
| `deviceCode` | String | Device code from `tvStartAuth` response |

**Response** (`TvPollAuthResponse`)
```json
{
  "status": "AUTHORIZED",
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "user": {
    "userId": "user@example.com",
    "email": "user@example.com",
    "userType": "ADVERTISER",
    "isPartner": false,
    "isAdmin": false,
    "businessName": "Acme Corp"
  }
}
```

> Possible `status` values: `"PENDING"` (keep polling) · `"AUTHORIZED"` (save token) · `"EXPIRED"`

---

### 4. Background Re-authentication (Silent)

| Field         | Value                                       |
|---------------|---------------------------------------------|
| **Method**    | `POST` (raw `HttpURLConnection`)            |
| **Endpoint**  | `/v2/authenticate_user`                     |
| **Auth**      | None (uses stored credentials)              |
| **Called by** | `AuthManager.reAuthenticateSync()`, `ImpressionRetryWorker` |

Used automatically when a `401` response is received or when the stored token is older than 6 days. Must be called on a background thread.

---

## 📺 Screens

### 5. Get Screens by User

| Field         | Value                              |
|---------------|------------------------------------|
| **Method**    | `GET`                              |
| **Endpoint**  | `/get_screen_by_user`              |
| **Auth**      | `Bearer {token}`                   |
| **Called by** | `SelectScreens.getIDs()`           |

**Response** (`List<Root>`)
```json
[
  {
    "screenId": "Demo136",
    "screenName": "Main Lobby Screen",
    "userId": "user@example.com",
    "location": "New York",
    "orientation": "LANDSCAPE",
    "screenDevice": "Android TV",
    "screenPlayer": "Adjaba Player",
    "screenWidth": 1920,
    "screenHeight": 1080,
    "screenSizeInch": 55,
    "screenBalance": 0,
    "dailyView": 0,
    "currency": "USD",
    "latitude": 40.7128,
    "longitude": -74.0060,
    "peakHours": [9, 10, 11, 14, 15, 16],
    "locationType": ["MALL", "RETAIL"],
    "screenTags": ["electronics", "fashion"],
    "imageList": [],
    "aboutScreen": "",
    "ageDomintaion": null,
    "genderDomination": null,
    "logoUrl": null,
    "customLogoUrl": null,
    "defaultFooter": null,
    "nearbyLocation": null,
    "dateCreated": "2024-01-15T10:00:00Z"
  }
]
```

---

## 📋 Playlists & Ads

### 6. Get Ads / Playlist for Screen

| Field         | Value                                          |
|---------------|------------------------------------------------|
| **Method**    | `GET`                                          |
| **Endpoint**  | `/get_screen_playlists/{screenId}`             |
| **Auth**      | `Bearer {token}`                               |
| **Called by** | `SelectScreens.getAds()`, `AdvertWatching.getAds()`, `AdvertLandWatch.getAds()` |

**Path Parameter**

| Param      | Type   | Description          |
|------------|--------|----------------------|
| `screenId` | String | Target screen ID     |

**Response** (`List<WatchingModel>`)
```json
[
  {
    "screenId": "Demo136",
    "contractId": "contract123",
    "adPlaying": false,
    "budget": 500,
    "currency": "USD",
    "duration": 15,
    "maxBid": 10,
    "orientation": "LANDSCAPE",
    "isActive": true,
    "excludeTags": [],
    "minScreenSizeInch": null,
    "resolution": null,
    "adContractData": {
      "advertId": "ad456",
      "format": "video",
      "videoUrl": "upload/boss/1776468466384_ad.mp4",
      "startDate": "2025-01-01",
      "endDate": "2025-12-31",
      "textTop": "Ad Title",
      "textBottom": "www.example.com",
      "textLeft": "",
      "textRight": "",
      "targeturl": "https://example.com",
      "targetHours": [9, 10, 11, 14, 15, 16],
      "targetAgeGroup": ["18-35"],
      "targetDevice": ["TV"],
      "targetEvent": [],
      "targetGender": ["ALL"],
      "targetLocationType": null,
      "targetTags": ["electronics"],
      "dateCreated": "2024-06-01T00:00:00Z"
    }
  }
]
```

**Error handling:**
- `401` → `AuthManager.reAuthenticateSync()` is called once, then `getAds()` retried
- Other errors → falls back to locally cached ads from Room database (`AdDatabase`)

---

### 7. Get Media URL (Presigned)

| Field         | Value                                            |
|---------------|--------------------------------------------------|
| **Method**    | `GET`                                            |
| **Endpoint**  | `/media/{path}`                                  |
| **Auth**      | `Bearer {token}`                                 |
| **Called by** | `SelectScreens.getUrl()`, `AdvertWatching.getUrl()`, `AdvertLandWatch.getUrl()` |

**Path Parameter**

| Param  | Type   | Description                                          |
|--------|--------|------------------------------------------------------|
| `path` | String | Relative media path from `adContractData.videoUrl` (e.g. `upload/boss/file.mp4`) |

**Response** (`VideoImageModel`)
```json
{
  "status": "SUCCESS",
  "url": "https://eu2.contabostorage.com/adjaba/upload/boss/file.mp4?signature=..."
}
```

**On success:** Media is downloaded to internal storage (`context.getFilesDir()`) and cached in Room (`AdEntity`).

---

### 8. Get Adverts by User

| Field         | Value                              |
|---------------|------------------------------------|
| **Method**    | `GET`                              |
| **Endpoint**  | `/get_advert_by_user`              |
| **Auth**      | `Bearer {token}`                   |
| **Called by** | `ApiCalls.getAdvertResponse()`     |

**Response** (`List<GetAdvertsResponse>`) — full advert list for the authenticated user.

---

## 📊 Impressions

### 9. Create Impression (Primary — Fire-and-forget)

| Field         | Value                                 |
|---------------|---------------------------------------|
| **Method**    | `POST` (raw `HttpURLConnection`)      |
| **Endpoint**  | `/create_impression`                  |
| **Auth**      | `Bearer {token}`                      |
| **Called by** | `APIImpression.sendImpression()`      |

**Request Body**
```json
{
  "impressionId": "uuid-v4-string",
  "advertId": "ad456",
  "contractId": "contract123",
  "screenId": "Demo136",
  "playTimeStamp": "2025-05-19T14:30:00Z",
  "playSec": 15,
  "duration": 15,
  "impressioncost": 0,
  "isactivecontract": 0,
  "amountSettled": false,
  "currency": "USD",
  "dayHour": 14,
  "format": "video",
  "locationType": "MALL",
  "maxBid": 10.0,
  "orientation": "LANDSCAPE",
  "screenDevice": "Android TV",
  "screenPlayer": "Adjaba Player",
  "tags": ["electronics"],
  "female20": 0,
  "female32": 0,
  "female40": 0,
  "female50": 0,
  "female50plus": 0,
  "male20": 0,
  "male32": 0,
  "male40": 0,
  "male50": 0,
  "male50plus": 0,
  "objectdetected": "",
  "textdetected": "",
  "totalview": 0
}
```

**On HTTP 200:** Impression deleted from local Room DB (`impDao().deleteAdById()`).  
**On failure:** Record stays in Room DB; retried by `ImpressionRetryWorker`.

---

### 10. Create Impression (Retry Worker — Background)

| Field         | Value                                     |
|---------------|-------------------------------------------|
| **Method**    | `POST` (raw `HttpURLConnection`)          |
| **Endpoint**  | `/create_impression`                      |
| **Auth**      | `Bearer {token}`                          |
| **Called by** | `ImpressionRetryWorker.doWork()`          |

**Request Body** *(same fields as above — sourced from `ImpressionEntity` in Room DB)*
```json
{
  "impressionId": "uuid-v4-string",
  "advertId": "ad456",
  "contractId": "contract123",
  "screenId": "Demo136",
  "playTimeStamp": "2025-05-19T14:30:00Z",
  "playSec": 15,
  "impressionCost": 0,
  "type": "IMPRESSION",
  "amountSettled": false,
  "currency": "USD",
  "dayHour": 14,
  "format": "video",
  "locationType": "MALL",
  "maxBid": 10.0,
  "orientation": "LANDSCAPE",
  "screenDevice": "Android TV",
  "screenPlayer": "Adjaba Player",
  "tags": ["electronics"]
}
```

**Retry logic:**
- `200` → delete from DB, continue
- `401` on first failure → calls `AuthManager.reAuthenticateSync()` once, then retries the same impression
- Other failures → `Result.retry()` (WorkManager exponential backoff)

---

## 🌤️ Weather

### 11. Get Weather Forecast

| Field         | Value                                    |
|---------------|------------------------------------------|
| **Method**    | `GET`                                    |
| **Endpoint**  | `https://api.weatherapi.com/v1/forecast.json` |
| **Auth**      | API key via query param                  |
| **Called by** | `AdvertWatching`, `AdvertLandWatch`      |

**Query Parameters**

| Param | Type   | Description                        |
|-------|--------|------------------------------------|
| `key` | String | Weather API key (`BuildConfig.WEATHER_API_KEY`) |
| `q`   | String | City name or lat/long coordinates  |

**Response** (`WeatherModel`) — standard WeatherAPI.com `forecast.json` response including current conditions + hourly forecast (`Forecastday` / `Hour` models).

---

## 🔑 Token Management Summary

| Scenario                     | Handler                                   |
|------------------------------|-------------------------------------------|
| Login                        | `LoginActivity` → `AuthManager.saveToken()` |
| TV Code login                | `TvLoginActivity` → `AuthManager.saveToken()` |
| 401 on API call (UI thread)  | Re-authenticate on background thread, retry once |
| 401 in Worker (background)   | `AuthManager.reAuthenticateSync()`, retry once |
| Token near expiry (> 6 days) | `App.onCreate()` proactive refresh         |
| Token storage                | Encrypted: `EncryptedSharedPreferences` (`AES256_GCM`) |
| Timeouts                     | `connectTimeout = 15s`, `readTimeout = 15s` |

---

## 📡 Real-Time Demographics (MQTT)

### 12. Subscribe to Demographic Data Stream

| Field         | Value                                    |
|---------------|------------------------------------------|
| **Protocol**  | MQTT v3.1.1 over TLS                     |
| **Broker**    | `ssl://api.adjaba.in:8883`               |
| **Username**  | `adjaba-mqtt-2026`                       |
| **Password**  | `Adjaba@1234`                           |
| **Topic**     | `store/{screenId}`                       |
| **QoS**       | 1 (at-least-once delivery)               |
| **Retained**  | ❌ No                                    |
| **Called by** | `AdvertWatching`, `AdvertLandWatch` (if enabled) |
| **Note**      | Port 1883 (plaintext) blocked on EMQX — TLS only |

**Connection Options**

| Option | Value |
|--------|-------|
| Clean Session | `true` |
| Auto-reconnect | `true` |
| Connection Timeout | 30 seconds |
| Keep-Alive Interval | 60 seconds |

**Message Format** (UTF-8 JSON, published by OnlyCamera analytics app)

```json
{
  "storeId": "Demo136",
  "timestamp": 1716134400000,
  "customerCount": 7,
  "ageRange": "(20, 32)",
  "gender": "M",
  "happy": 3,
  "neutral": 2,
  "sad": 0,
  "angry": 1,
  "fear": 0,
  "surprise": 1,
  "disgust": 0,
  "avgDwellSec": 14
}
```

**Field Reference**

| Field | Type | Description |
|-------|------|-------------|
| `storeId` | String | Screen/store identifier (same as `screenId`) |
| `timestamp` | Long (ms) | Message creation time |
| `customerCount` | Int | Total unique faces seen (male + female) |
| `ageRange` | String | Age bucket: `"(0, 20)"`, `"(20, 32)"`, `"(32, 43)"`, `"(43, 53)"`, `"(53, 100)"` |
| `gender` | String | `"M"` or `"F"` (most recently detected face) |
| `happy` | Int | Cumulative happy emotion count |
| `neutral` | Int | Cumulative neutral emotion count |
| `sad` | Int | Cumulative sad emotion count |
| `angry` | Int | Cumulative angry emotion count |
| `fear` | Int | Cumulative fear emotion count |
| `surprise` | Int | Cumulative surprise emotion count |
| `disgust` | Int | Cumulative disgust emotion count |
| `avgDwellSec` | Int | Average seconds viewers have been in frame |

**Publish Frequency**: Once every 10 seconds (when at least one face is detected).

**Ad Selection Logic**

When valid demographic data is received (`customerCount > 0`):
1. **Score each ad** in current playlist by:
   - Target hour match: +10 points
   - Age range overlap: +5 points
   - Happy emotion (high engagement): +3 points
2. **Select highest-scoring ad** and queue it for next playback slot
3. **Fallback**: If no demographic match, continue regular playlist rotation

**Enable/Disable**

MQTT demographics can be toggled via checkbox in `SelectScreens` activity:
- Checkbox ID: `mqtt_checkbox`
- Preference key: `mqtt_enabled` (default: `false`)
- Saved to `SharedPreferences` (`MyPrefs`)

**Connection Lifecycle**

| Event | Action |
|-------|--------|
| Activity `onCreate()` | Check `mqtt_enabled` pref → connect if `true` |
| MQTT message received | Parse JSON → score ads → schedule best match |
| Activity `onDestroy()` | Disconnect from broker, cleanup resources |
| Connection lost | Auto-reconnect enabled (Paho built-in retry) |

**Security Note**: TLS on port 8883 is active. A trust-all SSL factory is used in `MqttManager` to handle self-signed EMQX certificates — replace with a pinned CA trust store before final production release.

---

## 🗺️ Endpoint Quick Reference

| # | Method | Endpoint                            | Auth | Description                        |
|---|--------|-------------------------------------|------|------------------------------------|
| 1 | POST   | `/v2/authenticate_user`             | ❌   | Login with email + password        |
| 2 | POST   | `/tv/start-auth`                    | ❌   | Start TV device-code auth flow     |
| 3 | GET    | `/tv/poll-auth/{deviceCode}`        | ❌   | Poll for TV auth token             |
| 4 | POST   | `/v2/authenticate_user`             | ❌   | Background token re-auth (silent)  |
| 5 | GET    | `/get_screen_by_user`               | ✅   | Fetch all screens for user         |
| 6 | GET    | `/get_screen_playlists/{screenId}`  | ✅   | Fetch ad playlist for a screen     |
| 7 | GET    | `/media/{path}`                     | ✅   | Resolve presigned media URL        |
| 8 | GET    | `/get_advert_by_user`               | ✅   | Fetch all adverts for user         |
| 9 | POST   | `/create_impression`                | ✅   | Record ad play impression          |
|10 | POST   | `/create_impression`                | ✅   | Retry failed impressions (worker)  |
|11 | GET    | `https://api.weatherapi.com/v1/forecast.json` | Key | Weather forecast       |
|12 | MQTT   | `store/{screenId}`                  | Credentials | Real-time demographic data (from OnlyCamera) |

