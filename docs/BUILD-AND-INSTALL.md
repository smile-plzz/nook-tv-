# Build and install | 0.4.0-phone-preview

Baseline 2.1 documentation does not produce a new APK. PR #5 is merged; latest preview remains 0.4.0-phone-preview. A brief owner TV trial found default Home did not take effect (Home/restart returned elsewhere); do not treat installation or enabling the alias as success. Record exact installed version and setup screen, then follow [device checklist](DEVICE-TEST.md). The role-prompt improvement is planned in F1, not present in this APK.

## Scope of this APK

The 17 October launcher pivot is documented in [PRD](PRD.md). These commands build the 0.4 phone-file preview on the artwork-grid launcher with opt-in Home setup. See [prototype guide](LAUNCHER-PREVIEW.md). Paired-browser video upload and retained local playback are implemented; actual iPhone/Safari compatibility remains pending. AirPlay and Google Drive remain unimplemented.

The owner reports brief successful boot/exploration in BlueStacks; use that for preliminary visual/function checks only. Target-TV Home/wake/remote/receiver behavior needs actual hardware and real iPhone tests.

Compatibility app ID remains `com.ismail.nooktv.compat`, version code 4. It opens as an ordinary app until you explicitly enable Nook Home in Settings and select it through the system. The owner reports default-Home failure in brief TV use; exact installed version/setup cause and recovery remain unverified.

## Build

Requirements: JDK 21, Android SDK, network access for pinned Gradle/AGP/Media3 dependencies on first build. Gradle 8.13, AGP 8.13.2, Media3 1.11.1, compile SDK 36; target SDK 35, minimum API 26. `ANDROID_HOME` or Android Studio's local SDK configuration locates the SDK.

```powershell
cd android
./gradlew.bat --no-daemon assembleDebug assembleAndroidTest
```

On Linux/macOS: `sh gradlew --no-daemon assembleDebug assembleAndroidTest`.

App APK: `android/app/build/outputs/apk/debug/app-debug.apk`.
Instrumentation APK: `android/app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk`.

The old SDK-only M1 pipeline cannot compile Media3. `scripts/build-offline.ps1` now explains the required Gradle route instead of producing an incorrectly labeled APK. A populated Gradle cache may permit `--offline`; no claim is made that the local cache contains the new dependencies.

## Automated checks

```powershell
./scripts/test-core.ps1
./scripts/test-editor.ps1 -AndroidSdk 'C:\path\to\Android\Sdk'
```

These execute 82 core/document/source/viewing/HTTP-boundary assertions plus editor/transfer browser-state and syntax checks. Handler/document tests need `sources;android-35` for the SDK's AOSP JSON implementation. The helper strips annotations only in ignored test output; SDK sources are not redistributed in the APK.

The Compatibility APK workflow builds the app/test APKs and runs those checks. The Android runtime smoke workflow installs them in an API-35 emulator and runs a custom platform Instrumentation entrypoint:

```text
adb install -r android/app/build/outputs/apk/debug/app-debug.apk
adb install -r android/app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
adb shell am instrument -w com.ismail.nooktv.compat.test/com.ismail.nooktv.SmokeInstrumentation
```

Use a disposable test device/emulator for instrumentation: it changes dashboard/history fixtures. The owner installs only the app APK. Hosted artifacts are `nook-tv-0.4.0-phone-preview` and `android-smoke-evidence`.

## Current owner preview artifact

Delivered file: `nook-tv-0.4.0-phone-preview.apk`, version code 4, from the passing app code `9340bbfd98026e7d1ed8c017a6136a58b6fa6584`. Re-signed with the same private development certificate as M1/0.2/0.3; signatures, alignment and package/version verified. SHA-256: `b90ff039dbdfd812a7cd4c6f8fd767474c09d91b1e5c80905b98307ea58a3107`. It can update the earlier delivered preview without uninstalling.

## Historical 0.3 owner preview artifact

Delivered file: `nook-tv-0.3.0-launcher-preview.apk`, version code 3. It is the passing CI APK re-signed with the same development certificate as M1/0.2. v2/v3 signatures, alignment and package/version were verified. SHA-256: `19be610447536f328a9078cb6c1f6f038833d6b7395090a6e3d5389d8bc22353`. Raw CI artifacts may use another debug key.

## Install / update

Copy the app APK to a USB drive or trusted TV file-transfer app, open it with the TV file manager and install **Nook TV Preview**. Open from the app list. The normal system launcher stays available. Developer-install equivalent:

```text
adb install -r app-debug.apk
adb shell am start -n com.ismail.nooktv.compat/com.ismail.nooktv.MainActivity
```

Historical 0.2 artifact: `nook-tv-0.2.0-preview.apk` was re-signed from the passing CI build with the same private development key as the delivered M1 APK. Its certificate matches M1, and v2/v3 signatures and alignment were verified. It can update that M1 installation. SHA-256: `43680b094c3f07207f07de325dedb5defac05c1893f5fc8cbed608c75140360d`.

Same-key upgrades preserve/migrate M1 dashboard and progress. Raw Gradle/CI debug artifacts may use different keys from the delivered owner APK. If Android reports a signature mismatch, export your dashboard before deciding to uninstall/reinstall. Schema-v1 and schema-v2 dashboard imports are supported in the browser editor; viewing history and secrets are not in these exports. Keep the same development keystore for repeated builds where update continuity is needed. Production signing is still a later release gate.

## Browser editing / backup

On TV choose Pair PC. Open the displayed local HTTP address from a PC on the same home network, enter the code and approve the request on TV. Use Save to TV for ordinary edits; saving never automatically plays a link. Use Export for the current dashboard draft. Import shows item counts, asks before replacing, and keeps a pre-import recovery snapshot; TV Settings offers Undo last dashboard import.

Keep Nook foregrounded. Opening external apps stops the editor listener, closes its clients and cancels pending pairing; the browser keeps its draft. Reopen Nook and reload latest data before applying changes. Codes expire; sessions can be revoked on TV. No router forwarding, provider credentials or cloud account are needed.

## Metadata / sources

TV Settings accepts an optional OMDb key for native search/details. It is not editable over HTTP and is not exported. It supplies metadata, not streaming rights or availability.

For a site you can use, TV Settings accepts its documented HTTPS movie/series URL templates with `{imdb}` and, for series, `{season}`/`{episode}`. IMDb IDs are never substituted into TMDB fields. No provider is enabled by default or marked tested. Choose source at title details; opaque websites retain selected episode/recent opens without timestamp claims.

## Rollback

Select and verify the original launcher in system Home settings before removing Nook or uninstalling the preview; keep ordinary app access available for recovery. A lower-version-code APK may require uninstalling. Export your dashboard first if preserving it matters. Dashboard exports do not copy retained video bytes; uninstalling/clearing app storage deletes them, so preserve phone originals separately. Full target-TV acceptance remains pending; CI and emulator results are recorded separately from physical-device evidence.

## Launcher setup and recovery

Settings > Default Home setup enables Nook's Home option and opens system Home settings. Select Nook there if supported. Installation alone does not change your default. To revert, select the original Home first, then Remove Nook from Home choices. Removal is blocked while Nook is detected as default. Ordinary app entry remains. Exact settings vary by TV; do not execute speculative package-disabling/ADB commands or assume BlueStacks settings match the TV. Record Home from other apps, wake/reboot and successful rollback before launcher acceptance.

From iPhone now links to the paired-browser saved-file workflow. See [transfer guide](PHONE-TRANSFER-PREVIEW.md). This is not AirPlay, Cast or screen mirroring. Drive sign-in/media access is likewise not present. See [phone and Drive feasibility](PHONE-AND-DRIVE.md).
