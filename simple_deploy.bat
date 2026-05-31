@echo off
REM Simple, direct emulator deployment script for Adjaba Player
REM Broken into simple steps for reliability

setlocal enabledelayedexpansion
cd /d C:\project\adjaba-player

echo.
echo ╔════════════════════════════════════════════════════════════════╗
echo ║   ADJABA PLAYER - SIMPLE EMULATOR DEPLOYMENT                 ║
echo ╚════════════════════════════════════════════════════════════════╝
echo.

REM Set Android SDK path
set ANDROID_SDK_ROOT=C:\Users\User\AppData\Local\Android\Sdk
set AVD_NAME=Adjaba_TV_Test
set ADB=%ANDROID_SDK_ROOT%\..\platform-tools\adb.exe

echo [STEP 1] Checking Android SDK...
if not exist "%ANDROID_SDK_ROOT%" (
    echo ERROR: Android SDK not found
    echo Expected at: %ANDROID_SDK_ROOT%
    pause
    exit /b 1
)
echo OK - SDK found

echo.
echo [STEP 2] Building app...
echo This may take 3-5 minutes...
call gradlew.bat clean build -x test -q
if errorlevel 1 (
    echo ERROR: Build failed
    call gradlew.bat clean build -x test
    pause
    exit /b 1
)
echo OK - Build successful

echo.
echo [STEP 3] Checking for connected devices...
"%ADB%" devices > temp_devices.txt
type temp_devices.txt | find "device" > nul
if errorlevel 1 (
    echo No devices found - starting emulator...
    echo Starting: %AVD_NAME%
    start "" "%ANDROID_SDK_ROOT%\emulator\emulator.exe" -avd %AVD_NAME% -no-snapshot-load -accel on -memory 2048

    echo Waiting for emulator to boot...
    echo This takes 2-5 minutes on first run...
    timeout /t 120 /nobreak

    echo Checking device again...
    "%ADB%" wait-for-device
    echo Device ready!
) else (
    echo Device already connected
    type temp_devices.txt | find "emulator"
)
del temp_devices.txt

echo.
echo [STEP 4] Installing app...
call gradlew.bat installDebug -q
if errorlevel 1 (
    echo ERROR: Installation failed
    pause
    exit /b 1
)
echo OK - App installed

echo.
echo [STEP 5] Launching app...
"%ADB%" shell am start -n com.adjaba.adplayer/.activities.SelectScreens
timeout /t 3

echo.
echo [STEP 6] Verifying app...
"%ADB%" shell dumpsys package com.adjaba.adplayer | find "versionName" > nul
if errorlevel 1 (
    echo WARNING: Could not verify app
) else (
    echo OK - App verified on device
)

echo.
echo ════════════════════════════════════════════════════════════════
echo ✓ DEPLOYMENT COMPLETE
echo ════════════════════════════════════════════════════════════════
echo.
echo APP IS NOW RUNNING ON EMULATOR
echo.
echo NEXT: Perform manual tests on these features:
echo   1. Kolkata local time - Should show IST (UTC+5:30)
echo   2. Weather - Should display for Kolkata location
echo   3. News images - Should load from Times of India RSS
echo   4. Landscape - Headline should be 56dp from bottom
echo   5. QR codes - Visible on ads, hidden on news/weather
echo.
echo NAVIGATION:
echo   Arrow keys = D-pad / move
echo   Enter = select
echo   Escape = back
echo   Ctrl+F11 = rotate to landscape
echo.
echo COMMANDS:
echo   View logs:  adb logcat | findstr "Adjaba"
echo   Stop app:   adb shell am force-stop com.adjaba.adplayer
echo   Restart:    adb shell am start -n com.adjaba.adplayer/.activities.SelectScreens
echo   Kill emu:   adb emu kill
echo.
pause

