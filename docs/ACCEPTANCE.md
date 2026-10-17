# Acceptance and verification specification

Baseline 1.0. **Target-device execution results pending.** Local build and security checks are recorded in [build status](BUILD-STATUS.md). This document defines product acceptance; local checks do not substitute for actual-TV testing.

## Requirement coverage

| Scenario group | Requirement IDs | Observable result |
|---|---|---|
| A01 First run / apps | SETUP-01, APP-01 | Useful remote-only setup, pin/launch/uninstall behavior |
| A02 Personal home | DASH-01, PERS-01, NAV-01 | Persisted customization with stable D-pad focus |
| A03 PC save | PC-01, PC-02, SEND-01 | Pair/save/acknowledge; conflicts and lost acknowledgements handled |
| A04 Native viewing | PLAY-01, CW-01, CW-02, CW-03 | Genuine progress, resume, track controls and stable identities |
| A05 NowShowing / websites | NS-01, NS-02, NS-03, WEB-01 | Search/episode/source flow, truthful status and fallback |
| A06 YouTube | YT-01, YT-02, YT-03 | Version-appropriate route works; unimplemented optional routes not claimed |
| A07 Backup/privacy | DATA-01, PRIV-01 | Recoverable import, independent clear operations, no credentials leaked |
| A08 Device performance | PERF-01 | Measured startup, session stability and memory trend |
| A09 Protection | BLOCK-01, BLOCK-02 | Measured Brave/filter support and per-site exception behavior |
| A10 Launcher | LAUNCH-01 | Opt-in default selection and normal-app rollback |
| A11 Discovery/widgets | DISC-01, WIDGET-01 | Editable suggestions, bounded refresh and honest stale/offline state |
| A12 Future scope | LANMEDIA-01, PROFILES-01, CLOUD-01 | Not part of initial acceptance; require separate future criteria |

## Detailed scenarios

### A01 — Setup and apps
Given a fresh installation with no key, pairing or history, when the owner opens Apps and pins one, Home shows it after restart. Back and D-pad work throughout. Uninstalling the target later leaves an editable unavailable shortcut; Nook does not crash.

### A02 — Personalization and navigation
Given two named collections with mixed item types, when the owner reorders rows and hides one, changes survive process death/reboot. Removing an item from one collection leaves other memberships and watchlist intact. Changing the home layout while a player is active does not stop playback. Returning restores focus where possible.

### A03 — Pairing and edits
Given no session, config/history requests and mutations are rejected. An expired/incorrect code cannot create a session; valid code still needs TV approval. Revoked sessions fail immediately.

Given two editors reading revision N, the first save creates N+1. The second stale save is rejected without data loss. Retrying the same acknowledged or uncertain mutation ID does not duplicate a tile. A lost response is resolved using current revision/status.

Given Nook hands off to another app, the editor reports disconnection, preserves its draft and does not queue automatic mutations. On return, it checks the current address/session/revision. Save a link adds it without interrupting TV playback.

### A04 — Native playback and history
Use permitted known test media with stable duration and seek support. Watch, pause, seek, exit, reopen, force-stop and restart. Resume matches the last committed position within 5 seconds; document source seek accuracy separately. No progress is claimed after the last committed save.

Different episodes maintain separate state. Source switching retains identity and episode but does not reuse a timestamp across incompatible cuts. A live/unknown-duration item has no invented percentage. Remove, mark watched and start over each behave distinctly. Pausing history prevents new history writes while explicit saves still work.

### A05 — NowShowing and web sources
Search a movie and series; inspect metadata, change episode, choose source and retry. Opening details alone does not add progress. Unsupported ID formats fail with an actionable reason before constructing a bogus URL.

An iframe load without playback proof stays Unknown/Loading, not Playing. A documented message with incorrect origin/session/media ID is ignored. An opaque embed offers Open again with saved episode and no fake timestamp.

Test no network, metadata quota, missing poster, provider timeout, invalid embed, media failure and external browser return. Outer controls and Back remain usable with the remote. Record each provider's observed capability.

### A06 — YouTube
Save video/channel/playlist links including supported shortened forms; reject malformed targets. Installed YouTube receives the intended route where supported; missing handler offers browser. Returning does not invent watched state. Optional API search handles quota; optional embed respects official integration requirements. Public-account history import is not a test promise.

### A07 — Data and boundaries
Export/import with Unicode labels, missing apps and a background. Exports omit secrets and optional history unless chosen. Invalid/newer schema, oversized JSON/image and corrupt asset leave original state unchanged. Replace import has a recovery snapshot.

Cache clear preserves saved lists/progress. Per-site browser data clear preserves Room data. History clear does not delete watchlists. Test unauthorized origin/host, automatic external scheme, popup and untrusted page attempts to access editor privileges.

### A08 — Performance protocol
Device: actual owner's TV; record API/ABI/WebView, display, APK version and build type.
Fixture: 8 rows, up to 20 tiles each, mixed local/remote artwork, 20 saved viewing entries, 10 installed-app favorites where available. Record any unavailable fixture element.
Cold launch: app process stopped; existing data/cache retained; measure launch intent to usable focused Home over 20 runs. Target at least 18 runs under 3 seconds.
Edit: five successful LAN saves, UI reflection within 2 seconds after acknowledgement.
Playback: 2 hours native video plus repeated player open/close. Record network, codec, crashes, buffering and periodic PSS samples. After initial 15-minute warm-up, investigate sustained growth; provisional threshold: final steady-state PSS no more than 20% above first comparable steady-state window, without repeated low-memory kills. Compare equal playback/navigation states.
An external provider's server delay is recorded separately from Nook startup/UI time; it is not erased from the owner-experience report.

### A09 — Web protection
Record site/movie/episode, date, route, ruleset version and filtering off/on behavior. Verify per-site disable recovers a broken player without clearing saved data. Test blocked popup/redirect controls. Brave route passes only after installation, launch, remote input and return tests. Do not claim removal of every ad or full browser-extension coverage.

### A10 — Launcher
Only after owner opts into testing: record existing default, enable Nook home option if supported, exercise Home and reboot, then restore original default. Ordinary app launching stays available. If firmware prevents selection, document standalone behavior and limitation; do not disable system components automatically.

### A11 — Discovery and widgets
Owner-selected interests produce editable rows; suggestions explain genre/source basis. Curated seeds are not described as current trends. Weather handles permission/location choice, offline/stale state and bounded refresh. Disabling widgets releases their periodic work.

## Release evidence

Record requirement/scenario ID, commit/APK, device, fixture, steps, expected/actual result, pass/fail/blocked, evidence and issue reference. A blocked test is not a pass. Mark routes separately: native, web embed, YouTube external and Brave/browser.

First usable release requires all P0 scenarios passed on device. Full scope also records the outcomes and owner-accepted limitations described in [PRD](PRD.md). No unresolved crash, data loss, unauthorized editing or navigation trap is acceptable.

## Documentation verification

Check all relative links, requirement coverage, current source selection, authority/hold wording and consistency between release definitions. This is the only verification executed during the current documentation-only task.

