#!/bin/bash

# Test script for ServiceInfo endpoint with ResponseEntity<BaseResponse>
# Script de prueba para endpoint ServiceInfo con ResponseEntity<BaseResponse>

# Get the directory where the script is located
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Change to the repository root
cd "$SCRIPT_DIR" || exit 1

echo "=========================================="
echo "Testing ServiceInfo Endpoint"
echo "Working directory: $(pwd)"
echo "=========================================="
echo ""

# Create temporary log file in the project directory
LOG_FILE="$(pwd)/target/keygo-test-service-info.log"
mkdir -p "$(pwd)/target"

# Start application
echo "1. Starting application..."
java -jar keygo-run/target/keygo-run-1.0-SNAPSHOT.jar --server.port=8090 > "$LOG_FILE" 2>&1 &
APP_PID=$!

# Wait for startup
echo "2. Waiting for application to start..."
sleep 10

# Test endpoint
echo "3. Testing GET /api/v1/service/info"
echo ""

curl -i http://localhost:8090/keygo-server/api/v1/service/info
echo ""
echo ""

# Show formatted JSON
echo "4. Response body (formatted):"
curl -s http://localhost:8090/keygo-server/api/v1/service/info | python3 -m json.tool
echo ""

# Cleanup
echo ""
echo "5. Stopping application..."
kill $APP_PID 2>/dev/null

echo ""
echo "=========================================="
echo "Test completed!"
echo "=========================================="

