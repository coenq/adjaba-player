# 🔍 DIAGNOSTIC SCRIPT - Run this to debug the ads issue

## Step 1: Clear everything
echo "Clearing app data..."
adb shell pm clear com.adjaba
adb logcat -c
echo "Done!"

## Step 2: Start fresh
echo "Restarting app..."
adb shell am start -n com.adjaba/.activities.LoginActivity
sleep 3

## Step 3: Capture logs
echo "Starting log capture (running for 60 seconds)..."
adb logcat > adjaba_debug_$(date +%s).txt &
LOGCAT_PID=$!

echo ""
echo "==================================="
echo "PERFORM THIS TEST SCENARIO:"
echo "==================================="
echo "1. Wait for login screen"
echo "2. Enter: boss / password"
echo "3. Press Login"
echo "4. Wait for SelectScreens"
echo "5. Select Landscape"
echo "6. Select Demo136"
echo "7. Press PLAY"
echo "8. Watch for 45 seconds"
echo ""
echo "Waiting for you to complete test..."
sleep 45

## Step 4: Stop log capture
kill $LOGCAT_PID
wait $LOGCAT_PID 2>/dev/null

## Step 5: Generate report
LOG_FILE=$(ls -t adjaba_debug_*.txt | head -1)
echo ""
echo "==================================="
echo "DEBUG REPORT"
echo "==================================="
echo ""
echo "Log file: $LOG_FILE"
echo ""
echo "--- ERROR MESSAGES ---"
grep -i "error\|exception\|failed\|❌" $LOG_FILE | head -20
echo ""
echo "--- API CALLS ---"
grep "API_CALL\|API_RESPONSE\|getAds\|getAdsByScreen" $LOG_FILE
echo ""
echo "--- ADS RECEIVED ---"
grep "ADS_RECEIVED\|Ads received" $LOG_FILE
echo ""
echo "--- PLAYBACK ---"
grep "PLAYBACK\|Launching\|Playing" $LOG_FILE
echo ""
echo "Full log saved to: $LOG_FILE"


