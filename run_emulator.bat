@echo off
REM Start lightweight Android emulator for Adjaba Player testing
REM Uses minimal specs for fast boot on development machine

REM Set environment
set ANDROID_SDK_ROOT=C:\Users\User\AppData\Local\Android\Sdk
set ANDROID_HOME=%ANDROID_SDK_ROOT%
set AVD_NAME=Adjaba_TV_Test

echo.
echo ===== STARTING EMULATOR =====
echo.
echo AVD: %AVD_NAME%
echo This may take 2-5 minutes on first boot
echo.

REM Start emulator with optimizations
"%ANDROID_SDK_ROOT%\emulator\emulator.exe" ^
    -avd "%AVD_NAME%" ^
    -no-snapshot-load ^
    -accel on ^
    -memory 2048 ^
    -partition-size 1024 ^
    -netfast ^
    -qemu -enable-kvm

if errorlevel 1 (
    echo.
    echo [ERROR] Failed to start emulator
    echo Check that you have:
    echo   1. Android Virtual Device: %AVD_NAME% created
    echo   2. System image installed (android-33 or android-32)
    echo   3. Sufficient disk space (5GB minimum)
    echo.
    echo To create AVD, run: setup_emulator.bat
    pause
    exit /b 1
)

echo.
echo ===== EMULATOR RUNNING =====
echo Waiting for boot to complete (2-3 minutes)...
echo.
timeout /t 120 /nobreak

echo.
echo Checking device connectivity...
"%ANDROID_SDK_ROOT%\..\platform-tools\adb.exe" devices

pause

