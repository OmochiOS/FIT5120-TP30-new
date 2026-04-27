$ErrorActionPreference = 'Stop'
Set-Location -Path $PSScriptRoot
& 'C:\Program Files\Common Files\Oracle\Java\javapath\java.exe' -jar 'target\AmitySpace-0.0.1-SNAPSHOT.jar' --server.port=8080
