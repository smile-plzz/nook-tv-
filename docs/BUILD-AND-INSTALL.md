# Build and install the compatibility preview

Version 0.1.0-m1, compatibility app ID `com.ismail.nooktv.compat`. This separate preview ID can be uninstalled without affecting a future production app. No launcher replacement is registered or enabled.

## Windows SDK-only build

Requirements: Java JDK 21 (including `javac`, `jar`, `keytool`), PowerShell, Android SDK platform `android-35` and build-tools `35.0.1`. Node is needed for the browser syntax check. No downloaded runtime dependency is used by the app.

From repository root:

```powershell
./scripts/test-core.ps1
./scripts/test-editor.ps1 -AndroidSdk 'C:\path\to\Android\Sdk'
./scripts/build-offline.ps1 -AndroidSdk 'C:\path\to\Android\Sdk'
```

Editor-handler tests also need SDK package `sources;android-35`. They compile the installed AOSP JSON implementation into ignored test output, stripping only Android annotations. No SDK source is added to the APK or repository. Core checks do not require the SDK.

Build output: `android/app/build/offline/nook-tv-0.1.0-m1.apk`. The script compiles/resources, compiles Java, dexes, aligns, debug-signs, verifies and prints SHA-256. It stops on failed tool steps. `ANDROID_HOME` can be used instead of the argument. There is no dependency installation in this build path.

The generated debug signing key is in ignored `.signing/compat-debug.keystore`. Keep it locally for update continuity. It uses a standard development password and is not a production signing credential. Do not commit it.

## Gradle build

The conventional Android project includes Gradle 8.11.1 wrapper and AGP 8.9.1, compile/target SDK 35 and Java source compatibility 8. These versions match installed tooling; they are pinned for this compatibility branch, not claimed to be the latest available versions. Gradle needs network access or an already-populated dependency cache.

```powershell
$env:ANDROID_HOME = 'C:\path\to\Android\Sdk'
cd android
./gradlew.bat assembleDebug
```

On macOS/Linux use `sh gradlew assembleDebug`. The Gradle APK appears under `android/app/build/outputs/apk/debug/`. The SDK-only pipeline was executed locally; Gradle assembly has not yet been verified. Gradle's default debug key may differ from the SDK-only key. Do not assume the two APKs can update each other without reinstalling.

CI runs the SDK-only pipeline and uploads an APK artifact named `nook-tv-0.1.0-m1` in the GitHub Actions run. It does not publish a production release or claim device acceptance.

## Install on the TV

1. Copy the APK to a USB drive or use your preferred trusted TV file-transfer app.
2. Open it in the TV file manager and allow installation for that app if Android asks.
3. Install **Nook TV Preview**, then open it from your apps list.
4. Keep the original TV launcher. Nook does not request a default-home change in this build.
5. Test Apps and the sample video before pairing a PC.

If you already use Android debugging, the equivalent command is:

```powershell
adb install -r android/app/build/offline/nook-tv-0.1.0-m1.apk
adb shell am start -n com.ismail.nooktv.compat/com.ismail.nooktv.MainActivity
```

Do not enable network debugging or alter TV settings automatically. ADB was unavailable in the current execution environment, so installation is not yet verified.

## Pair your PC

Choose **Pair PC** on the TV. Open the displayed `http://<TV-address>:8765` on a PC on the same network, enter the code and approve the browser request on the TV. Edit and choose **Save to TV**. Nook must stay foregrounded, including when its native player is active. External YouTube/browser apps disconnect the editor; reopen Nook to reconnect.

If there is no address, check TV network connectivity. If the browser cannot connect, check guest Wi-Fi/client isolation and that both devices are on the same home network. Do not forward port 8765 through your router. Revoke browsers from TV Settings when needed.

## Update and rollback

Use an APK signed with the same key and an equal/higher version code to preserve preview data. If Android reports a signature mismatch, export your dashboard first, then uninstall/reinstall if you choose; this removes local preview state. Backup import is not implemented yet, so keep the export as a manual recovery reference.

Rollback in M1 means leaving or uninstalling Nook TV Preview and returning to the unchanged stock launcher. A downgrade to a lower Android version code may require uninstalling the preview. Do not use production user data with this compatibility build.
