# Third-party inventory

## Runtime

The M1 app uses Android framework APIs supplied by the device. No third-party runtime library, tracker, analytics service or advertising SDK is bundled. No NowShowing source, images or provider-specific code has been copied.

## Build tooling

- Gradle wrapper JAR from the locally installed official Gradle 8.11.1 distribution: Apache License 2.0. License text is in `third-party/Gradle-LICENSE.txt`; Gradle notice is in `third-party/Gradle-NOTICE.txt`. Project: https://gradle.org/.
- Android SDK/build-tools/API stubs are external build requirements under their applicable Android SDK terms; not redistributed here.
- AGP 8.9.1 is referenced by the optional Gradle build, not bundled in the application.
- GitHub Actions reference checkout/setup-java/setup-node/setup-android/upload-artifact by version tags. They run in GitHub CI, not on the TV.

## Test media

The app links, without bundling, to Big Buck Bunny on Google's public sample bucket and Apple's BipBop HLS sample. Big Buck Bunny: Blender Foundation, CC BY 3.0, https://peach.blender.org/about/. Apple sample: https://developer.apple.com/streaming/examples/. Samples are for compatibility testing; remote availability is unverified.

## Upstream behavioral reference

NowShowing-Stream at commit `5fc8b1870a028b2ee84bf324a11f6529e85f22a1`: https://github.com/smile-plzz/NowShowing-Stream. No root LICENSE was present in the inspected tree. Until reuse rights are established, implement behavior independently and do not copy upstream source/assets. This restriction does not silently remove the planned integration.
