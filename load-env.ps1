# Load environment variables from secrets.properties
# Usage: .\load-env.ps1

Write-Host "Loading environment variables from secrets.properties..." -ForegroundColor Green

if (Test-Path "secrets.properties") {
    Get-Content secrets.properties | ForEach-Object {
        if ($_ -and !$_.StartsWith('#') -and $_ -match '^([^=]+)=(.*)$') {
            $key = $matches[1].Trim()
            $value = $matches[2].Trim()
            [Environment]::SetEnvironmentVariable($key, $value, 'Process')
            Write-Host "  ✓ Loaded: $key" -ForegroundColor Gray
        }
    }
    Write-Host "`nEnvironment variables loaded successfully!" -ForegroundColor Green
    Write-Host "You can now run your Spring Boot application." -ForegroundColor Yellow
} else {
    Write-Host "Error: secrets.properties not found!" -ForegroundColor Red
    Write-Host "Please copy secrets.properties.example to secrets.properties and fill in your values." -ForegroundColor Yellow
    exit 1
}
