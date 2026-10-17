# Launcher prototype | 0.3.0-launcher-preview

17 October 2026 | L0 prototype and initial L1 Home plumbing; not target-TV acceptance

## Emulator screenshot

![Launcher prototype with installed-app artwork and phone/recent entry points](images/launcher-preview.png)

API-35 tablet emulator, captured from the running app. Installed apps and display proportions differ from the target TV. This is not a design mockup or physical-TV result.

This guide describes the historical 0.3 prototype. The [0.4 phone transfer guide](PHONE-TRANSFER-PREVIEW.md) supersedes its file-upload limitations; AirPlay and Drive remain unimplemented.

## What to review

This is the first working interface after the default-Home / Apple TV-style pivot. It uses a spacious native grid, installed-app icons/banners, quiet clock/header, rounded cards and a visible focus border. Favorites launch with one OK press. Hold OK for options, or use All Apps > Edit favorites to pin/unpin and move earlier/later.

With no pinned favorites, Home suggests up to four installed apps with YouTube/Spotify preferred when present. A YouTube website fallback appears only when there is no pinned layout and no YouTube favorite; it is not a fake installed app. Existing pinned layouts are preserved. App artwork is loaded off the UI thread into a bounded cache. Missing apps stay editable.

Home keeps From iPhone, All Apps and Shared & Recent reachable. Continue Watching and nonempty saved/collection rows retain the existing data. Old samples-section settings remain in backups for compatibility, but samples no longer render on Home. Samples, diagnostics, pairing and movie catalog access are in Settings. Favorites are always the top grid; section order applies to the remaining content rows.

Settings offers reduced motion and an optional Drive preview tile, hidden by default. The Drive screen states that account/file access is not implemented.

## Home setup and recovery

Settings > Default Home setup enables a disabled-by-default HOME alias and opens the system Home settings. Choose Nook there if the firmware permits it. Nook never disables another launcher. A normal app entry remains available.

Home intents use the existing single-task activity and reset the grid to its starting focus. Root Back stays on Home when Nook is the selected Home app or was launched through a Home intent. Ordinary app mode retains an exit confirmation. Back from other Nook screens returns to Home; return from external apps preserves the originating tile where possible.

Restore the original launcher in system Home settings before using Remove Nook from Home choices. The control refuses to remove the alias while Nook is still detected as default. Some firmware may not expose or honor the settings route; record that as blocked rather than claiming launcher acceptance. Actual-TV reboot/wake/Home/rollback remain unverified.

## Phone and media boundaries

From iPhone currently provides paired-browser saved-link instructions and access to installed receiver apps through All Apps. The existing companion can be opened in Safari; this preview does not receive AirPlay/Cast, mirror the phone, upload files or import receiver sessions. It does not automatically discover or configure an AirPlay receiver.

Shared & Recent shows existing saved links and Nook-observed playback/open history. Its cards open directly, with secondary history controls. It does not make ephemeral phone sessions durable. Real phone receiving, retained-file storage and reconnect-aware media identity remain L2 work. See [phone/Drive design](PHONE-AND-DRIVE.md).

Google Drive authentication/browsing/playback is not implemented. Spotify uses ordinary installed-app discovery/launch, not a new account client. No providers or new cloud services are enabled.

## Upgrade and validation

Same application ID, version code 3, existing dashboard schema v2. This slice does not migrate or reset user data. Launcher UI preferences are local SharedPreferences; dashboard backup still excludes them, history, credentials and media payloads. Keep the owner-preview signing key for upgrade continuity.

Local core/editor/model assertions and SDK compilation of the new tile/discovery classes pass. Hosted Gradle packaging and all 24 Android runtime smoke assertions passed; run links and code SHA are recorded in [build status](BUILD-STATUS.md). Runtime checks cover the grid, direct launch intent dispatch, phone/shared destinations, explicit Home setup and alias eligibility, alongside previous storage/player checks. A tablet emulator does not prove TV default selection or AirPlay compatibility.

Review first-screen spacing/artwork, D-pad focus, one-press app access and whether the home feels right. This build is the visual/interaction review milestone; remaining integration screens intentionally state their current limits.
