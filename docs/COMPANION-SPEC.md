> Current source (0.4.5): guided source editing/testing, QR address setup and authenticated rename/pin/collection/hide/reset/delete/thumbnail endpoints are implemented. [F5](F5-HARDENING.md) separates cache from saved source config and requires both save acknowledgements before clearing drafts. A failed source write explicitly retains its draft. Companion thumbnail display and broader tabbed redesign remain future refinements; the existence of a thumbnail endpoint is not evidence of a rendered browser thumbnail. Real Safari acceptance remains pending.

# Companion and personal media design

Baseline 2.2 | Planned improvements to the existing paired browser | PC-03, PHONE-03

## A comfortable place to customize Home

Use four primary areas: Home, Sources, Videos and Settings. Show connection/TV status and unsaved changes consistently. Desktop has a preview beside forms; phone uses a single column with a Preview switch and reachable Save/Discard actions. Preview is a layout approximation, not a screenshot or proof of TV focus behavior. Use the same card names, ordering and availability labels as the TV.

| Area | First useful controls |
|---|---|
| Home | Favorite app order/visibility, module order/visibility, background/accent, density and reduced-motion controls after schema support |
| Sources | Guided YouTube channel/playlist and RSS URL entry, validate/test result, preview items, enable/pause, refresh status and remove |
| Videos | Send video, progress/receipt, saved list, thumbnail/title/size/duration, rename and deliberate delete, Play/Resume only when supported |
| Settings | Pair/reconnect/revoke, backup/import scope, storage, accessible help and TV Home-setup status/instructions |

No drag-only operation: provide Move up/down, keyboard controls and touch targets of at least 44 CSS px. Tooltips supplement persistent labels and inline examples; help works by tap/focus, not hover only. Source validation distinguishes malformed URL, unreachable feed, unsupported format, unavailable handler and credential setup. Hide advanced options until requested. Reset appearance is separate from deleting files/history/accounts.

Maintain explicit Save and TV acknowledgement, current revision, preserved draft on conflict/disconnect and no silent write replay. Offer compare/reload or explicit reapply after reviewing new state; do not overwrite another editor. Backup/import previews clearly identify public layout/source configuration and omitted videos/history/credentials. Current v2 export remains unchanged until a tested migration ships.

## Phone workflow: send once, return later

Stage 1 validates the existing 0.4 path on real iPhone Safari: open TV address, pair with TV approval, select one MP4/MOV from Photos/Files, confirm copy, transfer, wait for saved receipt, then play from the TV. Disconnect phone, restart Nook and verify replay/resume. Existing limits remain 512 MiB/file, 1 GiB quota, 50 listed files and 128 MiB reserve until measured evidence supports changing them.

Stage 2 makes that path pleasant: QR for the local address (no bearer token embedded), readable network/setup help, storage-before-send, accurate sent/validating/saved states, optional title, local thumbnail and a clear saved-copy confirmation. Show where to find it on Home. A QR accelerates opening Safari; it is not native Share Sheet or AirPlay support.

Stage 3 adds a modest personal library: rename, pin or add to collection, mark watched/start over, filter recent/saved, storage totals and confirmed deletion. Thumbnail generation runs off main thread with bounded decoding, preserves rotation and fails gracefully. Home thumbnail visibility is owner-controlled for personal clips; no automatic Photos scan. Deleting a collection/history entry does not delete the saved file. A failed/unsupported playback does not silently delete a successfully saved copy.

Later candidates, after actual usage: sequential multi-file queue with per-file receipt/cancel, duplicate warning, optional retention reminders and an explicit authenticated Play on TV command. Never silently evict listed media or auto-interrupt a session. Resumable byte-range uploads, transcoding, remote internet access, background receiver service and native iOS extension are separate projects, not promised convenience fixes.

## Lifecycle and truthful state

The current server runs only while Nook is foregrounded. Switching to YouTube or another app can disconnect the editor/transfer. Explain this before upload and preserve the existing recoverable retry behavior. Default-Home status does not keep the service alive. The first refinement does not promise background uploading; any later service must specify user-visible state, lifecycle and cancellation before implementation.

The browser currently has metadata/upload endpoints, not a general download or remote playback API. New rename/delete/collection/play actions need explicit authenticated contracts, revision/idempotency semantics, confirmation where destructive and failure tests. Do not imply these proposed browser controls exist in 0.4. Browser previews must not expose private thumbnails or files without authorization; a thumbnail endpoint would need session checks and bounded output.

## Optional receiver integration

The owner reports external YouTube sharing worked and explicitly makes AirPlay replay optional. First identify the TV receiver and sending gesture. If a documented integration supplies a durable media reference, show Open again; only show Resume if actual position and replay access are available. Otherwise provide an optional receiver shortcut and an honest Start sharing again instruction. No interception, recording or extraction of someone else's session is assumed. Drive remains a later conditional source under [phone/Drive feasibility](PHONE-AND-DRIVE.md).

Acceptance: A13 companion usability, A14 saved-video experience and A15 optional receiver capability. The first real transfer test is more valuable than adding many options before proving the route.

