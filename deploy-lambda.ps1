# Deploy Lambda Blog Application
# This script builds the ClojureScript Lambda function and deploys it to AWS

param(
    [string]$Region = "eu-north-1",
    [switch]$SkipBuild = $false,
    [switch]$SkipInfra = $false
)

Write-Host "Starting deployment" -ForegroundColor Green

# Check prerequisites
Write-Host "Checking prerequisites..." -ForegroundColor Yellow

# Check if npm is installed
if (-not (Get-Command npm -ErrorAction SilentlyContinue)) {
    Write-Error "npm is not installed or not in PATH"
    exit 1
}

# Check if terraform is installed
if (-not (Get-Command terraform -ErrorAction SilentlyContinue)) {
    Write-Error "terraform is not installed or not in PATH"
    exit 1
}

# Check if AWS CLI is installed
if (-not (Get-Command aws -ErrorAction SilentlyContinue)) {
    Write-Error "AWS CLI is not installed or not in PATH"
    exit 1
}

# Check AWS credentials
try {
    aws sts get-caller-identity | Out-Null
    Write-Host "AWS credentials verified" -ForegroundColor Green
} catch {
    Write-Error "AWS credentials not configured or invalid"
    exit 1
}

if (-not $SkipBuild) {
    Write-Host "Installing dependencies..." -ForegroundColor Yellow
    npm install
    if ($LASTEXITCODE -ne 0) {
        Write-Error "Failed to install npm dependencies"
        exit 1
    }

    Write-Host "Building ClojureScript for Lambda..." -ForegroundColor Yellow
    npm run release-lambda
    if ($LASTEXITCODE -ne 0) {
        Write-Error "Failed to build ClojureScript Lambda function"
        exit 1
    }

    Write-Host "Installing production dependencies for Lambda..." -ForegroundColor Yellow
    # Create package.json for lambda with only production dependencies
    $lambdaPackageJson = @{
        name = "brotherus-blog-lambda"
        version = "1.0.0"
        dependencies = @{
            "he" = "^1.2.0"
            "highlight.js" = "^11.11.1"
            "marked" = "^15.0.11"
            "marked-highlight" = "^2.2.1"
            "aws-lambda" = "^1.0.7"
            "node-fetch" = "^2.7.0"
        }
    } | ConvertTo-Json -Depth 3

    $lambdaPackageJson | Out-File -FilePath "target/lambda/package.json" -Encoding UTF8

    # Install dependencies in the lambda directory
    Push-Location "target/lambda"
    try {
        npm install --production --no-package-lock --no-optional

        # Remove unnecessary files to reduce package size
        Write-Host "Optimizing package size..." -ForegroundColor Yellow

        # Remove documentation and test files
        Get-ChildItem -Path "node_modules" -Recurse -Include "*.md", "*.txt", "CHANGELOG*", "README*", "LICENSE*", "HISTORY*" | Remove-Item -Force -ErrorAction SilentlyContinue
        Get-ChildItem -Path "node_modules" -Recurse -Directory -Include "test", "tests", "spec", "specs", "example", "examples", "doc", "docs", ".github" | Remove-Item -Recurse -Force -ErrorAction SilentlyContinue

        # Remove source maps and TypeScript definition files
        Get-ChildItem -Path "node_modules" -Recurse -Include "*.map", "*.d.ts" | Remove-Item -Force -ErrorAction SilentlyContinue

        Write-Host "Lambda dependencies installed and optimized successfully" -ForegroundColor Green
    } finally {
        Pop-Location
    }

    Write-Host "Copying static assets to Lambda output..." -ForegroundColor Yellow
    # Copy static assets that the Lambda function might need
    if (Test-Path "target/lambda") {
        Copy-Item "resources/public/static.css" "target/lambda/" -ErrorAction SilentlyContinue
        Copy-Item "resources/public/images" "target/lambda/" -Recurse -ErrorAction SilentlyContinue
    }

    Write-Host "Creating Lambda deployment package..." -ForegroundColor Yellow
    # Create the deployment zip
    if (Test-Path "lambda-deployment.zip") {
        Remove-Item "lambda-deployment.zip" -Force
    }

    Push-Location "target/lambda"
    try {
        # Use PowerShell's Compress-Archive instead of zip command for Windows compatibility
        Compress-Archive -Path "*" -DestinationPath "../../lambda-deployment.zip" -Force
        Write-Host "Lambda deployment package created: lambda-deployment.zip" -ForegroundColor Green
    } finally {
        Pop-Location
    }
}

if (-not $SkipInfra) {
    Write-Host "Deploying infrastructure with Terraform..." -ForegroundColor Yellow
    
    Push-Location "infrastructure"
    try {
        # Initialize Terraform
        terraform init
        if ($LASTEXITCODE -ne 0) {
            Write-Error "Failed to initialize Terraform"
            exit 1
        }

        # Plan the deployment
        terraform plan -var="aws_region=$Region"
        if ($LASTEXITCODE -ne 0) {
            Write-Error "Terraform plan failed"
            exit 1
        }

        # Apply the deployment
        terraform apply -var="aws_region=$Region" -auto-approve
        if ($LASTEXITCODE -ne 0) {
            Write-Error "Terraform apply failed"
            exit 1
        }

        # Get outputs
        $ApiUrl = terraform output -raw api_gateway_url
        $LambdaName = terraform output -raw lambda_function_name
        $S3Bucket = terraform output -raw s3_bucket_name

        Write-Host "Deployment completed successfully!" -ForegroundColor Green
        Write-Host "API Gateway URL: $ApiUrl" -ForegroundColor Cyan
        Write-Host "Lambda Function: $LambdaName" -ForegroundColor Cyan
        Write-Host "S3 Bucket: $S3Bucket" -ForegroundColor Cyan

    } finally {
        Pop-Location
    }
} else {
    Write-Host "Skipping infrastructure deployment" -ForegroundColor Yellow
    
    # Just update the Lambda function code if infrastructure already exists
    Write-Host "Updating Lambda function code..." -ForegroundColor Yellow
    $FunctionName = "brotherus-blog"
    
    $updateResult = aws lambda update-function-code --function-name $FunctionName --zip-file fileb://lambda-deployment.zip --region $Region 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "Lambda function code updated successfully!" -ForegroundColor Green
    } else {
        Write-Error "Failed to update Lambda function code"
        Write-Error $updateResult
        exit 1
    }
}

Write-Host "Deployment process completed!" -ForegroundColor Green
