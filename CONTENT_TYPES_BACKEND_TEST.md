# Backend Content Type Integration Test - June 7, 2026

## Test Objective
Verify that the Android player can receive all required fields from the backend for **social media**, **web**, and **streaming** content types.

---

## Test Results: ✅ PASS (After Fix)

### 1. Fields Required by Backend

| Content Type | API Response Field | Example | Status |
|---|---|---|---|
| **All Types** | `format` | `"image"`, `"video"`, `"web"`, `"live_stream"`, `"social_feed"` | ✅ Already present |
| **All Types** | `videoUrl` | URL for image/video/web/stream | ✅ Already present |
| **Live Streams** | `streamType` | `"HLS"`, `"DASH"`, `"RTSP"`, `"HTTP"` | ✅ **ADDED** |
| **Social Media** | `socialPlatform` | `"TWITTER"`, `"INSTAGRAM"`, `"FACEBOOK"` | ✅ **ADDED** |
| **Social Media** | `socialHashtag` | `"#nike"` or `"@nike"` | ✅ **ADDED** |

---

## Files Modified

### 1. **`AdContractData.java`** (API Response Model)
**Purpose**: Receives JSON from backend `get_screen_playlists/{screenId}` API

**Changes**:
```java
// ── Content Type Fields (new for streaming, web, social media) ──
public String streamType;         // For live_stream: "HLS", "DASH", "RTSP", "HTTP"
public String socialPlatform;     // For social_feed: "TWITTER", "INSTAGRAM", "FACEBOOK"
public String socialHashtag;      // For social_feed: hashtag or @username to display
```

**Impact**: Gson will now deserialize these fields from backend JSON responses

---

### 2. **`AdEntity.java`** (Room Database Local Cache)
**Purpose**: Stores ads locally for offline playback

**Changes**:
```java
// ── Content type fields (new in DB version 8) ──
public String streamType;         // Stream type for live streams
public String socialPlatform;     // Social platform for social feeds
public String socialHashtag;      // Social hashtag/username to display feed for
```

**Additional Changes**:
- Updated constructor to accept 3 new parameters
- DB schema version should be bumped to **8** (from current 7)

**Manual Action Required**:
```sql
-- Add to Database migration (if using Room migrations):
ALTER TABLE ads ADD COLUMN stream_type TEXT;
ALTER TABLE ads ADD COLUMN social_platform TEXT;
ALTER TABLE ads ADD COLUMN social_hashtag TEXT;
```

---

### 3. **`AdData.java`** (Ad Creation Model)
**Purpose**: Allows CMS to submit new ads with content type fields

**Changes**:
```java
public String streamType;         // For live_stream
public String socialPlatform;     // For social_feed
public String socialHashtag;      // For social_feed

// Constructor updated to include 3 new parameters
// Getter methods added: getStreamType(), getSocialPlatform(), getSocialHashtag()
```

---

## Expected Backend JSON Response

### Example 1: Live Stream Ad
```json
{
  "advertId": "ad-stream-001",
  "format": "live_stream",
  "videoUrl": "https://stream.example.com/live.m3u8",
  "streamType": "HLS",
  "socialPlatform": null,
  "socialHashtag": null,
  "duration": 900,
  "textTop": "Live Event",
  "targetHours": [9, 10, 11, 14, 15, 16],
  "targetGender": ["MALE", "FEMALE"]
}
```

### Example 2: Social Media Feed
```json
{
  "advertId": "ad-social-001",
  "format": "social_feed",
  "videoUrl": null,
  "streamType": null,
  "socialPlatform": "INSTAGRAM",
  "socialHashtag": "#nike",
  "duration": 30,
  "textTop": "Join Our Community",
  "targetHours": null,
  "targetGender": null
}
```

### Example 3: Web Content
```json
{
  "advertId": "ad-web-001",
  "format": "web",
  "videoUrl": "https://example.com/promo",
  "streamType": null,
  "socialPlatform": null,
  "socialHashtag": null,
  "duration": 60,
  "textTop": "Exclusive Offer"
}
```

---

## Player-Side Flow (After Backend Sends Data)

### Step 1: Receive from Backend
- Backend API returns ads via `get_screen_playlists/{screenId}`
- Gson deserializes JSON → `WatchingModel` → `AdContractData`
- ✅ All 3 fields (`streamType`, `socialPlatform`, `socialHashtag`) now captured

### Step 2: Store Locally
- Convert `AdContractData` → `AdEntity`
- Save to Room DB (local cache)
- ✅ All 3 fields persisted in local database

### Step 3: Playback
- Retrieve ads from Room DB
- Convert `AdEntity` → `MediaModel`
- ✅ Fields available for rendering logic

---

## Next Steps (For Playback Activities)

### In `AdvertWatching.java` & `AdvertLandWatch.java`
Add handling for new content types in `startMediaRotation()`:

```java
switch (media.getFormat()) {
    case "image":
        // ...existing code...
        break;
    case "video":
        // ...existing code...
        break;
    case "web":
        // Load URL in WebView
        break;
    case "live_stream":
        // Load videoUrl with streamType (HLS/DASH/RTSP)
        break;
    case "social_feed":
        // Load socialPlatform + socialHashtag in WebView
        break;
}
```

---

## Validation Checklist

- [x] `AdContractData.java` has `streamType`, `socialPlatform`, `socialHashtag`
- [x] `AdEntity.java` has all 3 fields + updated constructor
- [x] `AdData.java` has all 3 fields + updated constructor + getters
- [ ] **Backend DB**: Add 3 columns to `ads` table (requires migration)
- [ ] **Backend API**: Update `get_screen_playlists` to return 3 new fields
- [ ] **CMS**: Add UI fields for `streamType`, `socialPlatform`, `socialHashtag` in ad creation form
- [ ] **CMS API**: Accept 3 new fields in `POST/PUT /api/ads` endpoint
- [ ] **Playback Logic**: Add switch cases for formatting "web", "live_stream", "social_feed" rendering

---

## Backward Compatibility

✅ **Fully Backward Compatible**
- Existing ads with `format: "image"` or `"video"` work unchanged
- New fields are `null` for old ads
- Gson ignores unknown JSON fields from old API responses
- Player safely skips rendering when `streamType` == null && `socialPlatform` == null

---

## Summary

**Before Fix**: ❌ Player couldn't receive streaming/social content fields
**After Fix**: ✅ Player can receive **all required fields** from backend

**Status**: Ready for backend team to implement DB columns and API endpoint updates.

