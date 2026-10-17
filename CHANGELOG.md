# Changelog

All notable changes to Nook TV are documented in this file.

## [Unreleased]
### Added
- **F1 Default Home Coordinator**: Supported system Home-role selection via `RoleManager.ROLE_HOME` on API 29+ (Android 10+).
- **Graceful Settings Fallbacks**: Multi-level fallback routing from `RoleManager` to `Settings.ACTION_HOME_SETTINGS` to `Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS` and general `Settings.ACTION_SETTINGS`.
- **Verified Status & Diagnostics**: Multi-signal verification checking both `RoleManager.isRoleHeld` and `PackageManager.resolveActivity` to detect confirmed default, role-only, resolved-only, or unresolved states.
- **Home Recovery & Guards**: Safe alias management guarding against disabling Nook while currently active as the system default.
- **F2 Visual Refinement**: Cohesive Apple TV-inspired aesthetic featuring a deep cinematic gradient (`#0d1320` -> `#090c14` -> `#141724`), 48dp TV safe margins, and 16dp rounded card geometry.
- **Micro-interactions & Focus**: Smooth 1.04x scale animation (140ms DecelerateInterpolator) with high-contrast 3dp white focus border and elevated depth, respecting reduced-motion preferences.
- **Dedicated Vector Glyphs**: Handcrafted vector canvas glyphs for YouTube channels/videos and RSS feeds in `LauncherTile`.
- **Proportional Media Layouts**: Normalized video and shelf media cards to 16:9 widescreen ratio (`224dp x 140dp`) with crisp typography.

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
