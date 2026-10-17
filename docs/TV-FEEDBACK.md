# TV feedback and product response

Baseline 2.1 | 17 October 2026 | Owner feedback from a few minutes on a physical TV

## Evidence, without overstating the test

The owner reports a smooth, usable starting point and an Apple-like direction, but the experience is not yet elegant, minimal or useful enough. Installed apps open correctly, and remote navigation worked in the paths tried. Home from YouTube returns to the existing TV environment (described as the default browser); restart also returns to the default. The exact destination package, APK version, firmware and whether the existing Home setup was attempted are not yet recorded. This is a failed observed default-Home outcome, not proof of a specific firmware defect.

iPhone file upload, retention and Nook resume were explicitly not tested. A YouTube video sent from iPhone using what the owner calls AirPlay worked, probably through the TV's existing environment. Receiver/protocol and Nook involvement are unverified; this is not evidence that Nook implements AirPlay or imports playback history. The admin panel needs substantial refinement; no particular transfer or save failure was reported.

| Feedback | Product interpretation | Response / gate |
|---|---|---|
| Closer to Apple style, smooth but only okay | Foundation works; visual acceptance has not passed | Cohesive spacing, typography, artwork and focus; owner reviews actual TV layout |
| Wants YouTube video cards and configurable RSS | Useful freshness is core to the next iteration | Small optional content shelf, direct actions, owner-selected sources; DYN-01/02 |
| Remote paths tried work | Preserve working controls; do not redesign inputs blindly | Regression checks plus discoverable shortcuts; NAV-02 |
| Home and reboot return to existing environment | Default-home goal remains unmet | Highest priority: role/settings flow, verified status and recovery; LAUNCH-01 |
| Apps launch correctly | Keep one-OK app routing | Partial owner confirmation, not exhaustive handler testing |
| Admin needs tools, options and guidance | Companion should be the comfortable place to configure Home | Preview, guided source forms, clear save state, mobile controls; PC-03 |
| Saved iPhone videos seem useful, not yet tested | Develop a deliberate personal video shelf | First verify the existing route, then thumbnails, naming, organization and storage; PHONE-03 |
| AirPlay resume would be nice; optional | Do not block the launcher on external session integration | Capability study only; AIR-01 conditional |

The phrase about refining Home with no widgets is interpreted in context as a complaint about the current lack of widgets: the owner immediately requests YouTube and RSS widgets. The proposed response is a few useful cards with hide/reorder controls, not an always-busy dashboard. Exact density remains a design choice to review.

## Intent and tradeoffs

Nook should reduce the effort between sitting down and doing something useful: open an app, play a chosen video, glance at an update, or return to a personal clip. Dynamic content should increase usefulness without moving focus, playing unsolicited audio or demanding accounts. The app grid remains the stable anchor. Feed cards are Nook-rendered modules, not a promise to host arbitrary Android widgets or third-party scripts.

Phone upload has a distinct benefit from AirPlay: a selected copy remains available when the phone disconnects. It is most useful for family clips and videos the owner wants to watch again. It costs TV storage and requires an explicit send, so it should complement existing sharing rather than attempt to replace every sharing gesture. Nook is not a backup for the phone library.

## Questions that improve implementation

1. What is the exact TV model/Android version and Nook version? In Settings > Default Home setup, what happens after enabling Nook and opening system settings? A photo of that screen is useful; omit serial numbers and network details.
2. Which two or three YouTube channels/playlists and RSS feeds would you actually want? Start with owner-selected feeds; personalized YouTube recommendations are not assumed.
3. Are phone videos mostly Photos clips or Files/downloads, roughly how long/large, and should they normally stay on TV? This informs thumbnails, queue and storage defaults.

These answers do not block documentation or the initial layout/system-flow work. Before claiming target-TV Home success, or personal media compatibility, collect the relevant answer and test evidence. See [development plan](DEVELOPMENT-PLAN.md), [dynamic Home](DYNAMIC-HOME.md) and [companion design](COMPANION-SPEC.md).
