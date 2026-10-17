# Decisions and scope record

Baseline 2.1 | 17 October 2026 | Supersedes dashboard-first baseline

Owner instructions govern; [PRD](PRD.md) is current scope. A design proposal is not an implemented capability. Baseline 2.1 was documentation-only. Subsequent owner authorization produced the 0.3 [launcher prototype](LAUNCHER-PREVIEW.md); it is not full device acceptance.

## Confirmed direction

| ID | Decision | Basis and consequence |
|---|---|---|
| D01 | Personal default TV home with media convenience | Updated by owner after BlueStacks use; launcher is core, not a late add-on |
| D02 | Default screen when pressing Home | Explicit answer; LAUNCH-01 becomes P0; keep normal app entry for recovery |
| D03 | NowShowing-Stream remains selected upstream | Original owner correction retained; catalog is secondary to launcher/phone workflows |
| D04 | PC browser customization | Existing requirement retained; phone-responsive companion proposed for sharing |
| D05 | YouTube and phone-shared video are principal activities | Explicit answer; prioritize direct app access and Shared & Recent |
| D06 | Brave/filtering remains requested later scope | Rare browser use reduces priority; no implied cancellation or universal ad-block promise |
| D07 | Android 14 / 2 GB TV | Owner-reported, actual capabilities still unverified |
| D08 | Apple TV-style aesthetics and overall experience | Explicit answer; app artwork/grid, readable spacing, predictable focus; no Apple asset copying |
| D09 | Easier return to media shared/watched from phone | Explicit answer; durable replay when available, honest reconnect state otherwise |
| D10 | Spotify is tentative, browser rare | Optional shortcut; do not assume Spotify account client |
| D11 | Google Drive desired if possible | Conditional integration with feasibility/authentication gate, not mandatory sign-in |
| D12 | BlueStacks boots and allows light exploration | Owner observation only; no thorough test or TV acceptance claim |

## Engineering proposals and boundaries

| ID | Decision / proposal | Status |
|---|---|---|
| E01 | Retain Java/native Views, Media3 and versioned AtomicFile stores | Existing implementation; replaces earlier default Compose/Room proposal |
| E02 | Redesign Home before more catalog expansion | New sequencing proposal reflecting confirmed priorities |
| E03 | One-OK app launch, hold OK/context alternative for management | Implemented for launcher apps; owner partially confirms navigation; added shortcuts remain planned |
| E04 | Local-first core; same-LAN companion, no always-on PC/account | Retained design; Drive/services optional |
| E05 | Retained Safari file-transfer prototype plus receiver capability assessment | Implemented in 0.4, awaiting owner transfer test; external AirPlay integration now optional under D18 |
| E06 | Home alias/system default selection with restore path | Alias/settings path implemented; observed Home outcome fails; role prompt planned under E15 |
| E07 | Foreground editor retained; background receiving separately assessed | No assumption that launcher status changes server lifecycle |
| E08 | Drive selected-file access first; scope/picker proof before folders | Conditional design; tokens never through editor HTTP |
| E09 | Native progress vs external opens vs live phone sessions | Required truthfulness; no universal cross-app history |
| E10 | No private context/account/Photos import by default | Privacy boundary retained |
| E11 | No upstream code/assets copied without reuse rights | Existing unresolved NowShowing license |
| E12 | Full-product closure requires actual core outcomes | Build/emulator pass or standalone fallback is insufficient |

## Superseded wording

- Standalone dashboard first, optional launcher later: replaced by default-home-first product.
- Personal dashboard as a counterpart to an app launcher: replaced by an app launcher with personal media convenience.
- NowShowing completion before Home quality: replaced by launcher, phone sharing and return-to-media sequence.
- Text-button operational dashboard as usable release: rejected by owner feedback.
- Room/Kotlin/Compose as assumed next implementation: no longer a commitment.
- Spotify/Drive as fixed mandatory services: never confirmed; remain optional/conditional.

## Authorization and next work

Earlier owner messages authorized implementation and continuing without immediate TV testing. The pivot instruction requested documentation updates; after merging, the owner requested the next steps. The 0.3 prototype now delivers the reviewable launcher interface and initial Home setup. 0.4 adds the [paired-browser retained-file path](PHONE-TRANSFER-PREVIEW.md); AirPlay and account connections remain unimplemented. Further work follows [development plan](DEVELOPMENT-PLAN.md).

## Open implementation decisions

Exact iPhone source/gesture (AirPlay, Photos, Files, app cast or URL), actual receiver/TV support, selected Drive authorization UX, transfer storage/transport limits and exact launcher layout metrics need validation. They do not block documenting or prototyping the confirmed default-home and Apple TV direction. Any narrower substitute for a core outcome must be explicit and owner-accepted.

## Baseline 2.1 decisions after physical-TV feedback

| ID | Decision | Status / consequence |
|---|---|---|
| D13 | Basic remote/app paths feel functional; appearance only partially meets intent | Owner observation from a few minutes, not exhaustive acceptance |
| D14 | Default Home and restart return to existing environment | Observed failed outcome; exact destination/setup/firmware unknown; F1 highest priority |
| D15 | Add dynamic YouTube cards and mobile/PC-configurable RSS | Confirmed request; DYN-01/02 P0; start selected public sources, exact provider design proposed |
| D16 | Modern, minimal and premium across Home and companion | Confirmed; shared visual language, helpful controls and inline guidance; PC-03 |
| D17 | Explore and refine retained iPhone videos | Confirmed interest; upload not owner-tested; personal shelf proposal PHONE-03 |
| D18 | External AirPlay replay/integration is optional | Explicit owner instruction; AIR-01 conditional, not a core release blocker |
| E13 | F1-F6 replaces L0-L5 execution order | Plan updated; historical preview stage names retained as evidence |
| E14 | Small Nook-rendered content modules, bounded refresh, no autoplay | Engineering/design proposal; no arbitrary widget framework |
| E15 | Home role request plus capability fallback and verified status | Proposed fix path; current code only enables alias/opens settings |
| E16 | Schema v3 for source/module settings with migration | Proposed only; 0.4 remains v2, media/cache/secrets separate |

This request authorizes analysis and repository documentation updates. The resulting documentation PR does not implement the planned features or claim a new build. All earlier engineering proposals must be read with current implementation evidence in [build status](BUILD-STATUS.md), and the [feedback record](TV-FEEDBACK.md) supersedes earlier claims that no TV observations exist.
