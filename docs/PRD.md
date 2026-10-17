# Nook TV product requirements

Current owner feedback: brief TV use partially confirms app launching/remote navigation, requests a more refined and dynamic Home, and reports Home/restart returning to the existing environment. Retained-file upload/resume were not tested. [TV feedback](TV-FEEDBACK.md) records evidence and uncertainties. Baseline 2.2 promotes selected YouTube/RSS content and companion refinement into P0; it does not claim these are implemented.

Baseline 2.2 | 17 October 2026 | Owner: Ismail Hossain | Launcher pivot confirmed; 0.4 launcher/file-transfer preview implemented, acceptance pending

## Product and authority

Nook is Ismail's default TV home screen, with Apple TV-style aesthetics and remote navigation. It brings favorite apps, media received from his iPhone and useful return-to-media shortcuts together. APK delivery remains Android; Apple TV is the experience reference, not the operating system or a compatibility claim.

This baseline supersedes the earlier standalone-dashboard-first plan. Explicit owner instructions take precedence, then this PRD. [Decisions](DECISIONS.md) records confirmed choices; [build status](BUILD-STATUS.md) records delivered behavior. The existing 0.2 APK is infrastructure, not the approved launcher experience.

## Owner evidence

Ismail installed 0.2 in BlueStacks and explored it briefly. It boots, but the structure does not meet his intended navigation or functionality. This is limited emulator feedback, not thorough testing or target-TV acceptance.

Confirmed on 17 October:
- Nook should be the default screen when pressing Home.
- Apple TV-style aesthetics and overall interaction are the design reference.
- Main use: YouTube, video shared from iOS, easier return to previously shared/watched media.
- Spotify is a possible secondary app; browser use is rare.
- Google Drive access is wanted if feasible.
- Existing requirements for PC customization and personal collections remain relevant.

## Daily experience

Turn on the TV or press Home, see familiar app artwork, and open YouTube or another favorite with one OK press. Open From iPhone to receive media through the configured supported route. Later find that item in Shared & Recent, with Resume only when Nook has measured progress and a replayable source. Open Google Drive when configured; missing optional services must not clutter Home or block setup.

Favorite apps, useful content cards and phone media define the scope. Movie discovery and streaming catalogs belong in independent installed apps. No sample streams, diagnostics, pairing controls, API-key instructions or generic create buttons occupy the everyday Home.

## Home and navigation

Default composition:
1. Quiet header: clock and small Settings access.
2. Favorites: prominent grid of recognizable app banners/icons; YouTube first when installed.
3. Continue Watching: shown only when genuine resumable entries exist.
4. Shared & Recent: retained phone media, saved sources and truthful reopen actions.
5. Owner-configured YouTube/RSS cards and collections; no embedded movie-discovery rows. Dynamic cards are hideable and do not displace the favorite-app anchor.

Favorites are a configurable grid, not a dense settings menu or a mandatory Netflix-style feed. From iPhone is a first-class utility tile. Spotify, Browser and Drive appear when installed/configured or explicitly pinned. All Apps remains accessible; no fake installed apps or automatic sign-in. Final spacing/order is a proposed design to validate visually, not a pixel layout dictated by the owner.

Primary destinations: Home, All Apps, From iPhone, Shared & Recent, optional Drive, Settings. Reach these through a compact dock/navigation treatment. PC editor is a management companion usable on desktop and phone browsers.

## Priority and release scope

P0 = first usable launcher. P1 = requested subsequent or conditional integration. P2 = optional future work. Requested scope is not removed by assigning a later phase. Conditional capabilities need an implementation decision and explicit limitation record; a browser shortcut does not count as private Drive integration.

| ID | Priority | Requirement and acceptance |
|---|---|---|
| LAUNCH-01 | P0 | Default Home: explicit system setup, Home from another app returns to Nook, reboot/wake tested, original launcher restoration documented. No automatic system-package disabling. Firmware blockage is a failed/blocked core gate, not full completion. |
| VIS-01 | P0 | Apple TV-inspired grid, generous spacing, readable typography, recognizable artwork, restrained focus animation, stable contrast and remote-visible selection. No copied Apple assets. |
| NAV-01 | P0 | D-pad/OK/Back operate all core flows; one OK opens a favorite; management via long press plus visible alternative; restore item/scroll when returning. Home resets to a predictable starting position. |
| APP-01 | P0 | Discover launchable TV/sideloaded apps, pin/reorder/hide/rename, launch directly. Missing app state stays editable. YouTube prioritized; Spotify optional; browser secondary. |
| SETUP-01 | P0 | Fresh install usable without PC, metadata key or cloud account. Detect apps, choose favorites, explain Home setup and offer From iPhone setup. |
| PHONE-01 | P0 | From iPhone destination with honest route status, simple same-network guidance, start/open receiver or paired browser transfer, cancellation and a documented tested path. Do not label upload as AirPlay. |
| PHONE-02 | P0 | One tested iPhone-to-TV video path plays a representative owner-selected file. Document retained-copy versus live-session behavior, receiver dependency, remote controls, phone disconnect and errors. AirPlay protocol implementation is conditional, not presumed. |
| RECENT-01 | P0 | Shared & Recent lists retained or reopenable items with durable identity, origin, availability and explicit action: Resume, Play again, Open in app, Reconnect phone or Unavailable. No dead session URL presented as replayable media. |
| CW-01 | P0 | Show genuine position/duration only for observed playback; live/opaque/external sessions do not invent percentages. Recently opened is distinct from Continue Watching. |
| CW-02 | P0 | Native progress saved about every 5 seconds and at lifecycle/seek events; survives process death and same-key upgrade within last-committed tolerance. Different media items remain distinct. |
| CW-03 | P0 | Resume/start over/remove/mark watched; finite VOD default threshold 95%; clearing history does not delete retained media. |
| PLAY-01 | P0 | Media3 native supported MP4/HLS/DASH; remote pause/seek/track selection and retry; honest unsupported-codec state. One active player. |
| YT-01 | P0 | One-click YouTube app entry plus saved video/channel/playlist links and handler fallback. External history is not fabricated. |
| DASH-01 | P0 | Persistent favorites/grid order, optional content rows, background and hidden items; useful offline Home. |
| PERS-01 | P0 | Owner-created/reordered collections with stable references; deleting membership does not delete source files. |
| PC-01 | P0 | Paired same-LAN editor, TV approval, appearance/layout/collections and acknowledged Save; phone viewport usable. |
| PC-02 | P0 | Revision conflicts preserve drafts; authenticated, idempotent mutation retries; revoked sessions rejected. |
| SEND-01 | P0 | Save validated link from paired browser without interrupting playback; explicit separate play action. File-transfer endpoint is a new contract, not existing link-save behavior. |
| DATA-01 | P0 | Versioned atomic persistence and validated dashboard backup/recovery; media payloads and credentials excluded unless an explicit later export mode exists. |
| PRIV-01 | P0 | Pause/clear history; separate cache/media/account/session controls; received personal media never displayed publicly without owner action. |
| PERF-01 | P0 | Responsive on reported 2 GB TV: bounded artwork/buffers/storage; no automatic video previews or expensive continuous blur. Measure on device. |
| SPOT-01 | P1 | Optional Spotify app shortcut/handoff; do not implement an account/music client by assumption. Core launcher does not require Spotify. |
| DRIVE-01 | P1, conditional | Assess authorized selected-file/folder media browsing and playback. Record authentication/scopes/selection UX/seek/reconnect limits before implementation; do not promise whole-Drive access with device-code scopes. |
| DRIVE-02 | P1, conditional | Drive items resolve fresh authorized targets by file ID; unavailable/revoked/changed file handled honestly; no OAuth tokens in editor/backups/history URLs. |
| YT-02 / YT-03 | P1 | Rich official metadata browsing / embedded-player evaluation retained, behind launcher and phone workflows. No account-wide history or ad-free promise. |
| WEB-01 | P1 | Rare browser use: launch configured browser or isolated web route; remote escape always available. |
| BLOCK-01 / BLOCK-02 | P1 | Requested Brave handoff and separate opt-in filtering remain tracked, subject to compatibility; no inherited Shields claim. |
| DYN-01 | P0 | Configurable YouTube selected-channel/playlist cards with current permitted metadata, exact-video action, freshness/error states and no fabricated personalized history. |
| DYN-02 | P0 | Public RSS/Atom cards configurable from PC/mobile, source validation, safe plain-text display, refresh/cache/offline behavior and stable focus. |
| PC-03 | P0 | Cohesive desktop/mobile companion with Home preview, guided sources, ordering/visibility, accessible inline help and explicit Save/conflict/draft states. |
| PHONE-03 | P0 | Refined retained-video shelf: clear send/save guidance, bounded thumbnails, title/organization, replay/resume and separate storage/delete controls; real Safari evidence. |
| NAV-02 | P0 | Discoverable contextual shortcuts with visible alternatives; no gesture-only essentials or interception of system Home/volume keys. |
| AIR-01 | P1, optional | Assess existing receiver and documented replay/metadata contract; provide honest reconnect/open action when session is opaque. No required native AirPlay implementation. |
| DISC-01 / WIDGET-01 | P2 | Mood discovery/weather/ambient extras and arbitrary widget hosting remain optional; selected YouTube/RSS modules are now DYN-01/02 P0. |
| LANMEDIA-01 / PROFILES-01 / CLOUD-01 | P2 | Future PC media server, household profiles, remote sync; separate designs required. Drive is a requested media source, not generic cloud sync. |

## Success targets

These are proposed acceptance targets, not observed performance.

| Task | Target |
|---|---|
| Home to favorite | One OK after focus; no options dialog |
| First useful Home | Available before pairing/sign-in; detected apps visible |
| Return from external app | Correct app/grid focus restored via Back; system Home uses documented Home reset |
| Cold startup | Usable focus within 3 seconds in at least 18/20 device trials |
| PC layout save | Visible within 2 seconds after successful acknowledgement in five LAN trials |
| Phone sharing | After one-time setup, discover the receiving action within two navigation steps from Home |
| Reopen retained phone video | Works after phone disconnect and TV app restart; otherwise explicitly says Reconnect phone |
| Resume | Within 5 seconds of last committed position for supported finite VOD |
| Stability | Two-hour playback and repeated Home/app switching without crashes or sustained unexplained memory growth |

## Boundaries and unresolved evidence

The owner reports Sony Plus Android 14 / 2 GB; actual API/ABI, certified TV services, Home role, Cast and AirPlay capabilities are unknown. BlueStacks is useful for visual/function review, not proof of these TV integrations.

AirPlay, Cast, mirroring, link sharing and uploading a retained copy are separate routes. A default launcher does not automatically receive AirPlay or Cast. Mirroring does not create a replayable file. The preferred exact iPhone sending gesture/source app is still unknown; support route selection without blocking visual design. See [phone and Drive feasibility](PHONE-AND-DRIVE.md).

No mandatory account for core Home. Drive/Spotify/YouTube may require their own services/accounts. No always-on PC, paid cloud dependency or companion iOS app is assumed. No DRM bypass, automatic Photos import or arbitrary website stream extraction. Optional integrations do not conceal failure of required launcher behavior.

## Acceptance and ownership

First usable launcher requires all P0 scenarios on the target TV and a real iPhone. If firmware or receiver constraints block a core outcome, report it and seek an explicit product decision rather than relabel the standalone APK complete. Later requested integrations retain their gates; full scope requires delivery or owner acceptance of specific limitations.

PRD owns scope; [UX](UX-SPEC.md) owns interactions, [architecture](ARCHITECTURE.md) the design, [plan](DEVELOPMENT-PLAN.md) sequencing, [acceptance](ACCEPTANCE.md) evidence. The 0.3 [launcher prototype](LAUNCHER-PREVIEW.md) implements the grid/direct-app and initial Home setup slice. 0.4 adds the [retained-file workflow](PHONE-TRANSFER-PREVIEW.md), not AirPlay/Drive or full P0 acceptance.

## Baseline 2.2 delivery interpretation

[Dynamic Home](DYNAMIC-HOME.md) specifies calm, owner-selected freshness; [companion design](COMPANION-SPEC.md) specifies configuration and the personal media shelf. Initial module density, refresh limits, styling values and schema v3 are proposals to validate, not owner-approved pixels or shipped behavior. Follow F1-F6 in [development plan](DEVELOPMENT-PLAN.md), which replaces the old L0-L5 order. Personalized YouTube Home/watch-history/Watch Later feeds are not promised. Existing external AirPlay use is not Nook acceptance; AIR-01 is optional and does not gate P0.

## Application boundary confirmed by owner

Movie/series discovery, metadata-service credentials and streaming-provider templates are outside Nook. Independent TV apps own those experiences; Nook discovers, pins and launches them through its generic app inventory with no special coupling. Retain YouTube/RSS cards, the browser, received-file playback/history, conditional Drive and ordinary app access. Local video metadata and YouTube card metadata are still useful; this removes the separate movie database integration, not all metadata processing.

Existing legacy saved-title URLs are normalized to browser links on load/import, preserving IDs, labels, collection references and revision. New saves cannot create the retired route. The private obsolete service credential, provider preferences and generated response cache are removed on upgrade; favorites, dashboard, viewing history and received files are preserved. No transfer of data to another application is assumed.
