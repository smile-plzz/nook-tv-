# Changelog

All notable changes to Nook TV are documented in this file.

## [Unreleased]
### Fixed — F5 hardening candidate (0.4.5, version code 5)
- Reconcile F3/F4 feature-branch work in a PR targeting main; no automatic merge.
- Save source config without cache metadata; acknowledge partial failures honestly, preserve both drafts and retry uncertain source writes idempotently.
- Run six feed jobs on two workers without self-blocking waits; throttle failures, invalidate changed-source cache, and stop workers on destruction.
- Preserve feed item identity and defer focused-shelf updates; filter hidden clips from all Home rows; persist history renames.
- Bound thumbnail decode/cache memory and disk writes, serialize thumbnail deletion, and keep offline source loading independent of DNS.
- Require verified default-Home status for success feedback; keep dashboard schema v2, app ID, data files and quotas.
- Add same-key F4-to-F5 emulator upgrade, focus/privacy, scheduling, source-save and bounded-thumbnail regressions.
### Added
- **F1 Default Home Coordinator**: Supported system Home-role selection via `RoleManager.ROLE_HOME` on API 29+ (Android 10+).
- **Graceful Settings Fallbacks**: Multi-level fallback routing from `RoleManager` to `Settings.ACTION_HOME_SETTINGS` to `Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS` and general `Settings.ACTION_SETTINGS`.
- **Verified Status & Diagnostics**: Multi-signal verification checking both `RoleManager.isRoleHeld` and `PackageManager.resolveActivity` to detect confirmed default, role-only, resolved-only, or unresolved states.
- **Home Recovery & Guards**: Safe alias management guarding against disabling Nook while currently active as the system default.
- **F2 Visual Refinement**: Cohesive Apple TV-inspired aesthetic featuring a deep cinematic gradient (`#0d1320` -> `#090c14` -> `#141724`), 48dp TV safe margins, and 16dp rounded card geometry.
- **Micro-interactions & Focus**: Smooth 1.04x scale animation (140ms DecelerateInterpolator) with high-contrast 3dp white focus border and elevated depth, respecting reduced-motion preferences.
- **Dedicated Vector Glyphs**: Handcrafted vector canvas glyphs for YouTube channels/videos and RSS feeds in `LauncherTile`.
- **Proportional Media Layouts**: Normalized video and shelf media cards to 16:9 widescreen ratio (`224dp x 140dp`) with crisp typography.
- **F3 Companion & Dynamic Content**: Guided configuration of YouTube channels/playlists and public RSS/Atom feeds in paired companion editor with live feed verification on TV.
- **Dynamic Content Shelves**: Responsive TV media shelves beneath apps displaying latest videos and article updates with custom vector glyphs and stable remote focus (no focus jumps on refresh).
- **Exact Video Launch & Plain-text Detail**: One-click direct launch to exact YouTube video with zero fabricated playback progress; concise plain-text dialog with "Open article" action for RSS.
- **Feed Hardening & SSRF/XXE Protection**: Enforced public HTTPS targets, rejected loopback/LAN/private network hosts, disabled XML DOCTYPE and external entity resolution, and bounded feed body size to 2 MiB.
- **Lifecycle & Cache Management**: Atomic persistence in `SourceStore`, 30-minute throttled background refresh with 2-worker bounded pool, and clear stale/offline status labeling when disconnected.
- **F4 Personal Video Shelf & QR Setup**: Retained iPhone video transfers rendered on TV Home screen with pin priority, measured resume indicators, and collection tags.
- **Pure Java QR Generator**: Self-contained, zero-dependency QR code generator (`QrCode.java`, Versions 1–4, Byte mode, ECC L) rendering pairing URLs on TV for instant iPhone camera scanning into Safari (no tokens embedded in QR).
- **Off-Main-Thread Video Thumbnails**: Rotation-aware video thumbnail generation (`MediaMetadataRetriever` + `Matrix.postRotate` downsampled to 320x180 JPEG) with LRU caching and vector fallback.
- **Companion Media Controls**: Authenticated endpoints (`/api/media/thumbnail`, `/api/media/update`, `/api/media/reset`, `/api/media/delete`) and rich cards with pre-upload storage checks in `media.js`.
- **TV Remote Organization Dialog**: Remote context menu for saved videos offering Play / Resume, Start over, Pin / Unpin, Rename, Set collection, Hide / Show on Home, and Delete copy.

## [0.4.0-phone-preview] - 2026-10-17
### Added
- Retained video transfer via paired local browser (up to 512 MiB per file, 1 GiB quota).
- Local private playback and progress resumption using Media3 ExoPlayer.
- Storage management UI for saved video copies and unindexed cleanup.
- Installed app discovery grid and one-click launch routing.
- Initial Home setup entry enabling `HomeEntry` alias and opening system Home settings.

## [0.3.0] - 2026-10-17
### Added
- Visual launcher prototype with Leanback and touch compatibility.
- Direct app launching and favorites management.
- "From iPhone" and "Shared & Recent" top-level destinations.

## [0.2.0] - 2026-10-17
### Added
- Media3 video playback pipeline with HLS and DASH support.
- Embedded local HTTP editor server with 6-digit pairing and session authentication.
- Atomic file persistence for dashboard configuration and viewing history.

