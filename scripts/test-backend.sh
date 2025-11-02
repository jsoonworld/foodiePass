#!/bin/bash

# FoodiePass Backend Test Script

cd "$(dirname "$0")/.."

echo "Running FoodiePass Backend Tests..."
cd backend && ./gradlew test
