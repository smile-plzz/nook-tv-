# Nook TV

Your default TV home, inspired by Apple TV: favorite apps, YouTube and media shared from your iPhone, with an easier way back to what you were watching.

## Direction and current state

**Launcher pivot confirmed, 17 October 2026.** Ismail wants Nook to open when he presses Home, with Apple TV-style aesthetics and navigation. Priorities are YouTube, iPhone video sharing and return to shared media. Spotify is optional, browser use is rare, and Google Drive access is requested if feasible.

The delivered **0.2.0-preview is infrastructure**, with dashboard/editor/storage/player/catalog features. It is not the redesigned launcher. The owner installed it in BlueStacks and briefly explored it: boot worked, but the experience missed the intended product. No thorough BlueStacks or actual-TV acceptance is claimed.

This documentation update changes the product baseline and next delivery order. It does not add Home selection, an iPhone receiver, video upload, Drive authorization or a new APK.

## Intended daily experience

- Recognizable app artwork in a spacious Apple TV-inspired grid.
- One OK opens YouTube or another favorite; editing is a secondary action.
- From iPhone provides a clear, tested receiving route.
- Shared & Recent returns to retained videos or tells you when the phone must reconnect.
- Continue Watching shows genuine progress where available.
- PC/phone browser customization supports the launcher; pairing and diagnostics live in Settings.
- Default Home behavior includes safe setup and restoring the original launcher.

Nook remains an Android app with its own branding, not tvOS. AirPlay, Cast, live mirroring and saved-file transfer are different capabilities. See [phone and Drive feasibility](docs/PHONE-AND-DRIVE.md) before making support claims.

## What exists in 0.2

- Installed-app discovery, pinning and launch; text-button dashboard with saved links/collections/background customization.
- Paired local browser editor, revision conflicts, persistent mutation receipts and dashboard import/recovery.
- Media3 direct-stream player and independent native progress/history.
- Initial OMDb metadata search, series selection and owner-configured web templates.
- YouTube external-app/browser handoff.

Hosted build, 74 contract assertions, editor checks and 15 Android emulator smoke assertions passed for the recorded code. Those tests do not prove the new launcher, iPhone integration or actual-TV behavior. [Build status](docs/BUILD-STATUS.md) preserves exact evidence.

## Next delivery order

1. Reviewable launcher visual/interaction prototype.
2. Default Home and direct favorite-app access.
3. Tested iPhone workflow plus durable, honest return to media.
4. Companion alignment, migrations, remote polish and actual-device performance.
5. Conditional Drive integration and optional service improvements.
6. Remaining NowShowing/YouTube/protection scope.

The owner's TV is reported as Sony Plus Android 14 / 2 GB; actual API/firmware/receiver services remain unverified. Ordinary app entry stays for recovery. A standalone fallback is not a completed default-home outcome.

[NowShowing-Stream](https://github.com/smile-plzz/NowShowing-Stream) remains the sole selected streaming baseline, as a secondary destination. Existing Brave/filtering and richer YouTube requests remain tracked. Catalog expansion and optional widgets do not displace the core launcher/phone workflow. No provider is enabled or verified by default.

## Documentation

| Document | Purpose |
|---|---|
| [PRD](docs/PRD.md) | Current launcher scope, priorities, requirement IDs and release gates |
| [UX specification](docs/UX-SPEC.md) | App grid, remote actions, From iPhone and Shared & Recent |
| [Decisions](docs/DECISIONS.md) | Owner confirmations, proposals and superseded assumptions |
| [Requirements analysis](docs/REQUIREMENTS-ANALYSIS.md) | Why the preview missed the goal and research lessons |
| [Phone and Drive feasibility](docs/PHONE-AND-DRIVE.md) | Protocol, replayability, OAuth and storage constraints |
| [Architecture](docs/ARCHITECTURE.md) | Existing foundation and proposed launcher/integration boundaries |
| [Development plan](docs/DEVELOPMENT-PLAN.md) | New L0-L5 sequence and gates |
| [Acceptance](docs/ACCEPTANCE.md) | Observable launcher, phone and integration scenarios |
| [Implementation handoff](docs/IMPLEMENTATION-HANDOFF.md) | Next-build starting instructions |
| [Build status](docs/BUILD-STATUS.md) | Delivered code versus future design and executed evidence |
| [Build/install](docs/BUILD-AND-INSTALL.md) | Existing APK, toolchain, upgrade and recovery instructions |
| [Device checklist](docs/DEVICE-TEST.md) | Actual TV/iPhone observations |
| [NowShowing audit](docs/NOWSHOWING-AUDIT.md) | Historical upstream findings, not current launcher scope |
| [M1 archive](docs/M1-STATUS.md) | Preserved historical compatibility evidence |
| [Third-party notices](THIRD-PARTY-NOTICES.md) | Actual dependencies and reuse boundaries |

Core Home needs no mandatory cloud account or always-on PC. Optional services need their own access/network. Personal media, account credentials and private history are not silently imported or included in dashboard backups.
