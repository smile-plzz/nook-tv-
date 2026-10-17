# Nook TV

Your personal space on the big screen: apps, Continue Watching, YouTube, NowShowing movies and series, and collections tailored by you from a PC browser.

## Status

**Implementation authorized — 17 October 2026. Milestone 1 compatibility preview built locally.**
The owner instructed “initiate the build accordingly.” The documentation hold is released. This branch introduces the first test APK; target-TV acceptance remains pending. It is not the first usable release or a claim of full product completion.

Start with [build and installation](docs/BUILD-AND-INSTALL.md), [current implementation status](docs/BUILD-STATUS.md), and the [TV test checklist](docs/DEVICE-TEST.md).

### What this APK can do

- Remote-friendly native Home, Apps, Settings and diagnostics.
- Discover, pin, launch and unpin installed apps.
- Save/reorder HTTPS links from an authenticated PC browser editor with explicit TV pairing approval, revision conflicts and browser revocation.
- Probe MP4/HLS using Android's platform player, with actual measured resume for the most recent finite native video.
- Hand off YouTube to its installed app or an external browser.
- Open an isolated web screen with visible exit/browser controls and a controlled test page.

### What remains

Media3/DASH, the native NowShowing catalog and source adapters, full collections/layout/background editing, Room-backed per-episode history, backup import, Brave-specific routing, filtering, widgets and launcher replacement remain future work. Native playback, PC connectivity and remote navigation need actual-TV testing. The full [PRD](docs/PRD.md) remains the delivery scope.

Primary target: the owner's Sony Plus TV, reported **Android 14 / 2 GB RAM**. Actual API level, ABI, WebView, codecs and launcher behavior await device testing.

## Product

- Standalone Android APK with optional launcher support.
- Native TV dashboard and content browsing, fully usable with a remote.
- Personal collections, favorite apps, watchlist and saved links.
- Genuine Resume when progress is available; Recently opened when it is not.
- YouTube favorites and native browsing, with official-app playback proposed initially.
- **[NowShowing-Stream](https://github.com/smile-plzz/NowShowing-Stream) is the only existing streaming baseline.**
- Browser customization on the home network; ordinary TV use does not need the PC running.
- Brave handoff and separate web filtering subject to compatibility tests.
- No mandatory cloud account; online video still needs internet.

The first usable release and full requested scope are distinct. Launcher/web-protection outcomes and the native YouTube experience stay tracked until implemented or specific limitations are accepted by the owner.

## Documentation map

| Read | Purpose |
|---|---|
| [Decisions and scope](docs/DECISIONS.md) | Confirmed requirements, proposals, outstanding decisions |
| [PRD](docs/PRD.md) | Priorities, requirement IDs, success targets and release scope |
| [TV and browser UX](docs/UX-SPEC.md) | First run, remote navigation, collections, editing and error flows |
| [Requirements analysis](docs/REQUIREMENTS-ANALYSIS.md) | Thread reconciliation, feasibility and tradeoffs |
| [NowShowing source audit](docs/NOWSHOWING-AUDIT.md) | Verified code findings and limits of review |
| [Architecture](docs/ARCHITECTURE.md) | Data, playback, local editor and lifecycle design |
| [Development plan](docs/DEVELOPMENT-PLAN.md) | Milestones and dependencies after authorization |
| [Acceptance scenarios](docs/ACCEPTANCE.md) | Requirement coverage and actual-device test protocol |
| [Implementation handoff](docs/IMPLEMENTATION-HANDOFF.md) | Authorization, starting instructions and remaining work |
| [Build status](docs/BUILD-STATUS.md) | Executed checks, limitations and milestone coverage |
| [Build and installation](docs/BUILD-AND-INSTALL.md) | Repeatable builds, signing, install/update/rollback |
| [Device test](docs/DEVICE-TEST.md) | Owner TV checklist and evidence template |

## Key feasibility facts

NowShowing's inspected Continue Watching implementation records opened titles, not playback timestamps. Nook needs separate series selection, launch history and measured playback state.

A website embed is not automatically a native media stream. Brave Shields is not inherited by Nook's WebView. YouTube account-wide history is not promised. Provider and device capabilities must be demonstrated rather than inferred from labels.

## Documentation verification

The initial documentation baseline was reviewed before implementation. Current executable checks and unrun tests are recorded in [build status](docs/BUILD-STATUS.md). A successful APK build is not evidence of provider playback or real-TV compatibility.

