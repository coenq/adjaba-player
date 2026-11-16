# Adjaba — Market Analysis & 90-Day Go-To-Market Plan

*Prepared July 2026. Contains internal pricing and margin figures — do not share externally as-is.*

---

## 1. What Adjaba Is

A closed-loop, audience-aware digital signage stack — three products working together:

1. **Adjaba Player** (this repo) — Android TV / Fire TV signage app: offline-first playback, boot auto-resume, weather/news filler, image/video/web/stream/social content, QR overlays, proof-of-play impressions.
2. **Footfall app** — DIY Android app collecting anonymous audience data (count, age band, gender, mood, dwell time) from a camera, published over MQTT per store.
3. **React CMS** — content scheduling, screen management, analytics backend (api.adjaba.in).

**Positioning: "Screens that sell. Data that proves it."** Signage + footfall analytics in one system, on commodity hardware, at SMB prices.

---

## 2. Market

- India digital signage market: **~$1.2–1.3B in 2026, ~10% CAGR, → $2.3B by 2032**. Drivers: retail, QSR, malls, cafés.
- Global signage SaaS pricing has settled at **$8–20/screen/month** (Yodeck $8–16, OptiSigns $10–30).
- Footfall analytics is sold separately today; dedicated hardware (e.g. FootfallCam) costs **~₹45,000/device** in India plus software — priced out of reach of nearly every SMB.

**The wedge:** an Indian SMB wanting "signage + know your customers" must currently buy two systems from two vendors at enterprise prices. Adjaba delivers both on a Fire TV stick + a phone camera.

### Competitive landscape

| Bucket | Players | They win on | Adjaba wins on |
|---|---|---|---|
| Global SaaS | Yodeck, OptiSigns, ScreenCloud | Fleet mgmt polish | Price (INR), audience analytics |
| Indian SaaS | Pickcel, Xtreme Media | Local presence | Audience loop at SMB tier |
| Footfall vendors | FootfallCam, Vemcount | Counting accuracy | 10x cheaper, includes signage |
| Free/DIY | Xibo, YouTube-on-TV | Free | Analytics story, reliability |

### Two prerequisites for the story to hold

1. Analytics must be positioned (and implemented) as **anonymous, aggregate audience measurement — no faces stored, no identification** (DPDP Act compliance = legal necessity and selling point).
2. Fix MQTT security (trust-all TLS, shared hardcoded credentials) before reseller/enterprise evaluations.

---

## 3. Target Segments (in priority order)

1. **Multi-outlet SMB chains (3–25 locations)** — QSR/cafés, salons, pharmacies, gyms, clinics. One decision-maker, multi-screen deals, real value from footfall data.
2. **Resellers — especially CCTV/security installers** (unfair advantage: they already sell cameras to every shop, have installation crews, and footfall gives their camera installs a recurring-revenue upsell). Then AV integrators, IT hardware dealers.
3. **Local ad networks** (screen owners selling slots) — demographic targeting + proof-of-play is their pitch to advertisers.

Single-location shops: self-serve only, never site visits.

---

## 4. Pricing

| Tier | Price | Contents |
|---|---|---|
| Player | ₹399/screen/mo | Signage + CMS + offline + auto-restart |
| Player + Insights | ₹999/screen/mo | + footfall, dwell, demographics dashboard |
| Setup (optional) | ₹2,500 one-time/site | Install + onboarding |
| Hardware kit (optional) | ₹5,500 one-time | Fire TV stick + mounts (~₹1.5K margin) |

- Resellers: **30% off list**, keep setup/hardware margin entirely.
- Annual prepay = 2 months free.

---

## 5. Social Media / GTM Plan (90 days, founder-led)

**Channels:** Instagram Reels + YouTube Shorts → SMBs. LinkedIn + WhatsApp → resellers.

**Content pillars (3 short videos/week — sustainable, not aspirational):**
1. *Transformation* — paper menu → live screen, shot in a real shop.
2. *Proof* — real dashboard screenshots, one genuine case study ("café learned 70% of evening footfall is 20–35 male, switched the 6pm promo").
3. *Credibility* (LinkedIn) — build-in-public: lessons from installs, the power-cut resilience story.

**Paid:** ₹30–45K/month, Meta ads geo-fenced to 2–3 serviceable cities, objective = WhatsApp message.

**Reseller outreach quota:** 20 personalized LinkedIn/WhatsApp messages/week to CCTV installers & AV dealers. Funnel target: 10 conversations → 3 demos → 1 signed reseller/month.

**Weekly cadence:** 3 Reels/Shorts + 2 LinkedIn posts + 20 reseller DMs + same-day replies to every inbound WhatsApp, for 12 weeks.

**Ready-made outreach content:** `marketing/whatsapp-smb.md` and `marketing/whatsapp-reseller.md`.

---

## 6. 90-Day Revenue Target

Assumptions: pricing above, founder-led sales (+1 helper), 2–3 cities, demo-able product.

| Month | Screens live | Paying screens | Resellers | MRR | One-time (cum.) |
|---|---|---|---|---|---|
| 1 | 15 (mostly pilots) | 5 | 0 | ₹8–12K | ₹25K |
| 2 | 40 | 25 | 1 | ₹35–45K | ₹90K |
| 3 | 80 | 60 | 2–3 | **₹90K–1.2L** | ₹2L |

**Target: ₹1L MRR + ₹2L cumulative one-time (≈₹4.5–5L total collected) by day 90**, with 2–3 active resellers feeding months 4–6. Conservative: ₹50–60K MRR. Stretch (one 15+ outlet chain): ₹2L+ MRR.

Month 1 is deliberately small: its job is 10–15 **reference sites** (even discounted/free) whose case-study content powers months 2–3.

**Weekly KPIs:** screens installed · paying screens · WhatsApp conversations started · demos booked · reseller conversations · churn (a dark screen = same-day call; use impression reporting as the health signal).

---

## 7. Product Next Steps (only what GTM needs)

1. **Self-serve demo** — prospect sees their own menu on screen within 10 minutes of a WhatsApp chat.
2. **Fleet health view in the CMS** — online/offline + last sync per screen (player already reports impressions; surface them). Resellers need this to manage 30+ screens.
3. **MQTT security fix + DPDP-safe analytics one-pager** ("what we collect, what we never store").
4. Defer everything else (code consolidation etc.) until ~100 screens.

**The single most important action: 10 real screens in real shops within 3 weeks, even free.** Every other part of the plan depends on reference sites existing.

---

## Sources

- [Coherent Market Insights — India digital signage market](https://www.coherentmarketinsights.com/market-insight/india-digital-signage-system-market-4857)
- [Coherent — $2.34B by 2032 press release](https://www.coherentmarketinsights.com/press-release/india-digital-signage-system-market-4122)
- [Grand View Research — India digital signage outlook](https://www.grandviewresearch.com/horizon/outlook/digital-signage-market/india)
- [Yodeck pricing](https://www.yodeck.com/pricing/) · [Yodeck 2026 price increase](https://brixsignage.com/blog/yodeck-price-increase-2026/)
- [OptiSigns pricing](https://www.optisigns.com/pricing)
- [CastHub — signage cost per screen 2026](https://cast-hub.com/digital-signage-cost-per-screen/)
- [FootfallCam India pricing (IndiaMART)](https://www.indiamart.com/proddetail/footfallcam-people-counter-5050751848.html)
- [FootfallCam in the India market](https://technowavegroup.com/why-footfallcam-is-the-best-people-counting-solution-for-the-india-market/)
