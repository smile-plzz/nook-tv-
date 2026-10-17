# Nook TV

Your personal space on the big screen: apps, Continue Watching, YouTube, NowShowing movies and series, and collections tailored by you from a PC browser.

## Status

**Documentation v1.0 finalized — 17 October 2026. Implementation has not started.**
The owner will explicitly say when to start building. Do not treat this planning baseline as permission to scaffold, install dependencies, configure CI, build an APK or deploy anything.

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
| [Implementation handoff](docs/IMPLEMENTATION-HANDOFF.md) | Starting instructions, implementation hold and readiness |

## Key feasibility facts

NowShowing's inspected Continue Watching implementation records opened titles, not playback timestamps. Nook needs separate series selection, launch history and measured playback state.

A website embed is not automatically a native media stream. Brave Shields is not inherited by Nook's WebView. YouTube account-wide history is not promised. Provider and device capabilities must be demonstrated rather than inferred from labels.

## Documentation verification

All relative document links and requirement-to-acceptance mappings are checked during finalization. No application tests, provider playback tests or real-TV tests have been run.

When authorized, the first milestone is an installable compatibility APK testing remote navigation, sample playback, web-player isolation, YouTube handoff and PC pairing.
