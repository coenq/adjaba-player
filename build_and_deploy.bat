@echo off
echo ================================================
echo BUILDING SMART PLAYLIST SYNC APK
echo ================================================
cd /d C:\project\adjaba-player

echo Cleaning previous build...
call gradlew.bat clean

echo.
echo Building debug APK with smart sync features...
call gradlew.bat assembleDebug --no-daemon --console=plain

echo.
echo ================================================
if exist app\build\outputs\apk\debug\app-debug.apk (
    echo BUILD SUCCESS!
    for %%A in (app\build\outputs\apk\debug\app-debug.apk) do (
        echo APK Size: %%~zA bytes
        echo Last Modified: %%~tA
    )
    echo.
    echo APK Location: app\build\outputs\apk\debug\app-debug.apk
    echo.
    echo Deploying to device...
    C:\Users\User\AppData\Local\Android\Sdk\platform-tools\adb.exe install -r app\build\outputs\apk\debug\app-debug.apk
    echo.
    echo DEPLOYMENT COMPLETE!
) else (
    echo BUILD FAILED - APK not found
    echo Check gradlew output above for errors
)
echo ================================================
pause

