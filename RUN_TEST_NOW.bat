@echo off
REM ============================================================
REM ADJABA PLAYER - EMULATOR DEPLOYMENT & TEST
REM One simple script - no guessing, no waiting, just DO IT
REM ============================================================

setlocal enabledelayedexpansion
cd /d C:\project\adjaba-player

echo.
echo ============================================================
echo   ADJABA PLAYER - EMULATOR TEST AUTOMATION
echo ============================================================
echo.

REM Configuration
set SDK=C:\Users\User\AppData\Local\Android\Sdk
set AVD=Adjaba_TV_Test
set APP=com.adjaba.adplayer
set ACTIVITY=com.adjaba.adplayer.activities.SelectScreens

REM ============================================================
echo [1/5] Building app...
echo ============================================================
call gradlew.bat clean build -x test -q
if errorlevel 1 (
    echo ERROR: Build failed
    call gradlew.bat clean build -x test
    pause
    exit /b 1
)
echo ✓ Build successful

REM ============================================================
echo.
echo [2/5] Starting emulator...
echo ============================================================
start "" "%SDK%\emulator\emulator.exe" -avd %AVD% -no-snapshot-load -accel on -memory 2048
echo ✓ Emulator starting (may take 1-5 minutes)...
timeout /t 60 /nobreak

REM ============================================================
echo.
echo [3/5] Waiting for device...
echo ============================================================
"%SDK%\..\platform-tools\adb.exe" wait-for-device
echo ✓ Device ready

REM ============================================================
echo.
echo [4/5] Installing app...
echo ============================================================
call gradlew.bat installDebug -q
if errorlevel 1 (
    echo ERROR: Install failed
    pause
    exit /b 1
)
echo ✓ App installed

REM ============================================================
echo.
echo [5/5] Launching app...
echo ============================================================
"%SDK%\..\platform-tools\adb.exe" shell am start -n "%APP%/%ACTIVITY%"
timeout /t 3

echo.
echo ============================================================
echo   ✓ APP IS NOW RUNNING ON EMULATOR
echo ============================================================
echo.
echo WHAT TO DO NOW:
echo.
echo 1. Navigate with arrow keys
echo 2. Select with Enter
echo 3. Rotate landscape with Ctrl+F11
echo.
echo TEST THESE 5 THINGS:
echo.
echo   [1] Select Kolkata - Clock should show IST (UTC+5:30)
echo   [2] Check weather - Should show Kolkata data
echo   [3] News - Images should load from Times of India
echo   [4] Landscape - Headline should NOT overlap logo
echo   [5] QR codes - Only visible on ads, hidden on news/weather
echo.
echo MONITOR LOGS (in another terminal):
echo   adb logcat | findstr "Adjaba"
echo.
echo STOP EMULATOR:
echo   adb emu kill
echo.
pause

