# Development script for the Lambda blog application
# This script starts both the ClojureScript watch build and the local development server

param(
    [switch]$ServerOnly = $false,
    [switch]$WatchOnly = $false,
    [int]$Port = 3003
)

Write-Host "Lambda Blog Development Environment" -ForegroundColor Green

if ($WatchOnly) {
    Write-Host "Starting ClojureScript watch build only..." -ForegroundColor Yellow
    npm run watch-lambda
} elseif ($ServerOnly) {
    Write-Host "Starting development server only on port $Port..." -ForegroundColor Yellow
    $env:PORT = $Port
    npm run dev-server
} else {
    Write-Host "Starting both ClojureScript watch build and development server..." -ForegroundColor Yellow
    Write-Host "Development server will be available at: http://localhost:$Port" -ForegroundColor Cyan
    Write-Host "Shadow-cljs status will be available at: http://localhost:9630" -ForegroundColor Cyan
    Write-Host "" 
    Write-Host "Press Ctrl+C to stop both processes" -ForegroundColor Yellow
    Write-Host ""
    
    $env:PORT = $Port
    npm run dev
}
