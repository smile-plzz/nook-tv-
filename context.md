# Nook TV — Project Context

## Product Goals
- **Default TV Home**: Nook TV serves as the primary default launcher on Android TV with modern, minimal, premium Apple TV-inspired aesthetics.
- **Fast Access**: Quick, direct one-click launch for YouTube and installed applications.
- **Dynamic Content**: Configurable YouTube channel/playlist cards and RSS feeds without background bloat or focus jumps.
- **Personal Video Shelf**: Retained video transfers from iPhone (MP4/MOV) stored locally on TV, with replay, resume, and storage quotas.
- **Companion Experience**: Responsive mobile/PC browser editor (port 8765) for Home layout, sources, and video management.
- **Independent Ecosystem**: NowShowing is completely separate and treated as an independent native TV APK. Nook pins and launches it like any standard app.

## Technology Stack
- **Language & Runtime**: Java 8 source/target compatibility, native Android Views.
- **Media Engine**: AndroidX Media3 (ExoPlayer 1.11.1, HLS, DASH, UI).
- **Target Platform**: Android TV / Leanback (minSdkVersion 26, targetSdkVersion 35, compileSdkVersion 36).
- **Build System**: Gradle 8.13, Android Gradle Plugin 8.13.2.
- **Persistence**: `android.util.AtomicFile` for transactional JSON persistence (`DashboardStore`, `ViewingStore`, `ReceivedMediaStore`).
- **Companion / Transfer**: Lightweight embedded HTTP server (`EditorServer`), pairing handshake (`Pairing`), and CSRF protection.

## Architectural Constraints
- **Application ID**: `com.ismail.nooktv.compat`.
- **Dashboard Schema**: Schema version 2. Atomic migrations required for any format expansion.
- **Signing & Data Continuity**: Preserve signing key continuity and existing user data. Never commit keystores or private keys.
- **Home Role & Recovery**: Use `RoleManager.ROLE_HOME` on API 29+ where available; fall back gracefully to `ACTION_HOME_SETTINGS` and general system settings. Never disable system packages, intercept keys, or force launcher replacement. Retain standard launcher entry for recovery.
- **Companion Quotas**: Foreground-only HTTP server. Video uploads capped at 512 MiB/file, 1 GiB retained quota, 50 files maximum, 128 MiB free-space reserve.
