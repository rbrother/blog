#!/bin/bash

# Build script for the Lambda blog application
# This script only builds the application without deploying

WATCH=false
CLEAN=false

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --watch)
            WATCH=true
            shift
            ;;
        --clean)
            CLEAN=true
            shift
            ;;
        *)
            echo "Unknown option: $1"
            exit 1
            ;;
    esac
done

echo -e "\033[32mBuilding ClojureScript Lambda Blog Application\033[0m"
echo -e "\033[32mClean:$CLEAN    Watch:$WATCH\033[0m"

if [ "$CLEAN" = true ]; then
    echo -e "\033[33mCleaning previous builds...\033[0m"
    rm -rf target
    rm -f lambda-deployment.zip
fi

if [ "$WATCH" = true ]; then
    echo -e "\033[33mStarting watch mode for Lambda development...\033[0m"
    npm run watch-lambda
else
    echo -e "\033[33mBuilding Lambda function...\033[0m"
    npm run release-lambda
    if [ $? -ne 0 ]; then
        echo -e "\033[31mFailed to build Lambda function\033[0m" >&2
        exit 1
    fi
    
    echo -e "\033[32mBuild completed successfully!\033[0m"
    echo -e "\033[36mLambda function built to: target/lambda/index.js\033[0m"
fi
