# Launcher architecture and implementation boundaries

Baseline 2.1 | 17 October 2026 | Current foundation plus proposed launcher design

## Current implementation versus target

Delivered 0.2 uses one Java/native-Views Android app, Media3 1.11.1, AtomicFile JSON dashboard/viewing stores, native OMDb client, and a foreground paired HTTP editor. It has no default-Home flow, iPhone receiver/file transfer or Drive integration. That describes 0.2. The 0.3 [prototype](LAUNCHER-PREVIEW.md) adds LauncherTile/LauncherApps, native grid/direct launching and an opt-in HOME alias. 0.4 adds ReceivedMediaStore, authenticated binary upload and stable local playback; see [transfer guide](PHONE-TRANSFER-PREVIEW.md). Remaining receiver/Drive boundaries below are design targets.

Keep working persistence, migrations, validation, pairing and playback. Do not rewrite in Kotlin/Compose or migrate to Room solely for appearance. The earlier Room/Compose proposal is superseded by retaining the current stack unless evidence justifies a separate change. Refactor MainActivity into clear responsibilities as needed for the launcher; module proliferation is not a goal.

## Responsibilities

| Component | Target responsibility |
|---|---|
| Launcher shell | Default-home entry, app grid, artwork, focus/navigation, header/dock and root Back behavior |
| App inventory/router | TV/sideloaded launchable activities, artwork, installed state, one-press launch, optional YouTube/Spotify/browser |
| Home model | Favorites/grid order, optional rows, collections, availability and stable focus IDs |
| Shared media repository | Received file/source identity, origin, replay capability, availability and storage ownership |
| Receive coordinator | Supported phone routes, transfer/receiver UI, lifecycle and cancellation |
| Player | One Media3 player, tracks, progress observations and resume |
| Viewing store | Separate measured playback, opaque opens and expired phone sessions |
| Drive adapter, conditional | Scoped authorization, file discovery/selection, fresh resolution, revocation and seek behavior |
| Editor | Foreground pairing, revision-safe layout mutations, portable dashboard backup |
| Settings/diagnostics | Home setup/recovery, sources/accounts, data controls and compatibility tools |
| Catalog/web adapters | Optional NowShowing/YouTube discovery and source-specific web routes; lower priority |

## Android Home contract

Plan a HOME-capable entry/alias alongside ordinary app/TV app-list launch. Inspect supported role/default-home settings and request via system UI where available. See [RoleManager](https://developer.android.com/reference/android/app/role/RoleManager#ROLE_HOME). API support and TV vendor behavior must be checked; never assume ROLE_HOME is universally requestable on TVs or BlueStacks.

The owner has chosen Nook as default Home. Do not auto-disable system packages, require root/device-owner privileges, or introduce accessibility interception merely to force selection. Record previous default and provide restore/open-settings instructions. Standalone entry remains recovery, not satisfaction of the core launcher requirement.

Treat root Home as persistent launcher surface: Back does not finish into a blank screen. Home reentry starts at predictable first-favorite focus; Back return from external activities restores originating focus/scroll. Test cold/reused tasks, wake/reboot, process death and system-settings return. Launcher status does not bypass Android service restrictions.

## Presentation and performance

Use recyclable/lazy grid/rows, asynchronous resized app artwork/thumbnails, explicit fallback icons and a lightweight focus treatment. Load installed favorites before network content. Avoid repeated full Home rebuilds on every focus event or receiver result. Preserve stable IDs during editor updates.

No background autoplay or full-screen live blur. Bound image disk/memory caches and Media3 buffers; release inactive WebViews/players. One active playback context. Profile actual 2 GB device before selecting final layout and cache/buffer limits.

## Durable data and migration

Current dashboard schema v2 has title/accent/background, ordered visible sections, apps, links and collections. Current limits: 200 links, 80 apps, 20 collections, 400 references. Viewing state is separate, bounded to 100 entries. Dashboard mutation receipts are bounded to 128 fingerprints.

Design new launcher fields and media records through explicit versioned migration; do not call a new schema implemented yet. Preserve existing favorites, backgrounds, links, collection references and per-item progress. Map old sections to optional new rows; test disabled sections and empty states. Unknown schema/corrupt files stay preserved; do not replace user data with defaults.

Proposed additions:
- Launcher preferences: grid order/style, first-focus policy and optional destination visibility.
- Source identity: opaque stable ID, origin (retained phone file, saved URL, app, Drive, session), availability and replay capability.
- Media record: title, safe local storage ID or canonical remote reference, MIME/size, fingerprint/version, thumbnail and timestamps.
- Playback record: position/duration only when observed, observation provenance and compatible content identity.
- Session record: connection lifetime, external handler and reconnect action; no fictional replay target.
- Drive account reference separate from private credentials.

Retained media is durable data, not an LRU cache. Separate history removal, collection removal, thumbnail cache clear and media deletion. No signed URL/token is canonical identity. Different versions/cuts/files must not share progress merely because titles match.

## Phone receiving and storage

See [feasibility](PHONE-AND-DRIVE.md) for protocol distinctions. Prefer a first self-contained retained-file prototype while assessing actual AirPlay/Cast/receiver options; this engineering proposal must not silently redefine system AirPlay.

0.4 adds GET /api/media and POST /api/media/upload: authenticated bounded streaming to app-private temporary files, atomic finalization, free-space checks, content validation, cancellation and cleanup. Keep requests separate from 1 MiB config JSON. Never ingest full videos into RAM or accept arbitrary paths. Received videos never auto-play.

Receive UI is foreground by default; if transfer must survive an external receiver/app handoff, design a compliant service/notification and security lifecycle explicitly. Default Home does not mean permanently foreground. Mirroring does not persist a recording. External receivers expose progress only through verified documented contracts.

## Drive source adapter

Not implemented. Resolve stable file IDs/resource keys with authorized HTTPS requests. Confirm selected-file access and OAuth scope/client flow before building a folder browser. Limited-input device flow is not whole-account Drive authorization.

Keep tokens in a private credential store with Keystore-backed protection, separate from dashboard/history. Refresh tokens without logging them; support disconnect/revoke. Re-resolve authorized range-capable media requests for playback/seek. Validate origin on redirects; never forward Google authorization to arbitrary URLs or players. No credential proxy through LAN HTTP. Bound discovery/cache work and handle permission loss, changed files and quota. Official references and gating decisions are in [PHONE-AND-DRIVE](PHONE-AND-DRIVE.md).

## Editor and backup contract

TV remains authoritative. Same-LAN code plus explicit TV approval, authenticated reads/writes, Host/Origin validation, CSRF defense, rate limits and revocable in-memory sessions remain. Mutations include expected revision and a unique fingerprint; the atomic envelope persists configuration and bounded receipts together. Retry verifies fingerprint and returns current state; no unlimited journal guarantee.

Stop listener/clients on leaving foreground. Browser preserves drafts, displays disconnected and re-reads revision before an explicit retry. No automatic mutation replay. A default launcher returning often does not justify a hidden background server.

Current dashboard backup is schema-v1/v2 JSON only: layout/links/apps/collections/background. It excludes viewing history, source templates, episode selections, credentials and media bytes. Import previews/replaces after confirmation and keeps a pre-import recovery snapshot. Any new export schema must explicitly state included references, absent local media and account reconnection; it must not falsely imply copying phone video payloads.

HTTP is not encrypted; pairing does not provide on-path confidentiality. Do not provision provider or Google secrets through it. Evaluate protected transport before a credential-bearing extension. Background images remain bounded JPEG derivatives decoded off main thread.

## Playback, history and external sources

Native player saves about every 5 seconds and on pause/seek/stop where allowed. Force-stop can lose uncommitted progress. Default finite-VOD watched threshold is 95%; unknown/live duration has no percentage.

Route contract: nativeDirect, retainedLocal, Drive-authorized, webEmbed, externalApp/browser or phoneSession. Declare canReplay, canObservePosition, canSeek and whether a sender/account/receiver is required. Handoff or iframe load is not PlayingConfirmed. TimedOut is inconclusive. Validate any web event's exact origin/session/media identity; no privileged bridge or cross-origin scraping.

NowShowing remains selected upstream behavior reference. No automatic provider enablement or copied code/assets while license is unresolved. YouTube/Spotify playback remains with actual eligible handlers initially; richer integration requires its own tested contract.

## Isolation, privacy and distribution

One isolated WebView with no editor privileges, unexpected scheme/popup/download blocking and a remote-accessible exit. Brave Shields is not inherited; filtering remains a later independent engine decision.

No silent account/history/Photos import. Shared media visibility is owner-controlled; exports omit private credentials. Logs redact addresses/tokens/session URLs and device identifiers. No default telemetry or new cloud service.

Keep app ID com.ismail.nooktv.compat and signing continuity unless a separately justified migration is approved. Build remains pinned Gradle 8.13 / AGP 8.13.2 / SDK 36, target 35, min 26. Same-key upgrades must preserve user state. CI is evidence of build/contracts; real TV and iPhone decide integration acceptance.

## Baseline 2.1 implementation boundaries

Introduce HomeSetupCoordinator for alias eligibility, role availability/request result, settings fallback and status recheck on return. Android API 29+ role methods require guards; availability and actual default resolution are separate from enabling the alias. A generic chooser cannot promise persistent default selection. Record unresolved/unsupported states, preserve normal app entry and safe rollback. See F1 in [plan](DEVELOPMENT-PLAN.md).

Add a SourceRepository and small provider adapters (YouTube selected source, public RSS/Atom, retained local media). Keep stable source/item IDs, explicit target action/capabilities, last successful fetch, bounded cache and error state. Home observes snapshots and applies updates without stealing focus. Network/parse/image work stays off the UI thread; no hidden polling service in the first version. Validate remote destinations at every redirect/resolution and disable unsafe XML features. Concrete limits and source decisions: [dynamic Home](DYNAMIC-HOME.md).

Proposed dashboard v3 stores module/source configuration and portable appearance preferences. Migrate v1/v2 with atomic recovery and stable IDs; preserve existing local appearance settings on first migration. Keep cached content/status, secrets, received files/index and measured viewing data outside exported configuration. Never persist signed URLs as identity. Schema limits and validation must be implemented on TV and companion together; old backups remain importable, future backups are rejected safely by older code.

Companion areas share the Home model, accessible reordering and explicit revision-safe saves. QR contains only the pairing address, not a credential. New browser media metadata/mutation/thumbnail/play endpoints are proposed, not present: define authorization, privacy, bounded responses, idempotency and destructive confirmations before adding them. Existing server foreground lifecycle remains. See [companion](COMPANION-SPEC.md).
