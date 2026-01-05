#!/bin/bash

# Appetize.io Upload Script
# This script uploads APK files to Appetize.io for browser-based testing

set -e

# Check for required environment variables
if [ -z "$APPETIZE_API_TOKEN" ]; then
    echo "Error: APPETIZE_API_TOKEN environment variable is not set"
    exit 1
fi

# Parse command line arguments
APK_PATH="$1"
if [ -z "$APK_PATH" ]; then
    echo "Error: APK path is required"
    echo "Usage: $0 <path-to-apk> [app-public-key]"
    exit 1
fi

if [ ! -f "$APK_PATH" ]; then
    echo "Error: APK file not found at $APK_PATH"
    exit 1
fi

APP_PUBLIC_KEY="${2:-}"
PLATFORM="android"

echo "Uploading APK to Appetize.io..."
echo "APK: $APK_PATH"

# Upload or update the app on Appetize.io
if [ -z "$APP_PUBLIC_KEY" ]; then
    # Create new app
    echo "Creating new app on Appetize.io..."
    RESPONSE=$(curl -s -X POST \
        https://${APPETIZE_API_TOKEN}@api.appetize.io/v1/apps \
        -F "file=@${APK_PATH}" \
        -F "platform=${PLATFORM}")
else
    # Update existing app
    echo "Updating existing app on Appetize.io..."
    RESPONSE=$(curl -s -X POST \
        https://${APPETIZE_API_TOKEN}@api.appetize.io/v1/apps/${APP_PUBLIC_KEY} \
        -F "file=@${APK_PATH}" \
        -F "platform=${PLATFORM}")
fi

# Check if upload was successful
if echo "$RESPONSE" | grep -q "publicKey"; then
    echo "Upload successful!"
    
    # Extract the public key
    PUBLIC_KEY=$(echo "$RESPONSE" | grep -o '"publicKey":"[^"]*"' | cut -d'"' -f4)
    
    echo ""
    echo "============================================"
    echo "Appetize.io App Details:"
    echo "============================================"
    echo "Public Key: $PUBLIC_KEY"
    echo "App URL: https://appetize.io/app/$PUBLIC_KEY"
    echo "Embed URL: https://appetize.io/embed/$PUBLIC_KEY"
    echo "============================================"
    echo ""
    
    # Save details to file
    mkdir -p appetize-outputs
    echo "$PUBLIC_KEY" > appetize-outputs/public-key.txt
    echo "https://appetize.io/app/$PUBLIC_KEY" > appetize-outputs/app-url.txt
    
    # Output for GitHub Actions
    if [ -n "$GITHUB_OUTPUT" ]; then
        echo "public_key=$PUBLIC_KEY" >> $GITHUB_OUTPUT
        echo "app_url=https://appetize.io/app/$PUBLIC_KEY" >> $GITHUB_OUTPUT
    fi
else
    echo "Upload failed!"
    echo "Response: $RESPONSE"
    exit 1
fi
