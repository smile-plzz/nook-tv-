# Nook TV

A personal Android TV dashboard for Ismail: Continue Watching, favorite apps, YouTube, movies and series through NowShowing-Stream, and customization from a PC browser.

## Project status

**Planning baseline — 17 October 2026.** This repository currently contains requirements and an implementation plan. No APK, device validation, or completed integrations are claimed.

Primary device: Sony Plus SP43SFV product listing; the owner reports **Android 14 and 2 GB RAM**. Actual Android API level, CPU ABI, WebView, codecs, DRM, and launcher behavior must be checked on the device.

## Product direction

- Standalone TV application with optional home-launcher support.
- Native, remote-friendly dashboard and content browsing.
- Continue Watching with accurate timestamps only where playback progress is observable.
- YouTube app handoff initially; embedded playback evaluated separately.
- **[NowShowing-Stream](https://github.com/smile-plzz/NowShowing-Stream) is the sole existing streaming reference.** CineVault/CineGemini-Stream and NowShowing-Stream-V2 are not implementation baselines.
- PC browser editor on the home network; settings and history live on the TV.
- Optional Brave handoff and separately implemented web filtering, subject to device/source tests.
- No mandatory cloud account or always-on PC.

## Planning documents

| Document | Purpose |
|---|---|
| [Product requirements](docs/PRD.md) | Scope, user stories, priorities, acceptance criteria, success targets |
| [Requirements analysis](docs/REQUIREMENTS-ANALYSIS.md) | Confirmed decisions, verified source findings, integration limits, unknowns |
| [Architecture](docs/ARCHITECTURE.md) | Proposed components, data model, pairing and playback contracts |
| [Development plan](docs/DEVELOPMENT-PLAN.md) | Implementation order, deliverables, quality gates |
| [Source audit](docs/NOWSHOWING-AUDIT.md) | Evidence from the selected upstream repository |

## First implementation milestone

An installable compatibility APK: D-pad navigation, sample native playback, an isolated web-player test, YouTube handoff, and paired browser access. Run it on the actual TV before expanding the dashboard.

See the development plan for the sequence. Product intent is established; technical choices marked proposed remain subject to the compatibility milestone.
