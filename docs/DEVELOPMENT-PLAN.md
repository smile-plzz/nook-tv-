# Development plan after TV feedback

Baseline 2.2 | 17 October 2026 | Replaces L0-L5 execution order; historical labels remain in preview guides

## Starting point

PR #5 is merged at 247671613c9552fa9f67eda179a67528a680ddcf. Main contains 0.4 retained transfer plus the 0.3 launcher foundation. The owner's brief TV trial partially confirms smoothness, app launching and remote use; visual quality needs refinement and default Home fails in the observed use. File transfer/resume were not tested. Exact tested APK/firmware remain to be recorded. See [feedback](TV-FEEDBACK.md).

The revision includes removal of embedded movie discovery and compatibility cleanup, but does not implement the planned Home/design/content features or deliver a new APK. Deliver small reviewable changes with updated docs/evidence. Keep movie-discovery/account/provider logic in independent installed apps; do not rewrite the UI stack before resolving daily-use gaps.

| Stage | Deliverable | Exit evidence |
|---|---|---|
| F1 Home selection and diagnosis | Explicit system Home-role request where available, fallback settings, verified status, retry and recovery | Owner TV: select Nook, Home from YouTube, restart/wake, restore original; inability is recorded as blocked core behavior |
| F2 Coherent visual design | Unified grid/cards/typography/spacing/focus, reduced motion, populated and empty examples of a small content shelf | Owner approves TV screenshots and remote flow; preserve direct launch; no focus jumps |
| F3 Companion and first live sources | Responsive guided editor, shared preview vocabulary, source validation and visibility/order; YouTube selected-source and RSS adapters | A12/A13 including exact-video launch, stale/offline, invalid feed, safe credentials, conflict/draft and migration tests |
| F4 Personal video shelf | First verify 0.4 real transfer; then QR/help, thumbnails, titles, organization and storage refinement | Real Safari upload, saved receipt, phone disconnect, replay/resume, interruption/codec/storage checks; A14 |
| F5 Daily-use hardening | Shortcut discoverability, populated-Home profiling, two-hour stability, upgrades and recovery | All P0 gates on target TV/iPhone; explicit owner visual approval |
| F6 Conditional integrations | Optional external receiver metadata/replay, Drive selected-file proof, optional Spotify; retained YouTube player/browser/filtering scope | Each integration has its own capability evidence and owner-accepted limits |

F1 and F2 can progress independently. Run the existing phone transfer test as soon as the owner is available, without waiting for F3; use the result to shape F4. F3 provider credentials/quotas are an early spike, not a late surprise. Ship the companion/source slice only when it produces useful real content; fixtures alone do not satisfy DYN-01/02. F1 failure must not be hidden by a prettier Home. AirPlay is optional and does not block F5.

## F1 implementation detail

Current MainActivity enables HomeEntry and opens ACTION_HOME_SETTINGS, with manual status refresh. It does not call RoleManager. Add a coordinator that enables the eligible alias on explicit setup, checks API 29+ and isRoleAvailable(ROLE_HOME), skips prompting if held, and calls createRequestRoleIntent when supported. Handle cancel/denial/unavailable activity without claiming success. On return/resume re-read role/default handler; where signals disagree show unresolved status. Older or unsupported systems use resolvable Home settings then general-settings guidance. An ACTION_CHOOSER dialog does not itself establish a persistent default.

Keep ordinary app entry and stock launcher, document recovery and retain the guard against removing Nook while default. Do not force Home by disabling packages, intercepting keys, a boot activity launch or accessibility workaround. Record exact firmware if normal selection is blocked; any alternative needs a separate product decision.

## Design and engineering dependencies

Read [dynamic Home](DYNAMIC-HOME.md) and [companion](COMPANION-SPEC.md). Proposed schema v3 includes module/source settings and portable appearance choices, with migration from v1/v2; caches, credentials and retained media stay separate. Extract Home setup, content aggregation and presentation responsibilities from MainActivity incrementally, preserving storage/player contracts. No Compose/Room rewrite is assumed.

## Validation and release

Preserve relevant regression coverage, editor/transfer state checks and Android runtime scenarios. Remove retired provider tests and add migration/cleanup checks; the historical 82-local/43-Android counts describe the delivered preview, not the revised suite. Add behavior tests for role outcome states, parser/URL bounds, source refresh, stable focus, migrations and new mutations when implemented. Use real TV/iPhone evidence for Home, handlers, codec, UX and performance. Check documentation links and requirement coverage on every scope change. Record source commit/APK/device/steps/results; no new deadline or version is promised until a slice is built.

Default Home, the refined visual design, configurable selected YouTube/RSS content, companion usability and one proven retained-file path are first-usable-launcher requirements. Full broader product scope still includes requested conditional integrations; see [PRD](PRD.md) and [acceptance](ACCEPTANCE.md).
