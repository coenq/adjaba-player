@echo off
setlocal enabledelayedexpansion

set ADB=C:\Users\User\AppData\Local\Android\Sdk\platform-tools\adb.exe
set APK=C:\project\adjaba-player\app\build\outputs\apk\debug\app-debug.apk
set LOG=C:\project\adjaba-player\deployment_status.log

echo [%date% %time%] Starting deployment check... > %LOG%

echo. >> %LOG%
echo ===== DEVICE CHECK ===== >> %LOG%
%ADB% devices >> %LOG% 2>&1

echo. >> %LOG%
echo ===== INSTALLING APK ===== >> %LOG%
%ADB% install -r "%APK%" >> %LOG% 2>&1

echo. >> %LOG%
echo ===== PACKAGE CHECK ===== >> %LOG%
%ADB% shell pm list packages ^| findstr adjaba >> %LOG% 2>&1

echo. >> %LOG%
echo ===== GETTING APP VERSION ===== >> %LOG%
%ADB% shell dumpsys package com.adjaba 2>&1 | findstr versionName >> %LOG%

echo. >> %LOG%
echo ===== DEPLOYMENT COMPLETE ===== >> %LOG%
echo [%date% %time%] Done >> %LOG%

type %LOG%

