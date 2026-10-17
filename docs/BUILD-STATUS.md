# Build status | 0.4 phone transfer preview

## Latest owner TV evidence and documentation revision

PR #5 merged into main at `247671613c9552fa9f67eda179a67528a680ddcf`. Baseline 2.1 updates the plan only; latest delivered APK remains 0.4.0-phone-preview. The exact APK used during the brief TV trial was not confirmed.

Owner reports Home is smooth and closer to the aesthetic, apps launch correctly, and tried remote navigation works. Visual acceptance is incomplete. Home from YouTube and TV restart return to the existing environment: **default-Home outcome failed in the reported test**, cause/setup path unconfirmed. File upload/retained replay were not tested. Reported external YouTube AirPlay use is not evidence of Nook integration. See [feedback](TV-FEEDBACK.md).

Planned, not implemented: role request prompt, refined design system, YouTube/RSS modules, companion redesign, QR/thumbnails/library refinements. Follow [F1-F6](DEVELOPMENT-PLAN.md); historical build/CI evidence below is preserved.

Updated 17 October 2026. The owner briefly installed/explored 0.2 in BlueStacks: it boots, but the experience does not match the desired launcher. He confirmed default Home, Apple TV-style aesthetics, YouTube, iPhone video sharing and easier return to shared media; Spotify is tentative, browser rare and Google Drive conditional.

[PRD baseline 2.1](PRD.md) supersedes the dashboard-first roadmap. 0.3 implements the launcher prototype/Home plumbing. 0.4 adds the first retained-file transfer/playback path in L2. Actual iPhone/TV acceptance and AirPlay/Drive remain pending. See [phone transfer guide](PHONE-TRANSFER-PREVIEW.md). See [prototype guide](LAUNCHER-PREVIEW.md).

## Pivot coverage

| New outcome | Actual status |
|---|---|
| Artwork app grid / direct app launch | Implemented; brief TV app use confirmed by owner, visual refinement still required |
| Default Home setup, task behavior and recovery | Disabled-by-default HOME alias, explicit setup, task reuse/root Back and removal guard implemented; owner reports Home/restart return elsewhere; setup cause, wake and rollback still unverified |
| From iPhone / retained file transfer | Paired-browser MP4/MOV file upload, app-private retained copies, progress/cancel/retry and storage controls implemented; AirPlay/Cast/mirroring absent; real Safari/TV unverified |
| Shared & Recent | Retained-copy row and measured local playback/resume added; live phone sessions still not imported |
| Google Drive authorization/media | Not implemented; selected-file/scopes feasibility gate |
| Spotify | Ordinary installed-app discovery may expose it; dedicated integration not implemented |
| PC customization, collections, Media3 | Existing foundation retained, not proof of new UX acceptance |

Only the limited physical-TV observations above are claimed; no successful Nook default-Home or real iPhone retained-transfer result is claimed. Historical M1 evidence remains in [M1 status](M1-STATUS.md).

## 0.4 verification and bounds

- Local: 22 core + 25 HTTP-handler + 35 model/source/viewing assertions pass (82 total), plus editor state and new binary-transfer browser-state checks.
- ReceivedMediaStore compiles against the local Android SDK.
- [Hosted app/test APK build](https://github.com/smile-plzz/nook-tv-/actions/runs/39037726748) passed for app code `9340bbfd98026e7d1ed8c017a6136a58b6fa6584`.
- [Android API-35 runtime](https://github.com/smile-plzz/nook-tv-/actions/runs/39037726747) passed all 43 assertions: existing checks plus retained storage/recreation/receipts, invalid/interrupted/revoked transfers, deletion/corruption preservation, real loopback HTTP upload without autoplay, and production local-player/seek persistence.
- Integration testing exposed a close-after-seek persistence failure. The player now preserves its ready timeline during buffering, saves explicit seeks and guards release callbacks from overwriting final progress. The same seek/return assertion passed after the fix.
- A subsequent test-only change waits for the writer before reopening AtomicFile state; runtime app source is unchanged. Its [app/test build](https://github.com/smile-plzz/nook-tv-/actions/runs/39037939006) and [43-assertion Android rerun](https://github.com/smile-plzz/nook-tv-/actions/runs/39037939021) also passed at `b37dd34e576d62411eeedff1c1820535b73146df`.
- Delivered owner APK has verified v2/v3 signatures, alignment and package/version, using the same preview certificate as M1/0.2/0.3. SHA-256: `b90ff039dbdfd812a7cd4c6f8fd767474c09d91b1e5c80905b98307ea58a3107`.
- Limits: 512 MiB/file, 1 GiB retained quota, 50 listed files, 128 MiB free-space reserve; one media operation, 64 KiB streaming buffer. Quota does not auto-evict user files.
- Authentication before upload ingestion; permission checked while receiving and before finalization. Original HTTP origin/host/CSRF boundary retained.
- File finalization/index commit precede acknowledgement; incomplete files are not listed. Media index corruption blocks writes and preserves data.
- Local playback uses stable IDs/private files, never an arbitrary external path. History and file deletion are separate.
- Dashboard backups exclude retained file bytes/index, viewing history and credentials. Uninstall/clear storage deletes retained copies; same-key upgrade retains them.
- Actual iPhone/Safari, real Wi-Fi, HEVC/HDR/rotation, large transfers and target-TV performance remain untested.

## Historical 0.3 verification

- 74 core/model/source/viewing/HTTP-handler assertions passed locally; editor state/syntax checks passed.
- New tile/discovery classes compile against the local Android SDK.
- [Hosted Gradle packaging](https://github.com/smile-plzz/nook-tv-/actions/runs/39034531772) passed at code commit `181c77855d7c0ad330b8f9814ed7c41bd2fab532`.
- [Android API-35 tablet smoke](https://github.com/smile-plzz/nook-tv-/actions/runs/39034531785) passed 24 assertions: prior storage/player cases plus grid reachability, absence of Home test/pairing clutter, focusable cards, one-click intended app launch dispatch (intercepted monitor), phone/shared destinations, Home setup and enabled HOME alias eligibility. System default selection/wake/reboot are not covered by alias eligibility.
- Emulator screenshot inspected and retained in [prototype guide](LAUNCHER-PREVIEW.md); app inventory differs from the owner's TV.
- Owner APK signed with the same certificate as M1/0.2; v2/v3 signature, alignment and package/version verified. SHA-256: `19be610447536f328a9078cb6c1f6f038833d6b7395090a6e3d5389d8bc22353`.
- Schema v2, app ID and existing user data retained; no new runtime dependency.
- From iPhone currently offers paired-browser link guidance and All Apps access for an existing receiver; no built-in AirPlay/Cast/file upload.
- Optional Drive preview tile is hidden by default and explicitly not connected.
- Samples, diagnostics, catalog and pairing are in Settings.

## Existing 0.2 implementation and evidence

The following describes the delivered pre-pivot APK. It must not be read as the new launcher design being complete.

## Implemented in this change

| Area | Implemented | Limits / remaining evidence |
|---|---|---|
| Dashboard | Schema-v2 migration from M1; ordered/visible Home rows; pinned apps; 200 saved items; 20 collections; references shared across collections | TV overscan, focus and performance pending |
| Browser editing | Actual TV app inventory, app ordering/pinning, link ordering, collection membership/order, title/accent and resized photo backgrounds; approximate preview | Actual LAN testing pending |
| Backup | Schema-v1/v2 import preview and explicit replacement; atomic pre-import recovery snapshot; Undo import on TV; dashboard-only export | Does not export secrets, viewing history, provider source templates or series selections |
| Concurrency | Revision leases and last 128 mutation fingerprints persist with the config in one AtomicFile envelope | Replay returns current state after verifying the original mutation fingerprint; not an unlimited mutation journal |
| Media3 | Pinned ExoPlayer 1.11.1, HLS/DASH modules, PlayerView, remote/media controls, audio/subtitle selection when tracks exist, retry and sample streams | Real content, codecs, seeking and two-hour tests pending |
| Viewing state | Up to 100 independent entries; native measured progress; resume/start over/mark watched/remove; recently opened web/app routes; pause and separate clear | No account history, cross-origin progress scraping or external-app timestamps |
| Metadata | Native OMDb search/pagination/details; saved movie/series entries; independent season/episode selection; 6-hour metadata cache bounded at 150 records / 8 MiB; quota/key/offline states | Owner supplies optional key on TV; OMDb metadata does not establish stream availability |
| Web sources | Up to ten owner-configured HTTPS IMDb URL templates; movie/series placeholder validation; selection saved separately; opaque web playback never claims timestamps | No provider preenabled or verified; provider contracts and remote input require later tests |
| Android verification | Real Android AtomicFile/mutation/recovery/progress, native navigation and Media3 playback instrumentation passed in hosted API-35 emulator | Emulator is not the owner's TV |

## Verification

- 22 core assertions passed locally.
- 17 actual HTTP-handler assertions passed locally with in-memory transport.
- 35 new schema/migration/collection/source/viewing assertions passed locally.
- Editor JavaScript syntax passed.
- Editor state tests passed (save/conflict/retry/import/draft/privacy).
- [Hosted Gradle build and APK packaging](https://github.com/smile-plzz/nook-tv-/actions/runs/39026599814) passed at code commit `1910d09803ede76cf22cef6f977ec77e76a2b428`.
- [Android runtime smoke](https://github.com/smile-plzz/nook-tv-/actions/runs/39026602016) passed all 15 assertions on an API-35 tablet emulator, including storage recreation, mutation receipts, import recovery, history, navigation and Media3 prepare/progress/seek/pause against a generated local H.264/AAC fixture.
- Playback instrumentation now waits for measured progress with a bounded timeout instead of assuming decoder startup completes after 900 ms. The earlier failure was this timing assertion.
- The downloadable owner APK has verified v2/v3 signatures using the same certificate as the M1 APK; alignment and package/version were checked.
- No owner-TV, physical remote, home LAN, provider playback, real-network media, 2 GB memory or two-hour result is claimed.

The tests fixed a concrete M1 defect: link validation counted five fields even though saved links contain four. Schema-v2 tests now exercise valid saved links and reject malformed/dangling references.

## Technical decisions

Java/native Views remain in use. Production playback now uses Media3 instead of the platform VideoView probe. Durable state currently uses versioned atomic JSON envelopes rather than Room; this is a recorded implementation deviation, not a claim that Room work was completed. The bounded state is covered by document and Android storage tests; SQLite/Room migration can follow measured requirements rather than running two authoritative stores concurrently.

Gradle 8.13, AGP 8.13.2 and compile SDK 36 are pinned for Media3 dependencies; minimum API remains 26 and target API remains 35. The previous SDK-only M1 build cannot compile these runtime dependencies and now directs contributors to Gradle.

Dashboard data and viewing state are separate atomic files. M1 config and last-native progress migrate on first use. A corrupt saved dashboard disables replacement rather than silently overwriting it; corrupt viewing data also remains untouched. Pairing sessions remain in memory and require pairing again after process death. Background images are JPEG-only, bounded in bytes/dimensions and decoded away from the UI thread.

The OMDb key is entered on TV and kept out of browser editing, backups and logs. OMDb requires the key in its HTTPS API query; the app sends it only to that endpoint and does not follow redirects. Source templates are owner-configured on TV and not included in dashboard backups.

## Next work

Follow the current [F1-F6 plan](DEVELOPMENT-PLAN.md): verified Home selection, cohesive visual refinement, selected YouTube/RSS modules and companion, personal video shelf, then device hardening. Drive, optional receiver integration and remaining catalog/player/Brave work follow their gates. Selected YouTube/RSS content is P0; weather/arbitrary widgets remain optional. Production signing is still separate. Use the owner feedback to focus the next [device tests](DEVICE-TEST.md); passing CI does not establish launcher acceptance.
