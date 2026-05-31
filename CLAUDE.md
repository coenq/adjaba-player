# Project Instructions

You are an expert Android developer specializing in Android TV and Amazon Fire TV applications.

## Project Context

I am working on an **existing Android TV / Fire TV digital signage application**.
Your role is to help modify and extend the app without breaking existing functionality.

The app:

* Uses a REST API backend
* Already has an established architecture and codebase
* Is deployed in production environments

## Core Objective

Your goal is to:

* Modify UI and workflows
* Improve reliability and usability
* Integrate cleanly with existing code
* Avoid unnecessary rewrites

## Strict Constraints

* DO NOT introduce new frameworks or libraries unless explicitly requested
* DO NOT refactor large parts of the app unless necessary
* FOLLOW the existing architecture, patterns, and naming conventions
* PRESERVE backward compatibility with current features
* MINIMIZE risk of regressions

## Functional Context

The app is used for digital signage and includes:

* Media playback (images, videos, possibly web content)
* Playlist scheduling from a backend
* Device-based usage (TVs in public/commercial environments)

## UX Requirements (TV-Specific)

* Fully navigable via D-pad (remote control only)
* Clear focus states and transitions
* Optimized for large screens (10-foot experience)
* No reliance on touch interaction

## Workflow Expectations

When suggesting changes:

* First, analyze the likely structure of the existing code
* Ask for missing files or clarify assumptions if needed
* Provide incremental updates (not full rewrites)
* Show only the modified parts of code when possible
* Explain how changes integrate into the current system

## API Integration Rules

* Work with existing REST API structure
* Do not change API contracts unless explicitly requested
* Handle network failures gracefully
* Preserve existing request/response handling patterns

## Code Output Rules

* Match existing coding style and structure
* Keep changes minimal and targeted
* Include comments for any non-obvious logic
* Avoid over-engineering

## When Uncertain

If you lack context about the current implementation:

* Ask for specific files (e.g., Activity, Fragment, Adapter, ViewModel)
* Do NOT guess large architectural details

## Tasks You Can Help With

* Modifying UI layouts and navigation
* Fixing or improving focus behavior
* Adjusting workflows (e.g., playback, scheduling)
* Debugging issues in production flows
* Improving stability and error handling

Always prioritize stability, simplicity, and compatibility with the existing system.

---

## MQTT IOT Demographic-Based Ad Selection (May 2026)

### Overview
Implemented real-time demographic-based ad selection using MQTT to receive audience analytics and dynamically select ads matching viewer profile in real-time.

### Architecture

#### MQTT Broker Configuration
```
Protocol: MQTT over TLS (v3.1.1)
Broker: ssl://api.adjaba.in:8883
Username: adjaba-mqtt-2026
Password: Adjaba@1234
Topic Pattern: store/{screenId}
QoS: 1 (at-least-once delivery)
```

#### IOT JSON Message Structure
```json
{
  "version": "1.0",
  "storeId": "demo959",
  "timestamp": 1779232472751,
  "audience": {
    "count": 2,                    // Total audience size
    "maleCount": 2,
    "femaleCount": 0,
    "avgDwellSec": 8               // Average viewing time in seconds
  },
  "profile": {
    "ageRange": "(32, 43)",        // Age bracket: (0,20), (20,32), (32,43), (43,53), (53,100)
    "ageBracket": "32-42",
    "gender": "M",                 // M or F
    "dominantEmotion": "neutral",  // happy, neutral, sad, angry, fear, surprise, disgust
    "emotionScores": {
      "happy": 5,
      "neutral": 72,
      "sad": 3,
      "angry": 8,
      "fear": 2,
      "surprise": 7,
      "disgust": 3
    }
  },
  "adTarget": {
    "segment": "32_42_M_neutral",
    "tags": ["M", "32-42", "neutral"]
  }
}
```

### Implementation Files

#### 1. DemographicData Model
**Path**: `app/src/main/java/com/adjaba/models/DemographicData.java`

Contains nested classes:
- `DemographicData` - Top-level wrapper
- `Audience` - Audience metrics
- `Profile` - Viewer demographics
- `EmotionScores` - Emotion breakdown
- `AdTarget` - Target segment info

**Key Methods**:
- `isValid()` - Validates data has required fields
- Convenience getters for all nested fields

#### 2. MqttManager
**Path**: `app/src/main/java/com/adjaba/utilities/MqttManager.java`

**Responsibilities**:
- Singleton MQTT client management
- TLS/SSL connection with self-signed cert support
- Background thread for blocking connect()
- Message parsing and validation
- Listener callback pattern

**Key Features**:
- Trust-all SSL factory for self-signed certs
- Automatic reconnection
- JSON deserialization via Gson
- Graceful error handling

#### 3. Playback Activities
Both activities have identical demographic logic:

**AdvertWatching.java** (Portrait & Landscape orientations)
- Lines 505-550: `initializeMqtt()` initialization
- Lines 523-539: `onDemographicDataReceived()` callback
- Lines 561-661: `selectBestAdForDemographic()` scoring
- Lines 681-695: `parseTargetHours()` helper
- Lines 703-707: `scheduleNextAd()` implementation

**AdvertLandWatch.java** (Forced Portrait orientation)
- Lines 430-475: `initializeMqtt()` initialization
- Lines 448-464: `onDemographicDataReceived()` callback
- Lines 486-586: `selectBestAdForDemographic()` scoring
- Lines 602-616: `parseTargetHours()` helper
- Lines 624-628: `scheduleNextAd()` implementation

#### 4. UI Integration
**SelectScreens.java** (Login Screen)
- Lines 236-246: IOT checkbox in layout
- Line 305: Preference restoration (default: false)
- Line 371: Preference saving on PLAY

**Layout Files**:
- `activity_select_screen.xml` - Portrait layout with IOT checkbox
- `layout-land/activity_select_screen.xml` - Landscape layout with IOT checkbox

### Ad Selection Algorithm

#### Scoring System (Cumulative Points)

1. **⏰ Hour Matching** (+15 pts)
   - Matches current hour against ad's `targetHours` field
   - Use case: Morning vs. evening content

2. **👤 Gender Detection** (+10 pts)
   - Recognizes viewer gender from demographic data
   - Bonus for gender field presence

3. **🎯 Age Bracket Matching** (+12 pts)
   - Matches viewer's `ageBracket` with ad targeting
   - Brackets: 20-35, 32-45, 43+
   - Adjusted scoring per bracket

4. **😊 Emotion-Based Selection** (Variable)
   - **Happy** (+20 pts): Promotional/upbeat content
   - **Neutral** (+10 pts): Brand/information content
   - **Sad/Angry** (+2 pts): Comfort/gentle content
   - **Happiness Threshold** (+15 bonus): If happy score > 60%

5. **⏳ Engagement Time Matching** (Variable)
   - **High dwell** (>15s, +8 pts): Long-form ads
   - **Quick engagement** (≤5s, +5 pts): Short ads
   - Adapts ad length to viewing behavior

6. **👥 Audience Size Bonus** (+5 pts)
   - Multi-person audiences (≥3 people)
   - Triggers group-appeal content

#### Selection Logic
```
1. Filter out weather/news slides
2. Score ALL remaining ads using algorithm
3. Select ad with HIGHEST total score
4. Log scoring breakdown for debugging
5. Queue selected ad to play next
```

### Enabling/Disabling Feature

**User-Facing Toggle**: SelectScreens activity checkbox
- Label: "IOT"
- Default: **Unchecked (disabled)**
- Persistence: SharedPreferences key `iot_enabled`

**Programmatic Check**:
```java
boolean iotEnabled = prefs.getBoolean("iot_enabled", false);
if (iotEnabled) {
    MqttManager.getInstance().connect(context, screenId, listener);
}
```

### Logging & Debugging

**Key Log Tags**: `AdvertWatching`, `AdvertLandWatch`, `MqttManager`

**Sample Output**:
```
🎬 DEMOGRAPHIC MATCH WINNER: ad-12345 (score: 87)
   Viewer: M (32, 43) | Emotion: neutral | Audience: 2
   ⏰ Hour match: 14
   👤 Gender detected: M
   🎯 Age bracket 32-45 match
   😐 Neutral emotion: Select brand/info ads
   ⌛ High engagement time: Select longer ads
   👥 Multi-person audience: Select group-appeal ads
```

### Security Considerations

**Current Implementation**:
- Trust-all X509TrustManager for self-signed certs
- Suitable for dev/test environments

**Production Requirements**:
- Replace with CA-pinned certificates
- Import EMQX broker CA into Android KeyStore
- Never use trust-all in production

### Testing Checklist

- [ ] MQTT checkbox appears in SelectScreens (portrait & landscape)  
- [ ] Checkbox persists state across app restart
- [ ] Disabling IOT falls back to normal rotation
- [ ] MQTT connects when enabled
- [ ] Demographic JSON parsed correctly
- [ ] Ad scoring produces expected winner
- [ ] All 3 orientations behave identically
- [ ] Logging shows detailed scoring breakdown

### Future Enhancements

1. **Gender-Based Ad Targeting**: Extend MediaModel with `targetGender` field
2. **Emotion Categories**: Map emotions to content types (happy→promo, sad→support, etc.)
3. **ML-Based Scoring**: Replace weighted scoring with trained model
4. **A/B Testing**: Compare demographic vs. random selection performance
5. **Analytics**: Send impression metrics back to backend
6. **Facial Recognition Integration**: Direct video analytics → ad selection
7. **Advanced Queue Management**: Insert demographic match into rotation intelligently
