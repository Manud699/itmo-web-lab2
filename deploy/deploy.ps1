# Builds the server jar and deploys it, together with the frontend, to helios.
# Usage, from the repo root:  powershell -ExecutionPolicy Bypass -File deploy\deploy.ps1
$ErrorActionPreference = "Stop"
$User = "s464921"
$Server = "se.ifmo.ru"
$Port = 2222
$Root = Split-Path -Parent $PSScriptRoot
$Stage = Join-Path $Root "backend\build\deploy"

Push-Location (Join-Path $Root "backend")
.\gradlew.bat jar
$buildExit = $LASTEXITCODE
Pop-Location
if ($buildExit -ne 0) { throw "Gradle build failed" }

if (Test-Path $Stage) { Remove-Item -Recurse -Force $Stage }
New-Item -ItemType Directory -Path (Join-Path $Stage "www") | Out-Null
Copy-Item (Join-Path $Root "backend\build\libs\lab2-server.jar") $Stage
Copy-Item (Join-Path $PSScriptRoot "restart-helios.sh") $Stage
foreach ($dir in "html", "css", "js") {
    Copy-Item -Recurse (Join-Path $Root "frontend\$dir") (Join-Path $Stage "www")
}

scp -P $Port -r $Stage "${User}@${Server}:~/web-programing/"
if ($LASTEXITCODE -ne 0) { throw "scp failed" }
ssh -p $Port "${User}@${Server}" "sh ~/web-programing/deploy/restart-helios.sh"
if ($LASTEXITCODE -ne 0) { throw "Restart on helios failed" }
