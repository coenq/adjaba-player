@echo off
setlocal enabledelayedexpansion

set ADB=C:\Users\User\AppData\Local\Android\Sdk\platform-tools\adb.exe
set PKG=com.adjaba
set LOG=C:\project\adjaba-player\kolk737_ads_query.txt

echo ============================================ > %LOG%
echo ADS QUERY FOR SCREEN: kolk737 >> %LOG%
echo ============================================ >> %LOG%
echo Timestamp: %date% %time% >> %LOG%
echo. >> %LOG%

echo [1] TOTAL ADS COUNT >> %LOG%
echo. >> %LOG%
%ADB% shell "run-as %PKG% cat /data/data/%PKG%/databases/adbase | strings | findstr kolk737 | head -30" >> %LOG% 2>&1

echo. >> %LOG%
echo [2] CHECKING DATAHOLDER LOGS >> %LOG%
echo. >> %LOG%
%ADB% logcat -d -s AdvertWatching:I AdvertWatching:W AdvertWatching:E | findstr /i "allAds Starting playback Total items kolk737" >> %LOG% 2>&1

echo. >> %LOG%
echo [3] CHECKING SELECTSCREENS LOGS >> %LOG%
echo. >> %LOG%
%ADB% logcat -d -s SelectScreens:I SelectScreens:W SelectScreens:E | findstr /i "downloaded ads kolk737 getAds" >> %LOG% 2>&1

echo. >> %LOG%
echo [4] CHECKING REFRESH LOGS >> %LOG%
echo. >> %LOG%
%ADB% logcat -d | findstr /i "refreshRunnable getWeather refresh" | head -20 >> %LOG% 2>&1

echo. >> %LOG%
echo ============================================ >> %LOG%
echo QUERY COMPLETE >> %LOG%
echo ============================================ >> %LOG%

type %LOG%
pause

