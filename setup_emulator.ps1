# Adjaba Player - Emulator Setup PowerShell Script
# Run as: powershell -ExecutionPolicy Bypass -File setup_emulator.ps1

$ErrorActionPreference = "Stop"
$WarningPreference = "SilentlyContinue"

# Configuration
$ANDROID_SDK_ROOT = "C:\Users\User\AppData\Local\Android\Sdk"
$AVD_NAME = "Adjaba_TV_Test"
$SYSTEM_IMAGE = "system-images;android-33;default;x86_64"
$DEVICE_TYPE = "tv_1080p"

Write-Host ""
Write-Host "╔════════════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║   ADJABA PLAYER - EMULATOR SETUP (PowerShell Edition)        ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""

# Check prerequisites
Write-Host "[1] Checking prerequisites..." -ForegroundColor Yellow

if (-not (Test-Path "$ANDROID_SDK_ROOT")) {
    Write-Host "[ERROR] Android SDK not found at: $ANDROID_SDK_ROOT" -ForegroundColor Red
    Write-Host "Please install Android Studio first" -ForegroundColor Red
    exit 1
}
Write-Host "    ✓ Android SDK found" -ForegroundColor Green

if (-not (Test-Path "$ANDROID_SDK_ROOT\emulator\emulator.exe")) {
    Write-Host "[ERROR] Emulator not found. Install via Android Studio > SDK Manager > SDK Tools" -ForegroundColor Red
    exit 1
}
Write-Host "    ✓ Emulator installed" -ForegroundColor Green

if (-not (Test-Path "$ANDROID_SDK_ROOT\tools\bin\avdmanager.bat")) {
    Write-Host "[ERROR] AVD Manager not found" -ForegroundColor Red
    exit 1
}
Write-Host "    ✓ AVD Manager available" -ForegroundColor Green

# List existing AVDs
Write-Host ""
Write-Host "[2] Existing Android Virtual Devices:" -ForegroundColor Yellow
& "$ANDROID_SDK_ROOT\emulator\emulator.exe" -list-avds 2>&1

# Check if AVD already exists
$existingAvds = & "$ANDROID_SDK_ROOT\emulator\emulator.exe" -list-avds 2>&1 | Select-String $AVD_NAME
if ($existingAvds) {
    Write-Host ""
    Write-Host "[INFO] AVD '$AVD_NAME' already exists" -ForegroundColor Cyan
    $createNew = Read-Host "Create new one anyway? (y/n)"
    if ($createNew -ne "y") {
        Write-Host "Using existing AVD: $AVD_NAME" -ForegroundColor Green
        Write-Host ""
        Write-Host "Next steps:" -ForegroundColor Cyan
        Write-Host "  1. Start emulator: .\run_emulator.bat" -ForegroundColor Gray
        Write-Host "  2. Build: gradlew clean build" -ForegroundColor Gray
        Write-Host "  3. Install: gradlew installDebug" -ForegroundColor Gray
        Write-Host ""
        exit 0
    }
}

# Create AVD
Write-Host ""
Write-Host "[3] Creating Android Virtual Device: $AVD_NAME" -ForegroundColor Yellow
Write-Host "    Profile: TV 1080p"
Write-Host "    System: Android 13"
Write-Host "    Architecture: x86_64"
Write-Host ""

$avdManagerPath = "$ANDROID_SDK_ROOT\tools\bin\avdmanager.bat"
$avdConfigDir = "$env:USERPROFILE\.android\avd\$AVD_NAME.avd"

# Create new AVD with StdIn input
$createCmd = @"
y
"@

Write-Host "    Running AVD Manager..." -ForegroundColor Gray
$createCmd | & $avdManagerPath create avd `
    -n $AVD_NAME `
    -k $SYSTEM_IMAGE `
    -d $DEVICE_TYPE `
    --force 2>&1 | ForEach-Object { Write-Host "      $_" }

# Verify creation
if (Test-Path "$avdConfigDir\config.ini") {
    Write-Host "    ✓ AVD created successfully" -ForegroundColor Green
} else {
    Write-Host "    [WARNING] AVD might not have created properly, continuing..." -ForegroundColor Yellow
}

# Configure for lightweight performance
Write-Host ""
Write-Host "[4] Optimizing AVD configuration..." -ForegroundColor Yellow

$configFile = "$avdConfigDir\config.ini"
if (Test-Path $configFile) {
    Write-Host "    Updating: $configFile" -ForegroundColor Gray

    # Add performance settings
    $config = Get-Content $configFile
    $config = $config -replace "^hw.gpu.enabled=.*", "hw.gpu.enabled=yes"
    $config = $config -replace "^hw.ramSize=.*", "hw.ramSize=2048"

    # Append if not exists
    if ($config -notmatch "hw.gpu.enabled") {
        $config += "`nhw.gpu.enabled=yes"
    }
    if ($config -notmatch "hw.ramSize") {
        $config += "`nhw.ramSize=2048"
    }
    if ($config -notmatch "hw.audioInput") {
        $config += "`nhw.audioInput=no"
    }
    if ($config -notmatch "hw.audioOutput") {
        $config += "`nhw.audioOutput=no"
    }
    if ($config -notmatch "hw.camera.back") {
        $config += "`nhw.camera.back=none"
    }
    if ($config -notmatch "hw.camera.front") {
        $config += "`nhw.camera.front=none"
    }

    $config | Set-Content $configFile
    Write-Host "    ✓ Configuration optimized" -ForegroundColor Green
}

Write-Host ""
Write-Host "╔════════════════════════════════════════════════════════════════╗" -ForegroundColor Green
Write-Host "║   ✓ SETUP COMPLETE                                           ║" -ForegroundColor Green
Write-Host "╚════════════════════════════════════════════════════════════════╝" -ForegroundColor Green
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Cyan
Write-Host ""
Write-Host "  1. Start the emulator:" -ForegroundColor Gray
Write-Host "     .\run_emulator.bat" -ForegroundColor Yellow
Write-Host ""
Write-Host "  2. In another terminal, build the app:" -ForegroundColor Gray
Write-Host "     gradlew clean build" -ForegroundColor Yellow
Write-Host ""
Write-Host "  3. Install to emulator:" -ForegroundColor Gray
Write-Host "     gradlew installDebug" -ForegroundColor Yellow
Write-Host ""
Write-Host "  4. Or run the full automation:" -ForegroundColor Gray
Write-Host "     .\deploy_to_emulator.bat" -ForegroundColor Yellow
Write-Host ""
Write-Host "AVD Name: $AVD_NAME" -ForegroundColor Cyan
Write-Host "Config: $configFile" -ForegroundColor Cyan
Write-Host ""

