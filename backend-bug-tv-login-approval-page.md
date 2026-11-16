# Backend bug: mobile approval page shows nothing after Approve

## What's happening

TV login flow (QR code scan → approve on phone → TV signs in):

1. TV calls `POST tv/start-auth`, shows the QR code / short code.
2. User scans it, lands on the activation page on their phone, taps **Approve**.
3. TV polls `GET tv/poll-auth/{deviceCode}` and correctly detects `status: approved` — it
   proceeds to sign in and load the next screen.
4. **On the phone, nothing visibly changes after tapping Approve.** No confirmation, no
   "you're logged in" message, no indication the tap actually did anything.

Confirmed this is not an Android/TV-side issue: the TV app is correctly receiving the approved
status and moving on, so the approval itself is being recorded server-side — it's purely that
the phone's page doesn't reflect it.

## What's needed

After the user taps **Approve** on the activation page, show a clear success state before
they navigate away or close the tab — for example:

> ✅ Approved — you can return to your TV now.

This matters more once the screen-setup form (see `backend-request-remote-screen-setup.md`)
is added to this same page, since users will expect visible confirmation that their submitted
setup (screen, orientation, slideshow link, etc.) was actually received — not just for the
plain login case.

## Suggested acceptance check

- Tap Approve on the activation page → page updates in place (or navigates) to show a
  success/confirmation state, without requiring a manual refresh.
- Works whether or not the screen-setup fields (once added) were filled in.
