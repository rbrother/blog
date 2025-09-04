# Deploy Lambda Blog Application
# This script builds the ClojureScript Lambda function and deploys it to AWS to:
# https://dlsqhsyaah.execute-api.eu-north-1.amazonaws.com/
# Terraform main.tf in the infrastructure directory defines resources up to and including the API Gateway.
# On top of that we have manually created CloudFront distribution and Route53 alias record
# mapping it with caching to https://www.brotherus.net
# Execution of the lambda can be slow with long articles, up to 18 sec for Infia article!
# But CloudFront cache returns even that in ~300ms (and ~20 ms if in local cache),
# also using Brotli compression.
# X-Cache response header tells if CloudFront cache was used.
# CachePolicy default ttl is 24 hours.
# Any cache item can be invalidated at: https://us-east-1.console.aws.amazon.com/cloudfront/v4/home?region=eu-north-1#/distributions/E13505H1AVUV02/invalidations/create

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

    Copy-Item "package.json" "target/lambda/package.json" -Force

    # Install dependencies in the lambda directory
    Push-Location "target/lambda"
    try {
        npm install --production --no-package-lock --no-optional
        Write-Host "Lambda dependencies installed and optimized successfully" -ForegroundColor Green
    } finally {
        Pop-Location
    }

    Write-Host "Creating Lambda deployment package..." -ForegroundColor Yellow
    # Create the deployment zip
    if (Test-Path "lambda-deployment.zip") {
        Remove-Item "lambda-deployment.zip" -Force
    }

    Push-Location "target/lambda"
    try {        
        bestzip ../../lambda-deployment.zip *
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

        # Upload static assets to S3
        Write-Host "Uploading static assets to S3..." -ForegroundColor Yellow
        if (Test-Path "../resources/public/images") {
            aws s3 cp "../resources/public/images" "s3://$S3Bucket/images/" --recursive --region $Region
            if ($LASTEXITCODE -eq 0) {
                Write-Host "Static assets uploaded successfully!" -ForegroundColor Green
            } else {
                Write-Error "Failed to upload static assets to S3"
            }
        }

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

    # Upload static assets to S3 even when skipping infrastructure
    Write-Host "Uploading static assets to S3..." -ForegroundColor Yellow
    $S3Bucket = "brotherus-blog-blog-static-assets"
    if (Test-Path "resources/public/images") {
        aws s3 cp "resources/public/images" "s3://$S3Bucket/images/" --recursive --region $Region
        if ($LASTEXITCODE -eq 0) {
            Write-Host "Static assets uploaded successfully!" -ForegroundColor Green
        } else {
            Write-Error "Failed to upload static assets to S3"
        }
    }
}

Write-Host "Deployment process completed!" -ForegroundColor Green
