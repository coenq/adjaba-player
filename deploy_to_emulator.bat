@echo off
REM Complete emulator setup and deployment script for Adjaba Player

REM Colors and formatting
setlocal enabledelayedexpansion

set ANDROID_SDK_ROOT=C:\Users\User\AppData\Local\Android\Sdk
set AVD_NAME=Adjaba_TV_Test
set PROJECT_DIR=%cd%

echo.
echo ╔════════════════════════════════════════════════════════════════╗
echo ║   ADJABA PLAYER - EMULATOR & DEPLOY AUTOMATION               ║
echo ╚════════════════════════════════════════════════════════════════╝
echo.

REM Check prerequisites
echo [1/6] Checking prerequisites...
if not exist "%ANDROID_SDK_ROOT%" (
    echo [ERROR] Android SDK not found at %ANDROID_SDK_ROOT%
    echo Please set up Android Studio first
    pause
    exit /b 1
)
echo     ✓ Android SDK found

if not exist "%PROJECT_DIR%\build.gradle" (
    echo [ERROR] Not in project root directory
    echo Expected: C:\project\adjaba-player
    pause
    exit /b 1
)
echo     ✓ Project directory verified

REM Step 1: Setup AVD
echo.
echo [2/6] Setting up lightweight Android Virtual Device...
echo     Creating: %AVD_NAME% (Android 13, x86_64, TV profile)
call setup_emulator.bat > nul 2>&1
if errorlevel 1 (
    echo [WARNING] AVD setup may have issues, continuing anyway
)
echo     ✓ AVD configured

REM Step 2: Build app
echo.
echo [3/6] Building app (clean rebuild)...
call gradlew.bat clean build -x test > build_output.log 2>&1
if errorlevel 1 (
    echo [ERROR] Build failed - see build_output.log
    type build_output.log
    pause
    exit /b 1
)
echo     ✓ Build successful

REM Step 3: Start emulator
echo.
echo [4/6] Starting emulator...
echo     Note: This runs in a separate window
start /B cmd /c run_emulator.bat

REM Wait for emulator to boot
echo     Waiting for emulator to boot (up to 2 minutes)...
timeout /t 30 /nobreak

REM Step 4: Wait for device
echo.
echo [5/6] Waiting for emulator to be ready...
:wait_loop
"%ANDROID_SDK_ROOT%\..\platform-tools\adb.exe" wait-for-device > nul 2>&1
if errorlevel 1 (
    echo     Still booting... waiting 5 more seconds
    timeout /t 5 /nobreak
    goto wait_loop
)
echo     ✓ Emulator ready

REM Step 5: Wait for system to fully boot
"%ANDROID_SDK_ROOT%\..\platform-tools\adb.exe" shell getprop sys.boot_completed > nul 2>&1
:boot_check
"%ANDROID_SDK_ROOT%\..\platform-tools\adb.exe" shell getprop sys.boot_completed 2>&1 | find /I "1" > nul
if errorlevel 1 (
    echo     System still booting... (5s more)
    timeout /t 5 /nobreak
    goto boot_check
)
echo     ✓ System fully booted

REM Step 6: Install app
echo.
echo [6/6] Installing app on emulator...
call gradlew.bat installDebug > install_output.log 2>&1
if errorlevel 1 (
    echo [ERROR] Installation failed - see install_output.log
    type install_output.log
    pause
    exit /b 1
)
echo     ✓ App installed successfully

REM Launch app
echo.
echo [LAUNCHING APP]
"%ANDROID_SDK_ROOT%\..\platform-tools\adb.exe" shell am start -n com.adjaba.adplayer/.activities.SelectScreens
timeout /t 3 /nobreak

echo.
echo ╔════════════════════════════════════════════════════════════════╗
echo ║   ✓ DEPLOYMENT COMPLETE                                       ║
echo ║                                                                ║
echo ║   App is now running on emulator!                             ║
echo ║   Test scenarios:                                             ║
echo ║   1. Select Kolkata location                                  ║
echo ║   2. Verify IST time display (UTC+5:30)                       ║
echo ║   3. Check weather displays for location                      ║
echo ║   4. Verify news images load                                  ║
echo ║   5. Landscape mode - headline doesn't overlap logo           ║
echo ║   6. Check QR codes only on ads (hidden on news/weather)      ║
echo ║                                                                ║
echo ║   View logs: ./gradlew logcat                                 ║
echo ║   Stop emulator: Stop from emulator window                    ║
echo ╚════════════════════════════════════════════════════════════════╝
echo.
pause

