$ErrorActionPreference = 'Stop'

$root = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$frontendDir = Join-Path $root 'front_end'
$backendDir = Join-Path $root 'back_end'
$distDir = Join-Path $frontendDir 'dist'
$staticDir = Join-Path $backendDir 'src\main\resources\static'
$releaseDir = Join-Path $root 'deploy\release'
$jarName = 'AmitySpace-0.0.1-SNAPSHOT.jar'
$releaseJar = Join-Path $releaseDir $jarName
$mavenCmd = Join-Path $backendDir 'tools\apache-maven-3.9.9\bin\mvn.cmd'
$settingsFile = Join-Path $backendDir 'settings-local.xml'
$pomFile = Join-Path $backendDir 'pom.xml'

Write-Host 'Building frontend...'
Set-Location -Path $frontendDir
& 'D:\node.js\npm.cmd' run build

Write-Host 'Syncing frontend dist into Spring Boot static resources...'
if (Test-Path $staticDir) {
  Remove-Item -LiteralPath $staticDir -Recurse -Force
}
New-Item -ItemType Directory -Path $staticDir | Out-Null
Copy-Item (Join-Path $distDir '*') -Destination $staticDir -Recurse -Force

Write-Host 'Packaging backend jar...'
Set-Location -Path $root
& $mavenCmd -s $settingsFile -f $pomFile -B -ntp -DskipTests package

Write-Host 'Publishing release jar...'
if (Test-Path $releaseDir) {
  Remove-Item -LiteralPath $releaseDir -Recurse -Force
}
New-Item -ItemType Directory -Path $releaseDir | Out-Null
Copy-Item (Join-Path $backendDir "target\$jarName") -Destination $releaseJar -Force

$jarInfo = Get-Item $releaseJar
Write-Host "Release ready: $($jarInfo.FullName)"
Write-Host "Jar size: $([Math]::Round($jarInfo.Length / 1MB, 2)) MB"
