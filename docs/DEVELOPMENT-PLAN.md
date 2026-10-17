# Development plan and verification gates

2026-10-17 | Documentation baseline v1.0 finalized. **The owner has authorized implementation. M1 preview is built; device gate is pending.** See [build status](BUILD-STATUS.md).

## Delivery policy

Build the smallest complete household workflow first, then expand integrations. Standalone operation is the primary path. Actual-TV observations decide compatibility claims. No calendar deadline has been agreed.

## Milestone 0 — Documentation baseline (this change)

Deliverables:
- README, PRD, requirements analysis, architecture, this plan and selected-source audit.
- Explicit NowShowing-Stream baseline.
- Capability-aware Continue Watching and source requirements.

Gate: document links resolve, priorities and measurable acceptance criteria present, unsupported integration promises removed.

## Milestone 1 — Device compatibility APK

Tasks:
1. Inspect repository instructions, available Android SDK/JDK/build tooling and current official library documentation.
2. Inspect upstream license before copying source/assets.
3. Create Gradle wrapper, stable app ID, normal TV entry and build CI.
4. Diagnostic screen: actual API level, ABI, memory class, WebView version, codec availability and app handlers; redact identifiers from exports.
5. D-pad focus demo with rows, detail screen and Back restoration.
6. Sample native MP4, HLS and DASH from known permitted test sources.
7. Isolated web screen using a controlled test page, then user-selected provider trial.
8. YouTube URL handoff.
9. Foreground LAN editor proof: pair with TV approval, authenticated read/write, revoke.
10. Optional home-intent discovery only; do not replace stock launcher during compatibility tests.

Gate: APK installs on the owner's TV; diagnostics and remote checklist captured; sample playback and pairing exercised. Record failures and fallback decisions.

## Milestone 2 — Personal dashboard and editor

Tasks:
- Durable layout, favorite apps, links, background, editable personal collections and empty states.
- Remote-only first run, pin first app, browser Save for later without interrupting playback.
- Browser editing, preview, Save acknowledgement and revision conflicts.
- Typed config validation and versioned import/export.
- History privacy controls, paired-device management and cache quotas.
- Return-to-item focus/scroll restoration.

Gate: PC customizes a layout; TV reflects it; restart/offline preserves it; browser revocation works; no mouse needed for core TV flows.

## Milestone 3 — Native playback and viewing state

Tasks:
- Media3 controls, audio/subtitles when present, media buttons.
- Canonical movie/episode identities and playback state machine.
- Native resume, watched/remove/start-over actions.
- Separate Recently opened for unobservable/external playback.
- Error/retry behavior and network interruption recovery.

Gate: progress survives normal exit and process death; resume within PRD tolerance; unknown progress never shown as a percentage; live/unknown-duration cases behave correctly.

## Milestone 4 — NowShowing-Stream integration

Tasks:
- Reimplement/port verified metadata and source-building behavior subject to license.
- Native search, detail, watchlist, season/episode selection.
- Explicit source capability and ID-format registry.
- Validate every enabled provider's movie/series URL contracts.
- Embed screen with remote-accessible source/retry/browser controls.
- Documented player-event adapters only where available.
- User-configured OMDb key, caching, request deduplication and quota states.
- Optional versioned migration from upstream lists; imports classified as recent opens unless real progress exists.

Gate: chosen providers tested individually on actual TV; every row has accurate state; no generic iframe-success claim. Disabled/unverified providers clearly identified.

## Milestone 5 — YouTube and web protection

Tasks:
- Polished video/channel/playlist favorites and app/browser handoff.
- Evaluate official API browsing, credentials/quota and embedded player separately.
- Brave detection/handoff and input compatibility.
- Separate filter-engine proof, license/memory review and test rules.
- Per-site exceptions, popup/redirect controls, last-good ruleset.
- Provider compatibility matrix including filtering off/on.

Gate: no claim of account-wide YouTube history sync, universal ad blocking or inherited Brave Shields. Filtering can be disabled without resetting user data.

## Milestone 6 — Personalization, launcher and release

Tasks:
- NowShowing-derived mood discovery and suggestions built on the existing editable P0 collections.
- Optional weather/photos, bounded refresh and offline states.
- Explicit opt-in home-launcher test with documented rollback.
- Accessibility and 2 GB performance profiling.
- Stable signed APK; update/data migration; install/update/rollback docs.
- Release notes, screenshots, source capability matrix and known issues.

Gate: owner acceptance on actual TV, repeatable build, signing continuity, no critical unresolved crash/navigation/data-loss issue.

## Test strategy

| Area | Meaningful tests |
|---|---|
| Source URLs | IMDb/TMDB separation, missing IDs, encoding, season/episode bounds, provider-specific formats |
| Progress | Pause/seek/end, duration unknown, live stream, duplicate events, episode/source switch, process death |
| Metadata | Quota, timeout, bad JSON, stale cache, deduplication, missing posters, pagination |
| Pairing | Expiry, brute-force throttling, TV denial, revocation, origin/host rejection, unauthenticated mutation |
| Editor | Invalid config, oversized upload, stale revision, import rollback, concurrent saves |
| Web boundary | Unexpected schemes/popups, invalid event origin/session, no privileged bridge, Back escape |
| Lifecycle | Leaving/reopening app, external player return, network loss, reboot, low-memory recovery |
| UI | D-pad reachability, focus restoration, long labels, 1080p safe margins, disabled/empty/error states |
| Performance | 20 cold starts, representative artwork-heavy home, 2-hour playback, repeated player open/close |
| Release | Upgrade old data, signed APK identity, export/import and rollback instructions |

Do not add tests that only repeat implementation details. Unit tests prove contracts; instrumentation and device checks prove actual focus/playback behavior.

## Release evidence template

- APK version, commit and build environment:
- TV actual API level/ABI/WebView:
- Remote model/buttons exercised:
- Native sample formats passed/failed:
- Selected provider movie and episode results:
- Resume and restart results:
- Pair/edit/revoke results:
- Filter/Brave results (if included):
- Startup samples and playback/memory observations:
- Launcher result and rollback (if tested):
- Known limitations and owner acceptance:

Until populated, these are pending checks, not passed tests.

## Release boundaries and dependencies

M1 proves compatibility; it is not a production release. M2-M4 plus the P0 YouTube handoff from M1/M5 form the first usable release. M5 web protection and M6 launcher resolve broader requested scope. Do not label the project complete simply because a P0 APK is available.

Implementation sequence may bring YouTube favorites forward into M2. P0 YouTube does not wait for optional API browsing or embedded-player research. P0 collections likewise do not wait for the mood engine.

No third-party source is marked supported before successful actual-TV playback and D-pad checks. Device-test results may narrow supported sources, but any change to the user's core requested experience must be recorded and discussed rather than hidden behind a fallback.

## Handoff and acceptance

Read [implementation handoff](IMPLEMENTATION-HANDOFF.md) before execution. [Acceptance scenarios](ACCEPTANCE.md) map requirement IDs to observable checks. The docs-only review validates these documents; it does not execute any milestone above or populate device evidence.

