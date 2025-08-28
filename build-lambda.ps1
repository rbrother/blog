# Build script for the Lambda blog application
# This script only builds the application without deploying

param(
    [switch]$Watch = $false,
    [switch]$Clean = $false
)

Write-Host "Building ClojureScript Lambda Blog Application" -ForegroundColor Green
Write-Host "Clean:$Clean    Watch:$Watch" -ForegroundColor Green

if ($Clean) {
    Write-Host "Cleaning previous builds..." -ForegroundColor Yellow
    if (Test-Path "target") {
        Remove-Item "target" -Recurse -Force
    }
    if (Test-Path "lambda-deployment.zip") {
        Remove-Item "lambda-deployment.zip" -Force
    }
}

# Install dependencies
Write-Host "Installing dependencies..." -ForegroundColor Yellow
npm install
if ($LASTEXITCODE -ne 0) {
    Write-Error "Failed to install npm dependencies"
    exit 1
}

if ($Watch) {
    Write-Host "Starting watch mode for Lambda development..." -ForegroundColor Yellow
    npm run watch-lambda
} else {
    Write-Host "Building Lambda function..." -ForegroundColor Yellow
    npm run release-lambda
    if ($LASTEXITCODE -ne 0) {
        Write-Error "Failed to build Lambda function"
        exit 1
    }
    
    Write-Host "Build completed successfully!" -ForegroundColor Green
    Write-Host "Lambda function built to: target/lambda/index.js" -ForegroundColor Cyan
}
