@echo off
setlocal enabledelayedexpansion

set ADB_PATH=C:\Users\User\AppData\Local\Android\Sdk\platform-tools\adb.exe
set APK_PATH=C:\project\adjaba-player\app\build\outputs\apk\debug\app-debug.apk
set LOG_FILE=C:\project\adjaba-player\deploy_log.txt

REM Clear log file
echo. > %LOG_FILE%

REM Restart ADB server
echo [%date% %time%] Restarting ADB server... >> %LOG_FILE%
%ADB_PATH% kill-server >> %LOG_FILE% 2>&1
timeout /t 2 /nobreak
%ADB_PATH% start-server >> %LOG_FILE% 2>&1
timeout /t 2 /nobreak

REM Check devices
echo [%date% %time%] Checking connected devices... >> %LOG_FILE%
%ADB_PATH% devices -l >> %LOG_FILE% 2>&1

REM Install APK
echo [%date% %time%] Installing APK... >> %LOG_FILE%
%ADB_PATH% install -r "%APK_PATH%" >> %LOG_FILE% 2>&1

echo [%date% %time%] Installation complete >> %LOG_FILE%

REM Display log
type %LOG_FILE%

