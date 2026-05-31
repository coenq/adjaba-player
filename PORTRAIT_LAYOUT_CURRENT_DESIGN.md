# 📱 PORTRAIT LAYOUT - CURRENT DESIGN ANALYSIS

## Current Portrait Layout Structure

The portrait layout (`fragment_advert_watching.xml`) displays two main slides that rotate: Weather and News. Here's exactly how each is currently structured:

---

## ⛈️ WEATHER SLIDE - CURRENT DESIGN

### Visual Layout
```
┌─────────────────────────────────────────┐
│  📍 LONDON                              │ ← Location with red pin icon
│  ════════════════════════════           │    (red accent bar is 4dp × 2dp)
│                                         │
│ 20px padding on all sides               │
│                                         │
│            15:05                        │ ← TIME (Hero - Large, Bold)
│                                         │    Font: sans-serif-black
│                                         │    Size: @dimen/text_time
│                                         │    Color: tvTextPrimary (white)
│                                         │
│         THU, 15 MAY                     │ ← DATE (UPPERCASE)
│                                         │    Font: sans-serif-medium
│                                         │    Size: @dimen/text_date
│                                         │    Color: tvTextSecondary
│                                         │
│            24°C                         │ ← TEMPERATURE (Hero - Large, Bold)
│                                         │    Font: sans-serif-black
│                                         │    Size: @dimen/text_temp
│                                         │    Color: tvTextPrimary
│                                         │
│      PARTLY CLOUDY                      │ ← CONDITION (UPPERCASE)
│                                         │    Font: sans-serif-medium
│                                         │    Size: @dimen/text_condition
│                                         │
│ ─────────────────────────────────────── │ ← Thin divider line (1dp)
│ (tvBorder color)                        │
│                                         │
│  🌬 WIND      │  💧 HUMIDITY            │ ← 2x2 METRICS GRID
│  32 km/h      │  60%                    │    Row 1: Wind | Humidity
│  ──────────────────────────────────---- │    (50% width each, side-by-side)
│  🌡 FEELS     │  ⊘ PRESSURE            │    
│  LIKE         │  1013 hPa               │    Row 2: Feels Like | Pressure
│  22°          │                         │    (50% width each, side-by-side)
│                                         │
│ 20px padding on all sides               │
└─────────────────────────────────────────┘
```

### Weather Slide Code Structure
**Lines 48-347 in fragment_advert_watching.xml**

```xml
<LinearLayout id="weatherLayout">                    ← Container
  ├─ LinearLayout (orientation=horizontal)           ← Location row
  │  ├─ ImageView (pin icon)
  │  └─ TextView (location text: "London")
  │
  ├─ View (red bar: 4dp × 2dp)                       ← Red accent separator
  │
  ├─ TextView (time: "15:05")                        ← HERO ELEMENT
  │
  ├─ TextView (date: "THU, 15 MAY")
  │
  ├─ TextView (temp: "24°C")                         ← HERO ELEMENT
  │
  ├─ TextView (condition: "PARTLY CLOUDY")
  │
  ├─ View (thin line divider: 1dp)                   ← Divider
  │
  └─ LinearLayout (orientation=vertical)             ← Metrics Grid Container
     ├─ LinearLayout (orientation=horizontal)        ← ROW 1
     │  ├─ LinearLayout (layout_weight=0.5)          ← Wind Cell
     │  │  ├─ ImageView (wind icon)
     │  │  ├─ TextView (value: "32")
     │  │  └─ TextView (label: "km/h")
     │  │
     │  └─ LinearLayout (layout_weight=0.5)          ← Humidity Cell
     │     ├─ ImageView (humidity icon)
     │     ├─ TextView (value: "60%")
     │     └─ TextView (label: "humidity")
     │
     └─ LinearLayout (orientation=horizontal)        ← ROW 2
        ├─ LinearLayout (layout_weight=0.5)          ← Feels Like Cell
        │  ├─ ImageView (thermostat icon)
        │  ├─ TextView (value: "22°")
        │  └─ TextView (label: "feels like")
        │
        └─ LinearLayout (layout_weight=0.5)          ← Pressure Cell
           ├─ ImageView (pressure icon)
           ├─ TextView (value: "1013")
           └─ TextView (label: "hPa")
```

### Key Design Properties

| Section | Property | Value |
|---------|----------|-------|
| **Container** | Layout | LinearLayout (vertical) |
| | Background | @drawable/bg_weather_slide |
| | Padding | 20dp all sides |
| | Visibility | gone (shown only when weather slide active) |
| **Location** | Font | sans-serif-medium, 0.18 letter spacing |
| | Color | tvTextSecondary |
| | Size | @dimen/text_location |
| **Red Bar** | Dimensions | 4dp wide × 2dp tall |
| | Color | tvAccent (Netflix red #E50914) |
| **Time** | Font | sans-serif-black, Bold |
| | Size | @dimen/text_time (largest) |
| | Gravity | center_horizontal |
| **Temperature** | Font | sans-serif-black, Bold |
| | Size | @dimen/text_temp (large) |
| | Gravity | center_horizontal |
| **Metrics Grid** | Layout | 2 rows × 2 columns |
| | Cell Width | 50% (layout_weight=0.5) |
| | Icons | @dimen/weather_metric_icon_size |
| | Icon Alpha | 0.80 |

---

## 📰 NEWS SLIDE - CURRENT DESIGN

### Visual Layout
```
┌─────────────────────────────────────────┐
│                                         │
│   ┌─ NEWS badge (top-left)              │
│   │  [NEWS] (red background)            │
│   │  Position: 40dp from left & top     │
│   │                                     │
│   │  HERO IMAGE SECTION                 │
│   │  (45% of screen height)             │
│   │                                     │
│   │  Background: actual news image      │
│   │  Overlay: Dark gradient (35% height)│
│   │  Gradient fades from transparent -> │
│   │  dark to ensure text readability    │
│   └─────────────────────────────────────│
│                                         │
│    BREAKING NEWS HEADLINE               │ ← Headline Section
│    THAT SPANS 2-3 LINES                 │    (45%-60% of screen)
│    MAXIMUM 3 LINES                      │
│                                         │ ← Thin space separator (~5%)
│                                         │
│  ────────────────────────────           │ ← Red accent line (48dp × 4dp)
│                                         │
│  News summary text that provides        │ ← Description Section
│  additional context and details about   │    (60%-100% of screen)
│  the news story. Shows up to 5 lines    │    Black background
│  of description content.                │
│                                         │
│                                         │
└─────────────────────────────────────────┘
```

### News Slide Code Structure
**Lines 357-519 in fragment_advert_watching.xml**

```xml
<FrameLayout id="newsLayout">                        ← Container (black bg)
  ├─ ShimmerFrameLayout (placeholder while loading)
  │
  └─ ConstraintLayout id="newsFrame"                 ← Main content
     ├─ Guideline (hero_section_end: 45%)            ← Section marker
     │
     ├─ ImageView id="news_img"                      ← HERO IMAGE
     │  └─ fills 0% to 45% of screen
     │  └─ scaleType: centerCrop
     │
     ├─ View (header_background)                     ← Dark Gradient Overlay
     │  └─ drawable: gradient_news_overlay
     │  └─ height: 35% of hero section
     │  └─ positioned at bottom of hero (for text readability)
     │
     ├─ TextView id="newsTitle"                      ← NEWS BADGE
     │  └─ Text: "NEWS"
     │  └─ Background: @drawable/bg_news_badge (red)
     │  └─ Position: 40dp from left & top (top-left corner)
     │  └─ Color: white (#FFFFFF)
     │
     ├─ Guideline (headline_section_end: 60%)        ← Section marker
     │
     ├─ TextView id="main_header"                    ← HEADLINE
     │  └─ Position: 45%-60% of screen
     │  └─ Font: sans-serif-black, Bold
     │  └─ Size: 42sp (large)
     │  └─ Color: white (#FFFFFF)
     │  └─ Max lines: 3
     │  └─ Line spacing: 1.2x
     │  └─ Gravity: center, horizontal
     │  └─ Margins: 40dp left/right, 30dp top, 20dp bottom
     │
     └─ LinearLayout id="desc_panel"                 ← DESCRIPTION PANEL
        ├─ Background: black (#000000)
        ├─ Position: 60%-100% of screen
        ├─ Padding: 40dp left/right, 20dp top, 40dp bottom
        │
        ├─ View (accent line)                        ← Red accent line
        │  └─ Dimensions: 48dp × 4dp
        │  └─ Color: tvAccent (red #E50914)
        │
        └─ TextView id="news_details"                ← DESCRIPTION TEXT
           └─ Font: sans-serif-light
           └─ Size: 18sp
           └─ Color: light gray (#B3B3B3)
           └─ Max lines: 5
           └─ Line spacing: 1.5x
           └─ Gravity: center
```

### Key Design Properties

| Section | Property | Value |
|---------|----------|-------|
| **Container** | Background | Pure black (#000000) |
| | Visibility | gone (shown only when news slide active) |
| **Hero Image** | Height | 45% of screen |
| | Scale Type | centerCrop (fills proportionally) |
| **Gradient Overlay** | Height | 35% of hero section |
| | Drawable | @drawable/gradient_news_overlay |
| | Purpose | Ensures white text readable over image |
| **"NEWS" Badge** | Background | @drawable/bg_news_badge |
| | Text Color | White (#FFFFFF) |
| | Font | Bold |
| | Position | Top-left (40dp, 40dp) |
| **Headline** | Font | sans-serif-black, Bold |
| | Size | 42sp (large) |
| | Color | White (#FFFFFF) |
| | Max Lines | 3 |
| | Height | ~15% of screen (45%-60%) |
| **Description  Panel** | Background | Black (#000000) |
| | Height | ~40% of screen (60%-100%) |
| | Padding | 40dp horizontal, 20dp top, 40dp bottom |
| **Red Accent Line** | Dimensions | 48dp × 4dp |
| | Color | tvAccent (#E50914) |
| | Margin | 20dp below it |
| **Description Text** | Font | sans-serif-light |
| | Size | 18sp |
| | Color | Light gray (#B3B3B3) |
| | Max Lines | 5 |
| | Line Spacing | 1.5x |

---

## 🎨 Color Palette Used

| Element | Color | Value | Resource |
|---------|-------|-------|----------|
| Weather Background | Custom drawable | @drawable/bg_weather_slide | (image file) |
| News Background | Black | #000000 | Pure black |
| Text Primary | White/Light | @color/tvTextPrimary | (usually white) |
| Text Secondary | Gray | @color/tvTextSecondary | (lighter gray) |
| Accent (Red) | Netflix Red | @color/tvAccent | (#E50914) |
| Borders | Border Gray | @color/tvBorder | (thin lines) |

---

## 🎯 Animation Overview

**Current Behavior:**
- Weather slide displays as a static vertical stack
- News slide displays as cinematic vertical sections
- Both auto-rotate after configurable duration
- Navigation via D-pad (left/right to switch between slides)

---

## 📋 Summary: Current Portrait Layout

### Weather Slide
✅ **Layout**: LinearLayout (vertical)  
✅ **Flow**: Location → Time → Date → Temp → Condition → Metrics (2x2)  
✅ **Style**: Clean, minimalist, data-focused  
✅ **Colors**: Light background + dark text + red accents  

### News Slide
✅ **Layout**: ConstraintLayout with guidelines  
✅ **Flow**: Hero Image (45%) → Headline (45%-60%) → Description (60%-100%)  
✅ **Style**: Cinematic, Netflix-style, premium feel  
✅ **Colors**: Black background + white headline + gray description + red badge  

---

## 💡 What Would You Like to Change?

Please tell me what new design you'd like for the **Weather** and/or **News** slides in **Portrait** mode:

**Options:**
- Change the layout structure (e.g., side-by-side instead of stacked)?
- Change colors or styling?
- Add/remove elements?
- Rearrange sections?
- Change typography (font sizes, weights)?
- Add new visual elements (gradients, borders, shadows)?

Just describe the desired design and I'll implement it! 🎨

