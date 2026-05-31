@echo off
echo Building Smart Playlist Sync APK...
cd /d C:\project\adjaba-player
call gradlew.bat --no-daemon clean assembleDebug > build_smart_sync.log 2>&1
echo Build complete. Check build_smart_sync.log for details.
echo.
echo Last 30 lines of build log:
type build_smart_sync.log | findstr /v /c:"^[[:space:]]*$" | findstr /n "^" | findstr "[0-9]*:." | findstr /v "Download" | tail -30
pause

