# Backend request: zone assignment for Dynamic Canvas (roadmap item 1, follow-up)

## Context

Dynamic Canvas (the N-zone layout orientation, up to 4 zones: ads/weather/news/social/clock/etc.)
shipped on the Android side in `release/v1.3.0`. It's fully built and tested — but on every real
account it just falls back to one full-screen zone, because the two fields it needs
(`zoneId`, `zoneLayoutType`) never arrive from the backend. This was confirmed from both sides:
logcat on the TV shows `Resolved layout: SINGLE_FULL_SCREEN (1 zones)` because no synced ad
carries a `zoneId`, and a full search of `my-express-api`'s codebase/schema confirms why — those
two fields don't exist anywhere: not on the `adverts` table, not in `advert.js`'s column mapping,
not on any route. (The only existing "zone" concept in that codebase is the unrelated
audience-heatmap `zone_data` — face-detection engagement zones on screens — a different thing
with the same word.)

This is net-new backend/CMS work, not something already built that just needs wiring.

## What the player already expects (so there's no ambiguity about the contract)

`AdContractData.java` (the model the player deserializes each ad into from
`GET get_screen_playlists/{screenId}`) already has these two fields declared and already reads
them with no further Android changes needed:

```java
/** Which zone (1-4) this ad plays in. Null/0 = zone 1 (the default/main zone). */
public Integer zoneId;
/** Screen-level layout name, one of ZoneLayout.LayoutType, e.g. "GRID_2X2". Only needs to
 *  be set on one ad per screen — AdvertDynamicCanvas takes the first non-null value it
 *  finds. Null = single full-screen zone (today's existing behavior, unchanged). */
public String zoneLayoutType;
```

So the JSON keys are exactly `zoneId` and `zoneLayoutType`, nested in each ad object the same
way `textBottom`/`textTop`/`targetHours`/etc. already are today.

### `zoneLayoutType` accepted values (case-sensitive, matches the TV's own enum)

One of: `SINGLE_FULL_SCREEN`, `SPLIT_HORIZONTAL_2`, `SPLIT_HORIZONTAL_3`, `SPLIT_VERTICAL_2`,
`GRID_2X2`, `MAIN_PLUS_BOTTOM_BAR`, `MAIN_PLUS_SIDE_BAR`, `L_SHAPE`. (`CUSTOM` also exists on the
player but has no preset — skip it for now, see "Not needed yet" below.) Each preset has a fixed
zone count and a fixed content type per zone, e.g. `GRID_2X2` = 4 zones (zone 1 = ads, zone 2 =
weather, zone 3 = news ticker, zone 4 = social feed) — see `ZoneLayout.java` in this repo for the
full table if useful context for the CMS copy.

### `zoneId` values

An integer 1-4. The player routes each ad to whichever zone in the resolved layout has that id;
`zoneId` 0/null, or any id that doesn't exist in the chosen layout, falls back to zone 1. Only
zones whose preset content type is `AD_ROTATION` (always zone 1 in every preset above) actually
rotate ads — weather/news/social/clock zones render themselves automatically, no ad needs to be
assigned to them.

## What we need

1. **Two new columns on `adverts`** (or a join table if the same ad can be assigned to different
   zones on different screens — your call, we don't have visibility into whether that's a real
   use case) — `zone_id` (int, nullable) and `zone_layout_type` (string, nullable).
2. **A CMS UI** for setting them, scoped to whenever Dynamic Canvas orientation is selected for a
   screen — realistically: a layout picker (one of the 8 presets above, maybe with a visual
   preview like the TV app's own "Layout Preview" on the orientation screen) + a per-ad zone
   picker (1-4) shown only when assigning/editing an ad for a Dynamic-Canvas screen. Exactly one
   ad per screen needs `zoneLayoutType` set — the player takes the first non-null value it finds
   among that screen's synced ads — so this could just be a screen-level setting under the hood
   even if the CMS shows it next to the ad.
3. **Include both fields in `GET get_screen_playlists/{screenId}`**'s per-ad response objects,
   same place `textBottom`/`textTop`/`targetHours` etc. already live. Omit or send `null` when
   unset — the player already treats that as "no zone assignment," which is exactly today's
   fallback behavior, so this is fully backward compatible.

## What we need from you to size this properly

- Whether zone assignment should be screen-level (pick a layout once per screen, then assign
  existing ads to zones 1-4) or needs to work differently — we don't know your CMS's existing
  ad/screen assignment UX well enough to prescribe the exact flow.
- Whether any account actually wants more than one ad rotating per zone (vs. one fixed item per
  non-`AD_ROTATION` zone) — affects whether the zone picker needs to support multi-select.

## Not needed yet (lower priority / already scoped out on our side too)

- `CUSTOM` layout (raw backend-supplied zone arrays with arbitrary percentages) — modeled on the
  player but not wired to a parser. Only worth building if the 8 fixed presets turn out to be too
  rigid once this is in use.
- `WEB_VIEW`/`DATA_WIDGET` zone types need a `contentSource` URL per zone — not covered by this
  doc; flag separately if/when a screen actually wants a widget zone.

## What happens on the TV side (nothing — already built)

No Android changes needed. The moment `zoneId`/`zoneLayoutType` start arriving in
`get_screen_playlists`, Dynamic Canvas will resolve the real layout and route ads to their zones
automatically — this is already tested end-to-end on-device, just waiting on real data.
