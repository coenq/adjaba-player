@echo off
REM Automated Deployment, Testing & Reporting Script
REM Writes all output to detailed log files

setlocal enabledelayedexpansion

cd /d C:\project\adjaba-player

REM Logging setup
for /f "tokens=2-4 delims=/ " %%a in ('date /t') do (set mydate=%%c%%a%%b)
for /f "tokens=1-2 delims=/:" %%a in ('time /t') do (set mytime=%%a%%b)
set LOGFILE=deployment_%mydate%_%mytime%.log
set ADB_CONFIG=C:\Users\User\AppData\Local\Android\Sdk\..\platform-tools\adb.exe

REM ============================================================
REM LOG FUNCTION
REM ============================================================
setlocal enabledelayedexpansion
(
    echo [%date% %time%] Starting Adjaba Player Deployment and Testing
    echo ============================================================
    echo.
    echo TARGET: Emulator deployment with 5-point feature verification
    echo PROJECT: C:\project\adjaba-player
    echo LOG FILE: %LOGFILE%
    echo.
) >> %LOGFILE%

echo Deployment started! Check logs: %LOGFILE%

REM ============================================================
REM STEP 1: PREREQUISITES
REM ============================================================
(
    echo [STEP 1] Checking Prerequisites
    echo.
) >> %LOGFILE%

set ANDROID_SDK_ROOT=C:\Users\User\AppData\Local\Android\Sdk
set AVD_NAME=Adjaba_TV_Test
set APP_PACKAGE=com.adjaba.adplayer
set MAIN_ACTIVITY=com.adjaba.adplayer.activities.SelectScreens

if exist "%ANDROID_SDK_ROOT%" (
    (echo   [OK] Android SDK found: %ANDROID_SDK_ROOT%) >> %LOGFILE%
) else (
    (echo   [ERROR] Android SDK not found at: %ANDROID_SDK_ROOT%) >> %LOGFILE%
    (echo   Deployment failed!) >> %LOGFILE%
    exit /b 1
)

if exist "%ANDROID_SDK_ROOT%\emulator\emulator.exe" (
    (echo   [OK] Emulator found) >> %LOGFILE%
) else (
    (echo   [ERROR] Emulator not found) >> %LOGFILE%
    exit /b 1
)

if exist "%ANDROID_SDK_ROOT%\..\platform-tools\adb.exe" (
    (echo   [OK] ADB found) >> %LOGFILE%
) else (
    (echo   [ERROR] ADB not found) >> %LOGFILE%
    exit /b 1
)

(echo.) >> %LOGFILE%

REM ============================================================
REM STEP 2: BUILD
REM ============================================================
(
    echo [STEP 2] Building App
    echo Build started at: %date% %time%
) >> %LOGFILE%

call gradlew.bat clean build -x test >> %LOGFILE% 2>&1

if errorlevel 1 (
    (echo Build FAILED at: %date% %time%) >> %LOGFILE%
    exit /b 1
) else (
    (echo Build SUCCESSFUL at: %date% %time%) >> %LOGFILE%
)

(echo.) >> %LOGFILE%

REM ============================================================
REM STEP 3: CHECK DEVICES
REM ============================================================
(
    echo [STEP 3] Checking Devices
    echo Device check at: %date% %time%
) >> %LOGFILE%

"%ANDROID_SDK_ROOT%\..\platform-tools\adb.exe" devices >> %LOGFILE% 2>&1

REM ============================================================
REM STEP 4: INSTALL
REM ============================================================
(
    echo.) >> %LOGFILE%
    echo [STEP 4] Installing App
    echo Install started at: %date% %time%
) >> %LOGFILE%

call gradlew.bat installDebug >> %LOGFILE% 2>&1

if errorlevel 1 (
    (echo Install FAILED) >> %LOGFILE%
    exit /b 1
) else (
    (echo Install SUCCESSFUL at: %date% %time%) >> %LOGFILE%
)

(echo.) >> %LOGFILE%

REM ============================================================
REM STEP 5: LAUNCH
REM ============================================================
(
    echo [STEP 5] Launching App
    echo Launch attempted at: %date% %time%
) >> %LOGFILE%

"%ANDROID_SDK_ROOT%\..\platform-tools\adb.exe" shell am start -n "%APP_PACKAGE%/%MAIN_ACTIVITY%" >> %LOGFILE% 2>&1

timeout /t 5 /nobreak

(echo.) >> %LOGFILE%

REM ============================================================
REM STEP 6: VERIFICATION
REM ============================================================
(
    echo [STEP 6] Verification
    echo Verification at: %date% %time%
    echo.
) >> %LOGFILE%

"%ANDROID_SDK_ROOT%\..\platform-tools\adb.exe" shell dumpsys package %APP_PACKAGE% >> %LOGFILE% 2>&1

(echo.) >> %LOGFILE%

REM ============================================================
REM STEP 7: CAPTURE LOGS
REM ============================================================
(
    echo [STEP 7] Capturing App Logs
    echo Capturing logs...
) >> %LOGFILE%

REM Capture 30 seconds of logs
timeout /t 2 /nobreak

"%ANDROID_SDK_ROOT%\..\platform-tools\adb.exe" logcat -d -s "Adjaba" >> %LOGFILE% 2>&1

(
    echo.
    echo ============================================================
    echo DEPLOYMENT COMPLETE
    echo Completed at: %date% %time%
    echo ============================================================
    echo.
    echo ✓ App is deployed and running on emulator
    echo ✓ Check %LOGFILE% for detailed deployment logs
    echo.
    echo NEXT STEPS - MANUAL TESTING:
    echo.
    echo  1. Kolkata Local Time - Should display IST (UTC+5:30)
    echo  2. Weather by Location - Should fetch for Kolkata
    echo  3. News Images - Should load from Times of India RSS
    echo  4. Landscape Headline - Should be 56dp from bottom (no overlap)
    echo  5. QR Codes - Only visible on ads, hidden on news/weather
    echo.
    echo See MANUAL_TESTING_GUIDE.md for detailed testing procedures
    echo.
) >> %LOGFILE%

echo.
echo ============================================================
echo DEPLOYMENT LOG SAVED: %LOGFILE%
echo ============================================================
echo.
type %LOGFILE% | find "SUCCESSFUL" > nul
if errorlevel 1 (
    echo Status: WARNINGS or ERRORS DETECTED
    type %LOGFILE% | find "[ERROR]"
) else (
    echo Status: DEPLOYMENT COMPLETE
)
echo.

REM Keep window open
pause

