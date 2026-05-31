@echo off
setlocal enabledelayedexpansion

set ADB=C:\Users\User\AppData\Local\Android\Sdk\platform-tools\adb.exe
set PKG=com.adjaba
set LOG=C:\project\adjaba-player\ads_diagnostic.log

echo ============================================ > %LOG%
echo AD DIAGNOSTIC FOR SCREEN kolk737 >> %LOG%
echo ============================================ >> %LOG%
echo. >> %LOG%

echo [%date% %time%] Clearing logcat... >> %LOG%
%ADB% logcat -c 2>&1 | findstr /v "failed to" >> %LOG%

echo [%date% %time%] Starting app... >> %LOG%
%ADB% shell am start -n %PKG%/.activities.SelectScreens >> %LOG% 2>&1

echo [%date% %time%] Waiting for app to load... >> %LOG%
timeout /t 5 /nobreak > nul

echo. >> %LOG%
echo ===== DATABASE CHECK ===== >> %LOG%
%ADB% shell "run-as %PKG% ls /data/data/%PKG%/databases/" >> %LOG% 2>&1

echo. >> %LOG%
echo ===== APP LOGS ===== >> %LOG%
%ADB% logcat -d -s AdvertWatching:I AdvertWatching:W AdvertWatching:E DataHolder:* SelectScreens:I >> %LOG% 2>&1

echo. >> %LOG%
echo ===== RECENT ERRORS ===== >> %LOG%
%ADB% logcat -d *:E | findstr /i "adjaba ad" >> %LOG% 2>&1

echo. >> %LOG%
echo ===== DIAGNOSTIC COMPLETE ===== >> %LOG%
echo [%date% %time%] Done >> %LOG%

type %LOG%

