@echo off
setlocal enabledelayedexpansion

set ADB=C:\Users\User\AppData\Local\Android\Sdk\platform-tools\adb.exe
set APK=C:\project\adjaba-player\app\build\outputs\apk\debug\app-debug.apk
set LOG=C:\project\adjaba-player\deploy_smart_sync.log

echo ======================================================== > %LOG%
echo SMART PLAYLIST SYNC DEPLOYMENT >> %LOG%
echo ======================================================== >> %LOG%
echo Timestamp: %date% %time% >> %LOG%
echo. >> %LOG%

echo [1] Checking connected devices... >> %LOG%
%ADB% devices >> %LOG% 2>&1

echo. >> %LOG%
echo [2] Checking APK... >> %LOG%
if exist %APK% (
    echo APK found: %APK% >> %LOG%
    for %%A in (%APK%) do echo APK size: %%~zA bytes >> %LOG%
    for %%A in (%APK%) do echo Last modified: %%~tA >> %LOG%
) else (
    echo ERROR: APK not found at %APK% >> %LOG%
    echo. >> %LOG%
    echo Building APK first... >> %LOG%
    cd /d C:\project\adjaba-player
    call gradlew.bat assembleDebug --console=plain >> %LOG% 2>&1
)

echo. >> %LOG%
echo [3] Deploying to device... >> %LOG%
%ADB% install -r %APK% >> %LOG% 2>&1

echo. >> %LOG%
echo [4] Checking installation... >> %LOG%
%ADB% shell pm list packages | findstr com.adjaba >> %LOG% 2>&1

echo. >> %LOG%
echo [5] Getting app version... >> %LOG%
%ADB% shell dumpsys package com.adjaba | findstr versionName >> %LOG% 2>&1

echo. >> %LOG%
echo ======================================================== >> %LOG%
echo DEPLOYMENT COMPLETE >> %LOG%
echo ======================================================== >> %LOG%

type %LOG%
echo.
echo Full log saved to: %LOG%
pause

