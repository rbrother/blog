#!/bin/bash

# This script sets up S3 backend for Terraform state
# Run this once to create the necessary AWS resources

REGION="eu-north-1"
BUCKET_NAME="brotherus-blog-terraform-state"
TABLE_NAME="brotherus-blog-terraform-locks"

echo -e "\033[32mSetting up Terraform S3 backend...\033[0m"

# Set up credentials
ACCESS_KEY=$(aws configure get aws_access_key_id --profile default 2>/dev/null)
SECRET_KEY=$(aws configure get aws_secret_access_key --profile default 2>/dev/null)
export AWS_ACCESS_KEY_ID="$ACCESS_KEY"
export AWS_SECRET_ACCESS_KEY="$SECRET_KEY"
export AWS_PROFILE=default

# Create S3 bucket for Terraform state
echo -e "\033[33mCreating S3 bucket for Terraform state: $BUCKET_NAME\033[0m"
aws s3api create-bucket \
    --bucket "$BUCKET_NAME" \
    --region "$REGION" \
    --create-bucket-configuration LocationConstraint="$REGION" 2>/dev/null || echo "Bucket already exists or creation failed"

# Enable versioning on the bucket (for recovery)
echo -e "\033[33mEnabling versioning on bucket...\033[0m"
aws s3api put-bucket-versioning \
    --bucket "$BUCKET_NAME" \
    --versioning-configuration Status=Enabled

# Enable encryption
echo -e "\033[33mEnabling encryption on bucket...\033[0m"
aws s3api put-bucket-encryption \
    --bucket "$BUCKET_NAME" \
    --server-side-encryption-configuration '{
        "Rules": [
            {
                "ApplyServerSideEncryptionByDefault": {
                    "SSEAlgorithm": "AES256"
                }
            }
        ]
    }'

# Block public access
echo -e "\033[33mBlocking public access to bucket...\033[0m"
aws s3api put-public-access-block \
    --bucket "$BUCKET_NAME" \
    --public-access-block-configuration \
    "BlockPublicAcls=true,IgnorePublicAcls=true,BlockPublicPolicy=true,RestrictPublicBuckets=true"

# Create DynamoDB table for state locking
echo -e "\033[33mCreating DynamoDB table for state locking: $TABLE_NAME\033[0m"
aws dynamodb create-table \
    --table-name "$TABLE_NAME" \
    --attribute-definitions AttributeName=LockID,AttributeType=S \
    --key-schema AttributeName=LockID,KeyType=HASH \
    --billing-mode PAY_PER_REQUEST \
    --region "$REGION" 2>/dev/null || echo "Table already exists or creation failed"

echo -e "\033[32mTerraform backend setup complete!\033[0m"
echo -e "\033[36mNext steps:\033[0m"
echo -e "\033[36m  1. Run: cd infrastructure && terraform init\033[0m"
echo -e "\033[36m  2. When prompted, confirm migration of state to S3\033[0m"
echo -e "\033[36m  3. You can now safely delete local terraform.tfstate files\033[0m"

