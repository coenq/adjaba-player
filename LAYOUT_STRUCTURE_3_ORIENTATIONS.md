# 📐 Layout Structure: News & Weather Screens Across 3 Orientations

## 🎯 Overview

The app implements **NEWS** and **WEATHER** screens with distinct layouts for **3 different orientations**:

1. **PORTRAIT** (Default)
2. **LANDSCAPE** 
3. **FORCED PORTRAIT** (Variant within portrait configuration)

Both use **ConstraintLayout** as the base and share all view IDs for Java compatibility.

---

## 🌡️ WEATHER SCREEN LAYOUTS

### 1️⃣ PORTRAIT (Default) — Vertical Cinematic Stack

**File**: `app/src/main/res/layout/fragment_advert_watching.xml` (Lines 54-463)

**Structure**: Premium vertical cascade from top to bottom

```
┌──────────────────────────────────────────┐
│ HEADER [0-9% of height]                  │
│ ┌────────────────────────────────────┐   │
│ │ 📍 Location | Red Accent Line      │   │  ← Location + Pin + 4dp Red Bar
│ └────────────────────────────────────┘   │
├──────────────────────────────────────────┤
│ TOP SECTION [9-27% of height]            │
│          🕐 15:05                        │  ← Time (42sp, sans-serif-black)
│        THU, 15 MAY                       │  ← Date (18sp, sans-serif-light)
├──────────────────────────────────────────┤
│ MIDDLE SECTION [27-68% of height]        │
│                                          │
│              🌤️ ✨                      │  ← Weather Icon (240x240) + Glow
│            24°C                          │  ← Temperature (60sp, bold)
│          PARTLY CLOUDY                   │  ← Condition (15sp, light)
│                                          │
├──────────────────────────────────────────┤
│ BOTTOM SECTION [68-100% of height]       │
│ ─────────────────────────────────────    │  ← Thin divider
│     Wind      │    Humidity              │
│   📍 32 km/h  │   💧 60%                 │  ← Row 1 (2x1 horizontal)
│               │                          │
│  Feels Like   │    Pressure              │
│   🌡️ 22°     │   🔘 1013 hPa           │  ← Row 2 (2x1 horizontal)
└──────────────────────────────────────────┘
```

#### 📏 Key Measurements (PORTRAIT)

| Section | Height | Content | Typography |
|---------|--------|---------|------------|
| **Header** | 9% | Location + Pin + Red Bar | 12sp (light) |
| **TOP** | 9-27% (18%) | Time + Date Hero | Time: 42sp (bold)<br/>Date: 18sp (light) |
| **MIDDLE** | 27-68% (41%) | Icon + Temp + Condition | Temp: 60sp (bold)<br/>Condition: 15sp (light) |
| **BOTTOM** | 68-100% (32%) | 2x2 Metrics Grid | Values: 16sp (bold)<br/>Labels: 12sp (light) |

#### 📊 Metrics Layout (PORTRAIT) — 2x2 Vertical Grid

```
ROW 1 (50% + 50%):
┌─────────────────────────────────────┐
│  Wind (50%)    │   Humidity (50%)   │
│  📍 32 km/h    │     💧 60%        │
└─────────────────────────────────────┘

ROW 2 (50% + 50%):
┌─────────────────────────────────────┐
│  Feels Like    │   Pressure        │
│  🌡️ 22°       │    🔘 1013 hPa   │
└─────────────────────────────────────┘
```

**Container**: LinearLayout (vertical orientation)
- Padding: 20dp (top/bottom), 24dp (sides)
- Background: tvSurface color
- Gravity: center

---

### 2️⃣ LANDSCAPE — Split Horizontal Layout

**File**: `app/src/main/res/layout-land/fragment_advert_watching.xml` (Lines 50-376)

**Structure**: Left-right split with unified header

```
┌──────────────────────────────────┬──────────────────────────────┐
│ HEADER [0-14% of height]         │                              │
│ ┌──────────────────────────────┐ │                              │
│ │📍 Location | Red Accent Line │ │                              │
│ └──────────────────────────────┘ │                              │
├──────────────────────────────────┼──────────────────────────────┤
│ LEFT SIDE [42% width]            │ RIGHT SIDE [42% width]       │
│       🕐 15:05                   │         🌤️ ✨               │
│     THU, 15 MAY                  │        24°C                  │
│   (Vertically centered)          │      PARTLY CLOUDY           │
│                                  │   (Vertically centered)      │
├──────────────────────────────────┼──────────────────────────────┤
│ METRICS STRIP [22% of height]    │                              │
│ Wind | Humidity | Feels Like     │ (Fills full width)           │
│ 32 km/h │ 60% │ 22°             │                              │
└──────────────────────────────────┴──────────────────────────────┘
```

#### 📏 Key Measurements (LANDSCAPE)

| Section | Dimension | Content | Typography |
|---------|-----------|---------|------------|
| **Header** | 14% height | Location + Pin + Red Bar | 12sp (medium) |
| **LEFT** | 42% width | Time + Date (vertical stack) | Time: 42sp (bold)<br/>Date: 16sp (medium) |
| **RIGHT** | 42% width | Icon + Temp (inline) + Condition | Temp: 40sp (bold)<br/>Condition: 14sp (medium) |
| **CENTER** | 1dp width | Subtle vertical divider | N/A |
| **METRICS** | 22% height | 3 columns (horizontal) | Values: 16sp (bold)<br/>Labels: 12sp (light) |

#### 📊 Metrics Layout (LANDSCAPE) — Horizontal Strip with Dividers

```
┌─────────────────┬─────────────────┬─────────────────┐
│     Wind        │    Humidity     │   Feels Like    │
│ 📍 32            │ 💧 60           │ 🌡️ 22         │
│    km/h         │      %          │      °          │
├─────────────────┼─────────────────┼─────────────────┤
│ (layout_weight  │ (layout_weight  │ (layout_weight  │
│  = 1)           │  = 1)           │  = 1)           │
└─────────────────┴─────────────────┴─────────────────┘
    with 1dp vertical dividers between columns
```

**Container**: LinearLayout (horizontal orientation)
- Background: tvSurface color
- Gravity: center
- All columns equal width (layout_weight=1)

---

### 3️⃣ FORCED PORTRAIT — Special Configuration

**Context**: The app detects and handles forced portrait mode through:
- Resource qualifiers: `layout-port/` (if needed)
- Activity configuration: `android:screenOrientation="portrait"`
- Java logic: Orientation detection + forced rendering

**Current Status**: Uses the same **PORTRAIT layout** (layout/fragment_advert_watching.xml) with:
- No rotation allowed
- Always renders as vertical cinematic stack
- All portrait measurements apply (9%, 27%, 68% guidelines)

**Use Case**: Ensure consistent vertical display on devices configured for portrait-only mode.

---

## 📰 NEWS SCREEN LAYOUTS

### 1️⃣ PORTRAIT (Default) — Vertical Hero Stack

**File**: `app/src/main/res/layout/fragment_advert_watching.xml` (Lines 473-635)

**Structure**: Hero image dominating top, text below

```
┌────────────────────────────────────┐
│ HERO IMAGE [0-45% of height]       │
│                                    │
│  🖼️ Full-bleed image              │
│     (centerCrop fill)              │
│                                    │
│  ∰∰∰ Gradient Overlay ∰∰∰         │
│  📰 NEWS (badge, top-left)        │
│                                    │
├────────────────────────────────────┤
│ HEADLINE [45-60% of height]        │
│ "Breaking news headline spans      │
│  2-3 lines maximum here"           │
│ (42sp, bold, white, centered)      │
├────────────────────────────────────┤
│ DESCRIPTION [60-100% of height]    │
│ ─────────────────────────────────  │ ← Red accent line
│ News summary and description       │
│ text goes here spanning 3-5        │
│ lines with 1.5 line spacing        │
│ (18sp, light gray, centered)       │
│                                    │
└────────────────────────────────────┘
```

#### 📏 Key Measurements (PORTRAIT)

| Section | Height | Content | Typography |
|---------|--------|---------|------------|
| **HERO** | 0-45% (45%) | Full-bleed image + gradient overlay | N/A |
| **BADGE** | Within hero | "NEWS" label top-left corner | 12sp (bold) |
| **HEADLINE** | 45-60% (15%) | Main story title (2-3 lines max) | 42sp (bold, white) |
| **DESCRIPTION** | 60-100% (40%) | Story summary (3-5 lines) | 18sp (light gray) |

#### 🎨 Visual Elements (PORTRAIT)

- **Hero Image**: 100% width, 45% height, centerCrop
- **Gradient Overlay**: Dark overlay at bottom 35% of hero (readability)
- **NEWS Badge**: Red background, top-left corner (40dp margin)
- **Headline**: Centered, 2-3 line max, with 40dp horizontal padding
- **Description Container**: Black background, red accent line (48dp) above text
- **Line Spacing**: Headline 1.2x, Description 1.5x

---

### 2️⃣ LANDSCAPE — Split Panel Layout

**File**: `app/src/main/res/layout-land/fragment_advert_watching.xml` (Lines 381-519)

**Structure**: Left image panel + right description panel

```
┌──────────────────────────────┬──────────────────────────────┐
│ LEFT PANEL [50% width]       │ RIGHT PANEL [50% width]      │
│                              │                              │
│  🖼️ Hero Image + Gradient   │  Description Section         │
│                              │                              │
│ ┌────────────────────────┐   │  ─────────────────────────  │
│ │ 📰 NEWS (badge)       │   │  "Breaking news headline"    │
│ │ (top-left)            │   │   (2 lines max)              │
│ │                        │   │                              │
│ │  ∰∰∰ Gradient ∰∰∰     │   │  Red accent line             │
│ │  "Breaking news        │   │  ─────────────────────────  │
│ │   headline"            │   │                              │
│ │  (bottom, over image)  │   │  News description spanning   │
│ └────────────────────────┘   │  up to 6 lines with good    │
│                              │  readability and padding    │
│  ▓▓ 2dp RED DIVIDER ▓▓      │                              │
└──────────────────────────────┴──────────────────────────────┘
```

#### 📏 Key Measurements (LANDSCAPE)

| Section | Dimension | Content | Typography |
|---------|-----------|---------|------------|
| **LEFT PANEL** | 50% width | Full-height image + headline overlay | Headline: varies |
| **HERO IMAGE** | 100% | Full-bleed image (centerCrop) | N/A |
| **HEADLINE** | Bottom 45% | Main title (2 lines max) in gradient area | Bold (news_headline) |
| **DIVIDER** | 2dp width | Red accent between panels | N/A |
| **RIGHT PANEL** | 50% width | Description + accent line + padding | N/A |
| **DESCRIPTION** | Full height | Story summary (6 lines max) | 14sp (light gray) |

#### 🎨 Visual Elements (LANDSCAPE)

- **Left Image**: Full height, centered-crop
- **Gradient Overlay**: Bottom 45% of image (text readability)
- **NEWS Badge**: Top-left corner (16dp margin)
- **Headline**: Pinned to bottom, 2 lines max, 16dp bottom margin
- **RED Divider**: 2dp width, full height, separates panels
- **Description Container**: 
  - Background: tvSurface color
  - Gravity: center_vertical
  - Padding: 24dp (top/bottom), 16dp (sides)
  - Red accent line: 32dp × 3dp (top of description)

---

### 3️⃣ FORCED PORTRAIT — Same as Portrait Default

**Implementation**: Uses the same **PORTRAIT layout** (layout/fragment_advert_watching.xml) with:
- Vertical hero stack (45% top image)
- Centered headline (2-3 lines)
- Bottom description (3-5 lines)
- All portrait measurements apply

**Consistency**: Guaranteed vertical layout, no rotation artifacts.

---

## 📁 File Structure Summary

### Layout Files

| File | Purpose | Orientations |
|------|---------|--------------|
| `layout/fragment_advert_watching.xml` | Default (portrait) | Portrait + Forced Portrait |
| `layout-land/fragment_advert_watching.xml` | Landscape-specific | Landscape only |

### Resource Qualifiers Used

| Directory | Usage |
|-----------|-------|
| `layout/` | Default (portrait) layouts |
| `layout-land/` | Landscape-specific layouts |
| `layout-port/` | Forced portrait (optional, not currently used) |
| `values-port/` | Dimen/color overrides for portrait |
| `values-land/` | Dimen/color overrides for landscape |

---

## 🎯 Key Design Differences Across Orientations

### WEATHER Screen

| Aspect | PORTRAIT | LANDSCAPE |
|--------|----------|-----------|
| **Layout Type** | Vertical stack | Horizontal split |
| **Time/Date** | Hero element (top) | Left column |
| **Weather Icon** | Center, large (240x240) | Right, compact (72x72) |
| **Temperature** | Below icon, 60sp | Inline with icon |
| **Metrics** | 2x2 vertical grid | 3-column horizontal row |
| **Space Usage** | Premium: 41% for weather | Compact: fits in right half |
| **Aesthetic** | Cinematic, Netflix-like | Professional, Bloomberg-like |

### NEWS Screen

| Aspect | PORTRAIT | LANDSCAPE |
|--------|----------|-----------|
| **Layout Type** | Vertical stack | Horizontal split |
| **Image** | Top 45%, full width | Left 50%, full height |
| **Headline** | Below image, centered | Over image, bottom, 2 lines |
| **Description** | Large section (40%) | Right 50%, full height |
| **Accent Line** | Above description | Above description |
| **Text Padding** | 40dp horizontal | 16dp on right panel |
| **Aesthetic** | Hero-focused | Balanced left-right |

---

## 🔧 Shared View IDs (Java Compatibility)

Both layouts use identical view IDs for maximum code reusability:

### Weather Views
- `weatherLayout` - Container
- `timeNow`, `dateNow` - Time/Date text
- `currentWeatherImg` - Weather icon
- `weatherTemp` - Temperature value
- `currentStatus` - Condition text
- `windW`, `hamudity`, `rain`, `pressure` - Metric values

### News Views
- `newsLayout` - Container
- `news_img` - Hero image
- `newsTitle` - "NEWS" badge
- `main_header` - Headline text
- `news_details` - Description text
- `header_background` - Gradient overlay
- `shimmer` - Loading shimmer effect

---

## 🎨 Typography & Colors (All Orientations)

### Typography Scale

| Purpose | Size | Font | Style |
|---------|------|------|-------|
| **Time** | 42sp | sans-serif-black | Bold |
| **Date** | 18sp (port) / 16sp (land) | sans-serif-light | Regular |
| **Temperature** | 60sp (port) / 40sp (land) | sans-serif-black | Bold |
| **Condition** | 15sp (port) / 14sp (land) | sans-serif-light | Regular |
| **Metrics** | 16sp | sans-serif-black | Bold |
| **Metric Labels** | 12sp | sans-serif-light | Regular |
| **News Headline** | 42sp (port) / varies (land) | sans-serif-black | Bold |
| **News Description** | 18sp (port) / 14sp (land) | sans-serif-light | Regular |

### Color Scheme

| Element | Color | Usage |
|---------|-------|-------|
| **Primary Text** | #FFFFFF (white) | Headlines, values |
| **Secondary Text** | #B3B3B3 (light gray) | Labels, descriptions |
| **Accent** | #E50914 (Netflix red) | Dividers, badges, highlights |
| **Background** | #000000 (pure black) | Main background |
| **Surface** | tvSurface (slightly lighter) | Metric panels, description areas |

---

## ✅ Verification Checklist

### PORTRAIT Layout
- ✅ Weather: 3-section stack (9%, 27%, 68% guidelines)
- ✅ News: Hero + headline + description (45%, 60% guidelines)
- ✅ 2x2 metrics grid for weather
- ✅ All view IDs match Java code
- ✅ Cinematic premium aesthetic

### LANDSCAPE Layout
- ✅ Weather: Left-right split (42% guideline)
- ✅ News: Left image + right text (50-50 split)
- ✅ 3-column metrics strip (horizontal)
- ✅ All view IDs match Java code
- ✅ Compact, professional aesthetic

### FORCED PORTRAIT
- ✅ Uses portrait layout (no rotation)
- ✅ Consistent appearance across devices
- ✅ All portrait measurements apply
- ✅ No landscape variants

---

## 📊 Summary Table: 3 Orientations

| Feature | Portrait | Landscape | Forced Portrait |
|---------|----------|-----------|----------------|
| **File** | `layout/` | `layout-land/` | `layout/` |
| **Weather Layout** | Vertical 3-section | Horizontal split | Vertical 3-section |
| **News Layout** | Vertical hero stack | Horizontal split | Vertical hero stack |
| **Primary Usage** | Tablets, portrait phones | TVs, wide displays | Phones in portrait mode |
| **Metrics Arrangement** | 2x2 grid | 3-column row | 2x2 grid |
| **Aesthetic** | Premium, cinematic | Professional, compact | Premium, cinematic |
| **Text Orientation** | Centered vertical | Split horizontal | Centered vertical |

---

**Status**: ✅ COMPLETE  
**Last Updated**: May 15, 2026  
**Contains**: 2 layout files, 3 orientation variants, comprehensive documentation


