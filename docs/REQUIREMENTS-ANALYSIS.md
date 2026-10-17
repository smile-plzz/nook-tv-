# Requirements analysis and launcher pivot

Baseline 2.0 | 17 October 2026 | Reconciles original intent, 0.2 implementation and BlueStacks feedback

## What changed

The owner briefly tried 0.2 in BlueStacks: installation/boot worked, but the product did not feel like the desired launcher. He then explicitly selected default Home and Apple TV-style aesthetics, prioritizing YouTube, iOS video sharing and return to previously shared media, with possible Spotify, rare browser use and conditional Google Drive access.

The implementation optimized infrastructure and editable dashboard breadth before the daily home experience. Text buttons, app options before launch, test samples and configuration-heavy empty states reinforced that mismatch. These are interaction/product gaps; passing storage/player tests cannot prove a satisfying launcher.

## Confirmed, proposed and unknown

| Topic | Classification | Treatment |
|---|---|---|
| Default Home | Confirmed | LAUNCH-01 promoted to P0 |
| Apple TV-style aesthetics and experience | Confirmed | App grid, artwork and remote focus central; exact layout proposed |
| YouTube | Confirmed main use | One-click app access first, richer integration retained later |
| iPhone video sharing | Confirmed core use | From iPhone plus at least one tested route; protocol not yet selected |
| Returning to phone-shared media | Confirmed core use | Durable identity, replayability and progress capability |
| Spotify | Tentative | Optional shortcut; no assumed account integration |
| Browser | Rare use | Secondary access; avoid browser-first Home |
| Google Drive | Requested if possible | Feasibility gate and scoped authorized media integration |
| PC editing/collections | Existing requirement retained | Companion management supports launcher |
| NowShowing-Stream | Existing selected reference retained | Optional discovery destination, later priority |
| Exact iPhone sending gesture/apps | Unknown | Verify AirPlay vs Photos/file transfer vs links; do not block design |
| Actual TV Home/Cast/AirPlay support | Unknown | Real-device checks; BlueStacks cannot establish these |
| Existing Java/Views/AtomicFile | Delivered engineering choice | Keep unless measured needs justify replacement |

## Priority reconciliation

The earlier standalone-first baseline made launcher support P1 and placed discovery before everyday navigation. That ordering is superseded. Launcher, recognizable app grid and phone-media return are P0. NowShowing/provider work, richer YouTube and Brave/filtering remain requested P1 work. Mood/weather/widgets become optional P2. Spotify/Drive do not become mandatory accounts for Home.

The pivot baseline was documentation-only, then the owner merged it and requested the next steps. The 0.3 [prototype](LAUNCHER-PREVIEW.md) now implements grid/direct-app and initial Home setup. Phone receiving/Drive remain future work. Follow the new sequence.

## Feasibility and evidence

AirPlay, Google Cast, mirroring and local transfer solve different tasks. Apple describes AirPlay to compatible receivers; Google describes iPhone casting from participating apps with local-network permission. Neither proves this TV supports either route. An external receiver can provide immediate access, but replay/history require a documented integration or a retained copy. No universal cross-app progress is assumed.

Google Drive supports authorized binary media download and byte-range requests. This makes a media source plausible, not already integrated. Google's limited-input device flow currently permits drive.file and drive.appdata; it does not automatically authorize browsing an entire existing Drive. File selection/scopes/authentication need a verified design before promising folders. See [phone and Drive assessment](PHONE-AND-DRIVE.md) for sources and implementation gates.

The full launcher can be feasible while system Home behavior is firmware-dependent. A blocked Home selection is a core product issue requiring a decision; do not silently declare the standalone fallback accepted.

## Existing source findings retained

The targeted NowShowing-Stream audit found that recent-title entries are added on detail open without timestamp progress; season/episode and confirmed-working source semantics are incomplete. Its iframe sources are not native media URLs. Keep independent identity/episode/progress records, no iframe-load success claims and no fake timestamps. No upstream copying until reuse rights are established. See [source audit](NOWSHOWING-AUDIT.md).

## Research references and lessons

| Reference | Lesson, not a reuse decision |
|---|---|
| [Projectivy developer listing](https://play.google.com/store/apps/details?id=com.spocky.projengmenu) | Personal categories/shortcuts and launcher utility; not the selected visual style |
| [FLauncher developer listing](https://play.google.com/store/apps/details?id=me.efesser.flauncher) | Useful app access and straightforward organization |
| [Dispatch developer listing](https://play.google.com/store/apps/details?id=com.spauldhaliwal.dispatch) | Content artwork and media entry points; Plex dependency does not fit core Nook |
| [Reddit Watch Next discussion](https://www.reddit.com/r/AndroidTV/comments/1cgc4x7/projectivity_launcher_continue_watching/) | Anecdotal reports of inconsistent app participation; not a compatibility guarantee |
| [Android TV channels](https://developer.android.com/training/tv/discovery/recommendations-channel) | Apps publish content via platform mechanisms; cross-app reading and available data must be verified |

Research supports the pivot; it does not authorize copying launcher code/assets or guarantee every service's recommendations. Apple TV is the owner's chosen aesthetic reference.

## Risks and decisions before implementation

- TV default-home restrictions: inspect actual firmware and test selection/restoration; never silently disable stock Home.
- Ephemeral phone media: store replay capability explicitly; retained transfer is distinct from live sharing.
- iPhone codec variability: use owner-selected representative files, including HEVC when relevant; no transcoding assumption.
- Drive authorization: validate selected-file access and token handling; no password collection or credential proxy through LAN HTTP.
- Large transfers on 2 GB TV: stream to disk, enforce quotas/free-space checks, clean cancellation and do not buffer whole videos in memory.
- Visual richness: lazy artwork/grid, no autoplay, bounded caches, reduced-motion mode.
- Privacy: shared media can be personal; explicit display/storage/delete controls, no silent Photos/library import.

Design decisions remain in [DECISIONS](DECISIONS.md), measurable outcomes in [PRD](PRD.md), and delivery order in [plan](DEVELOPMENT-PLAN.md).

