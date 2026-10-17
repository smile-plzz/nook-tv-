# Launcher and companion experience

TV feedback partially validates the direction, not the finished design. Next: coherent premium/minimal styling and a small configurable content shelf; see [dynamic Home](DYNAMIC-HOME.md) for card states/tokens and [companion design](COMPANION-SPEC.md) for browser/mobile flows. All new behavior below is planned unless recorded in [build status](BUILD-STATUS.md).

Baseline 2.2 | 17 October 2026 | Proposed implementation of the confirmed Apple TV-style default-home direction

0.4 implements the paired-browser retained-file path and local playback/storage controls; see [current transfer guide](PHONE-TRANSFER-PREVIEW.md). AirPlay, QR setup and Drive account access are not delivered by this slice. The design below still includes planned behavior.

## Design intent

A calm, recognizable home to use from the sofa. Use an app grid as the visual anchor, generous margins, rounded rectangular banners, legible type and a clear focused tile. Restrained enlargement/shadow and short transitions convey focus; reduced motion removes movement. Wallpaper supports the content rather than dominating it. Avoid dense toolbar buttons, glowing text-only tiles, autoplay heroes and continuous background effects on a 2 GB TV.

Borrow Apple TV's clarity and focus behavior; keep Nook's own branding, artwork and Android conventions. Do not promise tvOS, Apple services or use copied Apple icons/fonts/assets. [PRD](PRD.md) defines required outcomes.

## Home hierarchy

- Quiet header with clock and Settings access.
- Favorite app grid above the fold. YouTube first when installed; selected apps launch immediately.
- From iPhone utility tile stays easy to find. Drive is optional and labeled with setup status; Spotify only if wanted/installed.
- Continue Watching appears only for measured, resumable sessions.
- Shared & Recent emphasizes phone media with source/availability labels.
- Owner-selected YouTube/RSS content cards and collections below; independent streaming apps stay in the app grid rather than creating embedded movie-discovery rows. Hide empty modules and keep content refresh from stealing focus.

No playback samples, diagnostics, API setup, pair codes, empty collections or routine management buttons on Home. Keep one unobtrusive Customize action. All Apps and Settings remain reachable regardless of hidden rows.

Proposed layout: favorites use a six-column grid at 1080p with responsive safe margins; artwork can adapt to app banners or centered icons. This is a prototype starting point, not a frozen requirement. Verify actual display density/overscan before final dimensions.

## Core remote rules

| Input | Expected result |
|---|---|
| D-pad | Stable movement through grid, then content rows; no invisible focus or mouse dependency |
| OK on app | Launch directly |
| OK on media | Resume when valid, otherwise default action matches availability label |
| Hold OK | Context menu: reorder, pin/unpin, rename, hide or media actions |
| Visible More/Edit alternative | Same management actions for remotes without usable long press |
| Back | Close innermost overlay, then return to originating screen/card |
| Home in launcher mode | Return to Nook's documented starting focus; never exit to a blank screen |
| Media keys | Control the active supported player without changing app-grid focus |

Back on root Home in launcher mode stays on Home; it must not display a standalone-app exit dialog. Ordinary app entry remains available for recovery. Returning by Back from another app restores its originating tile; Home uses the predictable reset policy. Removed items select the nearest surviving tile.

## First run

1. Show useful detected apps immediately; no account, key or PC prerequisite.
2. Briefly explain Nook as a home screen; offer system default-home setup and a safe skip/retry.
3. Let owner choose/reorder favorites using the remote.
4. Offer From iPhone setup and optional PC customization.
5. Show connected/available state instead of claiming unsupported receiving features.
6. Missing YouTube/Spotify/browser gets an actionable explanation; never a fake app tile.

Setting Nook as default is the product goal. Skipping setup enables evaluation/recovery; it is not a substitute for the launcher release gate. Never disable the existing launcher automatically.

## From iPhone

Open a focused receiving screen with only supported routes. Examples are Open configured AirPlay receiver, Send a link, or Send a video copy. Show what must be on the same network, what opens externally and whether the TV keeps a copy. Do not combine these into an ambiguous Cast button.

For paired-browser transfer, proposed flow: scan QR/open address in Safari, pair with TV approval, select a video, show size and available TV storage, confirm transfer, display progress/cancel, then explicitly Play or Save. This does not implement iOS system Share Sheet integration. A share extension/Shortcut is a separate decision.

A new transfer never interrupts existing playback without confirmation. On disconnect, incomplete transfers stay out of replayable history. External receiver launch returns with an honest integration status; do not invent metadata from an unobservable session.

## Shared & Recent

| Item state | Primary action |
|---|---|
| Retained local video and known progress | Resume |
| Retained file with no progress | Play |
| Authorized Drive reference | Resolve access, then Play/Resume when supported |
| Saved external app/link | Open in app / Open again |
| Phone-hosted session no longer reachable | Reconnect phone |
| Screen mirroring only | Start sharing again; no fake replay card |
| Deleted/revoked source | Unavailable with Remove/Relink |

Cards show useful title/thumbnail where safely available, source badge and genuine progress. No raw signed URLs or account identifiers. History is owner-controlled and hideable. Remove from history, remove from collection and delete retained copy are separate actions; file deletion requires explicit confirmation.

## YouTube, Spotify, browser and Drive

YouTube launches its installed app with one OK. Saved videos/channels/playlists route where supported. Nook does not pretend to mirror account history.

Spotify is an optional app route, not a mandatory music service. Browser is a secondary app/shortcut; the original Brave request remains tracked in later integration work.

Drive's proposed destination lists only authorized media and selected folders/files with clear sign-in/reconnect/permission states. No Google password entry in Nook WebView or the LAN editor. The chosen OAuth flow and actual available scopes must be established first. Open in browser is an explicit fallback, not completion of native Drive access.

## PC and phone companion editor

Keep existing pairing, revision checks, explicit Save, draft recovery and dashboard backup. Add grid/favorite preview and source/destination visibility controls to match launcher vocabulary. Phone layout must work in Safari without desktop-width forms. Preview remains approximate.

Server disconnects when Nook leaves foreground under the current architecture; preserve drafts, explain reconnect and fetch latest revision. Default-home status does not imply an always-on server. Transfer/lifecycle needs a separate implementation decision.

## Appearance, accessibility and errors

Readable English UI with Unicode labels, long-title handling, safe margins and contrast over personal backgrounds. Use licensed app artwork with fallback initials, resized/lazy-loaded thumbnails and reduced motion. No perpetual shimmer when offline.

Useful messages include:
- This TV has not selected Nook as its Home app yet.
- Start sharing again from your iPhone; this session did not save a copy.
- This video is saved on the TV and can play without your phone.
- Drive access expired. Reconnect your account.
- Your TV storage is full. Free space or choose another video.
- Your layout changed elsewhere. Reload the latest version.

Technical details, sample players and diagnostics belong in Settings > Help & diagnostics. [Acceptance](ACCEPTANCE.md) verifies these flows; current 0.2 text-button UI does not satisfy this design.

## Refined setup, shortcuts and acceptance

Offer Make Nook your Home during onboarding and from Settings; explain before opening the actual system role prompt where available. Show Selected, Not selected, Selection unavailable or Could not verify after rechecking. Enabling a candidate is never displayed as selecting the default. Cancel is safe; retry is reachable without repeated unsolicited prompts. Exact system wording is firmware-owned.

Preserve working D-pad/OK/Back. Hold OK opens contextual actions with a visible More/Edit alternative. Add a compact Quick actions entry for Customize, Send video and source refresh. Menu-key support can mirror these actions where the remote provides it. Swipe/pointer controls may supplement mobile editing; gestures, double presses and remapping system Home/volume are not required. Validate any shortcut on the actual remote before assigning it.

Home must feel complete with zero sources and restrained with populated sources. Review empty, 3-5 visible content cards, long titles, poor artwork, offline, stale content, personal-media hidden and reduced-motion states. Owner approval on the TV remains the visual gate; merely adding rounded cards or more animation is insufficient.
