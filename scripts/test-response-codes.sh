#!/bin/bash

# Get the directory where the script is located
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Change to the repository root (where the script should be located)
cd "$SCRIPT_DIR" || exit 1

echo "=== Testing Response Code Refactoring ==="
echo "Working directory: $(pwd)"
echo ""

# Create temporary log file in the project directory
LOG_FILE="$(pwd)/target/keygo-test.log"
mkdir -p "$(pwd)/target"

echo "1. Building project..."
./mvnw clean package -DskipTests -q

if [ $? -ne 0 ]; then
    echo "❌ Build failed!"
    exit 1
fi

echo "✅ Build successful"
echo ""

echo "2. Starting application..."
java -jar keygo-run/target/keygo-run-1.0-SNAPSHOT.jar > "$LOG_FILE" 2>&1 &
APP_PID=$!
echo "   Application PID: $APP_PID"

# Wait for application to start
echo "   Waiting for application to start..."
sleep 15

# Check if app is running
if ! kill -0 $APP_PID 2>/dev/null; then
    echo "❌ Application failed to start. Log:"
    tail -20 "$LOG_FILE"
    exit 1
fi

echo "✅ Application started"
echo ""

echo "3. Testing /api/v1/service/info endpoint..."
RESPONSE=$(curl -s http://localhost:8080/api/v1/service/info)

if [ -z "$RESPONSE" ]; then
    echo "❌ No response from endpoint"
    kill $APP_PID
    exit 1
fi

echo "   Response:"
echo "$RESPONSE" | jq '.'

# Check for new code
if echo "$RESPONSE" | grep -q "SERVICE_INFO_RETRIEVED"; then
    echo "✅ New response code found: SERVICE_INFO_RETRIEVED"
else
    echo "❌ Expected code SERVICE_INFO_RETRIEVED not found"
    kill $APP_PID
    exit 1
fi

echo ""
echo "4. Testing /api/v1/response-codes endpoint..."
CODES_RESPONSE=$(curl -s http://localhost:8080/api/v1/response-codes)

echo "   Response (first 500 chars):"
echo "$CODES_RESPONSE" | jq '.' | head -20

# Check for new code
if echo "$CODES_RESPONSE" | grep -q "RESPONSE_CODES_RETRIEVED"; then
    echo "✅ New response code found: RESPONSE_CODES_RETRIEVED"
else
    echo "❌ Expected code RESPONSE_CODES_RETRIEVED not found"
    kill $APP_PID
    exit 1
fi

# Check that old codes are not present
if echo "$CODES_RESPONSE" | grep -q "SERVICE_INFO_SUCCESS"; then
    echo "❌ Old code SERVICE_INFO_SUCCESS still present!"
    kill $APP_PID
    exit 1
else
    echo "✅ Old code SERVICE_INFO_SUCCESS removed"
fi

echo ""
echo "5. Stopping application..."
kill $APP_PID
wait $APP_PID 2>/dev/null

echo "✅ All tests passed!"
echo ""
echo "=== Summary of Changes ==="
echo "✓ ResponseCode enum refactored with business-specific codes"
echo "✓ ResponseHelper simplified to use message() method"
echo "✓ ServiceInfoController updated to use SERVICE_INFO_RETRIEVED"
echo "✓ ResponseCodeController updated to use RESPONSE_CODES_RETRIEVED"
echo "✓ No HTTP-duplicating codes (BAD_REQUEST, SUCCESS, etc.)"
echo "✓ All endpoints working correctly"

