# Proposed architecture

2026-10-17 | Decision status: proposed until compatibility milestone.

## Components

| Component | Responsibility |
|---|---|
| Android application | Native TV shell, focus/navigation, activities and lifecycle |
| Dashboard feature | Rows, collections, tiles, personal layout |
| Catalog feature | NowShowing-style metadata, search, detail, seasons/episodes, mood discovery |
| Player feature | Media3 playback, tracks, progress, media session |
| Web-player feature | One isolated WebView for a selected source; no editor privileges |
| Source adapters | URL construction, ID requirements, capability declarations, event validation |
| Local data | Room database, schema migrations, DataStore preferences |
| Editor service | Foreground LAN server, pairing, configuration reads/writes, asset upload |
| Browser editor | Small TypeScript UI; no UI-framework requirement, static assets bundled in APK |
| External-app router | YouTube/browser/Brave launch, unsupported-handler states |
| Launcher alias | Opt-in HOME entry, independently testable from normal app launch |

Kotlin and Compose for TV are preferred candidates. Profile early on the actual 2 GB TV; use native Views where a measured issue warrants it. Do not choose library versions until build tooling and current official documentation are checked.

The browser editor does not require React; a small vanilla TypeScript implementation matches the selected upstream's lightweight direction. Reuse code only after license review.

## Suggested repository structure

```
android/
  app/
  core/model/
  core/data/
  core/network/
  feature/dashboard/
  feature/catalog/
  feature/player/
  feature/webplayer/
  feature/settings/
  feature/editorservice/
editor/
docs/
```

Start with packages inside one Android app module if Gradle-module overhead adds no benefit. Extract modules when boundaries justify it.

## Data model

- DashboardConfig: revision, schemaVersion, section order/visibility, theme, background asset.
- Section: stable ID, type, label, position, settings.
- Tile: stable ID, kind, label, target, sourceId, image reference.
- MediaIdentity: internal ID, provider namespace/ID, IMDb ID and actual optional TMDB ID.
- EpisodeIdentity: parent media ID, season, episode, provider-specific episode ID when available.
- PlaybackState: identity, sourceId, route, positionMs?, durationMs?, status, progressCapability, observationOrigin, updatedAt.
- LaunchRecord: target, source, openedAt, selected season/episode, no implied watch progress.
- WatchlistEntry: canonical identity or validated link, savedAt.
- SourceConfig: stable ID, enabled, required ID format, playback route, allowed origins, capability version, lastSelectedAt, lastConfirmedPlayingAt?.
- PairedDevice: ID, display name, credential verifier, issuedAt, lastSeenAt, revokedAt.
- CachedMetadata/CachedAsset: cache key, timestamps, bounded size, stale state.

Canonical identities prevent source switching from duplicating a movie. Episode identity prevents one episode overwriting another. Progress for different edits/cuts must not be combined solely by title.

## Playback contract

Each adapter declares:
- route: nativeDirect | webEmbed | externalApp | externalBrowser
- accepts: imdb | tmdb | youtube | directUrl | siteUrl
- progress: timestamp | episodeOnly | none
- canSeekToResume: true/false
- permitted event origins and event schema, if any
- live/DVR support and authentication requirements

The adapter returns a typed playback target; an embed URL is never passed to Media3 as if it were a direct stream.

Events: Requested, Loading, PlayingConfirmed, ProgressObserved, Paused, Ended, Failed, TimedOut, Handoff.
An external handoff or iframe load never emits PlayingConfirmed without evidence.
Timeout means unknown/inconclusive, not proven outage.

For documented web progress messages, validate exact origin, expected session, media ID, numeric bounds and message schema. Do not trust arbitrary postMessage payloads. No privileged addJavascriptInterface bridge in third-party pages. Do not inject cross-origin scraping as a substitute for an event contract.

## Progress lifecycle

Native player persists approximately every 5 seconds and on pause/seek/stop. Room transactions protect committed state. A force-stop can lose time since the last save; the app does not claim impossible shutdown guarantees.

Finite VOD uses a configurable watched threshold, default 95%. Unknown-duration/live streams do not receive completion percentages. Native resume seeks to the recorded point; source changes retain the episode but apply timestamps only when supported and compatible.

## Browser editor and pairing

Initial availability: while Nook is in the foreground. PC on same LAN. Server address shown on TV; no port forwarding.

1. User opens Pair on TV.
2. TV creates short-lived, rate-limited pairing code.
3. Browser submits code and device name; TV displays request for explicit approval.
4. Approval issues a high-entropy revocable session credential.
5. Subsequent mutations require authenticated session, CSRF protection and matching origin/host.
6. Save includes expectedRevision; server atomically validates/applies and returns the new revision.
7. UI observes config change immediately; stale saves receive conflict responses.

Bind only intended local interfaces; validate Host and Origin against the displayed address to reduce DNS rebinding and cross-site requests. Reject arbitrary origins and wildcard CORS. Keep read operations authenticated except minimal pairing/status endpoints. Rate-limit pairing and expire pending requests.

LAN HTTP alone cannot provide confidentiality from an on-path attacker. Initial editor must not collect third-party passwords, cookies or provider sessions. Present the actual transport state; evaluate TLS/pinning-assisted setup before broader credential-bearing control. Session tokens are excluded from exports/logs and can be revoked from TV.

Background images: bounded upload size, decode/type validation, resized derivative, filename sanitization; never arbitrary filesystem paths or remote code.

## Web-player isolation

One active WebView; release it on close/memory pressure. Separate from editor content and internal assets. Disable file/content access unless a documented trusted flow requires it. Block unexpected schemes, automatic external intents, downloads and popup windows. User-approved navigation and provider-required redirects are handled by explicit policies, not silently allowed wholesale.

Website login happens in the relevant player/browser, not the LAN editor. Per-site data clearing must not erase Room-backed lists.

## Filtering

Brave handoff uses installed eligible activities and user choice; no guarantee of Android TV support.
Embedded filtering uses a separately evaluated engine/list format and updater. Review licenses, rule provenance, memory cost and update integrity. Keep a last-known-good ruleset, per-site exceptions and a reset action.

Do not claim Android WebView loads desktop extensions or automatically uses Brave Shields. Some browser/service-worker/media requests and server-inserted ads may not be covered. Record measured coverage and failures. YouTube official player is a separate integration.

## Performance and resilience

- No automatic video previews.
- Lazy rows and resized artwork; avoid loading every poster.
- One media engine or website player at a time.
- Conservative buffers sized through device tests.
- Bounded LRU artwork cache; separate durable state from disposable cache.
- Request deduplication, timeouts, backoff and quota-aware metadata cache.
- Stale cached content remains visible with clear offline state.
- Redacted rolling logs; no default remote telemetry.
- Database migration test and backup before schema-changing upgrade.
- Full-screen reboot/power loss tests on actual hardware.

## Distribution

Versioned sideload APK first. Establish stable application ID and signing identity before regular user installation. Keep signing secrets outside source control and preserve them across releases. GitHub CI should build and test from pinned toolchain/dependencies; a release is not considered device-tested just because CI passes.
