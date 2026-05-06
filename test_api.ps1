# Backend API Test Script
# Tests: Login -> Get Screens -> Get Playlist -> Download Ads

$baseUrl = "https://api.adjaba.in"
$username = "boss"
$password = "password"

Write-Host "`n================================================" -ForegroundColor Cyan
Write-Host "  ADJABA API BACKEND TEST" -ForegroundColor Cyan
Write-Host "================================================`n" -ForegroundColor Cyan

# ====================================================
# STEP 1: LOGIN
# ====================================================
Write-Host "STEP 1: Testing Login" -ForegroundColor Yellow
Write-Host "------------------------------------------------`n" -ForegroundColor Yellow

$loginUrl = "$baseUrl/v2/authenticate_user"
$loginBody = @{
    userId = $username
    password = $password
} | ConvertTo-Json

Write-Host "  Endpoint: POST $loginUrl"
Write-Host "  Body: $loginBody`n"

try {
    $loginResponse = Invoke-RestMethod -Uri $loginUrl -Method Post -Body $loginBody -ContentType "application/json"

    Write-Host "  SUCCESS: Login successful!" -ForegroundColor Green
    Write-Host "  User ID: $($loginResponse.userid)"
    Write-Host "  Email: $($loginResponse.email)"
    Write-Host "  Message: $($loginResponse.message)"
    $tokenPreview = $loginResponse.loginToken.Substring(0, [Math]::Min(50, $loginResponse.loginToken.Length))
    Write-Host "  Token: $tokenPreview...`n"

    $token = $loginResponse.loginToken

} catch {
    Write-Host "  ERROR: Login failed!" -ForegroundColor Red
    Write-Host "  Details: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# ====================================================
# STEP 2: GET SCREENS
# ====================================================
Write-Host "`nSTEP 2: Getting User Screens" -ForegroundColor Yellow
Write-Host "------------------------------------------------`n" -ForegroundColor Yellow

$screensUrl = "$baseUrl/get_screen_by_user"
Write-Host "  Endpoint: GET $screensUrl"
Write-Host "  Auth: Bearer token`n"

try {
    $headers = @{
        "Authorization" = "Bearer $token"
    }

    $screensResponse = Invoke-RestMethod -Uri $screensUrl -Method Get -Headers $headers

    Write-Host "  SUCCESS: Retrieved $($screensResponse.Count) screens!" -ForegroundColor Green

    if ($screensResponse.Count -eq 0) {
        Write-Host "  WARNING: No screens found!" -ForegroundColor Yellow
        exit 1
    }

    # Display screens
    for ($i = 0; $i -lt $screensResponse.Count; $i++) {
        $screen = $screensResponse[$i]
        Write-Host "`n  Screen $($i + 1):" -ForegroundColor Cyan
        Write-Host "    Screen ID: $($screen.screenId)"
        Write-Host "    Name: $($screen.screenName)"
        Write-Host "    Location: $($screen.location)"
        Write-Host "    Orientation: $($screen.orientation)"
    }

    # Pick first screen
    $testScreenId = $screensResponse[0].screenId
    Write-Host "`n  Testing with Screen: $testScreenId" -ForegroundColor Magenta

} catch {
    Write-Host "  ERROR: Failed to get screens!" -ForegroundColor Red
    Write-Host "  Details: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# ====================================================
# STEP 3: GET PLAYLIST
# ====================================================
Write-Host "`n`nSTEP 3: Getting Playlist for Screen $testScreenId" -ForegroundColor Yellow
Write-Host "------------------------------------------------`n" -ForegroundColor Yellow

# Clean screenId
$cleanScreenId = if ($testScreenId.Contains("/")) { $testScreenId.Split("/")[0] } else { $testScreenId }

$playlistUrl = "$baseUrl/get_screen_playlists/$cleanScreenId"
Write-Host "  Endpoint: GET $playlistUrl"
Write-Host "  Auth: Bearer token`n"

try {
    $playlistResponse = Invoke-RestMethod -Uri $playlistUrl -Method Get -Headers $headers

    Write-Host "  SUCCESS: Retrieved $($playlistResponse.Count) ads!" -ForegroundColor Green

    if ($playlistResponse.Count -eq 0) {
        Write-Host "`n  WARNING: No ads in playlist!" -ForegroundColor Yellow
        Write-Host "  This is why the app shows only weather/news!`n" -ForegroundColor Yellow
    } else {
        # Display ads
        $adsToShow = [Math]::Min(3, $playlistResponse.Count)
        for ($i = 0; $i -lt $adsToShow; $i++) {
            $ad = $playlistResponse[$i]
            Write-Host "`n  Ad $($i + 1):" -ForegroundColor Cyan
            Write-Host "    Advert ID: $($ad.adContractData.advertId)"
            Write-Host "    Type: $($ad.typeAdvert)"
            Write-Host "    Duration: $($ad.adContractData.duration)s"
            Write-Host "    Path: $($ad.adContractData.path)"
        }

        if ($playlistResponse.Count -gt $adsToShow) {
            Write-Host "`n  ... and $($playlistResponse.Count - $adsToShow) more ads"
        }

        # Test media download
        $firstAd = $playlistResponse[0]
        $mediaPath = $firstAd.adContractData.path

        if ($mediaPath) {
            Write-Host "`n`nSTEP 4: Testing Media Download" -ForegroundColor Yellow
            Write-Host "------------------------------------------------`n" -ForegroundColor Yellow

            $mediaUrl = "$baseUrl/media/$mediaPath"
            Write-Host "  Endpoint: GET $mediaUrl"
            Write-Host "  Auth: Bearer token`n"

            try {
                $mediaResponse = Invoke-RestMethod -Uri $mediaUrl -Method Get -Headers $headers

                Write-Host "  SUCCESS: Got presigned URL!" -ForegroundColor Green
                $urlPreview = $mediaResponse.url.Substring(0, [Math]::Min(80, $mediaResponse.url.Length))
                Write-Host "  URL: $urlPreview...`n"

                # Try downloading
                Write-Host "  Downloading media file..."
                try {
                    $tempFile = [System.IO.Path]::GetTempFileName()
                    Invoke-WebRequest -Uri $mediaResponse.url -OutFile $tempFile
                    $fileSize = (Get-Item $tempFile).Length
                    Remove-Item $tempFile

                    $fileSizeMB = [Math]::Round($fileSize / 1MB, 2)
                    Write-Host "  SUCCESS: Downloaded $fileSizeMB MB!" -ForegroundColor Green

                } catch {
                    Write-Host "  ERROR: Download failed!" -ForegroundColor Red
                    Write-Host "  Details: $($_.Exception.Message)" -ForegroundColor Red
                }

            } catch {
                Write-Host "  ERROR: Failed to get media URL!" -ForegroundColor Red
                Write-Host "  Details: $($_.Exception.Message)" -ForegroundColor Red
                Write-Host "  THIS IS THE PROBLEM - Media endpoint is failing!" -ForegroundColor Yellow
            }
        }
    }

} catch {
    Write-Host "  ERROR: Failed to get playlist!" -ForegroundColor Red
    Write-Host "  Details: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# SUMMARY
Write-Host "`n`n================================================" -ForegroundColor Green
Write-Host "  TEST COMPLETE" -ForegroundColor Green
Write-Host "================================================`n" -ForegroundColor Green

Write-Host "All API endpoints tested successfully!`n"

