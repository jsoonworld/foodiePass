#!/bin/bash

# FoodiePass Backend Development Script

cd "$(dirname "$0")/.."

echo "Starting FoodiePass Backend..."
cd backend && ./gradlew bootRun --args='--spring.profiles.active=local'
