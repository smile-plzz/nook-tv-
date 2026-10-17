# Requirements analysis

2026-10-17 | Finalized documentation baseline v1.0. User discussion and targeted NowShowing-Stream source inspection; no device tests or implementation.

## Confirmed versus proposed

| Item | Status |
|---|---|
| Personal TV dashboard, standalone APK with launcher support | Owner decision |
| 2 GB RAM, Android 14 | Owner-reported, device capabilities pending |
| PC browser customization | Owner requirement |
| Continue Watching, YouTube, online video and Brave-style blocking | Owner requirements with capability-specific limits |
| NowShowing-Stream as streaming baseline | Explicit owner correction |
| Kotlin, Compose for TV, Room, Media3 | Proposed architecture |
| Local-network editor, no required cloud | Proposed default consistent with household use |
| Native catalog with web embeds when needed | Proposed interpretation of native support |
| CineVault/CineGemini-Stream/V2 reuse | Excluded by selected baseline |

## Source findings and implications

1. The selected repo is vanilla JavaScript ES modules and CSS, not React. A native TV shell should reuse domain behavior, not copy desktop DOM layouts into a full-screen wrapper.
2. Continue Watching stores metadata and updatedAt in localStorage, capped at 20. Entries are added when opening details. The inspected modal does not persist selected season/episode or playback position. Migration must treat these as recently opened, never accurate progress.
3. The source preference is global and updated on selection. Its comment claims last-working/per-title behavior that the implementation does not provide. Nook needs explicit selected versus confirmed-working states.
4. The source registry includes 11 configured providers. Configuration is evidence of intended integration, not evidence any provider is live, playable or compatible.
5. URL building uses provider-name/domain heuristics and IMDb IDs. Each provider needs explicit ID-format requirements and test fixtures; real TMDB mapping is required where numeric IDs are needed.
6. Web playback is iframe-based. A 12-second blank-screen hint is a timeout hint, not playback verification.
7. The inspected player has no documented progress event bridge. Adding a generic DOM listener will not make cross-origin playback observable.
8. The README identifies a public OMDb demo key. Do not reuse it as a production dependency; provide owner-configured credentials, caching and quota errors.
9. Modal focus handling is Tab/Escape-oriented, not proof of TV D-pad support.
10. Existing browser localStorage does not automatically appear on the TV. Any migration must use an explicit versioned export/import route.

See [source audit](NOWSHOWING-AUDIT.md) for reviewed paths and blob identifiers.

## Feasibility matrix

| Capability | Assessment | Required proof |
|---|---|---|
| Native dashboard and app tiles | Feasible target | Actual-TV focus, launch and performance |
| Native direct-media playback/resume | Feasible for supported formats | Device codec and stream tests |
| Native browsing of NowShowing catalog | Feasible target | API, identifiers, metadata caching |
| All existing embeds inside native player | Not established | Direct-media support or documented provider integration |
| Timestamp resume for opaque embeds | Unavailable without cooperation | Validated player events and resume command |
| YouTube app handoff | Feasible target | Installed handler and remote tests |
| Account-wide YouTube history import | Not offered through public Data API | Keep outside promised scope |
| Embedded official YouTube | Conditional | Current official requirements and device WebView/input tests |
| Brave opening links | Conditional | Installation, handler and navigation |
| Brave Shields inside ordinary Nook WebView | Not inherited | Separate filtering implementation needed |
| Home-launcher replacement | Conditional | Vendor firmware behavior |
| LocalFlix TV playback | Future integration | PC server/API and reachable media URLs |

## Requirements conflicts and decisions

### Native versus web sources
Keep dashboard, search, details and owned playback native. Render website-only sources in one isolated web-player screen. Offer browser fallback rather than label unsupported embeds as native streams.

### Continue Watching versus launch history
Store progress capability and provenance. A video title opened is not a video watched. Keep resume entries distinct from recently opened entries.

### Blocking versus player reliability
Filtering is opt-in and site-specific. Site exceptions are saved separately from source selection. Report observed success/failure, never universal ad-block guarantees. YouTube official-player integration follows its own requirements.

### PC control versus local security
A nearby device must pair before edits. TV approval, rate limits, revocable sessions, origin/host validation, revision checks and a foreground-only server form the initial boundary. LAN HTTP is not confidential against an on-path attacker; no provider-password entry through the editor. Evaluate TLS before broader credential-bearing controls.

### Rich home versus 2 GB RAM
One active playback context, no background autoplay, resized artwork, pagination/lazy rows, cache quotas and graceful low-memory recovery take priority over visual effects.

## Primary risks

| Risk | Mitigation | Fallback |
|---|---|---|
| Vendor Android differs from advertised features | Inspect actual APIs/ABI/WebView | Standalone compatible build |
| Provider changes or refuses embeds | Versioned source adapters, capability matrix | Open original site / source selection |
| Remote cannot operate third-party player | Real-TV tests with visible fallback | External app/browser, optional pointer mode later |
| Filter breaks video/login | Per-site off switch and clear diagnostics | Unfiltered route by explicit choice |
| Metadata quota exhausted | Bounded cache, request de-duplication, backoff | Cached catalog and manual links |
| Stale PC changes overwrite newer state | Versioned atomic updates | Reload/reapply conflict flow |
| Power loss corrupts user state | Transactional persistence and import staging | Last committed database |
| Upstream license not established | License review before copying | Independent implementation of behavior |

## Evidence references

- [Android Media3 format support](https://developer.android.com/media/media3/exoplayer/supported-formats)
- [YouTube public API history limitations](https://developers.google.com/youtube/data/revision_history) — January 28, 2021 entry.
- [YouTube embedded-player requirements](https://developers.google.com/youtube/terms/required-minimum-functionality)
- [YouTube developer policies](https://developers.google.com/youtube/terms/developer-policies)
- [Brave Shields](https://brave.com/shields/)
- [TV product listing](https://sonyplusbd.com/product/43-inch-frameless-voice-smart-android-led-tv/) — contradictory Android versions; owner reports 14.

These references inform planning. Recheck current integration documentation when implementing each adapter.

## Thread-to-scope reconciliation

| Conversation evidence | Final requirement treatment |
|---|---|
| Personal space tailored to interests, not a public platform | PERS-01 collections are P0; single-owner local-first default; no mandatory account |
| Customize from a PC browser and reflect on TV | PC-01/PC-02 plus SEND-01; transactional Save, conflict handling and visible connection state |
| Standalone app with launcher support | Standalone delivery first; launcher stays tracked rather than dropped |
| Native YouTube support | Native browsing plus official app handoff proposed initially; embedded route remains evaluated; clarify capability before full-scope closure |
| Continue Watching | True timestamps only when observable; selected episode and Recently opened supported independently |
| Other sites and native online streaming | Native catalog/player where source supports it; isolated embed/browser route otherwise |
| Brave ad blocker support | Brave handoff plus separately evaluated filtering; no Shields inheritance assumption |
| Explicitly use NowShowing-Stream | Sole streaming baseline; no CineVault or V2 substitution |
| Owner will say when to start building | Documentation-only hold, recorded in README and implementation handoff |

## Review corrections in v1.0

- Moved explicit personal collections into P0; recommendations remain later so basic personalization is not delayed.
- Distinguished first usable release from completion of the broader requested scope.
- Clarified that native YouTube handoff is a proposed interpretation rather than an owner-approved substitute for in-app playback.
- Added usable first run without a PC, save-from-PC behavior and no automatic playback interruption.
- Defined foreground editor disconnect/reconnect behavior and lost-save recovery.
- Separated series selection, recent playback attempts and measured playback progress.
- Clarified offline dashboard versus online video, current metadata versus source availability, and curated rows versus real-time trending.
- Added portable backup boundaries and TV-only API-key entry for the initial HTTP editor.

Personal context informs the preference for a lightweight, polished and manageable product. It does not authorize importing private history, relationship details, health information or account content into a TV visible to others. Interest collections start with owner-selected content.
