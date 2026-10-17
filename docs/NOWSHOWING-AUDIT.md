# NowShowing-Stream source audit

Baseline 2.1 priority note: [TV feedback](TV-FEEDBACK.md) promotes refined Home, selected YouTube/RSS modules and companion usability ahead of catalog expansion. This audit remains historical; its Room/DataStore mapping is superseded by current Java/AtomicFile [architecture](ARCHITECTURE.md). No provider has been newly tested by this documentation update.

> Scope after the 17 October 2026 pivot: NowShowing-Stream remains the selected streaming reference, but catalog/provider work is secondary to default Home, the Apple TV-style app grid and iPhone sharing/reopen. The source findings below are historical inspection evidence, not proof of launcher, receiver, Drive or provider support. Current priorities are in [PRD](PRD.md).

Reviewed: 2026-10-17.
Selected upstream: https://github.com/smile-plzz/NowShowing-Stream
Scope: README and four core files, fetched from the default branch. This is a targeted requirements audit, not full code review or live-provider testing.

## Reviewed evidence

| File | Git blob SHA at read | Findings |
|---|---|---|
| [README.md](https://github.com/smile-plzz/NowShowing-Stream/blob/main/README.md) | c260c63ef5d38632680a23b5494d3de7dc08e248 | Dependency-free JS modules, OMDb, moods, iframe sources, local lists |
| [js/storage.js](https://github.com/smile-plzz/NowShowing-Stream/blob/main/js/storage.js) | 118e33ef99fe01c9ca44802c28b4665f41d28055 | ns_wl watchlist, ns_cw recent entries capped 20, timestamp updatedAt, mood cache cap 400 |
| [js/components/modal.js](https://github.com/smile-plzz/NowShowing-Stream/blob/main/js/components/modal.js) | 017581ec21dd34e168fc3743ccb764eaccef01d1 | Inserts recent entry on detail open, iframe loader, season/episode UI, Tab focus trap |
| [js/sources.js](https://github.com/smile-plzz/NowShowing-Stream/blob/main/js/sources.js) | d382a69b792aa7704f17c25059a24755c0a40b9a | Provider-specific URL heuristics; global selected-source storage |
| [js/config.js](https://github.com/smile-plzz/NowShowing-Stream/blob/main/js/config.js) | fee457fe22b22bd195c1c73dd67f4fbb85fd0594 | Catalog seeds, public demo key, 11 configured providers |

Blob SHAs identify file contents, not repository commits. Links point to the branch and may change. Pin a full upstream commit before implementing reuse.

## Continue Watching findings

openModal immediately calls upsertContinue with a loading title, then calls again after metadata arrives. Stored entries carry title metadata and updatedAt, with no playback timestamp.

current includes season/episode in memory, but the inspected calls to upsertContinue do not persist either. New opens initialize season and episode to 1.

Implication: current behavior is recent-title access, not proven episode or timestamp resume. Imported entries must not appear as precise progress. Adding metadata alone is insufficient; Nook needs player observations or a provider event contract.

## Source preference findings

ns_last_source is one global source name. setPreferredSource runs when a source button is clicked. No playback confirmation is required, despite a comment describing last-working/per-title behavior.

Implication: Nook should store selected source separately from confirmed-playing state. A source can be preferred without having played successfully.

## Embed findings

The modal creates an iframe with video-related permissions and a no-referrer policy. The parent schedules a 12-second hint suggesting another source. The reviewed code has no player progress postMessage contract.

Implication:
- Iframe load or HTTP response cannot certify video success.
- Do not inherit referrer settings blindly across providers.
- No automatic cross-origin progress access is assumed.
- Remote-accessible outer controls must remain available if iframe focus is unsuitable.

## Configured provider inventory

These are configured upstream names, not verified services.

| Name | Native direct-stream support | Timestamp resume | Actual-TV test |
|---|---|---|---|
| VidSrc.to | Unverified | Unverified | Pending |
| VidSrc.icu | Unverified | Unverified | Pending |
| VidSrc.me | Unverified | Unverified | Pending |
| AutoEmbed | Unverified | Unverified | Pending |
| 2Embed | Unverified | Unverified | Pending |
| 2Embed.stream | Unverified | Unverified | Pending |
| VidSrc.xyz | Unverified | Unverified | Pending |
| MoviesAPI | Unverified | Unverified | Pending |
| MultiEmbed | Unverified | Unverified | Pending |
| EmbedSu | Unverified | Unverified | Pending |
| VidSrc.mov | Unverified | Unverified | Pending |

Source builders mostly pass IMDb IDs, with domain-string branches for a few providers. Validate required identifier formats independently; never put IMDb text into a numeric TMDB field.

## Reuse plan

Reuse as behavioral references:
- Catalog/search/detail/season/episode model.
- Watchlist and selected-source concepts.
- Mood discovery and metadata-caching strategy, after inspecting those actual files.

Replace/adapt:
- DOM, click/Tab-first focus, responsive phone layouts -> native TV D-pad UI.
- localStorage as system of record -> Room/DataStore with versioned export/import.
- Implicit source assumptions -> typed capabilities and tested adapters.
- Recent-title list -> explicit recent opens plus independently tracked resume.
- Public demo key -> owner credentials and quota-aware caching.

Not inspected yet: full API/mood implementation, complete routing/pages, license, tests, deployment, live providers. Do not claim those are audited.

## Documentation baseline 1.0

This targeted audit is retained as evidence, not expanded into a claim of full review. [Requirements analysis](REQUIREMENTS-ANALYSIS.md) records its product implications; [acceptance](ACCEPTANCE.md) defines future integration checks. Documentation finalization has not tested any provider or changed the upstream source.
