# Backend request: support remote screen setup during TV login

## Context

The TV app already has a QR-code login flow so users don't have to type credentials with a
remote:

1. TV calls `POST tv/start-auth` → gets back `device_code`, `user_code`, `qr_url`.
2. TV shows the QR code / short code.
3. User scans it (or visits the URL) on their **phone**, and approves login there.
4. TV polls `GET tv/poll-auth/{deviceCode}` until `status` becomes `"approved"`.

That approval page is the one place a user already interacts with their phone during setup.
We want to extend it so a user can **also configure the whole screen from their phone**,
instead of using the TV remote — typing things like a Google Drive URL with a D-pad is very
painful, and this reuses infrastructure you've already built.

## What we need

### 1. Extend the activation/approval web page

Add optional fields to the page the user sees after scanning the QR code (the same page that
currently just has an "Approve" button), so they can fill in the screen's setup **before**
approving:

| Field | Type | Notes |
|---|---|---|
| Screen | dropdown | Same screens already returned by `GET get_screen_by_user` for this account |
| Orientation | dropdown | One of: `Landscape`, `Portrait`, `Forced Portrait`, `Split Screen` |
| Refresh Interval | dropdown | Same 4 options as the TV's own spinner (see below) |
| Display Text | checkbox | |
| Business Rules | checkbox | |
| Weather | checkbox | default checked |
| News | checkbox | default checked |
| IOT (audience targeting) | checkbox | default unchecked |
| Cloud Slideshow | checkbox | default unchecked |
| Slideshow Google Drive folder link | text | only relevant if Cloud Slideshow is checked |
| Slideshow seconds per photo | number | default 5 |

All of these are optional — a user can still just hit "Approve" without filling any of it in,
and login should work exactly as it does today in that case.

### 2. Return the filled-in values from `tv/poll-auth/{deviceCode}`

Once approved, if the user filled in the setup fields, include them in the poll response as a
new `screenSetup` object:

```json
{
  "status": "approved",
  "token": "...",
  "user": { ... },
  "screenSetup": {
    "screenId": "l169889",
    "orientation": "Landscape",
    "refreshInterval": "0",
    "displayTextEnabled": true,
    "businessRulesEnabled": false,
    "weatherEnabled": true,
    "newsEnabled": true,
    "iotEnabled": false,
    "slideshowEnabled": true,
    "slideshowFolderUrl": "https://drive.google.com/drive/folders/1_vi6BXkm8beF0aTFQ2dbaBEJ1oJ5gZvi?usp=sharing",
    "slideshowIntervalSeconds": 5
  }
}
```

**If the user didn't fill in the setup form, omit `screenSetup` entirely (or send it as
`null`)** — the TV app already handles that as "no remote setup provided, fall back to manual
setup on the TV," so this is fully backward compatible with today's behavior. No existing
client changes required on your end for the plain-login case.

### Field reference

- `screenId` **(required to trigger auto-setup)** — must be a valid screenId for this account, same values as `GET get_screen_by_user`.
- `orientation` **(required to trigger auto-setup)** — exactly one of `"Landscape"`, `"Portrait"`, `"Forced Portrait"`, `"Split Screen"` (case-sensitive, matches the TV's own spinner values).
- `refreshInterval` — string, one of `"0"` (1 min), `"1"` (5 min), `"2"` (30 min), `"3"` (60 min). Defaults to `"0"` if omitted.
- `displayTextEnabled`, `businessRulesEnabled`, `iotEnabled` — booleans, default `false` if omitted.
- `weatherEnabled`, `newsEnabled` — booleans, default `true` if omitted (matches the TV checkboxes' own defaults).
- `slideshowEnabled` — boolean, default `false` if omitted. If `true`, `slideshowFolderUrl` should be a public ("Anyone with the link") Google Drive folder share link.
- `slideshowIntervalSeconds` — integer, default `5` if omitted.

**Important:** `screenSetup` is only acted on if both `screenId` and `orientation` are present and non-null. If either is missing, the TV falls back to normal manual setup — so partial/incomplete submissions are safe.

## What happens on the TV side (already built, no further Android work needed)

If `screenSetup` is present with at least `screenId` + `orientation`, the TV applies every
field and starts playing immediately — no manual setup screen, no remote needed. If it's
absent, login behaves exactly as it does today.
