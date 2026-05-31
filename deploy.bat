@echo off
REM Deployment script for Adjaba Player
setlocal enabledelayedexpansion

set ADB="C:\Users\User\AppData\Local\Android\Sdk\platform-tools\adb.exe"
set APK="C:\project\adjaba-player\app\build\outputs\apk\debug\app-debug.apk"

echo.
echo ========================================
echo Adjaba Player - APK Deployment
echo ========================================
echo.

REM Check if adb exists
if not exist %ADB% (
    echo ERROR: adb not found at %ADB%
    exit /b 1
)

REM Check if APK exists
if not exist %APK% (
    echo ERROR: APK not found at %APK%
    exit /b 1
)

echo Checking connected devices...
%ADB% devices
echo.

echo Installing APK to connected device...
%ADB% install -r %APK%

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo SUCCESS: APK installed successfully!
    echo ========================================
    echo.
    echo Starting app...
    %ADB% shell am start -n com.adjaba/.activities.SelectScreens
) else (
    echo.
    echo ========================================
    echo ERROR: Installation failed!
    echo ========================================
)

pause

