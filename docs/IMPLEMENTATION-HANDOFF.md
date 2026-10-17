# Implementation handoff after TV feedback

Baseline 2.1 | 17 October 2026

## Build the next useful experience

Nook is a default TV Home with Apple TV-inspired clarity, a stable favorite-app grid, a few configurable fresh-content cards, and convenient return to personal videos. The owner tested briefly on TV: apps and tried remote paths worked, appearance was only partially satisfactory, and Home/restart returned to the existing environment. File transfer/resume were not tested. Do not reset this evidence to "TV testing pending" or inflate it to acceptance.

Main includes merged PR #5 at 247671613c9552fa9f67eda179a67528a680ddcf. Latest delivered APK is 0.4.0-phone-preview. The current task updates documentation only; no role prompt, dynamic sources or new companion controls are delivered by it.

## Read order

1. [Feedback and questions](TV-FEEDBACK.md), [PRD](PRD.md), [decisions](DECISIONS.md).
2. [UX](UX-SPEC.md), [dynamic Home](DYNAMIC-HOME.md), [companion/media design](COMPANION-SPEC.md).
3. [Development sequence F1-F6](DEVELOPMENT-PLAN.md), [architecture](ARCHITECTURE.md).
4. [Acceptance](ACCEPTANCE.md), [device checklist](DEVICE-TEST.md), [build status](BUILD-STATUS.md).
5. [Current transfer contract](PHONE-TRANSFER-PREVIEW.md), [phone/Drive feasibility](PHONE-AND-DRIVE.md).

## Next implementation slices

Start F1: capability-aware Home-role prompt, settings fallback, verified result/retry and recovery. Current MainActivity only enables the alias and opens Home settings. Test cancellation, missing role/activity, stale default status and process recreation; only the owner's hardware can close the Home/reboot gate. Collect exact model/firmware and setup screen behavior.

F2: reviewable coherent visual refinement with empty/populated/offline/focus states. Keep one-OK app launch. F3: pair the content-source model with a usable phone/desktop editor; start chosen YouTube channels/playlists and public RSS/Atom, not arbitrary widgets or scraped personal recommendations. Refresh must not change the focused target. Verify credential provisioning before claiming automatic YouTube updates.

Test the existing Safari video route early; F4 improves guidance, QR address, thumbnails, titles, organization and storage based on that result. Keep AirPlay integration optional and separate from retained copies. F5 completes actual-device quality gates; Drive and broader integrations follow F6.

## Constraints and evidence

Retain Java/native Views, AtomicFile, Media3 and pairing/revision contracts; refactor responsibilities instead of rewriting the stack. Keep com.ismail.nooktv.compat and owner signing continuity; never commit keys. Gradle 8.13 / AGP 8.13.2 / compile 36 / target 35 / min 26 remain. Current schema v2 stays unchanged until an implemented, tested migration. Hosted CI can resolve dependencies when local execution cannot.

Preserve 82 local assertions and browser state checks, plus 43 Android smoke assertions. None proves the new design or target-TV default behavior. No system-package disabling, no automatic account/Photos import, no provider credentials through LAN HTTP, no fake external progress. Foreground-only editor/transfer lifecycle remains unless deliberately redesigned. Dashboard backup excludes retained files and history.

On each slice update capability status, tests, user guide and release evidence. A working APK or green CI does not close P0. Historical M1/0.3 and NowShowing audit evidence remain historical; their old task sequence does not govern this work.
