# Launcher pivot implementation handoff

Baseline 2.0 | 17 October 2026

## Start here

Build an Apple TV-style default TV home screen for Ismail. Main activities are YouTube, iPhone-shared video and getting back to that media. Spotify is tentative, browser rare, Google Drive requested if feasible. Keep PC customization/personal collections. NowShowing remains the selected streaming reference, but not the main product or next priority.

0.2 booted in a brief BlueStacks trial; owner says structure exists but experience misses the goal. Do not report that as launcher acceptance or thorough testing. Main includes merged PR #2 and the existing infrastructure. Read [build status](BUILD-STATUS.md) before claiming capabilities.

This change is documentation only. Earlier build authorization remains part of history; next implementation follows the new plan. No new APK, Home default, receiver or Drive account access is created by a documentation merge.

## Read order

1. [README](../README.md) and [decisions](DECISIONS.md).
2. [PRD](PRD.md) for requirement IDs and priority.
3. [UX](UX-SPEC.md) for Home/grid/direct-launch/phone return interactions.
4. [Phone and Drive feasibility](PHONE-AND-DRIVE.md).
5. [Architecture](ARCHITECTURE.md), [plan](DEVELOPMENT-PLAN.md), [acceptance](ACCEPTANCE.md).
6. [Requirements analysis](REQUIREMENTS-ANALYSIS.md) and historical [NowShowing audit](NOWSHOWING-AUDIT.md).

## First implementation slice

Create a reviewable launcher prototype before expanding integration scope:
- Artwork-led app grid, quiet header, Settings, All Apps and From iPhone.
- One OK launches an app; visible context alternative for editing.
- Shared & Recent and Continue Watching with truthful empty/availability states.
- Remove samples/diagnostics/pairing/configuration clutter from normal Home.
- Include remote focus, long titles, missing artwork, offline and reduced-motion states.

Review the visual/task result against the owner's Apple TV reference before broad integration work. Then implement Home selection/recovery, phone workflow and durable return, and performance/migrations. Drive/authentication and remaining catalog/protection follow their documented gates.

## Reuse and constraints

Keep Java/native Views, AtomicFile stores, revision/pairing contracts and Media3 unless a measured reason warrants change. Earlier Room/Compose preference is not a required rewrite. Keep com.ismail.nooktv.compat and preview signing continuity; do not commit keys. Current Gradle 8.13, AGP 8.13.2, compile SDK 36, target 35, min 26; use hosted CI if local dependencies cannot resolve.

Existing 74 contract assertions, editor tests and 15 Android smoke assertions cover foundation behavior, not the new launcher. Real TV, iPhone, receiver, network and Home-default tests remain necessary.

HOME support uses system-supported setup; retain original launcher and recovery. Never silently disable stock packages. A blocked default-Home gate is not solved by relabeling Nook an entertainment app.

A launcher does not automatically receive AirPlay/Cast. Select/test a real route; distinguish live session, stable link and retained file. New upload requires streaming disk storage/limits/cleanup; existing editor accepts configuration, not videos. Google device flow is not whole-Drive authorization; prove selected-file grants and keep tokens outside HTTP companion.

## Evidence and completion

Record actual TV API/ABI/memory/display/WebView, Home/wake/reboot/rollback, installed handlers and codecs. Record actual iPhone source/gesture/format/receiver and replay after disconnect. Redact identifiers and personal media.

Use [device checklist](DEVICE-TEST.md); track results in build status. Full completion follows PRD outcomes and owner acceptance, never just CI or an installable preview. Do not silently drop NowShowing, Brave/filtering or richer YouTube, but do not let them displace the launcher and phone workflow.
