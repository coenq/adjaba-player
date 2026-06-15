# Adjaba CMS - Demographic Targeting Implementation Checklist

## Quick Reference for React CMS Updates

### 🎯 Feature Overview
Add demographic-based ad targeting to the CMS to support the Android player's IOT/MQTT demographic ad selection feature.

---

## 📋 Implementation Tasks

### 1️⃣ Backend API Changes (Do First)

#### Database Schema
- [ ] Add `target_age_brackets` (JSON) to `ads` table
- [ ] Add `target_gender` (VARCHAR) to `ads` table  
- [ ] Add `target_emotions` (JSON) to `ads` table
- [ ] Add `primary_emotion` (VARCHAR) to `ads` table
- [ ] Add `target_hours` (JSON) to `ads` table
- [ ] Add `target_engagement_level` (VARCHAR) to `ads` table
- [ ] Add `min_audience_size` (INT) to `ads` table
- [ ] Add `demographic_targeting_enabled` (BOOLEAN) to `ads` table
- [ ] Add `iot_enabled` (BOOLEAN) to `screens` table
- [ ] Add `iot_screen_id` (VARCHAR) to `screens` table

#### API Endpoints
- [ ] Update `POST /api/ads` to accept demographic fields
- [ ] Update `PUT /api/ads/:id` to accept demographic fields
- [ ] Update `GET /api/ads` to return demographic fields
- [ ] Update `POST /api/screens` to accept IOT fields
- [ ] Update `PUT /api/screens/:id` to accept IOT fields
- [ ] Update `GET /api/screens` to return IOT fields
- [ ] Add validation for demographic field values
- [ ] Test backward compatibility (old ads without demographic data)

---

### 2️⃣ React CMS Frontend Changes

#### TypeScript/JavaScript Types
- [ ] Create `AdDemographicTargeting` interface/type
- [ ] Update `Ad` interface to include `demographicTargeting` field
- [ ] Update `Screen` interface to include `iotEnabled` and `iotScreenId` fields
- [ ] Create enums/constants for age brackets, emotions, engagement levels

#### New Components
```
src/components/
  ├── DemographicTargeting/
  │   ├── DemographicTargetingForm.jsx (or .tsx)
  │   ├── AgeBracketSelector.jsx
  │   ├── GenderSelector.jsx
  │   ├── EmotionSelector.jsx
  │   ├── HourSelector.jsx
  │   ├── EngagementLevelSelector.jsx
  │   └── index.js
  └── IOT/
      ├── IOTConfiguration.jsx
      └── index.js
```

- [ ] Create `DemographicTargetingForm` component
- [ ] Create sub-components (age, gender, emotion, hours, engagement selectors)
- [ ] Create `IOTConfiguration` component for screen settings
- [ ] Add unit tests for new components (if project has testing setup)

#### Modified Components

**Ad Creation/Edit Form**
- [ ] Import `DemographicTargetingForm` component
- [ ] Add collapsible "Demographic Targeting" section
- [ ] Wire up form state management
- [ ] Add validation for demographic fields
- [ ] Update submit handler to include demographic data
- [ ] Show "🎯 Targeted" badge when enabled

**Screen Configuration Form**
- [ ] Add "IOT Integration" section
- [ ] Add IOT enabled toggle
- [ ] Add IOT screen ID input field
- [ ] Add validation (screen ID required if IOT enabled)
- [ ] Update submit handler to include IOT data

**Ad List/Grid View**
- [ ] Add "🎯" badge/icon for demographic-enabled ads
- [ ] Add filter option for "Demographic Targeting"
- [ ] Add sort option for IOT-enabled ads
- [ ] Optional: Add bulk enable/disable demographic targeting action

**Screen List/Grid View**
- [ ] Add "IOT" badge/icon for IOT-enabled screens
- [ ] Add filter option for IOT-enabled screens
- [ ] Show IOT screen ID in list/grid view

#### API Integration
- [ ] Update ad creation API call to send demographic fields
- [ ] Update ad update API call to send demographic fields
- [ ] Update screen creation API call to send IOT fields
- [ ] Update screen update API call to send IOT fields
- [ ] Handle API errors gracefully
- [ ] Add loading states
- [ ] Add success/error notifications

---

### 3️⃣ UI/UX Elements

#### Demographic Targeting Form Fields

**Enable Toggle**
- [ ] Label: "Enable IOT Demographic Targeting"
- [ ] Default: OFF
- [ ] Shows/hides all demographic fields

**Age Brackets** (Multi-Select)
- [ ] Young Adults (20-32)
- [ ] Adults (32-42)
- [ ] Middle Age (43-53)
- [ ] Seniors (53+)

**Gender** (Radio/Dropdown)
- [ ] Any Gender (default)
- [ ] Male
- [ ] Female

**Content Tone/Emotion** (Simplified Recommended)
- [ ] Any Emotion (default)
- [ ] Upbeat/Promotional (happy)
- [ ] Neutral/Informational (neutral)
- [ ] Gentle/Supportive (sad/comfort)

**Target Hours** (Multi-Select or Chips)
- [ ] Hour picker (0-23)
- [ ] Pre-configured time blocks (Morning, Midday, Evening, Night)
- [ ] Visual timeline selector (optional)

**Engagement Level** (Dropdown)
- [ ] Any
- [ ] Quick Glance (≤5 seconds)
- [ ] Standard (5-15 seconds)
- [ ] High Attention (>15 seconds)

**Minimum Audience Size** (Number Input)
- [ ] Number field (default: 1)
- [ ] Range: 1-10+
- [ ] Help text explaining group-appeal ads

#### IOT Configuration Fields

**Screen IOT Settings**
- [ ] Enable IOT toggle
- [ ] IOT Screen ID text input
- [ ] Validation on screen ID format
- [ ] Help text explaining MQTT topic structure

---

### 4️⃣ Validation Rules

#### Ad Demographic Targeting
- [ ] Validate age brackets are from allowed list
- [ ] Validate gender is 'M', 'F', or 'ANY'
- [ ] Validate hours are 0-23
- [ ] Validate engagement level is valid enum value
- [ ] Validate min audience size ≥ 1
- [ ] Warn if demographic targeting enabled but no criteria set
- [ ] Handle null/undefined values (backward compatibility)

#### Screen IOT Configuration
- [ ] IOT Screen ID required if IOT enabled
- [ ] Screen ID format validation (alphanumeric + - _)
- [ ] Max length validation (100 chars)

---

### 5️⃣ Testing

#### Manual Testing
- [ ] Create new ad with demographic targeting
- [ ] Edit existing ad to add demographic targeting
- [ ] Edit existing ad to remove demographic targeting
- [ ] Create ad without demographic targeting (backward compatibility)
- [ ] Enable IOT on screen
- [ ] Disable IOT on screen
- [ ] Create screen without IOT (backward compatibility)
- [ ] Test form validation (invalid hours, invalid age brackets, etc.)
- [ ] Test API error handling
- [ ] Test on different screen sizes (responsive)
- [ ] Test with real player app (end-to-end)

#### Automated Testing (if applicable)
- [ ] Unit tests for `DemographicTargetingForm` component
- [ ] Unit tests for sub-components
- [ ] Integration tests for ad creation with demographics
- [ ] API integration tests
- [ ] Validation logic tests

---

### 6️⃣ Documentation

- [ ] Update user guide with demographic targeting instructions
- [ ] Update API documentation
- [ ] Add screenshots to documentation
- [ ] Create video tutorial (optional)
- [ ] Update release notes
- [ ] Create FAQ section

---

### 7️⃣ Deployment

- [ ] Deploy backend changes first
- [ ] Test backend API endpoints
- [ ] Deploy CMS frontend changes
- [ ] Smoke test production
- [ ] Monitor error logs
- [ ] Train customer support team
- [ ] Announce feature to users

---

## 🎨 UI/UX Best Practices

### Visual Hierarchy
```
Ad Form
├── Basic Info (name, media, duration) ← Always visible
├── Scheduling (dates, times) ← Always visible
└── 🆕 Demographic Targeting ← Collapsible section
    ├── [Toggle] Enable IOT Targeting
    └── (When enabled)
        ├── Age Brackets
        ├── Gender
        ├── Content Tone
        ├── Target Hours
        ├── Engagement Level
        └── Min Audience Size
```

### Design Guidelines
- Use collapsible sections to avoid overwhelming users
- Show help text/tooltips for complex fields
- Use icons/badges to indicate targeted ads (🎯)
- Match existing CMS design system
- Ensure responsive design
- Maintain accessibility (keyboard navigation, ARIA labels)

---

## 📊 Field Reference

### Age Brackets
| Value | Label | Description |
|-------|-------|-------------|
| `20-32` | Young Adults | Ages 20-32 |
| `32-42` | Adults | Ages 32-42 |
| `43-53` | Middle Age | Ages 43-53 |
| `53+` | Seniors | Ages 53+ |

### Gender Options
| Value | Label |
|-------|-------|
| `ANY` | Any Gender |
| `M` | Male |
| `F` | Female |

### Emotions (Advanced)
| Value | Use Case |
|-------|----------|
| `happy` | Promotional, upbeat content |
| `neutral` | Brand, informational content |
| `sad` | Gentle, supportive content |
| `angry` | Calm, de-escalation content |
| `fear` | Reassuring content |
| `surprise` | Engaging, unexpected content |
| `disgust` | (Rarely used) |

### Content Tone (Simplified - Recommended)
| Value | Maps To Emotion | Use Case |
|-------|-----------------|----------|
| `upbeat` | `happy` | Promotional ads, sales, new products |
| `neutral` | `neutral` | Brand awareness, information |
| `gentle` | `sad`, `angry` | Support, comfort, empathy-based content |

### Engagement Levels
| Value | Dwell Time | Ad Type |
|-------|------------|---------|
| `quick` | ≤ 5 seconds | Short ads, 5-10 sec videos |
| `standard` | 5-15 seconds | Normal ads, 15-30 sec videos |
| `high` | > 15 seconds | Long-form content, 30+ sec videos |

### Hour Format
- 24-hour format (0-23)
- Examples:
  - Morning: [6, 7, 8, 9, 10, 11]
  - Midday: [12, 13, 14]
  - Evening: [15, 16, 17, 18, 19]
  - Night: [20, 21, 22, 23]

---

## 🔗 API Payload Examples

### Creating Ad with Demographic Targeting
```json
POST /api/ads
{
  "name": "Summer Sale Promo",
  "url": "https://cdn.adjaba.in/ads/summer-sale.mp4",
  "duration": 30,
  "type": "video",
  "demographicTargeting": {
    "enableDemographicTargeting": true,
    "targetAgeBrackets": ["20-32", "32-42"],
    "targetGender": "ANY",
    "primaryEmotion": "happy",
    "targetHours": [10, 11, 12, 13, 14, 15, 16],
    "targetEngagementLevel": "standard",
    "minAudienceSize": 1
  }
}
```

### Updating Screen with IOT Configuration
```json
PUT /api/screens/12345
{
  "name": "Mall Entrance Display",
  "location": "Main Entrance",
  "orientation": "landscape",
  "iotEnabled": true,
  "iotScreenId": "mall_main_001"
}
```

---

## ⚠️ Common Pitfalls

1. ❌ **Breaking existing ads**: Always handle missing demographic fields gracefully
2. ❌ **Over-complicated UI**: Keep demographic form simple and optional
3. ❌ **No backend coordination**: Ensure backend API exists before frontend work
4. ❌ **Missing validation**: Validate on both frontend and backend
5. ❌ **No testing**: Test with real player app before production
6. ❌ **Ignoring mobile**: Ensure CMS works on tablets/mobile browsers
7. ❌ **Hardcoded values**: Use constants/enums for reusability

---

## ✅ Success Criteria

- [ ] Users can create ads with demographic targeting
- [ ] Users can enable/disable demographic targeting on existing ads
- [ ] All existing ads (without demographics) continue to work
- [ ] Users can enable IOT on screens
- [ ] Visual indicators show which ads/screens have targeting/IOT enabled
- [ ] Form validation prevents invalid data submission
- [ ] API integration works correctly
- [ ] Player app correctly receives and uses demographic data
- [ ] No production issues or regressions

---

## 📞 Support

For questions about:
- **Android Player App**: See `CLAUDE.md`
- **MQTT/IOT Integration**: See `ADJABA_CMS_PROMPT.md` MQTT section
- **Scoring Algorithm**: See `ADJABA_CMS_PROMPT.md` Reference section

---

**Last Updated**: May 31, 2026
**Feature**: Demographic-Based Ad Targeting (IOT)
**Version**: 1.0

