# Launcher acceptance and verification

Baseline 2.1 | 17 October 2026 | Brief target-TV observations recorded; comprehensive acceptance and iPhone transfer pending

The 0.2 build has existing contract/emulator evidence, and the owner reports brief BlueStacks boot/exploration. Neither proves the new launcher experience. This document defines future acceptance against [PRD](PRD.md).

## Coverage

| Scenario | Requirement IDs | Gate |
|---|---|---|
| A01 Useful first run | SETUP-01, APP-01, YT-01 | P0 |
| A02 Apple TV-style Home | VIS-01, DASH-01, PERS-01 | P0 |
| A03 Remote actions | NAV-01, APP-01 | P0 |
| A04 Default Home/recovery | LAUNCH-01 | P0 |
| A05 From iPhone | PHONE-01, PHONE-02 | P0 |
| A06 Return to media | RECENT-01, CW-01, CW-02, CW-03, PLAY-01 | P0 |
| A07 Companion/backup/privacy | PC-01, PC-02, SEND-01, DATA-01, PRIV-01 | P0 |
| A08 Performance | PERF-01 | P0 |
| A09 Drive/Spotify | DRIVE-01, DRIVE-02, SPOT-01 | Conditional P1 |
| A10 Retained broader scope | NS-01, NS-02, NS-03, WEB-01, YT-02, YT-03, BLOCK-01, BLOCK-02 | P1 |
| A11 Optional future | DISC-01, WIDGET-01, LANMEDIA-01, PROFILES-01, CLOUD-01 | P2, separate delivery |
| A12 Dynamic Home | DYN-01, DYN-02, VIS-01, PERF-01 | P0 |
| A13 Companion refinement | PC-03, PC-02, DATA-01 | P0 |
| A14 Personal video shelf | PHONE-03, PHONE-02, RECENT-01, PRIV-01 | P0 |
| A15 Optional receiver | AIR-01 | Optional P1 |
| A16 Shortcut discoverability | NAV-02, NAV-01 | P0 |

## A01 - Useful first run

Fresh install with no cloud account/PC/key: launchable apps appear, owner pins/reorders YouTube where installed and opens it with one OK. Missing app has an actionable state; no fake installed tile. Spotify/Drive sign-in never blocks Home. Pairing and default-home setup can be revisited from Settings.

## A02 - Visual and layout acceptance

Review actual screenshots/prototype and remote use with owner: recognizable artwork/grid, comfortable spacing/type, stable contrast, obvious focus and restrained motion. Home has no test samples/diagnostics/API setup. Continue Watching hides when empty. Shared & Recent and From iPhone are easy to locate.

Change order/background/collections, restart/force-stop/reboot and verify persistence. Unicode/long names/missing artwork do not clip controls. Collection removal preserves source items/files. Visual approval is explicit owner feedback, not a screenshot-exists test.

## A03 - Navigation and direct actions

D-pad reaches every grid tile, row, Settings and context alternative. OK launches app; management requires a deliberate secondary action. Back closes overlay/player before leaving destination and restores exact item/scroll when possible. Removed item selects nearest surviving item. No pointer or long press is the sole path. Home resets to documented start focus; Back return from external app restores originating app tile.

## A04 - Default Home and recovery

On actual TV, record previous launcher and supported selection flow. Select Nook through system-supported UI. From YouTube/another app press Home, then test repeated Home, root Back, settings return, process recreation, wake and reboot. Nook returns without empty screen or navigation trap. Ordinary app entry remains.

Restore original Home successfully. No root, automated system-package disabling or hidden interception. Firmware prevention is Blocked/Fail against core launcher requirement; standalone fallback needs explicit owner acceptance before narrowing scope.

## A05 - iPhone-to-TV media

Use real owner-selected iPhone source/file, note sender app/gesture and receiver route. TV offers accurate same-network/permission instructions. Complete one video sharing path and verify image/audio/orientation/remote controls.

For retained-file transfer: pair with approval, select file in Safari, show progress/cancel, verify free-space/size limits and finalize only complete files. Cancel/network loss must clean partials. Receiving must not auto-interrupt current playback. Test representative iPhone formats, including HEVC where relevant; unsupported codec gives useful error rather than fake success.

For AirPlay/Cast/external receiver: record actual receiver/device/protocol and supported media. Do not substitute upload and call it AirPlay. Test phone lock/disconnect and return to Nook. An unobservable external session has no invented Nook position.

## A06 - Return and native playback

Retained video plays after phone disconnect and TV process restart, resumes within 5 seconds of last committed measured position, and survives same-key upgrade. Multiple files/episodes have separate identities; replacing content does not inherit an incompatible timestamp.

Live phone session without retained source says Reconnect phone/Start sharing again. A stale URL is not presented as guaranteed replay. External apps say Open in app; details alone do not create progress.

Exercise MP4/HLS/DASH permitted test media, pause/seek/error/retry and supported tracks. Unknown/live duration has no percentage. Mark watched, Start over and Remove differ. History paused stops history writes; history clear preserves retained files and collections. Delete saved copy explicitly removes bytes and updates availability without crashing.

## A07 - Companion, migration and boundaries

Pair code needs TV approval; invalid/expired/denied/revoked sessions cannot read/write private state. Host/Origin/CSRF/size checks remain. Two editors at revision N: first commits, second conflicts without draft loss. Lost-save retry is idempotent.

Leaving foreground disconnects current companion; draft preserved, no silent queued mutation. Link Save does not start playback. Phone Safari companion layout fits usable controls.

Upgrade schema-v1/v2 data into new launcher mapping without losing favorites/background/collections/progress. Invalid/newer/corrupt import leaves original intact; pre-import recovery works. Dashboard export excludes credentials and media payloads, states missing references clearly. History, thumbnail cache, files and account/session clears remain separate.

Reject traversal/invalid MIME/oversized/interrupted upload. No privileged bridge from external web pages or access to editor credentials. Google tokens never appear in backup/editor/QR/logs/foreign-origin requests. No silent Photos or connected-account import.

## A08 - Device performance

Use actual reported 2 GB TV; record API/ABI/WebView/display/version and fixture.
- Fixture: 12 favorite apps where available, 20 shared/recent items, 4 optional collections and representative resized artwork; record differences from fixture.
- 20 cold starts with cache/data retained: at least 18 usable focused Homes under 3 seconds.
- Five LAN layout saves: reflect within 2 seconds after acknowledgement.
- Two hours native playback plus repeated app/Home/receiver/player switching; record crashes, network/codec, buffering and comparable PSS samples.
- After 15-minute warm-up investigate sustained memory growth; provisional final comparable steady-state PSS target within 20% of initial window, without repeated low-memory kills.
- Transfer a supported large file while observing memory/disk bounds; do not require full-file buffering.
- Offline Home/apps remain usable; missing sources do not freeze grid.

Separate network/server delays from UI timing, but retain them in the owner-experience report.

## A09 - Conditional Drive and optional Spotify

Document actual OAuth client/scopes and how existing selected files are authorized. Prove one selected file's metadata/access, playback and range seek; test token expiry/refresh, revocation, deletion, changed version and network loss. Reopen by durable file ID, not expired bearer URL. Disconnect clears credentials and updates source availability. Whole-Drive browsing cannot be marked passed from drive.file device-code login alone.

Spotify shortcut opens intended installed app and returns correctly; absence does not block Home. No Spotify Connect/account-wide media-session claims without separate tests.

## A10 - Broader entertainment scope retained

YouTube saved links use intended handler; no account-wide history claim. Optional API/embed follows official requirements. NowShowing search/details/series choice and each source's identifier/movie/episode/remote behavior tested separately. Iframe load remains unknown without evidence; invalid event origins ignored; source switch/retry retains episode.

Browser/Brave route needs actual handler/remote/Back tests. Filter engine has separate ruleset/version/off-on/site-exception results; disabling filtering preserves data. No universal blocking claim or preenabled unverified providers.

## Evidence and release

For each scenario record commit/APK hash, actual device, sender/receiver/source, steps, expected/actual, Pass/Fail/Blocked, evidence and issue. Do not publish account IDs, LAN identifiers or personal media without consent.

First usable launcher requires all P0 outcomes and explicit owner experience review on target TV/iPhone. Later conditional/requested scope needs delivered behavior or owner-accepted limitations. No unresolved data loss, unauthorized access, launcher lockout or remote navigation trap is acceptable.

Documentation links/coverage were validated. The 0.3 prototype passed 24 Android emulator storage/player/UI assertions; see [build status](BUILD-STATUS.md) for exact coverage. 0.4 adds retained-file/HTTP/production-player tests; current results are recorded in [build status](BUILD-STATUS.md). Brief physical-TV observations are recorded in [feedback](TV-FEEDBACK.md): partial visual/app/remote confirmation, observed default-Home failure. Real iPhone file transfer and comprehensive scenario acceptance remain unrun.

## Baseline 2.1 scenario extensions

### A04 extension: system selection, not just eligibility

Test role available/not available, API below 29, selected/not selected, denied/canceled prompt, missing settings activity, return/recreation and unresolved status. Enabling alias or showing a dialog never counts as success. On target TV select Nook, press Home from YouTube and another app, wake/restart, then restore stock Home. Record actual handler/status and setup steps. Current owner report fails the outcome; root cause is not established.

### A12 - Dynamic content

Configure an owner-selected public YouTube channel/playlist and a public RSS/Atom source from phone/PC. New items appear after permitted refresh with title/source/time and thumbnails when available. Select a YouTube card and verify the exact video opens; external playback creates no invented resume. RSS detail is plain text with an explicit article route. Private/deleted/unavailable videos, empty feed, no credentials, quota, malformed XML, oversize content, disallowed destinations and redirects produce useful states without blocking app access.

Exercise offline/stale/cache expiry, conditional fetch where supported, backoff/manual refresh bounds, source hide/remove/reorder and restart. Refresh while a card is focused: the selected identity and scroll do not jump. Malicious feed markup/XML cannot execute scripts, resolve external entities or fetch local admin services. A08 additionally uses 6 sources with 20 cached items each and bounded images; record actual fixture, timings and memory. No autoplay or perpetual animation.

### A13 - Companion refinement

On desktop and real iPhone Safari, pair, change Home order/visibility, configure/test each source, preview, explicitly save and see TV acknowledgement. Use touch, keyboard and button alternatives to drag. Inline examples/help work without hover. At narrow width/zoom no essential control is clipped. Test disconnect mid-edit, two-editor conflict, rejected source, revoked session, and schema v1/v2 upgrade/v3 export/import with original state preserved on failure. Preview cannot claim changes are saved before acknowledgement.

### A14 - Personal video shelf

First run A05/A06 on the actual phone/TV. Then test address QR and approval, title/thumbnail fallback/rotation, saved-copy receipt, storage-before-send, rename/collection/pin, privacy visibility, start over and confirmed deletion. Disconnect phone and restart Nook: saved file and measured resume remain valid. Collection/history deletion leaves bytes; explicit file deletion updates all references. Dashboard export contains no videos or secrets. Proposed browser controls cannot be marked passed before their endpoints exist.

### A15 - Optional receiver

Identify the reported YouTube-sharing receiver/protocol. Only a documented durable-reference/progress integration can earn replay/resume claims. Otherwise show Open receiver/Start sharing again. Failure to import an opaque external session is a documented optional limitation, not failure of retained-copy replay. Never label upload as AirPlay.

### A16 - Shortcuts

Every shortcut has a visible action; test hold-OK and its alternative on the owner's remote. Quick actions are reachable by D-pad, optional Menu key behaves consistently, Back restores focus, and system Home/volume behavior is not intercepted. Gesture-only or rapid-double-press actions cannot be essential.
