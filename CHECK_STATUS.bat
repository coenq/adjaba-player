@echo off
REM Quick status check of deployment

echo === DEPLOYMENT STATUS CHECK ===
echo Time: %date% %time%
echo.

set SDK=C:\Users\User\AppData\Local\Android\Sdk

echo [Checking Build]
if exist "app\build\outputs\apk\debug\app-debug.apk" (
    echo ✓ APK exists - BUILD COMPLETE
    for %%F in (app\build\outputs\apk\debug\app-debug.apk) do echo   Size: %%~zF bytes
) else (
    echo ✗ APK not found - BUILD IN PROGRESS or FAILED
)

echo.
echo [Checking Devices]
"%SDK%\..\platform-tools\adb.exe" devices

echo.
echo [Checking Emulator]
tasklist | findstr emulator > nul && (
    echo ✓ Emulator process running
) || (
    echo ✗ Emulator process not found
)

echo.
echo [Checking App Installation]
"%SDK%\..\platform-tools\adb.exe" shell pm list packages | findstr "adjaba" > nul && (
    echo ✓ App package found on device
) || (
    echo ✗ App package not found on device
)

echo.
echo Done.
pause

