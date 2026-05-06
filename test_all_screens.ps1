# Test all screens to find which have valid ads with media

$baseUrl = "https://api.adjaba.in"
$username = "boss"
$password = "password"

Write-Host "`nTesting all screens for valid ads...`n" -ForegroundColor Cyan

# Login
$loginUrl = "$baseUrl/v2/authenticate_user"
$loginBody = @{ userId = $username; password = $password } | ConvertTo-Json
$loginResponse = Invoke-RestMethod -Uri $loginUrl -Method Post -Body $loginBody -ContentType "application/json"
$token = $loginResponse.loginToken
Write-Host "Logged in successfully!`n" -ForegroundColor Green

# Get screens
$headers = @{ "Authorization" = "Bearer $token" }
$screensUrl = "$baseUrl/get_screen_by_user"
$screens = Invoke-RestMethod -Uri $screensUrl -Method Get -Headers $headers

Write-Host "Found $($screens.Count) screens. Testing each...`n" -ForegroundColor Yellow

# Test each screen
foreach ($screen in $screens) {
    $screenId = $screen.screenId
    $cleanId = if ($screenId.Contains("/")) { $screenId.Split("/")[0] } else { $screenId }

    Write-Host "======================================" -ForegroundColor Cyan
    Write-Host "Screen: $($screen.screenName) (ID: $screenId)" -ForegroundColor Cyan
    Write-Host "Location: $($screen.location)"

    try {
        $playlistUrl = "$baseUrl/get_screen_playlists/$cleanId"
        $playlist = Invoke-RestMethod -Uri $playlistUrl -Method Get -Headers $headers

        Write-Host "Ads Count: $($playlist.Count)" -ForegroundColor Yellow

        if ($playlist.Count -gt 0) {
            $validAds = 0
            for ($i = 0; $i -lt $playlist.Count; $i++) {
                $ad = $playlist[$i]
                $path = $ad.adContractData.path
                $duration = $ad.adContractData.duration

                if ($path -and $path -ne "" -and $duration -gt 0) {
                    $validAds++
                    Write-Host "  Ad $($i+1): $($ad.adContractData.advertId)" -ForegroundColor Green
                    Write-Host "    Path: $path"
                    Write-Host "    Duration: ${duration}s"
                    Write-Host "    Type: $($ad.typeAdvert)"
                } else {
                    Write-Host "  Ad $($i+1): $($ad.adContractData.advertId)" -ForegroundColor Red
                    Write-Host "    INVALID: Missing path or duration"
                }
            }

            if ($validAds -gt 0) {
                Write-Host "`n  RESULT: $validAds valid ads found!`n" -ForegroundColor Green
            } else {
                Write-Host "`n  RESULT: No valid ads (all missing paths)`n" -ForegroundColor Red
            }
        } else {
            Write-Host "  RESULT: No ads in playlist`n" -ForegroundColor Red
        }

    } catch {
        Write-Host "  ERROR: $($_.Exception.Message)`n" -ForegroundColor Red
    }
}

Write-Host "`n======================================" -ForegroundColor Cyan
Write-Host "SUMMARY: Backend Test Complete" -ForegroundColor Cyan
Write-Host "======================================`n" -ForegroundColor Cyan

