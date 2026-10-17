# Changelog

All notable changes to Nook TV are documented in this file.

## [Unreleased]
### Added
- **F1 Default Home Coordinator**: Supported system Home-role selection via `RoleManager.ROLE_HOME` on API 29+ (Android 10+).
- **Graceful Settings Fallbacks**: Multi-level fallback routing from `RoleManager` to `Settings.ACTION_HOME_SETTINGS` to `Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS` and general `Settings.ACTION_SETTINGS`.
- **Verified Status & Diagnostics**: Multi-signal verification checking both `RoleManager.isRoleHeld` and `PackageManager.resolveActivity` to detect confirmed default, role-only, resolved-only, or unresolved states.
- **Home Recovery & Guards**: Safe alias management guarding against disabling Nook while currently active as the system default.

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
