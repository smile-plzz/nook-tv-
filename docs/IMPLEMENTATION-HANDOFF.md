# Implementation handoff

Baseline 1.0 — 2026-10-17.

## Current authorization and status

**Documentation is finalized. Do not start building until Ismail explicitly instructs you to start.** The latest request authorizes document review/refinement only.

No application source, scaffold, Gradle setup, dependency installation, CI workflow, APK build, deployment, or device change is authorized by this document alone. A later explicit build instruction releases this hold; do not ask for duplicate permission then.

No APK or device validation exists. Docs describe future behavior and proposed targets, not delivered features.

## Read order

1. [README](../README.md): product and current state.
2. [Decisions](DECISIONS.md): owner decisions versus proposals.
3. [PRD](PRD.md): requirements and release scope.
4. [UX](UX-SPEC.md): TV/browser flows.
5. [Requirements analysis](REQUIREMENTS-ANALYSIS.md) and [source audit](NOWSHOWING-AUDIT.md).
6. [Architecture](ARCHITECTURE.md), [development plan](DEVELOPMENT-PLAN.md) and [acceptance](ACCEPTANCE.md).

## Non-negotiable context

- Personal TV dashboard, not a generic public streaming service.
- Owner reports Android 14 and 2 GB RAM.
- Standalone APK first, with launcher support still tracked.
- PC browser customization; settings remain on TV.
- Streaming reference is exactly https://github.com/smile-plzz/NowShowing-Stream.
- Native browsing, real Continue Watching where supported, explicit limits for opaque embeds and external apps.
- Brave handoff/filtering must be tested, not assumed.
- Basic editable interest collections are P0, not deferred until a recommendation engine.
- Do not import private user context or connected-account content without an explicit task.
- Preserve the upstream repo; work here unless separately asked to modify upstream.

## First work after a future build instruction

1. Read current repo/AGENTS instructions and recheck changes since this baseline.
2. Inspect available build environment; the previous shell connection was unavailable, so local Android tooling is not verified. Use available permitted tooling, and report actual blockers.
3. Pin the selected upstream revision and inspect licensing before copying source/assets.
4. Select current compatible Android toolchain and dependencies using official docs; record choices and license inventory.
5. Implement the M1 compatibility APK only as the first milestone; continue independent work when device feedback is pending, without claiming unrun tests passed.
6. Deliver a reproducible installable test build with installation and diagnostic instructions.
7. Use observed TV results to refine proposals, then execute the remaining plan.

## Device evidence to collect when testing is possible

Actual OS API level, ABI, screen dimensions/density, memory class, WebView version, launchable app handlers, available decoders, native stream results, remote buttons/focus behavior, editor network/pairing results and home-default support. Do not publish unique device identifiers or LAN secrets in a public repo.

## Product choices that are still proposals

Kotlin/Compose candidate, exact module layout, same-LAN HTTP editor boundaries, foreground-only editing, default watched threshold, native YouTube browsing with app playback, filter engine and technical numeric limits. Resolve through the documented milestones; do not misrepresent them as instructions from the owner.

## What completion must include

Built/signed versioned APK, build instructions and dependency versions; install/update/rollback guidance; device-test evidence; source capability matrix; stable data migrations; scoped known limitations. Full-scope closure follows PRD, not merely a passing build or a first P0 release.

## Documentation review outcome

The 1.0 refinement aligns every explicit request with requirements, adds P0 personalization and first-run behavior, separates measured progress from selected episodes/recent opens, defines edit/backup lifecycle, and makes outstanding device/provider checks visible. This completes documentation work only.
