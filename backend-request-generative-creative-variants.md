# Backend request: generative creative variants (one asset, many captions)

## Context

2026 DOOH research (see the market-gap conversation that produced `release/v1.3.0`) found that
companies like Broadsign/Katalyst are using generative AI to auto-adapt one creative asset into
many variants per screen/segment/language, instead of needing a human to design a new asset for
every segment. That's roadmap item 5 ("generative creative at the CMS layer").

**The good news: the player already carries everything needed to play these variants — this is
entirely a CMS/backend feature, not an Android change.** Every ad already has independent
caption/overlay fields (`textTop`, `textBottom`, `textLeft`, `textRight`) and targeting fields
(`targetTags`, `targetGender`, `targetAgeGroup`, `targetEmotion`, `targetHours`) that the player
already reads and renders per-ad today — see `AdContractData.java` / `MediaModel.java` in this
repo. A "variant" is just another ad record pointing at the same media file with different values
in those fields.

## What we need

1. **CMS: a "Generate Variants" action on ad upload.** Given one base asset (image/video) and a
   short product description, call an LLM to generate N caption variants for different segments
   (e.g. one per store, per target tag, per locale) — no human copywriter needed per screen.
2. **Publish each variant as its own ad record**, same `videoUrl` as the base asset, different
   `textTop`/`textBottom`/`textLeft`/`textRight` and targeting fields. No new fields needed on
   our side — this is the existing `AdContractData` schema, used exactly as it is today.
3. **Optional: an `aiGenerated` flag per ad.** One of the research sources found that labelling
   AI-generated content can be a trust/competitive advantage rather than a liability. Recommend
   surfacing this in the CMS UI and in any advertiser-facing report, not required by the player
   (we'd just ignore an unknown field if you added it without telling us).

## Known player-side limitation — worth knowing before this ships

`AdSyncWorker.java` downloads each ad's media file independently, under a fresh random filename,
keyed by `advertId` — it does **not** dedupe by `videoUrl`. If this feature generates, say, 10
caption variants of the same 20MB video for 10 store segments, today's player will download and
store that same video 10 separate times on every screen that syncs more than one variant. That's
a real bandwidth/storage cost once this is in regular use, not a blocker for shipping it, but
something we should fix on our side (content-hash-based dedup) before variant counts get large.
Flagging now so it isn't a surprise later — happy to take that on once this is live and we can see
real variant volumes.

## What happens on the TV side (no further Android work needed for the core feature)

Multiple ads sharing one media asset with different caption fields already play correctly today
— each one is just a normal ad in the synced playlist, selected/rotated the same way any ad is.
The only thing to watch is the download-dedup point above once variant volume grows.
