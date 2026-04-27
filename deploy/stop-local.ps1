$listener = Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue | Select-Object -First 1

if (-not $listener) {
  Write-Host 'No local AmitySpace process is listening on port 8080.'
  exit 0
}

Stop-Process -Id $listener.OwningProcess -Force
Write-Host 'Stopped local AmitySpace process on port 8080.' -ForegroundColor Yellow
