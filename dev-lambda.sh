#!/bin/bash

# Development script for the Lambda blog application
# This script starts both the ClojureScript watch build and the local development server

SERVER_ONLY=false
WATCH_ONLY=false
PORT=3003

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --server-only)
            SERVER_ONLY=true
            shift
            ;;
        --watch-only)
            WATCH_ONLY=true
            shift
            ;;
        --port)
            PORT="$2"
            shift 2
            ;;
        *)
            echo "Unknown option: $1"
            exit 1
            ;;
    esac
done

echo -e "\033[32mLambda Blog Development Environment\033[0m"

if [ "$WATCH_ONLY" = true ]; then
    echo -e "\033[33mStarting ClojureScript watch build only...\033[0m"
    npm run watch-lambda
elif [ "$SERVER_ONLY" = true ]; then
    echo -e "\033[33mStarting development server only on port $PORT...\033[0m"
    export PORT=$PORT
    npm run dev-server
else
    echo -e "\033[33mStarting both ClojureScript watch build and development server...\033[0m"
    echo -e "\033[36mDevelopment server will be available at: http://localhost:$PORT\033[0m"
    echo -e "\033[36mShadow-cljs status will be available at: http://localhost:9630\033[0m"
    echo ""
    echo -e "\033[33mPress Ctrl+C to stop both processes\033[0m"
    echo ""
    
    export PORT=$PORT
    npm run dev
fi
