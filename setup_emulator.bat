@echo off
REM Setup Lightweight Android Emulator for Adjaba Player Testing
REM This script creates a minimal Android emulator for development

REM Set SDK path (from local.properties)
set ANDROID_SDK_ROOT=C:\Users\User\AppData\Local\Android\Sdk
set ANDROID_HOME=%ANDROID_SDK_ROOT%

REM Check if SDK exists
if not exist "%ANDROID_SDK_ROOT%\emulator\emulator.exe" (
    echo [ERROR] Android emulator not found at %ANDROID_SDK_ROOT%\emulator\emulator.exe
    echo Please install Android Emulator through Android Studio:
    echo   - Open Android Studio
    echo   - Go to Tools ^> SDK Manager ^> SDK Tools
    echo   - Check "Android Emulator" and click OK
    pause
    exit /b 1
)

echo.
echo ===== ADJABA PLAYER - EMULATOR SETUP =====
echo.
echo SDK Path: %ANDROID_SDK_ROOT%
echo.

REM List existing AVDs
echo [STEP 1] Checking existing Android Virtual Devices...
"%ANDROID_SDK_ROOT%\emulator\emulator.exe" -list-avds

REM Create lightweight AVD if needed
set AVD_NAME=Adjaba_TV_Test
set SYSTEM_IMAGE=android-33

echo.
echo [STEP 2] Creating lightweight AVD: %AVD_NAME%
echo.

REM Create AVD with minimal specs
"%ANDROID_SDK_ROOT%\tools\bin\avdmanager.bat" create avd ^
    -n "%AVD_NAME%" ^
    -k "system-images;android-33;default;x86_64" ^
    -d "tv_1080p" ^
    --force

if errorlevel 1 (
    echo.
    echo [WARNING] AVD creation may have encountered issues
    echo Trying alternative method...

    REM Try with API 32 if 33 fails
    "%ANDROID_SDK_ROOT%\tools\bin\avdmanager.bat" create avd ^
        -n "%AVD_NAME%" ^
        -k "system-images;android-32;default;x86_64" ^
        -d "tv_1080p" ^
        --force
)

echo.
echo [STEP 3] Creating emulator config for performance...
echo.

REM Create .android directory if needed
if not exist "%userprofile%\.android" mkdir "%userprofile%\.android"

REM Create emulator configuration for lightweight setup
set AVD_CONFIG=%userprofile%\.android\avd\%AVD_NAME%.avd\config.ini

if exist "%AVD_CONFIG%" (
    echo # Lightweight AVD Configuration >> "%AVD_CONFIG%"
    echo hw.audioInput=no >> "%AVD_CONFIG%"
    echo hw.audioOutput=no >> "%AVD_CONFIG%"
    echo hw.camera.back=none >> "%AVD_CONFIG%"
    echo hw.camera.front=none >> "%AVD_CONFIG%"
    echo hw.gpu.enabled=yes >> "%AVD_CONFIG%"
    echo hw.gpu.mode=host >> "%AVD_CONFIG%"
    echo hw.ramSize=2048 >> "%AVD_CONFIG%"
    echo hw.vm.heapSize=512 >> "%AVD_CONFIG%"
)

echo.
echo ===== SETUP COMPLETE =====
echo.
echo Next steps:
echo   1. Start emulator: run_emulator.bat
echo   2. Build app: ./gradlew clean build
echo   3. Install app: ./gradlew installDebug
echo   4. Test features:
echo      - Kolkata: Should show IST time
echo      - Weather: Should fetch for location
echo      - News: Should display images
echo      - Headlines: Should not overlap logo
echo      - QR codes: Only on ads
echo.
pause

