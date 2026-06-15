# Adjaba CMS - One-Page Quick Reference

```
╔══════════════════════════════════════════════════════════════════════════════╗
║                    ADJABA CMS DEMOGRAPHIC TARGETING                          ║
║                         Quick Reference Card                                 ║
╚══════════════════════════════════════════════════════════════════════════════╝

┌──────────────────────────────────────────────────────────────────────────────┐
│ 📚 DOCUMENTS CREATED                                                         │
├──────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  1. ADJABA_CMS_PROMPT.md                    [AI/Developer Instructions]     │
│     → Full feature requirements & guidelines                                │
│     → Database schema & API specs                                           │
│     → UI/UX requirements & validation rules                                 │
│                                                                              │
│  2. CMS_IMPLEMENTATION_CHECKLIST.md         [Task Checklist]                │
│     → Step-by-step implementation tasks                                     │
│     → Field reference tables                                                │
│     → API payload examples                                                  │
│                                                                              │
│  3. CMS_REACT_CODE_EXAMPLES.md              [Code Templates]                │
│     → Ready-to-use React components                                         │
│     → TypeScript types & constants                                          │
│     → Form validation utilities                                             │
│                                                                              │
│  4. CMS_INTEGRATION_SUMMARY.md              [This Overview]                 │
│     → How to use the documents                                              │
│     → Implementation phases                                                 │
│     → Success criteria                                                      │
│                                                                              │
└──────────────────────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────────────────────┐
│ 🎯 FEATURE OVERVIEW                                                          │
├──────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  Android Player (Already Implemented):                                      │
│  ✓ Receives real-time demographic data via MQTT                             │
│  ✓ Scores ads based on viewer demographics                                  │
│  ✓ Selects best-matching ad dynamically                                     │
│                                                                              │
│  CMS (To Be Implemented):                                                   │
│  ☐ Configure demographic targeting on ads                                   │
│  ☐ Enable IOT on screens                                                    │
│  ☐ Provide UI for targeting criteria                                        │
│                                                                              │
└──────────────────────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────────────────────┐
│ 📋 NEW CMS FIELDS                                                            │
├──────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  AD DEMOGRAPHIC TARGETING:                                                  │
│  ┌────────────────────────────────────────────────────────────────────┐     │
│  │ ☑ Enable Demographic Targeting                                     │     │
│  │                                                                     │     │
│  │ Age Brackets:    ☐ 20-32  ☐ 32-42  ☐ 43-53  ☐ 53+                │     │
│  │ Gender:          ⦿ Any    ○ Male   ○ Female                       │     │
│  │ Content Tone:    ⦿ Upbeat ○ Neutral ○ Gentle                      │     │
│  │ Target Hours:    [9, 10, 11, 14, 15, 16, 17]                      │     │
│  │ Engagement:      ⦿ Standard (5-15 sec)                            │     │
│  │ Min Audience:    [1] people                                        │     │
│  └────────────────────────────────────────────────────────────────────┘     │
│                                                                              │
│  SCREEN IOT CONFIGURATION:                                                  │
│  ┌────────────────────────────────────────────────────────────────────┐     │
│  │ ☑ Enable IOT Mode                                                  │     │
│  │                                                                     │     │
│  │ IOT Screen ID:   [demo959________________]                         │     │
│  │ MQTT Topic:      store/demo959                                     │     │
│  └────────────────────────────────────────────────────────────────────┘     │
│                                                                              │
└──────────────────────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────────────────────┐
│ 🚀 IMPLEMENTATION PHASES                                                     │
├──────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  Phase 1: BACKEND (Do First!)          Estimated: 8-16 hours                │
│  ├─ Add database columns                                                    │
│  ├─ Update POST/PUT /api/ads endpoints                                      │
│  ├─ Update POST/PUT /api/screens endpoints                                  │
│  ├─ Add validation                                                          │
│  └─ Test with Postman                                                       │
│                                                                              │
│  Phase 2: FRONTEND COMPONENTS           Estimated: 16-24 hours              │
│  ├─ Create constants & types                                                │
│  ├─ Create DemographicTargetingForm                                         │
│  ├─ Create IOTConfiguration                                                 │
│  ├─ Create sub-components (age, gender, emotion, hours)                     │
│  └─ Add visual indicators (badges)                                          │
│                                                                              │
│  Phase 3: INTEGRATION                   Estimated: 8-12 hours               │
│  ├─ Integrate DemographicTargetingForm into AdForm                          │
│  ├─ Integrate IOTConfiguration into ScreenForm                              │
│  ├─ Update AdList with targeting badges                                     │
│  ├─ Update ScreenList with IOT badges                                       │
│  └─ Wire up API calls                                                       │
│                                                                              │
│  Phase 4: TESTING                       Estimated: 8-16 hours               │
│  ├─ Unit tests for components                                               │
│  ├─ Integration tests with API                                              │
│  ├─ Manual testing with checklist                                           │
│  └─ End-to-end test with Android player                                     │
│                                                                              │
│  Phase 5: DEPLOYMENT                    Estimated: 4-8 hours                │
│  ├─ Deploy backend (first)                                                  │
│  ├─ Deploy frontend                                                         │
│  ├─ Smoke test production                                                   │
│  └─ Monitor & document                                                      │
│                                                                              │
│  TOTAL ESTIMATED TIME: 44-76 hours (5-9 working days for 1 developer)       │
│                                                                              │
└──────────────────────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────────────────────┐
│ 💾 DATABASE CHANGES                                                          │
├──────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  ADS TABLE:                                                                 │
│  ALTER TABLE ads ADD COLUMN target_age_brackets JSON;                       │
│  ALTER TABLE ads ADD COLUMN target_gender VARCHAR(10);                      │
│  ALTER TABLE ads ADD COLUMN primary_emotion VARCHAR(20);                    │
│  ALTER TABLE ads ADD COLUMN target_hours JSON;                              │
│  ALTER TABLE ads ADD COLUMN target_engagement_level VARCHAR(20);            │
│  ALTER TABLE ads ADD COLUMN min_audience_size INT DEFAULT 1;                │
│  ALTER TABLE ads ADD COLUMN demographic_targeting_enabled BOOLEAN;          │
│                                                                              │
│  SCREENS TABLE:                                                             │
│  ALTER TABLE screens ADD COLUMN iot_enabled BOOLEAN DEFAULT FALSE;          │
│  ALTER TABLE screens ADD COLUMN iot_screen_id VARCHAR(100);                 │
│                                                                              │
└──────────────────────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────────────────────┐
│ 🔌 API ENDPOINTS                                                             │
├──────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  POST /api/ads                                                              │
│  PUT  /api/ads/:id                                                          │
│  GET  /api/ads                      ?demographicEnabled=true                │
│                                                                              │
│  POST /api/screens                                                          │
│  PUT  /api/screens/:id                                                      │
│  GET  /api/screens                  ?iotEnabled=true                        │
│                                                                              │
└──────────────────────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────────────────────┐
│ 📦 REACT COMPONENTS TO CREATE                                                │
├──────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  src/components/ads/demographic/                                            │
│    ├── DemographicTargetingForm.jsx        [Main container]                │
│    ├── AgeBracketSelector.jsx              [Age checkboxes]                │
│    ├── GenderSelector.jsx                  [Gender radio buttons]          │
│    ├── EmotionSelector.jsx                 [Content tone radio]            │
│    ├── HourSelector.jsx                    [Hour chips/picker]             │
│    ├── EngagementLevelSelector.jsx         [Engagement radio]              │
│    └── index.js                             [Exports]                       │
│                                                                              │
│  src/components/screens/                                                    │
│    └── IOTConfiguration.jsx                [IOT toggle + screen ID]         │
│                                                                              │
│  src/types/                                                                 │
│    └── demographics.ts                     [TypeScript types]               │
│                                                                              │
│  src/constants/                                                             │
│    └── demographics.js                     [Age/gender/emotion options]     │
│                                                                              │
│  src/utils/                                                                 │
│    └── demographicValidation.js            [Validation logic]               │
│                                                                              │
└──────────────────────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────────────────────┐
│ 🎨 VISUAL INDICATORS                                                         │
├──────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  Ad List:                                                                   │
│  ┌──────────────────────────────────────────────┐                           │
│  │ Summer Sale Promo  🎯 Targeted               │                           │
│  │ 30 sec | Video                               │                           │
│  └──────────────────────────────────────────────┘                           │
│                                                                              │
│  Screen List:                                                               │
│  ┌──────────────────────────────────────────────┐                           │
│  │ Mall Entrance Display  📡 IOT                │                           │
│  │ Main Entrance | Landscape                    │                           │
│  └──────────────────────────────────────────────┘                           │
│                                                                              │
└──────────────────────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────────────────────┐
│ ⚡ SCORING ALGORITHM (Player App Reference)                                  │
├──────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  Criterion                Points      Example                               │
│  ─────────────────────────────────────────────────────────                  │
│  Hour Match               +15         Current: 14h, Ad: [14,15,16]          │
│  Gender Match             +10         Viewer: M, Ad: M                      │
│  Age Bracket Match        +12         Viewer: 32-42, Ad: 32-42              │
│  Emotion: Happy           +20         Viewer happy, Ad: upbeat              │
│  Emotion: Neutral         +10         Viewer neutral, Ad: neutral           │
│  Emotion: Sad/Angry       +2          Viewer sad, Ad: gentle                │
│  High Happiness           +15         Happiness score > 60%                 │
│  High Engagement          +8          Viewer dwell > 15s, Ad: high          │
│  Quick Engagement         +5          Viewer dwell ≤ 5s, Ad: quick          │
│  Multi-Person Audience    +5          Audience ≥ 3, Ad min_audience ≥ 3    │
│                                                                              │
│  Selection: Ad with HIGHEST total score wins                                │
│                                                                              │
└──────────────────────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────────────────────┐
│ ✅ SUCCESS CRITERIA                                                          │
├──────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  ☐ Users can create ads with demographic targeting                          │
│  ☐ Users can edit/disable demographic targeting                             │
│  ☐ "🎯 Targeted" badge appears on demographic ads                           │
│  ☐ Users can enable IOT on screens                                          │
│  ☐ "📡 IOT" badge appears on IOT-enabled screens                            │
│  ☐ Form validation prevents invalid inputs                                  │
│  ☐ API integration works correctly                                          │
│  ☐ Player app receives & uses demographic data                              │
│  ☐ All existing ads/screens work (backward compatibility)                   │
│  ☐ No production errors or regressions                                      │
│                                                                              │
└──────────────────────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────────────────────┐
│ 🚨 CRITICAL REMINDERS                                                        │
├──────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  ⚠️  DO BACKEND FIRST - Frontend depends on API support                     │
│  ⚠️  BACKWARD COMPATIBILITY - All existing ads must continue working         │
│  ⚠️  DEFAULT VALUES - New fields default to disabled/null                    │
│  ⚠️  VALIDATION - Validate on both frontend and backend                      │
│  ⚠️  TESTING - Test with real Android player before production              │
│  ⚠️  DEPLOYMENT - Deploy backend before frontend                             │
│                                                                              │
└──────────────────────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────────────────────┐
│ 🎓 HOW TO USE THESE DOCUMENTS                                                │
├──────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  FOR AI ASSISTANT:                                                          │
│    1. Share ADJABA_CMS_PROMPT.md with AI                                    │
│    2. Show your current CMS code                                            │
│    3. Ask AI to implement specific components                               │
│    4. Use CMS_REACT_CODE_EXAMPLES.md as reference                           │
│                                                                              │
│  FOR DEVELOPER:                                                             │
│    1. Read ADJABA_CMS_PROMPT.md (15 min)                                    │
│    2. Follow CMS_IMPLEMENTATION_CHECKLIST.md                                │
│    3. Copy code from CMS_REACT_CODE_EXAMPLES.md                             │
│    4. Adapt to your project                                                 │
│    5. Test & deploy                                                         │
│                                                                              │
│  FOR PROJECT MANAGER:                                                       │
│    1. Read CMS_INTEGRATION_SUMMARY.md                                       │
│    2. Review CMS_IMPLEMENTATION_CHECKLIST.md for tasks                      │
│    3. Estimate 5-9 days for 1 full-stack developer                          │
│    4. Coordinate backend team first                                         │
│                                                                              │
└──────────────────────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────────────────────┐
│ 📞 SUPPORT & REFERENCE                                                       │
├──────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  Android Player Implementation:   CLAUDE.md                                 │
│  CMS Full Instructions:           ADJABA_CMS_PROMPT.md                      │
│  Implementation Checklist:        CMS_IMPLEMENTATION_CHECKLIST.md           │
│  Code Examples:                   CMS_REACT_CODE_EXAMPLES.md                │
│  Integration Guide:               CMS_INTEGRATION_SUMMARY.md                │
│  Quick Reference:                 CMS_QUICK_REFERENCE.md (this file)        │
│                                                                              │
└──────────────────────────────────────────────────────────────────────────────┘

╔══════════════════════════════════════════════════════════════════════════════╗
║  🚀 YOU'RE READY TO START!                                                   ║
║                                                                              ║
║  All documentation, code examples, and checklists are ready.                ║
║  Follow the phases, coordinate with your backend team, and test thoroughly. ║
║                                                                              ║
║  Created: May 31, 2026 | Feature: Demographic Ad Targeting                  ║
╚══════════════════════════════════════════════════════════════════════════════╝
```

