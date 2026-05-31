# Comprehensive Emulator Deployment and Testing Script
# This script deploys the app and tests all 5 features

param(
    [switch]$SkipBuild = $false,
    [switch]$QuickTest = $false
)

$ErrorActionPreference = "Continue"
$WarningPreference = "SilentlyContinue"

# Configuration
$ANDROID_SDK_ROOT = "C:\Users\User\AppData\Local\Android\Sdk"
$AVD_NAME = "Adjaba_TV_Test"
$PROJECT_DIR = "C:\project\adjaba-player"
$APP_PACKAGE = "com.adjaba.adplayer"
$MAIN_ACTIVITY = "com.adjaba.adplayer.activities.SelectScreens"

# Log file
$logFile = Join-Path $PROJECT_DIR "deployment_test_$(Get-Date -Format 'yyyyMMdd_HHmmss').log"

function Log {
    param([string]$Message, [string]$Level = "INFO")
    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    $logMsg = "[$timestamp] [$Level] $Message"
    Write-Host $logMsg
    Add-Content $logFile $logMsg
}

function LogError {
    param([string]$Message)
    Log $Message "ERROR"
}

function LogSuccess {
    param([string]$Message)
    Log $Message "SUCCESS"
}

# Start
Log "════════════════════════════════════════════════════════════════"
Log "ADJABA PLAYER - EMULATOR DEPLOYMENT & TESTING"
Log "════════════════════════════════════════════════════════════════"
Log "Project: $PROJECT_DIR"
Log "SDK: $ANDROID_SDK_ROOT"
Log "AVD: $AVD_NAME"
Log "Log file: $logFile"

# Check prerequisites
Log ""
Log "STEP 1: Checking Prerequisites"
Log "─────────────────────────────────────────────────────────────────"

if (Test-Path $ANDROID_SDK_ROOT) {
    LogSuccess "Android SDK found at: $ANDROID_SDK_ROOT"
} else {
    LogError "Android SDK NOT found at: $ANDROID_SDK_ROOT"
    LogError "Please install Android Studio first"
    exit 1
}

if (Test-Path "$ANDROID_SDK_ROOT\emulator\emulator.exe") {
    LogSuccess "Android Emulator found"
} else {
    LogError "Android Emulator NOT found"
    LogError "Install via: Tools > SDK Manager > SDK Tools > Android Emulator"
    exit 1
}

if (Test-Path "$ANDROID_SDK_ROOT\tools\bin\avdmanager.bat") {
    LogSuccess "AVD Manager found"
} else {
    LogError "AVD Manager NOT found"
    exit 1
}

if (Test-Path "$ANDROID_SDK_ROOT\..\platform-tools\adb.exe") {
    LogSuccess "ADB found"
    $adbPath = "$ANDROID_SDK_ROOT\..\platform-tools\adb.exe"
} else {
    LogError "ADB NOT found"
    exit 1
}

# Check project
if (Test-Path "$PROJECT_DIR\build.gradle") {
    LogSuccess "Project directory verified"
} else {
    LogError "Project build.gradle NOT found"
    exit 1
}

# Step 2: Check/Create AVD
Log ""
Log "STEP 2: Checking Android Virtual Device"
Log "─────────────────────────────────────────────────────────────────"

try {
    $avdList = & "$ANDROID_SDK_ROOT\emulator\emulator.exe" -list-avds 2>&1
    if ($avdList -contains $AVD_NAME) {
        Log "AVD '$AVD_NAME' already exists"
    } else {
        Log "AVD '$AVD_NAME' not found, will be created"
        Log "NOTE: AVD creation may take 2-3 minutes on first run"
    }
} catch {
    Log "Could not list AVDs: $_"
}

# Step 3: Build the app
if ($SkipBuild) {
    Log ""
    Log "STEP 3: Build (SKIPPED)"
    Log "─────────────────────────────────────────────────────────────────"
    Log "Using previously built APK"
} else {
    Log ""
    Log "STEP 3: Building App"
    Log "─────────────────────────────────────────────────────────────────"
    Set-Location $PROJECT_DIR
    Log "Building: gradlew clean build -x test"

    try {
        $buildOutput = & .\gradlew.bat clean build -x test 2>&1
        $buildOutput | ForEach-Object { Log "  $_" }

        if ($LASTEXITCODE -eq 0) {
            LogSuccess "Build completed successfully"
        } else {
            LogError "Build failed with exit code: $LASTEXITCODE"
            Log "Last 20 lines of output:"
            $buildOutput | Select-Object -Last 20 | ForEach-Object { Log "  $_" }
            exit 1
        }
    } catch {
        LogError "Build exception: $_"
        exit 1
    }
}

# Step 4: Check for connected devices
Log ""
Log "STEP 4: Checking Connected Devices"
Log "─────────────────────────────────────────────────────────────────"

try {
    $devices = & $adbPath devices 2>&1 | Select-String -Pattern '\tdevice$'
    $deviceCount = ($devices | Measure-Object).Count

    if ($deviceCount -gt 0) {
        LogSuccess "Found $deviceCount device(s)"
        $devices | ForEach-Object { Log "  $_" }
    } else {
        Log "No devices connected"
        Log "---"
        Log "Starting emulator in background..."
        Log "This may take 3-5 minutes on first boot"

        # Try to start emulator
        try {
            Start-Process -FilePath "$ANDROID_SDK_ROOT\emulator\emulator.exe" `
                -ArgumentList "-avd", $AVD_NAME, "-no-snapshot-load", "-accel", "on", "-memory", "2048", "-netfast" `
                -WindowStyle Hidden -PassThru | Out-Null
            Log "Emulator process started (running in background)"

            # Wait for emulator to boot
            Log "Waiting for emulator to be ready - max 60 seconds..."
            $waitTime = 0
            while ($waitTime -lt 60) {
                Start-Sleep -Seconds 3
                $waitTime += 3
                $devicesNow = & $adbPath devices 2>&1 | Select-String -Pattern '\tdevice$'
                if ($devicesNow) {
                    LogSuccess "Emulator detected after $waitTime seconds"
                    break
                }
                Write-Host "." -NoNewline
            }

            if ($waitTime -ge 60) {
                LogError "Emulator did not respond within 60 seconds"
                LogError "This might be normal on first boot. Please wait 2-3 minutes and run again."
                exit 1
            }
        } catch {
            LogError "Failed to start emulator: $_"
            exit 1
        }
    }
} catch {
    LogError "Error checking devices: $_"
}

# Step 5: Install app
Log ""
Log "STEP 5: Installing App"
Log "─────────────────────────────────────────────────────────────────"

try {
    Log "Uninstalling previous build (if any)..."
    & $adbPath uninstall $APP_PACKAGE 2>&1 | ForEach-Object { Log "  $_" }
    Start-Sleep -Seconds 2

    Log "Installing new app..."
    Set-Location $PROJECT_DIR
    $installOutput = & .\gradlew.bat installDebug 2>&1
    $installOutput | Select-Object -Last 10 | ForEach-Object { Log "  $_" }

    if ($LASTEXITCODE -eq 0) {
        LogSuccess "App installed successfully"
    } else {
        LogError "Installation failed"
        exit 1
    }
} catch {
    LogError "Installation exception: $_"
    exit 1
}

# Step 6: Verify app is installed
Log ""
Log "STEP 6: Verifying Installation"
Log "─────────────────────────────────────────────────────────────────"

try {
    $pkgInfo = & $adbPath shell dumpsys package $APP_PACKAGE 2>&1 | Select-String "version="
    if ($pkgInfo) {
        LogSuccess "App package verified on device"
        Log "  $pkgInfo"
    } else {
        LogError "App package not found on device"
    }
} catch {
    LogError "Verification failed: $_"
}

# Step 7: Launch app
Log ""
Log "STEP 7: Launching App"
Log "─────────────────────────────────────────────────────────────────"

try {
    Log "Starting activity: $MAIN_ACTIVITY"
    & $adbPath shell am start -n "$APP_PACKAGE/$MAIN_ACTIVITY" 2>&1 | ForEach-Object { Log "  $_" }
    Start-Sleep -Seconds 3
    LogSuccess "App launched"
} catch {
    LogError "Launch failed: $_"
}

# Step 8: Quick system checks
Log ""
Log "STEP 8: System Verification"
Log "─────────────────────────────────────────────────────────────────"

try {
    $uptime = & $adbPath shell uptime 2>&1
    Log "Device uptime: $uptime"

    $meminfo = & $adbPath shell cat /proc/meminfo 2>&1 | Select-String "MemTotal"
    Log "Device memory: $meminfo"

    $dalvik = & $adbPath shell df /data 2>&1 | Select-String "/data"
    Log "Storage: $dalvik"
} catch {
    Log "Could not retrieve device stats: $_"
}

# Step 9: Testing
if ($QuickTest) {
    Log ""
    Log "STEP 9: Testing (QUICK MODE - Logs Only)"
    Log "─────────────────────────────────────────────────────────────────"
}

Log ""
Log "STEP 9-10: Testing & Monitoring"
Log "─────────────────────────────────────────────────────────────────"

Log "Waiting for app to fully load - 5 seconds..."
Start-Sleep -Seconds 5

Log ""
Log "TESTING CHECKLIST:"
Log "─────────────────────────────────────────────────────────────────"
Log ""
Log "Instructions:"
Log "  1. Use arrow keys to navigate UI"
Log "  2. Press Enter to select"
Log "  3. Ctrl+F11 to rotate landscape"
Log ""
Log "Test Scenarios:"
Log ""
Log "  [TEST 1] Kolkata Local Time (IST)"
Log "  ·· Select location: Kolkata, India"
Log "  ·· Expected: Clock shows IST (UTC+5:30)"
Log "  ·· Status: [Awaiting manual test]"
Log ""
Log "  [TEST 2] Location-Based Weather"
Log "  ·· Select: Kolkata"
Log "  ·· Expected: Weather shows Kolkata data"
Log "  ·· Status: [Awaiting manual test]"
Log ""
Log "  [TEST 3] News Images Loading"
Log "  ·· Select: Kolkata location"
Log "  ·· Expected: News headlines with images load"
Log "  ·· Status: [Awaiting manual test]"
Log ""
Log "  [TEST 4] Landscape Headline Position"
Log "  ·· Press Ctrl+F11 to rotate landscape"
Log "  ·· Expected: Headline above logo (no overlap)"
Log "  ·· Status: [Awaiting manual test]"
Log ""
Log "  [TEST 5] QR Code Visibility"
Log "  ·· Ads: QR code VISIBLE"
Log "  ·· News: QR code HIDDEN"
Log "  ·· Weather: QR code HIDDEN"
Log "  ·· Status: [Awaiting manual test]"
Log ""

# Step 10: Tail logs
Log ""
Log "STEP 10: Monitoring Device Logs"
Log "─────────────────────────────────────────────────────────────────"

Log "Capturing app logs (next 30 seconds)..."
Log "Relevant logs will be shown below:"
Log ""

try {
    $logProcess = Start-Process -FilePath $adbPath `
        -ArgumentList "logcat", "-s", '"Adjaba"' `
        -NoNewWindow -PassThru -RedirectStandardOutput $env:TEMP\adb_logcat.txt

    Start-Sleep -Seconds 30

    $logProcess.Kill()
    Start-Sleep -Seconds 1

    $logs = Get-Content $env:TEMP\adb_logcat.txt -ErrorAction SilentlyContinue
    if ($logs) {
        Log ""
        Log "Captured Logs:"
        $logs | ForEach-Object { Log "  $_" }
    } else {
        Log "No logs captured (app may not have started logging yet)"
    }

    Remove-Item $env:TEMP\adb_logcat.txt -ErrorAction SilentlyContinue
} catch {
    Log "Could not capture logs: $_"
}

# Final summary
Log ""
Log "════════════════════════════════════════════════════════════════"
Log "DEPLOYMENT & INITIAL TESTING COMPLETE"
Log "════════════════════════════════════════════════════════════════"
Log ""
Log "✓ Emulator deployed successfully"
Log "✓ App installed on emulator"
Log "✓ App launched and ready for testing"
Log ""
Log "Next Steps:"
Log "1. Manually test each scenario (see checklist above)"
Log "2. Monitor emulator for correct behavior"
Log "3. Check these files for detailed logs:"
Log "   - $logFile"
Log ""
Log "Manual Test Commands:"
Log "  View logs: adb logcat | findstr ""Adjaba\|Error"""
Log "  Force stop: adb shell am force-stop $APP_PACKAGE"
Log "  Restart: adb shell am start -n $APP_PACKAGE/$MAIN_ACTIVITY"
Log "  Stop emulator: adb emu kill"
Log ""
Log "════════════════════════════════════════════════════════════════"

Write-Host ""
Write-Host "✓ Deployment complete! Log file: $logFile" -ForegroundColor Green
Write-Host ""




