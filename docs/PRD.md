# Nook TV — Product Requirements Document

Version 1.0 scope | 2026-10-17 | Owner: Ismail Hossain | Status: implementation authorized; M1 compatibility preview built

This is the planning baseline, not evidence that all features are implemented or device-verified. The owner has now instructed implementation. [Build status](BUILD-STATUS.md) records actual coverage; [decision record](DECISIONS.md) distinguishes requirements from implementation choices.

## 1. Problem statement

Ismail's TV starts with an interface that does not fit his preferences. Apps, videos, movies, series, and saved links need an organized personal home that can be managed comfortably from a PC and used with a standard TV remote. Opening disconnected services and repeatedly finding unfinished content creates daily friction.

## 2. Product vision and decisions

Nook TV is a personal entertainment dashboard, delivered as a standalone Android APK with optional launcher support. The TV owns the saved configuration and viewing state. A browser on the same home network edits that state.

Confirmed decisions:
- Primary device reports Android 14 and 2 GB RAM; hardware capabilities remain unverified.
- Personal dashboard rather than just an app grid.
- Continue Watching, YouTube, online video sites, installed apps, and browser customization.
- Online streaming reference: https://github.com/smile-plzz/NowShowing-Stream only.
- Brave-style protection is desired, but embedding Brave Shields is not assumed feasible.
- No cloud account or always-on PC required for core use.

## 3. Goals and success targets

Targets are proposed acceptance goals, not measured results.

| Goal | Target | Measurement |
|---|---|---|
| Comfortable sofa operation | Every core action works using D-pad, OK, Back and available media keys | Real-TV task checklist |
| Fast familiar home | Cached home usable within 3 seconds for at least 18 of 20 cold launches | Debug timing, representative saved dashboard |
| Simple customization | Edit row order and save in under 60 seconds after pairing; TV reflects acknowledged save within 2 seconds | Five local-network trials |
| Reliable progress | Resume within 5 seconds of the last saved position for supported native VOD | Pause, exit, process-kill and restart cases |
| Stability | 2-hour playback session without app crash or sustained memory growth after warm-up | Actual-TV profiling |
| Personal value | Owner uses Nook on at least 5 of the first 7 days and rates access to preferred content at least 4/5 | Voluntary owner feedback, no telemetry |

## 4. Users and stories

### TV owner
- As Ismail, I want unfinished content visible first so I can pick up quickly.
- I want favorite apps, playlists, channels and sites together so I spend less time navigating.
- I want movies and series organized like NowShowing, adapted to a remote.
- I want the app to retain my layout and history across restarts and network failures.
- I want clear source errors and a way to retry or change playback route.

### PC editor
- As Ismail at my PC, I want to type URLs, organize rows and choose backgrounds without using an on-screen TV keyboard.
- I want a preview, explicit Save, and confirmation that the TV accepted the change.
- I want to revoke a paired browser and export settings.

### Shared-room use
- As the owner, I want to hide or clear personal history before sharing the screen.
- I want visitors on the network unable to edit my dashboard without pairing.

## 5. Information architecture

Default home order:
1. Header: clock, personal background, optional weather.
2. Continue Watching, only for resumable items.
3. Favorite apps.
4. Watchlist / Play Later (Nook's own list).
5. YouTube favorites.
6. NowShowing movies and series.
7. Personal-interest collections.

Navigation: Home, Watch, YouTube, Apps, Saved, Settings.
Sections can be hidden or reordered. Empty sections offer setup or remain hidden. No autoplay hero video.

## 6. Requirements and acceptance criteria

P0 = first usable release; P1 = subsequent planned scope; P2 = optional future scope. Completing P0 alone does not complete the requested product. Launcher support and web protection remain requested outcomes: each needs a tested implementation or a documented limitation accepted by the owner before full-scope closure. Compatibility prototypes may test P1 capabilities without making them release promises.

| ID | Priority | Requirement | Acceptance criteria |
|---|---|---|---|
| PERS-01 | P0 | Personal interest collections | Create, rename, reorder and remove collections; pin supported links, titles, apps and playlists; user explicitly selects content; changes persist without PC |
| DASH-01 | P0 | Personal dashboard | Reorder/hide rows and pin tiles from browser; saved layout survives process death and reboot |
| NAV-01 | P0 | Complete remote operation | Every core control reachable; focus visible; Back returns predictably; returning from player restores prior item and scroll |
| APP-01 | P0 | Installed app favorites | Discover launchable TV/mobile activities as permitted; launch or explain unavailable/uninstalled app |
| CW-01 | P0 | Accurate Continue Watching | Known-progress VOD shows position, duration and Resume; unavailable progress uses Recently opened, without a percentage |
| CW-02 | P0 | Durable progress | Save every 5 seconds during native playback and on pause/seek/stop where lifecycle allows; restart restores last committed state |
| CW-03 | P0 | Viewing-state controls | Resume/start over/remove/mark watched; proposed watched threshold 95% for finite VOD; live sources have no fabricated completion |
| PLAY-01 | P0 | Native direct-stream playback | Test MP4/H.264/AAC plus representative HLS and DASH; seek, pause, error recovery, subtitle/audio selection when tracks exist |
| NS-01 | P0 | NowShowing catalog | Search, movie/series detail, watchlist, season/episode selection; at least one enabled source passes movie and series-episode playback tests on the TV, or owner explicitly accepts a documented narrower release |
| NS-02 | P0 | Source selection | Remember selected source by stable ID; distinguish selected/loading/confirmed-playing/failed/unknown |
| NS-03 | P0 | Honest source fallback | Show timeout as inconclusive; offer retry/source switch/browser; HTTP 200 or iframe load never means verified playback |
| YT-01 | P0 | YouTube favorites and app handoff | Save video/channel/playlist URL from PC; validate link type; open supported installed handler or browser fallback |
| WEB-01 | P0 | Online video sites | Per-site launch route: isolated web screen or external browser; unknown playback capability clearly shown |
| PC-01 | P0 | Paired local browser editor | TV-displayed address/code; explicit TV approval; edit rows/tiles/links/background; validation and save acknowledgement |
| SETUP-01 | P0 | Useful first run without PC | Open Apps, pin a favorite and return Home using the remote; pairing and richer customization may be completed later |
| PC-02 | P0 | Revision-safe edits | Two editors cannot silently overwrite one another; stale save offers reload/reapply |
| SEND-01 | P0 | Save from PC for TV | Add a supported URL to a collection or Play Later; validate scheme/target, allow label editing, confirm save without automatically interrupting TV playback |
| DATA-01 | P0 | Local persistence and backup | Export/import versioned settings and lists; invalid import leaves existing data intact |
| PERF-01 | P0 | 2 GB device suitability | Lazy images, bounded caches, one active player/WebView, no network or image decoding on main thread |
| PRIV-01 | P0 | Personal controls | Pause history, clear history/cache separately, revoke paired devices; secrets excluded from exports/logs |
| LAUNCH-01 | P1 | Optional home launcher | Explicit opt-in; test Home/default selection and rollback; no automatic stock-launcher disabling |
| YT-02 | P1 | Rich YouTube discovery | Official API for supported metadata/search where configured; quota/error handling; no claimed import of account history |
| YT-03 | P1 | Embedded YouTube | Only after real-device remote/player test; official player behavior; own observed session progress kept distinct from account-wide history |
| BLOCK-01 | P1 | Brave handoff | Detect eligible handler, test launch and Back; show honest installation/compatibility status |
| BLOCK-02 | P1 | Embedded web filtering | Separate opt-in filter engine; global/per-site controls; popup/redirect controls; documented compatibility and rollback |
| DISC-01 | P1 | Mood discovery and suggestions | Build on PERS-01 collections with NowShowing-derived mood matching; show recommendation reasons; user can hide suggestions; no assumed account-wide viewing history |
| WIDGET-01 | P1 | Weather and ambient photos | Optional location; bounded refresh/cache; cached/stale/offline state; background image resizing |
| LANMEDIA-01 | P2 | LocalFlix / PC media | Separate authenticated media-serving integration, only when an actual server/API exists |
| PROFILES-01 | P2 | Household profiles | Separate lists/history/settings; no speculative multi-user backend in v1 |
| CLOUD-01 | P2 | Remote access/sync | Separate opt-in architecture and threat model; no router port forwarding in initial setup |

## 7. Playback and source behavior

Native experience means native dashboard, catalog and controls where Nook owns playback. It does not imply converting every embedded website to a native media stream.

- Native direct media: precise progress from Media3.
- Cooperating embed: progress only via documented, validated events; seeking only when supported.
- Opaque embed: selected title/episode and last-opened state; no timestamp claims.
- External YouTube/browser/app: handoff with launch history, not automatic playback history.
- Live content: reopen channel; DVR resume only where explicitly supported.
- A blocked source cannot crash, freeze, or erase the dashboard state.

YouTube playback remains in the official app initially. Nook's Play Later is distinct from YouTube Watch Later. The public YouTube Data API does not expose account watch-history/Watch Later lists.

## 8. Scope boundaries

- Core use does not depend on replacing the stock launcher.
- No universal progress scraping across external apps.
- No guaranteed native conversion of arbitrary iframe sites.
- No Chromium/Brave browser fork for v1; filtering is a separate implementation, not a Shields switch.
- No DRM bypass, video hosting, transcoding or downloading initiative.
- Source availability, embedding permission, account requirements and device DRM are tested individually.
- No promise of ad-free YouTube through the official API integration.
- Preserve NowShowing-Stream upstream; implementation changes belong in Nook unless separately requested.

## 9. Error and empty states

Provide actionable states for: first launch, no saved content, no paired browser, disconnected network, missing external app, expired URL, source unavailable, unsupported codec, metadata quota reached, invalid import, stale editor revision and revoked session. Retry must not duplicate entries.

## 10. Dependencies and open questions

| Question | Owner | Blocking scope | Resolution |
|---|---|---|---|
| Real API level, ABI, WebView and decoders? | Engineering + owner device test | Device compatibility | Diagnostic screen and sample playback |
| Which configured NowShowing providers work here? | Engineering + owner | Provider release claims | Movies/episodes and remote tests on actual TV |
| Does Brave install and navigate acceptably? | Engineering + owner | Brave route only | Sideload/handler/input test; external browser fallback |
| Does firmware honor home intent/default choice? | Engineering + owner | Launcher only | Explicit opt-in test after standalone works |
| Which YouTube app/handlers are installed? | Engineering | YouTube route | Detect and exercise handoff |
| Which initial channels, playlists and interests? | Owner | Content personalization, not development | Empty editable starter collections; no invented personal feeds |
| What upstream reuse license applies? | Engineering | Copying code/assets | Inspect LICENSE/package headers before reuse; reimplement behavior if unresolved |

No deadline was supplied. Delivery is gated by working milestones, not guessed calendar dates.

## 11. First-release and full-scope definitions

The first usable release satisfies all P0 requirements and the relevant [acceptance scenarios](ACCEPTANCE.md). It is a staged delivery, not completion of all requested integrations. Full requested scope additionally resolves LAUNCH-01, BLOCK-01/BLOCK-02 and the native YouTube experience: implement and validate the chosen route or record the owner's acceptance of a specific limitation. Optional widgets, richer discovery and P2 ideas do not silently become release blockers.

Native YouTube browsing with official-app playback is the proposed initial interpretation, not a claim that the owner requested an external handoff. Embedded playback remains a separately evaluated route. Neither route promises account-wide history import.

## 12. Release definition

Signed, versioned APK; installation/update/rollback instructions; actual-TV acceptance record; documented source capabilities; export/import verified; known limitations; reproducible build instructions and dependency/license inventory. Passing emulator tests alone is insufficient.

## 13. Operational product rules

- Core personalization uses explicit choices; it must not import private personal context, account data or inferred interests automatically.
- A PC is required only while editing. Online media still requires internet; cached layout working offline does not imply offline streaming.
- The initial editor is available only while Nook is foregrounded, including its own player screens. Launching YouTube/Brave may disconnect the editor; returning restores access. No silent queued edits are applied later.
- Opening details does not create a playback-progress record. A recent-open record begins on explicit playback/handoff request; failed attempts remain labeled as such.
- Keep selected episode per series independently from verified watched status. Closing/reopening an unobservable player can restore the selection without claiming timestamp resume.
- Personalization should feel immediately useful: first run exposes installed apps and lets the owner pin a favorite without metadata keys or browser pairing.
- No runtime cloud AI or paid AI service is required for interests or mood suggestions.

## 14. Document ownership

PRD owns scope and requirement IDs; [UX](UX-SPEC.md) owns interaction detail; [architecture](ARCHITECTURE.md) owns technical proposals; [acceptance](ACCEPTANCE.md) owns verification scenarios; [handoff](IMPLEMENTATION-HANDOFF.md) owns execution readiness and the implementation hold. If they conflict, follow explicit owner instructions, then PRD, and resolve the other documents before implementation.

