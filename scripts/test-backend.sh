#!/bin/bash

# FoodiePass Backend Test Script

cd "$(dirname "$0")/.." || exit 1

echo "Running FoodiePass Backend Tests..."
cd backend || exit 1
./gradlew test
