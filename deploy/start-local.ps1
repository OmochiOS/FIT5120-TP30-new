$ErrorActionPreference = 'Stop'

$repoRoot = Split-Path -Parent $PSScriptRoot
$frontendDir = Join-Path $repoRoot 'front_end'
$backendDir = Join-Path $repoRoot 'back_end'
$staticDir = Join-Path $backendDir 'src\main\resources\static'
$mavenSettings = Join-Path $backendDir 'settings-local.xml'
$mavenCmd = Join-Path $backendDir 'tools\apache-maven-3.9.9\bin\mvn.cmd'
$jarPath = Join-Path $backendDir 'target\AmitySpace-0.0.1-SNAPSHOT.jar'
$stdoutLog = Join-Path $backendDir 'target\local-backend.out.log'
$stderrLog = Join-Path $backendDir 'target\local-backend.err.log'
$npmCmd = 'D:\node.js\npm.cmd'
$javaCommand = (Get-Command java.exe -ErrorAction SilentlyContinue).Source

if (-not $javaCommand) {
  $javaCommand = (Get-Command javaw.exe -ErrorAction Stop).Source
}

function Invoke-Native {
  param(
    [Parameter(Mandatory = $true)]
    [string]$FilePath,
    [Parameter(Mandatory = $false)]
    [string[]]$Arguments = @(),
    [Parameter(Mandatory = $true)]
    [string]$WorkingDirectory
  )

  Push-Location $WorkingDirectory
  try {
    & $FilePath @Arguments
    $exitCode = $LASTEXITCODE
  } finally {
    Pop-Location
  }

  if ($exitCode -ne 0) {
    throw "$FilePath failed with exit code $exitCode."
  }
}

Invoke-Native -FilePath $npmCmd -Arguments @('run', 'build') -WorkingDirectory $frontendDir

if (Test-Path $staticDir) {
  Remove-Item -Recurse -Force $staticDir
}

New-Item -ItemType Directory -Path $staticDir | Out-Null
Copy-Item (Join-Path $frontendDir 'dist\*') -Destination $staticDir -Recurse -Force

Invoke-Native -FilePath $mavenCmd -Arguments @('-s', $mavenSettings, '-B', '-ntp', '-DskipTests', 'package') -WorkingDirectory $backendDir

$listener = Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue | Select-Object -First 1
if ($listener) {
  Stop-Process -Id $listener.OwningProcess -Force
  Start-Sleep -Seconds 1
}

if (Test-Path $stdoutLog) {
  Remove-Item -Force $stdoutLog
}

if (Test-Path $stderrLog) {
  Remove-Item -Force $stderrLog
}

$startProcessParams = @{
  FilePath = $javaCommand
  ArgumentList = @('-jar', $jarPath, '--server.port=8080')
  WorkingDirectory = $backendDir
}

if ($javaCommand -like '*java.exe') {
  $startProcessParams.RedirectStandardOutput = $stdoutLog
  $startProcessParams.RedirectStandardError = $stderrLog
}

Start-Process @startProcessParams

$ready = $false
for ($i = 0; $i -lt 45; $i++) {
  Start-Sleep -Seconds 1

  try {
    $statusCode = (Invoke-WebRequest -Uri 'http://127.0.0.1:8080/api/locations' -UseBasicParsing).StatusCode
    if ($statusCode -eq 200) {
      $ready = $true
      break
    }
  } catch {
  }
}

if (-not $ready) {
  throw 'Backend did not become ready on http://127.0.0.1:8080 within 45 seconds.'
}

Write-Host 'AmitySpace is ready at http://127.0.0.1:8080' -ForegroundColor Green
Write-Host "Backend logs: $stdoutLog / $stderrLog"
