@echo off
echo ================================================
echo Building Smart Playlist Sync APK...
echo ================================================
cd /d C:\project\adjaba-player
call gradlew.bat assembleDebug --console=plain --no-daemon
echo.
echo ================================================
if exist app\build\outputs\apk\debug\app-debug.apk (
    echo BUILD SUCCESS!
    for %%A in (app\build\outputs\apk\debug\app-debug.apk) do echo APK Size: %%~zA bytes
    echo APK Location: app\build\outputs\apk\debug\app-debug.apk
) else (
    echo BUILD FAILED - APK not found
)
echo ================================================

