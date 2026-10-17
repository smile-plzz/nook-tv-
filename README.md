# Nook TV

Your personal space on the big screen: apps, Continue Watching, YouTube, NowShowing movies and series, and collections tailored by you from a PC browser.

## Status

**0.2.0 preview in development — 17 October 2026.**
The owner asked to continue while TV testing is unavailable. This preview expands the dashboard/editor, adds Media3 playback and begins native movie/series browsing. Target-TV acceptance remains pending; this is not a completed P0 or full-product release.

Start with [build and installation](docs/BUILD-AND-INSTALL.md), [current implementation status](docs/BUILD-STATUS.md), and the [TV test checklist](docs/DEVICE-TEST.md).

### What this APK can do

- Remote-friendly native Home, Apps, Settings and diagnostics.
- Discover, pin, launch and unpin installed apps.
- Organize apps, saved links/titles and personal collections; reorder/hide Home rows and customize colors/photos from the paired browser editor.
- Export/import versioned dashboards, with revision checks, persistent mutation receipts and a pre-import recovery snapshot.
- Play native MP4/HLS/DASH using Media3, with multiple measured resume entries and supported audio/subtitle controls.
- Search movie/series metadata natively using an optional owner-entered OMDb key; save titles, choose episodes, and use owner-configured web source contracts.
- Hand off YouTube to its installed app or an external browser.
- Open an isolated web screen with visible exit/browser controls and a controlled test page.

### What remains

Provider URL contracts/playback and actual TV operation remain unverified. No provider is enabled by default. Richer catalog artwork/episode browsing, native YouTube discovery, Brave-specific routing, filtering, widgets and launcher replacement remain future work. Durable bounded state currently uses atomic versioned JSON rather than Room; this deviation is recorded in build status. The full [PRD](docs/PRD.md) remains the delivery scope.

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

