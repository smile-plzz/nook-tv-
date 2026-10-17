# Nook TV

Your default TV home, inspired by Apple TV: favorite apps, YouTube and media shared from your iPhone, with an easier way back to what you were watching.

## Direction and current state

**Launcher pivot confirmed, 17 October 2026.** Ismail wants Nook to open when he presses Home, with Apple TV-style aesthetics and navigation. Priorities are YouTube, iPhone video sharing and return to shared media. Spotify is optional, browser use is rare, and Google Drive access is requested if feasible.

**0.4.0-phone-preview adds retained video transfer** through the paired browser, with saved-copy playback/resume and storage controls. See [phone transfer guide](docs/PHONE-TRANSFER-PREVIEW.md). The 0.3 launcher prototype introduced: installed-app artwork grid, direct app launch, phone/recent destinations and explicit Home setup. See [prototype guide](docs/LAUNCHER-PREVIEW.md). The earlier 0.2 supplied infrastructure; the owner briefly tested it in BlueStacks and confirmed boot but rejected the experience. A brief owner TV trial now reports smooth Home, working tried app/remote paths, incomplete visual refinement and failed default-Home behavior. Upload/resume were not tested; exact tested version is unconfirmed. See [TV feedback](docs/TV-FEEDBACK.md).

This build adds a visual/interaction prototype and an opt-in Home candidate. Paired-browser video upload is now implemented. AirPlay and Drive authorization/playback remain unimplemented.

## Intended daily experience

- Recognizable app artwork in a spacious Apple TV-inspired grid, with configurable fresh YouTube/RSS cards.
- One OK opens YouTube or another favorite; editing is a secondary action.
- From iPhone provides a clear, tested receiving route.
- Shared & Recent returns to retained videos or tells you when the phone must reconnect.
- Continue Watching shows genuine progress where available.
- PC/phone browser customization supports the launcher; pairing and diagnostics live in Settings.
- Default Home behavior includes safe setup and restoring the original launcher.

Nook remains an Android app with its own branding, not tvOS. AirPlay, Cast, live mirroring and saved-file transfer are different capabilities. See [phone and Drive feasibility](docs/PHONE-AND-DRIVE.md) before making support claims.

## Foundation retained

- Installed-app discovery, pinning and launch; text-button dashboard with saved links/collections/background customization.
- Paired local browser editor, revision conflicts, persistent mutation receipts and dashboard import/recovery.
- Media3 direct-stream player and independent native progress/history.
- YouTube external-app/browser handoff.

The delivered 0.4 preview passed 82 local assertions plus browser-state checks and 43 Android assertions. Current source removes obsolete contracts and adds upgrade/cleanup checks; see build status for this revision's validation. Those checks do not prove real iPhone/Safari or target-TV behavior. [Build status](docs/BUILD-STATUS.md) preserves exact evidence.

## Next delivery order

1. Fix and verify system default-Home selection on the owner's TV.
2. Refine the visual language and a restrained content shelf.
3. Improve the phone/PC companion and configurable YouTube/RSS sources.
4. Validate the existing iPhone transfer early, then refine the personal video shelf.
5. Measure daily-use performance, upgrades and recovery.
6. Evaluate optional AirPlay replay, conditional Drive and remaining integrations.

See [F1-F6 development plan](docs/DEVELOPMENT-PLAN.md). This revision also removes embedded movie discovery and its service configuration. The last delivered APK remains 0.4; these source changes need a new build before they appear on TV.

The owner's TV is reported as Sony Plus Android 14 / 2 GB; actual API/firmware/receiver services remain unverified. Ordinary app entry stays for recovery. A standalone fallback is not a completed default-home outcome.

Nook focuses on Home, installed apps, configurable YouTube/RSS cards and personal phone videos. Movie discovery and streaming-platform logic belong in independent installed apps. Existing browser/filtering and richer YouTube requests remain tracked; weather and arbitrary widgets remain later work.

## Documentation

| Document | Purpose |
|---|---|
| [TV feedback](docs/TV-FEEDBACK.md) | Owner observations, intent, uncertainties and focused questions |
| [Dynamic Home](docs/DYNAMIC-HOME.md) | YouTube/RSS modules, visual rules, freshness and technical limits |
| [Companion design](docs/COMPANION-SPEC.md) | Mobile/PC editing and personal video shelf improvements |
| [PRD](docs/PRD.md) | Current launcher scope, priorities, requirement IDs and release gates |
| [UX specification](docs/UX-SPEC.md) | App grid, remote actions, From iPhone and Shared & Recent |
| [Decisions](docs/DECISIONS.md) | Owner confirmations, proposals and superseded assumptions |
| [Requirements analysis](docs/REQUIREMENTS-ANALYSIS.md) | Why the preview missed the goal and research lessons |
| [Phone transfer preview](docs/PHONE-TRANSFER-PREVIEW.md) | Send/cancel/retry, retained copies, limits and storage controls |
| [Phone and Drive feasibility](docs/PHONE-AND-DRIVE.md) | Protocol, replayability, OAuth and storage constraints |
| [Architecture](docs/ARCHITECTURE.md) | Existing foundation and proposed launcher/integration boundaries |
| [Development plan](docs/DEVELOPMENT-PLAN.md) | Current F1-F6 sequence and gates |
| [Acceptance](docs/ACCEPTANCE.md) | Observable launcher, phone and integration scenarios |
| [Implementation handoff](docs/IMPLEMENTATION-HANDOFF.md) | Next-build starting instructions |
| [Build status](docs/BUILD-STATUS.md) | Delivered code versus future design and executed evidence |
| [Build/install](docs/BUILD-AND-INSTALL.md) | Existing APK, toolchain, upgrade and recovery instructions |
| [Device checklist](docs/DEVICE-TEST.md) | Actual TV/iPhone observations |
| [M1 archive](docs/M1-STATUS.md) | Preserved historical compatibility evidence |
| [Third-party notices](THIRD-PARTY-NOTICES.md) | Actual dependencies and reuse boundaries |

Core Home needs no mandatory cloud account or always-on PC. Optional services need their own access/network. Personal media, account credentials and private history are not silently imported or included in dashboard backups.
