# Product decisions and scope record

Documentation baseline 1.0 — 2026-10-17.

## Authority

The owner's explicit instructions take precedence. PRD owns current scope and acceptance requirements; other documents explain how to deliver and verify it. Finalized documentation is not owner acceptance of every proposal and is not authorization to start implementation.

## Decisions

| ID | Decision | Status / basis | Consequence |
|---|---|---|---|
| D01 | Nook TV is a personal dashboard, not only an app launcher | Owner-confirmed | Collections and layout customization are core |
| D02 | Standalone APK with optional launcher support | Owner-confirmed | Normal launch must always work; launcher is separately tested |
| D03 | Use NowShowing-Stream | Owner-confirmed correction | Do not substitute CineVault, CineGemini-Stream or V2 |
| D04 | PC browser customization | Owner-confirmed | Editor manages TV-owned data |
| D05 | Continue Watching, YouTube and online streaming | Owner-confirmed outcomes | Source-specific capability handling |
| D06 | Brave/ad-blocking support desired | Owner-confirmed outcome | Test Brave handoff and separate filtering; no universal promise |
| D07 | Android 14 / 2 GB target | Owner-reported | Actual API/ABI/decoders still pending |
| D08 | Local-first, same-LAN editor, no required cloud | Proposed engineering default | PC can be off during ordinary TV use |
| D09 | Native catalog, Media3 for direct streams, isolated web embeds | Proposed architecture | Avoid treating all sites as direct media URLs |
| D10 | YouTube official-app handoff first | Proposed staged interpretation | Native browsing yes; embedded playback evaluated; owner acceptance required for scope reduction |
| D11 | Kotlin, Compose candidate, Room and vanilla TypeScript editor | Proposed implementation | Validate hardware/toolchain before committing to versions |
| D12 | No background editor server initially | Proposed scope/performance tradeoff | External-app handoff disconnects editor |
| D13 | Explicit personal choices rather than imported profile data | Product privacy default | Start with editable collections; no silent account or personal-context ingestion |
| D14 | Build only when the owner says to start | Released: owner said “initiate the build accordingly” on 2026-10-17 | Implementation and APK work are now authorized |
| D15 | M1 used Java native Views, platform VideoView and atomic JSON config | Historical compatibility choice | Preview 0.2 replaces VideoView with Media3; atomic versioned JSON remains authoritative instead of Room |
| D16 | Continue later phases without current TV access | Explicit owner instruction on 2026-10-17 | Use hosted builds/emulator tests; retain all physical-device gates as pending |
| D17 | Dashboard schema v2 plus independent viewing ledger | Engineering implementation | Backward migration, persistent receipt/recovery envelope, per-media native progress; no invented external progress |

## Release vocabulary

- Documentation complete: specification reviewed for consistency and committed.
- Compatibility APK: limited test build after authorization; not a completed product.
- First usable release: all P0 criteria verified on target hardware.
- Full requested scope: P0 plus resolution of launcher, web protection and native YouTube experience; unachievable behavior requires documented owner acceptance of the limitation.
- P2 ideas: optional, not implied obligations.

P1 timing is a delivery proposal. It must not be used to silently remove an explicit request.

## Open decisions and default actions

| Topic | Default | Evidence needed / when |
|---|---|---|
| Specific interests/channels/playlists | Empty customizable collections and optional sample labels | Owner fills these during onboarding; not a build blocker |
| YouTube stays inside Nook? | App handoff for first release | Evaluate embedded route during integration; revisit if owner requires in-app playback |
| Brave viability | Optional handler, other browser fallback | Real TV install/input test |
| Accurate web progress | None until provider proves support | Documented events plus observed resume |
| Source availability and IDs | Unverified | Provider-by-provider integration checks |
| Local HTTPS | HTTP with bounded non-secret editor scope initially | Transport review before adding credential-bearing controls |
| Play Now from PC / remote control | Not part of P0; Save for later only | Add only if owner requests or approves added scope |
| Multiple household profiles | P2 | Later owner request |
| Toolchain and UI toolkit | Preferred candidates, no version pins yet | Authorized compatibility milestone |
| Upstream code reuse | Behavioral reference until license checked | Before copying code/assets |

## Change discipline

Record the trigger, affected requirement IDs, new behavior, acceptance changes and implementation impact. Update PRD and relevant companion documents together. External constraints should be reported as constraints, not attributed to an owner decision. No calendar or budget commitments have been supplied.

