# F5 hardening — 0.4.5 candidate

17 October 2026 | version code 5 | Dashboard schema v2 unchanged

## Reconciled baseline

PR #14 merged into main at `e3b6159` on 17 October 2026 with explicit owner approval. F3/F4 consolidation and F5 repairs are included. Automated verification passed; owner TV/iPhone acceptance remains pending. Next action: [owner test session](DEVICE-TEST.md#045-owner-test-session).

## Correctness fixes

- Source GET responses include cache/status metadata; the companion now projects only schemaVersion/revision/sources for persistence. Both layout and source acknowledgements are required before claiming complete save or clearing drafts.
- Partial failure explicitly reports that layout saved but sources did not. Source drafts survive, restoring a browser draft restores both documents, and uncertain source retries keep the same mutation fingerprint. Edits made while a request is pending remain unsaved drafts.
- Fetches use two actual workers without a coordinator waiting inside their pool. Batches reject overlap; normal refresh is 30 minutes, failures back off for a minute, forced refresh has a 10-second floor. Manager disposal stops workers/callbacks. Cache identity includes source URL/type; removed/changed sources cannot reuse unrelated results.
- Shelf tags use feed item identity rather than array position. An update is deferred while focus remains in a source shelf and applied when focus leaves, preserving the selected target. No automatic timer or autoplay is introduced.
- Hidden retained clips are filtered from Saved, Continue Watching and Recently Opened on Home. Shared/storage remain deliberate management destinations. Deleted local references do not remain Home history cards.
- Renaming writes into the viewing ledger itself rather than modifying a detached JSON copy.
- Home-role feedback requires verified default status; RESULT_OK alone does not claim success.

## Memory, storage and offline behavior

Each artwork/thumbnail LRU is capped at min(4 MiB, heap/32), with a 256 KiB floor; trim-memory evicts cached references without recycling bitmaps still owned by displayed views. Two caches therefore have an 8 MiB absolute ceiling.

API 27+ uses bounded MediaMetadataRetriever extraction (320 × 180 bounding box); API 26 uses vector fallback. It never falls back to full-resolution frame extraction. JPEG writes use a temporary file with failed-write cleanup. Thumbnail reads/extraction and deletion serialize to prevent recreating a deleted thumbnail. Extraction is lazy, outside upload acknowledgement, and preserves at least 128 MiB plus write headroom. Thumbnail bytes count toward quota, not unindexed media.

Saved hostname configurations validate structurally without network access, so offline startup does not mark a valid source store corrupt. Fetch-time destination checks inspect all resolved addresses, reject private/local/multicast/ULA/shared-network targets, and repeat on redirects. This is not a pinned-DNS transport; authenticated/private feeds remain out of scope.

Feed XML is bounded to 2 MiB and UTF-8, rejects DOCTYPE before parsing and rejects external entity resolution even on parsers lacking optional features. Unsafe article URLs are omitted. Disposable cache files are bounded to 1 MiB/six sources/20 items and skipped when reserve space is unavailable.

## Verification

Run both PowerShell suites, Gradle assembleDebug/assembleAndroidTest, and Android runtime smoke. Focused regressions cover partial source failure/retry/draft recovery, six-job scheduling, offline config, stable identity, hidden-history filtering and ledger rename. Android tests add deferred focus refresh and bounded thumbnail checks.

The emulator workflow seeds the F4 APK with dashboard/favorites, sources, media bytes/organization/thumbnail, history and appearance settings, updates to F5 with the same runner signing key, then verifies preservation before smoke tests. This proves fixture continuity in the emulator, not owner signing-key possession or physical-device compatibility.

Verified at `16118d7`: 654 local Java assertions, both browser-state suites, hosted APK/test build, 64 Android storage/activity assertions and eight same-key upgrade checks. See [BUILD-STATUS](BUILD-STATUS.md) for exact CI links. The focus fixture waits for layout and uses actual D-pad input before asserting focus; it does not treat a rejected offscreen request as a refresh failure.

Target-TV performance, real Safari, portrait/HEVC/HDR, Home/reboot and original-launcher recovery remain pending. F6 remains conditional; no receiver/Drive implementation is added.
