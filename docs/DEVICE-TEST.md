# M1 TV test — record actual results

Use the owner's TV. Do not mark a result passed until observed. The first goal is compatibility; provider integration and production acceptance follow later.

| Test | Steps | Record |
|---|---|---|
| Install | Open APK, install, launch from TV app list | Installed / error text |
| Diagnostics | Home → Diagnostics | Android/API, ABI, display, memory class, WebView, decoders; omit network identifiers |
| Remote | D-pad across Home, Apps and Settings; open/close dialogs; Back from player and web | Unreachable controls, focus lost, screen clipping |
| Favorite app | Apps → choose → Pin; return Home; reopen Nook; launch; unpin | Persistence and intended app |
| Native MP4 | Play sample, pause, seek, exit, reopen using Resume | Picture/audio, saved position, difference in seconds |
| HLS | Play HLS sample, pause, seek and exit | Picture/audio, network error or codec error |
| Web | Open controlled test, move focus, press Back; try a saved HTTPS website | Exit always works; website behavior separately |
| YouTube | Open YouTube; return to Nook | Handler used; remote operation; no fake progress |
| PC pairing | Same network; Pair PC, enter code, approve TV prompt; change welcome line and add link; save | Address reachable, approval, save acknowledgement, TV updates |
| Deny/revoke | Deny a new request; revoke paired browsers; try a saved browser write | Access rejected |
| Conflict | Pair two browsers; load same revision; save different changes sequentially | Second save reports conflict and keeps draft |
| Lifecycle | Leave Nook for another app while editor is open; return and Reload latest | Disconnected state, retained draft; no silent save |
| Restart | Exit/force-stop/reopen Nook | Dashboard persists; browser must pair again if process died |

DASH, filtering, launcher replacement, provider playback and full collections are not implemented in this preview; record them as Not implemented, not Passed.

Evidence entry:

```text
APK version / SHA-256:
TV API / ABI / WebView:
Test:
Steps:
Expected:
Actual:
Pass / Fail / Blocked:
Screenshot or error text:
```

After these checks, the full release still needs the PRD's performance, durability and two-hour playback acceptance scenarios. Report problems before labeling M1 complete.
