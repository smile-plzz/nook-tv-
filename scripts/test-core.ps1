$ErrorActionPreference = 'Stop'
$repo = Split-Path $PSScriptRoot -Parent
$out = Join-Path $repo 'test-output'
New-Item -ItemType Directory -Force $out | Out-Null
$core = Join-Path $repo 'android\app\src\main\java\com\ismail\nooktv'
& javac --release 8 -encoding UTF-8 -d $out "$core\Pairing.java" "$core\Targets.java" "$repo\tests\CoreTests.java"
if ($LASTEXITCODE -ne 0) { throw 'Core test compilation failed' }
& java -cp $out com.ismail.nooktv.CoreTests
if ($LASTEXITCODE -ne 0) { throw 'Core tests failed' }
& node --check "$repo\android\app\src\main\assets\editor.js"
if ($LASTEXITCODE -ne 0) { throw 'Editor JavaScript syntax check failed' }
Push-Location $repo
try { & node tests/editor-state.test.cjs; if ($LASTEXITCODE -ne 0) { throw 'Editor state tests failed' } } finally { Pop-Location }
& node --check "$repo\android\app\src\main\assets\media.js"
if ($LASTEXITCODE -ne 0) { throw 'Media transfer JavaScript syntax check failed' }
Push-Location $repo
try { & node tests/media-transfer.test.cjs; if ($LASTEXITCODE -ne 0) { throw 'Media transfer tests failed' } } finally { Pop-Location }
