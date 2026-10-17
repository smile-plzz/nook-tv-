# Implementation status — 0.1.0-m1

Baseline 2.1 navigation note: this archive is unchanged evidence for M1. The later [TV feedback](TV-FEEDBACK.md) and [F1-F6 plan](DEVELOPMENT-PLAN.md) govern current work, including the unresolved default Home and requested dynamic content. Do not use this archive's next-execution or old capability statements as current scope.

> Historical M1 evidence, preserved after the 17 October 2026 launcher pivot. The current product is a default Home with Apple TV-style aesthetics and iPhone-media convenience; see [PRD](PRD.md). Any standalone-first or optional-launcher wording below describes the old milestone, not current scope. No historical result proves the new launcher.

2026-10-17. The owner explicitly authorized implementation after documentation finalization. This is the compatibility milestone, not a P0 or full-product release.

## Implemented

| M1 area | Actual implementation | Remaining evidence/work |
|---|---|---|
| Installable app | Normal Android + Leanback entry; stable compatibility ID `com.ismail.nooktv.compat`; API 26 minimum/35 target; vector icon/banner | Installation on owner's TV |
| Remote UI | Native Java Views with visible focus, Home/Apps/Settings, saved links and player return focus | Actual remote, overscan and low-memory behavior |
| Apps | Discover launchable TV/mobile apps, pin/unpin, launch, missing-app label | Firmware package visibility/handlers |
| Diagnostics | API/ABI, display, app memory class, WebView package, selected decoder types, YouTube/Brave installation, home handlers | Owner screenshot/transcription; no unique identifiers |
| Native video | Platform VideoView MP4/HLS probes; visible play/pause/seek/exit and media keys; measured last-video resume | Actual codec/network/playback results; Media3, DASH, per-episode storage and tracks deferred |
| Web | Controlled test page; one WebView; no bridge/file/content access, HTTPS-only navigation, popup/download rejection, cross-host link confirmation, visible exit | Real-TV remote escape/provider behavior; filtering not implemented |
| YouTube | Try official TV/mobile handlers, then browser; no fabricated playback history | Installed handler and actual URL behavior; richer validation/discovery remains |
| PC editor | Foreground-only port 8765; short-lived code plus TV approval; high-entropy bearer/CSRF credentials, Host/Origin checks, bounded threads/headers/body, revocation, revision conflict | Actual home-network connectivity; transport is HTTP, not encrypted |
| Saved state | Atomic JSON dashboard, welcome line, up to 40 HTTPS links and 40 pinned app components; editor reorder/remove; dashboard-only export | Full layout/collections/backgrounds/import and Room migrations remain |
| CI | Hosted Windows build, core/handler checks and APK artifact upload passed on commit `0b822b60c08812613b9916cf78eb7eb6a508b53a` | No device/runtime result is implied |

## Executed locally

- Java source compiled against Android API 35 and converted to DEX.
- Resource/manifest packaging and zip alignment succeeded.
- APK signature verification passed for v2 and v3 signing schemes.
- APK manifest inspection confirms compatibility package ID, version, minimum/target API and normal/TV entry points.
- 22 core assertions passed: code expiry, explicit approval, single-use claims, credentials, immediate revocation, lockout, URL schemes/credential/host rejection and exact editor hosts.
- 17 actual editor-handler assertions passed using in-memory request/response streams and the SDK's AOSP JSON implementation: authenticated reads/writes, no credential before TV approval, origin/host checks, JSON/body limits, conflicts and revocation. These do not test a live LAN socket or Android lifecycle.
- Browser editor JavaScript syntax checked with Node.

GitHub Actions independently repeated the SDK-only build, core checks and editor-handler tests and uploaded the APK in [run 39010354732](https://github.com/smile-plzz/nook-tv-/actions/runs/39010354732). The initial setup failed because the setup action requested a removed `tools` package; the workflow now explicitly requests `platform-tools` and the rerun passed the build/test/upload steps.

No app runtime, emulator, target-TV, media-source, LAN pairing, two-hour playback, provider, performance or accessibility result is claimed. ADB could not start its local service in this execution environment. Shell downloads failed DNS resolution; GitHub connector access worked.

## Explicit M1 limitations

- Platform player is a compatibility probe. It does not implement Media3, DASH, track selection, live/DVR state or production per-episode history. Only the most recent native VOD is resumed; previously watched entries are not a complete library.
- No NowShowing catalog/provider source is enabled or verified. Selected upstream commit: `5fc8b1870a028b2ee84bf324a11f6529e85f22a1`. No upstream source/assets copied because a root reuse license was not found during inspection.
- No default launcher change, Brave-specific launch option, ad filtering, widgets, art cache, metadata credentials, backup import, arbitrary media proxy or cloud service.
- YouTube handoff and generic HTTPS links are supported; comprehensive video/channel/playlist URL canonicalization is later work.
- Config writes are atomic. Mutation receipts and browser credentials last only for the app process; reconnect/restart requires reading current revision. Lost-save retry is idempotent within that process (128 recent mutation IDs). Export covers dashboard configuration only.
- Browser drafts stay in browser local storage. Session secrets stay in tab memory; they are never exported. HTTP cannot protect against someone intercepting home-network traffic. No provider credentials are accepted.
- The editor server stops on Activity pause; it resumes when Nook is foregrounded. Browser drafts are retained without automatically replaying writes. Pairing accepts at most eight in-memory sessions before revocation is needed.
- This is debug-signed. The local ignored key supports updates from this workspace; independently built CI APKs use a new debug key each run and may require uninstalling a previous preview. Production signing continuity is a later gate.

## Next execution

Run [device tests](DEVICE-TEST.md), record actual evidence, and address compatibility failures. Independent work can proceed on data models, Media3 dependency setup and catalog adapters without treating pending device tests as passes. Keep all P0/P1 requests in the PRD; this preview does not reduce them.
