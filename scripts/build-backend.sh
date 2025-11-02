#!/bin/bash

# FoodiePass Backend Build Script

cd "$(dirname "$0")/.."

echo "Building FoodiePass Backend..."
cd backend && ./gradlew clean build
