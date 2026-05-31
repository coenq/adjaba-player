# IOT Ad Contract Data Specification
**For Backend API & React Admin Interface**

## Overview
This document specifies the complete data contract required for IOT demographic-based ad targeting. These fields enable real-time ad selection based on audience analytics from OnlyCamera/facial recognition systems.

---

##  Complete AdContractData Fields

### ✅ Currently Implemented
```json
{
  "advertId": "adja1965",
  "contractId": "contract-uuid",
  "dateCreated": "2026-05-20T10:30:00Z",
  "startDate": "2026-05-01",
  "endDate": "2026-06-30",
  "format": "VIDEO",
  "videoUrl": "path/to/media.mp4",
  
  // Display overlays
  "textTop": "Special Offer!",
  "textBottom": "Visit us today",
  "textLeft": null,
  "textRight": null,
  "targeturl": "https://example.com",
  
  // Demographic targeting (IOT-enabled)
  "targetHours": [9, 10, 11, 14, 15, 16, 17, 18],
  "targetGender": ["MALE", "FEMALE"],
  "targetAgeGroup": ["20-32", "32-42", "42-50"],
  "targetTags": ["brand", "promo", "sports"],
  "targetEmotion": ["happy", "neutral"],  // ✨ NEW - Added May 2026
  
  // Other targeting
  "targetDevice": ["TV", "TABLET"],
  "targetEvent": ["weekend", "holiday"],
  "targetLocationType": ["retail", "mall"]
}
```

---

##  Field Specifications

### 1. **targetEmotion** ✨ NEW
**Type**: `Array<String>`  
**Required**: No (optional)  
**Default**: `null` (matches all emotions)

**Purpose**: Target ads to specific viewer emotional states detected by facial recognition.

**Valid Values**:
- `"happy"` - Joyful, smiling viewers → promotional, celebration, upbeat ads
- `"neutral"` - Calm, attentive viewers → informational, brand awareness ads
- `"sad"` - Sad, downcast viewers → comfort, support, gentle ads
- `"angry"` - Frustrated, annoyed viewers → problem-solving, stress-relief ads
- `"surprise"` - Surprised viewers → attention-grabbing, curiosity ads
- `"fear"` - Worried viewers → reassurance, security ads
- `"disgust"` - Repulsed viewers → NOT RECOMMENDED for ad targeting

**Examples**:
```json
// Upbeat promotional ad - only show to happy viewers
"targetEmotion": ["happy"]

// Brand awareness - show to calm/neutral viewers
"targetEmotion": ["neutral"]

// Support service ad - show to sad/angry viewers
"targetEmotion": ["sad", "angry"]

// Universal ad - show to anyone (omit field or use null)
"targetEmotion": null
```

**Scoring Impact**:
- **Exact match**: +25 points (happiness), +15 (neutral), +20 (sad/angry)
- **No filter (null)**: Baseline +3 to +8 points to all ads
- **Mismatch**: 0 points (ad won't show to unmatched emotions)

---

### 2. **targetGender**
**Type**: `Array<String>`  
**Required**: No  
**Default**: `null` (all genders)

**Valid Values**: `["MALE"]`, `["FEMALE"]`, `["MALE", "FEMALE"]`, `null`

**IOT Mapping**:
- IOT sends: `"M"` or `"F"`
- App converts: `M → MALE`, `F → FEMALE`
- Match logic: Case-insensitive substring match

**Scoring**:
- **Match**: +15 points
- **No filter**: +5 points (baseline)
- **Mismatch**: 0 points

---

### 3. **targetAgeGroup**
**Type**: `Array<String>`  
**Required**: No  
**Default**: `null` (all ages)

**Valid Values**:
```json
["0-20", "20-32", "32-42", "42-50", "50+"]
```

**IOT Format**:
- IOT sends: `"ageBracket": "32-42"` (exact match format)
- Direct match against array entries

**Scoring**:
- **Match**: +15 points
- **No filter**: +5 to +8 (fallback age detection)
- **Mismatch**: 0 points

**Examples**:
```json
// Youth product - target Gen Z and young Millennials
"targetAgeGroup": ["0-20", "20-32"]

// Retirement planning - target mature adults
"targetAgeGroup": ["42-50", "50+"]

// Universal product
"targetAgeGroup": null
```

---

### 4. **targetTags**
**Type**: `Array<String>`  
**Required**: No  
**Default**: `null`

**Purpose**: Flexible tag-based targeting. IOT system can send custom tags in `adTarget.tags`.

**Example Use Cases**:
```json
// Brand awareness campaign
"targetTags": ["brand", "awareness"]

// Limited-time promo
"targetTags": ["promo", "sale", "limited-time"]

// Sports-related ad
"targetTags": ["sports", "fitness", "active"]

// Location-specific
"targetTags": ["premium", "high-end", "luxury"]
```

**Scoring**: +5 points per matching tag

---

### 5. **targetHours**
**Type**: `Array<Integer>`  
**Required**: No  
**Default**: `null` (24/7)

**Valid Range**: 0-23 (24-hour format)

**Examples**:
```json
// Morning commute coffee ad
"targetHours": [6, 7, 8, 9]

// Lunch special
"targetHours": [11, 12, 13, 14]

// Evening entertainment
"targetHours": [18, 19, 20, 21, 22]

// Business hours only
"targetHours": [9, 10, 11, 12, 13, 14, 15, 16, 17]
```

**Scoring**: +15 points if current hour in array

---

##  React Admin Interface Requirements

### Ad Creation Form - Targeting Section

```jsx
<FormSection title="Demographic Targeting (IOT)">
  
  {/* Time Targeting */}
  <HourPicker
    label="Target Hours (optional)"
    name="targetHours"
    multiple={true}
    options={[0,1,2,...,23]}
    placeholder="Select hours or leave empty for 24/7"
  />
  
  {/* Gender Targeting */}
  <CheckboxGroup
    label="Target Gender (optional)"
    name="targetGender"
    options={[
      { value: "MALE", label: "Male" },
      { value: "FEMALE", label: "Female" }
    ]}
    defaultChecked={["MALE", "FEMALE"]}
    helperText="Leave both checked for all genders"
  />
  
  {/* Age Targeting */}
  <CheckboxGroup
    label="Target Age Groups (optional)"
    name="targetAgeGroup"
    options={[
      { value: "0-20", label: "Under 20 (Gen Z)" },
      { value: "20-32", label: "20-32 (Young Millennials)" },
      { value: "32-42", label: "32-42 (Millennials)" },
      { value: "42-50", label: "42-50 (Gen X)" },
      { value: "50+", label: "50+ (Boomers+)" }
    ]}
    helperText="Leave empty to target all ages"
  />
  
  {/* Emotion Targeting ✨ NEW */}
  <CheckboxGroup
    label="Target Emotions (IOT) ✨ NEW"
    name="targetEmotion"
    options={[
      { value: "happy", label: " Happy (Promotional/Upbeat)" },
      { value: "neutral", label: " Neutral (Brand/Info)" },
      { value: "sad", label: " Sad (Comfort/Support)" },
      { value: "angry", label: " Angry (Problem-Solving)" },
      { value: "surprise", label: " Surprise (Attention-Grabbing)" },
      { value: "fear", label: " Fear (Reassurance)" }
    ]}
    helperText="Select emotions this ad targets. Leave empty for universal ads."
    badges={true}  // Show selected emotions as colorful badges
  />
  
  {/* Tags */}
  <TagInput
    label="Custom Tags (optional)"
    name="targetTags"
    placeholder="Add tags: brand, promo, sports, etc."
    suggestions={["brand", "promo", "sale", "sports", "lifestyle", "tech"]}
  />

</FormSection>
```

### UI/UX Recommendations

1. **Emotion Icons**: Use emoji/icons for better UX
2. **Smart Defaults**: 
   - All genders checked by default
   - All ages checked by default
   - No emotions selected by default (universal)
3. **Preview**: Show estimated audience reach based on selections
4. **Help Text**: Explain each targeting option with examples
5. **Validation**: Warn if targeting is too narrow (e.g., only one age + one gender + one emotion)

---

##  Backend API Requirements

### 1. Database Schema Updates

**Add `targetEmotion` column** to `ad_contracts` table:
```sql
ALTER TABLE ad_contracts 
ADD COLUMN target_emotion TEXT[];  -- PostgreSQL array of strings
-- or JSON if using MySQL
```

### 2. API Endpoint Updates

**GET /api/get_screen_playlists/:screenId**

Response should include ALL targeting fields:
```json
{
  "adContractData": {
    "advertId": "adja1965",
    "targetHours": [9, 10, 11, 14, 15, 16],
    "targetGender": ["MALE", "FEMALE"],
    "targetAgeGroup": ["20-32", "32-42"],
    "targetTags": ["brand", "promo"],
    "targetEmotion": ["happy", "neutral"],  // ✨ MUST include
    "videoUrl": "path/to/video.mp4",
    // ... other fields
  },
  "duration": 30,
  "contractId": "contract-uuid",
  // ... other fields
}
```

**POST /api/ad_contracts** (Create ad)

Accept `targetEmotion` in request body:
```json
{
  "advertId": "new-ad-123",
  "targetEmotion": ["happy"],
  // ... other fields
}
```

**PUT /api/ad_contracts/:id** (Update ad)

Allow updating `targetEmotion`:
```json
{
  "targetEmotion": ["neutral", "happy"]
}
```

### 3. Validation Rules

```javascript
// Backend validation
const VALID_EMOTIONS = ['happy', 'neutral', 'sad', 'angry', 'fear', 'surprise', 'disgust'];
const VALID_AGE_GROUPS = ['0-20', '20-32', '32-42', '42-50', '50+'];
const VALID_GENDERS = ['MALE', 'FEMALE'];

function validateAdContract(data) {
  // targetEmotion validation
  if (data.targetEmotion) {
    if (!Array.isArray(data.targetEmotion)) {
      throw new Error('targetEmotion must be an array');
    }
    for (const emotion of data.targetEmotion) {
      if (!VALID_EMOTIONS.includes(emotion)) {
        throw new Error(`Invalid emotion: ${emotion}`);
      }
    }
  }
  
  // targetAgeGroup validation
  if (data.targetAgeGroup) {
    for (const age of data.targetAgeGroup) {
      if (!VALID_AGE_GROUPS.includes(age)) {
        throw new Error(`Invalid age group: ${age}`);
      }
    }
  }
  
  // targetGender validation
  if (data.targetGender) {
    for (const gender of data.targetGender) {
      if (!VALID_GENDERS.includes(gender)) {
        throw new Error(`Invalid gender: ${gender}`);
      }
    }
  }
  
  return true;
}
```

---

##  Testing Scenarios

### Test Case 1: Emotion-Targeted Ad
```json
{
  "advertId": "test-happy-promo",
  "format": "VIDEO",
  "videoUrl": "happy-promo.mp4",
  "targetEmotion": ["happy"],
  "targetGender": null,
  "targetAgeGroup": null,
  "targetHours": null
}
```
**Expected**: Only shows to happy viewers, any gender/age, 24/7

### Test Case 2: Multi-Demographic Ad
```json
{
  "advertId": "test-millennial-female",
  "targetEmotion": ["neutral", "happy"],
  "targetGender": ["FEMALE"],
  "targetAgeGroup": ["20-32", "32-42"],
  "targetHours": [10, 11, 12, 13, 14, 15]
}
```
**Expected**: Shows to happy/neutral female Millennials during business hours

### Test Case 3: Universal Ad (No Filters)
```json
{
  "advertId": "test-universal",
  "targetEmotion": null,
  "targetGender": null,
  "targetAgeGroup": null,
  "targetHours": null
}
```
**Expected**: Shows to everyone, always (baseline scoring)

---

##  Analytics Recommendations

Track these metrics in your backend:

1. **Emotion Distribution**: How often each emotion triggers
2. **Match Rate**: % of matches vs. total IOT messages
3. **Emotion Effectiveness**: CTR/engagement per emotion type
4. **Narrow Targeting Warning**: Alert if ad targeting is too specific (low match rate)

### Suggested Dashboard Metrics

```javascript
{
  "adId": "adja1965",
  "iotMetrics": {
    "totalImpressions": 1250,
    "iotTriggeredImpressions": 450,  // Played due to IOT match
    "emotionBreakdown": {
      "happy": 280,
      "neutral": 150,
      "sad": 20
    },
    "avgScore": 62.5,  // Average matching score
    "matchRate": 0.36  // 36% of viewers matched targeting
  }
}
```

---

##  Deployment Checklist

### Backend/API
- [ ] Add `targetEmotion` column to database
- [ ] Update API endpoints to include `targetEmotion`
- [ ] Add validation for emotion values
- [ ] Update API documentation
- [ ] Test with sample ads

### React Admin
- [ ] Add emotion targeting UI component
- [ ] Update ad creation form
- [ ] Update ad edit form
- [ ] Add emotion badge/icon display
- [ ] Add help tooltips with examples
- [ ] Test form submission with emotion targeting

### Android App
- [ ] ✅ AdContractData model updated
- [ ] ✅ AdEntity DB schema (v7)
- [ ] ✅ MediaModel extended
- [ ] ✅ Scoring algorithm enhanced
- [ ] ✅ All constructor calls updated
- [ ] ✅ Build successful
- [ ] Deploy to test devices
- [ ] Verify MQTT demographic messages
- [ ] Verify emotion-based ad selection
- [ ] Check logs for scoring breakdown

---

##  Future Enhancements (Optional)

### 1. Advanced Emotion Combinations
```json
// Show to happy OR surprise (either one)
"targetEmotion": ["happy", "surprise"],
"emotionLogic": "OR"

// Show only to happy AND neutral together
"targetEmotion": ["happy", "neutral"],
"emotionLogic": "AND"
```

### 2. Audience Size Targeting
```json
"targetAudienceSize": {
  "min": 3,  // Only show to groups of 3+
  "max": 10  // Not for large crowds
}
```

### 3. Dwell Time Targeting
```json
"targetDwellTime": {
  "min": 5,   // Viewer must watch at least 5 seconds
  "max": 60   // Quick engagement only
}
```

### 4. Emotion Score Threshold
```json
"emotionThreshold": {
  "happy": 60,  // Must be at least 60% happy
  "neutral": 40  // Or at least 40% neutral
}
```

---

##  Support & Questions

For implementation questions:
- **Android App**: See `IOT_AD_CONTRACT_SPEC.md` (this file)
- **API Spec**: Refer to sections above
- **Testing**: Use test scenarios provided

**Version**: 1.0 (May 2026)  
**Last Updated**: May 20, 2026
