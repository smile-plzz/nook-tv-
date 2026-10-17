# Third-party inventory

## Runtime

M1 used only Android framework APIs. Preview 0.2 adds AndroidX Media3 1.11.1 (ExoPlayer, HLS, DASH, UI and their transitive dependencies), under their applicable Apache 2.0 and transitive licenses. Gradle resolves these from Google Maven; no tracker/advertising SDK is intentionally added. Media3: https://github.com/androidx/media. No NowShowing source/assets/provider code has been copied. The added SourceTemplate implements a general owner-provided URL contract independently.

## Build tooling

- Gradle wrapper JAR from the locally installed official Gradle 8.11.1 distribution: Apache License 2.0. License text is in `third-party/Gradle-LICENSE.txt`; Gradle notice is in `third-party/Gradle-NOTICE.txt`. Project: https://gradle.org/.
- Android SDK/build-tools/API stubs are external build requirements under their applicable Android SDK terms; not redistributed here.
- Current build pins Gradle 8.13 and AGP 8.13.2. The bootstrap wrapper JAR remains the official Gradle 8.11.1 wrapper; it downloads the pinned 8.13 distribution. Build tooling is not bundled in the APK.
- GitHub Actions reference checkout/setup-java/setup-node/setup-android/upload-artifact by version tags. They run in GitHub CI, not on the TV.

## Test media

The app links, without bundling, to Big Buck Bunny on Google's public sample bucket and Apple's BipBop HLS sample. Big Buck Bunny: Blender Foundation, CC BY 3.0, https://peach.blender.org/about/. Apple sample: https://developer.apple.com/streaming/examples/. Samples are for compatibility testing; remote availability is unverified.

## Upstream behavioral reference

NowShowing-Stream at commit `5fc8b1870a028b2ee84bf324a11f6529e85f22a1`: https://github.com/smile-plzz/NowShowing-Stream. No root LICENSE was present in the inspected tree. Until reuse rights are established, implement behavior independently and do not copy upstream source/assets. This restriction does not silently remove the planned integration.

## Launcher pivot references and future integrations

The 17 October 2026 documentation pivot introduces no new runtime dependency or copied code/assets. Apple TV is a visual/interaction reference, not an Apple product integration; use Nook branding and licensed artwork, not copied Apple assets. Projectivy, FLauncher and Dispatch are research references, not incorporated dependencies.

AirPlay/Cast receiver code, transfer libraries, Google Drive/Google sign-in SDKs, Spotify clients, icon packs and filtering engines are not added by the documentation change. Review actual dependency licenses, protocol/service requirements and distribution terms before selecting or bundling any integration. No receiver certification, Apple/Google affiliation or universal interoperability is claimed.

## TV-feedback refinement: planned sources only

Baseline 2.1 documentation adds no libraries, feed content, thumbnail assets or receiver code. Planned YouTube/RSS modules use independent Nook rendering. Before implementation, check provider API/branding/cache requirements and publisher image/excerpt rights; attribute sources and avoid republishing full articles. XML/QR/image helpers, if added, require a dependency/license inventory update. No Apple icons/fonts or arbitrary third-party widget code are authorized by the Apple-like visual direction. See [dynamic Home](docs/DYNAMIC-HOME.md).
