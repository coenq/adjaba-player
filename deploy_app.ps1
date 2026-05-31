$adbPath = "C:\Users\User\AppData\Local\Android\Sdk\platform-tools\adb.exe"
$apkPath = "C:\project\adjaba-player\app\build\outputs\apk\debug\app-debug.apk"
$logFile = "C:\project\adjaba-player\deploy_log.txt"

# Clear log file
"" | Out-File -FilePath $logFile -Force

# Kill and restart ADB server
Add-Content -Path $logFile -Value "$(Get-Date): Restarting ADB server..."
& $adbPath kill-server | Out-File -FilePath $logFile -Append
Start-Sleep -Seconds 2
& $adbPath start-server | Out-File -FilePath $logFile -Append
Start-Sleep -Seconds 2

# Check devices
Add-Content -Path $logFile -Value "$(Get-Date): Checking connected devices..."
& $adbPath devices -l | Out-File -FilePath $logFile -Append

# Install APK
Add-Content -Path $logFile -Value "$(Get-Date): Installing APK..."
& $adbPath install -r $apkPath | Out-File -FilePath $logFile -Append

Add-Content -Path $logFile -Value "$(Get-Date): Installation complete"

