#!/bin/bash

# Migrate local Terraform state to S3 backend
# Run this after backend.tf is in place and S3/DynamoDB resources exist

echo -e "\033[32mMigrating Terraform state to S3 backend...\033[0m"
echo ""

cd infrastructure

# Set up credentials
ACCESS_KEY=$(aws configure get aws_access_key_id --profile default 2>/dev/null)
SECRET_KEY=$(aws configure get aws_secret_access_key --profile default 2>/dev/null)
export AWS_ACCESS_KEY_ID="$ACCESS_KEY"
export AWS_SECRET_ACCESS_KEY="$SECRET_KEY"
export AWS_PROFILE=default

# Run terraform init (will prompt to migrate state)
echo -e "\033[33mRunning 'terraform init'...\033[0m"
terraform init

if [ $? -eq 0 ]; then
    echo ""
    echo -e "\033[32m✅ State migration successful!\033[0m"
    echo ""
    echo -e "\033[33mNow you can safely delete local state files:\033[0m"
    echo "  cd infrastructure"
    echo "  rm -f terraform.tfstate terraform.tfstate.backup"
    echo "  rm -rf .terraform"
    echo "  rm -f .terraform.lock.hcl"
    echo "  cd .."
    echo ""
    echo -e "\033[33mVerify with:\033[0m"
    echo "  cd infrastructure && terraform plan -var='aws_region=eu-north-1'"
    echo ""
else
    echo -e "\033[31m❌ Migration failed. Check your AWS credentials.\033[0m"
    exit 1
fi

cd ..

