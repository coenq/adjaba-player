@echo off
echo ================================================
echo DEPLOYMENT STATUS CHECK
echo ================================================
echo.

set ADB=C:\Users\User\AppData\Local\Android\Sdk\platform-tools\adb.exe

echo [1] Connected Devices:
%ADB% devices
echo.

echo [2] Current App Version on Device:
%ADB% shell dumpsys package com.adjaba | findstr versionName
echo.

echo [3] Clearing logcat for fresh monitoring...
%ADB% logcat -c
echo.

echo [4] Starting logcat monitoring (press Ctrl+C to stop)
echo     Watch for these indicators of smart sync:
echo     - "SelectScreens: SMART SYNC"
echo     - "AdSyncWorker"
echo     - "NEW ads to download"
echo     - "REMOVED ads to delete"
echo.
pause
echo.
%ADB% logcat | findstr /i "SelectScreens AdSyncWorker AdvertWatching AdDao"

