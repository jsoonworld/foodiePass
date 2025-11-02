#!/bin/bash

# FoodiePass Backend Build Script

cd "$(dirname "$0")/.." || exit 1

echo "Building FoodiePass Backend..."
cd backend || exit 1
./gradlew clean build
