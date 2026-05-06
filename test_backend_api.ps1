# ====================================================
# Adjaba API Backend Test Script
# ====================================================
# Tests: Login → Get Screens → Get Playlist → Download Ads
# ====================================================

$baseUrl = "https://api.adjaba.in"
$username = "boss"
$password = "password"

Write-Host "`n╔════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║  ADJABA API BACKEND TEST - May 5, 2026           ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════════════════╝`n" -ForegroundColor Cyan

# ====================================================
# STEP 1: LOGIN
# ====================================================
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Yellow
Write-Host "🔐 STEP 1: Testing Login Endpoint" -ForegroundColor Yellow
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━`n" -ForegroundColor Yellow

$loginUrl = "$baseUrl/v2/authenticate_user"
$loginBody = @{
    userId = $username
    password = $password
} | ConvertTo-Json

Write-Host "  📍 Endpoint: POST $loginUrl" -ForegroundColor Gray
Write-Host "  📦 Body: $loginBody" -ForegroundColor Gray
Write-Host ""

try {
    $loginResponse = Invoke-RestMethod -Uri $loginUrl `
                                       -Method Post `
                                       -Body $loginBody `
                                       -ContentType "application/json" `
                                       -ErrorAction Stop

    Write-Host "  ✅ LOGIN SUCCESS!" -ForegroundColor Green
    Write-Host "  ├─ User ID: $($loginResponse.userid)" -ForegroundColor White
    Write-Host "  ├─ Email: $($loginResponse.email)" -ForegroundColor White
    Write-Host "  ├─ Message: $($loginResponse.message)" -ForegroundColor White
    Write-Host "  └─ Token: $($loginResponse.loginToken.Substring(0, [Math]::Min(50, $loginResponse.loginToken.Length)))..." -ForegroundColor White

    $token = $loginResponse.loginToken

} catch {
    Write-Host "  ❌ LOGIN FAILED!" -ForegroundColor Red
    Write-Host "  └─ Error: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "  └─ Status: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
    exit 1
}

# ====================================================
# STEP 2: GET SCREENS
# ====================================================
Write-Host "`n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Yellow
Write-Host "📺 STEP 2: Getting User Screens" -ForegroundColor Yellow
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━`n" -ForegroundColor Yellow

$screensUrl = "$baseUrl/get_screen_by_user"
Write-Host "  📍 Endpoint: GET $screensUrl" -ForegroundColor Gray
Write-Host "  🔑 Auth: Bearer $($token.Substring(0, [Math]::Min(30, $token.Length)))..." -ForegroundColor Gray
Write-Host ""

try {
    $headers = @{
        "Authorization" = "Bearer $token"
    }

    $screensResponse = Invoke-RestMethod -Uri $screensUrl `
                                        -Method Get `
                                        -Headers $headers `
                                        -ErrorAction Stop

    Write-Host "  ✅ SCREENS RETRIEVED!" -ForegroundColor Green
    Write-Host "  └─ Total Screens: $($screensResponse.Count)" -ForegroundColor White
    Write-Host ""

    if ($screensResponse.Count -eq 0) {
        Write-Host "  ⚠️ No screens found for this user!" -ForegroundColor Yellow
        exit 1
    }

    # Display screens
    for ($i = 0; $i -lt $screensResponse.Count; $i++) {
        $screen = $screensResponse[$i]
        Write-Host "  📺 Screen $($i + 1):" -ForegroundColor Cyan
        Write-Host "     ├─ Screen ID: $($screen.screenId)" -ForegroundColor White
        Write-Host "     ├─ Name: $($screen.screenName)" -ForegroundColor White
        Write-Host "     ├─ Location: $($screen.location)" -ForegroundColor White
        Write-Host "     ├─ Orientation: $($screen.orientation)" -ForegroundColor White
        Write-Host "     └─ Device: $($screen.screenDevice)" -ForegroundColor White
        Write-Host ""
    }

    # Pick first screen for testing
    $testScreenId = $screensResponse[0].screenId
    Write-Host "  🎯 Test Target: Screen ID = $testScreenId" -ForegroundColor Magenta

} catch {
    Write-Host "  ❌ GET SCREENS FAILED!" -ForegroundColor Red
    Write-Host "  └─ Error: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        Write-Host "  └─ Status: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
    }
    exit 1
}

# ====================================================
# STEP 3: GET PLAYLIST (ADS)
# ====================================================
Write-Host "`n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Yellow
Write-Host "🎬 STEP 3: Getting Screen Playlist (Ads)" -ForegroundColor Yellow
Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━`n" -ForegroundColor Yellow

# Handle screenId with "/" (e.g., "Demo136/L")
$cleanScreenId = if ($testScreenId.Contains("/")) {
    $testScreenId.Split("/")[0]
} else {
    $testScreenId
}

$playlistUrl = "$baseUrl/get_screen_playlists/$cleanScreenId"
Write-Host "  📍 Endpoint: GET $playlistUrl" -ForegroundColor Gray
Write-Host "  🔑 Auth: Bearer $($token.Substring(0, [Math]::Min(30, $token.Length)))..." -ForegroundColor Gray
Write-Host ""

try {
    $playlistResponse = Invoke-RestMethod -Uri $playlistUrl `
                                         -Method Get `
                                         -Headers $headers `
                                         -ErrorAction Stop

    Write-Host "  ✅ PLAYLIST RETRIEVED!" -ForegroundColor Green
    Write-Host "  └─ Total Ads: $($playlistResponse.Count)" -ForegroundColor White
    Write-Host ""

    if ($playlistResponse.Count -eq 0) {
        Write-Host "  ⚠️ No ads in playlist for screen $cleanScreenId" -ForegroundColor Yellow
        Write-Host "  └─ This is why the app shows only weather/news!" -ForegroundColor Yellow
    } else {
        # Display ads
        for ($i = 0; $i -lt [Math]::Min(5, $playlistResponse.Count); $i++) {
            $ad = $playlistResponse[$i]
            Write-Host "  🎥 Ad $($i + 1):" -ForegroundColor Cyan
            Write-Host "     ├─ Advert ID: $($ad.adContractData.advertId)" -ForegroundColor White
            Write-Host "     ├─ Type: $($ad.typeAdvert)" -ForegroundColor White
            Write-Host "     ├─ Duration: $($ad.adContractData.duration)s" -ForegroundColor White
            Write-Host "     ├─ Media Path: $($ad.adContractData.path)" -ForegroundColor White
            Write-Host "     └─ Contract ID: $($ad.adContractData.contractId)" -ForegroundColor White
            Write-Host ""
        }

        if ($playlistResponse.Count -gt 5) {
            Write-Host "  ... and $($playlistResponse.Count - 5) more ads" -ForegroundColor Gray
            Write-Host ""
        }

        # Test downloading first ad
        $firstAd = $playlistResponse[0]
        $mediaPath = $firstAd.adContractData.path

        if ($mediaPath -and $mediaPath -ne "") {
            Write-Host "`n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Yellow
            Write-Host "📥 STEP 4: Testing Media Download" -ForegroundColor Yellow
            Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━`n" -ForegroundColor Yellow

            $mediaUrl = "$baseUrl/media/$mediaPath"
            Write-Host "  📍 Endpoint: GET $mediaUrl" -ForegroundColor Gray
            Write-Host "  🔑 Auth: Bearer $($token.Substring(0, [Math]::Min(30, $token.Length)))..." -ForegroundColor Gray
            Write-Host ""

            try {
                $mediaResponse = Invoke-RestMethod -Uri $mediaUrl `
                                                  -Method Get `
                                                  -Headers $headers `
                                                  -ErrorAction Stop

                Write-Host "  ✅ MEDIA URL RETRIEVED!" -ForegroundColor Green
                Write-Host "  └─ Presigned URL:" -ForegroundColor White
                Write-Host "     $($mediaResponse.url.Substring(0, [Math]::Min(100, $mediaResponse.url.Length)))..." -ForegroundColor Gray
                Write-Host ""

                # Try to download actual media file
                Write-Host "  🌐 Attempting to download media file..." -ForegroundColor Cyan
                try {
                    $tempFile = [System.IO.Path]::GetTempFileName()
                    Invoke-WebRequest -Uri $mediaResponse.url -OutFile $tempFile -ErrorAction Stop
                    $fileSize = (Get-Item $tempFile).Length
                    Remove-Item $tempFile

                    Write-Host "  ✅ MEDIA FILE DOWNLOADED SUCCESSFULLY!" -ForegroundColor Green
                    Write-Host "  └─ File Size: $([Math]::Round($fileSize / 1MB, 2)) MB" -ForegroundColor White

                } catch {
                    Write-Host "  ❌ MEDIA FILE DOWNLOAD FAILED!" -ForegroundColor Red
                    Write-Host "  └─ Error: $($_.Exception.Message)" -ForegroundColor Red
                }

            } catch {
                Write-Host "  ❌ GET MEDIA URL FAILED!" -ForegroundColor Red
                Write-Host "  └─ Error: $($_.Exception.Message)" -ForegroundColor Red
                if ($_.Exception.Response) {
                    Write-Host "  └─ Status: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
                    Write-Host "  └─ This is the problem! The /media/{path} endpoint is failing!" -ForegroundColor Yellow
                }
            }
        } else {
            Write-Host "  ⚠️ First ad has no media path - cannot test download" -ForegroundColor Yellow
        }
    }

} catch {
    Write-Host "  ❌ GET PLAYLIST FAILED!" -ForegroundColor Red
    Write-Host "  └─ Error: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        Write-Host "  └─ Status: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
    }
    exit 1
}

# ====================================================
# SUMMARY
# ====================================================
Write-Host "`n╔════════════════════════════════════════════════════╗" -ForegroundColor Green
Write-Host "║  🎯 BACKEND API TEST COMPLETE                     ║" -ForegroundColor Green
Write-Host "╚════════════════════════════════════════════════════╝`n" -ForegroundColor Green

Write-Host "RESULTS SUMMARY:" -ForegroundColor Cyan
Write-Host "  Login: Working" -ForegroundColor Green
Write-Host "  Get Screens: Working" -ForegroundColor Green
Write-Host "  Get Playlist: Working" -ForegroundColor Green
Write-Host ""
Write-Host "Next Steps:" -ForegroundColor Yellow
Write-Host "  1. Check if ads exist in the playlist" -ForegroundColor White
Write-Host "  2. Test media download endpoint" -ForegroundColor White
Write-Host "  3. Compare results with app behavior" -ForegroundColor White
Write-Host ""


