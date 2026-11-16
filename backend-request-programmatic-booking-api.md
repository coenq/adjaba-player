# Backend request: ad-slot availability + booking (roadmap item 6)

## Context

Research for `release/v1.3.0` found two relevant 2026 data points:

- India's retail-media ad spend is estimated at ~₹30,000 crore in 2026 (~25% YoY), with in-store
  screens projected to drive most of that growth through 2029.
- Invidis' India-specific coverage found current deployments mostly just **show** content rather
  than let anyone **book or target** specific inventory — the targeting/booking layer is the gap,
  not the screens themselves.

This is roadmap item 6 ("programmatic/booking API"). Unlike item 5, this one is **not** something
the player can mostly already support — it's a real product surface (availability, booking,
possibly a bidstream connection to an ad exchange) that lives entirely in the CMS/backend, which
isn't in this repo. This doc is scoped as a requirements/starting-point note, not a full spec —
we don't have visibility into your contract/billing data model to design the booking side for
you.

## What the player already gives you today

Every play is already logged and uploaded via `POST /create_impression` (see
`ImpressionRetryWorker.java` / `APIImpression.java` in this repo) with `screenId`, `advertId`,
`contractId`, `dayHour`, `playSec`, `format`, `orientation`, `locationType`, `tags`. As of
`release/v1.3.0` these rows also persist after upload (previously deleted once sent) and there's
an on-device "Proof of Play" report, so this history is real and auditable, not just a snapshot of
whatever's currently unsent.

**That's the raw material for an availability calculation** — "how many ad-seconds has screen X
played in daypart Y, and how much of that is already booked" is derivable from impressions +
whatever your contracts table already tracks, without needing anything new from the player.

## What we think is needed (your side, for discussion — not a locked spec)

1. **An availability endpoint**: given a screen (or a set of screens by location/tag) and a
   daypart, return how much inventory is open vs. already committed to a contract. Computed from
   existing impression history + contract data, both of which already exist on your side.
2. **A booking flow** (likely a CMS UI, not an API we'd call from the player): lets a reseller or
   advertiser see availability and commit a contract/budget against specific screens/dayparts/
   targeting, the same way `AdContractData`'s existing targeting fields (`targetHours`,
   `targetGender`, `targetAgeGroup`, `targetTags`, `targetEmotion`) already work — a booking is
   really just "create a contract that becomes ads with these targeting fields," which the sync
   pipeline already knows how to deliver.
3. **Optional, longer-term: an OpenRTB/VAST bridge** if you want to sell inventory into external
   DOOH ad exchanges rather than only direct/reseller bookings. This is the biggest lift of the
   three and probably not the first thing to build — mentioning it because the research flagged
   programmatic DOOH as one of the fastest-growing pieces of the market, but it's a distinct
   project from (1) and (2).

## What we need from you to size this properly

- Whatever your current contract/billing data model already looks like (so we know whether
  "availability" is a new concept or something you're already tracking informally).
- Whether booking should be reseller-facing, advertiser-facing, or both — changes who the CMS UI
  in (2) is actually for.

## What happens on the TV side

Nothing changes on the player until there's a concrete API to call — (1) and (2) above are purely
backend/CMS. If a booking ever needs something the player doesn't already expose (e.g. a specific
new targeting dimension), that would come back to us as its own backend-request-style doc once
the shape of (2) is clearer.
