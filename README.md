# Nook TV

A personal Android TV Home screen with direct app access, selected YouTube/RSS cards, and videos deliberately sent from an iPhone browser.

## Current source and release status

The F5 hardening candidate is **0.4.5-f5-hardening**, version code **5**. It includes F1–F4 and the correctness fixes described in [F5 hardening](docs/F5-HARDENING.md). Dashboard schema v2, app ID `com.ismail.nooktv.compat`, native Java Views, Media3 and independent atomic stores are preserved.

**Merged for owner testing:** [PR #14](https://github.com/smile-plzz/nook-tv-/pull/14) was merged into `main` with explicit owner approval on 17 October 2026, merge `e3b6159`. F1–F4 and F5 repairs are included. Follow the [current device checklist](docs/DEVICE-TEST.md#045-owner-test-session); physical-device acceptance is pending.

Implemented source includes:

- Installed-app artwork/favorites, one-OK launch, opt-in Home-role selection and safe recovery.
- A paired foreground LAN browser companion with layout drafts, explicit saves and revision conflict handling.
- Up to six selected public YouTube channel/playlist feeds or public UTF-8 RSS/Atom feeds; cached cards and exact-video handoff.
- Retained MP4/MOV transfers: 512 MiB/file, 1 GiB quota, 50 videos, 128 MiB disk reserve.
- QR address setup, personal-video thumbnails, rename/pin/collection/hide/delete, and measured local resume.
- F5 fixes for source-save acknowledgements, feed scheduling, focus identity, Home privacy, bounded thumbnail extraction and release identification.

## Evidence and remaining work

Verification passed: 654 Java assertions, both browser-state suites, hosted APK packaging, 64 Android runtime assertions and eight same-key F4-to-F5 upgrade checks. See [build status](docs/BUILD-STATUS.md) for exact commits and CI links. The latest source is a release candidate, not target-device acceptance.

The owner briefly confirmed smooth app launch and tried remote paths, but default Home/restart failed in that earlier trial. The tested APK/firmware was not recorded. Real iPhone Safari retained-file transfer, target-TV Home/reboot/recovery, HEVC/HDR/portrait clips, two-hour stability and low-RAM measurements remain required.

The companion stops when Nook leaves foreground. Upload is saved-file transfer, not AirPlay/Cast/mirroring. External-app opens never imply measured playback progress. Core Home requires no mandatory cloud account or always-on PC.

## Product boundaries

Nook discovers and launches independent TV applications. Movie discovery, streaming catalogs and provider logic belong in those independent apps. Conditional Drive access, external receiver investigation, richer YouTube integration and browser/filtering work remain F6; none is implicitly implemented.

## Documentation

| Document | Purpose |
|---|---|
| [F5 hardening](docs/F5-HARDENING.md) | Fixes, contracts, validation and remaining gates |
| [Build status](docs/BUILD-STATUS.md) | Current evidence and historical results |
| [PRD](docs/PRD.md) | Product scope and requirement IDs |
| [Development plan](docs/DEVELOPMENT-PLAN.md) | F1–F6 sequence |
| [Implementation handoff](docs/IMPLEMENTATION-HANDOFF.md) | Current continuation instructions |
| [Architecture](docs/ARCHITECTURE.md) | Storage, launcher, companion and playback boundaries |
| [UX](docs/UX-SPEC.md) | Remote and companion experience |
| [Dynamic Home](docs/DYNAMIC-HOME.md) | Feed contracts and limits |
| [Companion](docs/COMPANION-SPEC.md) | Editing and personal-video workflows |
| [Phone transfer](docs/PHONE-TRANSFER-PREVIEW.md) | Sending, limits, storage and APIs |
| [Phone/Drive feasibility](docs/PHONE-AND-DRIVE.md) | Conditional integrations |
| [Acceptance](docs/ACCEPTANCE.md) | Observable outcomes |
| [Device tests](docs/DEVICE-TEST.md) | Owner TV/iPhone checklist |
| [Build/install](docs/BUILD-AND-INSTALL.md) | Toolchain, upgrade and recovery |
| [Feedback](docs/TV-FEEDBACK.md) | Preserved owner observations |
| [Decisions](docs/DECISIONS.md) | Confirmed direction and engineering choices |
| [Third-party notices](THIRD-PARTY-NOTICES.md) | Dependencies and reuse |

