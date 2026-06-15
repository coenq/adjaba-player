# React CMS - Code Examples for Demographic Targeting

This file contains example React components and code snippets for implementing demographic targeting in the Adjaba CMS.

---

## 📁 File Structure

```
src/
├── components/
│   ├── ads/
│   │   ├── AdForm.jsx (MODIFY)
│   │   ├── AdList.jsx (MODIFY)
│   │   └── demographic/
│   │       ├── DemographicTargetingForm.jsx (NEW)
│   │       ├── AgeBracketSelector.jsx (NEW)
│   │       ├── GenderSelector.jsx (NEW)
│   │       ├── EmotionSelector.jsx (NEW)
│   │       ├── HourSelector.jsx (NEW)
│   │       ├── EngagementLevelSelector.jsx (NEW)
│   │       └── index.js (NEW)
│   └── screens/
│       ├── ScreenForm.jsx (MODIFY)
│       ├── ScreenList.jsx (MODIFY)
│       └── IOTConfiguration.jsx (NEW)
├── types/
│   └── demographics.ts (NEW)
├── constants/
│   └── demographics.js (NEW)
└── utils/
    └── demographicValidation.js (NEW)
```

---

## 1️⃣ TypeScript Types

### `src/types/demographics.ts`

```typescript
export type Gender = 'M' | 'F' | 'ANY';

export type AgeBracket = '20-32' | '32-42' | '43-53' | '53+';

export type Emotion = 
  | 'happy' 
  | 'neutral' 
  | 'sad' 
  | 'angry' 
  | 'fear' 
  | 'surprise' 
  | 'disgust';

export type ContentTone = 'upbeat' | 'neutral' | 'gentle';

export type EngagementLevel = 'quick' | 'standard' | 'high';

export interface AdDemographicTargeting {
  enableDemographicTargeting?: boolean;
  targetAgeBrackets?: AgeBracket[];
  targetGender?: Gender;
  targetEmotions?: Emotion[];
  primaryEmotion?: ContentTone;
  targetHours?: number[];
  targetEngagementLevel?: EngagementLevel | null;
  minAudienceSize?: number;
}

export interface Ad {
  id: string;
  name: string;
  url: string;
  duration: number;
  type: 'image' | 'video' | 'web';
  // ...other existing fields
  demographicTargeting?: AdDemographicTargeting;
}

export interface Screen {
  id: string;
  name: string;
  location: string;
  orientation: 'portrait' | 'landscape';
  // ...other existing fields
  iotEnabled?: boolean;
  iotScreenId?: string;
}
```

---

## 2️⃣ Constants

### `src/constants/demographics.js`

```javascript
export const AGE_BRACKETS = [
  { value: '20-32', label: 'Young Adults (20-32)' },
  { value: '32-42', label: 'Adults (32-42)' },
  { value: '43-53', label: 'Middle Age (43-53)' },
  { value: '53+', label: 'Seniors (53+)' }
];

export const GENDER_OPTIONS = [
  { value: 'ANY', label: 'Any Gender' },
  { value: 'M', label: 'Male' },
  { value: 'F', label: 'Female' }
];

export const CONTENT_TONE_OPTIONS = [
  { 
    value: null, 
    label: 'Any Emotion',
    description: 'No emotion preference'
  },
  { 
    value: 'upbeat', 
    label: 'Upbeat/Promotional',
    description: 'Targets happy, excited viewers (sales, new products)',
    emotion: 'happy'
  },
  { 
    value: 'neutral', 
    label: 'Neutral/Informational',
    description: 'Targets calm, neutral viewers (brand, info)',
    emotion: 'neutral'
  },
  { 
    value: 'gentle', 
    label: 'Gentle/Supportive',
    description: 'Targets sad or frustrated viewers (support, empathy)',
    emotion: 'sad'
  }
];

export const ENGAGEMENT_LEVELS = [
  { value: null, label: 'Any Engagement' },
  { value: 'quick', label: 'Quick Glance (≤5 sec)', description: 'Short ads' },
  { value: 'standard', label: 'Standard (5-15 sec)', description: 'Normal ads' },
  { value: 'high', label: 'High Attention (>15 sec)', description: 'Long-form content' }
];

export const HOUR_BLOCKS = [
  { label: 'Morning', hours: [6, 7, 8, 9, 10, 11] },
  { label: 'Midday', hours: [12, 13, 14] },
  { label: 'Afternoon', hours: [15, 16, 17, 18, 19] },
  { label: 'Night', hours: [20, 21, 22, 23] }
];

export const ALL_HOURS = Array.from({ length: 24 }, (_, i) => i);
```

---

## 3️⃣ Validation Utilities

### `src/utils/demographicValidation.js`

```javascript
import { AGE_BRACKETS, ALL_HOURS } from '../constants/demographics';

export const validateDemographicTargeting = (targeting) => {
  const errors = {};

  if (!targeting || !targeting.enableDemographicTargeting) {
    return { isValid: true, errors: {} };
  }

  // Validate age brackets
  if (targeting.targetAgeBrackets && targeting.targetAgeBrackets.length > 0) {
    const validBrackets = AGE_BRACKETS.map(b => b.value);
    const invalidBrackets = targeting.targetAgeBrackets.filter(
      b => !validBrackets.includes(b)
    );
    if (invalidBrackets.length > 0) {
      errors.targetAgeBrackets = `Invalid age brackets: ${invalidBrackets.join(', ')}`;
    }
  }

  // Validate gender
  if (targeting.targetGender && !['M', 'F', 'ANY'].includes(targeting.targetGender)) {
    errors.targetGender = 'Gender must be M, F, or ANY';
  }

  // Validate hours
  if (targeting.targetHours && targeting.targetHours.length > 0) {
    const invalidHours = targeting.targetHours.filter(h => h < 0 || h > 23);
    if (invalidHours.length > 0) {
      errors.targetHours = `Hours must be between 0-23. Invalid: ${invalidHours.join(', ')}`;
    }
  }

  // Validate engagement level
  if (targeting.targetEngagementLevel) {
    const valid = ['quick', 'standard', 'high'].includes(targeting.targetEngagementLevel);
    if (!valid) {
      errors.targetEngagementLevel = 'Invalid engagement level';
    }
  }

  // Validate min audience size
  if (targeting.minAudienceSize && targeting.minAudienceSize < 1) {
    errors.minAudienceSize = 'Minimum audience size must be at least 1';
  }

  // Warn if enabled but no criteria set
  const hasCriteria = 
    (targeting.targetAgeBrackets && targeting.targetAgeBrackets.length > 0) ||
    (targeting.targetGender && targeting.targetGender !== 'ANY') ||
    targeting.primaryEmotion ||
    (targeting.targetHours && targeting.targetHours.length > 0) ||
    targeting.targetEngagementLevel ||
    (targeting.minAudienceSize && targeting.minAudienceSize > 1);

  if (!hasCriteria) {
    errors.general = 'Demographic targeting is enabled but no criteria are set';
  }

  return {
    isValid: Object.keys(errors).length === 0,
    errors
  };
};
```

---

## 4️⃣ Main Component: DemographicTargetingForm

### `src/components/ads/demographic/DemographicTargetingForm.jsx`

```jsx
import React, { useState } from 'react';
import {
  Box,
  Card,
  CardContent,
  CardHeader,
  Collapse,
  FormControlLabel,
  Switch,
  Typography,
  Alert
} from '@mui/material';
import { ExpandMore as ExpandMoreIcon } from '@mui/icons-material';
import AgeBracketSelector from './AgeBracketSelector';
import GenderSelector from './GenderSelector';
import EmotionSelector from './EmotionSelector';
import HourSelector from './HourSelector';
import EngagementLevelSelector from './EngagementLevelSelector';
import { validateDemographicTargeting } from '../../../utils/demographicValidation';

const DemographicTargetingForm = ({ value, onChange, disabled = false }) => {
  const [expanded, setExpanded] = useState(
    value?.enableDemographicTargeting || false
  );

  const handleToggle = (event) => {
    const enabled = event.target.checked;
    onChange({
      ...value,
      enableDemographicTargeting: enabled
    });
    setExpanded(enabled);
  };

  const handleFieldChange = (field) => (newValue) => {
    onChange({
      ...value,
      [field]: newValue
    });
  };

  const validation = validateDemographicTargeting(value);

  return (
    <Card variant="outlined" sx={{ mt: 2 }}>
      <CardHeader
        avatar={<span>🎯</span>}
        title="Demographic Targeting"
        subheader="Target ads to specific viewer demographics using IOT data"
        action={
          <FormControlLabel
            control={
              <Switch
                checked={value?.enableDemographicTargeting || false}
                onChange={handleToggle}
                disabled={disabled}
                color="primary"
              />
            }
            label="Enable"
          />
        }
      />
      
      <Collapse in={expanded}>
        <CardContent>
          {!validation.isValid && (
            <Alert severity="warning" sx={{ mb: 2 }}>
              {validation.errors.general || 'Please check your targeting criteria'}
            </Alert>
          )}

          {/* Age Brackets */}
          <AgeBracketSelector
            value={value?.targetAgeBrackets || []}
            onChange={handleFieldChange('targetAgeBrackets')}
            disabled={disabled}
            error={validation.errors.targetAgeBrackets}
          />

          {/* Gender */}
          <GenderSelector
            value={value?.targetGender || 'ANY'}
            onChange={handleFieldChange('targetGender')}
            disabled={disabled}
            error={validation.errors.targetGender}
          />

          {/* Emotion/Content Tone */}
          <EmotionSelector
            value={value?.primaryEmotion || null}
            onChange={handleFieldChange('primaryEmotion')}
            disabled={disabled}
          />

          {/* Target Hours */}
          <HourSelector
            value={value?.targetHours || []}
            onChange={handleFieldChange('targetHours')}
            disabled={disabled}
            error={validation.errors.targetHours}
          />

          {/* Engagement Level */}
          <EngagementLevelSelector
            value={value?.targetEngagementLevel || null}
            onChange={handleFieldChange('targetEngagementLevel')}
            disabled={disabled}
            error={validation.errors.targetEngagementLevel}
          />

          {/* Min Audience Size */}
          <Box sx={{ mt: 3 }}>
            <Typography variant="subtitle2" gutterBottom>
              👥 Minimum Audience Size
            </Typography>
            <TextField
              type="number"
              value={value?.minAudienceSize || 1}
              onChange={(e) => handleFieldChange('minAudienceSize')(parseInt(e.target.value) || 1)}
              disabled={disabled}
              inputProps={{ min: 1, max: 20 }}
              helperText="Ad scores higher when this many people are watching (e.g., 3 for group-appeal ads)"
              error={!!validation.errors.minAudienceSize}
              fullWidth
              size="small"
            />
          </Box>

          {/* Info Box */}
          <Alert severity="info" sx={{ mt: 3 }}>
            <Typography variant="body2">
              <strong>How it works:</strong> When IOT is enabled on a screen, 
              the player will receive real-time viewer demographics and select ads 
              that best match the current audience.
            </Typography>
          </Alert>
        </CardContent>
      </Collapse>
    </Card>
  );
};

export default DemographicTargetingForm;
```

---

## 5️⃣ Sub-Components

### `src/components/ads/demographic/AgeBracketSelector.jsx`

```jsx
import React from 'react';
import {
  FormGroup,
  FormControlLabel,
  Checkbox,
  Typography,
  FormHelperText,
  Box
} from '@mui/material';
import { AGE_BRACKETS } from '../../../constants/demographics';

const AgeBracketSelector = ({ value = [], onChange, disabled = false, error }) => {
  const handleToggle = (bracket) => {
    const newValue = value.includes(bracket)
      ? value.filter(b => b !== bracket)
      : [...value, bracket];
    onChange(newValue);
  };

  return (
    <Box sx={{ mb: 3 }}>
      <Typography variant="subtitle2" gutterBottom>
        🎯 Target Age Brackets
      </Typography>
      <FormGroup>
        {AGE_BRACKETS.map((bracket) => (
          <FormControlLabel
            key={bracket.value}
            control={
              <Checkbox
                checked={value.includes(bracket.value)}
                onChange={() => handleToggle(bracket.value)}
                disabled={disabled}
              />
            }
            label={bracket.label}
          />
        ))}
      </FormGroup>
      {error && <FormHelperText error>{error}</FormHelperText>}
      {!error && (
        <FormHelperText>
          Ad will score higher when viewers match these age ranges
        </FormHelperText>
      )}
    </Box>
  );
};

export default AgeBracketSelector;
```

### `src/components/ads/demographic/GenderSelector.jsx`

```jsx
import React from 'react';
import {
  FormControl,
  FormLabel,
  RadioGroup,
  FormControlLabel,
  Radio,
  FormHelperText,
  Box
} from '@mui/material';
import { GENDER_OPTIONS } from '../../../constants/demographics';

const GenderSelector = ({ value = 'ANY', onChange, disabled = false, error }) => {
  return (
    <Box sx={{ mb: 3 }}>
      <FormControl component="fieldset" error={!!error} disabled={disabled}>
        <FormLabel component="legend">👤 Target Gender</FormLabel>
        <RadioGroup
          value={value}
          onChange={(e) => onChange(e.target.value)}
        >
          {GENDER_OPTIONS.map((option) => (
            <FormControlLabel
              key={option.value}
              value={option.value}
              control={<Radio />}
              label={option.label}
            />
          ))}
        </RadioGroup>
        {error && <FormHelperText>{error}</FormHelperText>}
        {!error && (
          <FormHelperText>
            Ad will score higher for matching gender
          </FormHelperText>
        )}
      </FormControl>
    </Box>
  );
};

export default GenderSelector;
```

### `src/components/ads/demographic/EmotionSelector.jsx`

```jsx
import React from 'react';
import {
  FormControl,
  FormLabel,
  RadioGroup,
  FormControlLabel,
  Radio,
  FormHelperText,
  Box,
  Typography
} from '@mui/material';
import { CONTENT_TONE_OPTIONS } from '../../../constants/demographics';

const EmotionSelector = ({ value = null, onChange, disabled = false }) => {
  return (
    <Box sx={{ mb: 3 }}>
      <FormControl component="fieldset" disabled={disabled} fullWidth>
        <FormLabel component="legend">😊 Content Tone</FormLabel>
        <RadioGroup
          value={value || ''}
          onChange={(e) => onChange(e.target.value || null)}
        >
          {CONTENT_TONE_OPTIONS.map((option) => (
            <FormControlLabel
              key={option.value || 'any'}
              value={option.value || ''}
              control={<Radio />}
              label={
                <Box>
                  <Typography variant="body2">{option.label}</Typography>
                  <Typography variant="caption" color="text.secondary">
                    {option.description}
                  </Typography>
                </Box>
              }
            />
          ))}
        </RadioGroup>
        <FormHelperText>
          Ad will score higher when viewer emotion matches content tone
        </FormHelperText>
      </FormControl>
    </Box>
  );
};

export default EmotionSelector;
```

### `src/components/ads/demographic/HourSelector.jsx`

```jsx
import React from 'react';
import {
  Box,
  Typography,
  Chip,
  FormHelperText,
  Button,
  Stack
} from '@mui/material';
import { HOUR_BLOCKS, ALL_HOURS } from '../../../constants/demographics';

const HourSelector = ({ value = [], onChange, disabled = false, error }) => {
  const handleToggleHour = (hour) => {
    const newValue = value.includes(hour)
      ? value.filter(h => h !== hour)
      : [...value, hour].sort((a, b) => a - b);
    onChange(newValue);
  };

  const handleToggleBlock = (hours) => {
    const allSelected = hours.every(h => value.includes(h));
    if (allSelected) {
      onChange(value.filter(h => !hours.includes(h)));
    } else {
      onChange([...new Set([...value, ...hours])].sort((a, b) => a - b));
    }
  };

  const formatHour = (hour) => {
    const ampm = hour >= 12 ? 'PM' : 'AM';
    const displayHour = hour === 0 ? 12 : hour > 12 ? hour - 12 : hour;
    return `${displayHour}${ampm}`;
  };

  return (
    <Box sx={{ mb: 3 }}>
      <Typography variant="subtitle2" gutterBottom>
        ⏰ Target Hours
      </Typography>
      
      {/* Quick Select Buttons */}
      <Stack direction="row" spacing={1} sx={{ mb: 2 }}>
        {HOUR_BLOCKS.map((block) => {
          const allSelected = block.hours.every(h => value.includes(h));
          return (
            <Button
              key={block.label}
              size="small"
              variant={allSelected ? 'contained' : 'outlined'}
              onClick={() => handleToggleBlock(block.hours)}
              disabled={disabled}
            >
              {block.label}
            </Button>
          );
        })}
        <Button
          size="small"
          variant="outlined"
          onClick={() => onChange([])}
          disabled={disabled || value.length === 0}
        >
          Clear
        </Button>
      </Stack>

      {/* Hour Chips */}
      <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
        {ALL_HOURS.map((hour) => (
          <Chip
            key={hour}
            label={formatHour(hour)}
            onClick={() => handleToggleHour(hour)}
            color={value.includes(hour) ? 'primary' : 'default'}
            variant={value.includes(hour) ? 'filled' : 'outlined'}
            disabled={disabled}
            size="small"
          />
        ))}
      </Box>

      {error && <FormHelperText error>{error}</FormHelperText>}
      {!error && (
        <FormHelperText>
          Ad will score higher during selected hours (24-hour format)
        </FormHelperText>
      )}
    </Box>
  );
};

export default HourSelector;
```

### `src/components/ads/demographic/EngagementLevelSelector.jsx`

```jsx
import React from 'react';
import {
  FormControl,
  FormLabel,
  RadioGroup,
  FormControlLabel,
  Radio,
  FormHelperText,
  Box,
  Typography
} from '@mui/material';
import { ENGAGEMENT_LEVELS } from '../../../constants/demographics';

const EngagementLevelSelector = ({ value = null, onChange, disabled = false, error }) => {
  return (
    <Box sx={{ mb: 3 }}>
      <FormControl component="fieldset" error={!!error} disabled={disabled} fullWidth>
        <FormLabel component="legend">⏳ Viewer Engagement Level</FormLabel>
        <RadioGroup
          value={value || ''}
          onChange={(e) => onChange(e.target.value || null)}
        >
          {ENGAGEMENT_LEVELS.map((option) => (
            <FormControlLabel
              key={option.value || 'any'}
              value={option.value || ''}
              control={<Radio />}
              label={
                <Box>
                  <Typography variant="body2">{option.label}</Typography>
                  {option.description && (
                    <Typography variant="caption" color="text.secondary">
                      {option.description}
                    </Typography>
                  )}
                </Box>
              }
            />
          ))}
        </RadioGroup>
        {error && <FormHelperText>{error}</FormHelperText>}
        {!error && (
          <FormHelperText>
            Match ad length to viewer dwell time
          </FormHelperText>
        )}
      </FormControl>
    </Box>
  );
};

export default EngagementLevelSelector;
```

---

## 6️⃣ IOT Configuration Component

### `src/components/screens/IOTConfiguration.jsx`

```jsx
import React from 'react';
import {
  Box,
  Card,
  CardContent,
  CardHeader,
  FormControlLabel,
  Switch,
  TextField,
  Typography,
  Alert
} from '@mui/material';

const IOTConfiguration = ({ value, onChange, disabled = false }) => {
  const iotEnabled = value?.iotEnabled || false;
  const iotScreenId = value?.iotScreenId || '';

  const handleToggle = (event) => {
    onChange({
      ...value,
      iotEnabled: event.target.checked,
      iotScreenId: event.target.checked ? iotScreenId : ''
    });
  };

  const handleScreenIdChange = (event) => {
    onChange({
      ...value,
      iotScreenId: event.target.value
    });
  };

  const hasError = iotEnabled && !iotScreenId.trim();

  return (
    <Card variant="outlined" sx={{ mt: 2 }}>
      <CardHeader
        avatar={<span>📡</span>}
        title="IOT Integration"
        subheader="Enable real-time demographic ad targeting"
        action={
          <FormControlLabel
            control={
              <Switch
                checked={iotEnabled}
                onChange={handleToggle}
                disabled={disabled}
                color="primary"
              />
            }
            label="Enable IOT"
          />
        }
      />
      
      {iotEnabled && (
        <CardContent>
          <TextField
            label="IOT Screen ID"
            value={iotScreenId}
            onChange={handleScreenIdChange}
            disabled={disabled}
            error={hasError}
            helperText={
              hasError
                ? 'Screen ID is required when IOT is enabled'
                : 'Unique identifier for MQTT topic (e.g., demo959, store_001)'
            }
            placeholder="demo959"
            fullWidth
            required
            inputProps={{
              pattern: '[a-zA-Z0-9_-]+',
              maxLength: 100
            }}
          />

          <Alert severity="info" sx={{ mt: 2 }}>
            <Typography variant="body2">
              <strong>MQTT Topic:</strong> store/{iotScreenId || '{screenId}'}
            </Typography>
            <Typography variant="body2" sx={{ mt: 1 }}>
              The player will receive real-time audience analytics via MQTT 
              and dynamically select ads matching viewer demographics.
            </Typography>
          </Alert>
        </CardContent>
      )}
    </Card>
  );
};

export default IOTConfiguration;
```

---

## 7️⃣ Integrating into Ad Form

### `src/components/ads/AdForm.jsx` (Modifications)

```jsx
import React, { useState } from 'react';
import DemographicTargetingForm from './demographic/DemographicTargetingForm';

const AdForm = ({ initialData, onSubmit }) => {
  const [formData, setFormData] = useState({
    name: initialData?.name || '',
    url: initialData?.url || '',
    duration: initialData?.duration || 30,
    type: initialData?.type || 'video',
    // ...other fields
    
    // NEW: Demographic targeting
    demographicTargeting: initialData?.demographicTargeting || {
      enableDemographicTargeting: false,
      targetAgeBrackets: [],
      targetGender: 'ANY',
      primaryEmotion: null,
      targetHours: [],
      targetEngagementLevel: null,
      minAudienceSize: 1
    }
  });

  const handleDemographicChange = (newTargeting) => {
    setFormData({
      ...formData,
      demographicTargeting: newTargeting
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    // Clean up demographic data if disabled
    const submitData = {
      ...formData,
      demographicTargeting: formData.demographicTargeting.enableDemographicTargeting
        ? formData.demographicTargeting
        : null
    };
    
    await onSubmit(submitData);
  };

  return (
    <form onSubmit={handleSubmit}>
      {/* Existing form fields */}
      <TextField label="Name" value={formData.name} {...} />
      <TextField label="URL" value={formData.url} {...} />
      {/* ... */}

      {/* NEW: Demographic Targeting Section */}
      <DemographicTargetingForm
        value={formData.demographicTargeting}
        onChange={handleDemographicChange}
      />

      <Button type="submit" variant="contained">
        {initialData ? 'Update Ad' : 'Create Ad'}
      </Button>
    </form>
  );
};

export default AdForm;
```

---

## 8️⃣ Integrating into Screen Form

### `src/components/screens/ScreenForm.jsx` (Modifications)

```jsx
import React, { useState } from 'react';
import IOTConfiguration from './IOTConfiguration';

const ScreenForm = ({ initialData, onSubmit }) => {
  const [formData, setFormData] = useState({
    name: initialData?.name || '',
    location: initialData?.location || '',
    orientation: initialData?.orientation || 'landscape',
    // ...other fields
    
    // NEW: IOT configuration
    iotEnabled: initialData?.iotEnabled || false,
    iotScreenId: initialData?.iotScreenId || ''
  });

  const handleIOTChange = (newIOTConfig) => {
    setFormData({
      ...formData,
      ...newIOTConfig
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    // Validate IOT configuration
    if (formData.iotEnabled && !formData.iotScreenId.trim()) {
      alert('IOT Screen ID is required when IOT is enabled');
      return;
    }
    
    await onSubmit(formData);
  };

  return (
    <form onSubmit={handleSubmit}>
      {/* Existing form fields */}
      <TextField label="Name" value={formData.name} {...} />
      <TextField label="Location" value={formData.location} {...} />
      {/* ... */}

      {/* NEW: IOT Configuration Section */}
      <IOTConfiguration
        value={{
          iotEnabled: formData.iotEnabled,
          iotScreenId: formData.iotScreenId
        }}
        onChange={handleIOTChange}
      />

      <Button type="submit" variant="contained">
        {initialData ? 'Update Screen' : 'Create Screen'}
      </Button>
    </form>
  );
};

export default ScreenForm;
```

---

## 9️⃣ Visual Indicators in Lists

### Ad List Badge Component

```jsx
const AdTargetingBadge = ({ ad }) => {
  if (!ad.demographicTargeting?.enableDemographicTargeting) {
    return null;
  }

  return (
    <Chip
      label="🎯 Targeted"
      size="small"
      color="primary"
      variant="outlined"
      sx={{ ml: 1 }}
    />
  );
};

// In AdList component
<Typography variant="h6">
  {ad.name}
  <AdTargetingBadge ad={ad} />
</Typography>
```

### Screen IOT Badge Component

```jsx
const ScreenIOTBadge = ({ screen }) => {
  if (!screen.iotEnabled) {
    return null;
  }

  return (
    <Chip
      label="📡 IOT"
      size="small"
      color="success"
      variant="outlined"
      sx={{ ml: 1 }}
    />
  );
};

// In ScreenList component
<Typography variant="h6">
  {screen.name}
  <ScreenIOTBadge screen={screen} />
</Typography>
```

---

## 🔟 API Integration

### Example API Service

```javascript
// src/services/api.js

import axios from 'axios';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'https://api.adjaba.in';

export const adService = {
  // Create ad with demographic targeting
  createAd: async (adData) => {
    const response = await axios.post(`${API_BASE_URL}/api/ads`, adData);
    return response.data;
  },

  // Update ad
  updateAd: async (adId, adData) => {
    const response = await axios.put(`${API_BASE_URL}/api/ads/${adId}`, adData);
    return response.data;
  },

  // Get ads (with optional demographic filter)
  getAds: async (filters = {}) => {
    const params = new URLSearchParams();
    if (filters.demographicEnabled !== undefined) {
      params.append('demographicEnabled', filters.demographicEnabled);
    }
    const response = await axios.get(`${API_BASE_URL}/api/ads?${params}`);
    return response.data;
  }
};

export const screenService = {
  // Create screen with IOT config
  createScreen: async (screenData) => {
    const response = await axios.post(`${API_BASE_URL}/api/screens`, screenData);
    return response.data;
  },

  // Update screen
  updateScreen: async (screenId, screenData) => {
    const response = await axios.put(`${API_BASE_URL}/api/screens/${screenId}`, screenData);
    return response.data;
  },

  // Get screens
  getScreens: async (filters = {}) => {
    const params = new URLSearchParams();
    if (filters.iotEnabled !== undefined) {
      params.append('iotEnabled', filters.iotEnabled);
    }
    const response = await axios.get(`${API_BASE_URL}/api/screens?${params}`);
    return response.data;
  }
};
```

---

## 1️⃣1️⃣ Complete Example: Creating a Targeted Ad

```jsx
import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { adService } from '../../services/api';
import AdForm from './AdForm';
import { Snackbar, Alert } from '@mui/material';

const CreateAdPage = () => {
  const navigate = useNavigate();
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(false);

  const handleSubmit = async (adData) => {
    try {
      await adService.createAd(adData);
      setSuccess(true);
      setTimeout(() => navigate('/ads'), 2000);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to create ad');
    }
  };

  return (
    <div>
      <h1>Create New Ad</h1>
      <AdForm onSubmit={handleSubmit} />
      
      <Snackbar open={!!error} autoHideDuration={6000} onClose={() => setError(null)}>
        <Alert severity="error">{error}</Alert>
      </Snackbar>
      
      <Snackbar open={success} autoHideDuration={2000}>
        <Alert severity="success">Ad created successfully!</Alert>
      </Snackbar>
    </div>
  );
};

export default CreateAdPage;
```

---

## 📝 Notes

1. **Material-UI**: These examples use Material-UI v5. Adjust imports if using different UI framework.

2. **State Management**: Examples use local state. Adapt for Redux/MobX/Context as needed.

3. **Styling**: Adjust `sx` props to match your design system.

4. **Validation**: Add more robust validation as needed for your use case.

5. **Accessibility**: Ensure proper ARIA labels and keyboard navigation.

6. **Testing**: Add unit tests for each component and integration tests for forms.

---

## 🚀 Quick Start

1. Copy constants file: `src/constants/demographics.js`
2. Copy types file (if TypeScript): `src/types/demographics.ts`
3. Copy validation utils: `src/utils/demographicValidation.js`
4. Create demographic components folder
5. Copy all sub-components
6. Integrate `DemographicTargetingForm` into `AdForm`
7. Integrate `IOTConfiguration` into `ScreenForm`
8. Update API service
9. Test!

---

**Last Updated**: May 31, 2026

