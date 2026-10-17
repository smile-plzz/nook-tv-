# Implementation status | 0.2.0-preview

Updated 17 October 2026. Ismail asked to proceed with the next phases while TV testing is unavailable. Development continues independently; the target-TV compatibility and acceptance gates remain pending. Historical M1 evidence is in [M1 status](M1-STATUS.md).

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

When the TV is available, execute [device tests](DEVICE-TEST.md), then validate individual source contracts and network/remote behavior. Remaining full scope includes tested providers, richer metadata/artwork/episode browsing, fully polished native YouTube routes, Brave-specific routing/filtering, optional widgets, optional launcher and production release signing. Do not label P0 or the full product accepted merely because CI passes.
