#!/bin/bash
# =========================================================
# Quick Start Script for KeyGo with Supabase
# Script de inicio rápido para KeyGo con Supabase
# =========================================================

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

echo "🚀 KeyGo + Supabase Quick Start"
echo "================================"
echo ""

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Step 1: Start local database
echo -e "${GREEN}Step 1: Starting local PostgreSQL database${NC}"
bash "$SCRIPT_DIR/db/start.sh"

echo ""
echo -e "${YELLOW}Waiting for database to be ready...${NC}"
sleep 5

# Step 2: Set environment variables
echo -e "${GREEN}Step 2: Setting environment variables${NC}"
if [ -f "$PROJECT_ROOT/.env" ]; then
    set -a; source "$PROJECT_ROOT/.env"; set +a
    echo "  ✅ Variables cargadas desde .env"
else
    export SUPABASE_URL=jdbc:postgresql://localhost:5432/keygo
    export SUPABASE_USER=postgres
    export SUPABASE_PASSWORD=postgres
    echo "  ⚠️  Sin .env — usando valores por defecto"
fi

echo "  ✅ SUPABASE_URL=${SUPABASE_URL:-jdbc:postgresql://localhost:5432/keygo}"

# Optional: SMTP for email verification (registration flow)
export SMTP_HOST="${SMTP_HOST:-localhost}"
export SMTP_PORT="${SMTP_PORT:-1025}"
export SMTP_USERNAME="${SMTP_USERNAME:-}"
export SMTP_PASSWORD="${SMTP_PASSWORD:-}"
export KEYGO_MAIL_FROM="${KEYGO_MAIL_FROM:-noreply@keygo.local}"
export KEYGO_MAIL_APP_NAME="${KEYGO_MAIL_APP_NAME:-KeyGo}"

echo "  ✅ SMTP_HOST=$SMTP_HOST:$SMTP_PORT (override with env vars)"
echo ""

# Step 3: Run migrations
echo -e "${GREEN}Step 3: Running database migrations${NC}"
bash "$SCRIPT_DIR/db/migrate.sh"

echo ""

# Step 4: Build the application
echo -e "${GREEN}Step 4: Building the application${NC}"
cd "$PROJECT_ROOT" && ./mvnw clean package -DskipTests

echo ""

# Step 5: Show migration info
echo -e "${GREEN}Step 5: Migration status${NC}"
bash "$SCRIPT_DIR/db/info.sh"

echo ""
echo "================================"
echo -e "${GREEN}🎉 Setup Complete!${NC}"
echo "================================"
echo ""
echo "▶️  To start the application, run:"
echo "   ./mvnw spring-boot:run -pl keygo-run"
echo ""
echo "🛑 To stop the database:"
echo "   ./scripts/db/stop.sh"
echo ""

