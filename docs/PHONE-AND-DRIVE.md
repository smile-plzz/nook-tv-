# iPhone media and Google Drive feasibility

17 October 2026 | Planning assessment, not implemented functionality

The owner wants native-feeling iPhone video sharing and a simple way back to previously shared media. Google Drive access is requested if feasible. These outcomes are now tracked separately from NowShowing and arbitrary website playback.

## Phone routes must remain distinct

| Route | What it can provide | Replay/history limitation | Planned treatment |
|---|---|---|---|
| TV's existing AirPlay receiver | iPhone AirPlay/mirroring if receiver and source app support it | Live session may have no reusable file or Nook-visible progress | Detect/configure a documented launch route; verify actual TV |
| External Android receiver app | Potential AirPlay receiver UI | Integration, license, cost, DRM and lifecycle vary | Evaluate independently; no bundled/recommended app established yet |
| Existing Google Cast receiver | Participating iPhone apps can cast to compatible receiver | Not arbitrary Photos/Share Sheet/AirPlay; Nook may not observe session | Reuse verified device facility; do not advertise a built-in Cast receiver |
| Paired Safari link submission | Save a supported stable URL and open in intended handler | Link may expire; external progress unavailable | Extend existing companion UX; preserve source identity/capability |
| Paired Safari file transfer | TV keeps a permitted video copy, plays it through Media3 | Needs disk quota, upload endpoint, codec and interrupted-transfer handling | Preferred first self-contained prototype for durable replay; proposal, not owner-selected replacement for AirPlay |
| iOS Share Sheet/Shortcut/companion | Potential shorter sending gesture | Requires a separate integration and privacy/setup design | Evaluate after basic route; no iOS app currently exists |

Sources: [Apple AirPlay instructions](https://support.apple.com/en-us/102661), [Apple local-network permissions](https://support.apple.com/en-us/102229), [Google iPhone casting/local network](https://support.google.com/chromecast/answer/10063094?hl=en). Consulted 17 October 2026. These explain supported platform behavior, not compatibility with the owner's TV.

No receiver implementation is chosen. Do not make Apple protocol licensing, DRM, receiver interoperability or Google Cast certification claims based on a launcher APK. Apple TV-style visual design does not supply AirPlay.

## Replayability contract

Store durable source identity separately from resolved playback URL:
- Retained copy: app-private media ID, safe filename/display title, MIME/size, thumbnail reference, created time and verified existence.
- Saved URL: canonical reference and handler; avoid storing bearer/signed URLs as durable identity.
- Phone session: route/session capability and optional non-sensitive title; expires when sender ends session.
- Drive: file ID, optional resource key, account-reference ID and revision/version fingerprint where available; no token in media records.

Record position only when observed. A replayable native item may say Resume. An external link says Open again. A phone-only session says Reconnect phone. Mirroring alone does not create a saved recording or playable history item. Do not merge same-named files or apply progress across changed file content.

## Proposed retained-transfer design

TV opens a foreground receive screen, displays a QR/address, and requires pairing/TV approval. Safari chooses one file; TV checks configured size/quota/free space before and during streaming. Use opaque IDs, MIME/content validation, safe metadata and no arbitrary filesystem paths.

This is a new media endpoint, separate from the current 1 MiB JSON editor contract. Stream to app-private temporary disk, enforce request and storage limits, cancel/delete partial files, atomically finalize complete files. Never base64 a video into dashboard JSON or load the entire body into RAM. Initial proposed limits: one transfer at a time, configurable retained-media quota, and an explicit tested per-file maximum; choose numeric defaults only after TV free-space and throughput measurements.

Retained media is durable user data, not disposable cache. History clear preserves files; Delete saved copy explicitly confirms file deletion. Dashboard export excludes media payloads and reports unresolved local references after restore on another device.

Current foreground HTTP editor is paired but not encrypted. Do not present file upload as confidential on untrusted LANs. Decide whether to add authenticated TLS/pinned setup or limit an explicitly described home-LAN prototype; keep Google tokens/passwords outside HTTP. No always-on server is implied by default-home status. Background transfer requires its own lifecycle/notification decision before implementation.

Real iPhone tests: Photos/Files selection, orientation, large file, interruption, cancellation, low space, HEVC compatibility, phone locking/disconnection, TV restart and resumed replay. Transcoding is not included by assumption.

## Drive route and authentication gate

Feasible API building blocks:
- [files.get / alt=media and partial downloads](https://developers.google.com/workspace/drive/api/guides/manage-downloads): authorized binary media download and Range requests.
- [Resource keys](https://developers.google.com/workspace/drive/api/guides/resource-keys): some link-shared items require an additional resource key.
- [TV/limited-input OAuth](https://developers.google.com/identity/protocols/oauth2/limited-input-device): allowed Drive scopes include drive.file and drive.appdata, not unrestricted Drive read scopes.
- [Choose scopes](https://developers.google.com/workspace/drive/api/guides/api-specific-auth): scope selection and verification implications.
- [Google Picker](https://developers.google.com/workspace/drive/picker/guides/overview): possible explicit file-selection route; must verify authentication/client/origin requirements.

These sources were consulted 17 October 2026. Device-code sign-in alone is not a complete selected-existing-file picker or whole-Drive folder browser. Establish how owner-selected existing files become accessible with the chosen client/scopes. Do not move access/refresh tokens through the LAN editor to patch a missing OAuth design. If broader access is truly needed, assess supported system-browser authorization, least privilege and verification requirements explicitly.

Proposed first outcome: authorize selected media, show its title/artwork, play a supported file, seek and save a stable return reference. Folder browsing is conditional on proven grant/discovery semantics; a pinned public link is an explicitly narrower fallback, not private Drive integration.

Token handling: private encrypted storage with Android Keystore-backed key material, HTTPS to Google, Authorization headers, bounded refresh retries and a disconnect/revoke action. Do not forward Google credentials to redirected/untrusted origins, external player intents, QR codes, editor, logs or dashboard exports. Re-resolve targets rather than persist access-token URLs.

Respect download permissions, quota/rate limits, file removal/revocation and unsupported media. Test seeking across token refresh, changed file identity, lost network and reconnect. No background indexing of the entire account, offline download library, transcoding, account sync or mandatory cloud service is implied.

## Required decision record before implementation

Record chosen phone route and expected sender gesture; receiver dependency and license; retained/live distinction; foreground/background lifecycle; transport and transfer limits; actual TV/iPhone evidence. Separately record Drive client type/scopes, existing-file selection, credential storage, picker/origin needs, playback/seek result and any owner-visible limitation.

[PRD](PRD.md) requires one real phone video workflow for launcher acceptance. It does not require pretending that every sharing protocol is supported.
