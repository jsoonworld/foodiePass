#!/bin/bash

# FoodiePass Backend Development Script

cd "$(dirname "$0")/.." || exit 1

echo "Starting FoodiePass Backend..."
cd backend || exit 1
./gradlew bootRun --args='--spring.profiles.active=local'
