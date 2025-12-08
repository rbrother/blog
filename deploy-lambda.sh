#!/bin/bash

# Deploy Lambda Blog Application. Read README.md for mode.

REGION="eu-north-1"
SKIP_BUILD=false
SKIP_INFRA=false

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --region)
            REGION="$2"
            shift 2
            ;;
        --skip-build)
            SKIP_BUILD=true
            shift
            ;;
        --skip-infra)
            SKIP_INFRA=true
            shift
            ;;
        *)
            echo "Unknown option: $1"
            exit 1
            ;;
    esac
done

echo -e "\033[32mStarting deployment\033[0m"

# Check prerequisites
echo -e "\033[33mChecking prerequisites...\033[0m"

# Check if npm is installed
if ! command -v npm &> /dev/null; then
    echo -e "\033[31mnpm is not installed or not in PATH\033[0m" >&2
    exit 1
fi

# Check if terraform is installed
if ! command -v terraform &> /dev/null; then
    echo -e "\033[31mterraform is not installed or not in PATH\033[0m" >&2
    exit 1
fi

# Check if AWS CLI is installed
if ! command -v aws &> /dev/null; then
    echo -e "\033[31mAWS CLI is not installed or not in PATH\033[0m" >&2
    exit 1
fi

# Check AWS credentials
if ! aws sts get-caller-identity > /dev/null 2>&1; then
    echo -e "\033[31mAWS credentials not configured or invalid\033[0m" >&2
    exit 1
fi
echo -e "\033[32mAWS credentials verified\033[0m"

echo -e "\033[33mInstalling dependencies...\033[0m"
npm install
if [ $? -ne 0 ]; then
    echo -e "\033[31mFailed to install npm dependencies\033[0m" >&2
    exit 1
fi

echo -e "\033[33mBuilding ClojureScript for Lambda...\033[0m"
npm run release-lambda
if [ $? -ne 0 ]; then
    echo -e "\033[31mFailed to build ClojureScript Lambda function\033[0m" >&2
    exit 1
fi

echo -e "\033[33mInstalling production dependencies for Lambda...\033[0m"
# Create package.json for lambda with only production dependencies

cp "package.json" "target/lambda/package.json"

# Install dependencies in the lambda directory
pushd "target/lambda" > /dev/null
npm install --production --no-package-lock --no-optional
popd > /dev/null
echo -e "\033[32mLambda dependencies installed and optimized successfully\033[0m"

echo -e "\033[33mCreating Lambda deployment package...\033[0m"
# Create the deployment zip
rm -f lambda-deployment.zip

pushd "target/lambda" > /dev/null
zip -r ../../lambda-deployment.zip *
popd > /dev/null
echo -e "\033[32mLambda deployment package created: lambda-deployment.zip\033[0m"

if [ "$SKIP_INFRA" = false ]; then
    echo -e "\033[33mDeploying infrastructure with Terraform...\033[0m"
    
    # Check if static credentials exist
    ACCESS_KEY=$(aws configure get aws_access_key_id --profile default 2>/dev/null)
    SECRET_KEY=$(aws configure get aws_secret_access_key --profile default 2>/dev/null)
    
    if [ -z "$ACCESS_KEY" ] || [ -z "$SECRET_KEY" ]; then
        echo -e "\033[31mError: No static AWS credentials found in ~/.aws/credentials\033[0m" >&2
        echo -e "\033[33mTerraform requires static IAM access keys (not SSO).\033[0m" >&2
        echo -e "\033[33mTo set up credentials:\033[0m" >&2
        echo -e "\033[36m  1. Create access keys in IAM: https://console.aws.amazon.com/iam/home#/users/rjb-admin\033[0m" >&2
        echo -e "\033[36m  2. Run: aws configure --profile default\033[0m" >&2
        echo -e "\033[36m  3. Enter the Access Key ID and Secret Access Key\033[0m" >&2
        exit 1
    fi
    
    # Export credentials for Terraform
    export AWS_ACCESS_KEY_ID="$ACCESS_KEY"
    export AWS_SECRET_ACCESS_KEY="$SECRET_KEY"
    export AWS_PROFILE=default
    
    pushd "infrastructure" > /dev/null
    
    # Initialize Terraform
    terraform init
    if [ $? -ne 0 ]; then
        echo -e "\033[31mFailed to initialize Terraform\033[0m" >&2
        popd > /dev/null
        exit 1
    fi

    # Plan the deployment
    terraform plan -var="aws_region=$REGION"
    if [ $? -ne 0 ]; then
        echo -e "\033[31mTerraform plan failed\033[0m" >&2
        popd > /dev/null
        exit 1
    fi

    # Apply the deployment
    terraform apply -var="aws_region=$REGION" -auto-approve
    if [ $? -ne 0 ]; then
        echo -e "\033[31mTerraform apply failed\033[0m" >&2
        popd > /dev/null
        exit 1
    fi

    # Get outputs
    API_URL=$(terraform output -raw api_gateway_url)
    LAMBDA_NAME=$(terraform output -raw lambda_function_name)
    S3_BUCKET=$(terraform output -raw s3_bucket_name)

    echo -e "\033[32mDeployment completed successfully!\033[0m"
    echo -e "\033[36mAPI Gateway URL: $API_URL\033[0m"
    echo -e "\033[36mLambda Function: $LAMBDA_NAME\033[0m"
    echo -e "\033[36mS3 Bucket: $S3_BUCKET\033[0m"

    popd > /dev/null
else
    echo -e "\033[33mSkipping infrastructure deployment\033[0m"

    # Just update the Lambda function code if infrastructure already exists
    echo -e "\033[33mUpdating Lambda function code...\033[0m"
    FUNCTION_NAME="brotherus-blog"

    UPDATE_RESULT=$(aws lambda update-function-code --function-name "$FUNCTION_NAME" --zip-file fileb://lambda-deployment.zip --region "$REGION" 2>&1)
    if [ $? -eq 0 ]; then
        echo -e "\033[32mLambda function code updated successfully!\033[0m"
    else
        echo -e "\033[31mFailed to update Lambda function code\033[0m" >&2
        echo -e "\033[31m$UPDATE_RESULT\033[0m" >&2
        exit 1
    fi

    # Upload static assets to S3 even when skipping infrastructure
    echo -e "\033[33mUploading static assets to S3...\033[0m"
    S3_BUCKET="brotherus-blog-blog-static-assets"
    if [ -d "resources/public/images" ]; then
        aws s3 cp "resources/public/images" "s3://$S3_BUCKET/images/" --recursive --region "$REGION"
        if [ $? -eq 0 ]; then
            echo -e "\033[32mStatic assets uploaded successfully!\033[0m"
        else
            echo -e "\033[31mFailed to upload static assets to S3\033[0m" >&2
        fi
    fi

fi

echo -e "\033[32mDeployment process completed!\033[0m"
