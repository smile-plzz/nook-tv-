# TV and browser experience specification

Baseline 1.0 — design intent, no UI implemented.

## Experience principles

Nook should be personal, calm and quick on the owner's 2 GB TV. Content comes before decorative widgets. Prefer readable type, restrained motion, clear focus and a small number of useful rows. Backgrounds may be personal, but text contrast must remain stable. No autoplay video or large animated background.

English UI initially, while preserving Unicode titles, including Bangla, without broken rendering. Support long titles without letting them push controls off screen. Add a display-safe-margin control if device testing reveals overscan.

## Screens and states

| Screen | Main purpose | Required exit/return |
|---|---|---|
| Welcome | Explain standalone use; open Apps or pair PC | Skip pairing and enter useful dashboard |
| Home | Personal rows and resume entries | Back to top, then explicit exit choice in standalone mode |
| Watch | NowShowing search, curated picks, movie/series detail | Return to same row/card |
| YouTube | Pinned videos, channels, playlists | Return from external handler without inventing progress |
| Apps | Launchable apps and favorites | Preserve selected app |
| Saved | Watchlist, Play Later, collections, Recently opened | Manage/remove with clear feedback |
| Player | Native playback or source-specific web route | Back restores title selection and prior screen |
| Settings | Appearance, data, pairing, sources and optional launcher | Return focus to opening control |
| Browser editor | Customize, organize and save | Dirty-state warning and connection status |

## First run

1. Nook opens as an ordinary app.
2. Show two clear actions: Explore apps and Customize from PC.
3. Apps can be browsed and pinned immediately; metadata credentials are optional at this stage.
4. Home shows favorites and prompts to add a collection, with no fake viewing history.
5. Pairing can be started later from Settings.
6. Offer launcher setup only after standalone use has been validated; it is not part of mandatory onboarding.

## Remote navigation

- D-pad moves predictably between navigation, rows and cards.
- OK activates the focused item; all important actions have visible controls.
- Back dismisses the innermost dialog/player overlay before leaving a screen.
- Do not intercept the remote's system Home behavior in standalone mode.
- Long press may provide shortcuts but cannot be the only way to edit/remove.
- Restore focus to the exact originating tile when it still exists; otherwise nearest surviving item.
- Removing the final tile focuses the add action or section header.
- An embedded player must not trap the owner without a remote-accessible way to return to Nook.
- A source that requires a mouse is not listed as fully remote-supported.
- Apply row changes during playback without interrupting video; on Home, preserve focused item where possible.
- Use light motion and respect reduced-motion settings where available.

## Continue Watching and Recently opened

A resume card displays title, episode if applicable, and genuine measured progress. OK offers Resume by default; secondary actions include Start over, Mark watched and Remove.

A recent-open card shows title/episode and Open again. It never shows a made-up percentage. Explicit playback request may create a launch record; opening a detail page does not. Failed playback attempts are not marked watched.

If a source supports timestamp observation but not seeking, show recorded progress as informational and label the action Open again rather than Resume. If duration is unknown, omit the percentage. Ended or manually watched items leave Continue Watching but remain available in history if enabled.

Example:
- Native movie at 31:20: Resume 31:20.
- Opaque web series, S2 E3 selected: Open S2 E3; progress unavailable.
- YouTube handed off externally: Open in YouTube; no Nook progress bar.

## NowShowing browsing

Search and details remain native. Movie details show metadata and Play/Save actions. Series details preserve selected season and episode. Source selection has a visible status with a useful fallback.

Do not auto-cycle providers indefinitely. Offer user-controlled retry/switch, preserve episode selection, and avoid opening multiple players at once. Curated seed rows are labeled Picks, not live Trending. Metadata availability does not imply streaming availability.

## Personal collections

The owner creates names and chooses items: movies, videos, playlists, sites and app shortcuts. Optional suggested labels such as Watch tonight, Music or Learn are examples, not preselected assumptions about the owner.

Collections can be reordered or hidden. Hiding a row retains its contents. Removing a collection requires clear confirmation about whether contained items are merely ungrouped; v1 removes membership, not independent watchlist/history entries. Removing an item from one collection does not remove it elsewhere.

## Browser editor

Sections: Dashboard, Collections, Saved links, Appearance, Sources, Paired device information and Backup.

- Layout preview is approximate, labeled Preview; it is not a live screenshot.
- Draft changes are distinct from saved TV state.
- Save validates and returns confirmation with revision.
- Failed saves preserve the draft and explain the retry.
- A stale revision presents Reload latest and a way to reapply the draft; no silent overwrite.
- Add link recognizes supported YouTube links and valid HTTPS websites/direct media targets; owner can edit label.
- Saving a link never starts playback without a separate explicit future Play Now action.
- Disable provider-password/API-key input in the initial HTTP editor; show TV settings guidance.
- Configured app favorites use the TV's reported app inventory, not the PC browser's installed apps.

Connection states: Connected, Saving, Saved, Conflict, Disconnected, Pairing expired, Access revoked.

When Nook leaves the foreground, show that the TV app needs to be reopened. Retain browser drafts, but re-read current TV revision before saving. An address change gets reconnect instructions, not an endless spinner.

## Data actions

Separate buttons: Clear image cache, Clear viewing history, Remove paired browsers, Export backup, Import backup. Cache clearing must not imply a library reset. Imports show what will be replaced and require confirmation. Export includes optional history only when selected.

History paused means no new playback/launch history is retained after the choice takes effect; already saved entries remain until cleared. Explicit watchlist and collection saves continue working.

## Error copy examples

- Can't reach the TV. Open Nook TV and check that both devices are on the same network.
- This source has not confirmed playback. Retry, choose another source, or open it in a browser.
- Progress is unavailable for this player. You can reopen the same episode.
- This app is no longer installed. Choose another app or remove the shortcut.
- Your dashboard changed elsewhere. Reload the latest version before saving.
- Offline. Your saved layout is available; online videos need a connection.

Technical diagnostics belong in Settings, not routine browsing.
