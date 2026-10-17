# Launcher acceptance and verification

Baseline 2.0 | 17 October 2026 | Target-TV/iPhone execution pending

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

Documentation links/coverage were validated. The 0.3 prototype passed 24 Android emulator storage/player/UI assertions; see [build status](BUILD-STATUS.md) for exact coverage. Physical TV/iPhone and full scenario acceptance remain unrun.

