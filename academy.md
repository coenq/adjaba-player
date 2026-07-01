# Adjaba Academy — Partner Guide

Welcome to Adjaba Academy. This guide explains what **Adjaba Player** is, how it works, and how to set up and run it on your own screens. It's written for partners doing a self-service (DIY) install — no developer background required.

---

## 1. What is Adjaba Player?

Adjaba Player is a digital signage app that turns a TV, Fire TV, or Android device into an advertising/content screen. Once a screen is registered to your account, it automatically:

- Downloads and plays the content scheduled for it (ads, videos, images, live streams, web pages, social feeds)
- Mixes in optional weather and news slides
- Keeps playing on a loop, refreshing its playlist on a schedule you control
- Can optionally tailor which ad plays next based on who's in front of the screen (see [Smart Audience Targeting](#8-smart-audience-targeting-iot-optional))

You don't need to touch the device after the initial setup — content is managed remotely from the Adjaba backend/CMS and pushed to the screen automatically.

---

## 2. Supported Devices

| Device type | Notes |
|---|---|
| Android TV | Full remote/D-pad navigation, appears in the TV app launcher |
| Amazon Fire TV | Same app, sideloaded via Downloader or similar |
| Android phone/tablet | Useful for previewing content or running a screen on a mounted tablet |

Minimum Android version: 5.0 (Lollipop). The app needs an active internet connection at all times — it downloads content over the network and reports status back to the backend.

---

## 3. Installing the App (DIY)

### On Android TV / Fire TV
1. Install a file manager / sideload tool (e.g. **Downloader** app from the Fire TV/Android TV app store) if one isn't already installed.
2. Use it to download the Adjaba Player APK from the link provided by your Adjaba contact.
3. Open the downloaded file and select **Install**. You may need to enable "Apps from unknown sources" for the sideload tool the first time.
4. Once installed, find **Adjaba Player** in your apps list and open it.

### On an Android phone/tablet
1. Download the APK file to the device (e.g. via a download link, email, or USB transfer).
2. Open it from your file manager/Downloads. If prompted, allow installs from this source.
3. Tap **Install**, then open the app once it's done.

### If the install gets stuck or fails
This is almost always a device-side issue, not an app problem. Try these in order:

1. **Re-download the file.** A partial/corrupted transfer is the most common cause of an installer that hangs forever on "Installing...".
2. **Uninstall any older copy of the app first**, then install the new APK. Installing over a copy signed with a different key can cause the installer to silently fail or hang on some devices.
3. **Check free storage** on the device — low storage can cause silent install failures.
4. **Check your internet connection.** On phones, Google Play Protect scans sideloaded APKs before installing, and that scan can hang if it can't reach the network.
5. If it still won't install, connect the device to a computer with USB debugging enabled and use `adb install` — it will report the exact error instead of leaving you with a stuck spinner.

---

## 4. Activating Your Screen (Login)

Every device needs to be linked to your Adjaba account before it can play content.

### Phone / tablet login
Enter your **Screen ID** (your account login, shown as an email-style field) and your **Pin** (password), then sign in. Checking **Remember Me** keeps you logged in across app restarts.

### TV login (remote-friendly)
TVs use a code-based login instead of typing credentials with a remote:
1. Open the app — it displays a short code and/or QR code on screen.
2. On your phone or computer, go to the activation page and enter the code shown on the TV (or scan the QR code).
3. Approve the device from there.
4. The TV automatically detects the approval and signs in — no typing required.

---

## 5. Setting Up a Screen

After logging in, you'll land on the **Select Screen** setup page. This is where you configure how this specific device should behave. Settings are remembered per device.

| Setting | What it does |
|---|---|
| **Screen ID** | Which registered screen (location) this device represents — pick from the list tied to your account |
| **Orientation / Layout** | `Landscape`, `Portrait`, `Forced Portrait`, or `Split Screen` (see below) |
| **Refresh Interval** | How often the device checks for a new playlist: every 1, 5, 30, 60 minutes, or never (manual refresh only) |
| **Display Text** | Show/hide text overlays (e.g. promo captions) on top of content |
| **Business Hours** | When enabled, ads with scheduled hours only play during their assigned time windows |
| **Weather** | Insert weather slides into the rotation |
| **News** | Insert news slides into the rotation |
| **Smart Audience Targeting (IOT)** | Optional — enables real-time audience-aware ad selection (see section 8) |

Once configured, press **Play** to start the screen. These settings persist across reboots, so a screen will resume the same configuration automatically if power is cut and restored.

### Orientation / Layout options explained
- **Landscape** — standard widescreen TV layout.
- **Portrait** — vertical layout, for screens mounted upright.
- **Forced Portrait** — for TVs that are physically rotated 90° but whose hardware doesn't natively support portrait mode; the app rotates the rendering in software.
- **Split Screen** — divides the screen into multiple zones (e.g. a main video area plus a sidebar), each zone playing its own content independently. Useful for menu boards or mixed content layouts.

---

## 6. Content Types You Can Display

A screen's playlist can mix any combination of:

- **Images** — standard ad creatives (jpg, png, gif, webp, etc.)
- **Videos** — standard video ad creatives (mp4, mkv, mov, etc.)
- **Web Content** — a live web page rendered directly on the screen, refreshing on its own schedule
- **Live Stream** — a live video stream (e.g. a security camera feed or live broadcast)
- **Social Feed** — posts pulled from a social platform/hashtag, displayed as a rotating feed
- **Weather** — auto-updating local weather, if enabled
- **News** — auto-updating news headlines, if enabled

You don't configure these on the device itself — content is assigned to a screen from the Adjaba backend/CMS, and the device automatically downloads and plays whatever is scheduled for it. If your screen is set up but shows nothing, it likely means no content has been assigned to that Screen ID yet — check with your Adjaba contact.

If the device temporarily has no ads to show (e.g. nothing scheduled, or no network to fetch new content), it falls back to looping weather/news slides if those are enabled, rather than going blank.

---

## 7. Multi-Zone / Split Screen Layouts

When **Split Screen** is selected as the orientation, the screen is divided into independent zones — for example, a large zone for video/ad content and a smaller zone for a social feed or web content ticker. Each zone plays its own assigned content on its own schedule, all rendered simultaneously on the same physical screen. Zone layout and assignment is configured from the backend/CMS, same as regular content.

---

## 8. Smart Audience Targeting (IOT, optional)

This is an advanced, opt-in feature. When enabled, the screen connects to a real-time audience-analytics feed and automatically favors the best-matching ad for whoever is currently in front of the screen — factoring in things like estimated age range, gender, mood, group size, and time of day. If the feed has nothing to report, or the feature is off, the screen just plays its normal scheduled rotation.

To use this feature:
- Tick the **IOT** checkbox on the Select Screen setup page.
- Your Adjaba contact needs to enable and connect the audience-analytics sensor for your store/location — there's no setup required on the device beyond ticking the box.

This is entirely optional. Leaving it unchecked (the default) just runs the normal scheduled playlist.

---

## 9. Day-to-Day Operation

- **Keep it powered and connected.** The device needs to stay online to receive new content and report playback. Wi-Fi/Ethernet should be stable and always-on.
- **Auto-start on reboot.** Once a screen has been started with **Play**, the app resumes playback automatically if the device loses power and comes back — no need to manually relaunch it after an outage. On newer devices (Android 10 and above) this requires granting the app the **"Display over other apps"** permission once, in the device's app settings.
- **Works offline.** Ads are downloaded and stored on the device, so if the internet drops out (or is down after a power cut), the screen keeps playing the last downloaded playlist and syncs again when the connection returns.
- **Remote control only on TV.** All TV screens are fully navigable with a D-pad remote; no touchscreen or mouse required.
- **Leave it running.** Don't force-close the app during business hours — it's designed to run continuously.

---

## 10. Troubleshooting

| Problem | Likely cause / fix |
|---|---|
| Install hangs or fails | See [section 3](#if-the-install-gets-stuck-or-fails) |
| Can't log in | Double-check Screen ID/Pin; confirm the device has internet access |
| No screens show up after login | Your account may not have a screen registered yet — contact your Adjaba rep |
| Screen plays nothing / blank | No content assigned to that Screen ID yet, or no network to download it |
| Content looks outdated | Check the Refresh Interval setting — lower it, or trigger a manual refresh |
| Weather/News not showing | Make sure the relevant checkbox is enabled on the Select Screen page |
| Smart Audience Targeting not affecting ad choice | Confirm IOT is checked, and that the analytics sensor for your location is active (ask your Adjaba contact) |

---

## 11. Getting Help

If something isn't covered here, or behaves unexpectedly, reach out to your Adjaba point of contact with:
- The Screen ID
- What device/TV model you're using
- What you expected to happen vs. what actually happened

This makes it much faster to diagnose than "it's not working."
