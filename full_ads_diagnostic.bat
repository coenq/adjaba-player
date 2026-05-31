@echo off
setlocal enabledelayedexpansion

set ADB=C:\Users\User\AppData\Local\Android\Sdk\platform-tools\adb.exe
set PKG=com.adjaba
set DB_PATH=/data/data/com.adjaba/databases/adbase
set LOG=C:\project\adjaba-player\ads_full_diagnostic.txt

echo ========================================== > %LOG%
echo COMPLETE ADS DIAGNOSTIC FOR SCREEN: kolk737 >> %LOG%
echo ========================================== >> %LOG%
echo Timestamp: %date% %time% >> %LOG%
echo. >> %LOG%

echo [1] COUNTING TOTAL ADS FOR SCREEN kolk737 >> %LOG%
echo. >> %LOG%
%ADB% shell "run-as %PKG% sqlite3 %DB_PATH% 'SELECT COUNT(*) FROM watchingModels WHERE screenId=''kolk737'';'" >> %LOG% 2>&1

echo. >> %LOG%
echo [2] LISTING ALL ADS FOR SCREEN kolk737 >> %LOG%
echo. >> %LOG%
%ADB% shell "run-as %PKG% sqlite3 %DB_PATH% 'SELECT advertId, adContractData, mediaPath, urlPath FROM watchingModels WHERE screenId=''kolk737'' LIMIT 20;'" >> %LOG% 2>&1

echo. >> %LOG%
echo [3] CHECKING MEDIA FILES DOWNLOADED >> %LOG%
echo. >> %LOG%
%ADB% shell "run-as %PKG% ls -la /data/data/%PKG%/files/ | head -20" >> %LOG% 2>&1

echo. >> %LOG%
echo [4] CHECKING APP LOGS FOR DOWNLOAD ERRORS >> %LOG%
echo. >> %LOG%
%ADB% logcat -d *:E *:W | findstr /i "adjaba media download" >> %LOG% 2>&1

echo. >> %LOG%
echo [5] DATABASE INFO >> %LOG%
echo. >> %LOG%
%ADB% shell "run-as %PKG% sqlite3 %DB_PATH% '.tables'" >> %LOG% 2>&1

echo. >> %LOG%
echo ========================================== >> %LOG%
echo DIAGNOSTIC COMPLETE >> %LOG%
echo ========================================== >> %LOG%

type %LOG%
pause

