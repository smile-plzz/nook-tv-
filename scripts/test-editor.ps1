param([string]$AndroidSdk = $env:ANDROID_HOME)
$ErrorActionPreference = 'Stop'
$repo = Split-Path $PSScriptRoot -Parent
if (-not $AndroidSdk) { $AndroidSdk = Join-Path $env:LOCALAPPDATA 'Android\Sdk' }
$jsonSources = Join-Path $AndroidSdk 'sources\android-35\org\json'
if (-not (Test-Path -LiteralPath $jsonSources)) { throw 'Install SDK sources;android-35 to run the editor-handler tests.' }
$out = Join-Path $repo 'test-output\editor'
$jsonOut = Join-Path $out 'org\json'
New-Item -ItemType Directory -Force $jsonOut | Out-Null
# Use the SDK AOSP JSON implementation for JVM tests, retaining its license headers.
# Strip Android-only annotations, which do not affect JSON behavior. No modified SDK
# code is bundled in the application or committed to this repository.
Get-ChildItem -LiteralPath $jsonSources -Filter *.java | ForEach-Object {
    $text = Get-Content -LiteralPath $_.FullName -Raw
    $text = $text -replace '(?m)^import (static )?(android\.|libcore\.).*;\r?\n',''
    $text = $text -replace '@(NonNull|Nullable|UnsupportedAppUsage)(\([^)]*\))?',''
    $text = $text -replace '@SystemApi\([^)]*\)',''
    [IO.File]::WriteAllText((Join-Path $jsonOut $_.Name), $text)
}
$core = Join-Path $repo 'android\app\src\main\java\com\ismail\nooktv'
$sources = @(Get-ChildItem -LiteralPath $jsonOut -Filter *.java | ForEach-Object { $_.FullName })
& javac --release 8 -encoding UTF-8 -d $out @sources "$core\Pairing.java" "$core\Targets.java" "$core\EditorServer.java" "$core\DashboardDocument.java" "$core\ViewingLedger.java" "$core\SourceTemplate.java" "$repo\tests\EditorBoundaryTests.java" "$repo\tests\DocumentTests.java"
if ($LASTEXITCODE -ne 0) { throw 'Editor handler tests failed to compile' }
& java -cp $out com.ismail.nooktv.EditorBoundaryTests
if ($LASTEXITCODE -ne 0) { throw 'Editor handler tests failed' }
& java -cp $out com.ismail.nooktv.DocumentTests
if ($LASTEXITCODE -ne 0) { throw 'Document and viewing tests failed' }
