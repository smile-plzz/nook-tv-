param([string]$AndroidSdk = $env:ANDROID_HOME)
throw 'The 0.2 preview uses Media3 dependencies. Build with android/gradlew.bat assembleDebug (see docs/BUILD-AND-INSTALL.md). The SDK-only build applies only to historical M1 source.'
