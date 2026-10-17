# Implementation handoff — F5 hardening candidate

17 October 2026. Read [F5-HARDENING](F5-HARDENING.md), [BUILD-STATUS](BUILD-STATUS.md), [PRD](PRD.md), and [DEVICE-TEST](DEVICE-TEST.md) first.

## Source reconciliation

PR #14 (`fix/f5-launcher-hardening`) merged into main with explicit owner approval on 17 October 2026 at `e3b6159`. Final verification documentation is included. The owner will test and return feedback; continue from current main and reproduce reported issues before changing behavior. Never merge further PRs without explicit owner instruction.

## Current candidate

Version `0.4.5-f5-hardening`, version code 5. F1 Home coordinator, F2 visual refinement, F3 selected-source companion/shelves and F4 saved-video organization are implemented in this candidate. F5 fixes source configuration/cache separation, honest partial-save results and draft/retry preservation, non-blocking two-worker refresh, identity-based focus with deferred shelf updates, hidden-media filtering in Home history, real ledger rename, bounded thumbnail decoding/cache budgets, safe offline source loading, and verified Home-result feedback.

See build status for executed validation. Do not treat compile success or tablet emulator results as TV/iPhone acceptance. F5 is not fully accepted until actual-device gates pass.

## Next work after owner testing

1. Record installed version/signing certificate and back up dashboard.
2. On owner's TV, select Nook as Home; test Home from YouTube, wake/reboot and restoration of the original launcher.
3. In real Safari, pair through QR, save/test a chosen YouTube and RSS source, verify source-save errors retain drafts.
4. Transfer a small Photos/Files MP4/MOV, wait for receipt, disconnect phone, restart and replay/resume. Test portrait/HEVC/HDR separately.
5. Measure 20 cold starts and two-hour populated-Home/playback stability on the 2 GB device.
6. Investigate the existing receiver package/protocol and conditional Drive only after these results. No native AirPlay/Cast/Drive capability is promised.

## Constraints

Keep Java 8/native Views, Media3 1.11.1, Gradle 8.13, AGP 8.13.2, compile 36/target 35/min 26, app ID `com.ismail.nooktv.compat`, dashboard schema v2 and same-key upgrades. Sources remain independently versioned in `sources-v1.json`; received metadata in `received-media-v1.json`; history in `viewing-v1.json`. Backup remains dashboard-only.

Foreground HTTP port 8765, paired approval/auth/CSRF, 512 MiB/file, 1 GiB quota, 50 videos, 128 MiB reserve remain. Keep independent streaming applications generic. Do not disable system packages, fabricate external progress, silently import Photos, or transmit account secrets through LAN HTTP.

Update dev-log, changelog, build status and changed capability docs per slice. A practical Vim tip for this codebase: `/saveSources`, then `n`, finds source persistence call sites.

