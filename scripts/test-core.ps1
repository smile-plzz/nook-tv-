$ErrorActionPreference = 'Stop'
$repo = Split-Path $PSScriptRoot -Parent
$out = Join-Path $repo 'test-output'
New-Item -ItemType Directory -Force $out | Out-Null
$core = Join-Path $repo 'android\app\src\main\java\com\ismail\nooktv'
& javac --release 8 -encoding UTF-8 -d $out "$core\Pairing.java" "$core\Targets.java" "$core\QrCode.java" "$core\SourceRefreshQueue.java" "$repo\tests\CoreTests.java" "$repo\tests\QrTests.java" "$repo\tests\SourceRefreshQueueTests.java"
if ($LASTEXITCODE -ne 0) { throw 'Core test compilation failed' }
& java -cp $out com.ismail.nooktv.CoreTests
if ($LASTEXITCODE -ne 0) { throw 'Core tests failed' }
& java -cp $out com.ismail.nooktv.QrTests
if ($LASTEXITCODE -ne 0) { throw 'QR tests failed' }
& java -cp $out com.ismail.nooktv.SourceRefreshQueueTests
if ($LASTEXITCODE -ne 0) { throw 'Source refresh scheduling tests failed' }
$sdk = $env:ANDROID_HOME
if (-not $sdk) { $sdk = Join-Path $env:LOCALAPPDATA 'Android\Sdk' }
$androidJar = Join-Path $sdk 'platforms\android-35\android.jar'
if (-not (Test-Path -LiteralPath $androidJar)) {
    $platforms = Get-ChildItem (Join-Path $sdk 'platforms') -Filter 'android.jar' -Recurse -ErrorAction SilentlyContinue
    if ($platforms) { $androidJar = $platforms[0].FullName }
}
if (Test-Path -LiteralPath $androidJar) {
    & javac -encoding UTF-8 -cp "$out;$androidJar" -d $out "$core\HomeCoordinator.java" "$repo\tests\HomeRoleTests.java"
    if ($LASTEXITCODE -ne 0) { throw 'Home role test compilation failed' }
    & java -cp "$out;$androidJar" com.ismail.nooktv.HomeRoleTests
    if ($LASTEXITCODE -ne 0) { throw 'Home role tests failed' }
}
& node --check "$repo\android\app\src\main\assets\editor.js"
if ($LASTEXITCODE -ne 0) { throw 'Editor JavaScript syntax check failed' }
Push-Location $repo
try { & node tests/editor-state.test.cjs; if ($LASTEXITCODE -ne 0) { throw 'Editor state tests failed' } } finally { Pop-Location }
& node --check "$repo\android\app\src\main\assets\media.js"
if ($LASTEXITCODE -ne 0) { throw 'Media transfer JavaScript syntax check failed' }
Push-Location $repo
try { & node tests/media-transfer.test.cjs; if ($LASTEXITCODE -ne 0) { throw 'Media transfer tests failed' } } finally { Pop-Location }

