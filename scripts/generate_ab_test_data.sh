#!/bin/bash

# Generate A/B Test Sample Data
# This script generates sample menu scans and survey responses for testing

# Configuration
TOTAL_USERS=100
BASE_URL="http://localhost:8080"

echo "=== A/B Test Data Generator ==="
echo "Generating data for $TOTAL_USERS users..."
echo ""

# Arrays to track scan IDs for later survey submission
declare -a CONTROL_SCANS=()
declare -a TREATMENT_SCANS=()

# Generate menu scans (which will be randomly assigned to Control/Treatment)
for i in $(seq 1 $TOTAL_USERS); do
    USER_ID=$(uuidgen | tr -d '-')

    # Use a minimal payload to avoid actual API processing
    # The backend will assign A/B group randomly
    RESPONSE=$(curl -s -X POST "$BASE_URL/api/menus/scan" \
        -H "Content-Type: application/json" \
        -d "{
            \"userId\": \"$USER_ID\",
            \"imageBase64\": \"data:image/png;base64,iVBORw0KGgo=\",
            \"userLanguageName\": \"Korean\",
            \"userCurrencyName\": \"South Korean won\"
        }" 2>/dev/null)

    # Extract scanId and abGroup from response
    SCAN_ID=$(echo "$RESPONSE" | jq -r '.scanId // empty')
    AB_GROUP=$(echo "$RESPONSE" | jq -r '.abGroup // empty')

    if [ -n "$SCAN_ID" ] && [ -n "$AB_GROUP" ]; then
        if [ "$AB_GROUP" == "CONTROL" ]; then
            CONTROL_SCANS+=("$SCAN_ID")
        else
            TREATMENT_SCANS+=("$SCAN_ID")
        fi

        # Progress indicator
        if [ $((i % 10)) -eq 0 ]; then
            echo "  Generated $i/$TOTAL_USERS scans..."
        fi
    else
        echo "  Warning: Failed to create scan for user $i"
    fi

    # Rate limiting
    sleep 0.1
done

echo ""
echo "Scan generation complete!"
echo "  Control group: ${#CONTROL_SCANS[@]} scans"
echo "  Treatment group: ${#TREATMENT_SCANS[@]} scans"
echo ""

# Generate survey responses with realistic patterns
# Control group: Lower confidence rate (30-40%)
# Treatment group: Higher confidence rate (70-80%)

echo "Generating survey responses..."

# Control group surveys (30% confidence rate)
CONTROL_YES=0
for SCAN_ID in "${CONTROL_SCANS[@]}"; do
    # 30% chance of "yes" response
    HAS_CONFIDENCE=$(( RANDOM % 10 < 3 ? 1 : 0 ))
    if [ $HAS_CONFIDENCE -eq 1 ]; then
        HAS_CONFIDENCE_BOOL="true"
        ((CONTROL_YES++))
    else
        HAS_CONFIDENCE_BOOL="false"
    fi

    curl -s -X POST "$BASE_URL/api/surveys" \
        -H "Content-Type: application/json" \
        -d "{
            \"scanId\": \"$SCAN_ID\",
            \"hasConfidence\": $HAS_CONFIDENCE_BOOL
        }" > /dev/null 2>&1
done

# Treatment group surveys (75% confidence rate)
TREATMENT_YES=0
for SCAN_ID in "${TREATMENT_SCANS[@]}"; do
    # 75% chance of "yes" response
    HAS_CONFIDENCE=$(( RANDOM % 10 < 7 ? 1 : 0 ))
    if [ $HAS_CONFIDENCE -eq 1 ]; then
        HAS_CONFIDENCE_BOOL="true"
        ((TREATMENT_YES++))
    else
        HAS_CONFIDENCE_BOOL="false"
    fi

    curl -s -X POST "$BASE_URL/api/surveys" \
        -H "Content-Type: application/json" \
        -d "{
            \"scanId\": \"$SCAN_ID\",
            \"hasConfidence\": $HAS_CONFIDENCE_BOOL
        }" > /dev/null 2>&1
done

echo "  Control: $CONTROL_YES/${#CONTROL_SCANS[@]} confident"
echo "  Treatment: $TREATMENT_YES/${#TREATMENT_SCANS[@]} confident"
echo ""

# Fetch and display results
echo "=== A/B Test Results ==="
echo ""

# A/B Test Summary
echo "1. Menu Scan Distribution:"
curl -s "$BASE_URL/api/admin/ab-test/results" | jq '.'
echo ""

# Survey Analytics
echo "2. Survey Analytics (Confidence Rates):"
curl -s "$BASE_URL/api/admin/surveys/analytics" | jq '.'
echo ""

# Calculate expected ratios
if [ ${#CONTROL_SCANS[@]} -gt 0 ]; then
    CONTROL_RATE=$(echo "scale=2; $CONTROL_YES * 100 / ${#CONTROL_SCANS[@]}" | bc)
else
    CONTROL_RATE=0
fi

if [ ${#TREATMENT_SCANS[@]} -gt 0 ]; then
    TREATMENT_RATE=$(echo "scale=2; $TREATMENT_YES * 100 / ${#TREATMENT_SCANS[@]}" | bc)
else
    TREATMENT_RATE=0
fi

if [ "$CONTROL_RATE" != "0" ] && [ "$(echo "$CONTROL_RATE > 0" | bc)" -eq 1 ]; then
    RATIO=$(echo "scale=2; $TREATMENT_RATE / $CONTROL_RATE" | bc)
else
    RATIO="N/A"
fi

echo "=== Summary ==="
echo "Control confidence rate: ${CONTROL_RATE}%"
echo "Treatment confidence rate: ${TREATMENT_RATE}%"
echo "Treatment/Control ratio: ${RATIO}x"
echo ""

if [ "$RATIO" != "N/A" ] && [ "$(echo "$RATIO >= 2.0" | bc)" -eq 1 ]; then
    echo "✅ Hypothesis H3 VALIDATED: Treatment group is ${RATIO}x more confident than Control"
else
    echo "⚠️  Hypothesis H3 NOT MET: Target is 2x, achieved ${RATIO}x"
fi
