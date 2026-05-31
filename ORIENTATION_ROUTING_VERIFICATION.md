# 🔍 ORIENTATION ROUTING LOGIC - VERIFICATION

## Current Routing in SelectScreens.java

Based on the code at lines 394-398 in SelectScreens.java:

```java
if (orient.toLowerCase().equalsIgnoreCase("tv portrait")) {
    startActivity(new Intent(context, AdvertLandWatch.class));
} else {
    startActivity(new Intent(context, AdvertWatching.class));
}
```

---

## Current Routing Map

```
┌─────────────────────────────────────────────────────────────┐
│           SELECTSCREENS ORIENTATION SELECTION               │
└─────────────────────────────────────────────────────────────┘
                            │
        ┌───────────────────┼───────────────────┐
        │                   │                   │
   "Portrait"        "Landscape"        "TV Portrait"
        │                   │                   │
        │                   │                   │
        └───────────────────┴───────────────────┘
                       (else branch)           (if branch)
                            │                   │
                            ▼                   ▼
                    AdvertWatching          AdvertLandWatch
                    (class only)            (class only)
                    R.layout.fragment_advert_watching
                            │               R.layout.activity_advert_land_watch
                            │                   │
                ┌───────────┴───────────┐       │
                │                       │       │
          Device in             Device in       │
          PORTRAIT MODE         LANDSCAPE MODE  │
          (physical)            (physical)      │
                │                       │       │
                ▼                       ▼       │
        layout/                layout-land/    │
        fragment_advert_watching ...   │       │
        (vertical stack)              │       │
                                   SIDE-BY-SIDE└─►
                                   LEFT/RIGHT
                                   SPLIT LAYOUT
```

---

## The Issue You're Identifying

You're right to question this! Here's what's happening:

### Scenario 1: User selects "Portrait"
- ✅ Correct Activity launched: `AdvertWatching`
- ✅ Correct Layout loaded: `R.layout.fragment_advert_watching`
- ❌ BUT: Android chooses the variant based on PHYSICAL device orientation:
  - If device is in portrait orientation → `layout/fragment_advert_watching.xml` (VERTICAL STACK ✓)
  - If device is rotated to landscape → `layout-land/fragment_advert_watching.xml` (SIDE-BY-SIDE ✗)

### Scenario 2: User selects "TV Portrait"
- ✅ Correct Activity launched: `AdvertLandWatch`
- ✅ Correct Layout loaded: `R.layout.activity_advert_land_watch`
- ✅ Uses rotations (-90°) to display on landscape-mounted TV as portrait

---

## The Problem

When you select "Portrait" and see side-by-side layout, it means:

**Your physical device is in LANDSCAPE orientation**, so Android automatically loads the landscape layout file (`layout-land/fragment_advert_watching.xml`).

---

## Solution Options

### Option 1: Lock AdvertWatching to Portrait-Only Mode
Force AdvertWatching to ONLY display in portrait orientation, regardless of device rotation.

**Where to add:** AdvertWatching.java onCreate():
```java
setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
```

### Option 2: Use Side-by-Side for Portrait (Current Landscape Layout)
If you WANT the side-by-side layout when "Portrait" is selected, use activity_advert_land_watch.xml instead.

**Where to change:** SelectScreens.java lines 394-398

### Option 3: Create New Dedicated Portrait Layout
Create a new side-by-side layout specifically for "Portrait" orientation (different from current TV Portrait).

---

## What Do You Want?

**Please clarify:**

1. ❓ When you select "Portrait", do you want:
   - **Option A**: Vertical stack (Location → Time → Temp → Condition → Metrics), ALWAYS in portrait (lock orientation)?
   - **Option B**: Side-by-side layout (like current landscape), but with a different design/styling?

2. ❓ Is the physical device auto-rotating based on the tablet's rotation sensor, OR is it fixed?

3. ❓ Should "Portrait" mode:
   - Be locked to portrait-only orientation?
   - Or allow rotation but always show vertical stack?

Once you tell me, I can fix the routing to use the correct layout! 🎯

