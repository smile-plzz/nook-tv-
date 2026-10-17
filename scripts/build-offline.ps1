param([string]$AndroidSdk = $env:ANDROID_HOME, [string]$BuildTools = '35.0.1')
$ErrorActionPreference = 'Stop'
$repo = Split-Path $PSScriptRoot -Parent
if (-not $AndroidSdk) { $AndroidSdk = Join-Path $env:LOCALAPPDATA 'Android\Sdk' }
$sdk = (Resolve-Path -LiteralPath $AndroidSdk).Path
$tools = Join-Path $sdk "build-tools\$BuildTools"
$platform = Join-Path $sdk 'platforms\android-35\android.jar'
$build = Join-Path $repo 'android\app\build\offline'
$source = Join-Path $repo 'android\app\src\main'
New-Item -ItemType Directory -Force $build,"$build\classes","$build\dex","$build\gen" | Out-Null
function Check([string]$Step) { if ($LASTEXITCODE -ne 0) { throw "$Step failed with exit code $LASTEXITCODE" } }
& "$tools\aapt2.exe" compile --dir "$source\res" -o "$build\resources.zip"
Check 'Compile resources'
$manifest = [xml](Get-Content -LiteralPath "$source\AndroidManifest.xml" -Raw)
$manifest.manifest.SetAttribute('package','com.ismail.nooktv')
$manifest.Save("$build\AndroidManifest.xml")
& "$tools\aapt2.exe" link -o "$build\unsigned.apk" -I $platform --manifest "$build\AndroidManifest.xml" --java "$build\gen" --rename-manifest-package com.ismail.nooktv.compat --version-code 1 --version-name 0.1.0-m1 -A "$source\assets" "$build\resources.zip"
Check 'Link resources'
$sources = @(Get-ChildItem "$source\java","$build\gen" -Filter *.java -Recurse -File | ForEach-Object { '"' + $_.FullName.Replace('\','/') + '"' })
[IO.File]::WriteAllLines("$build\sources.txt",$sources)
& javac --release 8 -encoding UTF-8 -classpath $platform -d "$build\classes" "@$build\sources.txt"
Check 'Java compile'
& jar "-J-Djava.io.tmpdir=$build" cf "$build\classes.jar" -C "$build\classes" .
Check 'Java archive'
& "$tools\d8.bat" --lib $platform --min-api 26 --output "$build\dex" "$build\classes.jar"
Check 'DEX compile'
Add-Type -AssemblyName System.IO.Compression.FileSystem
$archive = [IO.Compression.ZipFile]::Open("$build\unsigned.apk",[IO.Compression.ZipArchiveMode]::Update)
try { [IO.Compression.ZipFileExtensions]::CreateEntryFromFile($archive,"$build\dex\classes.dex",'classes.dex',[IO.Compression.CompressionLevel]::Optimal) | Out-Null } finally { $archive.Dispose() }
& "$tools\zipalign.exe" -f -p 4 "$build\unsigned.apk" "$build\aligned.apk"
Check 'Zip alignment'
$keys = Join-Path $repo '.signing'
New-Item -ItemType Directory -Force $keys | Out-Null
$key = Join-Path $keys 'compat-debug.keystore'
if (-not (Test-Path -LiteralPath $key)) {
    & keytool -genkeypair -keystore $key -storepass android -keypass android -alias androiddebugkey -dname 'CN=Nook Compatibility, O=Nook, C=BD' -keyalg RSA -keysize 2048 -validity 10000
    Check 'Debug key generation'
}
$apk = Join-Path $build 'nook-tv-0.1.0-m1.apk'
& "$tools\apksigner.bat" sign --ks $key --ks-key-alias androiddebugkey --ks-pass pass:android --key-pass pass:android --out $apk "$build\aligned.apk"
Check 'APK signing'
& "$tools\apksigner.bat" verify --verbose $apk
Check 'APK verification'
Get-FileHash -Algorithm SHA256 -LiteralPath $apk
Write-Output "APK: $apk"
