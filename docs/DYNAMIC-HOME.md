# Dynamic Home specification

Baseline 2.1 | Proposed, not implemented in 0.4 | DYN-01, DYN-02, VIS-01, NAV-02

## Composition

Keep a calm app grid and add useful content beneath it. First prototype: quiet clock/header, one or two rows of favorites according to viewport, then a single mixed or source-specific shelf of three to five visible cards. Keep All Apps, From iPhone and Settings reachable. Show retained videos only when present. Owner can hide/reorder modules; no fake data or preselected news sources. A source setup prompt belongs in Customize, not a permanent empty Home tile.

Use a shared scale for margins, card radii, text sizes and focus treatment across Home, menus and media. Prototype starting values: 48 dp safe inset, 24 dp card gaps, 16 dp radii, 1.04 focus scale over 140 ms; tune against actual TV density/overscan rather than treating these as approved dimensions. Focus includes a high-contrast outline, not color alone. Reduced motion removes scaling/transitions. Keep text readable from the sofa, a maximum of two title lines, source labels and consistent artwork crops. No autoplay, marquee ticker, timed carousel or live wallpaper by default.

## First modules

| Module | Data and visible action | Unavailable behavior |
|---|---|---|
| YouTube latest / chosen playlist | Thumbnail, title, channel, publication time; OK opens that video in eligible YouTube handler | Retain permitted cached metadata with freshness label; unavailable video offers source/app route |
| RSS/Atom updates | Headline, publisher, date, optional thumbnail; OK opens concise plain-text detail with Open article action | Last known items plus stale/offline state; empty/error setup in companion |
| Saved on this TV | Local thumbnail/title/duration and measured progress; Play/Resume | Missing copy becomes unavailable, not a dead playback request |
| Clock/date | Quiet local information | Works without network |

Weather, arbitrary information providers and third-party widget hosting are not part of the first module delivery. Native local content can work before network sources are configured.

## YouTube source decision

Preferred supported implementation: official YouTube Data API for owner-selected public channel uploads or playlists. Channel uploads are resolved to the uploads playlist; playlistItems supplies video references. Batch metadata as necessary, cache, and budget quota before rollout. Do not poll search on every Home visit. This requires an enabled Google project/API credential; choose a constrained credential provisioning/distribution approach in the implementation spike. Never ship an unrestricted developer secret or transmit credentials through the current HTTP companion. No mandatory credential for core Home. If a usable source cannot be configured safely, show Setup needed in the companion and keep app/saved-link access working; this is not completion of DYN-01.

Public channel feeds may be evaluated as an alternative only after checking their current supported contract, reliability and applicable service rules. Do not promise that they replace the official API or offer authenticated recommendations. No scraping logged-in pages. Personal Home recommendations, watch history and Watch Later are not first-release feeds; the official playlist API explicitly does not expose watch history/Watch Later. Do not label a Nook collection as YouTube Watch Later.

Canonical video IDs and validated HTTPS links drive routing. One OK opens the exact selected video, rather than only the YouTube landing page. Missing handler offers an explicit browser fallback; no Nook timestamp for opaque external playback. Test private/deleted/region-limited content and handler differences on the owner's TV.

## Refresh, focus and resource contract

Proposed initial bounds: up to 6 configured network sources, 20 cached items per source, 6 cards per module before See all; at most 2 concurrent fetches. Refresh due sources on foreground entry with a 30-minute minimum normal interval; manual refresh is throttled, respects provider limits and backs off after errors. No always-on service is needed for the initial implementation. Read supported cache validators and provider expiry/deletion requirements; provider policy overrides these engineering defaults.

Render cached apps/content first. A fetch may update content but must not move the focused card: retain stable identity, defer removal/reorder until focus leaves or choose a documented nearest fallback. Preserve row/scroll on return. Show last successful refresh rather than falsely calling cached content live. Network failure must not delay app launching.

Provisional budgets: 10-second request deadline, 2 MiB decompressed feed body, 3 redirects, 8 MiB decoded image cache and 32 MiB image disk cache. Decode sampled images off main thread; validate content type and pixel bounds. Measure and adjust budgets on the 2 GB TV. No parallel players for cards.

## Feed parsing and data ownership

Fetch feeds on the TV so browser CORS is not a dependency. Accept public HTTPS RSS/Atom; reject credentials in URLs, unsupported schemes, loopback/private/link-local destinations and equivalent redirect/DNS resolutions. Fetch no arbitrary local admin endpoints. Bound bytes, item count, time, nesting and title length; disable XML DTDs/external entities. Treat descriptions as untrusted text, never executable HTML. Article/image URLs get their own validation; strip tracking where safe without breaking source identity. Private/authenticated/LAN feeds require a separate design, not implicit support.

Store source configuration (stable ID, type, canonical reference, label, enabled/order and presentation) separately from fetched items/status and secrets. Export permitted public configuration, not cache, credentials or private media. Proposed dashboard schema v3 must migrate v1/v2 atomically, preserve existing favorites/collections and refuse unsupported newer schemas without data loss. Old apps cannot be assumed to read v3 exports; clearly label compatibility. No schema change is delivered by this documentation PR.

## Verification and sources

Acceptance A12 covers exact-video routing, RSS validation, focus stability, offline/stale states and refresh limits. A08 measures device responsiveness with populated modules; appearance needs owner approval.

Primary references checked 17 October 2026:
- [YouTube playlistItems.list](https://developers.google.com/youtube/v3/docs/playlistItems/list): playlist retrieval, errors and unavailable watch history/Watch Later.
- [YouTube playlists implementation](https://developers.google.com/youtube/v3/guides/implementation/playlists): uploaded-video playlist approach.
- [Android TV navigation](https://developer.android.com/design/ui/tv/guides/foundations/navigation-on-tv): directional focus, selection and manufacturer-dependent system keys.

These references establish platform primitives; the layout, budgets and selected-source strategy are Nook design proposals, not platform guarantees.
