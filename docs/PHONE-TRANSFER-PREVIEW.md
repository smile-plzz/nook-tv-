# Phone video transfer | 0.4.0-phone-preview

Current 0.4 implementation guide, preserved after the TV feedback. PR #5 is merged. The owner has not yet tested upload or retained replay/resume; external YouTube sharing is not verification of this path. Proposed QR, thumbnails, titles/organization and richer browser controls are specified in [companion design](COMPANION-SPEC.md), and are not 0.4 capabilities. Start with the focused [device tests](DEVICE-TEST.md).

17 October 2026 | First retained-file path in L2; actual iPhone/owner-TV acceptance pending

## Send from iPhone

1. On TV open From iPhone > Pair a browser. Keep Nook in the foreground.
2. Open the displayed HTTP address in Safari on the same home network. Enter the code and approve the browser request on TV. No iOS app is required.
3. In Send a video, choose an MP4 or MOV from Photos or Files, then Send to TV. Confirm that a copy will be kept on this TV.
4. Keep Safari and Nook open until **Saved on TV** appears. 100% sent is not a save acknowledgement: the TV still checks/finalizes the file.
5. On TV open Shared & Recent or the Saved on this TV row. Select the video to play/resume. Your phone can disconnect once saving is confirmed.

The same companion works from a PC browser. This is binary file transfer over local HTTP, not AirPlay, Cast, screen mirroring, an iOS Share Sheet extension or an always-on receiver. Safari/Photos/Files selection and actual iPhone codecs still require device testing. Do not treat Android emulator evidence as a tested iPhone route.

## Limits and behavior

- MP4/MOV container with a readable video track. H.264 is covered by the generated Android test fixture; HEVC/HDR/rotation and other phone formats depend on the TV and remain unverified.
- Maximum 512 MiB per file, 1 GiB retained storage and 50 listed videos. Transfers reserve 128 MiB of free device space. These are conservative preview defaults, not measured maximum TV capacity.
- One media operation at a time. Full storage is an error, not a trigger to delete older videos automatically.
- Video bytes stream through a 64 KiB buffer to app-private disk; they are never base64 dashboard JSON or a whole-file RAM allocation.
- No automatic playback. A new saved copy appears on TV while ordinary playback continues undisturbed.
- Leaving Nook stops the foreground server and interrupts transfers. Interrupted/invalid partial files are removed; returning does not silently resume an upload.
- Cancel may arrive after the TV has already finalized a file. Refresh the saved list to check; a completed copy is not silently deleted.
- Uncertain retry uses the same tab-local transfer ID. A committed receipt prevents another copy for that ID/name/size. Selecting a file again generates a new ID and can deliberately create another copy. This is not content-hash deduplication.
- Receipt and file metadata persist with the retained library. If an acknowledgement is lost, refresh the list before retrying. Browser credentials and selected-file objects are never persisted to localStorage.

## Playback and storage controls

Each received file has a generated stable ID; paths and original filenames never determine disk locations. Native playback resolves the private file by ID. Measured progress uses a separate local route in the existing viewing ledger and persists independently per copy.

Open Settings > Saved video storage, or Shared & Recent > Manage saved videos. Select a listed video for Play/Resume, Start over or Delete saved copy. Deletion requires confirmation and does not affect the original on the phone. History removal/clear leaves the retained file intact; image/metadata cache clear does not remove videos.

Dashboard backup does not include video bytes, the retained-file index or viewing history. Same-key upgrades retain app data; uninstall/clear app storage removes these copies. Keep phone originals or a separate backup. Do not uninstall merely to resolve an APK signature mismatch without considering retained media.

Corrupt media indexes are preserved and block writes. Generated partials are cleaned after interruption/recreation; completed unindexed files remain counted in storage until the owner explicitly chooses Clean unindexed transfer storage. No background sweep deletes listed user videos.

## Transport and privacy

The existing LAN pairing, explicit TV approval, bearer/CSRF credentials and Host/Origin checks protect the new endpoint. Authentication happens before raw-body ingestion, with permission rechecked during transfer and before finalization. No remote filesystem path, video-download endpoint, shell or account credential input is added.

The current companion is HTTP, so pairing does not encrypt traffic. Use the intended trusted home network; the send confirmation states the transport. Media stays app-private on the TV and is not exposed as an unauthenticated web library. No Google or provider tokens pass through this route.

## Endpoint contract

| Operation | Contract |
|---|---|
| GET /api/media | Authenticated metadata/availability, usage, limits, damage/busy state; no video bytes |
| POST /api/media/upload | Same-origin authenticated application/octet-stream, explicit Content-Length, X-Nook-Filename (percent-encoded UTF-8), X-Nook-Upload-Id (32 lowercase hex) |
| Response | 200 with item only after file validation/finalization/index commit, or after matching an existing retained receipt |
| Errors | 400 invalid identity/name, 401 expired/revoked permission, 409 busy/reused identity, 413 limits, 415 unsupported container/track, 507 storage, 503 unavailable/corrupt index |

Chunked/ambiguous framing remains rejected. The dashboard JSON endpoint keeps its separate 1 MB boundary. Real Safari framing/interruption behavior is a pending compatibility gate. The index is versioned AtomicFile JSON; startup and deletion handle only generated files inside the app's received-media directory.

## Verification

82 local core/model/HTTP assertions and browser-state checks pass. Hosted Gradle packaging and 43 Android runtime assertions passed; exact run links/code SHA are recorded in [build status](BUILD-STATUS.md). Android cases cover generated-video reception, store recreation, receipt reuse, interruption, invalid file/size, revoked permission, path rejection, history/file separation, deletion and corruption preservation. Runtime also exercises the real loopback HTTP path and the production local player/progress save. This does not establish physical TV, real Safari, Wi-Fi throughput, HEVC or large-file performance.

Next: real iPhone-to-TV trial and codec/storage tuning, then the separately gated receiver and Drive work in [plan](DEVELOPMENT-PLAN.md).
