# Adjaba CMS React Application - AI Instructions

You are an expert React developer specializing in modern web applications with REST API integration.

## Project Context

You are working on the **Adjaba CMS** - a React-based content management system for controlling digital signage displays on Android TV and Fire TV devices.

The CMS:
* Manages screens (displays), advertisements, bookings, and playlists
* Communicates with a REST API backend
* Already has an established architecture and codebase
* Is deployed in production environments
* Serves commercial/enterprise clients managing digital signage networks

## Core Objective

Your goal is to:
* Extend the CMS to support new demographic-based ad targeting features
* Maintain backward compatibility with existing non-demographic ads
* Integrate cleanly with existing UI/UX patterns
* Preserve all current functionality
* Follow React best practices and existing code patterns

## Strict Constraints

* **DO NOT** introduce new UI frameworks (stick to existing: React, Material-UI, Ant Design, or whatever is currently used)
* **DO NOT** refactor unrelated components
* **FOLLOW** existing component structure, naming conventions, and state management patterns
* **PRESERVE** backward compatibility - all existing ads/screens must work unchanged
* **MINIMIZE** risk of breaking production features
* **MATCH** existing code style, formatting, and patterns

## Functional Context

The CMS manages:
* **Screens**: Digital signage displays with unique IDs, locations, orientations
* **Ads/Media**: Images, videos, web content with scheduling and targeting
* **Bookings**: Ad campaign scheduling with date/time ranges
* **Playlists**: Sequences of media to play on specific screens
* **Users/Clients**: Multi-tenant access control
* **Analytics**: Playback metrics and reporting

## New Feature Requirements: Demographic-Based Ad Targeting (May 2026)

### Overview
The Android player app now supports real-time demographic-based ad selection using MQTT IOT data. The CMS must allow users to configure ads with demographic targeting parameters.

### Player App Capabilities

The Android player:
1. Receives real-time audience analytics via MQTT (age, gender, emotion, dwell time, audience size)
2. Scores each ad based on demographic matching algorithm
3. Selects best-matching ad dynamically during playback
4. Can be enabled/disabled per screen via "IOT" toggle

### CMS Feature Requirements

#### 1. Ad/Media Model Extensions

**New Fields to Add to Ad Entity**:

```typescript
interface AdDemographicTargeting {
  // Age Targeting
  targetAgeBrackets?: string[];  // ["20-32", "32-42", "43-53", "53+"]
  
  // Gender Targeting
  targetGender?: 'M' | 'F' | 'ANY';  // M, F, or ANY (default)
  
  // Emotion Targeting
  targetEmotions?: string[];  // ["happy", "neutral", "sad", "angry", "fear", "surprise", "disgust"]
  primaryEmotion?: 'happy' | 'neutral' | 'sad';  // Simplified: upbeat/neutral/comfort content
  
  // Time-of-Day Targeting
  targetHours?: number[];  // [9, 10, 11, 14, 15, 16] - hours when ad should play
  
  // Engagement Targeting
  targetEngagementLevel?: 'quick' | 'standard' | 'high';  // Based on dwell time
  // quick: ≤5s (short ads)
  // standard: 5-15s (normal ads)
  // high: >15s (long-form content)
  
  // Audience Size Targeting
  minAudienceSize?: number;  // Minimum viewers (e.g., 3 for group-appeal ads)
  
  // Feature Toggle
  enableDemographicTargeting?: boolean;  // Default: false (backward compatibility)
}

interface Ad {
  // ...existing fields (id, name, url, duration, etc.)
  
  // NEW: Demographic targeting configuration
  demographicTargeting?: AdDemographicTargeting;
}
```

#### 2. Screen Model Extensions

**New Fields to Add to Screen Entity**:

```typescript
interface Screen {
  // ...existing fields (id, name, location, orientation, etc.)
  
  // NEW: IOT/Demographic feature toggle
  iotEnabled?: boolean;  // Default: false
  iotScreenId?: string;  // MQTT topic identifier (e.g., "demo959")
}
```

#### 3. UI Components to Add/Modify

##### A. Ad Creation/Edit Form

**Section: "Demographic Targeting" (Collapsible/Optional)**

1. **Enable Demographic Targeting** (Toggle/Checkbox)
   - Label: "Enable IOT Demographic Targeting"
   - Default: OFF
   - When enabled, show additional fields below

2. **Age Targeting** (Multi-Select Checkboxes)
   - Label: "Target Age Brackets"
   - Options:
     * ☐ Young Adults (20-32)
     * ☐ Adults (32-42)
     * ☐ Middle Age (43-53)
     * ☐ Seniors (53+)
   - Help text: "Ad will score higher when viewers match these age ranges"

3. **Gender Targeting** (Radio Buttons or Dropdown)
   - Label: "Target Gender"
   - Options:
     * ( ) Any Gender (default)
     * ( ) Male
     * ( ) Female
   - Help text: "Ad will score higher for matching gender"

4. **Emotion Targeting** (Simplified or Advanced)

   **Option A - Simplified** (Recommended for UX):
   - Label: "Content Tone"
   - Options:
     * ( ) Any Emotion (default)
     * ( ) Upbeat/Promotional (targets happy viewers)
     * ( ) Neutral/Informational (targets neutral viewers)
     * ( ) Gentle/Supportive (targets sad/angry viewers)

   **Option B - Advanced**:
   - Label: "Target Emotions" (Multi-Select)
   - Options: Happy, Neutral, Sad, Angry, Fear, Surprise, Disgust
   - Help text: "Ad will score higher when viewer emotions match"

5. **Time-of-Day Targeting** (Hour Selector)
   - Label: "Target Hours"
   - UI Component: Segmented hour picker (0-23) or multi-select dropdown
   - Example: [6-9] Morning, [10-14] Midday, [15-19] Evening, [20-23] Night
   - Help text: "Ad will score higher during these hours (24-hour format)"
   - Visual: Hour chips or timeline selector

6. **Engagement Level** (Dropdown or Radio)
   - Label: "Viewer Engagement Level"
   - Options:
     * Any
     * Quick Glance (≤5 seconds) - for short ads
     * Standard (5-15 seconds) - for normal ads
     * High Attention (>15 seconds) - for long-form content
   - Help text: "Match ad length to viewer dwell time"

7. **Minimum Audience Size** (Number Input)
   - Label: "Minimum Viewers"
   - Input: Number (default: 1)
   - Range: 1-10+
   - Help text: "Ad scores higher when this many people are watching (e.g., 3 for group-appeal ads)"

**UI/UX Guidelines**:
- Group all demographic fields in an expandable card/section
- Show visual indicators when demographic targeting is active (e.g., badge on ad list)
- Provide tooltips/help text explaining the scoring system
- Include a "Preview Score" calculator (optional) showing estimated matches
- Add validation: at least one targeting criterion if enabled

##### B. Screen Configuration Form

**Section: "IOT Integration"**

1. **Enable IOT Demographic Targeting** (Toggle)
   - Label: "Enable IOT Mode"
   - Default: OFF
   - Help text: "Receive real-time audience analytics via MQTT for demographic ad selection"

2. **IOT Screen ID** (Text Input)
   - Label: "IOT Screen/Store ID"
   - Placeholder: "e.g., demo959"
   - Validation: Required if IOT enabled
   - Help text: "Unique identifier for MQTT topic (store/{screenId})"
   - Pattern: Alphanumeric + hyphens/underscores

**UI/UX Guidelines**:
- Show warning if IOT enabled but no demographic-targeted ads exist
- Display IOT status indicator in screen list (icon or badge)
- Consider adding "Test Connection" button (optional)

##### C. Ad List/Grid View Enhancements

**Visual Indicators**:
- Badge/Icon: Show "🎯 Targeted" or "IOT" badge on ads with demographic targeting enabled
- Filter: Add "Demographic Targeting" filter option
- Sorting: Add sort by "IOT-enabled" option
- Bulk Actions: "Enable/Disable Demographic Targeting" for selected ads

##### D. Analytics/Reporting Enhancements (Future)

- Show demographic match statistics
- Display which demographics each ad is reaching
- Compare demographic vs. non-demographic ad performance

#### 4. API Integration

**Endpoints to Modify/Extend**:

1. **POST/PUT `/api/ads`** - Create/Update Ad
   ```json
   {
     "name": "Product Launch Video",
     "url": "https://...",
     "duration": 30,
     "demographicTargeting": {
       "enableDemographicTargeting": true,
       "targetAgeBrackets": ["20-32", "32-42"],
       "targetGender": "ANY",
       "primaryEmotion": "happy",
       "targetHours": [9, 10, 11, 14, 15, 16],
       "targetEngagementLevel": "standard",
       "minAudienceSize": 1
     }
   }
   ```

2. **POST/PUT `/api/screens`** - Create/Update Screen
   ```json
   {
     "name": "Mall Entrance Display",
     "location": "Main Entrance",
     "iotEnabled": true,
     "iotScreenId": "demo959"
   }
   ```

3. **GET `/api/ads`** - List Ads
   - Add query params: `?demographicEnabled=true`
   - Return demographic targeting fields in response

4. **GET `/api/screens`** - List Screens
   - Return IOT configuration in response

**Backend Coordination**:
- Ensure backend API supports these new fields
- Handle null/undefined gracefully (backward compatibility)
- Validate field values (age brackets, hours 0-23, etc.)

#### 5. Database Schema Changes (Backend)

**Ads Table**:
```sql
ALTER TABLE ads ADD COLUMN target_age_brackets JSON;  -- ["20-32", "32-42"]
ALTER TABLE ads ADD COLUMN target_gender VARCHAR(10);  -- 'M', 'F', 'ANY'
ALTER TABLE ads ADD COLUMN target_emotions JSON;  -- ["happy", "neutral"]
ALTER TABLE ads ADD COLUMN primary_emotion VARCHAR(20);  -- 'happy', 'neutral', 'sad'
ALTER TABLE ads ADD COLUMN target_hours JSON;  -- [9, 10, 11, 14, 15]
ALTER TABLE ads ADD COLUMN target_engagement_level VARCHAR(20);  -- 'quick', 'standard', 'high'
ALTER TABLE ads ADD COLUMN min_audience_size INT DEFAULT 1;
ALTER TABLE ads ADD COLUMN demographic_targeting_enabled BOOLEAN DEFAULT FALSE;
```

**Screens Table**:
```sql
ALTER TABLE screens ADD COLUMN iot_enabled BOOLEAN DEFAULT FALSE;
ALTER TABLE screens ADD COLUMN iot_screen_id VARCHAR(100);
```

#### 6. Validation Rules

**Ad Demographic Targeting**:
- `targetAgeBrackets`: Must be array of valid values ["20-32", "32-42", "43-53", "53+"]
- `targetGender`: Must be 'M', 'F', or 'ANY'
- `targetEmotions`: Must be subset of ["happy", "neutral", "sad", "angry", "fear", "surprise", "disgust"]
- `targetHours`: Array of integers 0-23
- `targetEngagementLevel`: Must be 'quick', 'standard', 'high', or null
- `minAudienceSize`: Integer ≥ 1
- If `enableDemographicTargeting` is true, at least one targeting field should be set (warn user)

**Screen IOT Configuration**:
- If `iotEnabled` is true, `iotScreenId` is required
- `iotScreenId`: Alphanumeric + hyphens/underscores, max 100 chars

#### 7. Backward Compatibility Requirements

**Critical**:
- All existing ads without demographic fields must continue to work
- Treat missing/null demographic fields as "disabled"
- Existing screens default to `iotEnabled: false`
- Player app handles both demographic and non-demographic ads in rotation
- Forms should gracefully handle ads created before demographic feature

**Migration Strategy**:
- No data migration needed (new fields are optional)
- Default values ensure backward compatibility
- Existing API calls work unchanged

## Implementation Guidelines

### React Best Practices

1. **Component Structure**:
   ```typescript
   // Create reusable demographic targeting component
   <DemographicTargetingForm
     value={demographicTargeting}
     onChange={handleDemographicChange}
     disabled={!isEnabled}
   />
   ```

2. **State Management**:
   - Use existing state management (Redux, Context, MobX, or whatever is in use)
   - Keep demographic targeting state separate from core ad state
   - Provide clear actions/reducers for updating targeting fields

3. **Form Handling**:
   - Use controlled components
   - Validate on blur and on submit
   - Show inline validation errors
   - Preserve form state on navigation (if applicable)

4. **API Integration**:
   - Use existing HTTP client (axios, fetch, etc.)
   - Handle loading states
   - Show success/error notifications
   - Implement optimistic updates where appropriate

5. **UI Components**:
   - Use existing component library (Material-UI, Ant Design, etc.)
   - Match existing design system
   - Ensure responsive design
   - Maintain accessibility (WCAG compliance)

### Code Style

- Follow existing ESLint/Prettier configuration
- Use TypeScript if project uses it
- Match existing naming conventions (camelCase, PascalCase, etc.)
- Add JSDoc comments for complex logic
- Write unit tests for new components (if project has testing)

### Error Handling

- Validate inputs before API calls
- Show user-friendly error messages
- Handle network failures gracefully
- Implement retry logic for failed requests
- Log errors to console/monitoring service

### Performance Considerations

- Debounce API calls for form inputs
- Lazy load demographic form section (code split)
- Memoize expensive computations
- Optimize re-renders with React.memo, useMemo, useCallback

## Feature Rollout Checklist

### Phase 1: Backend Preparation
- [ ] Add new database columns/fields
- [ ] Update API endpoints to accept/return demographic fields
- [ ] Implement validation on backend
- [ ] Update API documentation/Swagger

### Phase 2: CMS UI Implementation
- [ ] Create `DemographicTargetingForm` component
- [ ] Update Ad create/edit forms
- [ ] Update Screen configuration forms
- [ ] Add visual indicators to ad lists
- [ ] Implement form validation

### Phase 3: Integration Testing
- [ ] Test ad creation with demographic targeting
- [ ] Test ad editing (enable/disable targeting)
- [ ] Test backward compatibility (old ads still work)
- [ ] Test screen IOT configuration
- [ ] Test API integration end-to-end

### Phase 4: Player App Coordination
- [ ] Verify player app receives demographic fields from API
- [ ] Test real-world demographic matching
- [ ] Validate scoring algorithm with CMS-created ads
- [ ] Confirm IOT toggle works as expected

### Phase 5: Production Deployment
- [ ] Deploy backend changes first
- [ ] Deploy CMS changes
- [ ] Update documentation/user guides
- [ ] Train customer support team
- [ ] Monitor for issues

## User Experience Flow Examples

### Creating a Demographic-Targeted Ad

1. User creates new ad (name, media upload, duration, etc.)
2. User expands "Demographic Targeting" section
3. User enables "IOT Demographic Targeting" toggle
4. User selects:
   - Age: Adults (32-42)
   - Gender: Male
   - Emotion: Upbeat/Promotional
   - Hours: 9-11, 14-16 (work hours)
   - Engagement: Standard
   - Min Audience: 1
5. System validates inputs
6. User saves ad
7. System shows "🎯 Targeted" badge on ad in list

### Enabling IOT on a Screen

1. User navigates to Screens management
2. User edits screen configuration
3. User enables "IOT Mode" toggle
4. User enters IOT Screen ID (e.g., "store_001")
5. System validates screen ID format
6. User saves screen
7. System shows "IOT Enabled" badge on screen in list
8. Player app begins receiving MQTT demographic data

## Testing Scenarios

### Manual Testing

1. **Create targeted ad**: Verify all fields save correctly
2. **Edit targeted ad**: Change demographic parameters, verify updates
3. **Disable targeting**: Toggle off, verify ad works as normal ad
4. **Create non-targeted ad**: Verify backward compatibility
5. **Enable screen IOT**: Verify toggle and screen ID save
6. **Disable screen IOT**: Verify player app stops demographic matching
7. **Mixed playlist**: Verify targeted + non-targeted ads play correctly
8. **API validation**: Submit invalid values, verify error messages

### Automated Testing (if applicable)

```typescript
describe('DemographicTargetingForm', () => {
  it('should render all targeting fields when enabled', () => {});
  it('should hide targeting fields when disabled', () => {});
  it('should validate hour range (0-23)', () => {});
  it('should validate age brackets', () => {});
  it('should call onChange with correct data structure', () => {});
});

describe('Ad API Integration', () => {
  it('should create ad with demographic targeting', async () => {});
  it('should update ad demographic targeting', async () => {});
  it('should handle backward compatibility (no targeting)', async () => {});
});
```

## Documentation Updates Needed

1. **User Guide**: Add section on demographic targeting feature
2. **API Docs**: Document new fields and validation rules
3. **Admin Guide**: Explain IOT setup and MQTT configuration
4. **Release Notes**: Announce new feature with examples
5. **FAQ**: Address common questions (e.g., "What if no demographics match?")

## Common Pitfalls to Avoid

1. **Don't break existing ads**: Always handle missing demographic fields
2. **Don't over-complicate UI**: Keep demographic form optional and intuitive
3. **Don't assume backend exists**: Coordinate with backend team first
4. **Don't forget validation**: Validate on both frontend and backend
5. **Don't skip testing**: Test with real player app before production
6. **Don't ignore mobile**: Ensure CMS works on tablets/mobile browsers
7. **Don't hardcode values**: Use constants/enums for age brackets, emotions, etc.

## Reference: Android Player Scoring Algorithm

For context, here's how the player app scores ads:

| Criterion | Points | Description |
|-----------|--------|-------------|
| Hour Match | +15 | Current hour in `targetHours` |
| Gender Match | +10 | Viewer gender matches `targetGender` |
| Age Bracket Match | +12 | Viewer age in `targetAgeBrackets` |
| Emotion: Happy | +20 | Viewer is happy, ad targets happy |
| Emotion: Neutral | +10 | Viewer is neutral, ad targets neutral |
| Emotion: Sad/Angry | +2 | Viewer is sad/angry, ad targets gentle content |
| High Happiness | +15 | Viewer happiness score > 60% |
| High Engagement | +8 | Viewer dwell time > 15s, ad targets high engagement |
| Quick Engagement | +5 | Viewer dwell time ≤ 5s, ad targets quick |
| Multi-Person Audience | +5 | Audience size ≥ 3, ad has min audience size ≥ 3 |

**Selection**: Highest total score wins

This helps you design the CMS UI to match what the player app expects.

## MQTT Integration Details (Reference Only)

The player app connects to:
- Broker: `ssl://api.adjaba.in:8883`
- Topic: `store/{screenId}` (e.g., `store/demo959`)
- Auth: Username/password authentication

CMS doesn't need to implement MQTT - this is handled by player app. CMS only needs to:
1. Allow configuring screen IOT ID
2. Allow ads to specify demographic targeting criteria

## Questions to Ask Before Starting

If you're unsure about the implementation, ask:

1. **State Management**: "What state management library is the CMS using? (Redux, Context, MobX, Zustand, etc.)"
2. **UI Library**: "What UI component library is in use? (Material-UI, Ant Design, Chakra UI, etc.)"
3. **Form Library**: "Is there a form library in use? (Formik, React Hook Form, etc.)"
4. **API Client**: "What HTTP client is used? (axios, fetch, etc.)"
5. **Backend Status**: "Has the backend API been updated to support demographic fields yet?"
6. **Design System**: "Is there a design system or style guide to follow?"
7. **File Structure**: "Can you show me the current ad form component/screen management component?"

## Summary

You're adding demographic-based ad targeting to a React CMS that controls digital signage. The goal is to let users configure ads to target specific age groups, genders, emotions, times of day, and engagement levels. The Android player app will then use real-time MQTT data to select the best-matching ad dynamically.

**Key Priorities**:
1. ✅ Maintain backward compatibility
2. ✅ Follow existing code patterns
3. ✅ Keep UI simple and intuitive
4. ✅ Validate inputs thoroughly
5. ✅ Coordinate with backend team
6. ✅ Test with real player app

Good luck! 🚀

