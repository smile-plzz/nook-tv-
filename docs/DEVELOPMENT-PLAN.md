# Launcher development plan

Baseline 2.0 | 17 October 2026 | Supersedes old M1-M6 delivery order

## Starting position

M1 compatibility and 0.2 dashboard/editor/player/catalog infrastructure exist. The owner briefly used 0.2 in BlueStacks, confirmed boot but rejected the overall experience as the intended launcher. Do not restart storage/pairing work or extend catalogs before correcting the home experience.

The current task is documentation only. Future build work follows these stages; no deadline is assumed. Physical TV tests remain pending, but design and independent checks can proceed.

## L0 - Reviewable launcher prototype

Deliver Apple TV-inspired Home with app grid/artwork, YouTube priority, From iPhone entry, optional Spotify/Drive/browser, Shared & Recent and Settings. Include empty/offline/missing-app/focus/menu states. Remove samples/pairing/configuration tools from normal Home.

Gate: owner can assess the actual first screen and remote task flow before integration expansion. Do not treat a technically functioning button grid as design acceptance. Exact layout is proposed until reviewed.

## L1 - Default Home and direct app access

Implement system-supported HOME entry/setup/recovery, one-OK app launch, favorites management, All Apps, focus restoration and root Back behavior. Keep normal app entry and original-launcher recovery. Migrate 0.2 favorites/layout without losing data.

Gate: Home from another app opens Nook; reboot/wake/task reuse behave predictably; original default restored successfully on target TV. BlueStacks validates visuals/navigation only. Firmware blockage is recorded as blocked core functionality, not a completed launcher.

## L2 - iPhone workflow and Shared & Recent

Assess actual TV AirPlay/Cast/receiver support and owner sending gesture. Prototype a paired Safari retained-file path and stable-link submission without claiming AirPlay. Add a truthful From iPhone route selector, transfer progress/cancel/storage errors and replayability-aware media records.

Native retained files play/resume after phone disconnect and TV restart. External/live sessions show reconnect or open-app actions, with no invented progress. Define thumbnail/privacy/data deletion and backup boundaries. New file upload is separate from existing JSON editor.

Gate: one real iPhone video workflow tested end-to-end, with documented receiver dependency/retention and replay limitations. Test interruption, low space, unsupported codec, app restart and history controls. A desktop upload test is insufficient.

## L3 - Cohesive launcher polish

Bring PC companion preview/editing into the new grid model; phone Safari layout works. Verify contrast, long names, safe margins, reduced motion, missing artwork, remote context alternatives and responsive startup. Keep diagnostics in Settings. Test schema migrations, backup/restore and key continuity.

Gate: all launcher P0 scenarios pass on actual TV/iPhone; owner confirms the daily experience fits. Performance and two-hour testing use representative media/artwork. Only then label first usable launcher ready.

## L4 - Conditional Drive and optional services

Prove Drive OAuth client/scopes and existing-file selection before folder-browser implementation. Build selected-media access/playback, stable references, range seeking, refresh/reconnect and disconnect privacy. Do not promise full Drive through TV device-code scopes or pass tokens through HTTP companion.

Spotify is optional app access; richer account/music features require a separate request. Evaluate Android TV channels/Watch Next only against accessible platform data and participating apps, without universal history claims.

Gate: chosen Drive workflow demonstrated with a real authorized file and truthful limits; scope decision recorded if it cannot meet the intended experience. Drive is requested conditional scope, not silently dropped.

## L5 - Remaining entertainment scope

Retain NowShowing-Stream catalog/episode/source work, richer YouTube browsing, Brave routing and separately evaluated filtering. Each provider/app route must pass remote/playback checks before support claims. No iframe-success or inherited-Shields assumptions. Optional mood/weather/widgets follow core quality; PC media/profile/cloud-sync work stays separate future scope.

Gate: record each requested integration's supported behavior or owner-accepted limitation. Full product is not closed by L3 alone.

## Verification by layer

| Layer | Evidence |
|---|---|
| Documentation | Valid links, requirement/scenario coverage, no stale optional-launcher claims outside history |
| Models/storage | Migration, atomic recovery, stable file/source identity, history-vs-file deletion, schema limits |
| App/remote UI | Grid focus, one-click launch, context alternative, Home/Back task behavior, return state |
| Transfer | Auth, quotas, streaming memory, cancellation, cleanup, permission/MIME/path validation |
| Phone integration | Actual iPhone sending, receiver capability, disconnect/replay and codec behavior |
| Drive | Granted-file access, refresh, range seek, revocation, changed/deleted media and redacted logs |
| Device/release | Home default/reboot/rollback, 2 GB performance, two-hour playback, same-key upgrade |

Preserve existing 74 contract assertions, editor state checks and 15 Android smoke assertions as regression coverage; they do not validate the new launcher UX or receiving integrations. Add meaningful tests for changed contracts when implementing, not artificial tests during this docs-only task.

## Dependencies and completion

Read [handoff](IMPLEMENTATION-HANDOFF.md), [PRD](PRD.md), [UX](UX-SPEC.md), [architecture](ARCHITECTURE.md) and [phone/Drive feasibility](PHONE-AND-DRIVE.md). Track actual results in [build status](BUILD-STATUS.md), never infer milestone acceptance from green CI. Record version/commit/device/route/expected/actual/evidence for each gate.
