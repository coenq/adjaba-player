# 🎯 Quick Copy-Paste: Forced Portrait Styling Guide

If you need to apply forced portrait styles to a new element, use these exact values:

## Essential Styling Defaults

```xml
<!-- PRIMARY TEXT (Headlines, Values) -->
android:fontFamily="sans-serif-black"
android:textColor="@color/tvTextPrimary"        <!-- #FFFFFF -->
android:textStyle="bold"
android:includeFontPadding="false"
android:gravity="center"

<!-- SECONDARY TEXT (Labels, Descriptions) -->
android:fontFamily="sans-serif-light"
android:textColor="@color/tvTextSecondary"      <!-- #B3B3B3 -->
android:gravity="center"

<!-- ACCENT/MEDIUM TEXT (Condition, Metrics, Date) -->
android:fontFamily="sans-serif-medium"
android:textColor="@color/tvTextSecondary"      <!-- #B3B3B3 -->

<!-- ACCENT LINES/DIVIDERS -->
android:background="@color/tvAccent"            <!-- #E50914 -->
```

## Typography Tracking Values

```xml
<!-- PREMIUM TRACKING (Headlines, Important) -->
android:letterSpacing="0.20"      <!-- Condition text -->
android:letterSpacing="0.18"      <!-- Date, Location -->
android:letterSpacing="0.05"      <!-- Time (landscape) -->

<!-- NEUTRAL TRACKING (Default) -->
android:letterSpacing="0"         <!-- No tracking -->

<!-- COMPACT TRACKING (Tight/Warm) -->
android:letterSpacing="-0.02"     <!-- Temperature -->
android:letterSpacing="-0.01"     <!-- Tight -->
```

## Line Spacing (News/Description Text)

```xml
<!-- Premium Headlines -->
android:lineSpacingMultiplier="1.15"   <!-- Use for all headlines -->

<!-- Premium Body Text -->
android:lineSpacingMultiplier="1.3"    <!-- Use for descriptions -->

<!-- Alternative (Less Recommended) -->
android:lineSpacingMultiplier="1.2"    <!-- Slightly tight -->
android:lineSpacingMultiplier="1.4"    <!-- Slightly loose -->
```

## Gravity/Alignment

```xml
<!-- Headlines & Center Content -->
android:gravity="center"

<!-- Body Text & Descriptions -->
android:gravity="start"             <!-- Left-aligned (natural reading) -->
```

## Complete Widget Templates

### ✅ Weather Time/Temperature Display
```xml
<TextView
    android:id="@+id/timeNow"
    android:fontFamily="sans-serif-black"
    android:gravity="center"
    android:includeFontPadding="false"
    android:letterSpacing="0.05"
    android:text="15:05"
    android:textColor="@color/tvTextPrimary"
    android:textSize="@dimen/text_time"
    android:textStyle="bold" />
```

### ✅ Weather Date Display
```xml
<TextView
    android:id="@+id/dateNow"
    android:fontFamily="sans-serif-medium"
    android:gravity="center"
    android:letterSpacing="0.18"
    android:text="Thu, 15 May"
    android:textAllCaps="true"
    android:textColor="@color/tvTextSecondary"
    android:textSize="@dimen/text_date" />
```

### ✅ Weather Condition Display
```xml
<TextView
    android:id="@+id/currentStatus"
    android:fontFamily="sans-serif-medium"
    android:letterSpacing="0.20"
    android:text="PARTLY CLOUDY"
    android:textAllCaps="true"
    android:textColor="@color/tvTextSecondary"
    android:textSize="@dimen/text_condition" />
```

### ✅ News Headline
```xml
<TextView
    android:id="@+id/main_header"
    android:fontFamily="sans-serif-black"
    android:gravity="center"
    android:lineSpacingMultiplier="1.15"
    android:maxLines="3"
    android:text="Breaking news headline"
    android:textColor="@color/tvTextPrimary"
    android:textSize="@dimen/text_news_headline"
    android:textStyle="bold" />
```

### ✅ News Description
```xml
<TextView
    android:id="@+id/news_details"
    android:fontFamily="sans-serif-light"
    android:gravity="start"
    android:lineSpacingMultiplier="1.3"
    android:maxLines="6"
    android:text="News description goes here"
    android:textColor="@color/tvTextSecondary"
    android:textSize="@dimen/text_news_desc" />
```

### ✅ Metric Value
```xml
<TextView
    android:fontFamily="sans-serif-medium"
    android:text="32"
    android:textColor="@color/tvTextPrimary"
    android:textSize="@dimen/text_metrics" />
```

### ✅ Metric Label
```xml
<TextView
    android:fontFamily="sans-serif-light"
    android:text="km/h"
    android:textColor="@color/tvTextSecondary"
    android:textSize="@dimen/text_label" />
```

## Color Reference

```xml
@color/tvTextPrimary       = #FFFFFF (white - headlines, values)
@color/tvTextSecondary     = #B3B3B3 (light gray - labels, descriptions)
@color/tvAccent            = #E50914 (Netflix red - dividers, badges)
@color/tvBorder            = subtle border color
@color/tvDivider           = thin divider color  
@color/tvSurface           = background for panels
@color/tvBg                = main background (#000000)
```

## Dimension Reference

```xml
@dimen/text_location           = ~12sp
@dimen/text_time               = ~42sp
@dimen/text_date               = ~18sp (portrait) / ~16sp (landscape)
@dimen/text_temp               = ~60sp (portrait) / ~40sp (landscape)
@dimen/text_condition          = ~15sp (portrait) / ~14sp (landscape)
@dimen/text_metrics            = ~16sp
@dimen/text_label              = ~12sp
@dimen/text_news_headline      = ~varies by orientation
@dimen/text_news_desc          = ~14sp to 18sp

@dimen/weather_icon_size       = ~240dp (portrait) / ~72dp (landscape)
@dimen/weather_metric_icon_size = ~24dp
@dimen/weather_icon_glow_size  = ~280dp

@dimen/news_text_padding       = ~40dp (portrait) / ~16dp (landscape)
@dimen/metric_divider_height   = ~48dp
```

## Common Mistakes to Avoid

```xml
<!-- ❌ DON'T: Use sans-serif-black for metrics values -->
<!-- ✅ DO: Use sans-serif-medium for metrics values -->

<!-- ❌ DON'T: Add letterSpacing to all text -->
<!-- ✅ DO: Only add for headlines, dates, and accents -->

<!-- ❌ DON'T: Center-align descriptions -->
<!-- ✅ DO: Use gravity="start" for body text -->

<!-- ❌ DON'T: Use 1.5 line spacing for descriptions -->
<!-- ✅ DO: Use lineSpacingMultiplier="1.3" -->

<!-- ❌ DON'T: Hardcode colors like #B3B3B3 -->
<!-- ✅ DO: Use @color/tvTextSecondary -->

<!-- ❌ DON'T: Forget textStyle="bold" on headlines -->
<!-- ✅ DO: Always include for emphasis -->

<!-- ❌ DON'T: Mix font families inconsistently -->
<!-- ✅ DO: Follow: black=headlines, medium=secondary, light=body -->
```

## Android API Level Support

All styling attributes used are supported on **API 21+**:
- ✅ `fontFamily` - API 16+
- ✅ `letterSpacing` - API 21+
- ✅ `lineSpacingMultiplier` - API 1+
- ✅ `includeFontPadding` - API 1+
- ✅ `@color/` references - API 1+

Safe for production use.

---

**Last Updated**: May 15, 2026  
**Applied To**: Portrait, Landscape, Forced Portrait layouts  
**Build Status**: ✅ Verified & Successful

