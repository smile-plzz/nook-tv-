# Target-TV and iPhone test checklist

## Results received 17 October 2026

Brief owner TV trial: Home smooth/closer but design not approved; app launching and tried remote paths work; Home from YouTube and restart return to the existing environment. Upload and retained replay/resume were not tested. External YouTube sharing described as AirPlay worked, but receiver/protocol and Nook involvement are unverified. Exact APK/firmware and setup steps are still needed. See [feedback](TV-FEEDBACK.md).

Next focused session: (1) capture Nook version and TV model/Android version without identifiers; (2) show what Settings > Default Home setup does and whether Nook appears in system choices; (3) after selecting it, Home from YouTube, restart and restore original; (4) send one small Photos/Files video through Safari, wait for Saved, play, disconnect phone, restart Nook and replay. Do not repeat broad tests before recording these results.

For the later dynamic build: supply 2-3 preferred YouTube/RSS sources, test exact-video opening and stale/offline cards, verify refresh never moves focus, and try companion editing on phone and PC. Use A12-A16 in [acceptance](ACCEPTANCE.md).

17 October 2026 | Launcher pivot | Record observed results only

The owner reports 0.2 boots and allows light exploration in BlueStacks, but its experience misses the intended launcher. BlueStacks does not establish TV Home, remote, receiver, codecs, LAN or iPhone compatibility. 0.3 now adds the grid, direct app access and opt-in Home setup; 0.4 adds paired-browser retained-file transfer/local playback; real Safari transfer and comprehensive TV validation remain pending; Drive is unimplemented. Mark unavailable future tests Blocked, not passed.

## Before testing

Record APK/version/hash, TV actual API/ABI/display/memory/WebView, remote buttons and iPhone/iOS/source app. Keep personal identifiers, LAN details and private screenshots out of public evidence. Export dashboard before upgrade/default-home experiments. Know how to open system settings and restore the original launcher.

| Test | Action | Record |
|---|---|---|
| Install/update | Same-key upgrade, open from app list | Data preserved; package/version |
| Home appearance | View grid/artwork/header/shared rows | Owner visual review; no diagnostic/sample clutter |
| First run | No PC/key/account; find YouTube/apps and From iPhone | Useful without setup prerequisites |
| Direct launch | OK on app, Back return, hold OK and visible Edit | No launch-options detour; focus restored |
| Default Home | Select via supported system setup; Home from another app | Actual selection and task behavior |
| Root Back / Home | Repeat Back/Home, open settings and return | No exit loop, blank screen or focus trap |
| Wake/reboot | Sleep/wake/reboot/process recreation | Nook default persists or exact limitation |
| Restore original | Restore previous Home and reopen Nook normally | Recovery verified; no package disabling |
| iPhone route | Actual sender gesture and supported receiver/transfer | AirPlay/Cast/upload/link labeled correctly |
| Retained video | Send, play, disconnect phone, restart TV app, reopen | Replay exists; measured resume only |
| Live-only session | Stop sender, reopen Shared & Recent | Reconnect phone; no fake file/progress |
| Transfer failures | Cancel/network loss/full disk/unsupported codec | Partial cleanup, useful errors, no interruption |
| YouTube | App and saved video/channel/playlist handoff | Intended handler, remote return, no fake history |
| Spotify, optional | Open installed app and return | Missing optional app handled |
| Drive, conditional | Authorized existing file, play/seek/reopen/revoke | Actual grant/scopes and limitations |
| Native playback | MP4/HLS/DASH, pause/seek/tracks, several items | Independent measured progress |
| Companion | PC and Safari pair/approve/save, conflict/revoke | Responsive editor, draft recovery, no secret entry |
| Upgrade/backup | Old config migration, export/import/undo | Layout/items preserved; media not implied by backup |
| Privacy/storage | Clear history/cache separately; delete saved copy | File deletion deliberate; no accidental loss |
| Performance | 20 cold starts, two-hour session, Home switching | Timing/memory/codec/network observations |

Detailed scenarios and thresholds are in [acceptance](ACCEPTANCE.md). Phone/Drive prerequisites are in [feasibility](PHONE-AND-DRIVE.md). Put technical diagnostics under Settings > Help & diagnostics in the target design; historical 0.2 exposed some of them on Home; current previews move them into Settings.

## Evidence entry

```text
Requirement / scenario:
APK version / commit / SHA-256:
Device API / ABI / WebView:
iPhone / source app / receiver route, if relevant:
Fixture / steps:
Expected:
Actual:
Pass / Fail / Blocked:
Evidence / issue:
Recovery outcome:
```

Default Home and one real iPhone sharing/reopen workflow are core gates. Do not mark the launcher ready from emulator boot or a standalone app fallback. Browser/provider/Brave tests remain separate later scope; no provider is verified merely by opening a page.
